package org.example.demo.services.posts;

import javafx.concurrent.Task;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HuggingFaceImageService {
    private final String apiKey;
    private final String uploadDir = "src/main/resources/images/posts/";

    public HuggingFaceImageService(String apiKey) {
        this.apiKey = apiKey;
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public Task<String> generateImageFromDescriptionTask(String description) {
        return new Task<String>() {
            @Override
            protected String call() throws Exception {
                return generateImageFromDescription(description);
            }
        };
    }

    public String generateImageFromDescription(String description) throws IOException {
        URL url = new URL("https://api-inference.huggingface.co/models/stabilityai/stable-diffusion-xl-base-1.0");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Create JSON request
        String jsonRequest = "{\"inputs\":\"" + description + "\"}";

        // Send it
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonRequest.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Process response
        if (connection.getResponseCode() != 200) {
            throw new IOException("Failed to generate image: HTTP error code " + connection.getResponseCode());
        }

        // Generate unique filename nafs format AjouterPosts class
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String filename = "AI-" + timestamp + ".jpg";
        String filePath = uploadDir + filename;

        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return filename;
    }
}