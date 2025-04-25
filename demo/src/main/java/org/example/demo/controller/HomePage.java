package org.example.demo.controller;

import org.example.demo.HelloApplication;
import org.example.demo.utils.sessionManager;

public class HomePage {
    public void onLogoutButtonClick() {
        sessionManager.getInstance().clearSession();
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
    }
}
