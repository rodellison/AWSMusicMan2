package com.rodellison.musicman.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
                speechOutputBuilder.append(String.format("There are no additional events for %s. You can say 'Start over' to begin a new request, or 'I'm done', to exit.", strMonthValue));
            else
                speechOutputBuilder.append("There are no additional events. You can say 'Start over' to begin a new request, or 'I'm done', to exit.");

            repromptSpeechText1 = "There are no additional events. You can say 'Start over' to begin a new request, or 'I'm done', to exit.";
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
                primaryTextDisplay, secondaryTextDisplay, IntentName);

    }

    public static Optional<Response> returnFailSpeech(HandlerInput input, String strIntentName)
    {

        String speechText = "Sorry!, The Music Man couldn't understand your last question. Please try again, asking a question similar to one of these,  " +
                "Who is coming to Staples Center, or Where is Iron Maiden playing?. " +
                "You can also say 'start over' to begin a new request, or say I'm done, to exit.";

        String repromptSpeechText1 = "Please ask a question similar to one of these:";
        String repromptSpeechText2 = "Who's coming to Staples Center, or Where is Iron Maiden playing?";
        String repromptSpeechText3 = "You can also say 'Start over' to begin a new request, or say 'I'm done', to exit.";

        String primaryTextDisplay = "<b>The Music Man Help</b>.<br/>";
        String secondaryTextDisplay = repromptSpeechText1 + "<br/><br/>" + repromptSpeechText2 + "<br/><br/>" + repromptSpeechText3;

        return TemplatesUtil.createResponse(input, speechText,
                repromptSpeechText1 + repromptSpeechText2 + repromptSpeechText3,
                primaryTextDisplay, secondaryTextDisplay, strIntentName);
    }

    public static Optional<Response> returnNoEventDataFound(HandlerInput input, String strIntentName, String strArtistVenueValue, String strMonthValue)
    {
        String speechText = "";
        String primaryTextDisplay = "";

        if (strIntentName == "ArtistIntent")
        {
             speechText = strMonthValue != "" ? String.format("<p>I couldn't find any events where %s are playing in %s.</p>", strArtistVenueValue, strMonthValue) :
                    String.format("<p>I couldn't find any events where %s are playing.</p>", strArtistVenueValue);
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
        String repromptSpeechText1 = "You can say 'Start over' to begin a new request, or say I'm done, to exit.";
        String secondaryTextDisplay = "<br/><br/>";

        log.warn("Responding with No events found, possibly unrecognized Artist data...");
        return TemplatesUtil.createResponse(input, speechText, repromptSpeechText1 ,
                primaryTextDisplay, secondaryTextDisplay, strIntentName);
    }


}