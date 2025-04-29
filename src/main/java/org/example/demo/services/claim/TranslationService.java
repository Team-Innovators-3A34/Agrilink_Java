package org.example.demo.services.claim;


import org.example.demo.models.Reclamation;
import org.example.demo.models.Reponses;
import org.example.demo.utils.dataBaseHelper;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
//import org.json.JSONObject;

public class TranslationService {
    private String apiUrl;
    private String apiKey;
    private String apiHost;
    private HttpClient client;

    /**
     * Le constructeur initialise le service avec vos paramètres d'API.
     */
    public TranslationService() {
        // Utilisez les valeurs de vos variables d'environnement ou copiez-les directement pour un test local
        this.apiUrl = "https://google-translation-unlimited.p.rapidapi.com/trans"; // '%env(TRANSLATION_API_URL)%'
        this.apiKey = "b87ca822cdmsh0a0c4c3c7567bd9p1824ddjsn90c4848fb6c4";         // '%env(RAPID_API_KEY_TRANSLATION)%'
        this.apiHost = "google-translation-unlimited.p.rapidapi.com";               // '%env(TRANSLATION_API_HOST)%'
        this.client = HttpClient.newHttpClient();
    }


    /**
     * Traduit un texte dans la langue cible.
     *
     * @param text Texte à traduire.
     * @param targetLanguage Code de la langue cible (ex. "en" ou "es").
     * @return Le texte traduit ou le texte original en cas d’échec.
     */
    public String translateText(String text, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }
        try {
            String form = "text=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                    + "&target=" + URLEncoder.encode(targetLanguage, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(apiUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("x-rapidapi-key", apiKey)
                    .header("x-rapidapi-host", apiHost)
                    .POST(HttpRequest.BodyPublishers.ofString(form))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonObject = new JSONObject(response.body());

            if (jsonObject.has("translation") && !jsonObject.getString("translation").isEmpty()) {
                return jsonObject.getString("translation");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Retourne le texte original si la traduction échoue
        return text;
    }
}
