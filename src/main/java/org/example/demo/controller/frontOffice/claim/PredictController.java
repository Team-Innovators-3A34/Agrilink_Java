package org.example.demo.controller.frontOffice.claim;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
        // Récupérer les valeurs saisies dans le formulaire
        String region = regionComboBox.getValue();
        String soilType = soilTypeComboBox.getValue();
        String crop = cropComboBox.getValue();
        String rainfallStr = rainfallField.getText();
        String temperatureStr = temperatureField.getText();
        boolean fertilizerUsed = fertilizerCheckBox.isSelected();
        boolean irrigationUsed = irrigationCheckBox.isSelected();
        String weatherCondition = weatherComboBox.getValue();
        String daysToHarvestStr = daysToHarvestField.getText();

        // Validation basique des champs numériques
        if(rainfallStr.isEmpty() || temperatureStr.isEmpty() || daysToHarvestStr.isEmpty()){
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez remplir tous les champs numériques.");
            return;
        }

        try {
            double rainfall = Double.parseDouble(rainfallStr);
            double temperature = Double.parseDouble(temperatureStr);
            int daysToHarvest = Integer.parseInt(daysToHarvestStr);

            // Création de l'objet JSON avec les données du formulaire
            JsonObject jsonData = new JsonObject();
            jsonData.addProperty("Region", region);
            jsonData.addProperty("Soil_Type", soilType);
            jsonData.addProperty("Crop", crop);
            jsonData.addProperty("Rainfall_mm", rainfall);
            jsonData.addProperty("Temperature_Celsius", temperature);
            jsonData.addProperty("Fertilizer_Used", fertilizerUsed);
            jsonData.addProperty("Irrigation_Used", irrigationUsed);
            jsonData.addProperty("Weather_Condition", weatherCondition);
            jsonData.addProperty("Days_to_Harvest", daysToHarvest);

            // Appel à l'API de prédiction via HTTP POST
            String predictionStr = callPredictionAPI(jsonData.toString());
            double predictionValue = 0.0;
            try {
                predictionValue = Double.parseDouble(predictionStr);
            } catch(NumberFormatException e) {
                // On peut gérer ici un éventuel message d'erreur spécifique.
                predictionStr = "Erreur de conversion";
            }
            // Formatage à deux décimales
            String formattedPrediction = String.format("%.2f", predictionValue);

            // Affichage du résultat dans une alerte personnalisée
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Résultat de la prédiction");
            alert.setHeaderText(null);
            // Personnalisation du contenu et du style
            Label contentLabel = new Label("Le rendement estimé est : " + formattedPrediction + " tonnes par hectare");
            contentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");
            alert.getDialogPane().setContent(contentLabel);
            alert.getDialogPane().setStyle("-fx-padding: 20; -fx-background-color: #f1f8e9;");
            alert.showAndWait();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer des valeurs numériques valides.");
        }
    }


    private String callPredictionAPI(String jsonData) {
        try {
            // Remplacez l'URL par votre endpoint d'API
            URL url = new URL("http://localhost:5100/predict");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setDoOutput(true);

            // Envoyer les données JSON
            connection.getOutputStream().write(jsonData.getBytes(StandardCharsets.UTF_8));
            connection.getOutputStream().flush();
            connection.getOutputStream().close();

            // Lire la réponse de l'API
            int responseCode = connection.getResponseCode();
            if(responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(connection.getInputStream());
                StringBuilder responseBuilder = new StringBuilder();
                while(scanner.hasNext()){
                    responseBuilder.append(scanner.nextLine());
                }
                scanner.close();

                // Supposons que la réponse JSON contient une propriété "prediction"
                JsonObject responseJson = new Gson().fromJson(responseBuilder.toString(), JsonObject.class);
                return responseJson.has("prediction") ? responseJson.get("prediction").getAsString() : "Aucune prédiction reçue";
            } else {
                return "Erreur lors de l'appel à l'API: " + responseCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'appel à l'API";
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
