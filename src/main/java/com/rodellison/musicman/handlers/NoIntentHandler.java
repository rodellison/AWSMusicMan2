package com.rodellison.musicman.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.rodellison.musicman.util.TemplatesUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class NoIntentHandler implements RequestHandler {

    private static final String INTENT_NAME = "NoIntent";
    private static final Logger log = LogManager.getLogger(NoIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.NoIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        log.warn("NoIntentHandler called");

        String speechText = "Ok. say 'Start over' to do a new search, or 'I'm done' to exit.";

        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .build();



    }

}
