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
// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import static com.amazon.ask.request.Predicates.intentName;


public class ArtistIntentHandler implements RequestHandler {

    private static final Logger log = LogManager.getLogger(ArtistIntentHandler.class);
    private static final String INTENT_NAME = "ArtistIntent";
    private static final String ARTIST_SLOT = "artist";
    private static final String MONTH_SLOT = "month";
    private String strArtistID;
    private String strTheArtist;

    @Override
    public boolean canHandle(HandlerInput input) {

        return input.matches(intentName("ArtistIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {


        IntentRequest thisRequest=(IntentRequest)input.getRequestEnvelope().getRequest();

        // Get the slots from the intent.
        Map<String, Slot> slots = thisRequest.getIntent().getSlots();
        Slot myArtistSlot, myMonthSlot;
        ArrayList<String> events;

        log.warn("ArtistIntentHandler called");

        String speechText,
                primaryTextDisplay,
                strTheMonth = "";

        try {
            myArtistSlot = slots.get(ARTIST_SLOT);
            myMonthSlot = slots.get(MONTH_SLOT);

            strTheArtist = myArtistSlot.getValue().toLowerCase();

            //Edit to avoid calling Songkick API for times when Alexa may have captured background audio or someone asking
            //the music man who is playing a song...
            //e.g.. see alot of these type of requests which are in error: ArtistIntentHandler - Artist slot input recieved: play passion born
            if (strTheArtist.contains("play") && !(strTheArtist.contains("coldplay") || strTheArtist.contains("plug ")))
                return EventDataUtil.returnFailSpeech(input, INTENT_NAME);
            if (strTheArtist.contains(" song") || strTheArtist.contains(" music"))
                return EventDataUtil.returnFailSpeech(input, INTENT_NAME);

            strTheMonth = myMonthSlot.getValue();  //this can be, and may usually be null..
            if (strTheMonth == null)
                strTheMonth = "";
            else
            {
                //a few known cleanups
                strTheMonth = strTheMonth.toLowerCase().replace("mae", "May");
            }
            log.warn(strTheMonth == "" ? "Artist slot input received: " + strTheArtist : "Artist slot input received: "
                    + strTheArtist + ", " + strTheMonth);


            //This is if the user is spelling out a name, or for some names like
            strTheArtist = strTheArtist.replace(". ", "");
            strTheArtist = strTheArtist.replace(".", "");
            strTheArtist = strTheArtist.replace(" artist ", " ");
            events = getArtistDates(strTheArtist, strTheMonth);
        }
        catch (Exception ex) {
            log.warn("Exception getting Artist Slot data: " + ex.getMessage());
            return EventDataUtil.returnFailSpeech(input, INTENT_NAME);
        }

        //If weve reached this point, there WAS a value provided in the intent slot and we have now
        //made the calls for data to the API

        log.info("Process Artist event data into Speech and Cards");
        int currentIndex = 0;

        strTheArtist = EventDataUtil.toTitleCase(strTheArtist);
        strTheMonth = EventDataUtil.toTitleCase(strTheMonth);

        //There may not be any events, or the Songkick service may not recognize the value
        //If that's the case, then provide a response to user, and ask them to start a new request
        if (null == events || events.isEmpty()) {

            //Only send an SNS if events was null meaning likely bad user input.. events won't be null if there WERE events, but just not for the requested month, etc.
            if (events == null)
                SNSMessageUtil.SendSMSMessage(INTENT_NAME, "Artist: " + strTheArtist + ", Month: " + strTheMonth);
            return EventDataUtil.returnNoEventDataFound(input, INTENT_NAME, strTheArtist, strTheMonth);

        }

        speechText = strTheMonth != "" ? String.format("<p>Here is where %s is playing in %s.</p>", strTheArtist, strTheMonth) :
                String.format("<p>Here is where " + strTheArtist + " " + "is playing</p> ");

        primaryTextDisplay = strTheMonth != "" ? String.format("Upcoming dates for %s in %s:", strTheArtist, strTheMonth) :
                String.format("Upcoming dates for %s:", strTheArtist);

        return EventDataUtil.ProcessEventData(input, 0, speechText, primaryTextDisplay, events, INTENT_NAME, strTheArtist, strTheMonth, strArtistID);

    }


    /**
     * Creates a {@code ArrayList<String>} to provide the Intent service handler the events they need to formulate for saying.
     *
     * @param  strArtistValue contains the string text object to be processed
     * @return ArrayList contains the String array of respective events for Artist Calendars entries
     */
    protected ArrayList<String> getArtistDates(String strArtistValue, String strMonthValue) {

        String strArtistURLRequest, strArtistCalendarURLRequest;
        strTheArtist = EventDataUtil.cleanupKnownUserError(strArtistValue);

        //Artist names may be frequently misunderstood - query dynamodb MusicManParmTable that houses
        //a list of common failed items - e.g. government mule should be Gov't Mule when querying Songkick..
        log.warn("Query the MusicManParmTable for the Artist Value");
        //Update the strTheArtist variable with any updates (in case there was a correction available
        //in the DynamoDB table
        strTheArtist = DynamoDataUtil.queryMusicManParmTable(strTheArtist);
        log.warn("Processing Artist request for: " + strTheArtist);

        String strURLEncodedParm = "";
        try {
            strURLEncodedParm = URLEncoder.encode(strTheArtist, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            log.error("Error in URLEncoding the parm: " + strTheArtist);
            return null;
        }

       strArtistURLRequest= "http://api.songkick.com/api/3.0/search/artists.json?query=" + strURLEncodedParm + "&apikey=APIKEYVALUE";
       String responseBody = APIDataUtil.GetAPIRequest(strArtistURLRequest);

        log.info("getArtistDates: Begin JSON Processing");
        try {
           //Jackson JSON
           log.info("Creating JSONFactory and JSONParser, performing readTree");
           ObjectMapper mapper=new ObjectMapper();
           JsonFactory factory=mapper.getFactory();
           JsonParser jp=factory.createParser(responseBody);
           JsonNode input=mapper.readTree(jp);
           log.info("Completed JSONFactory and JSONParser creation and readTree");

           try {
               JsonNode myArtistNode =  input.get("resultsPage").get("results").get("artist").get(0);
               strArtistID = myArtistNode.get("id").asText();
           } catch (Exception ex) {
               log.warn("Did not find artist id for: " + strURLEncodedParm);
               return null;
           }

           //Now use the Artist ID to get their calendar of upcoming events
           strArtistCalendarURLRequest = "http://api.songkick.com/api/3.0/artists/" + strArtistID + "/calendar.json?apikey=APIKEYVALUE";
             responseBody = APIDataUtil.GetAPIRequest(strArtistCalendarURLRequest);

           jp=factory.createParser(responseBody);
           input=mapper.readTree(jp);

           try {
               int intTotalEntries = input.get("resultsPage").findValue("totalEntries").asInt();

               if (intTotalEntries > 0) {
                   ArrayList<String> strArrayArtistsCalendarEntries = new ArrayList<String>();
                   String theEvent = "";

                   JsonNode strArtistCalendarValues1 = input.get("resultsPage").get("results").findValue("event");
                   for(Iterator<JsonNode> i = strArtistCalendarValues1.iterator(); i.hasNext(); ) {
                       JsonNode item = i.next();
                       JsonNode cityItem = item.get("location");
                       theEvent = item.get("displayName").asText() + " in " + cityItem.get("city").asText().replace(", US", "");

                       int locOfAt = theEvent.lastIndexOf(" at ");
                       String tempString = theEvent.replace(" (", ", on (");
                       tempString = tempString.replace("on (CANCELLED)", " is Cancelled.");

                       strMonthValue = strMonthValue.replace(".", "");
                       if ((strMonthValue != "" && tempString.toLowerCase().contains(strMonthValue.toLowerCase()) || strMonthValue == ""))
                       {
                           if (locOfAt >= 0) {
                               if (!strArrayArtistsCalendarEntries.contains(tempString.substring(locOfAt)))
                                   strArrayArtistsCalendarEntries.add(tempString.substring(locOfAt));
                           } else {
                               if (!strArrayArtistsCalendarEntries.contains(tempString))
                                   strArrayArtistsCalendarEntries.add(tempString);
                           }
                       }
                   }
                   log.info("Returning Array of Artist Calendar events, size: " + strArrayArtistsCalendarEntries.size());
                   return strArrayArtistsCalendarEntries;

               } else {
                   //No calendar entries for this Artist
                   log.info("Returning null for Artist Calendar events");
                   return null;
               }
           } catch (Exception ex) {
               log.info("Returning null for Artist Calendar events with Exception: " + ex.getMessage());
               return null;
           }
       }
       catch (IOException ioe) {
           log.info("Completed JSONFactory and JSONParser creation and readTree with IOException");
       }
       return null;
    }

}
