package org.example.demo.controller.frontOffice.ressource;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.example.demo.HelloApplication;
import org.example.demo.models.Demandes;
import org.example.demo.models.Ressources;
import org.example.demo.models.User;
import org.example.demo.services.ressource.DemandesService;
import org.example.demo.services.ressource.LanguageToolValidator;
import org.example.demo.services.ressource.PredictionService;
import org.example.demo.utils.sessionManager;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.List;

public class addDemandes {

    @FXML
    private ImageView backtosettings;
    @FXML
    private Button btnAjouter;
    @FXML
    private ComboBox<String> comboPriorite;
    @FXML
    private DatePicker dateExpire;
    @FXML
    private Text message;
    @FXML
    private Text title;
    private User user = new User();
    @FXML
    private TextArea txtMessage;
    private Ressources ressourceId;
    private final DemandesService demandeService = new DemandesService();

    private Demandes selectedDemande = null;

    public void setDemande(Demandes demande) {
        this.selectedDemande = demande;
        this.ressourceId = new Ressources();
        ressourceId.setId(demande.getRessourceId());
        ressourceId.setUserId(demande.getToUserId());

        // Pré-remplir le formulaire
        txtMessage.setText(demande.getMessage());
        comboPriorite.setValue(demande.getPriorite());
        dateExpire.setValue(demande.getExpireDate());
        title.setText("Modifier la Demande");
        btnAjouter.setText("Mettre à jour");
    }

    @FXML
    public void initialize() {
        comboPriorite.getItems().addAll("Haute", "Moyenne", "Basse"); // valeur par défaut
        user = sessionManager.getInstance().getUser();
    }

    public void setRessource(Ressources ressourceId) {
        this.ressourceId = ressourceId;
        System.out.println("Ressource ID reçu = " + ressourceId); // juste pour vérifier
    }

    @FXML
    void ajouterDemande(ActionEvent event) {
        try {
            LocalDate createdAt = LocalDate.now();
            LocalDate expireAt = dateExpire.getValue();
            String message = txtMessage.getText().trim();
            String priorite = comboPriorite.getValue();

            if (expireAt == null || expireAt.isBefore(createdAt)) {
                HelloApplication.error("La date d'expiration doit être supérieure à la date actuelle.");
                return;
            }

            if (message.isEmpty() || priorite == null) {
                HelloApplication.error("Veuillez remplir tous les champs.");
                return;
            }

            // 🔥 Ici tu ajoutes le contrôle de correction grammaticale
            List<String> erreurs = LanguageToolValidator.getDescriptionErrors(message);
            if (erreurs.size() > 2) {
                Alert alert2 = new Alert(Alert.AlertType.WARNING);
                alert2.setTitle("Message incorrect");
                alert2.setHeaderText("Le message contient trop d'erreurs :");

                StringBuilder content = new StringBuilder();
                for (String erreur : erreurs) {
                    content.append(erreur).append("\n\n");
                }

                alert2.setContentText(content.toString());
                alert2.showAndWait();
                return; // Ne pas continuer si trop d'erreurs
            }
            // Appel de l'IA pour prédiction
            String expireDateStr = expireAt.toString();

            String predictionJson = PredictionService.predictStatus(expireDateStr, message);
            System.out.println("Réponse brute : " + predictionJson);

            JSONObject jsonObject = new JSONObject(predictionJson);
            String prediction = jsonObject.getString("prediction");

            String status = prediction.equalsIgnoreCase("approuvé") ? "Approuvée" :
                    prediction.equalsIgnoreCase("rejeté") ? "Rejetée" : "Inconnue";

            System.out.println("Statut final : " + status);

            // Message d'information à l'utilisateur
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Prédiction IA");
            alert.setHeaderText("Statut prédit pour cette demande :");
            alert.setContentText("La demande est probablement : " + status);
            alert.showAndWait();

            if (selectedDemande != null) {
                // Mise à jour
                selectedDemande.setMessage(message);
                selectedDemande.setPriorite(priorite);
                selectedDemande.setExpireDate(expireAt);

                demandeService.modifier(selectedDemande); // tu dois créer cette méthode dans le service
                HelloApplication.succes("Demande modifiée avec succès !");
            } else {
                // Nouvelle demande
                Demandes demande = new Demandes(createdAt, expireAt, message, priorite);
                demande.setRessourceId(ressourceId.getId());
                demande.setToUserId(ressourceId.getUserId());
                demande.setUserId(user.getId());
                //  Envoi du SMS
             /*   String numeroTest = "+21698476000"; // change par ton vrai numéro de test
                String contenuSms = "Votre demande a été enregistrée avec priorité : " + priorite;
                SmsService.envoyerSms(numeroTest, contenuSms);*/
                demandeService.ajouter(demande);
                HelloApplication.succes("Demande ajoutée avec succès !");
            }

            HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");

        } catch (Exception e) {
            HelloApplication.error("Erreur lors de l'enregistrement : " + e.getMessage());
        }
    }


    @FXML
    void backtosettings(MouseEvent event) {

    }

}
