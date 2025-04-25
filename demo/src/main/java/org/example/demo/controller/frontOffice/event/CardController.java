package org.example.demo.controller.frontOffice.event;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import org.example.demo.models.event; // Assuming your Event model

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
    }

}