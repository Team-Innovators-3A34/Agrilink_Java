package org.example.demo.controller.backOffice.claim;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.demo.models.Reclamation;
import org.example.demo.services.claim.TranslationService; // Assurez-vous que ce service est bien importé
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

public class DetailsReclamationController {

    @FXML private Label idLabel;
    @FXML private Label nomUserLabel;
    @FXML private Label mailUserLabel;
    @FXML private Label titleLabel;
    // Pour le contenu, vous pouvez conserver un Label ou un Text en fonction de vos besoins.
    @FXML private Label contentLabel;
    @FXML private Label statusLabel;
    @FXML private Label dateLabel;
    @FXML private Label typeLabel;
    @FXML private Label prioriteLabel;
    @FXML private ImageView imageView;
    @FXML private ComboBox<String> languageComboBox;

    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private Reclamation currentRec;
    // Service de traduction utilisé dans la vue détail
    private TranslationService translationService = new TranslationService();

    @FXML
    public void initialize() {
        // Initialisation du ComboBox avec les langues disponibles
        languageComboBox.setItems(FXCollections.observableArrayList("en", "fr", "es"));
        languageComboBox.getSelectionModel().select("fr"); // langue par défaut

        // Configuration du rendu personnalisé du ComboBox pour afficher les drapeaux
        languageComboBox.setCellFactory(listView -> new ListCell<String>() {
            private final ImageView flagView = new ImageView();
            {
                flagView.setFitWidth(20);
                flagView.setFitHeight(15);
            }
            @Override
            protected void updateItem(String lang, boolean empty) {
                super.updateItem(lang, empty);
                if (empty || lang == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(lang.toUpperCase());
                    String imagePath = "/icons/" + lang + ".png";
                    InputStream is = getClass().getResourceAsStream(imagePath);
                    if (is != null) {
                        flagView.setImage(new Image(is));
                        setGraphic(flagView);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        languageComboBox.setButtonCell(new ListCell<String>() {
            private final ImageView flagView = new ImageView();
            {
                flagView.setFitWidth(20);
                flagView.setFitHeight(15);
            }
            @Override
            protected void updateItem(String lang, boolean empty) {
                super.updateItem(lang, empty);
                if (empty || lang == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(lang.toUpperCase());
                    String imagePath = "/icons/" + lang + ".png";
                    InputStream is = getClass().getResourceAsStream(imagePath);
                    if (is != null) {
                        flagView.setImage(new Image(is));
                        setGraphic(flagView);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });

        // Ajout d'un écouteur pour déclencher la traduction lors du changement de langue
        languageComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && currentRec != null) {
                updateTranslations(newVal);
            }
        });
    }

    /**
     * Initialise les données de la réclamation et affiche les informations.
     * L'ID est affiché, les champs textuels sont traduits selon la langue sélectionnée,
     * et l'image associée est chargée.
     * @param rec La réclamation à afficher.
     */
    public void initData(Reclamation rec) {
        this.currentRec = rec;
        nomUserLabel.setText(rec.getNomUser());
        mailUserLabel.setText(rec.getMailUser());
        dateLabel.setText(rec.getDate() != null ? rec.getDate().format(dateFormatter) : "N/A");

        // Chargement de l'image associée à la réclamation
        String imagePath;
        if (rec.getImage() != null && !rec.getImage().isEmpty()){
            imagePath = "file:C:/Users/user/IdeaProjects/reclamattion/uploads/images/" + rec.getImage();
        } else {
            imagePath = "file:C:/Users/user/IdeaProjects/reclamattion/uploads/images/default.png";
        }
        imageView.setImage(new Image(imagePath));

        // Affichage initial avec la langue par défaut
        updateTranslations(languageComboBox.getValue());
    }

    /**
     * Met à jour la traduction des champs textuels selon la langue sélectionnée.
     * L’ID n’est pas soumis à traduction puisqu’il s’agit d’un identifiant numérique.
     * @param targetLanguage La langue cible ("en", "fr", "es", …).
     */
    private void updateTranslations(String targetLanguage) {
        if (translationService != null && !targetLanguage.trim().isEmpty()) {
            titleLabel.setText(translationService.translateText(currentRec.getTitle(), targetLanguage));
            contentLabel.setText(translationService.translateText(currentRec.getContent(), targetLanguage));
            // Le statut est mis en majuscules après traduction
            statusLabel.setText(translationService.translateText(currentRec.getStatus(), targetLanguage).toUpperCase());
            typeLabel.setText(translationService.translateText(getTypeName(currentRec.getType()), targetLanguage));
            prioriteLabel.setText(translationService.translateText(String.valueOf(currentRec.getPriorite()), targetLanguage));
        } else {
            titleLabel.setText(currentRec.getTitle());
            contentLabel.setText(currentRec.getContent());
            statusLabel.setText(currentRec.getStatus().toUpperCase());
            typeLabel.setText(getTypeName(currentRec.getType()));
            prioriteLabel.setText(String.valueOf(currentRec.getPriorite()));
        }
        // Par exemple, ajouter une classe CSS au statut pour mise en forme spécifique
        statusLabel.getStyleClass().add("status-tag");
    }

    /**
     * Retourne le nom descriptif du type de réclamation.
     * @param type un entier représentant le type
     * @return la chaîne descriptive correspondante
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
     * Action liée au bouton "Retour à la liste" pour revenir à la scène précédente.
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/claim/ListClaim.fxml"));
            Parent root = loader.load();
            idLabel.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
