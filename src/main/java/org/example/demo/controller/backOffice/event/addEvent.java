package org.example.demo.controller.backOffice.event;

import org.example.demo.utils.EventValidator;

import org.example.demo.HelloApplication;
import org.example.demo.models.event;
import org.example.demo.models.categorie;
import org.example.demo.services.event.catService;
import org.example.demo.services.event.eventService;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.example.demo.models.categorie;
import org.example.demo.models.event;

import java.io.File;
import java.time.LocalDate;

public class addEvent {
    @FXML private ComboBox<categorie> eventcategorieField;
    @FXML private TextField eventnomField;
    @FXML private TextField eventadresseField;
    @FXML private TextField eventlongitudeField;
    @FXML private TextField eventlatitudeField;
    @FXML private DatePicker eventdateField;
    @FXML private ComboBox<String> eventypeField;
    @FXML private TextField eventplaceField;
    @FXML private TextField eventimageField;
    @FXML private TextArea eventdescriptionField;
    @FXML private Button btnAjouter;
    @FXML private Button btnUpload;
    @FXML private WebView mapView;



    private final eventService eventService = new eventService();
    private final catService catService = new catService();

    @FXML
    public void initialize() {

        eventypeField.getItems().addAll("Présentiel", "En ligne");
        eventcategorieField.getItems().addAll(catService.rechercher());

        btnUpload.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                try {
                    String fileName = file.getName();
                    File destDir = new File("src/main/resources/images");
                    if (!destDir.exists()) destDir.mkdirs();

                    File destFile = new File(destDir, fileName);

                    // Copier l'image vers le dossier resources/images
                    java.nio.file.Files.copy(file.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                    // On sauvegarde uniquement le nom du fichier
                    eventimageField.setText(fileName);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });



        WebEngine webEngine = mapView.getEngine();
        JavaConnector connector = new JavaConnector() {
            @Override
            public void setCoordinates(double lat, double lng) {
                Platform.runLater(() -> {
                    eventlatitudeField.setText(String.valueOf(lat));
                    eventlongitudeField.setText(String.valueOf(lng));
                });
            }
        };

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", connector);
            }
        });

        // Load your map.html
        webEngine.load(getClass().getResource("/org/example/demo/fxml/Backoffice/event/map.html").toExternalForm());

        btnAjouter.setOnAction(e -> ajouterEvent());

    }


    private void ajouterEvent() {
        try {
            String nom = eventnomField.getText().trim();
            String adresse = eventadresseField.getText().trim();
            String longitude = eventlongitudeField.getText().trim();
            String latitude = eventlatitudeField.getText().trim();
            LocalDate date = eventdateField.getValue();
            String type = eventypeField.getValue();
            String nbrPlaces = eventplaceField.getText().trim();
            String image = eventimageField.getText().trim();
            String description = eventdescriptionField.getText().trim();
            categorie selectedCategorie = eventcategorieField.getValue();

            // ✅ Validation des champs
            String erreur = EventValidator.valider(nom, adresse, longitude, latitude, date, type, nbrPlaces, image, description, selectedCategorie);
            if (erreur != null) {
                EventValidator.showError(erreur);
                return;
            }

            // ✅ Création et enregistrement de l’événement
            event event = new event(
                    nom,
                    adresse,
                    Double.parseDouble(longitude),
                    Double.parseDouble(latitude),
                    date.toString(),
                    type,
                    Integer.parseInt(nbrPlaces),
                    image,
                    description,
                    selectedCategorie
            );

            eventService.ajouter(event);
            System.out.println("✅ Événement ajouté avec succès !");
            HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/event/listEvent.fxml");

        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'ajout de l'événement : " + e.getMessage());
        }
    }



    private void clearForm() {
        eventnomField.clear();
        eventadresseField.clear();
        eventlongitudeField.clear();
        eventlatitudeField.clear();
        eventdateField.setValue(null);
        eventypeField.setValue(null);
        eventplaceField.clear();
        eventimageField.clear();
        eventdescriptionField.clear();
        eventcategorieField.setValue(null);
    }

    public class JavaConnector {
        public void setCoordinates(double lat, double lng) {
            System.out.println("Lat: " + lat + ", Lng: " + lng);
            // You can update your JavaFX TextFields from here if needed, but it must run on the JavaFX thread
        }
    }
}
