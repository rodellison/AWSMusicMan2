package com.rodellison.musicman.util;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SNSMessageUtil {

    private static final String CLASS_NAME = "SNSMessageUtil";
    private static final Logger log = LogManager.getLogger(SNSMessageUtil.class);
    private static PropertiesUtil myProps = new PropertiesUtil(CLASS_NAME);

    /**
     * SendSMSMessage is used to interface with AWS SNS service for the purposes of sending the value that
     * was tried by the user, but failed. This will be used to help correct data in the Parm Fixer table
     *
     * @param message String object containing the item being sent to topic as part of message.
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    public static void SendSMSMessage(String intentName, String message) {

        String topic = myProps.getPropertyValue("SNSMessageTopic");

        try {

            log.warn("Creating SNS Message");
            AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1)
                    .build();

            //publish to an SNS topic
            String msg = "Music Man user request failure from : " + intentName + ", value: " + message;
            PublishRequest publishRequest = new PublishRequest(topic, msg);
            PublishResult publishResult = snsClient.publish(publishRequest);
//print MessageId of message published to SNS topic
            log.info("MessageId - " + publishResult.getMessageId());

        } catch (Exception e) {

            log.error("SNS Message exception error: " + e.getMessage());

        }

        return;

    }
}
