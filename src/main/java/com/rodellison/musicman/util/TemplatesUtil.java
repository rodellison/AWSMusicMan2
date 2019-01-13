package com.rodellison.musicman.util;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.SupportedInterfaces;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class TemplatesUtil {

    private static final Logger log = LogManager.getLogger(TemplatesUtil.class);
    private static final String CLASS_NAME = "TemplatesUtil";

    /**
     * Helper method that returns text content to be used in the standard card.
     *
     * @param text incoming text which may have rich text markup that needs removed for general home card text.
     * @return String that will be rendered for the simple or standard home card
     */
    public static String prepForSimpleStandardCardText(String text)
    {
        String returnText = text.replace("<br/>", "\n\n");
        returnText = returnText.replace("<p>", "");
        returnText = returnText.replace("</p>", "\n\n");
        returnText = returnText.replace("<s>", "");
        returnText = returnText.replace("</s>", "\n\n");
        returnText = returnText.replace("<b>", "");
        returnText = returnText.replace("</b>", "");
        returnText = returnText.replace("<font size='1'>", "");
        returnText = returnText.replace("</font>", "");
        returnText = returnText.replace("&", " and ");

        return returnText;
    }

    public static boolean supportsApl(HandlerInput input) {
        SupportedInterfaces supportedInterfaces = input.getRequestEnvelope().getContext().getSystem().getDevice().getSupportedInterfaces();
        return supportedInterfaces.getAlexaPresentationAPL() != null;
    }


}
