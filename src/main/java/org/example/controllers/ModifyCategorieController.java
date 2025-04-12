package org.example.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import org.example.entities.TypeRec;
import org.example.services.TypeRecService;

import java.io.IOException;

public class ModifyCategorieController {

    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;

    private BorderPane root;
    private TypeRec typeRec;
    private TypeRecService typeRecService = new TypeRecService();

    public void setRoot(BorderPane root) {
        this.root = root;
    }

    public void setTypeRec(TypeRec typeRec) {
        this.typeRec = typeRec;
        populateFields();
    }

    private void populateFields() {
        if (typeRec != null) {
            nomField.setText(typeRec.getNom());
            descriptionArea.setText(typeRec.getDescription());
        }
    }

    @FXML
    private void modifierCategorie() {
        String nom = nomField.getText();
        String description = descriptionArea.getText();

        if (nom.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis !");
            return;
        }

        typeRec.setNom(nom);
        typeRec.setDescription(description);
        boolean updated = typeRecService.modifierTypeRec(typeRec);
        if (updated) {
            showAlert("Succès", "Catégorie modifiée avec succès !");
            retourListeCategorie();
        } else {
            showAlert("Erreur", "La modification a échoué !");
        }
    }

    @FXML
    private void retourListeCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listCategorie.fxml"));
            Parent listRoot = loader.load();
            root.setCenter(listRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
