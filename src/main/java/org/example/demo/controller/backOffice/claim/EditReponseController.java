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

public class EditReponseController {

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

    // L'objet réponse à modifier
    private Reponses reponse;

    // Valeur par défaut si l'API ne répond pas
    private String generatedResponse = "Ceci est une réponse générée automatiquement.";
    // Clé API RapidAPI (remplacez-la par votre clé réelle)
    private String rapidApiKey = "b87ca822cdmsh0a0c4c3c7567bd9p1824ddjsn90c4848fb6c4";

    @FXML
    private void initialize() {
        // Initialisation de la date par défaut
        datePicker.setValue(LocalDate.now());
    }

    /**
     * Injection de la réponse à modifier.
     */
    public void setReponse(Reponses reponse) {
        this.reponse = reponse;
        if (reponse.getReclamation() != null) {
            titleLabel.setText("Modifier la réponse : " + reponse.getReclamation().getTitle());
        } else {
            titleLabel.setText("Modifier la réponse : []");
        }
        contentTextArea.setText(reponse.getContent());
        solutionTextField.setText(reponse.getSolution());
        if (reponse.getDate() != null) {
            datePicker.setValue(reponse.getDate().toLocalDate());
        } else {
            datePicker.setValue(LocalDate.now());
        }
        isAutoCheckBox.setSelected(reponse.getIsAuto());
        toggleContentField();
    }

    /**
     * Active ou désactive la modification des champs selon l'option Réponse auto.
     * Si la case est cochée, une réponse automatique est générée.
     */
    @FXML
    private void toggleContentField() {
        if (isAutoCheckBox.isSelected()) {
            String reclamationContent = "";
            if (reponse != null && reponse.getReclamation() != null) {
                reclamationContent = reponse.getReclamation().getContent();
            }
            String aiResponse = generateAIResponse(reclamationContent);
            contentTextArea.setText(aiResponse != null ? aiResponse : generatedResponse);
            contentTextArea.setEditable(false);
            solutionTextField.setText("Nous allons essayer de trouver une solution.");
            solutionTextField.setEditable(false);
        } else {
            contentTextArea.setEditable(true);
            solutionTextField.setEditable(true);
            // Vous pouvez décider de vider les champs ou de laisser la valeur existante
            // contentTextArea.clear();
            // solutionTextField.clear();
        }
    }

    /**
     * Génère une réponse automatique via l'API RapidAPI.
     *
     * @param reclamationContent le contenu de la réclamation
     * @return la réponse générée ou null en cas d'erreur
     */
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

    /**
     * Gère la sauvegarde des modifications.
     */
    @FXML
    private void handleSave() {
        String content = contentTextArea.getText().trim();
        String solution = solutionTextField.getText().trim();
        LocalDateTime date = (datePicker.getValue() != null)
                ? datePicker.getValue().atStartOfDay()
                : LocalDateTime.now();

        // ——— AJOUT : CONTROLES DE SAISIE ———
        if (content.isEmpty()) {
            showAlert("Le contenu ne peut pas être vide.", Alert.AlertType.WARNING);
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
        // ——————————————————————————

        // — le reste de ton code reste inchangé —
        reponse.setContent(content);
        reponse.setSolution(solution);
        reponse.setDate(date);
        reponse.setIsAuto(isAutoCheckBox.isSelected());


        reponsesService.modifierReponse(reponse);
        showAlert("La réponse a été modifiée avec succès.", Alert.AlertType.INFORMATION);
        resetForm();
        handleBack();
    }


    /**
     * Retourne à la vue des réclamations (ListClaim.fxml).
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/claim/ListClaim.fxml"));
            Parent root = loader.load();
            // Optionnel : si ListClaimController dispose d'une méthode pour rafraîchir la liste, vous pouvez l'appeler ici
            // ListClaimController controller = loader.getController();
            // controller.loadClaims();
            titleLabel.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Réinitialise le formulaire.
     */
    private void resetForm() {
        contentTextArea.clear();
        solutionTextField.clear();
        datePicker.setValue(LocalDate.now());
        isAutoCheckBox.setSelected(false);
        toggleContentField();
    }

    /**
     * Affiche une alerte à l'utilisateur.
     */
    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
