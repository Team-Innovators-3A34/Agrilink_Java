package org.example.demo.services.ressource;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class LanguageToolValidator {

    public static List<String> getDescriptionErrors(String text) {
        List<String> errors = new ArrayList<>();
        try {
            String data = "text=" + URLEncoder.encode(text, "UTF-8")
                    + "&language=fr";

            URL url = new URL("https://api.languagetool.org/v2/check");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            OutputStream os = conn.getOutputStream();
            os.write(data.getBytes());
            os.flush();
            os.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while((inputLine = in.readLine()) != null){
                response.append(inputLine);
            }
            in.close();

            JSONObject json = new JSONObject(response.toString());
            JSONArray matches = json.getJSONArray("matches");

            for (int i = 0; i < matches.length(); i++) {
                JSONObject match = matches.getJSONObject(i);
                String message = match.getString("message");
                JSONArray replacements = match.getJSONArray("replacements");

                StringBuilder suggestion = new StringBuilder();
                if (replacements.length() > 0) {
                    suggestion.append("Suggestions : ");
                    for (int j = 0; j < Math.min(replacements.length(), 3); j++) {
                        suggestion.append(replacements.getJSONObject(j).getString("value"));
                        if (j < replacements.length() - 1) suggestion.append(", ");
                    }
                } else {
                    suggestion.append("Aucune suggestion.");
                }

                errors.add("❌ " + message + "\n" + suggestion);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return errors;
    }
}
