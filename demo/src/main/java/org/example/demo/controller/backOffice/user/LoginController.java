package org.example.demo.controller.backOffice.user;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.example.demo.HelloApplication;
import org.example.demo.models.Demandes;
import org.example.demo.models.User;
import org.example.demo.services.DemandeService;
import org.example.demo.services.user.userService;
import org.example.demo.utils.GoogleAuth;
import org.example.demo.utils.sessionManager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

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
               //int userId = authenticateUser(username, password);
                User user = userService.authenticate(username, password);
                DemandeService demandesService = new DemandeService();
                if (user != null) {
                    int userId = user.getId(); // Récupération de l'ID utilisateur

                    boolean expireAujourdHui = demandesService.hasDemandeQuiExpireAujourdhui(user);
                    if (expireAujourdHui) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Alerte Expiration");
                        alert.setContentText("⚠️ Une de vos demandes expire aujourd'hui !");
                        alert.showAndWait();
                    }

                    // Suite : redirection vers la page principale...

                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setContentText("Nom d'utilisateur ou mot de passe incorrect !");
                    alert.showAndWait();
                }
            }
        }
    }

    private void verifierExpirationDemande(int userId, List<Demandes> demandes) {
        LocalDate dateAujourdHui = LocalDate.now();

        for (Demandes demande : demandes) {
            if (demande.getUserId()==userId && demande.getExpireDate().isEqual(dateAujourdHui)) {
                afficherNotification();
                break;
            }
        }

    }

    // Méthode pour afficher une notification
    public void afficherNotification() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notification");
        alert.setHeaderText("Expiration de votre demande");
        alert.setContentText("Votre demande expire aujourd'hui.");
        alert.showAndWait();
    }
    /*public List<Demandes> getDemandesFromDatabase(int userId) {
        // Implémentation pour récupérer les demandes de l'utilisateur depuis la base de données
        // Cette méthode doit interroger la base de données pour obtenir les demandes de l'utilisateur
        return DemandesService.getDemandesByUserId1(userId);
    }*/
    private int authenticateUser(String username, String password) {
        // Implémentation de l'authentification (à remplacer par votre propre logique)
        // Retourne l'ID de l'utilisateur si authentifié
        return 5; // ID fictif de l'utilisateur pour l'exemple
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