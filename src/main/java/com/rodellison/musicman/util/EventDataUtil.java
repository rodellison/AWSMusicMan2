package com.rodellison.musicman.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

public class EventDataUtil {

    private static final Logger log = LogManager.getLogger(EventDataUtil.class);
    private static final int PAGINATION_SIZE = 3;

    public static Optional<Response> ProcessEventData (HandlerInput input, int sessionIndex, String currentSpeechText,
                                                       String currentPrimaryText, ArrayList<String> events,
                                                       String IntentName, String strArtistVenueValue, String strMonthValue){

        log.warn("Process event data into Speech and Cards");
        Map<String, Object> attributes = input.getAttributesManager().getSessionAttributes();

        StringBuilder speechOutputBuilder = new StringBuilder();
        speechOutputBuilder.append(currentSpeechText);
        StringBuilder cardOutputBuilder = new StringBuilder();
        int currentIndex = sessionIndex;
        String speechText, repromptSpeechText1, primaryTextDisplay, secondaryTextDisplay = "";

        currentIndex = sessionIndex;
        primaryTextDisplay = currentPrimaryText;


        for (int i = 0; i < PAGINATION_SIZE; i++) {
            try {
                if (null != events.get(currentIndex)) {
                    speechOutputBuilder.append("<s>");
                    speechOutputBuilder.append(events.get(currentIndex));
                    speechOutputBuilder.append("</s>");

                    cardOutputBuilder.append("<br/>");
                    cardOutputBuilder.append(events.get(currentIndex));
                    cardOutputBuilder.append("<br/>");
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
            repromptSpeechText1 = " Would you like to hear more?";
            // If there are more than 3 events, set the count to the currentIndex and add the events
            // to the session attributes
        } else {
            if (strMonthValue != "" && strMonthValue != null)
                speechOutputBuilder.append(String.format("<p>There are no additional events for %s.</p> You can say 'Start over' to begin a new request, or 'I'm done', to exit.", strMonthValue));
            else
                speechOutputBuilder.append("<p>There are no additional events.</p> You can say 'Start over' to try a new request, or 'I'm done', to exit.");

            repromptSpeechText1 = "<p>There are no additional events.</p> You can say 'Start over' to try a new request, or 'I'm done', to exit.";
        }

        attributes.put("SESSION_INDEX", currentIndex);
        attributes.put("SESSION_EVENTS", events);
        attributes.put("LAST_SESSION_INTENT", IntentName);
        attributes.put("ARTIST_VENUE_VALUE", strArtistVenueValue);
        attributes.put("MONTH_VALUE", strMonthValue);

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

        log.info("speechText: " + speechText);
        log.info("repromptSpeechText1: " + repromptSpeechText1);
        log.info("primaryTextDisplay: " + primaryTextDisplay);
        log.info("secondaryTextDisplay: " + secondaryTextDisplay);

        return TemplatesUtil.createResponse(input, speechText,
                repromptSpeechText1,
                primaryTextDisplay, secondaryTextDisplay, IntentName, true);

    }

    public static Optional<Response> returnFailSpeech(HandlerInput input, String strIntentName)
    {

        String speechText = "Sorry!, The Music Man couldn't understand your last question. <p>Please try again, asking a question similar to one of these:</p>  " +
                "Who is coming to Staples Center, or Where is Iron Maiden playing?. " +
                "You can also say 'start over' to begin a new request, or say I'm done, to exit.";

        String repromptSpeechText1 = "<p>Please ask a question similar to one of these:</p>";
        String repromptSpeechText2 = "Who's coming to Staples Center, or Where is Iron Maiden playing?";
        String repromptSpeechText3 = "You can also say 'Start over' to begin a new request, or say 'I'm done', to exit.";

        String primaryTextDisplay = "<b>The Music Man Help</b>.<br/>";
        String secondaryTextDisplay = repromptSpeechText1 + "<br/><br/>" + repromptSpeechText2 + "<br/><br/>" + repromptSpeechText3;

        return TemplatesUtil.createResponse(input, speechText,
                repromptSpeechText1 + repromptSpeechText2 + repromptSpeechText3,
                primaryTextDisplay, secondaryTextDisplay, strIntentName, false);
    }

    public static Optional<Response> returnNoEventDataFound(HandlerInput input, String strIntentName, String strArtistVenueValue, String strMonthValue)
    {
        String speechText = "";
        String primaryTextDisplay = "";

        if (strIntentName == "ArtistIntent")
        {
             speechText = strMonthValue != "" ? String.format("<p>I couldn't find any events where %s is playing in %s.</p>", strArtistVenueValue, strMonthValue) :
                    String.format("<p>I couldn't find any events where %s is playing.</p>", strArtistVenueValue);
             primaryTextDisplay = strMonthValue != "" ? String.format("<br/>The Music Man couldn't find any upcoming events for <b> %s </b> in <b> %s </b>", strArtistVenueValue, strMonthValue) :
                    String.format("<br/>The Music Man couldn't find any upcoming events for <b> %s </b>", strArtistVenueValue);

        }
        else
        {
            speechText = strMonthValue != "" ? String.format("<p>I couldn't find any events coming to %s in %s.</p>", strArtistVenueValue, strMonthValue) :
                    String.format("<p>I couldn't find any events coming to %s.</p>", strArtistVenueValue);
            primaryTextDisplay = strMonthValue != "" ? String.format("<br/>The Music Man couldn't find any upcoming events at <b> %s </b> in <b> %s </b>", strArtistVenueValue, strMonthValue) :
                    String.format("<br/>The Music Man couldn't find any upcoming events at <b> %s </b>", strArtistVenueValue);

        }
        speechText += "If you'd like to try another search, say 'Start over', or say I'm done, to exit.";
        String repromptSpeechText1 = "<p>You can say 'Start over' to begin a new request, or say I'm done, to exit.</p>";
        String secondaryTextDisplay = "<br/><br/>";

        log.warn("Responding with No events found, possibly unrecognized Artist data...");
        return TemplatesUtil.createResponse(input, speechText, repromptSpeechText1 ,
                primaryTextDisplay, secondaryTextDisplay, strIntentName, true);
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

}