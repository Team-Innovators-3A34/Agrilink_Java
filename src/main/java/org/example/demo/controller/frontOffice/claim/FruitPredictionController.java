package org.example.demo.controller.frontOffice.claim;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

public class FruitPredictionController {

    @FXML
    private StackPane dropZone;

    @FXML
    private ImageView imagePreview;

    @FXML
    private Text dropText;

    @FXML
    private Label selectedFileLabel;

    @FXML
    private TextArea resultArea;

    @FXML
    private Button submitBtn;

    private File selectedImage;

    // Client HTTP pour appeler l'API de prédiction
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // URL de l'API de prédiction pour le type et les calories
    private final String predictApiUrl = "http://127.0.0.1:5000/predict/fruit";

    @FXML
    public void initialize() {
        // Désactive le bouton de soumission tant qu'aucune image n'est sélectionnée
        submitBtn.setDisable(true);

        // Configuration du drag-and-drop
        dropZone.setOnMouseClicked(event -> openFileChooser());

        dropZone.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles() && !db.getFiles().isEmpty()) {
                selectedImage = db.getFiles().get(0);
                displayImage(selectedImage);
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }

    /**
     * Ouvre le sélecteur de fichier pour choisir une image.
     */
    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        Stage stage = (Stage) dropZone.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedImage = file;
            displayImage(selectedImage);
        }
    }

    /**
     * Affiche l'image sélectionnée dans l'ImageView.
     */
    private void displayImage(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            Image image = new Image(fis);
            imagePreview.setImage(image);
            imagePreview.setVisible(true);
            dropText.setVisible(false);
            selectedFileLabel.setText("Fichier: " + file.getName());
            submitBtn.setDisable(false);
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'image.");
        }
    }

    /**
     * Envoie l'image sélectionnée à l'API de prédiction pour obtenir le type et les calories.
     */
    @FXML
    private void handleSubmit(ActionEvent event) {
        if (selectedImage == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez sélectionner une image avant de prédire.");
            return;
        }

        resultArea.setText("Analyse de l'image en cours...");

        new Thread(() -> {
            try {
                String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(bos, "UTF-8"), true);

                writer.append("--" + boundary).append("\r\n");
                writer.append("Content-Disposition: form-data; name=\"file\"; filename=\"" + selectedImage.getName() + "\"").append("\r\n");
                writer.append("Content-Type: " + Files.probeContentType(selectedImage.toPath())).append("\r\n");
                writer.append("\r\n").flush();
                Files.copy(selectedImage.toPath(), bos);
                bos.flush();
                writer.append("\r\n").flush();
                writer.append("--" + boundary + "--").append("\r\n").flush();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(predictApiUrl))
                        .timeout(Duration.ofMinutes(1))
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(bos.toByteArray()))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                String prediction = jsonResponse.has("model1_prediction")
                        ? jsonResponse.get("model1_prediction").getAsString()
                        : "Aucune prédiction";
                String calories = jsonResponse.has("calories_per_100g")
                        ? jsonResponse.get("calories_per_100g").getAsString()
                        : "N/A";

                Platform.runLater(() ->
                        resultArea.setText("Résultat de l'analyse :\n\n" +
                                "Type: " + prediction + "\n" +
                                "Calories: " + calories + " calories/100g")
                );
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                        resultArea.setText("Erreur lors de la prédiction: " + e.getMessage())
                );
            }
        }).start();
    }

    /**
     * Affiche une alerte sur l'interface.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}