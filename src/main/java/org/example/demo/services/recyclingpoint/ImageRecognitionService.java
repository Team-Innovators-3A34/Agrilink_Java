
package org.example.demo.services.recyclingpoint;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClient;

public class ImageRecognitionService {
    private static final String RAPIDAPI_HOST = "ai-api-photo-description.p.rapidapi.com";
    private static final String RAPIDAPI_KEY  = "c4286c5e8cmshf69303343b44530p13d059jsn5101130016d8";
    private static final String RAPIDAPI_URL  = "https://ai-api-photo-description.p.rapidapi.com/description-from-url";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String analyze(File file) throws IOException, InterruptedException {
        String publicUrl = uploadToCloudinary(file);
        if (publicUrl == null) throw new IOException("Échec de l'upload Cloudinary");

        String body = "{\"url\":\"" + publicUrl + "\"}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(RAPIDAPI_URL))
                .header("x-rapidapi-host", RAPIDAPI_HOST)
                .header("x-rapidapi-key", RAPIDAPI_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> resp = HttpClient.newHttpClient()
                .send(req, HttpResponse.BodyHandlers.ofString());

        JsonNode root = objectMapper.readTree(resp.body());
        String desc = root.path("caption").asText("No description").replace("<error>", "").trim();
        return desc;
    }

    private String uploadToCloudinary(File file) throws IOException {
        String cloudName = "dcfad76uv";
        String preset = "javafx";
        String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        OkHttpClient client = new OkHttpClient();
        MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("image/*")))
                .addFormDataPart("upload_preset", preset)
                .build();

        Request req = new Request.Builder().url(url).post(body).build();
        try (Response r = client.newCall(req).execute()) {
            if (!r.isSuccessful()) throw new IOException("Cloudinary error " + r);
            String resp = r.body().string();
            return objectMapper.readTree(resp).get("secure_url").asText();
        }
    }

    public boolean isRecyclable(String description) {
        try (AsyncHttpClient client = new DefaultAsyncHttpClient()) {
            String jsonBody = "{\"sentence\":\"" + description.replace("\"", "\\\"") + "\"}";

            String summarizedText = client.prepare("POST", "https://text-analysis-classification-summarisation.p.rapidapi.com/api/v1/text_summarisation/")
                    .setHeader("x-rapidapi-key", "956073a9bcmshd684efe7f1e6b55p1bd2a0jsn2211c15af27f")
                    .setHeader("x-rapidapi-host", "text-analysis-classification-summarisation.p.rapidapi.com")
                    .setHeader("Content-Type", "application/json")
                    .setBody(jsonBody)
                    .execute()
                    .toCompletableFuture()
                    .thenApply(response -> response.getResponseBody())
                    .join();

            String lower = summarizedText.toLowerCase();
            return lower.contains("plastic bottle") || lower.contains("glass jar") || lower.contains("newspaper")
                    || lower.contains("cardboard") || lower.contains("metal can");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}