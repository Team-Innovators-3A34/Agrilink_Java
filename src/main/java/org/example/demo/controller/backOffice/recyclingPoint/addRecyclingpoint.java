package org.example.demo.controller.backOffice.recyclingPoint;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import netscape.javascript.JSObject;
import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.models.recyclingpoint;
import org.example.demo.services.recyclingpoint.recyclingpointService;
import org.example.demo.utils.sessionManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class addRecyclingpoint {
    @FXML
    private Button btnUpload;

    @FXML
    private Button btnrecyclingpointAjouter;

    @FXML
    private Text title;

    @FXML
    private WebView mapView;

    @FXML
    private TextField recyclingpointadressefield;

    @FXML
    private TextArea recyclingpointdescriptionField;

    @FXML
    private TextField recyclingpointimageField;

    @FXML
    private TextField recyclingpointlatitudeField;

    @FXML
    private TextField recyclingpointlongitudeField;

    @FXML
    private TextField recyclingpointnomfield;

    @FXML
    private ComboBox<String> recyclingpointypeField;

    private recyclingpoint recyclingpointAModifier = null;

    private final recyclingpointService recyclingpointservice = new recyclingpointService();
    private User user;


    public void setRecyclingPointPourModification(recyclingpoint r) {
        this.recyclingpointAModifier = r;

        recyclingpointnomfield.setText(r.getNom());
        recyclingpointadressefield.setText(r.getAdresse());
        recyclingpointlongitudeField.setText(String.valueOf(r.getLongitude()));
        recyclingpointlatitudeField.setText(String.valueOf(r.getLatitude()));
        recyclingpointypeField.setValue(r.getType());
        recyclingpointimageField.setText(r.getImage());
        recyclingpointdescriptionField.setText(r.getDescription());
        title.setText("Modifier votre Point de Recyclage");
    }

    @FXML
    public void initialize() {
        user = sessionManager.getInstance().getUser();
        WebEngine webEngine = mapView.getEngine();
        addRecyclingpoint.JavaConnector connector = new addRecyclingpoint.JavaConnector() {
            @Override
            public void setCoordinates(double lat, double lng) {
                Platform.runLater(() -> {
                    recyclingpointlatitudeField.setText(String.valueOf(lat));
                    recyclingpointlongitudeField.setText(String.valueOf(lng));
                });
            }
        };

        recyclingpointypeField.getItems().addAll(
                "Plastique",
                "Verre",
                "Papier",
                "Métal",
                "Déchets électroniques"
        );

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", connector);
            }
        });

        // Load your map.html
        webEngine.load(getClass().getResource("/org/example/demo/fxml/Backoffice/recyclingPoint/map.html").toExternalForm());
    }

    public void ajouterRecyclingpoint() {
        String nom = recyclingpointnomfield.getText();
        String adresse = recyclingpointadressefield.getText();
        double longitude = Double.parseDouble(recyclingpointlongitudeField.getText());
        double latitude = Double.parseDouble(recyclingpointlatitudeField.getText());
        String type = (String) recyclingpointypeField.getValue();
        String image = recyclingpointimageField.getText();
        String description = recyclingpointdescriptionField.getText();
        if (recyclingpointAModifier == null) {
            try {

                if (nom.isEmpty() || adresse.isEmpty() || type == null) {
                    HelloApplication.error("⚠️ Veuillez remplir tous les champs obligatoires !");
                    return;
                }

                recyclingpoint recyclingpoint = new recyclingpoint(nom, adresse, longitude, latitude, type, image, description);
                recyclingpoint.setOwner_id(user.getId());
                if(recyclingpointservice.ajouter(recyclingpoint)) {
                    HelloApplication.succes("✅ recyclingpoint ajouté avec succès !");
                    // tu peux rediriger ici
                    clearForm();
                    HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/listRecyclingPoint.fxml");
                }else {
                    HelloApplication.error("❌ Erreur lors de l'ajout du recyclingpoint : ");
                }

            } catch (Exception e) {
                HelloApplication.error("❌ Erreur lors de l'ajout du recyclingpoint : " + e.getMessage());
            }
        }else {
            recyclingpointAModifier.setNom(nom);
            recyclingpointAModifier.setAdresse(adresse);
            recyclingpointAModifier.setLongitude(longitude);
            recyclingpointAModifier.setLatitude(latitude);
            recyclingpointAModifier.setType(type);
            recyclingpointAModifier.setImage(image);
            recyclingpointAModifier.setDescription(description);

            if(recyclingpointservice.modifier(recyclingpointAModifier)) {
                HelloApplication.succes("✅ recyclingpoint modifié avec succès !");
                // tu peux rediriger ici
                clearForm();
                HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/listRecyclingPoint.fxml");
            }else {
                HelloApplication.error("❌ Erreur lors de la modification du recyclingpoint : ");
            }
        }
    }

    private void clearForm() {
        recyclingpointnomfield.clear();
        recyclingpointadressefield.clear();
        recyclingpointlongitudeField.clear();
        recyclingpointlatitudeField.clear();
        recyclingpointypeField.setValue(null);
        recyclingpointimageField.clear();
        recyclingpointdescriptionField.clear();
    }

    public class JavaConnector {
        public void setCoordinates(double lat, double lng) {
            System.out.println("Lat: " + lat + ", Lng: " + lng);
            // You can update your JavaFX TextFields from here if needed, but it must run on the JavaFX thread
        }
    }

    @FXML
    void saveImg(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");

        // Filtrer uniquement les fichiers image
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(recyclingpointimageField.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Nom de fichier
                String fileName = selectedFile.getName();

                // Chemin de destination : dossier "images" dans src/main/resources
                File destDir = new File("src/main/resources/images");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                // Copie du fichier dans le dossier images
                File destFile = new File(destDir, fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Mise à jour du champ image avec juste le nom de l'image
                recyclingpointimageField.setText(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                HelloApplication.error("Erreur lors de la sauvegarde de l'image.");
            }
        }
    }
}
