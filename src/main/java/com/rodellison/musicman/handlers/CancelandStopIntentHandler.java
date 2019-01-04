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

public class CancelandStopIntentHandler implements RequestHandler {

    private static final String INTENT_NAME = "CancelStopIntent";

    private static final Logger log = LogManager.getLogger(CancelandStopIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.StopIntent").or(intentName("AMAZON.CancelIntent")).or(intentName("AMAZON.NoIntent")));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        log.warn("CancelandStopIntentHandler called");

        String speechText = "Thanks for using the Music Man. Goodbye.";
        speechText += "<audio src='soundbank://soundlibrary/musical/amzn_sfx_musical_drone_intro_02'/>";
        String primaryTextDisplay = "Thanks for using <b>The Music Man</b>.<br/>";
        String secondaryTextDisplay = "<br/><br/>" + "Goodbye!";

        return TemplatesUtil.createResponse(input, speechText,
                "",
                primaryTextDisplay, secondaryTextDisplay, INTENT_NAME);

    }
}
