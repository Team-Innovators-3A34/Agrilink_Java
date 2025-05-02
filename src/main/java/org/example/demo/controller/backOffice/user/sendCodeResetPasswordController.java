package org.example.demo.controller.backOffice.user;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.demo.HelloApplication;
import org.example.demo.services.user.userService;
import org.example.demo.utils.sessionManager;

public class sendCodeResetPasswordController {
    @FXML
    private TextField emailField;

    userService userService = new userService();


    @FXML
    public void SendCodeValidation(){
        String email = emailField.getText();

        if (!userService.checkUniqueEmail(email)) {
            HelloApplication.error("Email Untrouvable! Veuillez entrez une email valide ");
            return;
        }

        if (userService.checkBannedUser(email)) {
            HelloApplication.error("Compte Banned par l'admistrateur!");
            return;
        }

        if (!userService.checkVerifyUser(email)) {
            HelloApplication.error("Compte non verifie ,Vous devez verifier votre compte avant!");
            return;
        }

        userService.SendCodeResetPassword(email);
        HelloApplication.succes("Code envoye ! verifier le mail");
        sessionManager.setTempemail(email);
        HelloApplication.changeScene("/org/example/demo/fxml/Security/ResetPassword.fxml");


    }

    @FXML
    public void toLogin(){
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
    }


}
