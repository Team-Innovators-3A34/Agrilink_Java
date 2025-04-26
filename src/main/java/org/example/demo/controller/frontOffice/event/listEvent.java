package org.example.demo.controller.frontOffice.event;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.example.demo.models.event;
import org.example.demo.services.event.eventService;

import javax.script.CompiledScript;
import java.io.IOException;
import java.util.List;

public class listEvent {

    @FXML
    private GridPane eventList;

    @FXML
    private WebView mapView;

    private eventService eventService = new eventService();

    @FXML
    public void initialize() {
        List<event> events = eventService.rechercher();

        int column = 0;
        int row = 0;

        WebEngine webEngine = mapView.getEngine();
        webEngine.load(getClass().getResource("/org/example/demo/fxml/Backoffice/event/map.html").toExternalForm());



        for (int i = 0; i < events.size(); i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/event/testcarsd.fxml"));
                AnchorPane card = loader.load();

                CardController controller = loader.getController();
                controller.setEvent(events.get(i));

                // Taille des cartes
                card.setPrefWidth(230);
                card.setPrefHeight(290);

                // Ajout à la grille
                eventList.add(card, column, row);
                GridPane.setMargin(card, new Insets(10)); // marge entre cartes

                column++;
                if (column == 3) { // 3 colonnes max
                    column = 0;
                    row++;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
