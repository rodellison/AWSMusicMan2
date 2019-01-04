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

public class FallBackIntentHandler implements RequestHandler{

    private static final String INTENT_NAME = "FallBackIntent";

    private static final Logger log = LogManager.getLogger(FallBackIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.FallbackIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        log.warn("FallBackIntentHandler called");

        String speechText = "Sorry!, The Music Man couldn't understand the question you asked. Please try asking a question similar to one of these,  " +
                "Who is coming to Staples Center, or Where is Iron Maiden playing?. " +
                "You can also say 'start over' to begin a new request, or say I'm done, to exit.";

        String repromptSpeechText1 = "Please ask a question similar to one of these:";
        String repromptSpeechText2 = "Who's coming to Staples Center, or Where is Iron Maiden playing?";
        String repromptSpeechText3 = "You can also say 'Start over' to begin a new request, or say 'I'm done', to exit.";

        String primaryTextDisplay = "<b>The Music Man Help</b>.<br/>";
        String secondaryTextDisplay = repromptSpeechText1 + "<br/><br/>" + repromptSpeechText2 + "<br/><br/>" + repromptSpeechText3;

        return TemplatesUtil.createResponse(input, speechText,
                repromptSpeechText1 + repromptSpeechText2 + repromptSpeechText3,
                primaryTextDisplay, secondaryTextDisplay, INTENT_NAME);
    }

}
