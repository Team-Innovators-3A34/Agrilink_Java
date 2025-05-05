package org.example.demo.controller.frontOffice.pointRecyclage;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.models.recyclingpoint;
import org.example.demo.services.recyclingpoint.recyclingpointService;
import org.example.demo.utils.sessionManager;

import java.io.File;

public class addRecyclingPoint {

    @FXML
    private TextField adresseField;

    @FXML
    private ImageView backtosettings;

    @FXML
    private TextArea descriptionField;

    @FXML
    private Button image;

    @FXML
    private WebView mapView;

    @FXML
    private TextField nomField;

    @FXML
    private Text title;

    @FXML
    private TextField txtImage;

    @FXML
    private ComboBox<String > typeField;

    private User user;
    private final recyclingpointService recyclingPointService = new recyclingpointService();
    private recyclingpoint pointAModifier;

    private double latitude;
    private double longitude;




    public void setPointPourModification(recyclingpoint r) {
        this.pointAModifier = r;

        nomField.setText(r.getNom());
        adresseField.setText(r.getType());
        descriptionField.setText(r.getDescription());
        txtImage.setText(r.getAdresse());
        typeField.setValue(r.getType());
        title.setText("Modifier votre Point");
    }

    @FXML
    private void initialize() {
        user = sessionManager.getInstance().getUser();

        typeField.getItems().addAll(
                "Plastique",
                "Verre",
                "Papier",
                "Métal",
                "Déchets électroniques"
        );

    }

    @FXML
    void backtosettings(MouseEvent event) {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
    }

    @FXML
    void onAddpoint(ActionEvent event) {
        String nom = nomField.getText();
        String adresse = adresseField.getText();
        String description = descriptionField.getText();
        String imagePath = txtImage.getText();
        String type = typeField.getSelectionModel().getSelectedItem();

        if(pointAModifier == null) {

            if (nom.isEmpty() || adresse.isEmpty() || description.isEmpty() || imagePath.isEmpty()) {
                HelloApplication.error("Veuillez remplir tous les champs.");
                return;
            }

            // Coordonnées (à adapter si récupérées depuis la carte)
            double lat = 12;
            double lon = 12;

            recyclingpoint point = new recyclingpoint();
            point.setNom(nom);
            point.setAdresse(adresse);
            point.setDescription(description);
            point.setImage(imagePath);
            point.setLatitude(lat);
            point.setLongitude(lon);
            point.setOwner_id(user.getId());
            point.setType(type);

            boolean success = recyclingPointService.ajouter(point);

            if (success) {
                HelloApplication.succes("✅ Point de recyclage ajouté avec succès !");
                HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
            } else {
                HelloApplication.error("❌ Échec de l'ajout du point.");
            }
        }else {
            pointAModifier.setNom(nom);
            pointAModifier.setAdresse(adresse);
            pointAModifier.setDescription(description);
            pointAModifier.setImage(imagePath);
            pointAModifier.setLatitude(20);
            pointAModifier.setLongitude(12);
            pointAModifier.setOwner_id(user.getId());
            pointAModifier.setType(type);

            boolean success = recyclingPointService.modifier(pointAModifier);

            if (success) {
                HelloApplication.succes("✅ Point de recyclage modifie avec succès !");
                HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
            } else {
                HelloApplication.error("❌ Échec de la modification du point.");
            }
        }
    }

    @FXML
    void selectImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(image.getScene().getWindow());

        if (selectedFile != null) {
            String imageName = selectedFile.getName();
            txtImage.setText(imageName); // On sauvegarde seulement le nom dans le champ

            // Destination : dossier local dans le projet
            File destination = new File("C:\\Users\\user\\Desktop\\ESPRIT\\javafx\\demo\\src\\main\\resources\\images", imageName);

            try {
                java.nio.file.Files.copy(selectedFile.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                System.out.println("✅ Image copiée avec succès vers " + destination.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la copie de l'image : " + e.getMessage());
                HelloApplication.error("Impossible de copier l'image.");
            }
        }
    }



}
