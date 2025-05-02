package org.example.demo.services.posts;

import org.example.demo.models.Posts;
import  org.example.demo.utils.ConfigUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class PostSentimentAPI {
    private final String API_URL;
    private final String API_KEY;
    private final HttpClient client;

    // if 1 fails nestaamlou 2 5ater mara ye5dem mara error
    private static final String PRIMARY_MODEL = "https://api-inference.huggingface.co/models/cardiffnlp/twitter-xlm-roberta-base-sentiment";
    private static final String FALLBACK_MODEL = "https://api-inference.huggingface.co/models/distilbert-base-uncased-finetuned-sst-2-english";

    private final Map<String, SentimentResult> sentimentCache = new HashMap<>();

    // sentiment label and score
    public static class SentimentResult {
        private final String label;
        private final double score;

        public SentimentResult(String label, double score) {
            this.label = label;
            this.score = score;
        }

        public String getLabel() {
            return label;
        }

        public double getScore() {
            return score;
        }
    }

    public PostSentimentAPI() {
        this.API_URL = PRIMARY_MODEL;
        this.API_KEY = ConfigUtil.getSentimentApiKey();
        this.client = HttpClient.newHttpClient();
    }

    public boolean analyzeAndUpdatePostSentiment(Posts post) {
        if (post == null || post.getDescription() == null || post.getDescription().trim().isEmpty()) {
            post.updateSentiment(Posts.SENTIMENT_NEUTRAL, 0.5);
            return false;
        }

        try {
            String text = post.getDescription();

            // nchoufou lcache ken fama 7aja 9dyma nupdatiw
            if (sentimentCache.containsKey(text)) {
                SentimentResult result = sentimentCache.get(text);
                post.updateSentiment(result.getLabel(), result.getScore());
                return true;
            }

            // API call
            SentimentResult result = analyzeSentiment(text);
            post.updateSentiment(result.getLabel(), result.getScore());
            // Cache the result
            sentimentCache.put(text, result);

            return true;
        } catch (Exception e) {
            post.updateSentiment(Posts.SENTIMENT_NEUTRAL, 0.5);
            return false;
        }
    }

    public SentimentResult analyzeSentiment(String text) {
        try {

            JSONObject requestBody = new JSONObject();
            requestBody.put("inputs", text);

            // Try primary model first
            SentimentResult result = callSentimentAPI(API_URL, requestBody);

            if (result == null) {
                result = callSentimentAPI(FALLBACK_MODEL, requestBody);
            }

            if (result == null) {
                result = performSimpleSentimentAnalysis(text);
            }

            return result;
        } catch (Exception e) {
            return new SentimentResult(Posts.SENTIMENT_NEUTRAL, 0.5);
        }
    }

    private SentimentResult callSentimentAPI(String apiUrl, JSONObject requestBody) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            // request+response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the API call was successful
            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                return null;
            }
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    private SentimentResult parseResponse(String responseBody) {
        try {
            if (responseBody.startsWith("[")) {
                JSONArray results = new JSONArray(responseBody);

                String highestSentiment = Posts.SENTIMENT_NEUTRAL;
                double highestScore = 0.5;

                for (int i = 0; i < results.length(); i++) {
                    JSONObject prediction = results.getJSONObject(i);
                    String label = prediction.getString("label");
                    double score = prediction.getDouble("score");

                    if (score > highestScore) {
                        highestScore = score;
                        if (label.toLowerCase().contains("positive")) {
                            highestSentiment = Posts.SENTIMENT_POSITIVE;
                        } else if (label.toLowerCase().contains("negative")) {
                            highestSentiment = Posts.SENTIMENT_NEGATIVE;
                        } else {
                            highestSentiment = Posts.SENTIMENT_NEUTRAL;
                        }
                    }
                }

                return new SentimentResult(highestSentiment, highestScore);
            } else {
                // response
                JSONArray results = new JSONArray(responseBody);
                JSONObject firstResult = results.getJSONObject(0);

                String highestSentiment = Posts.SENTIMENT_NEUTRAL;
                double highestScore = 0.5;

                for (String key : firstResult.keySet()) {
                    double score = firstResult.getDouble(key);

                    if (score > highestScore) {
                        highestScore = score;
                        if (key.toLowerCase().contains("positive") || key.toLowerCase().contains("pos")) {
                            highestSentiment = Posts.SENTIMENT_POSITIVE;
                        } else if (key.toLowerCase().contains("negative") || key.toLowerCase().contains("neg")) {
                            highestSentiment = Posts.SENTIMENT_NEGATIVE;
                        } else {
                            highestSentiment = Posts.SENTIMENT_NEUTRAL;
                        }
                    }
                }

                return new SentimentResult(highestSentiment, highestScore);
            }
        } catch (Exception e) {
            return new SentimentResult(Posts.SENTIMENT_NEUTRAL, 0.5);
        }
    }

    private SentimentResult performSimpleSentimentAnalysis(String text) {
        // Null check
        if (text == null || text.trim().isEmpty()) {
            return new SentimentResult(Posts.SENTIMENT_NEUTRAL, 0.5);
        }

        // Basic prep - convert to lowercase
        String lowerText = text.toLowerCase().trim();

        // Print the text we're analyzing - DEBUGGING
        System.out.println("ANALYZING TEXT: " + lowerText);

        /////////////////////////////////////////////
        // DIRECT CHECK FOR SPECIFIC NEGATIVE WORDS
        /////////////////////////////////////////////

        // Specifically check for "triste" and "decu" for French
        if (lowerText.contains("triste") ||
                lowerText.contains("decu") ||
                lowerText.contains("déçu") ||
                lowerText.contains("sad") ||
                lowerText.contains("mal") ||
                lowerText.contains("mauvais") ||
                lowerText.contains("terrible") ||
                lowerText.contains("horrible") ||
                lowerText.contains("malheureux") ||
                lowerText.contains("nul") ||
                lowerText.contains("affreux") ||
                lowerText.contains("déteste") ||
                lowerText.contains("deteste") ||
                lowerText.contains("hate") ||
                lowerText.contains("bad") ||
                lowerText.contains("awful") ||
                lowerText.contains("disappointed") ||
                lowerText.contains("disappointing") ||
                lowerText.contains("negative")) {

            System.out.println("FOUND NEGATIVE WORD! Returning NEGATIVE sentiment.");
            return new SentimentResult(Posts.SENTIMENT_NEGATIVE, 0.1);
        }

        /////////////////////////////////////////////
        // DIRECT CHECK FOR SPECIFIC POSITIVE WORDS
        /////////////////////////////////////////////

        if (lowerText.contains("heureux") ||
                lowerText.contains("content") ||
                lowerText.contains("j'aime") ||
                lowerText.contains("jaime") ||
                lowerText.contains("adore") ||
                lowerText.contains("génial") ||
                lowerText.contains("genial") ||
                lowerText.contains("super") ||
                lowerText.contains("bien") ||
                lowerText.contains("excellent") ||
                lowerText.contains("happy") ||
                lowerText.contains("great") ||
                lowerText.contains("wonderful") ||
                lowerText.contains("good") ||
                lowerText.contains("awesome") ||
                lowerText.contains("love") ||
                lowerText.contains("enjoy") ||
                lowerText.contains("positive")) {

            System.out.println("FOUND POSITIVE WORD! Returning POSITIVE sentiment.");
            return new SentimentResult(Posts.SENTIMENT_POSITIVE, 0.9);
        }

        // If no clear sentiment is found, return neutral
        System.out.println("NO CLEAR SENTIMENT FOUND. Returning NEUTRAL.");
        return new SentimentResult(Posts.SENTIMENT_NEUTRAL, 0.5);
    }
}