package org.example.demo.controller.frontOffice.ressource;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.example.demo.HelloApplication;
import org.example.demo.models.Ressources;
import org.example.demo.models.User;
import org.example.demo.services.ressource.LanguageToolValidator;
import org.example.demo.services.ressource.RessourcesService;
import org.example.demo.utils.sessionManager;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class addRessources implements Initializable {
    @FXML
    private Button ajout;

    @FXML
    private Text title;

    @FXML
    private ImageView backtosettings;

    @FXML
    private Button image;

    @FXML
    private TextField txtAdresse;

    @FXML
    private TextField txtDescription;

    @FXML
    private TextField txtImage;

    @FXML
    private TextField txtMarque;

    @FXML
    private TextField txtNom;

    @FXML
    private TextField txtPrix;

    @FXML
    private ComboBox<String> txtStatus;

    @FXML
    private TextField txtSuperficie;

    @FXML
    private ComboBox<String> txtType;

    private final RessourcesService ps = new RessourcesService();

    private Ressources ressourceAModifier = null;

    private User user ;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        txtType.getItems().addAll("materiel", "terrain");
        txtStatus.getItems().addAll("disponible", "indisponible");
        user = sessionManager.getInstance().getUser();
    }

    public void setRessourcePourModification(Ressources r) {
        this.ressourceAModifier = r;

        txtNom.setText(r.getName());
        txtType.setValue(r.getType());
        txtStatus.setValue(r.getStatus());
        txtAdresse.setText(r.getAdresse());
        txtDescription.setText(r.getDescription());
        txtPrix.setText(String.valueOf(r.getPrixLocation()));
        txtSuperficie.setText(String.valueOf(r.getSuperficie()));
        txtMarque.setText(r.getMarque());
        txtImage.setText(r.getImage());
        title.setText("Modifier votre ressource");
    }

    @FXML
    void ajouterRessource(ActionEvent event) {
        try {
            String description = txtDescription.getText();

            // 🛑 Vérification de champs vides
            if (txtNom.getText().isEmpty() || txtType.getValue() == null ||
                    txtStatus.getValue() == null || txtAdresse.getText().isEmpty() ||
                    txtDescription.getText().isEmpty() || txtPrix.getText().isEmpty() ||
                    txtSuperficie.getText().isEmpty() || txtMarque.getText().isEmpty()) {

                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champs manquants");
                alert.setHeaderText(null);
                alert.setContentText("Veuillez remplir tous les champs.");
                alert.showAndWait();
                return;
            }

            // ✅ Vérification sémantique via RapidAPI
            List<String> erreurs = LanguageToolValidator.getDescriptionErrors(description);
            if (erreurs.size() > 2) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Description incorrecte");
                alert.setHeaderText("La description contient trop d'erreurs :");

                StringBuilder content = new StringBuilder();
                for (String erreur : erreurs) {
                    content.append(erreur).append("\n\n");
                }

                alert.setContentText(content.toString());
                alert.showAndWait();
                return;
            }



            // 🔢 Vérification des valeurs numériques
            double prix = Double.parseDouble(txtPrix.getText());
            double superficie = Double.parseDouble(txtSuperficie.getText());

            if (prix <= 0 || superficie <= 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Valeurs incorrectes");
                alert.setHeaderText(null);
                alert.setContentText("Le Prix et la Superficie doivent être des valeurs positives.");
                alert.showAndWait();
                return;
            }

            if (ressourceAModifier == null) {
                // ➕ Ajout d'une nouvelle ressource
                Ressources r = new Ressources(
                        txtType.getValue(),
                        description,
                        txtStatus.getValue(),
                        txtNom.getText(),
                        txtMarque.getText(),
                        prix,
                        superficie,
                        txtAdresse.getText(),
                        txtImage.getText()
                );

                r.setUserId(user.getId());
                ps.ajouter(r);
                HelloApplication.succes("Ressource ajoutée avec succès !");
                HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");

            } else {
                // ✏️ Modification d'une ressource existante
                ressourceAModifier.setName(txtNom.getText());
                ressourceAModifier.setType(txtType.getValue());
                ressourceAModifier.setStatus(txtStatus.getValue());
                ressourceAModifier.setAdresse(txtAdresse.getText());
                ressourceAModifier.setDescription(description);
                ressourceAModifier.setPrixLocation(prix);
                ressourceAModifier.setMarque(txtMarque.getText());
                ressourceAModifier.setSuperficie(superficie);
                ressourceAModifier.setImage(txtImage.getText());

                ps.modifier(ressourceAModifier);
                HelloApplication.succes("Ressource modifiée avec succès !");
                HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
            }

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de saisie");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez entrer des valeurs numériques valides pour le prix et la superficie.");
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue : " + e.getMessage());
            alert.showAndWait();
        }
    }


    @FXML
    void selectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            txtImage.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void backtosettings(MouseEvent event) {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
    }
}
