package org.example.demo.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleAuth {
    private static final String CLIENT_ID = "167398771817-7lrcrae359lpdgt9o9spmi5ingqekv9m.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost";  // URL de redirection
    private static final String AUTH_URL = "https://accounts.google.com/o/oauth2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json";
    private static final String SCOPE = "https://www.googleapis.com/auth/userinfo.email";

    public interface AuthCallback {
        void onSuccess(String email);
        void onFailure(String error);
    }

    public static void authenticate(AuthCallback callback) {
        // Créer une nouvelle fenêtre indépendante
        Stage authStage = new Stage();
        authStage.initModality(Modality.APPLICATION_MODAL);
        authStage.setTitle("Connexion avec Google");

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        String authUrl = AUTH_URL + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=code"
                + "&scope=" + SCOPE
                + "&access_type=offline";

        webEngine.load(authUrl);

        // Intercepter la redirection après connexion
        webEngine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
            if (newLocation.startsWith(REDIRECT_URI)) {
                authStage.close(); // Fermer la fenêtre d'authentification

                String authCode = extractAuthCode(newLocation);
                if (authCode != null) {
                    // Échanger le code contre un token d'accès
                    new Thread(() -> {
                        try {
                            String accessToken = getAccessToken(authCode);
                            String email = getUserEmail(accessToken);

                            // Exécuter le callback dans le thread principal
                            Platform.runLater(() -> callback.onSuccess(email));
                        } catch (Exception e) {
                            Platform.runLater(() -> callback.onFailure(e.getMessage()));
                        }
                    }).start();
                } else {
                    callback.onFailure("Erreur lors de la récupération du code.");
                }
            }
        });

        authStage.setScene(new Scene(webView, 800, 600));
        authStage.show();
    }

    private static String extractAuthCode(String url) {
        Pattern pattern = Pattern.compile("code=([^&]+)");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String getAccessToken(String authCode) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        String body = "code=" + authCode +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&redirect_uri=" + REDIRECT_URI +
                "&grant_type=authorization_code";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        return jsonResponse.get("access_token").getAsString();
    }

    private static String getUserEmail(String accessToken) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USER_INFO_URL))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
        return jsonResponse.get("email").getAsString();
    }
}

