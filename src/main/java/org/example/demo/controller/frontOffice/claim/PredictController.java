package org.example.demo.controller.frontOffice.claim;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class PredictController {

    @FXML
    private ComboBox<String> regionComboBox;
    @FXML
    private ComboBox<String> soilTypeComboBox;
    @FXML
    private ComboBox<String> cropComboBox;
    @FXML
    private TextField rainfallField;
    @FXML
    private TextField temperatureField;
    @FXML
    private CheckBox fertilizerCheckBox;
    @FXML
    private CheckBox irrigationCheckBox;
    @FXML
    private ComboBox<String> weatherComboBox;
    @FXML
    private TextField daysToHarvestField;

    @FXML
    private void initialize() {
        // Initialiser les listes de choix
        ObservableList<String> regions = FXCollections.observableArrayList("Ouest", "Sud", "Nord", "Est");
        regionComboBox.setItems(regions);
        regionComboBox.getSelectionModel().selectFirst();

        ObservableList<String> soilTypes = FXCollections.observableArrayList("Sableux", "Argileux", "Limon", "Silt");
        soilTypeComboBox.setItems(soilTypes);
        soilTypeComboBox.getSelectionModel().selectFirst();

        ObservableList<String> crops = FXCollections.observableArrayList("Coton", "Riz", "Orge", "Blé", "Soja");
        cropComboBox.setItems(crops);
        cropComboBox.getSelectionModel().selectFirst();

        ObservableList<String> weatherConditions = FXCollections.observableArrayList("Ensoleillé", "Nuageux", "Pluvieux");
        weatherComboBox.setItems(weatherConditions);
        weatherComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handlePredict() {
        try {
            // Lecture des valeurs du formulaire
            String region = regionComboBox.getValue();
            String soilType = soilTypeComboBox.getValue();
            String crop = cropComboBox.getValue();

            // Validation des champs numériques
            if (rainfallField.getText().isEmpty() || temperatureField.getText().isEmpty() || daysToHarvestField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs numériques.");
                return;
            }

            double rainfall;
            double temperature;
            int daysToHarvest;

            try {
                rainfall = Double.parseDouble(rainfallField.getText());
                temperature = Double.parseDouble(temperatureField.getText());
                daysToHarvest = Integer.parseInt(daysToHarvestField.getText());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de format", "Les valeurs numériques doivent être des nombres valides.");
                return;
            }

            boolean fertilizer = fertilizerCheckBox.isSelected();
            boolean irrigation = irrigationCheckBox.isSelected();
            String weather = weatherComboBox.getValue();

            // Création de l'objet JSON à envoyer (correspondant au format de l'API)
            JsonObject json = new JsonObject();
            json.addProperty("Region", region);
            json.addProperty("Soil_Type", soilType);
            json.addProperty("Crop", crop);
            json.addProperty("Rainfall_mm", rainfall);
            json.addProperty("Temperature_Celsius", temperature);
            json.addProperty("Fertilizer_Used", fertilizer);
            json.addProperty("Irrigation_Used", irrigation);
            json.addProperty("Weather_Condition", weather);
            json.addProperty("Days_to_Harvest", daysToHarvest);

            // Appel à l'API et récupération du résultat
            String result = callPredictionAPI(json.toString());

            // Affichage du résultat
            showAlert(Alert.AlertType.INFORMATION, "Résultat de la prédiction", "Le rendement prévu est : " + result);

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue : " + e.getMessage());
        }
    }

    private String callPredictionAPI(String jsonData) {
        try {
            // Utilisation de l'URL correcte du serveur Flask
            URL url = new URL("http://localhost:5100/predict/rendement");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Envoyer les données JSON
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Lire la réponse
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (Scanner scanner = new Scanner(connection.getInputStream(), StandardCharsets.UTF_8)) {
                    StringBuilder responseBuilder = new StringBuilder();
                    while (scanner.hasNext()) {
                        responseBuilder.append(scanner.nextLine());
                    }
                    JsonObject responseJson = new Gson().fromJson(responseBuilder.toString(), JsonObject.class);
                    return responseJson.has("prediction") ? responseJson.get("prediction").getAsString() : "Aucune prédiction reçue";
                }
            } else {
                return "Erreur lors de l'appel à l'API: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'appel à l'API: " + e.getMessage();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}