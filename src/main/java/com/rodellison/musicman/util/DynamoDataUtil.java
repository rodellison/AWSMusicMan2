package com.rodellison.musicman.util;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
public class DynamoDataUtil {

    private static final String CLASS_NAME = "DynamoDataUtil";
    private static final Logger log = LogManager.getLogger(DynamoDataUtil.class);
    private static PropertiesUtil myProps;

    static {
        myProps = new PropertiesUtil(CLASS_NAME);
    }

    /**
     * queryMusicManParmTable is used to interface with a dynamoDB table for the purposes of locating a corrected text value of something
     * Alexa may have captured from the user incorrectly. Some input as captured by Alexa is not valid for sending to Songkick, so
     *            it needs to be converted to an accurate parm. e.g. Alexa may interpret the users request for artist 'Government Mule', but
     *            Songkick recognizes the artist as Gov't Mule. This table provides the swap value.

     *
     * @param strArtistValue
     *            String object containing the incoming text (as captured by Alexa).
     *
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    public static String queryMusicManParmTable(String strArtistValue) {

        String strDynamoDBTableName = myProps.getPropertyValue("DynamoDBTable");

        String strTextValue = strArtistValue.toLowerCase();
        try {

            log.info("Creating AmazonDynamoDBClient");

            AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                    .withRegion(Regions.US_EAST_1)
                    .build();

            DynamoDB dynamoDB = new DynamoDB(client);

            Table table = dynamoDB.getTable(strDynamoDBTableName);
            log.info("Finished creating DynamoDB Client, and connecting table to DynamoDB MusicManParmTable");


            Item item = table.getItem("SongKickInvalidParm", strTextValue);
            if (null!=item) {
                String theReturnedValue = item.getString("SongKickValidParm");
                log.warn("Found an item to swap: " + theReturnedValue);
                strTextValue =  theReturnedValue;
            } else {
                log.warn("Did not find a value to swap for " + strTextValue);
            }

            dynamoDB.shutdown();
            client.shutdown();

        }
        catch (Exception e) {

            log.error("DynamoDB exception error: " + e.getMessage());
            //Item not found, or an error - in either case, just return what the user had provided
        }

        return strTextValue;

    }
}
