package com.rodellison.musicman.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.rodellison.musicman.util.TemplatesUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class NoIntentHandler implements RequestHandler {

    private static final int PAGINATION_SIZE = 3;

    private static final String INTENT_NAME = "NoIntent";

    private static final Logger log = LogManager.getLogger(NoIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.NoIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        log.warn("NoIntentHandler called");

        String speechText,
                repromptSpeechText1,
                repromptSpeechText2,
                repromptSpeechText3,
                primaryTextDisplay,
                secondaryTextDisplay = "";

        Map<String, Object> attributes = input.getAttributesManager().getSessionAttributes();
        //If we've not yet established any attributes already established, it means we're coming into this intent very out of sequence.
        //Tell the user to essentially start at the top.
        if (attributes.get("AppTitle") == null) {

            speechText = "Sorry!, The Music Man couldn't understand the question you asked. Please try asking a question similar to one of these,  " +
                    "Who is coming to Staples Center, or Where is Iron Maiden playing?. " +
                    "You can also say 'start over' to begin a new request, or say I'm done, to exit.";

            repromptSpeechText1 = "Please ask a question similar to one of these:";
            repromptSpeechText2 = "Who's coming to Staples Center, or Where is Iron Maiden playing?";
            repromptSpeechText3 = "You can also say 'Start over' to begin a new request, or say 'I'm done', to exit.";

            primaryTextDisplay = "<b>The Music Man</b>.<br/>";
            secondaryTextDisplay = repromptSpeechText1 + "<br/><br/>" + repromptSpeechText2 + "<br/><br/>" + repromptSpeechText3;

            return TemplatesUtil.createResponse(input, speechText,
                    repromptSpeechText1,
                    primaryTextDisplay, secondaryTextDisplay, INTENT_NAME);
        }

        speechText = "Ok. If you would like to do a new search, say 'Start over', or say 'I'm done' to exit.";
        repromptSpeechText1 = speechText;

        primaryTextDisplay = "<b>The Music Man</b>.<br/>";
        secondaryTextDisplay = speechText;


        return TemplatesUtil.createResponse(input, speechText,
                repromptSpeechText1,
                primaryTextDisplay, secondaryTextDisplay, INTENT_NAME);



    }

}