package com.rodellison.musicman.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;
import com.amazon.ask.model.ui.Image;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rodellison.musicman.util.PropertiesUtil;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Import log4j classes.
import com.rodellison.musicman.util.TemplatesUtil;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static com.amazon.ask.request.Predicates.requestType;
import static com.rodellison.musicman.util.TemplatesUtil.supportsApl;

public class LaunchRequestHandler implements RequestHandler {

    private static String CLASS_NAME = "LaunchRequestHandler";
    private static final String INTENT_NAME = "LaunchRequest";
    private static final Logger log = LogManager.getLogger(LaunchRequestHandler.class);
    private static Image myStandardCardImage;
    private static PropertiesUtil myProps = new PropertiesUtil(CLASS_NAME);


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
                        .withDatasources(mapper.convertValue(dataSources, new TypeReference<Map<String, Object>>() {}))
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

            myStandardCardImage = Image.builder()
            .withLargeImageUrl(myProps.getPropertyValue("SmallImageUrl"))
            .withSmallImageUrl(myProps.getPropertyValue("LargeImageUrl"))
            .build();

            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withReprompt(repromptSpeechText1 + repromptSpeechText2)
                    .withStandardCard(myProps.getPropertyValue("AppTitle"), TemplatesUtil.prepForSimpleStandardCardText(repromptSpeechText1 + repromptSpeechText2), myStandardCardImage)
                    .build();
        }


    }
}
