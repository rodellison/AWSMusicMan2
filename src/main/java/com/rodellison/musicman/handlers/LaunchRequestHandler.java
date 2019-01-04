package com.rodellison.musicman.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.rodellison.musicman.util.TemplatesUtil;
import java.util.Optional;

// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static com.amazon.ask.request.Predicates.requestType;

public class LaunchRequestHandler implements RequestHandler {

    private static final String INTENT_NAME = "LaunchRequest";
    private static final Logger log = LogManager.getLogger(LaunchRequestHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(requestType(LaunchRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        log.warn("LaunchRequestHandler called");

        String speechText = "<audio src='soundbank://soundlibrary/musical/amzn_sfx_musical_drone_intro_02'/>";
        speechText += "Hello!, The Music Man can tell you where an artist is playing, or who's coming to a " +
                "particular venue. <p>Try asking a question similar to one of these:</p>" +
                " Who is coming to Staples Center, or Where is Iron Maiden playing in July?";

        String repromptSpeechText1 = "<p>Please ask a question similar to one of these:</p>";
        String repromptSpeechText2 = "Who's coming to Staples Center, or Where is Iron Maiden playing in July?";

        String primaryTextDisplay = "Welcome to <b>The Music Man</b>.<br/><br/>";
        String secondaryTextDisplay = repromptSpeechText1 + "<br/><br/>" + repromptSpeechText2;

        return TemplatesUtil.createResponse(input, speechText,
                repromptSpeechText1 + repromptSpeechText2,
                primaryTextDisplay, secondaryTextDisplay, INTENT_NAME, true);

    }
}
