package com.rodellison.musicman.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.display.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.*;

public class TemplatesUtil {


    private static final Logger log = LogManager.getLogger(TemplatesUtil.class);
    private static final String CLASS_NAME = "TemplatesUtil";
    private static PropertiesUtil myProps;

    static {
        myProps = new PropertiesUtil(CLASS_NAME);
    }

    /**
     * main method to create a response, handling both ask and tell type responses..
     *
     * @param input         input passed from the Handler class
     * @param speechText    the speech text to be used as the main message to the user
     * @param responseSpeechText the response speech text to be presented to the user if no verbal input is provided
     * @param primaryTextDisplay        the main text to display on the Visible interface for user e.g. Show, Spot
     * @param secondaryTextDisplay         the secondary text to display on the Visible interface for user e.g. Show, Spot
     * @return Response the response object to provide back to AWS/Alexa service
     */
    public static Optional<Response> createResponse(HandlerInput input, String speechText, String responseSpeechText,
                                                    String primaryTextDisplay, String secondaryTextDisplay, String IntentName, Boolean DisplayCard) {


        log.warn("TemplatesUtil called");

        //Establish the config session attributes if they aren't present
        Map<String, Object> attributes = input.getAttributesManager().getSessionAttributes();

        switch (IntentName)
        {
            //Initialize Reset some Attribute values here, based on if we're launching new, or starting over
            case "CancelStopIntent":
            case "LaunchRequest":
            case "RestartSkill":
                attributes.put("SESSION_INDEX", 0);
                attributes.put("SESSION_EVENTS", "");
            default:
                attributes.put("LAST_SESSION_INTENT", IntentName);
                break;
        }

        input.getAttributesManager().setSessionAttributes(attributes);

        String title = myProps.getPropertyValue("AppTitle");
        String LargeImageUrl = myProps.getPropertyValue("LargeImageUrl");
        String SmallImageUrl = myProps.getPropertyValue("SmallImageUrl");
        String ActionImageUrl = myProps.getPropertyValue("ActionImageUrl");
        String actionText = "<br/><br/><img src='" + ActionImageUrl + "' width='121' height='34' alt='Alexa Skill Action Image' />";

        Image image = getImage(LargeImageUrl);

        log.info("TemplatesUtil calling getBodyTemplate3");
        Template template = getBodyTemplate3(title, prepForTemplateCardText(primaryTextDisplay),
                prepForTemplateCardText(secondaryTextDisplay + actionText), image);

        log.info("TemplatesUtil checking responseSpeechText");
        //This should be the end, as with no response, the skill should be done..
        //removing the .withReprompt
        // Device supports display interface
        if (responseSpeechText == "")
            if (null != input.getRequestEnvelope().getContext().getDisplay()) {
                if (DisplayCard)
                {
                    log.info("TemplatesUtil returning Tell response for Display with card");
                    return input.getResponseBuilder()
                            .withSpeech(speechText)
                            .withSimpleCard(title, prepForSimpleStandardCardText(primaryTextDisplay + secondaryTextDisplay))
                            .addRenderTemplateDirective(template)
                            .build();
                }
                else
                {
                    log.info("TemplatesUtil returning Tell response for Display but no card");
                    return input.getResponseBuilder()
                            .withSpeech(speechText)
                            .build();
                }
            } else {
                //Headless device
                if (DisplayCard) {
                    log.info("TemplatesUtil returning Tell response for Headless with card");
                    com.amazon.ask.model.ui.Image standardUICardImage = com.amazon.ask.model.ui.Image.builder()
                            .withSmallImageUrl(SmallImageUrl)
                            .withLargeImageUrl(LargeImageUrl)
                            .build();
                    return input.getResponseBuilder()
                            .withSpeech(speechText)
                            .withStandardCard(title, prepForSimpleStandardCardText(primaryTextDisplay + secondaryTextDisplay), standardUICardImage)
                            .build();
                }
                else
                {
                    log.info("TemplatesUtil returning Tell response for Headless with no card");
                    return input.getResponseBuilder()
                            .withSpeech(speechText)
                             .build();
                }
            }
        else {
            // Device supports display interface
            if (null != input.getRequestEnvelope().getContext().getDisplay()) {
                if (DisplayCard) {
                    log.info("TemplatesUtil returning Ask response for Display with Card");
                    return input.getResponseBuilder()
                            .withSpeech(speechText)
                            .withSimpleCard(title, prepForSimpleStandardCardText(primaryTextDisplay + secondaryTextDisplay))
                            .addRenderTemplateDirective(template)
                            .withReprompt(responseSpeechText)
                            .build();
                }
                else
                {
                    log.info("TemplatesUtil returning Ask response for Display with no Card");
                    return input.getResponseBuilder()
                            .withSpeech(speechText)
                            .withReprompt(responseSpeechText)
                            .build();
                }
            } else {
                //Headless device
                if (DisplayCard) {
                    log.info("TemplatesUtil returning Ask response for Headless with Card");
                    com.amazon.ask.model.ui.Image standardUICardImage = com.amazon.ask.model.ui.Image.builder()
                            .withSmallImageUrl(SmallImageUrl)
                            .withLargeImageUrl(LargeImageUrl)
                            .build();
                    return input.getResponseBuilder()
                            .withSpeech(speechText)
                            .withStandardCard(title, prepForSimpleStandardCardText(primaryTextDisplay + secondaryTextDisplay), standardUICardImage)
                            .withReprompt(responseSpeechText)
                            .build();
                }
                else
                {
                    log.info("TemplatesUtil returning Ask response for Headless with no Card");
                     return input.getResponseBuilder()
                            .withSpeech(speechText)
                            .withReprompt(responseSpeechText)
                            .build();
                }
            }
        }
    }

