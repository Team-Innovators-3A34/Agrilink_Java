package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.example.entities.Reclamation;
import org.example.services.TranslationService;

import java.time.format.DateTimeFormatter;

public class DetailsReclamationController {

    @FXML private Label idLabel;
    @FXML private Label nomUserLabel;
    @FXML private Label mailUserLabel;
    @FXML private Label titleLabel;
    @FXML private Text contentLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateLabel;
    @FXML private Label typeLabel;
    @FXML private Label prioriteLabel;
    @FXML private ImageView imageView;
    // ComboBox pour la sélection de la langue dans la vue détail
    @FXML private ComboBox<String> languageComboBox;

    private DashboardController dashboardController;
    private Reclamation currentRec;
    // Service de traduction utilisé dans la vue détail
    private TranslationService translationService = new TranslationService();

    @FXML
    public void initialize() {
        // Initialisation du ComboBox des langues disponibles
        languageComboBox.setItems(FXCollections.observableArrayList("en", "fr", "es"));
        languageComboBox.getSelectionModel().select("fr"); // langue par défaut

        // Ajout d'un écouteur sur le ComboBox pour déclencher la traduction à la volée
        languageComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && currentRec != null) {
                updateTranslations(newVal);
            }
        });
    }

    /**
     * Initialise les données de la réclamation et affiche les informations de base.
     */
    public void initData(Reclamation rec, DashboardController dashboardController) {
        this.dashboardController = dashboardController;
        this.currentRec = rec;

        idLabel.setText(String.valueOf(rec.getId()));
        nomUserLabel.setText(rec.getNomUser());
        mailUserLabel.setText(rec.getMailUser());
        dateLabel.setText(rec.getDate() != null ? rec.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");

        if (rec.getImage() != null && !rec.getImage().isEmpty()){
            imageView.setImage(new Image("file:uploads/images/" + rec.getImage()));
        }

        // Affichage initial dans la langue par défaut
        updateTranslations(languageComboBox.getValue());
    }

    /**
     * Met à jour les champs traduits selon la langue choisie.
     */
    private void updateTranslations(String targetLanguage) {
        if (translationService != null && !targetLanguage.trim().isEmpty()) {
            titleLabel.setText(translationService.translateText(currentRec.getTitle(), targetLanguage));
            contentLabel.setText(translationService.translateText(currentRec.getContent(), targetLanguage));
            statusLabel.setText(translationService.translateText(currentRec.getStatus(), targetLanguage));
            typeLabel.setText(translationService.translateText(getTypeName(currentRec.getType()), targetLanguage));
            prioriteLabel.setText(translationService.translateText(String.valueOf(currentRec.getPriorite()), targetLanguage));
        } else {
            titleLabel.setText(currentRec.getTitle());
            contentLabel.setText(currentRec.getContent());
            statusLabel.setText(currentRec.getStatus());
            typeLabel.setText(getTypeName(currentRec.getType()));
            prioriteLabel.setText(String.valueOf(currentRec.getPriorite()));
        }
        // Mise en forme spécifique, par exemple en majuscules pour le statut
        statusLabel.setText(statusLabel.getText().toUpperCase());
        statusLabel.getStyleClass().add("status-tag");
    }

    /**
     * Mappe l'entier représentant le type vers une chaîne descriptive.
     */
    private String getTypeName(int type) {
        switch(type) {
            case 1: return "Type 1";
            case 2: return "Type 2";
            case 3: return "Type 3";
            default: return "N/A";
        }
    }

    /**
     * Retour à la vue principale.
     */
    @FXML
    private void handleBack(){
        if (dashboardController != null) {
            dashboardController.restoreCenter();
        }
    }
}
