package org.example.demo.controller.frontOffice.profile;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.ressource.addDemandes;
import org.example.demo.controller.frontOffice.ressource.addRessources;
import org.example.demo.models.Ressources;
import org.example.demo.models.User;
import org.example.demo.models.recyclingpoint;
import org.example.demo.services.ressource.RessourcesService;

import java.io.IOException;

public class ressourceCard {

    @FXML
    private Text ressourcenameText;

    @FXML
    private Text ressourcetypeText;

    private Ressources ressource;
    private RessourcesService ressourceService;

    private User currentUser = new User();

    private Runnable refreshCallback;  // Callback to notify profileController to refresh

    @FXML
    private AnchorPane deleteRessource;

    @FXML
    private AnchorPane demanderRessource;

    @FXML
    private AnchorPane updateRessource;

    @FXML
    private AnchorPane viewRessource;
    @FXML
    private Slider ratingSlider;

    @FXML
    private AnchorPane ratingPane; // pour pouvoir l'afficher que si l'utilisateur est non-propriétaire

    private recyclingpoint recyclingpoint;

    @FXML private ImageView star1;
    @FXML private ImageView star2;
    @FXML private ImageView star3;
    @FXML private ImageView star4;
    @FXML private ImageView star5;
    @FXML private HBox starsBox;
    @FXML

    private void rate1() {
        selectedRating = 1;
        updateStars();
    }

    @FXML
    private void rate2() {
        selectedRating = 2;
        updateStars();
    }

    @FXML
    private void rate3() {
        selectedRating = 3;
        updateStars();
    }

    @FXML
    private void rate4() {
        selectedRating = 4;
        updateStars();
    }

    @FXML
    private void rate5() {
        selectedRating = 5;
        updateStars();
    }

    private void updateStars() {
        Image filled = new Image(getClass().getResourceAsStream("/images/star_filled.png"));
        Image empty = new Image(getClass().getResourceAsStream("/images/star_empty.png"));

        star1.setImage(selectedRating >= 1 ? filled : empty);
        star2.setImage(selectedRating >= 2 ? filled : empty);
        star3.setImage(selectedRating >= 3 ? filled : empty);
        star4.setImage(selectedRating >= 4 ? filled : empty);
        star5.setImage(selectedRating >= 5 ? filled : empty);
    }

    private int selectedRating = 0;
    private double selectedRating2 = 0;

    // Constructor or initialization method
    public ressourceCard() {
        this.ressourceService = new RessourcesService();
    }

    @FXML
    void onRateResourceClicked() {
        selectedRating2 = ratingSlider.getValue();
        double rounded = Math.round(selectedRating2 * 100.0) / 100.0;


        if (rounded == 0) {
            System.out.println("❌ Veuillez sélectionner une note avant de soumettre.");
            return;
        }

        double newRating = rounded;
        double currentRating = ressource.getRating();
        int currentRatingCount = ressource.getRatingCount();

        double newRatingValue;
        if (currentRatingCount > 0) {
            newRatingValue = ((currentRating * currentRatingCount) + newRating) / (currentRatingCount + 1);
        } else {
            newRatingValue = newRating;
        }

        ressource.setRating(newRatingValue);
        ressource.setRatingCount(currentRatingCount + 1);

        try {
            ressourceService.modifierRating(ressource); // méthode dans le service pour faire update dans la BD
            System.out.println("✅ Rating mis à jour !");
            if (refreshCallback != null) refreshCallback.run();
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de la mise à jour du rating : " + e.getMessage());
        }
    }

    public void setRessource(Ressources r, User currentUser) {
        this.ressource = r;
        this.currentUser = currentUser;

        ressourcenameText.setText(r.getName());
        ressourcetypeText.setText(r.getType());

        // Check if the current user is the owner of the resource
        if (currentUser.getId() == ressource.getUserId()) {
            // Show the delete and update buttons for the owner
            deleteRessource.setVisible(true);
            updateRessource.setVisible(true);
            demanderRessource.setVisible(false);  // Hide "Demander" button
            ratingPane.setVisible(false); // cacher le rating si c'est le propriétaire
            viewRessource.setVisible(true);  // Hide "View" button
        } else {
            // Show the "Demander" and "View" buttons for non-owners
            deleteRessource.setVisible(false);  // Hide delete button
            updateRessource.setVisible(false);  // Hide update button
            demanderRessource.setVisible(true);  // Show "Demander" button
            ratingPane.setVisible(true); // autoriser rating
            viewRessource.setVisible(true);  // Show "View" button
        }
    }

    public void setPoint(recyclingpoint r, User currentUser) {
        this.recyclingpoint = r;
        this.currentUser = currentUser;

        ressourcenameText.setText(r.getNom());
        ressourcetypeText.setText(r.getType());

        // Check if the current user is the owner of the resource
        if (currentUser.getId() == ressource.getUserId()) {
            // Show the delete and update buttons for the owner
            deleteRessource.setVisible(true);
            updateRessource.setVisible(true);
            demanderRessource.setVisible(false);  // Hide "Demander" button
            viewRessource.setVisible(true);  // Hide "View" button
        } else {
            // Show the "Demander" and "View" buttons for non-owners
            deleteRessource.setVisible(false);  // Hide delete button
            updateRessource.setVisible(false);  // Hide update button
            demanderRessource.setVisible(true);  // Show "Demander" button
            viewRessource.setVisible(true);  // Show "View" button
        }
    }

    public void setRefreshCallback(Runnable callback) {
        this.refreshCallback = callback;  // Set the callback from profileController
    }

    @FXML
    void onDeleteRessourceClicked() {
        if (ressource != null) {
            try {
                // Call the supprimer method in the service
                ressourceService.supprimer(ressource);
                System.out.println("✅ Ressource supprimée avec succès.");
                if (refreshCallback != null) {
                    refreshCallback.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    void onUpdateRessourceClicked() {
        try {
            HelloApplication.changeSceneWithController(
                    "/org/example/demo/fxml/Frontoffice/ressources/addRessource.fxml",
                    controller -> {
                        if (controller instanceof addRessources) {
                            ((addRessources) controller).setRessourcePourModification(ressource);
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onDemandeRessourceClicked() {
        try {
            HelloApplication.changeSceneWithController(
                    "/org/example/demo/fxml/Frontoffice/ressources/addDemande.fxml",
                    controller -> {
                        if (controller instanceof addDemandes) {
                            ((addDemandes) controller).setRessource(ressource);
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
