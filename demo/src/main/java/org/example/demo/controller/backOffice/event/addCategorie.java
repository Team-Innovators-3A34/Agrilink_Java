package org.example.demo.controller.backOffice.event;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.example.demo.HelloApplication;
import org.example.demo.services.event.catService;
import org.example.demo.models.categorie;

public class addCategorie {

        @FXML
        private TextField catName;

        @FXML
        private Button btnAjouter;

        private catService service = new catService();

        @FXML
        private void initialize() {
            btnAjouter.setOnAction(e -> {
                String nom = catName.getText();
                if (nom != null && !nom.trim().isEmpty()) {
                    categorie cat = new categorie(nom);
                    service.ajouter(cat);
                    HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/event/listCategorie.fxml");
                } else {
                    System.out.println("Champ vide !");
                }
            });
        }




}
