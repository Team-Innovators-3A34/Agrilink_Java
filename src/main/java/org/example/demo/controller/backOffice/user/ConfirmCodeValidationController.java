package org.example.demo.controller.backOffice.user;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.example.demo.HelloApplication;
import org.example.demo.services.user.userService;
import org.example.demo.utils.sessionManager;

public class ConfirmCodeValidationController {

    @FXML
    private TextField otpfield1, otpfield2, otpfield3, otpfield4;

    userService userService = new userService();

    public void onValidateAccount() {

        String otpCode = otpfield1.getText() + otpfield2.getText() + otpfield3.getText() + otpfield4.getText();

        if (userService.ConfirmCodeValidationAccount( sessionManager.getTempemail(),otpCode)){
            HelloApplication.succes("Compte verifier vous pouvez se connecter!");
            sessionManager.setTempemail(null);
            HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
        }else {
            HelloApplication.error("Erreur de verification!verifier le code ou le code a deja expirer");
            return;
        }
    }

    public void toLogin(){
        sessionManager.setTempemail(null);
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
    }

}
