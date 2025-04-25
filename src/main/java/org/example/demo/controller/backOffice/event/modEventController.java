package org.example.demo.controller.backOffice.event;

import org.example.demo.utils.EventValidator;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import netscape.javascript.JSObject;
import org.example.demo.HelloApplication;
import org.example.demo.models.categorie;
import org.example.demo.models.event;
import org.example.demo.services.event.catService;
import org.example.demo.services.event.eventService;

import java.io.File;
import java.util.List;

public class modEventController {

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
    @FXML private Button btnModifier;
    @FXML private Button btnUploadMod;
    @FXML private WebView mapView;

    private final eventService eventService = new eventService();
    private final catService catService = new catService();
    private event selectedEvent;

    @FXML
    private void initialize() {
        selectedEvent = ListEventController.getSelectedEvent();

        if (selectedEvent == null) {
            System.out.println("❌ Aucun événement sélectionné pour modification.");
            return;
        }

        List<categorie> categories = catService.rechercher();
        eventcategorieField.getItems().setAll(categories);

        eventnomField.setText(selectedEvent.getNom());
        eventadresseField.setText(selectedEvent.getAdresse());
        eventlongitudeField.setText(String.valueOf(selectedEvent.getLongitude()));
        eventlatitudeField.setText(String.valueOf(selectedEvent.getLatitude()));
        eventdateField.setValue(java.time.LocalDate.parse(selectedEvent.getDate().split(" ")[0]));
        eventypeField.setValue(selectedEvent.getType());
        eventplaceField.setText(String.valueOf(selectedEvent.getNbrPlaces()));
        eventimageField.setText(selectedEvent.getImage());
        eventdescriptionField.setText(selectedEvent.getDescription());
        eventcategorieField.setValue(selectedEvent.getCategorie());

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

        webEngine.load(getClass().getResource("/org/example/demo/fxml/Backoffice/event/map.html").toExternalForm());

        btnModifier.setText("Modifier");
        btnModifier.setOnAction(e -> {
            try {
                String nom = eventnomField.getText();
                String adresse = eventadresseField.getText();
                String longitudeStr = eventlongitudeField.getText();
                String latitudeStr = eventlatitudeField.getText();
                java.time.LocalDate date = eventdateField.getValue();
                String type = eventypeField.getValue();
                String placesStr = eventplaceField.getText();
                String image = eventimageField.getText();
                String description = eventdescriptionField.getText();
                categorie cat = eventcategorieField.getValue();

                String erreur = EventValidator.valider(nom, adresse, longitudeStr, latitudeStr, date, type, placesStr, image, description, cat);
                if (erreur != null) {
                    EventValidator.showError(erreur);
                    return;
                }

// Mise à jour de l'objet
                selectedEvent.setNom(nom);
                selectedEvent.setAdresse(adresse);
                selectedEvent.setLongitude(Double.parseDouble(longitudeStr));
                selectedEvent.setLatitude(Double.parseDouble(latitudeStr));
                selectedEvent.setDate(date.toString());
                selectedEvent.setType(type);
                selectedEvent.setNbrPlaces(Integer.parseInt(placesStr));
                selectedEvent.setImage(image);
                selectedEvent.setDescription(description);
                selectedEvent.setCategorie(cat);



                eventService.modifier(selectedEvent);
                HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/event/listEvent.fxml");

            } catch (Exception ex) {
                System.out.println("❌ Erreur de modification : " + ex.getMessage());
            }
        });

        btnUploadMod.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png"));
            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                eventimageField.setText(file.getAbsolutePath());
            }
        });
    }

    public interface JavaConnector {
        void setCoordinates(double lat, double lng);
    }
}
