package org.example.demo.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {
    private static Properties properties = null;

    private static void loadProperties() {
        if (properties == null) {
            properties = new Properties();
            try {
                //load from resources directory
                InputStream input = ConfigUtil.class.getClassLoader().getResourceAsStream("config.properties");

                // ken l9inehech 8ady nda5el l path kol
                if (input == null) {
                    input = new FileInputStream("src/main/resources/config.properties");
                }

                properties.load(input);
                input.close();
            } catch (IOException e) {
                System.err.println("Failed to load configuration: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static String getHuggingFaceApiKey() {
        loadProperties();
        return properties.getProperty("huggingface.api.key");
    }
    public static String getHuggingFaceApiKeyTip() {
        loadProperties();
        return properties.getProperty("huggingface.api.keyfortip");
    }
    public static String getTemporaryFilePath() {
        return System.getProperty("java.io.tmpdir");
    }
    public static String getSentimentApiKey() {
        loadProperties();
        return properties.getProperty("sentiment.api.key");
    }

}