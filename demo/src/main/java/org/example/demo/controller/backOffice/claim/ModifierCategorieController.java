package org.example.demo.controller.backOffice.claim;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.example.demo.models.TypeRec;
import org.example.demo.services.claim.TypeRecService;

import java.io.IOException;

public class ModifierCategorieController {

    @FXML
    private TextField nomField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Button updateButton;

    private final TypeRecService typeRecService = new TypeRecService();

    private TypeRec typeRec;

    @FXML
    public void initialize() {
        updateButton.setOnAction(event -> modifierCategorie());
    }

    private void modifierCategorie() {
        String nom = nomField.getText().trim();
        String description = descriptionField.getText().trim();

        if (nom.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis !");
            return;
        }
        if (nom.length() < 3 || nom.length() > 10) {
            showAlert("Erreur", "Le nom doit contenir entre 3 et 10 caractères !");
            return;
        }
        if (description.length() < 5 || description.length() > 30) {
            showAlert("Erreur", "La description doit contenir entre 5 et 30 caractères !");
            return;
        }

        typeRec.setNom(nom);
        typeRec.setDescription(description);

        boolean updated = typeRecService.modifierTypeRec(typeRec);
        if (updated) {
            showAlert("Succès", "Catégorie modifiée avec succès !");
            // Optionnel : vider les champs (ceci est rarement nécessaire si l'on retourne à la liste)
            nomField.clear();
            descriptionField.clear();
            retourListeCategories();
        } else {
            showAlert("Erreur", "La modification a échoué !");
        }
    }

    private void retourListeCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/claim/listCategories.fxml"));
            Parent listCateRoot = loader.load();
            // Remplace la racine de la scène actuelle par la vue liste des catégories
            updateButton.getScene().setRoot(listCateRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de retourner à la liste des catégories.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setTypeRec(TypeRec typeRec) {
        this.typeRec = typeRec;
        nomField.setText(typeRec.getNom());
        descriptionField.setText(typeRec.getDescription());
    }
}
