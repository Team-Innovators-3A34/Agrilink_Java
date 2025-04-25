package org.example.demo.services;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
public class TextValidator {


    private static final String API_URL = " https://nsfw-text-moderation-api.p.rapidapi.com/moderation_check.php ";
    private static final String API_KEY = "42ca578365msh13e76ef83160f1ap15c91fjsn04bd35041560"; // Remplacez par votre clé
    private static final String API_HOST = "nsfw-text-moderation-api.p.rapidapi.com";

    public static boolean isValidDescription(String text) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("X-RapidAPI-Key", API_KEY);
            conn.setRequestProperty("X-RapidAPI-Host", API_HOST);
            conn.setDoOutput(true);

            String postData = "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            if (status != 200) {
                System.out.println("Erreur HTTP " + status);
                return false;
            }

            InputStream responseStream = conn.getInputStream();
            String jsonResponse = new String(responseStream.readAllBytes(), StandardCharsets.UTF_8);

            System.out.println("Réponse RapidAPI : " + jsonResponse);

            // Analysez la réponse JSON pour déterminer la validité
            // Par exemple, si le type est "neutral" ou "negative", considérez comme invalide
            return jsonResponse.contains("\"type\":\"positive\"");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
