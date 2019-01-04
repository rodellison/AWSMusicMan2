package com.rodellison.musicman.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.rodellison.musicman.util.EventDataUtil;
import com.rodellison.musicman.util.PropertiesUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class YesIntentHandler implements RequestHandler {

    private static final String INTENT_NAME = "YesIntent";

    private static PropertiesUtil myProps = new PropertiesUtil();

    private static final Logger log = LogManager.getLogger(YesIntentHandler.class);

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("AMAZON.YesIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {

        log.warn("YesIntentHandler called");

        String primaryTextDisplay = "";

        Map<String, Object> attributes = input.getAttributesManager().getSessionAttributes();

        int sessionIndex = (int) attributes.get("SESSION_INDEX");
        ArrayList<String> events = (ArrayList<String>) attributes.get("SESSION_EVENTS");
        String lastSessionIntent = (String) attributes.get("LAST_SESSION_INTENT");
        String strArtistVenueValue = (String)attributes.get("ARTIST_VENUE_VALUE");
        String strMonthValue = (String)attributes.get("MONTH_VALUE");

        if (lastSessionIntent.equals("ArtistIntent"))
            primaryTextDisplay = strMonthValue != "" && strMonthValue != null ? String.format("Upcoming dates for <b>%s</b> in <b>%s</b>:<br/><br/>", strArtistVenueValue, strMonthValue) :
                    String.format("Upcoming dates for <b>%s</b>:<br/>", strArtistVenueValue);
       else
            primaryTextDisplay = strMonthValue != "" && strMonthValue != null ? String.format("Upcoming dates at <b>%s</b> in <b>%s</b>:<br/><br/>", strArtistVenueValue, strMonthValue) :
                    String.format("Upcoming dates at <b>%s</b>:<br/>", strArtistVenueValue);

        return EventDataUtil.ProcessEventData(input, sessionIndex, "", primaryTextDisplay, events, lastSessionIntent, strArtistVenueValue, strMonthValue);

    }

}
