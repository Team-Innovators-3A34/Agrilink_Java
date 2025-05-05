package org.example.demo.controller.frontOffice.settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.services.user.userService;
import org.example.demo.utils.sessionManager;

public class updatePassword {

    @FXML
    private ImageView backtosettings;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField oldPasswordField;

    userService userService = new userService();

    @FXML
    void backtosettings(MouseEvent event) {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/settings.fxml");
    }

    @FXML
    void onUpdatePasswordClicked() {
        User currentUser = sessionManager.getInstance().getUser();
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            HelloApplication.error("Please fill in all password fields.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            HelloApplication.error("New password and confirm password do not match.");
            return;
        }

        if ( ! userService.verifyPassword(currentUser, oldPassword)) {
            HelloApplication.error("The old password you entered is incorrect.");
            return;
        }

        boolean updateSuccessful = userService.updatePassword(currentUser, newPassword);

        if (updateSuccessful) {
            HelloApplication.succes( "Your password has been updated successfully.");
            HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/settings.fxml");

        } else {
            HelloApplication.error("An error occurred while updating your password. Please try again later.");
        }

    }

}
