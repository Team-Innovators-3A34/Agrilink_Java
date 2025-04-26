package org.example.demo.controller.frontOffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.profile.profileController;
import org.example.demo.models.User;
import org.example.demo.utils.sessionManager;

import java.io.IOException;

public class sideBarController {

    private User user;

    @FXML
    public void initialize() {
        user= sessionManager.getInstance().getUser();
        if (sessionManager.getInstance().isAdmin()) {
            Platform.runLater(() -> {
                HelloApplication.error("Access Denied: Insufficient permissions");
                sessionManager.getInstance().clearSession();
                HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
            });
            return;
        }
    }

    public void onParamtereClicked(){
        if (!sessionManager.getInstance().isAdmin() && sessionManager.getInstance().isSessionActive()) {
            HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/settings.fxml");
        }
    }

    public void onProfileClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
    }

    public void onEventClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/event/listEvent.fxml");
    }






}
