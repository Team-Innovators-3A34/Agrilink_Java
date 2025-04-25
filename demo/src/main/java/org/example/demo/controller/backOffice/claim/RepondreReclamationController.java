package org.example.demo.controller.backOffice.claim;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import org.example.demo.models.Reclamation;
import org.example.demo.models.Reponses;
import org.example.demo.services.claim.ReponsesService;
import org.json.JSONObject;

import java.io.IOException;
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

    @FXML
    private WebView mapView;

    // Service pour la gestion des réponses
    private ReponsesService reponsesService = new ReponsesService();

    // Réponse par défaut si l'API ne répond pas
    private String generatedResponse = "Ceci est une réponse générée automatiquement.";

    // Clé API RapidAPI (à remplacer par votre clé réelle)
    private String rapidApiKey = "b87ca822cdmsh0a0c4c3c7567bd9p1824ddjsn90c4848fb6c4";

    // La réclamation pour laquelle on répond
    private Reclamation reclamation;

    @FXML
    private void initialize() {
        // Initialisation de la date par défaut
        datePicker.setValue(LocalDate.now());
        // Initialisation du WebView ou d'autres composants si nécessaire.
    }

    /**
     * Injection de la réclamation à traiter.
     *
     * @param reclamation l'objet réclamation
     */
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

            // Remplissage du champ solution et le rendre non modifiable
            solutionTextField.setText("Nous allons essayer de trouver une solution.");
            solutionTextField.setEditable(false);
        } else {
            // Rendre le champ contenu modifiable et le vider
            contentTextArea.clear();
            contentTextArea.setEditable(true);

            // Vider le champ solution et le rendre modifiable
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
        String content = contentTextArea.getText().trim();
        String solution = solutionTextField.getText().trim();
        LocalDateTime date = (datePicker.getValue() != null)
                ? datePicker.getValue().atStartOfDay()
                : LocalDateTime.now();

        // ——— AJOUT : CONTROLES DE SAISIE ———
        if (content.isEmpty()) {
            showAlert("Le contenu de la réponse ne peut pas être vide.", Alert.AlertType.WARNING);
            return;
        }
        if (content.length() < 5) {
            showAlert("Le contenu doit comporter au moins 5 caractères.", Alert.AlertType.WARNING);
            return;
        }
        if (solution.isEmpty()) {
            showAlert("La solution ne peut pas être vide.", Alert.AlertType.WARNING);
            return;
        }
        if (solution.length() < 5) {
            showAlert("La solution doit comporter au moins 5 caractères.", Alert.AlertType.WARNING);
            return;
        }
        // ——————————————————————————————

        // — le reste de ton code reste inchangé —
        Reponses rep = new Reponses();
        rep.setContent(content);
        rep.setSolution(solution);
        rep.setDate(date);
        rep.setIsAuto(isAutoCheckBox.isSelected());
        rep.setReclamation(reclamation);
        rep.setStatus("En attente");

        reponsesService.ajouterReponse(rep);
        showAlert("Réponse envoyée avec succès.", Alert.AlertType.INFORMATION);
        resetForm();
        handleBack();
    }


    /**
     * Méthode appelée lorsqu'on clique sur le bouton "Retour à la liste".
     * Elle navigue vers la page de la liste des réclamations en remplaçant la racine de la scène.
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/claim/ListClaim.fxml"));
            Parent root = loader.load();
            titleLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
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
