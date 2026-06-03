package com.BEProjects.url_shortener.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


public class UrlExistenceValidator {


    private static final Logger log =  LoggerFactory.getLogger(UrlExistenceValidator.class);

    public static boolean isUrlExists(String utlString) {
        try {
            log.debug("Checking if url exists: {}" , utlString);
            URL url = new URI(utlString).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int responseCode = connection.getResponseCode();

            return (responseCode >= 200 && responseCode < 400);
        } catch(Exception e) {
            log.error("Error while checking URL: {}", utlString, e);
            return false;
        }
    }

}
