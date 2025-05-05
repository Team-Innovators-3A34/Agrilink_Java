package org.example.demo.controller.frontOffice.event;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.example.demo.models.event;
import org.example.demo.services.event.eventService;

import java.io.IOException;
import java.util.List;

public class listEvent {

    @FXML
    private GridPane eventList;

    @FXML
    private WebView mapView;

    private final eventService eventService = new eventService();

    @FXML
    public void initialize() {
        List<event> events = eventService.rechercher();

        int column = 0;
        int row = 0;

        WebEngine webEngine = mapView.getEngine();
        webEngine.load(getClass().getResource("/org/example/demo/fxml/Frontoffice/event/mapFront.html").toExternalForm());

        // S'assurer que le JS est prêt avant d'exécuter les scripts
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                System.out.println("✅ HTML chargé, injection JS...");
                for (event ev : events) {
                    double lat = ev.getLatitude();
                    double lng = ev.getLongitude();

                    if (lat != 0 && lng != 0) {
                        String nom = escapeForJS(ev.getNom());
                        String adresse = escapeForJS(ev.getAdresse());
                        String js = String.format("addEventMarker(%f, %f, '%s', '%s');", lat, lng, nom, adresse);
                        System.out.println("➡️ Envoi JS : " + js);
                        webEngine.executeScript(js);
                    }
                }
            }
        });


        // Afficher les cartes des événements dans la grille
        for (event ev : events) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/event/testcarsd.fxml"));
                AnchorPane card = loader.load();

                CardController controller = loader.getController();
                controller.setEvent(ev);

                card.setPrefWidth(230);
                card.setPrefHeight(290);

                eventList.add(card, column, row);
                GridPane.setMargin(card, new Insets(10));

                column++;
                if (column == 3) {
                    column = 0;
                    row++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String escapeForJS(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "")
                .replace("\r", "");
    }
}
