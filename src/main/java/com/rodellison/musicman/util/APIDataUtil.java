package com.rodellison.musicman.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class APIDataUtil {

    private static final Logger log = LoggerFactory.getLogger(APIDataUtil.class);

    /**
     * GetAPIRequest is used to call the external (from AWS) API to get data
     *
     * @param strRequestString
     *         String object containing the url to be requested
     *
     * @return String containing API response results (json text)
     */
    public static String GetAPIRequest(String strRequestString) {

        log.warn("Performing APIDataUtil GetAPIRequest");

        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpGet httpget = new HttpGet(strRequestString);

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
