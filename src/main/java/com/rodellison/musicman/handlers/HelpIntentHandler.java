package com.rodellison.musicman.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;
import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rodellison.musicman.util.TemplatesUtil;
// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.amazon.ask.request.Predicates.intentName;
import static com.rodellison.musicman.util.TemplatesUtil.supportsApl;

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

        String primaryTextDisplay = "The Music Man Help";
        String Text1Display = "Alexa, Ask The Music Man:";
        String Text2Display = "when {artist} are playing<br/>" +
                "when is {artist} playing<br/>" +
                "where is {artist} playing<br/>" +
                "who is coming to {venue}<br/>" +
                "who is playing at {venue}<br/>";

        String Text3Display = "To get specific events by month, You can add 'in {month}' to any of the requests above";

         if (supportsApl(input)) {
            //  ViewportProfile viewportProfile = ViewportUtils.getViewportProfile(input.getRequestEnvelope());

            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.readTree(new File("MusicManDocData.json"));
                JsonNode documentNode = node.get("document");
                JsonNode dataSourcesNode = node.get("dataSources");

                TypeReference<HashMap<String, Object>> MusicManMapType = new TypeReference<HashMap<String, Object>>() {};

                log.info("LaunchRequestHandler called, reading documentNode value");
                Map<String, Object> document = mapper.readValue(documentNode.toString(), MusicManMapType);

                log.info("LaunchRequestHandler called, reading dataSources node");
                JsonNode dataSources = mapper.readTree(dataSourcesNode.toString());

                log.info("LaunchRequestHandler called, getting properties node");
                ObjectNode MusicManTemplateProperties = (ObjectNode) dataSources.get("musicManTemplateData").get("properties");


                log.info("LaunchRequestHandler called, setting properties");

                MusicManTemplateProperties.put("LayoutToUse", "Help");
                MusicManTemplateProperties.put("HeadingText", primaryTextDisplay);
                MusicManTemplateProperties.put("EventImageUrl", "NA");
                MusicManTemplateProperties.put("HintString", "Who is coming to the Mohawk in May");
                List<String> textEvents = new ArrayList<String>();
                textEvents.add(Text1Display);
                textEvents.add(Text2Display);
                textEvents.add(Text3Display);

                ObjectMapper textEventMapper = new ObjectMapper();
                ArrayNode theArray = mapper.valueToTree(textEvents);
                MusicManTemplateProperties.putArray("EventText").addAll(theArray);

                log.info("LaunchRequestHandler called, building Render Document");

                RenderDocumentDirective documentDirective = RenderDocumentDirective.builder()
                        .withDocument(document)
                        .withDatasources(mapper.convertValue(dataSources, new TypeReference<Map<String, Object>>() {
                        }))
                        .build();

                log.info(documentDirective);

                log.info("LaunchRequestHandler called, calling responseBuilder");

                return input.getResponseBuilder()
                        .withSpeech(speechText)
                        .withReprompt(repromptSpeechText1 + repromptSpeechText2)
                        .addDirective(documentDirective)
                        .build();

            } catch (IOException e) {
                throw new AskSdkException("Unable to read or deserialize device data", e);
            }
        } else {
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptSpeechText1 + repromptSpeechText2)
                    .build();
        }



    }

}
