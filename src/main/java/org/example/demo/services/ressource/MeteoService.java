package org.example.demo.services.ressource;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MeteoService {

    private static final String API_KEY = "4b86b337ade60da0f51b2df130fc4229";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather?q=";

    public static String getMeteo(String ville) {
        try {
            String urlString = BASE_URL + URLEncoder.encode(ville, StandardCharsets.UTF_8)
                    + "&appid=" + API_KEY + "&units=metric&lang=fr";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);

            in.close();

            // 📦 Parsing JSON
            JSONObject json = new JSONObject(response.toString());

            String nomVille = json.getString("name");
            JSONObject main = json.getJSONObject("main");
            double temp = main.getDouble("temp");

            String description = json.getJSONArray("weather")
                    .getJSONObject(0)
                    .getString("description");

            return "🌤 Ville : " + nomVille + "\n🌡 Température : " + temp + "°C\n📋 Description : " + description;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Erreur météo : " + e.getMessage();
        }
    }
}