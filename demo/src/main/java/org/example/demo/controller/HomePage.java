package org.example.demo.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import org.example.demo.HelloApplication;
import org.example.demo.services.ressource.MeteoService;
import org.example.demo.utils.sessionManager;

public class HomePage {
    @FXML
    private TextField ville;
    @FXML
    private Text meteo;

    MeteoService meteoservice;


    public void onLogoutButtonClick() {
        sessionManager.getInstance().clearSession();
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
    }


    public void showMeteo(){
        meteo.setText(meteoservice.getMeteo(ville.getText()));
    }

}
