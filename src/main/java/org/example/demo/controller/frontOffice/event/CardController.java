package org.example.demo.controller.frontOffice.event;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.example.demo.models.event;
import org.example.demo.services.event.eventService;
import org.example.demo.utils.EmailSender;
import org.example.demo.utils.sessionManager;

import javax.mail.MessagingException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

public class CardController {

    @FXML private Text EventAdresseText;
    @FXML private Text EventDayText;
    @FXML private Text EventMonthText;
    @FXML private Text EventPlacesText;
    @FXML private Text eventNameText;
    @FXML private ImageView imgevent;
    @FXML private Button applyButton;
    @FXML private Button cancelButton;

    private final eventService service = new eventService();
    private final int currentUserId = sessionManager.getInstance().getUser().getId(); // à remplacer dynamiquement si besoin
    private event ev;

    public void setEvent(event ev) {
        this.ev = ev;

        eventNameText.setText(ev.getNom());
        EventAdresseText.setText(ev.getAdresse());

        // Formatage date
        LocalDate date = LocalDate.now();
        EventMonthText.setText(date.format(DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)));
        EventDayText.setText(date.format(DateTimeFormatter.ofPattern("dd")));

        EventPlacesText.setText(ev.getNbrPlaces() + " places available");

        // Image
        String imagePath = "/images/" + ev.getImage();
        InputStream is = getClass().getResourceAsStream(imagePath);
        if (is != null) {
            imgevent.setImage(new Image(is));
        }

        // Vérifier si déjà participé
        boolean dejaParticipe = service.aDejaParticipe(ev.getId(), currentUserId);

        applyButton.setDisable(dejaParticipe || ev.getNbrPlaces() <= 0);
        cancelButton.setDisable(!dejaParticipe);

        // Action bouton Apply
        applyButton.setOnAction(e -> {
            boolean success = service.participer(ev.getId(), currentUserId);
            if (success) {
                int updatedPlaces = ev.getNbrPlaces() - 1;
                ev.setNbrPlaces(updatedPlaces);
                EventPlacesText.setText(updatedPlaces + " places available");

                applyButton.setDisable(true);
                cancelButton.setDisable(false);
                if(Objects.equals(ev.getType(), "En ligne")) {
                    try {
                        EmailSender.send(sessionManager.getInstance().getUser().getEmail(), "Votre lien meet de l'evenement est :" + ev.getMeet());
                    } catch (MessagingException ex) {
                        throw new RuntimeException(ex);
                    }
                }

            }
        });

        // Action bouton Cancel
        cancelButton.setOnAction(e -> {
            boolean success = service.annulerParticipation(ev.getId(), currentUserId);
            if (success) {
                int updatedPlaces = ev.getNbrPlaces() + 1;
                ev.setNbrPlaces(updatedPlaces);
                EventPlacesText.setText(updatedPlaces + " places available");

                applyButton.setDisable(false);
                cancelButton.setDisable(true);
            }
        });
    }
}
