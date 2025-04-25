package org.example.demo.utils;

import javafx.scene.control.Alert;
import org.example.demo.HelloApplication;
import org.example.demo.models.User;

public class AccessControl {

    public static boolean requireRole(String requiredRole) {
        User user = sessionManager.getInstance().getUser();
        System.out.println(user.getRoles());
        return user != null && requiredRole.equals(user.getRoles());
    }

    public static void redirectIfUnauthorized(String requiredRole, Runnable ifAuthorized, Runnable ifDenied) {
        if (requireRole(requiredRole)) {
            ifAuthorized.run();
        } else {
            ifDenied.run();
        }
    }

    public static void showAccessDeniedAlert() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Accès refusé");
        alert.setHeaderText("Vous n'avez pas la permission d'accéder à cette page.");
        alert.setContentText("Contactez un administrateur si vous pensez qu'il s'agit d'une erreur.");
        alert.showAndWait();
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");

    }
}
