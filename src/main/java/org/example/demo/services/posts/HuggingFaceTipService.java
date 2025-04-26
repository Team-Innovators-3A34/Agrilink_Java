package org.example.demo.services.posts;


import org.example.demo.utils.ConfigUtil;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HuggingFaceTipService {
    private static final String HUGGINGFACE_API_URL = "https://api-inference.huggingface.co/models/openai-community/gpt2";
    private final HttpClient client;
    private final String apiKey;

    public HuggingFaceTipService() {
        client = HttpClient.newHttpClient();
        apiKey = ConfigUtil.getHuggingFaceApiKeyTip();
    }

    public CompletableFuture<String> generateAgricultureTip() {
        String prompt = "Give me short simple Tips for sustainable agriculture: ";
        String requestBody = String.format(
                "{\"inputs\": \"%s\", \"parameters\": {\"max_length\": 100, \"temperature\": 0.7, \"top_p\": 0.9, \"do_sample\": true}}",
                prompt
        );

        CompletableFuture<String> result = new CompletableFuture<>();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HUGGINGFACE_API_URL))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    try {
                        // Extract the generated text using regex
                        Pattern pattern = Pattern.compile("\"generated_text\":\"(.*?)\"");
                        Matcher matcher = pattern.matcher(responseBody);

                        if (matcher.find()) {
                            String generatedText = matcher.group(1)
                                    .replace("\\n", "\n")
                                    .replace("\\\"", "\"")
                                    .replace("\\\\", "\\");

                            // Clean up the text to ensure it's just the agriculture tip
                            String tip = generatedText.replace(prompt, "").trim();

                            result.complete(tip);
                        } else {
                            result.completeExceptionally(new RuntimeException("Failed to parse response: " + responseBody));
                        }
                    } catch (Exception e) {
                        result.completeExceptionally(e);
                    }
                })
                .exceptionally(e -> {
                    result.completeExceptionally(e);
                    return null;
                });

        return result;
    }
}