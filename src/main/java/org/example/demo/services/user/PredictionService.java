package org.example.demo.services.user;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PredictionService {

    private static final String API_URL = "http://localhost:5001/predict";

    public String predict(String jsonInput) {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonInput))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return extractCrop(response.body());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Simple JSON parsing without external libraries
    private String extractCrop(String json) {
        try {
            int index = json.indexOf("recommended_crop");
            if (index == -1) return null;

            int startQuote = json.indexOf("\"", index + 18);
            int endQuote = json.indexOf("\"", startQuote + 1);
            return json.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
