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

        String speechText = "There's various ways to ask for Artist or Venue information. " +
                "<p>Ask a question similar to one of these:</p>" +
                " Who is coming to the Mohawk, or Where are the Rolling Stones playing in June. ";

        String repromptSpeechText1 = "<p>Ask a question similar to one of these:</p>";
        String repromptSpeechText2 = "Who is coming to the Mohawk, or Where are the Rolling Stones playing in June?";

        String primaryTextDisplay = "<b>The Music Man Help</b>.<br/><br/>";
        String secondaryTextDisplay = "Alexa, Ask The Music Man:<br/>" +
                "<font size='1'>" +
                "when {artist} are playing<br/>" +
                "when is {artist} playing<br/>" +
                "where is {artist} playing<br/>" +
                "where {artist} is playing<br/>" +
                "who is coming to {venue}<br/>" +
                "who is playing at {venue}<br/>" +
                "who's playing {venue}<br/>"+
                 "for shows coming to {venue}<br/>"+
                "for shows at {venue}<br/>"+
                "To get specific events by month, You can add 'in {month}' to any of the requests above<br/><br/>" +
                "</font>";

        return TemplatesUtil.createResponse(input, speechText,
                repromptSpeechText1 + repromptSpeechText2,
                primaryTextDisplay, secondaryTextDisplay, INTENT_NAME, true);
    }

}
