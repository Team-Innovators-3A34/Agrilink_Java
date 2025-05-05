package org.example.demo.controller.frontOffice.claim;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;

public class PlanteController {

    @FXML
    private StackPane dropZone;

    @FXML
    private ImageView imagePreview;

    @FXML
    private TextArea predictionResult;

    @FXML
    private TextField diseaseSearchField;

    @FXML
    private TextArea diseaseInfoArea;

    private File selectedImage;

    // Client HTTP pour les appels API (prédiction et ChatGPT)
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(30))  // Augmenter le timeout pour les appels API
            .build();

    // URL de l'API de prédiction et de l'API ChatGPT (à adapter)
    private final String predictApiUrl = "http://localhost:5001/predict/plant";
    private final String chatGptApiUrl = "https://chatgpt-42.p.rapidapi.com/chatgpt";
    // Votre clé API RapidAPI (remplacez "YOUR_RAPIDAPI_KEY" par votre clé)
    private final String rapidApiKey = "b87ca822cdmsh0a0c4c3c7567bd9p1824ddjsn90c4848fb6c4";

    /**
     * Méthode d'initialisation appelée par JavaFX.
     * Configure la zone de drag-and-drop.
     */
    @FXML
    private void initialize() {
        // Configuration des messages d'interface
        dropZone.setOnMouseClicked(event -> openFileChooser());
        dropZone.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });
        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                selectedImage = db.getFiles().get(0);
                displayImage(selectedImage);
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });

        // Message d'accueil
        predictionResult.setText("Déposez une image de plante ou cliquez pour en sélectionner une.");
        diseaseInfoArea.setText("Entrez le nom d'une maladie de plante pour obtenir des informations détaillées.");
    }

    /**
     * Ouvre le sélecteur de fichier pour choisir une image.
     */
    private void openFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de plante");
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

            // Effacer les résultats précédents
            predictionResult.setText("Image chargée avec succès. Cliquez sur 'Analyser' pour identifier les maladies.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger l'image.");
        }
    }

    /**
     * Gère la prédiction : appelle l'API de prédiction puis ChatGPT pour obtenir des informations.
     */
    @FXML
    private void handlePredict(ActionEvent event) {
        if (selectedImage == null) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez sélectionner une image avant de prédire.");
            return;
        }

        predictionResult.setText("Analyse de l'image en cours...\nVérification si l'image contient une plante...");

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
                        .timeout(Duration.ofMinutes(2))  // Augmenter le timeout car la vérification d'image peut prendre du temps
                        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                        .POST(HttpRequest.BodyPublishers.ofByteArray(bos.toByteArray()))
                        .build();

                // Mise à jour de l'UI pendant la requête
                Platform.runLater(() -> predictionResult.setText("Analyse de l'image en cours...\nCommunication avec le serveur..."));

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

                // Vérifier s'il y a une erreur (image non-plante)
                if (jsonResponse.has("error")) {
                    String errorMessage = jsonResponse.get("error").getAsString();
                    Platform.runLater(() -> {
                        predictionResult.setText("Erreur: " + errorMessage);
                        showAlert(Alert.AlertType.WARNING, "Image non valide", errorMessage);
                    });
                    return;
                }

                String prediction = jsonResponse.has("prediction")
                        ? jsonResponse.get("prediction").getAsString()
                        : "Aucune prédiction";

                Platform.runLater(() -> {
                    predictionResult.setText("Prédiction réussie: " + prediction + "\n\nRecherche d'informations sur cette maladie...");
                });

                String prompt = "Donne-moi des informations détaillées sur la maladie '" + prediction +
                        "' des plantes, en indiquant ses causes, ses symptômes, comment la traiter et en donnant des conseils pratiques pour les agriculteurs.";

                JsonObject chatGptRequestBody = new JsonObject();
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", prompt);
                chatGptRequestBody.add("messages", gson.toJsonTree(new JsonObject[]{ message }));
                chatGptRequestBody.addProperty("web_access", false);

                HttpRequest chatRequest = HttpRequest.newBuilder()
                        .uri(URI.create(chatGptApiUrl))
                        .timeout(Duration.ofSeconds(30))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .header("x-rapidapi-key", rapidApiKey)
                        .header("x-rapidapi-host", "chatgpt-42.p.rapidapi.com")
                        .POST(HttpRequest.BodyPublishers.ofString(chatGptRequestBody.toString()))
                        .build();

                HttpResponse<String> chatResponse = httpClient.send(chatRequest, HttpResponse.BodyHandlers.ofString());
                JsonObject chatJson = gson.fromJson(chatResponse.body(), JsonObject.class);
                String diseaseInfo = chatJson.has("result")
                        ? chatJson.get("result").getAsString()
                        : "Aucune information sur la maladie.";

                Platform.runLater(() -> {
                    StringBuilder resultText = new StringBuilder();
                    resultText.append("Prédiction: ").append(prediction).append("\n\n");
                    resultText.append("Informations sur la maladie:\n").append(diseaseInfo);
                    predictionResult.setText(resultText.toString());
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    predictionResult.setText("Erreur lors de l'analyse: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la prédiction: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Recherche d'informations sur une maladie via le texte saisi par l'utilisateur.
     */
    @FXML
    private void handleSearchDisease(ActionEvent event) {
        String query = diseaseSearchField.getText().trim();
        if (query.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez entrer le nom d'une maladie.");
            return;
        }
        diseaseInfoArea.setText("Recherche d'informations pour : " + query + "\n");
        new Thread(() -> {
            try {
                String prompt = "Donne-moi des informations détaillées sur la maladie '" + query +
                        "' des plantes, en indiquant ses causes, ses symptômes, comment la traiter et en donnant des conseils pratiques pour les agriculteurs.";
                Gson gson = new Gson();
                JsonObject chatGptRequestBody = new JsonObject();
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", prompt);
                chatGptRequestBody.add("messages", gson.toJsonTree(new JsonObject[]{ message }));
                chatGptRequestBody.addProperty("web_access", false);

                HttpRequest chatRequest = HttpRequest.newBuilder()
                        .uri(URI.create(chatGptApiUrl))
                        .timeout(Duration.ofSeconds(30))
                        .header("Accept", "application/json")
                        .header("Content-Type", "application/json")
                        .header("x-rapidapi-key", rapidApiKey)
                        .header("x-rapidapi-host", "chatgpt-42.p.rapidapi.com")
                        .POST(HttpRequest.BodyPublishers.ofString(chatGptRequestBody.toString()))
                        .build();

                HttpResponse<String> chatResponse = httpClient.send(chatRequest, HttpResponse.BodyHandlers.ofString());
                JsonObject chatJson = gson.fromJson(chatResponse.body(), JsonObject.class);
                String diseaseInfo = chatJson.has("result")
                        ? chatJson.get("result").getAsString()
                        : "Aucune information sur la maladie.";

                Platform.runLater(() -> diseaseInfoArea.setText(diseaseInfo));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    diseaseInfoArea.setText("Erreur lors de la recherche: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * Retourne à la vue principale.
     */
    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherReclamations.fxml"));
            Parent mainView = loader.load();
            BorderPane root = (BorderPane) ((Button) event.getSource()).getScene().getRoot();
            root.setCenter(mainView);
        } catch (IOException ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de revenir à la vue principale.");
        }
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

    /**
     * Réinitialise l'interface et efface l'image sélectionnée.
     */
    @FXML
    private void handleReset(ActionEvent event) {
        selectedImage = null;
        imagePreview.setImage(null);
        predictionResult.setText("Déposez une image de plante ou cliquez pour en sélectionner une.");
        diseaseSearchField.clear();
        diseaseInfoArea.setText("Entrez le nom d'une maladie de plante pour obtenir des informations détaillées.");
    }
}