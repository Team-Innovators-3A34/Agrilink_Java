// TikTokAPIService.java
package org.example.demo.services.claim;

import org.example.demo.models.TikTokVideo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class TikTokAPIService {

    // Clé d'API (à adapter selon votre configuration)
    private final String apiKey = "b87ca822cdmsh0a0c4c3c7567bd9p1824ddjsn90c4848fb6c4";
    private final String apiHost = "tiktok-scraper7.p.rapidapi.com";

    public List<TikTokVideo> getAgricultureVideos(int count, int cursor) {
        List<TikTokVideo> videos = new ArrayList<>();

        // Préparation des paramètres de requête
        String keywords = "agriculture,farming,crops,organic farming,agrotech";
        String region = "us";
        String query = String.format("keywords=%s&region=%s&count=%d&cursor=%d&publish_time=0&sort_type=0",
                URLEncoder.encode(keywords, StandardCharsets.UTF_8),
                URLEncoder.encode(region, StandardCharsets.UTF_8),
                count,
                cursor
        );

        String url = "https://tiktok-scraper7.p.rapidapi.com/feed/search?" + query;

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("x-rapidapi-host", apiHost)
                    .header("x-rapidapi-key", apiKey)
                    .GET()
                    .build();

            // Exécution de la requête et récupération de la réponse en chaîne
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Traitement du JSON de réponse
            JSONObject jsonResponse = new JSONObject(response.body());
            JSONObject dataObject = jsonResponse.getJSONObject("data");

            // Extraction du tableau de vidéos
            JSONArray videosArray = dataObject.optJSONArray("videos");
            if (videosArray != null) {
                for (int i = 0; i < videosArray.length(); i++) {
                    JSONObject videoObject = videosArray.getJSONObject(i);
                    String videoId = videoObject.optString("video_id", null);
                    JSONObject authorObject = videoObject.optJSONObject("author");
                    String username = (authorObject != null) ? authorObject.optString("unique_id", null) : null;
                    String title = videoObject.optString("title", "Titre non disponible");
                    // Construction de l'embed URL si possible
                    String embedUrl = (videoId != null && username != null)
                            ? "https://www.tiktok.com/embed/@" + username + "/video/" + videoId
                            : null;
                    String cover = videoObject.optString("cover", null);
                    // Récupération de l'URL de la vidéo (à adapter si l’API évolue)
                    String videoUrl = videoObject.optString("play", null);

                    // Si l'ID n'est pas présent, en générer un
                    if (videoId == null) {
                        videoId = "vid-" + java.util.UUID.randomUUID().toString();
                    }

                    TikTokVideo video = new TikTokVideo(videoId, username, title, embedUrl, cover, videoUrl);
                    videos.add(video);
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'appel à l'API TikTok : " + e.getMessage());
        }

        return videos;
    }
}
