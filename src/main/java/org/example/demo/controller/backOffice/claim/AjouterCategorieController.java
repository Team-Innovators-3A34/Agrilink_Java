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

public class AjouterCategorieController {

    @FXML
    private TextField catName;

    @FXML
    private TextArea catDescription;

    @FXML
    private Button btnAjouter;

    private final TypeRecService typeRecService = new TypeRecService();

    @FXML
    private void initialize() {
        btnAjouter.setOnAction(e -> ajouterTypeRec());
    }

    private void ajouterTypeRec() {
        String nom = catName.getText();
        String description = catDescription.getText();

        if (nom == null || nom.trim().isEmpty() ||
                description == null || description.trim().isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis !");
            return;
        }
        // Contrôle de saisie : nom entre 3 et 10 caractères, description entre 5 et 30 caractères
        if (nom.trim().length() < 3 || nom.trim().length() > 10) {
            showAlert("Erreur", "Le nom doit contenir entre 3 et 10 caractères !");
            return;
        }
        if (description.trim().length() < 5 || description.trim().length() > 30) {
            showAlert("Erreur", "La description doit contenir entre 5 et 30 caractères !");
            return;
        }

        TypeRec typeRec = new TypeRec();
        typeRec.setNom(nom.trim());
        typeRec.setDescription(description.trim());

        boolean inserted = typeRecService.ajouterTypeRec(typeRec);
        if (inserted) {
            showAlert("Succès", "Type de réclamation ajouté avec succès !");
            // Effacer les champs
            catName.clear();
            catDescription.clear();
            // Retourner à la page de liste des catégories
            retourListeCategories();
        } else {
            showAlert("Erreur", "L'ajout a échoué !");
        }
    }

    // Recharge la page de liste des catégories (liste complète de la vue)
    private void retourListeCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/claim/listCategories.fxml"));
            Parent listCateRoot = loader.load();
            // Remplacer la racine de la scène par la vue de liste
            btnAjouter.getScene().setRoot(listCateRoot);
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
}
