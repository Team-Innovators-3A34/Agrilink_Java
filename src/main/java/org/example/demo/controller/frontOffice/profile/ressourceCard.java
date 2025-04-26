package org.example.demo.controller.frontOffice.profile;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
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
import java.sql.SQLException;

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
    private ImageView ressourceImage;


    private int selectedRating = 0;
    public ressourceCard() {
        this.ressourceService = new RessourcesService();
    }

    @FXML
    void onRateResourceClicked() {
        try {
            int oldCount = ressource.getRatingCount();
            double oldRating = ressource.getRating();

            int newCount = oldCount + 1;
            double newRating = ((oldRating * oldCount) + selectedRating) / newCount;

            ressource.setRating(newRating);
            ressource.setRatingCount(newCount);

            ressourceService.modifierRating(ressource);

            System.out.println("Note soumise : " + selectedRating);
            // 🎯 Afficher une alerte de confirmation
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Merci pour votre avis !");
            alert.setHeaderText(null);
            alert.setContentText("Votre note a été enregistrée avec succès !");
            alert.showAndWait();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    public void setRessource(Ressources r, User currentUser) {
        this.ressource = r;
        this.currentUser = currentUser;

        ressourcenameText.setText(r.getName());
        ressourcetypeText.setText(r.getType());
        String imageFileName = ressource.getImage();
        if (imageFileName != null && !imageFileName.isEmpty()) {
            String imagePath = "/images/" + imageFileName;
            try {
                Image image = new Image(getClass().getResource(imagePath).toString());
                ressourceImage.setImage(image);
            } catch (NullPointerException e) {
                System.out.println("Image ressource not found");
            }
        } else {
            System.out.println("Image ressource not found");
        }


        if (currentUser.getId() == ressource.getUserId()) {
            deleteRessource.setVisible(true);
            updateRessource.setVisible(true);
            demanderRessource.setVisible(false);
            viewRessource.setVisible(true);
            ratingPane.setVisible(true);

            ratingPane.getChildren().clear();
            double Rating=ressource.getRating();
            HBox starBox = createReadOnlyStarsWithImages(Rating);
            System.out.println("Rating récupéré: " + ressource.getRating());

            ratingPane.getChildren().add(starBox);
        } else {
            deleteRessource.setVisible(false);
            updateRessource.setVisible(false);
            demanderRessource.setVisible(true);
            viewRessource.setVisible(true);
            ratingPane.setVisible(true);

            ratingPane.getChildren().clear();

            double rating = ressource.getRating();
            HBox starBox = createStarRating(rating);
            ratingPane.getChildren().add(starBox);
        }
    }

    // ✨ Nouvelle méthode pour afficher les étoiles en utilisant des images
    /*private HBox createReadOnlyStarsWithImages(double rating) {
        HBox starsBox = new HBox();
        starsBox.setSpacing(5);

        int fullStars = (int) Math.floor(rating);

        for (int i = 1; i <= 5; i++) {
            ImageView starImage = new ImageView();
            starImage.setFitHeight(15);
            starImage.setFitWidth(15);

            if (i <= fullStars) {
                starImage.setImage(new javafx.scene.image.Image("images/star.png"));
            } else {
                starImage.setImage(new javafx.scene.image.Image("images/star-disable.png"));
            }

            starsBox.getChildren().add(starImage);
        }

        return starsBox;
    }*/
    // ✨ Nouvelle méthode pour afficher les étoiles en utilisant les caractères ★ et ☆
    private HBox createReadOnlyStarsWithImages(double rating) {
        HBox starsBox = new HBox();
        starsBox.setSpacing(5);

        double fullStars = rating;

        for (int i = 1; i <= 5; i++) {
            Label star = new Label();

            if (i <= fullStars) {
                star.setText("★");
                star.setStyle("-fx-font-size: 18px; -fx-text-fill: gold;");
            } else {
                star.setText("☆");
                star.setStyle("-fx-font-size: 18px; -fx-text-fill: gray;");
            }

            starsBox.getChildren().add(star);
        }

        return starsBox;
    }

    private HBox createReadOnlyStars(double rating) {
        HBox starsBox = new HBox();
        starsBox.setSpacing(5);

        int fullStars = (int) Math.floor(rating); // ⭐ arrondi vers le bas pour les étoiles pleines

        for (int i = 1; i <= 5; i++) {
            Label star = new Label();

            if (i <= fullStars) {
                star.setText("★");
                star.setStyle("-fx-font-size: 18px; -fx-text-fill: gold;");
            } else {
                star.setText("☆");
                star.setStyle("-fx-font-size: 18px; -fx-text-fill: gray;");
            }

            starsBox.getChildren().add(star);
        }

        return starsBox;
    }

    private HBox createStarRating(double rating) {
        HBox starsBox = new HBox();
        starsBox.setSpacing(5);
        for (int i = 1; i <= 5; i++) {
            Label star = new Label();
            star.setText(i <= rating ? "★" : "☆");
            star.setStyle("-fx-font-size: 18px; -fx-text-fill: gold;");

            int currentRating = i;

            // Ajouter un évènement pour rendre l'étoile cliquable
            star.setOnMouseClicked(event -> {
                // Mettre à jour la note sélectionnée
                selectedRating = currentRating;
                updateStarVisual(starsBox, selectedRating); // Mettre à jour les étoiles visuellement
            });

            starsBox.getChildren().add(star);
        }

        return starsBox;
    }
    private void updateStarVisual(HBox starsBox, double rating) {
        for (int i = 0; i < starsBox.getChildren().size(); i++) {
            Label star = (Label) starsBox.getChildren().get(i);
            if (i < rating) {
                star.setText("★");  // Remplacer par une étoile pleine
                star.setStyle("-fx-font-size: 18px; -fx-text-fill: gold;");
            } else {
                star.setText("☆");  // Remplacer par une étoile vide
                star.setStyle("-fx-font-size: 18px; -fx-text-fill: gray;");
            }
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
