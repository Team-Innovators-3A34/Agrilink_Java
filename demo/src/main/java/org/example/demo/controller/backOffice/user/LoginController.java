package org.example.demo.controller.backOffice.user;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.demo.HelloApplication;
import org.example.demo.services.user.userService;
import org.example.demo.utils.GoogleAuth;
import org.example.demo.utils.sessionManager;

import java.io.IOException;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginGoogleButton;
    userService userService = new userService();


    @FXML
    public void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            HelloApplication.error("Veuillez remplir tous les champs.");
            return;
        }

        if (userService.login(username, password)) {
            if (sessionManager.getInstance().getUser().getAccountVerification().equals("pending")){
                sessionManager.getInstance().clearSession();
                HelloApplication.error("\"Votre compte n'est pas encore vérifié.\"");
                return;
            }

            if (sessionManager.getInstance().getUser().getStatus().equals("hide")){
                sessionManager.getInstance().clearSession();
                HelloApplication.error("\"Votre compte est bloqué.\"");
                return;
            }

            if (sessionManager.getInstance().getUser().getRoles().equals( "[\"ROLE_ADMIN\"]")) {
                HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/dashboard.fxml");
            }else {
                HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/HomePage.fxml");
            }
        }
    }

    @FXML
    public void onLoginGoogleButtonClick() throws IOException, InterruptedException {
        GoogleAuth.authenticate(new GoogleAuth.AuthCallback() {
            @Override
            public void onSuccess(String email) {
                System.out.println("Connexion réussie, email : " + email);

                if (userService.loginGoogle(email)) {
                    if (sessionManager.getInstance().getUser().getAccountVerification().equals("pending")) {
                        sessionManager.getInstance().clearSession();
                        HelloApplication.error("Votre compte n'est pas encore vérifié.");
                        return;
                    }

                    if (sessionManager.getInstance().getUser().getStatus().equals("hide")) {
                        sessionManager.getInstance().clearSession();
                        HelloApplication.error("Votre compte est bloqué.");
                        return;
                    }

                    if (sessionManager.getInstance().getUser().getRoles().equals("[\"ROLE_ADMIN\"]")) {
                        HelloApplication.changeScene("/org/example/demo/fxml/Dashboard.fxml");
                    } else {
                        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/HomePage.fxml");
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                HelloApplication.error("Échec de l'authentification : " + error);
            }
        });
    }

    @FXML
    public void onVerifButtonClick(){}

    @FXML
    public void OnRegisterClick() {
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Register.fxml");
    }

    @FXML
    public void onVerifyClick(){
        HelloApplication.changeScene("/org/example/demo/fxml/Security/sendCodeValidation.fxml");
    }


    public void onForgetPassword(){
        HelloApplication.changeScene("/org/example/demo/fxml/Security/sendCodeResetPassword.fxml");
    }

}