    /**
     * Helper method to create a body template 3
     *
     * @param title         the title to be displayed on the template
     * @param primaryText   the primary text to be displayed on the template
     * @param secondaryText the secondary text to be displayed on the template
     * @param image         the url of the image
     * @return Template
     */
    public static Template getBodyTemplate3(String title, String primaryText, String secondaryText, Image image) {
        return BodyTemplate3.builder()
                .withImage(image)
                .withTitle(title)
                .withTextContent(getTextContent(primaryText, secondaryText))
                .build();
    }

    /**
     * Helper method to create a body template 6
     *
     * @param primaryText   the primary text to be displayed in the template on the show
     * @param secondaryText the secondary text to be displayed in the template on the show
     * @param image         the url of the image
     * @return Template
     */
    public static Template getBodyTemplate6(String primaryText, String secondaryText, Image image) {
        return BodyTemplate6.builder()
                .withBackgroundImage(image)
                .withTextContent(getTextContent(primaryText, secondaryText))
                .build();
    }

    /**
     * Helper method to create the image object for display interfaces
     *
     * @param imageUrl the url of the image
     * @return Image that is used in a body template
     */
    public static Image getImage(String imageUrl) {
        List<ImageInstance> instances = getImageInstance(imageUrl);
        return Image.builder()
                .withSources(instances)
                .build();
    }

    /**
     * Helper method to create List of image instances
     *
     * @param imageUrl the url of the image
     * @return instances that is used in the image object
     */
    private static List<ImageInstance> getImageInstance(String imageUrl) {
        List<ImageInstance> instances = new ArrayList<>();
        ImageInstance instance = ImageInstance.builder()
                .withUrl(imageUrl)
                .build();
        instances.add(instance);
        return instances;
    }

    /**
     * Helper method that returns text content to be used in the body template.
     *
     * @param primaryText
     * @param secondaryText
     * @return RichText that will be rendered with the body template
     */
    private static TextContent getTextContent(String primaryText, String secondaryText) {
        return TextContent.builder()
                .withPrimaryText(makeRichText(primaryText))
                .withSecondaryText(makeRichText(secondaryText))
                .build();
    }

    /**
     * Helper method that returns the rich text that can be set as the text content for a body template.
     *
     * @param text The string that needs to be set as the text content for the body template.
     * @return RichText that will be rendered with the body template
     */
    private static RichText makeRichText(String text) {
        return RichText.builder()
                .withText(text)
                .build();
    }

    /**
     * Helper method that returns text content to be used in the body template.
     *
     * @param text incoming text which may have rich text markup that needs removed for general home card text.
     * @return String that will be rendered for the simple or standard home card
     */
    private static String prepForSimpleStandardCardText(String text)
    {
        String returnText = text.replace("<br/>", "\n");
        returnText = returnText.replace("<p>", "");
        returnText = returnText.replace("</p>", "\n");
        returnText = returnText.replace("<b>", "");
        returnText = returnText.replace("</b>", "");
        returnText = returnText.replace("<font size='1'>", "");
        returnText = returnText.replace("</font>", "");
        returnText = returnText.replace("&", " and ");

        return returnText;
    }
    /**
     * Helper method that returns text content to be used in the body template.
     *
     * @param text incoming text which may have speech markup that needs removed for template card text.
     * @return String that will be rendered for the simple or standard home card
     */
    private static String prepForTemplateCardText(String text)
    {
        String returnText = text.replace("<p>", "");
        returnText = returnText.replace("</p>", "");
        returnText = returnText.replace("<s>", "");
        returnText = returnText.replace("</s>", "");
        returnText = returnText.replace("&", " and ");

        return returnText;
    }


}
