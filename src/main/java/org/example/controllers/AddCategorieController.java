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

public class AddCategorieController {

    // Les champs pour le formulaire ne comporteront que le nom et la description
    @FXML private TextField nomField;
    @FXML private TextArea descriptionArea;

    // Référence à la racine principale pour permettre le retour vers le dashboard
    // (pour remplacer la scène entière)
    private BorderPane root;

    private TypeRecService typeRecService = new TypeRecService();

    public void setRoot(BorderPane root) {
        this.root = root;
    }

    @FXML
    private void ajouterCategorie() {
        // Récupération des valeurs du formulaire
        String nom = nomField.getText();
        String description = descriptionArea.getText();

        // Vérifier que tous les champs sont remplis
        if (nom.isEmpty() || description.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis !");
            return;
        }

        // Création de l'objet TypeRec (l'ID est auto-généré en base)
        TypeRec typeRec = new TypeRec(nom, description);
        boolean inserted = typeRecService.ajouterTypeRec(typeRec);
        if (inserted) {
            showAlert("Succès", "Catégorie ajoutée avec succès !");
            retourDashboard();
        } else {
            showAlert("Erreur", "L'ajout de la catégorie a échoué !");
        }
    }

    @FXML
    private void retourDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            // Remplacez l'ensemble de la scène pour éviter la duplication du sidebar
            root.getScene().setRoot(dashboardRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le tableau de bord.");
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
