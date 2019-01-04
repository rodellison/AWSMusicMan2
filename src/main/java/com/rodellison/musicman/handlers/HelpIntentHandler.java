package com.rodellison.musicman.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.rodellison.musicman.util.TemplatesUtil;
// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class HelpIntentHandler implements RequestHandler {

    private static final String INTENT_NAME = "HelpIntent";

    private static final Logger log = LogManager.getLogger(HelpIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.HelpIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        log.warn("HelpIntentHandler called");

        String speechText = "There's various ways to ask for Artist or Venue event information. " +
                "You can ask a question similar to one of these,  " +
                " Who is coming to Staples Center, or Where is Iron Maiden playing in July?. " +
                "You can also say 'Start over' to begin a new request, or say 'I'm done' to exit.";

        String repromptSpeechText1 = "Ask a question similar to one of these:";
        String repromptSpeechText2 = "Who's coming to Staples Center, or Where is Iron Maiden playing?";
        String repromptSpeechText3 = "You can also say 'Start over' to begin a new request, or say 'I'm done', to exit.";

        String primaryTextDisplay = "<b>The Music Man Help</b>.<br/>";
        String secondaryTextDisplay = "Alexa, Ask The Music Man:<br/><br/>" +
                "<font size='1'>" +
                "when {artist} are playing<br/>" +
                "when {artist} is playing<br/>" +
                "when is {artist} playing<br/>" +
                "where is {artist} playing<br/>" +
                "where is {artist}<br/>" +
                "where are {artist} playing<br/>" +
                "where {artist} are playing<br/>" +
                "where {artist} is playing<br/>" +

                "who is coming to {venue}<br/>" +
                "who is playing at {venue}<br/>" +
                "who's coming to {venue}<br/>"+
                "who's playing at {venue}<br/>"+
                "who's playing {venue}<br/>"+
                "who's at {venue}<br/>"+
                "for events coming to {venue}<br/>"+
                "for events at {venue}<br/>"+
                "for shows coming to {venue}<br/>"+
                "for shows at {venue}<br/><br/>"+
                "To get specific events by month, You can add 'in {month}' to any of the requests above<br/><br/>"+
                "To navigate, Say 'Start over' to start a new search, or 'I'm done' if you're ready to exit." +
                "</font>";


        return TemplatesUtil.createResponse(input, speechText,
                repromptSpeechText1 + repromptSpeechText2 + repromptSpeechText3,
                primaryTextDisplay, secondaryTextDisplay, INTENT_NAME);
    }

}
