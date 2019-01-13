package com.rodellison.musicman.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.Response;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
// Import log4j classes.
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static com.amazon.ask.request.Predicates.intentName;
import static com.rodellison.musicman.util.TemplatesUtil.supportsApl;

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

        String speechText = "Ok, <p>Try asking a question similar to one of these:</p>" +
                " Who is coming to Staples Center, or Where is Iron Maiden playing in July?";

        String repromptSpeechText1 = "<p>Please ask a question similar to one of these:</p>";
        String repromptSpeechText2 = "Who's coming to Staples Center, or Where is Iron Maiden playing in July?";

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

                MusicManTemplateProperties.put("LayoutToUse", "Home");
                MusicManTemplateProperties.put("HeadingText", "Welcome to the Music Man");
                MusicManTemplateProperties.put("EventImageUrl", "NA");
                MusicManTemplateProperties.put("HintString", "Where is Iron Maiden playing in July");

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
