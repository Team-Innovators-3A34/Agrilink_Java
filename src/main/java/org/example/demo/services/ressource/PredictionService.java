package org.example.demo.services.ressource;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class PredictionService {

    public static String predictStatus(String expireDate, String message) {
        try {
            URL url = new URL("http://127.0.0.1:5000/predict"); // URL de ton API Flask
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Configuration de la requête POST
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // JSON à envoyer
            String jsonInputString = String.format(
                    "{\"expire_date\": \"%s\", \"message\": \"%s\"}",
                    expireDate, message
            );

            // Écrire les données dans la requête
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Lire la réponse
            Scanner scanner = new Scanner(conn.getInputStream(), "utf-8");
            String response = scanner.useDelimiter("\\A").next();
            scanner.close();

            return response; // Tu peux aussi parser le JSON si nécessaire

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la prédiction";
        }
    }
}