package com.rodellison.musicman.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;
import com.amazon.ask.model.ui.Image;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.rodellison.musicman.util.TemplatesUtil.supportsApl;

public class EventDataUtil {

    private static String CLASS_NAME = "EventDataUtil";
    private static final Logger log = LogManager.getLogger(EventDataUtil.class);
    private static final int PAGINATION_SIZE = 3;
    private static final String SongkickArtistImageURL = "https://images.sk-static.com/images/media/profile_images/artists/ARTISTID/huge_avatar";
    private static Image myStandardCardImage;
    private static PropertiesUtil myProps = new PropertiesUtil(CLASS_NAME);


    public static Optional<Response> ProcessEventData (HandlerInput input, int sessionIndex, String currentSpeechText,
                                                       String currentPrimaryText, ArrayList<String> events,
                                                       String IntentName, String strArtistVenueValue, String strMonthValue, String ArtistID){

        log.warn("Process event data into Speech and Cards");
        Map<String, Object> attributes = input.getAttributesManager().getSessionAttributes();

        StringBuilder speechOutputBuilder = new StringBuilder();
        speechOutputBuilder.append(currentSpeechText);
        StringBuilder cardOutputBuilder = new StringBuilder();
        int currentIndex = sessionIndex;
        String speechText, repromptSpeechText, primaryTextDisplay, secondaryTextDisplay = "";

        currentIndex = sessionIndex;
        primaryTextDisplay = currentPrimaryText;

        List<String> textEvents = new ArrayList<String>();

        for (int i = 0; i < PAGINATION_SIZE; i++) {
            try {
                if (null != events.get(currentIndex)) {
                    speechOutputBuilder.append("<s>");
                    speechOutputBuilder.append(events.get(currentIndex));
                    speechOutputBuilder.append("</s>");

                    textEvents.add(events.get(currentIndex));
                    cardOutputBuilder.append(events.get(currentIndex));
                    cardOutputBuilder.append("\n");

                    currentIndex++;

                }
            } catch (Exception ex) {
                //May have been less than 3 entries, so an out of bounds may have been thrown.
                //lets just get out
            }
        }

        log.info("Process event data 2 for reprompt queue");

        if (events.size() > currentIndex) {
            speechOutputBuilder.append(" Would you like to hear more?");
            repromptSpeechText = " Would you like to hear more?";
            // If there are more than 3 events, set the count to the currentIndex and add the events
            // to the session attributes
        } else {
            if (strMonthValue != "" && strMonthValue != null)
                speechOutputBuilder.append(String.format("<p>There are no additional events for %s.</p> You can say 'Start over' to begin a new request, or 'I'm done', to exit.", strMonthValue));
            else
                speechOutputBuilder.append("<p>There are no additional events.</p> You can say 'Start over' to try a new request, or 'I'm done', to exit.");

            repromptSpeechText = "<p>There are no additional events.</p> You can say 'Start over' to try a new request, or 'I'm done', to exit.";
        }

        attributes.put("SESSION_INDEX", currentIndex);
        attributes.put("SESSION_EVENTS", events);
        attributes.put("LAST_SESSION_INTENT", IntentName);
        attributes.put("ARTIST_VENUE_VALUE", strArtistVenueValue);
        attributes.put("MONTH_VALUE", strMonthValue);
        attributes.put("ARTIST_ID", ArtistID);

        log.info("Process event data 3 final construct for TemplateUtil");

        speechText = speechOutputBuilder.toString();
        //final cleanups to remove troublesome SSML chars
        speechText = speechText.replace("(", "");
        speechText = speechText.replace(")", "");
        speechText = speechText.replace("&", " and ");

        secondaryTextDisplay = cardOutputBuilder.toString();
        secondaryTextDisplay = secondaryTextDisplay.replace("(", "");
        secondaryTextDisplay = secondaryTextDisplay.replace(")", "");
        secondaryTextDisplay = secondaryTextDisplay.replace("&", " and ");


        if (supportsApl(input)) {
            //  ViewportProfile viewportProfile = ViewportUtils.getViewportProfile(input.getRequestEnvelope());

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(new File("MusicManDocData.json"));
                JsonNode documentNode = node.get("document");
                JsonNode dataSourcesNode = node.get("dataSources");

                TypeReference<HashMap<String, Object>> MusicManMapType = new TypeReference<HashMap<String, Object>>() {};

                log.info("LaunchRequestHandler called, reading documentNode value");
                Map<String, Object> document = mapper.readValue(documentNode.toString(), MusicManMapType);

                log.info("LaunchRequestHandler called, reading dataSources node");
                JsonNode dataSources = mapper.readTree(dataSourcesNode.toString());

                log.info("LaunchRequestHandler called, getting properties node");
                ObjectNode MusicManTemplateProperties = (ObjectNode) dataSources.get("musicManTemplateData").get("properties");

                log.info("LaunchRequestHandler called, setting properties");

                MusicManTemplateProperties.put("LayoutToUse", "Events");
                MusicManTemplateProperties.put("HeadingText", primaryTextDisplay);

                //This URL only exists at Songkick for Artists,.. not venues
                if (IntentName.contains("Artist")) {
                    String SongkickArtistImageURLUpdated = SongkickArtistImageURL.replace("ARTISTID", ArtistID);
                    MusicManTemplateProperties.put("EventImageUrl", SongkickArtistImageURLUpdated);
                }
                else
                    MusicManTemplateProperties.put("EventImageUrl", "NA");

                MusicManTemplateProperties.put("HintString", "Who is coming to the Mohawk");

                ObjectMapper textEventMapper = new ObjectMapper();
                ArrayNode theArray = mapper.valueToTree(textEvents);
                MusicManTemplateProperties.putArray("EventText").addAll(theArray);

                log.info("LaunchRequestHandler called, building Render Document");

                RenderDocumentDirective documentDirective = RenderDocumentDirective.builder()
                        .withDocument(document)
                        .withDatasources(mapper.convertValue(dataSources, new TypeReference<Map<String, Object>>() {
                        }))
                        .build();

                log.info(documentDirective);

                log.info("LaunchRequestHandler called, calling responseBuilder");

                return input.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(repromptSpeechText)
                        .addDirective(documentDirective)
                        .build();

            } catch (IOException e) {
                throw new AskSdkException("Unable to read or deserialize device data", e);
            }
        } else {

            myStandardCardImage = Image.builder()
                    .withLargeImageUrl(myProps.getPropertyValue("SmallImageUrl"))
                    .withSmallImageUrl(myProps.getPropertyValue("LargeImageUrl"))
                    .build();

            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptSpeechText)
                    .withStandardCard(myProps.getPropertyValue("AppTitle"), TemplatesUtil.prepForSimpleStandardCardText(speechText), myStandardCardImage)
                    .build();
        }

    }

    public static Optional<Response> returnFailSpeech(HandlerInput input, String strIntentName)
    {

        String speechText = "Sorry!, The Music Man couldn't understand your last question. <p>Please try again, asking a question similar to one of these:</p>  " +
                "Who is coming to Staples Center, or Where is Iron Maiden playing?. " +
                "You can also say 'start over' to begin a new request, or say I'm done, to exit.";

        String repromptSpeechText1 = "<p>Please ask a question similar to one of these:</p>";
        String repromptSpeechText2 = "Who's coming to Staples Center, or Where is Iron Maiden playing?";
        String repromptSpeechText3 = "You can also say 'Start over' to begin a new request, or say 'I'm done', to exit.";

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(repromptSpeechText1 + repromptSpeechText2 + repromptSpeechText3)
                .build();
    }

    public static Optional<Response> returnNoEventDataFound(HandlerInput input, String strIntentName, String strArtistVenueValue, String strMonthValue)
    {
        String speechText = "";

        if (strIntentName == "ArtistIntent")
        {
             speechText = strMonthValue != "" ? String.format("<p>I couldn't find any events where %s is playing in %s.</p>", strArtistVenueValue, strMonthValue) :
                    String.format("<p>I couldn't find any events where %s is playing.</p>", strArtistVenueValue);

        }
        else
        {
            speechText = strMonthValue != "" ? String.format("<p>I couldn't find any events coming to %s in %s.</p>", strArtistVenueValue, strMonthValue) :
                    String.format("<p>I couldn't find any events coming to %s.</p>", strArtistVenueValue);

        }
        speechText += "If you'd like to try another search, say 'Start over', or say I'm done, to exit.";
        String repromptSpeechText1 = "<p>You can say 'Start over' to begin a new request, or say I'm done, to exit.</p>";

        log.warn("Responding with No events found, possibly unrecognized Artist data...");

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(repromptSpeechText1 )
                .build();
    }

    public static String cleanupKnownUserError(String theValue)
    {
        String cleanedUpValue =  theValue;

        cleanedUpValue = cleanedUpValue.toLowerCase().replace("u. s.", "US");   //e.g. U. S. Bank Arena should be US Bank Arena
        cleanedUpValue = cleanedUpValue.toLowerCase().replace("a. t. and t ", "AT&T");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace("a. t. and t.", "AT&T");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace("b. b. and t.", "BB&T");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace(" marina", " Arena");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace(" farina", " Arena");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace("amplitheater", "Amphitheater");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace(" today", "");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace(" tonight", "");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace(" tomorrow", "");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace(" this week", "");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace(" next week", "");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace(" this month", "");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace(" next month", "");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace("the rock group ", "");
        cleanedUpValue = cleanedUpValue.toLowerCase().replace("the music group ", "");

        return cleanedUpValue;

    }

    public static String toTitleCase(String givenString) {

        if (givenString.trim() == "")
            return "";

        String tempString = givenString;

        String[] arr = tempString.split(" ", 0);
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < arr.length; i++) {
                sb.append(Character.toUpperCase(arr[i].charAt(0)))
                        .append(arr[i].substring(1)).append(" ");
        }

        return sb.toString().trim();
    }




}