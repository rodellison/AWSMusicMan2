package com.rodellison.musicman.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PropertiesUtil {

    private static final Logger log = LogManager.getLogger(PropertiesUtil.class);
    private Properties prop = null;

    public PropertiesUtil(String strCaller){

        log.info("Performing PropertiesUtil load for " + strCaller);

        InputStream is = null;
        try {
            this.prop = new Properties();
            is = this.getClass().getResourceAsStream("/config.properties");
            prop.load(is);

        } catch (FileNotFoundException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public String getPropertyValue(String key)
    {
        return this.prop.getProperty(key);
    }

}