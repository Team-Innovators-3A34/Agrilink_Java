package org.example.demo.services.ressource;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TextValidator {

    private static final String API_URL = "https://api-inference.huggingface.co/models/textattack/roberta-base-CoLA";
    private static final String API_TOKEN = "Bearer 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";

    public static boolean isValidDescription(String text) {
        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", API_TOKEN);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String payload = "{\"inputs\": \"" + text.replace("\"", "") + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // lecture de la réponse
            java.io.InputStream responseStream = conn.getInputStream();
            String jsonResponse = new String(responseStream.readAllBytes());

            System.out.println("Réponse Hugging Face : " + jsonResponse);

            // exemple simple : si "LABEL_1" (acceptable), on valide
            return jsonResponse.contains("LABEL_1");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
