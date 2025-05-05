package org.example.demo.controller.backOffice.user;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.example.demo.HelloApplication;
import org.example.demo.services.user.userService;
import org.example.demo.utils.sessionManager;

public class Confirm2FACodeController {

    @FXML
    private TextField otpfield1;

    @FXML
    private TextField otpfield2;

    @FXML
    private TextField otpfield3;

    @FXML
    private TextField otpfield4;

    @FXML
    private Button validatefield;
    private sessionManager sessionManager;
    private userService userService=new userService();

    @FXML
    void onValidateAccount(ActionEvent event) {
        String otpCode = otpfield1.getText() + otpfield2.getText() + otpfield3.getText() + otpfield4.getText();
        if(userService.confirmCode2FA(sessionManager.getTempemail(), otpCode)){
            if (userService.loginGoogle(sessionManager.getTempemail())) {
                sessionManager.setTempemail(null);
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
        }else {
            HelloApplication.error("Code expired or wrong!");
        }

    }

    @FXML
    void toLogin(ActionEvent event) {
        sessionManager.setTempemail(null);
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
    }

}
