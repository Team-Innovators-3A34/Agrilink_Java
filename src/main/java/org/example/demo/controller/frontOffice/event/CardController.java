package org.example.demo.controller.frontOffice.event;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.example.demo.models.event; // Assuming your Event model

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CardController {

    @FXML
    private Text EventAdresseText;

    @FXML
    private Text EventDayText;

    @FXML
    private Text EventMonthText;

    @FXML
    private Text EventPlacesText;

    @FXML
    private Text eventNameText;

    @FXML
    private ImageView imgevent  ;

    public void setEvent(event ev) {
        eventNameText.setText(ev.getNom());
        EventAdresseText.setText(ev.getAdresse());

        // Format date
        LocalDate date = LocalDate.now(); // Assuming it's a LocalDate
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH);
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("dd");

        EventMonthText.setText(date.format(monthFormatter));
        EventDayText.setText(date.format(dayFormatter));

        EventPlacesText.setText(ev.getNbrPlaces() + " places available");

        // Construire le chemin correct pour l'image
        String imageName = ev.getImage(); // Exemple : "logo.png"
        System.out.println("Nom d'image reçu depuis l'objet event : " + imageName);

        // Chemin relatif correct sans slash avant "images"
        String imagePath = "/images/" + imageName;
        System.out.println("Image Path: " + imagePath);  // Vérifier le chemin généré

        // Charger l'image avec un InputStream
        InputStream is = getClass().getResourceAsStream(imagePath);
        if (is != null) {
            Image image = new Image(is);
            imgevent.setImage(image);
        } else {
            System.out.println("Image not found: " + imagePath);
        }
    }


}