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
       /* String prompt = "Give me short simple tips for sustainable agriculture: ";
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
                            result.completeExceptionally(new RuntimeException("Failed to get response: " + responseBody));
                        }
                    } catch (Exception e) {
                        result.completeExceptionally(e);
                    }
                })
                .exceptionally(e -> {
                    result.completeExceptionally(e);
                    return null;
                });*/

        CompletableFuture<String> result = new CompletableFuture<>();
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                int randomIndex = (int) (Math.random() * FALLBACK_TIPS.length);
                result.complete(FALLBACK_TIPS[randomIndex]);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                result.completeExceptionally(e);
            }
        }).start();
        return result;
    }
















//try more or just keep the fallback for now
    private static final String[] FALLBACK_TIPS = {
            "Rotate crops to prevent soil depletion and reduce pest problems.",
            "Use companion planting to naturally deter pests and improve pollination.",
            "Collect rainwater to reduce water consumption during dry periods.",
            "Apply mulch to retain soil moisture and suppress weeds naturally.",
            "Compost kitchen waste to create nutrient-rich soil amendments.",
            "Plant cover crops during off-seasons to prevent soil erosion.",
            "Use natural predators like ladybugs to control aphid populations.",
            "Create windbreaks to protect crops from strong winds and reduce water loss.",
            "Practice minimal tillage to preserve soil structure and beneficial organisms.",
            "Install drip irrigation systems to conserve water and target plant roots directly.",
            "Use organic fertilizers to improve soil health without harmful chemicals.",
            "Implement integrated pest management to reduce reliance on pesticides.",
            "Plant native species that are adapted to local climate conditions.",
            "Create buffer zones near water sources to prevent runoff contamination.",
            "Practice crop diversity to increase resilience against diseases and pests.",
            "Use beneficial microorganisms to enhance soil fertility and plant health.",
            "Time planting according to seasonal patterns to optimize growth cycles.",
            "Maintain hedgerows to provide habitat for beneficial insects and pollinators.",
            "Practice agroforestry by integrating trees with crops for mutual benefits.",
            "Use solar-powered equipment to reduce fossil fuel consumption.",
            "Monitor soil pH regularly and adjust it naturally with organic amendments.",
            "Implement no-till farming to maintain soil structure and reduce erosion.",
            "Create habitat corridors to support biodiversity and natural pest control.",
            "Use crop residues as natural mulch to return nutrients to the soil.",
            "Practice contour farming on slopes to prevent water runoff and soil erosion.",
            "Implement precision agriculture techniques to optimize resource usage.",
            "Use biological pest controls like nematodes to manage soil-dwelling pests.",
            "Plant nitrogen-fixing legumes to naturally enrich soil fertility.",
            "Practice intercropping to maximize space and create beneficial plant interactions.",
            "Set up bee hives to improve pollination and increase crop yields naturally.",
            "Use heritage seeds to preserve genetic diversity and regional adaptation.",
            "Implement holistic grazing management to improve soil health and sequester carbon.",
            "Create swales to capture and infiltrate water during heavy rain events.",
            "Plant early-flowering plants to support beneficial insects early in the season.",
            "Use thermal weed control methods instead of chemical herbicides.",
            "Practice targeted pruning to improve air circulation and reduce disease pressure.",
            "Implement silvopasture by combining trees, forage plants, and livestock grazing.",
            "Create habitat for birds to help control insect populations naturally.",
            "Use green manures to add organic matter and nutrients to depleted soils.",
            "Practice water conservation through mulching and appropriate irrigation timing."
    };
}