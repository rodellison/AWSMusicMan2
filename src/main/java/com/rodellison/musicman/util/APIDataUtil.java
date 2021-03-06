package com.rodellison.musicman.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
// Import log4j classes.
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;

public class APIDataUtil {

    private static final String CLASS_NAME = "APIDataUtil";
    private static final Logger log = LogManager.getLogger(APIDataUtil.class);
    private static PropertiesUtil myProps = new PropertiesUtil(CLASS_NAME);

    /**
     * GetAPIRequest is used to call the external (from AWS) API to get data
     *
     * @param strRequestString
     *         String object containing the url to be requested
     *
     * @return String containing API response results (json text)
     */
    public static String GetAPIRequest(String strRequestString) {


        String strAPIKey = myProps.getPropertyValue("apikey");
        String strTheRequestString = strRequestString.replace("APIKEYVALUE", strAPIKey);

        log.warn("Performing APIDataUtil GetAPIRequest : " + strTheRequestString);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpget = new HttpGet(strTheRequestString);

        // Create a response handler
        ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

            @Override
            public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    log.warn("Unexpected status returned from httpGet call " + status);
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            }
        };

        try {
            log.info("Executing httpclient request");
            String responseBody = httpclient.execute(httpget, responseHandler);
            log.info("httpclient request returned");
            httpclient.close();
            return responseBody;

        } catch (ClientProtocolException cpe) {
            log.error("APIDataUtil:ClientProtocolException occurred: " + cpe.getMessage());
            return null;
        } catch (IOException ioe) {
            log.error("APIDataUtil:IOException occurred: " + ioe.getMessage());
            return null;

        }
    }


}
