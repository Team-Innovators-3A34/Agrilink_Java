package org.example.demo.controller.backOffice.user;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.demo.HelloApplication;
import org.example.demo.services.user.userService;
import org.example.demo.utils.sessionManager;

public class ResetPasswordController {

    @FXML private TextField otpField,PasswordField;
    userService userService = new userService();


    public void ResetPassword(){
        String otp = otpField.getText();
        String password = PasswordField.getText();
        String email = sessionManager.getTempemail();

        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            HelloApplication.error("Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre !");
            return;
        }

        if(userService.ResetPassword(email,otp,password)){
            HelloApplication.succes("Mot de passe modifier avec succes!");
            sessionManager.setTempemail(null);
            HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
        }else {
            HelloApplication.error("Erreur de mise a jour du mot de passe!verifier le code ou le code a deja expirer");
            return;
        }


    }

    @FXML
    public void toLogin(){
        sessionManager.setTempemail(null);
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
    }
}
