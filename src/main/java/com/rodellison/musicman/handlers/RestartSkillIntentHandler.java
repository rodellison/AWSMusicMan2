package com.rodellison.musicman.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.rodellison.musicman.util.TemplatesUtil;

import java.util.Optional;
// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


import static com.amazon.ask.request.Predicates.intentName;

public class RestartSkillIntentHandler implements RequestHandler {

    private static final String INTENT_NAME = "RestartSkill";
    private static final Logger log = LogManager.getLogger(RestartSkillIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("RestartSkillIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        log.warn("RestartSkillIntentHandler called");

        String speechText = "<p>OK. Try asking a question similar to one of these:</p>" +
                "Who is coming to the Hollywood Bowl, or Where is Ariana Grande playing in June?";

        String repromptSpeechText1 = "<p>Please ask a question similar to one of these:</p>";
        String repromptSpeechText2 = "Who is coming to the Hollywood Bowl, or Where is Ariana Grande playing in June?";

        String primaryTextDisplay = "Welcome to <b>The Music Man</b>.<br/><br/>";
        String secondaryTextDisplay = repromptSpeechText1 + "<br/><br/>" + repromptSpeechText2;

        return TemplatesUtil.createResponse(input, speechText,
                repromptSpeechText1 + repromptSpeechText2,
                primaryTextDisplay, secondaryTextDisplay, INTENT_NAME, false);

    }
}
