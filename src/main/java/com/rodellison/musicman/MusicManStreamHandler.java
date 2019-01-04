package com.rodellison.musicman;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.SkillStreamHandler;
import com.rodellison.musicman.handlers.*;
import com.rodellison.musicman.util.PropertiesUtil;

public class MusicManStreamHandler extends SkillStreamHandler {

    private static Skill getSkill() {

        PropertiesUtil myProps = new PropertiesUtil();

        return Skills.standard()
                .addRequestHandlers(
                        new ArtistIntentHandler(),
                        new VenueIntentHandler(),
                        new YesIntentHandler(),
                        new NoIntentHandler(),
                        new CancelandStopIntentHandler(),
                        new RestartSkillIntentHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new SessionEndedRequestHandler(),
                        new FallBackIntentHandler())
                // Add your skill id below and uncomment to enable skill ID verification
                .withSkillId(myProps.getPropertyValue("SkillID"))
                .build();
    }

    public MusicManStreamHandler() {
        super(getSkill());
    }

}
