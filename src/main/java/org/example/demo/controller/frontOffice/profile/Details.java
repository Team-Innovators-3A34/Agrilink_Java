package org.example.demo.controller.frontOffice.profile;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.example.demo.models.Ressources;
import org.example.demo.services.ressource.RessourcesService;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Details {
    @FXML
    private Text title;
    @FXML
    private Text typeText;
    @FXML
    private Text descriptionText;
    @FXML
    private Text statusText;
    @FXML
    private Text superficieText;
    @FXML
    private Text adresseText;
    @FXML
    private Text prixText;
    @FXML
    private ImageView ressourceImageView;

    private RessourcesService service = new RessourcesService();

    private Ressources ressource;

    public void setRessource(Ressources ressource) {
        this.ressource = ressource;
        afficherDetails();
    }

    private void afficherDetails() {
        if (ressource != null) {
            title.setText("Détails de " + ressource.getName());
            typeText.setText("Type: " + ressource.getType());
            descriptionText.setText("Description: " + ressource.getDescription());
            statusText.setText("Status: " + ressource.getStatus());
            superficieText.setText("Superficie: " + ressource.getSuperficie() + " m²");
            adresseText.setText("Adresse: " + ressource.getAdresse());
            prixText.setText("Prix Location: " + ressource.getPrixLocation() + " TND");

            String imageFileName = ressource.getImage();
            if (imageFileName != null && !imageFileName.isEmpty()) {
                String imagePath = "/images/" + imageFileName;
                try {
                    Image image = new Image(getClass().getResource(imagePath).toString());
                    ressourceImageView.setImage(image);
                } catch (NullPointerException e) {
                    System.out.println("Image profile not found");
                }
            } else {
                System.out.println("Image profile not found");
            }
        }
    }
}
