package org.example.demo.controller.frontOffice.claim;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class SoilPredictionController {

    @FXML
    private StackPane dropZone;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Text dropText;

    @FXML
    private Label selectedFileLabel;

    @FXML
    private TextArea predictionResult;

    private File selectedImage;

    @FXML
    public void initialize() {
        // Configuration du drag & drop et clic
        dropZone.setOnMouseClicked(event -> handleSelectImage());
        dropZone.setOnDragOver(this::handleDragOver);
        dropZone.setOnDragDropped(this::handleDragDropped);
    }

    @FXML
    public void handleSelectImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de sol");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) dropZone.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            loadSelectedImage(file);
        }
    }

    private void handleDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            File file = db.getFiles().get(0);
            loadSelectedImage(file);
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void loadSelectedImage(File file) {
        selectedImage = file;
        Image img = new Image(file.toURI().toString());
        imagePreview.setImage(img);
        imagePreview.setVisible(true);
        dropText.setVisible(false);
        selectedFileLabel.setText(file.getName());
    }

    @FXML
    public void handlePredict() {
        if (selectedImage == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez sélectionner une image avant de prédire.");
            return;
        }

        predictionResult.setText("Analyse de l'image en cours...");

        new Thread(() -> {
            try {
                String response = sendImageForPrediction(selectedImage);

                // Analyser la réponse JSON
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

                double confidence = jsonResponse.has("confidence") ?
                        jsonResponse.get("confidence").getAsDouble() : 0.0;

                String soilType = jsonResponse.has("predicted_class") ?
                        jsonResponse.get("predicted_class").getAsString() : "Type de sol inconnu";

                // Formater le résultat avec des informations sur le type de sol
                String formattedResponse = formatSoilInformation(soilType, confidence);

                Platform.runLater(() -> predictionResult.setText(formattedResponse));
            } catch (IOException e) {
                Platform.runLater(() -> {
                    predictionResult.setText("Erreur lors de la communication avec le serveur: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la communication avec le serveur: " + e.getMessage());
                });
            }
        }).start();
    }

    private String sendImageForPrediction(File image) throws IOException {
        String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String urlString = "http://127.0.0.1:5000/predict/soil";

        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        try (OutputStream outputStream = connection.getOutputStream()) {
            outputStream.write((twoHyphens + boundary + lineEnd).getBytes());
            outputStream.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + image.getName() + "\"" + lineEnd).getBytes());
            outputStream.write(("Content-Type: " + Files.probeContentType(image.toPath()) + lineEnd + lineEnd).getBytes());
            Files.copy(image.toPath(), outputStream);
            outputStream.write(lineEnd.getBytes());
            outputStream.write((twoHyphens + boundary + twoHyphens + lineEnd).getBytes());
        }

        StringBuilder response = new StringBuilder();
        try (var reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    private String formatSoilInformation(String soilType, double confidence) {
        StringBuilder result = new StringBuilder();
        String confidencePercent = String.format("%.2f", confidence * 100);

        result.append("Résultat de la prédiction : \n");
        result.append("Type de sol détecté: ").append(soilType).append(" (confiance: ").append(confidencePercent).append("%)\n\n");

        // Ajouter des informations selon le type de sol détecté
        switch (soilType) {
            case "Black Soil":
                result.append("Information sur le sol noir (Black Soil):\n\n");
                result.append("Le sol noir, également connu sous le nom de Regur ou sol noir de coton, est reconnu pour sa fertilité exceptionnelle. ")
                        .append("Riche en calcium, magnésium, potassium et argile, il possède une excellente capacité de rétention d'eau. ")
                        .append("Ces sols sont particulièrement adaptés à la culture du coton, mais conviennent également à de nombreuses autres cultures.\n\n")
                        .append("Caractéristiques principales:\n")
                        .append("- Couleur foncée due à la présence de composés ferreux et de matière organique\n")
                        .append("- Forte teneur en argile (jusqu'à 60%)\n")
                        .append("- Excellente capacité de rétention d'eau\n")
                        .append("- Autolabourage (formation de fissures en période sèche)\n")
                        .append("- pH légèrement alcalin (7.5-8.5)\n\n")
                        .append("Cultures adaptées: coton, blé, millet, sorgho, soja, tournesol et diverses légumineuses.");
                break;
            case "Cinder Soil":
                result.append("Information sur le sol de cendre (Cinder Soil):\n\n");
                result.append("Le sol de cendre est formé à partir de matériaux volcaniques, principalement de cendres volcaniques. ")
                        .append("Il est généralement léger, poreux et présente un bon drainage. ")
                        .append("Ces sols sont riches en minéraux mais peuvent être déficients en certains nutriments essentiels.\n\n")
                        .append("Caractéristiques principales:\n")
                        .append("- Texture grossière et poreuse\n")
                        .append("- Excellent drainage\n")
                        .append("- Faible capacité de rétention d'eau\n")
                        .append("- Riche en minéraux d'origine volcanique\n")
                        .append("- Peut être déficient en azote et phosphore\n\n")
                        .append("Cultures adaptées: vignes, oliviers, agrumes et certains légumes à condition d'irriguer adéquatement.");
                break;
            case "Laterite Soil":
                result.append("Information sur le sol latéritique (Laterite Soil):\n\n");
                result.append("Le sol latéritique est un type de sol très altéré trouvé dans les régions tropicales et subtropicales. ")
                        .append("Il est riche en oxydes de fer et d'aluminium, ce qui lui confère une couleur rougeâtre caractéristique. ")
                        .append("Ces sols sont généralement acides et pauvres en éléments nutritifs en raison du lessivage intensif.\n\n")
                        .append("Caractéristiques principales:\n")
                        .append("- Couleur rouge-brun due aux oxydes de fer\n")
                        .append("- Texture variable, souvent graveleuse\n")
                        .append("- Faible teneur en matière organique\n")
                        .append("- pH acide (4.5-6.0)\n")
                        .append("- Faible capacité d'échange cationique\n\n")
                        .append("Cultures adaptées: noix de cajou, thé, café, hévéa, ananas, manioc et certaines légumineuses adaptées aux sols acides.");
                break;
            case "Peat Soil":
                result.append("Information sur le sol tourbeux (Peat Soil):\n\n");
                result.append("Le sol tourbeux est riche en matière organique et se caractérise par une grande capacité de rétention d'eau. ")
                        .append("Il est généralement acide (pH bas) et contient peu d'éléments nutritifs disponibles pour les plantes. ")
                        .append("Ces sols se forment dans les zones humides où la décomposition de matière végétale est lente.\n\n")
                        .append("Caractéristiques principales:\n")
                        .append("- Très riche en matière organique (plus de 30%)\n")
                        .append("- Capacité élevée de rétention d'eau\n")
                        .append("- Généralement acide (pH 3.5-5.5)\n")
                        .append("- Faible en éléments nutritifs minéraux\n")
                        .append("- Faible densité apparente\n\n")
                        .append("Cultures adaptées: bleuets, canneberges, rhododendrons, azalées et certaines plantes carnivores.");
                break;
            case "Yellow Soil":
                result.append("Information sur le sol jaune (Yellow Soil):\n\n");
                result.append("Le sol jaune est caractérisé par sa couleur jaunâtre due à la présence d'oxydes de fer hydratés. ")
                        .append("On le trouve principalement dans les régions subtropicales avec des précipitations modérées à élevées. ")
                        .append("Ces sols sont généralement acides et moins fertiles que les sols rouges ou noirs.\n\n")
                        .append("Caractéristiques principales:\n")
                        .append("- Couleur jaune ou jaune-brun due aux oxydes de fer hydratés\n")
                        .append("- Texture variable, souvent limoneuse\n")
                        .append("- Acidité modérée à élevée (pH 4.5-6.0)\n")
                        .append("- Teneur modérée en matière organique\n")
                        .append("- Lessivage modéré des nutriments\n\n")
                        .append("Cultures adaptées: thé, agrumes, fruits tropicaux, riz, patate douce et certaines légumineuses tolérantes à l'acidité.");
                break;
            default:
                result.append("Information détaillée non disponible pour ce type de sol.");
                break;
        }

        return result.toString();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}