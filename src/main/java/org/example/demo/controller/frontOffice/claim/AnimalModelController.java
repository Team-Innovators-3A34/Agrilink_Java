package org.example.demo.controller.frontOffice.claim;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

public class AnimalModelController {

    @FXML
    private BorderPane root;

    @FXML
    private TextField speciesField; // Saisie de l'espèce de l'animal

    @FXML
    private TextArea symptomsArea; // Saisie des symptômes (texte ou via reconnaissance vocale)

    @FXML
    private Button voiceSymptomsButton; // Bouton pour lancer la saisie vocale

    @FXML
    private Slider sensitivitySlider; // Exemple de slide bar

    @FXML
    private Button predictButton; // Bouton pour lancer la prédiction

    @FXML
    private TextArea resultArea; // Affichage du résultat

    // Pour la reconnaissance vocale avec Vosk
    private Model model;
    private volatile boolean isRecording = false;
    private Thread recognitionThread;

    // Client HTTP pour appeler l’API Flask
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    // URL de l’API Flask pour la prédiction
    private final String predictApiUrl = "http://127.0.0.1:5001/predict/vet";

    @FXML
    public void initialize() {
        // Initialisation de Vosk pour la reconnaissance vocale
        try {
            LibVosk.setLogLevel(LogLevel.WARNINGS);
            URL modelUrl = getClass().getResource("/vosk-model-fr");
            if (modelUrl == null) {
                throw new IllegalArgumentException("Le dossier du modèle Vosk est introuvable !");
            }
            // Conversion en URI pour gérer les espaces et autres caractères spéciaux
            Path modelPath = Paths.get(modelUrl.toURI());
            model = new Model(modelPath.toString());
        } catch (Exception e) {
            e.printStackTrace();
            voiceSymptomsButton.setDisable(true);
        }

        // Définition des actions sur les boutons
        voiceSymptomsButton.setOnAction(e -> startVoiceRecognition(symptomsArea));
        predictButton.setOnAction(this::handlePredict);
    }

    /**
     * Démarre la reconnaissance vocale et insère le texte reconnu dans le champ cible.
     */
    private void startVoiceRecognition(TextInputControl targetField) {
        if (isRecording) {
            stopVoiceRecognition();
        }
        isRecording = true;
        recognitionThread = new Thread(() -> {
            try {
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                if (!AudioSystem.isLineSupported(info)) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur", "Format audio non supporté."));
                    return;
                }
                TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                Recognizer recognizer = new Recognizer(model, 16000);
                byte[] buffer = new byte[4096];

                long startTime = System.currentTimeMillis();
                long timeout = 5000; // Enregistrement pendant 5 secondes
                while (isRecording && (System.currentTimeMillis() - startTime < timeout)) {
                    int count = line.read(buffer, 0, buffer.length);
                    if (count > 0 && recognizer.acceptWaveForm(buffer, count)) {
                        String result = recognizer.getResult();
                        Gson gson = new Gson();
                        JsonObject jsonObject = gson.fromJson(result, JsonObject.class);
                        String text = jsonObject.has("text") ? jsonObject.get("text").getAsString() : "";
                        if (!text.isEmpty()) {
                            Platform.runLater(() -> {
                                String currentText = targetField.getText();
                                targetField.setText(currentText + (currentText.isEmpty() ? "" : " ") + text);
                            });
                            break;
                        }
                    }
                }
                // Récupération du résultat final
                String finalResult = recognizer.getFinalResult();
                Gson gson = new Gson();
                JsonObject finalJson = gson.fromJson(finalResult, JsonObject.class);
                String finalText = finalJson.has("text") ? finalJson.get("text").getAsString() : "";
                if (!finalText.isEmpty()) {
                    Platform.runLater(() -> {
                        String currentText = targetField.getText();
                        targetField.setText(currentText + (currentText.isEmpty() ? "" : " ") + finalText);
                    });
                }
                line.stop();
                line.close();
                recognizer.close();
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de reconnaissance vocale: " + e.getMessage()));
            }
            isRecording = false;
        });
        recognitionThread.setDaemon(true);
        recognitionThread.start();
    }

    /**
     * Arrête la reconnaissance vocale.
     */
    private void stopVoiceRecognition() {
        isRecording = false;
        if (recognitionThread != null) {
            try {
                recognitionThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Gère l'envoi de la requête de prédiction à l’API Flask et affiche le résultat.
     */
    private void handlePredict(ActionEvent event) {
        String species = speciesField.getText();
        String symptoms = symptomsArea.getText();

        if (species == null || species.trim().isEmpty() || symptoms == null || symptoms.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez renseigner l'espèce et les symptômes.");
            return;
        }

        // Indiquer à l'utilisateur que l'analyse est en cours
        Platform.runLater(() -> resultArea.setText("Analyse en cours..."));

        new Thread(() -> {
            try {
                // Création du payload JSON
                JsonObject payload = new JsonObject();
                payload.addProperty("symptoms", symptoms);
                payload.addProperty("species", species);
                String requestBody = payload.toString();
                System.out.println("Payload envoyé: " + requestBody);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(predictApiUrl))
                        .timeout(Duration.ofSeconds(30))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Réponse de l'API: " + response.body());

                Gson gson = new Gson();
                JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);
                String result = jsonResponse.has("result") && !jsonResponse.get("result").getAsString().isEmpty()
                        ? jsonResponse.get("result").getAsString()
                        : "Aucun résultat trouvé. Veuillez vérifier les symptômes et l'espèce.";
                Platform.runLater(() -> resultArea.setText(result));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> resultArea.setText("Erreur lors de la prédiction: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Affiche une alerte sur l'interface.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
