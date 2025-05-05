package org.example.demo.services.recyclingpoint;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class GPTService {
    private static final String API_URL    = "https://chatgpt-42.p.rapidapi.com/aitohuman";
    private static final String API_KEY    = "c4286c5e8cmshf69303343b44530p13d059jsn5101130016d8";
    private static final int    MAX_TOKENS = 150;

    public static String askGPT(String userQuestion) throws Exception {
        String instruction = """
            You are EcoBot, an expert assistant.
            You may ONLY ever discuss:
              • Recycling
              • Sustainability
              • Waste management
              • Eco-friendly practices
              • Agriculture
            Answer the user's question ONLY within these domains.
            """;

        JSONObject body = new JSONObject()
                .put("text", instruction + "\nUser: " + userQuestion + "\nEcoBot:")
                .put("max_tokens", MAX_TOKENS);

        String requestBody = body.toString();
        System.out.println("➡️ REQUEST JSON: " + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept", "application/json")
                .header("X-RapidAPI-Key", API_KEY)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("⬅️ RESPONSE BODY: " + response.body());

        JSONObject json = new JSONObject(response.body());
        // First look for the old “text” field:
        if (json.has("text")) {
            return json.getString("text").trim();
        }
        // Otherwise grab the first element of “result”:
        else if (json.has("result")) {
            return json.getJSONArray("result")
                    .getString(0)
                    .trim();
        }
        // Errors:
        else if (json.has("error")) {
            throw new RuntimeException("Server error: " + json.getString("error"));
        }
        else {
            throw new RuntimeException("Unexpected response: " + response.body());
        }
    }
}
