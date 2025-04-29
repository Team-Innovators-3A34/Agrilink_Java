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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class addRecyclingpoint {
    @FXML private Button btnUpload;
    @FXML private Button btnrecyclingpointAjouter;
    @FXML private Text title;
    @FXML private WebView mapView;
    @FXML private TextField recyclingpointadressefield;
    @FXML private TextArea recyclingpointdescriptionField;
    @FXML private TextField recyclingpointimageField;
    @FXML private TextField recyclingpointlatitudeField;
    @FXML private TextField recyclingpointlongitudeField;
    @FXML private TextField recyclingpointnomfield;
    @FXML private ComboBox<String> recyclingpointypeField;

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

        // Java connector to receive clicks
        JavaConnector connector = new JavaConnector() {
            @Override
            public void setCoordinates(double lat, double lng) {
                System.out.println("[DEBUG] JS -> Java coords: lat=" + lat + ", lng=" + lng);
                Platform.runLater(() -> {
                    recyclingpointlatitudeField.setText(String.valueOf(lat));
                    recyclingpointlongitudeField.setText(String.valueOf(lng));
                });
            }
        };

        // Populate the combo box
        recyclingpointypeField.getItems().setAll(
                "Plastique", "Verre", "Papier", "Métal", "Déchets électroniques"
        );

        // Load the HTML map
        URL mapUrl = getClass().getResource(
                "/org/example/demo/fxml/Backoffice/recyclingPoint/map.html"
        );
        if (mapUrl == null) {
            throw new IllegalStateException("map.html not found in resources");
        }
        webEngine.load(mapUrl.toExternalForm());

        // Once loaded, bind the connector and attach click-handler
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                // Expose JavaConnector to JS
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", connector);

                // Simplified click listener: one-liner
                webEngine.executeScript(
                        "map.on('click', function(e) {" +
                                "window.javaConnector.setCoordinates(e.latlng.lat, e.latlng.lng);" +
                                "});"
                );

                // Optional: test call to Java upon injection
                webEngine.executeScript(
                        "console.log('Click handler injected');"  // will not show in Java console
                );
            }
        });
}

public class JavaConnector {
    public void setCoordinates(double lat, double lng) { /* overridden */ }
}
    public void ajouterRecyclingpoint() {
        String nom = recyclingpointnomfield.getText();
        String adresse = recyclingpointadressefield.getText();
        double longitude = Double.parseDouble(recyclingpointlongitudeField.getText());
        double latitude = Double.parseDouble(recyclingpointlatitudeField.getText());
        String type = recyclingpointypeField.getValue();
        String image = recyclingpointimageField.getText();
        String description = recyclingpointdescriptionField.getText();
        if (recyclingpointAModifier == null) {
            try {
                if (nom.isEmpty() || adresse.isEmpty() || type == null) {
                    HelloApplication.error("⚠️ Veuillez remplir tous les champs obligatoires !");
                    return;
                }
                recyclingpoint rp = new recyclingpoint(nom, adresse, longitude, latitude, type, image, description);
                rp.setOwner_id(user.getId());
                if (recyclingpointservice.ajouter(rp)) {
                    HelloApplication.succes("✅ recyclingpoint ajouté avec succès !");
                    clearForm();
                    HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/listRecyclingPoint.fxml");
                } else {
                    HelloApplication.error("❌ Erreur lors de l'ajout du recyclingpoint !");
                }
            } catch (Exception e) {
                HelloApplication.error("❌ Erreur lors de l'ajout du recyclingpoint : " + e.getMessage());
            }
        } else {
            recyclingpointAModifier.setNom(nom);
            recyclingpointAModifier.setAdresse(adresse);
            recyclingpointAModifier.setLongitude(longitude);
            recyclingpointAModifier.setLatitude(latitude);
            recyclingpointAModifier.setType(type);
            recyclingpointAModifier.setImage(image);
            recyclingpointAModifier.setDescription(description);
            if (recyclingpointservice.modifier(recyclingpointAModifier)) {
                HelloApplication.succes("✅ recyclingpoint modifié avec succès !");
                clearForm();
                HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/listRecyclingPoint.fxml");
            } else {
                HelloApplication.error("❌ Erreur lors de la modification du recyclingpoint !");
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

    @FXML
    void saveImg(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        File selectedFile = fileChooser.showOpenDialog(recyclingpointimageField.getScene().getWindow());
        if (selectedFile != null) {
            try {
                String fileName = selectedFile.getName();
                File destDir = new File("src/main/resources/images");
                if (!destDir.exists()) destDir.mkdirs();
                File destFile = new File(destDir, fileName);
                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                recyclingpointimageField.setText(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                HelloApplication.error("Erreur lors de la sauvegarde de l'image.");
            }
        }
    }
}