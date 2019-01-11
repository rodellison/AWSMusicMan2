package com.rodellison.musicman.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.Slot;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodellison.musicman.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static com.amazon.ask.request.Predicates.intentName;

// Import log4j classes.

public class VenueIntentHandler implements RequestHandler {

    private static final Logger log = LogManager.getLogger(VenueIntentHandler.class);
    private static final String INTENT_NAME = "VenueIntent";
    private static final String VENUE_SLOT = "venue";
    private static final String MONTH_SLOT = "month";
    private String strOriginalVenueValue = "";

    @Override
    public boolean canHandle(HandlerInput input) {

        return input.matches(intentName("VenueIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        IntentRequest thisRequest = (IntentRequest) input.getRequestEnvelope().getRequest();

        // Get the slots from the intent.
        Map<String, Slot> slots = thisRequest.getIntent().getSlots();
        Slot myVenueSlot, myMonthSlot;
        ArrayList<String> events;

        log.warn("VenueIntentHandler called");

        String speechText,
                primaryTextDisplay,
                strTheVenue, strTheMonth = "";


        try {
            myVenueSlot = slots.get(VENUE_SLOT);
            myMonthSlot = slots.get(MONTH_SLOT);

            strOriginalVenueValue = myVenueSlot.getValue();
            strTheVenue = strOriginalVenueValue.toLowerCase();
            strTheMonth = myMonthSlot.getValue();  //this can be, and may usually be null..

            if (strTheMonth == null)
                strTheMonth = "";
            else
            {
                //a few known cleanups
                strTheMonth = strTheMonth.toLowerCase().replace("mae", "May");
            }

            log.warn(strTheMonth == "" ? "Venue slot input recieved: " + strTheVenue : "Venue slot input recieved: " + strTheVenue + ", " + strTheMonth);
            events = getVenueDates(strTheVenue, strTheMonth);
        } catch (Exception ex) {

            log.warn("Exception getting Venue Slot data: " + ex.getMessage());
            return EventDataUtil.returnFailSpeech(input, INTENT_NAME);
        }

        //If weve reached this point, there WAS a value provided in the intent slot and we have now
        //made the calls for data to the API

        log.info("Process Venue event data into Speech and Cards");
        int currentIndex = 0;

        //There may not be any events, or the Songkick service may not recognize the value
        //If that's the case, then provide a response to user, and ask them to start a new request
        if (null == events || events.isEmpty()) {
            //Only send an SNS if events was null.. events won't be null if there WERE events, but just not for the requested month, etc.
            if (events == null)
                SNSMessageUtil.SendSMSMessage(INTENT_NAME, "Venue: " + strOriginalVenueValue + ", Month: " + strTheMonth);
            return EventDataUtil.returnNoEventDataFound(input, INTENT_NAME, strOriginalVenueValue, strTheMonth);
        }

        speechText = strTheMonth != "" ? String.format("<p>Here are upcoming events at %s in %s.</p>", strOriginalVenueValue, strTheMonth) :
                String.format("<p>Here are upcoming events at " + strOriginalVenueValue + "</p>");
        primaryTextDisplay = strTheMonth != "" ? String.format("Upcoming dates at <b>%s</b> in <b>%s</b>:<br/><br/>", strOriginalVenueValue, strTheMonth) :
                String.format("Upcoming dates at <b>%s</b>:<br/>", strOriginalVenueValue);

        return EventDataUtil.ProcessEventData(input, 0, speechText, primaryTextDisplay, events, INTENT_NAME, strTheVenue, strTheMonth);

    }

    /**
     * Creates a {@code ArrayList<String>} to provide the Intent service handler the events they need to formulate for saying.
     *
     * @param strVenueValue contains the string text object to be processed
     * @return ArrayList contains the String array of respective events for Venues Calendar entries
     */
    protected ArrayList<String> getVenueDates(String strVenueValue, String strMonthValue) {

        String strTheVenue, strVenueURLRequest, strVenueCalendarURLRequest;

        //Some venue names and utterences can be misunderstood
        //a list of common failed items, and sometimes when the user inadvertantly includes things like 'tonight, next week, etc.'
        // e.g. madison square gardeners should be madison square gardens
        strTheVenue = EventDataUtil.cleanupKnownUserError(strVenueValue);

        //Update the value used in the speech with any corrections that may have been made
        log.info("Begin query the MusicManParmTable for the ArtistVenue Value");
        strTheVenue = DynamoDataUtil.queryMusicManParmTable(strTheVenue);
        //Quick check here to see if a swap value was present. If it was, then change our original value so that it
        //will appear corrected in display cards.
        if (strTheVenue.toLowerCase() != strOriginalVenueValue.toLowerCase())
            strOriginalVenueValue = strTheVenue;
        log.info("End query the MusicManParmTable");

        String strURLEncodedParm = "";
        try {
            strURLEncodedParm = URLEncoder.encode(strTheVenue, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error("Error in URLEncoding the parm: " + strTheVenue);
            return null;
        }

        strVenueURLRequest = "http://api.songkick.com/api/3.0/search/venues.json?query=" + strURLEncodedParm + "&apikey=APIKEYVALUE";

        String strVenueID = "";
        String responseBody = APIDataUtil.GetAPIRequest(strVenueURLRequest);

        log.info("getVenueDates: Begin JSON Processing");
        try {
            //Jackson JSON
            log.info("Creating JSONFactory and JSONParser, performing readTree");
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getFactory();
            JsonParser jp = factory.createParser(responseBody);
            JsonNode input = mapper.readTree(jp);
            log.info("Completed JSONFactory and JSONParser creation and readTree");

            try {
                JsonNode myVenueNode = input.get("resultsPage").get("results").get("venue").get(0);
                strVenueID = myVenueNode.get("id").asText();
            } catch (Exception ex) {
                log.warn("Did not find venue id for: " + strURLEncodedParm);
                return null;
            }

            //Now use the Venue ID to get their calendar of upcoming events
            strVenueCalendarURLRequest = "http://api.songkick.com/api/3.0/venues/" + strVenueID + "/calendar.json?apikey=APIKEYVALUE";
            responseBody = APIDataUtil.GetAPIRequest(strVenueCalendarURLRequest);

            jp = factory.createParser(responseBody);
            input = mapper.readTree(jp);

            try {
                int intTotalEntries = input.get("resultsPage").findValue("totalEntries").asInt();

                if (intTotalEntries > 0) {
                    ArrayList<String> strArrayVenuesCalendarEntries = new ArrayList<String>();
                    String theEvent = "";

                    JsonNode strVenueCalendarValues1 = input.get("resultsPage").get("results").findValue("event");
                    for (Iterator<JsonNode> i = strVenueCalendarValues1.iterator(); i.hasNext(); ) {
                        JsonNode item = i.next();
                        theEvent = item.get("displayName").asText();

                        //Remove the venue name included in the calendar of responses
                        int loc1 = theEvent.indexOf(" at ");
                        int loc2 = theEvent.indexOf(" (");

                        if (loc1 > 1 && loc2 > loc1) {
                            //found the venue included in the response... at <some location> (
                            //e.g. Buckethead at Cain's Ballroom (June 13, 2016)
                            //Remove the ' at Cain's Ballroom ' part.
                            theEvent = theEvent.substring(0, loc1) + ", on " + theEvent.substring(loc2);
                        }
                        strMonthValue = strMonthValue.replace(".", "");
                        if ((strMonthValue != "" && theEvent.toLowerCase().contains(strMonthValue.toLowerCase()) || strMonthValue == "")) {
                            if (!strArrayVenuesCalendarEntries.contains(theEvent))
                                strArrayVenuesCalendarEntries.add(theEvent);
                        }
                    }
                    return strArrayVenuesCalendarEntries;
                } else {
                    //No calendar entries for this Venue
                    log.info("Returning null for Venue Calendar events");
                    return null;
                }
            } catch (Exception ex) {
                log.info("Returning null for Venue Calendar events with Exception: " + ex.getMessage());
                return null;
            }
        } catch (IOException ioe) {
            log.info("Completed JSONFactory and JSONParser creation and readTree with IOException");
        }
        return null;
    }

}
