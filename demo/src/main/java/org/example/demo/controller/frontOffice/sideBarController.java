package org.example.demo.controller.frontOffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import org.example.demo.HelloApplication;
import org.example.demo.utils.sessionManager;

public class sideBarController {

    @FXML
    public void initialize() {
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

    public void onProfileClicked(){
        if (!sessionManager.getInstance().isAdmin() && sessionManager.getInstance().isSessionActive()) {
            HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
        }
    }

}
