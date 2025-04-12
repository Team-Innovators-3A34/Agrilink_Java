package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entities.Reponses;
import org.example.entities.Reclamation;
import org.example.services.ReponsesService;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class RepondreReclamationController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextArea contentTextArea;

    @FXML
    private TextField solutionTextField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private CheckBox isAutoCheckBox;

    private ReponsesService reponsesService = new ReponsesService();

    // Réponse générée par défaut (utilisée si l'API ne répond pas)
    private String generatedResponse = "Ceci est une réponse générée automatiquement.";

    // Clé API RapidAPI (remplacez "votre_rapid_api_key" par votre clé réelle)
    private String rapidApiKey = "b87ca822cdmsh0a0c4c3c7567bd9p1824ddjsn90c4848fb6c4";

    // La réclamation à traiter
    private Reclamation reclamation;

    // Référence au DashboardController pour navigation ou autres interactions
    private DashboardController dashboardController;

    // Méthode d'injection du DashboardController
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now());
        toggleContentField();
    }

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        titleLabel.setText("Répondre à la réclamation : " + reclamation.getTitle());
    }

    @FXML
    private void toggleContentField() {
        if (isAutoCheckBox.isSelected()) {
            // Génération de la réponse automatique pour le champ contenu
            String aiResponse = generateAIResponse(reclamation != null ? reclamation.getContent() : "");
            contentTextArea.setText(aiResponse != null ? aiResponse : generatedResponse);
            contentTextArea.setEditable(false);

            // Définition du champ solution et le rendre non modifiable
            solutionTextField.setText("Nous allons essayer de trouver une solution.");
            solutionTextField.setEditable(false);
        } else {
            // Rendre le champ contenu modifiable et le vider
            contentTextArea.clear();
            contentTextArea.setEditable(true);

            // On vide le champ solution et on le rend modifiable (vous pouvez aussi conserver la saisie manuelle si souhaité)
            solutionTextField.clear();
            solutionTextField.setEditable(true);
        }
    }

    private String generateAIResponse(String reclamationContent) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonBody = new JSONObject()
                    .put("messages", new org.json.JSONArray()
                            .put(new JSONObject().put("role", "user").put("content", reclamationContent)))
                    .put("web_access", false)
                    .toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://chatgpt-42.p.rapidapi.com/chatgpt"))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("x-rapidapi-key", rapidApiKey)
                    .header("x-rapidapi-host", "chatgpt-42.p.rapidapi.com")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject jsonResponse = new JSONObject(response.body());

            if (jsonResponse.has("result") && !jsonResponse.getString("result").isEmpty()) {
                return jsonResponse.getString("result");
            } else {
                showAlert("Aucune réponse générée.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur API : " + e.getMessage(), Alert.AlertType.ERROR);
        }
        return null;
    }

    @FXML
    private void handleSubmit() {
        String content = contentTextArea.getText();
        String solution = solutionTextField.getText();
        LocalDateTime date = (datePicker.getValue() != null) ? datePicker.getValue().atStartOfDay() : LocalDateTime.now();

        if (content == null || content.trim().isEmpty()) {
            showAlert("Le contenu de la réponse ne peut pas être vide", Alert.AlertType.WARNING);
            return;
        }

        Reponses rep = new Reponses();
        rep.setContent(content);
        rep.setSolution(solution);
        rep.setDate(date);
        rep.setIsAuto(isAutoCheckBox.isSelected());
        rep.setReclamation(reclamation);
        rep.setStatus("En attente");

        reponsesService.ajouterReponse(rep);
        showAlert("Réponse envoyée avec succès", Alert.AlertType.INFORMATION);
        resetForm();

        if (dashboardController != null) {
            dashboardController.restoreCenter();
        }
    }

    @FXML
    private void handleBack() {
        if (dashboardController != null) {
            dashboardController.restoreCenter();
        } else {
            System.out.println("DashboardController non défini.");
        }
    }

    private void resetForm() {
        contentTextArea.clear();
        solutionTextField.clear();
        datePicker.setValue(LocalDate.now());
        isAutoCheckBox.setSelected(false);
        toggleContentField();
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
