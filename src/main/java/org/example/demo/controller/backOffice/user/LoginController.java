package org.example.demo.controller.backOffice.user;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.services.ressource.DemandeService;
import org.example.demo.services.user.userService;
import org.example.demo.utils.GoogleAuth;
import org.example.demo.utils.sessionManager;

import java.io.*;
import java.util.Properties;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button loginGoogleButton;
    userService userService = new userService();
    @FXML
    private ImageView seeMdp;
    private boolean isPasswordVisible = false;
    @FXML private CheckBox rememberMeCheckBox;

    private final String credentialsPath = "remember_me.properties";

    @FXML
    public void initialize() {
        loadSavedCredentials();
    }

    @FXML
    public void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = isPasswordVisible ? visiblePasswordField.getText() : passwordField.getText();
        if (rememberMeCheckBox.isSelected()) {
            saveCredentials(username, password);
        } else {
            clearCredentials();
        }

        if(userService.check2FAUser(username)) {
            sessionManager.setTempemail(username);
            userService.sendCode2FA(username);
            HelloApplication.changeScene("/org/example/demo/fxml/Security/Confirm2FACode.fxml");
            return;
        }

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
                        alert.setContentText("⚠ Une de vos demandes expire aujourd'hui !");
                        alert.showAndWait();
                    }
                }
            }
        }
    }

    @FXML
    public void onLoginGoogleButtonClick() throws IOException, InterruptedException {
        GoogleAuth.authenticate(new GoogleAuth.AuthCallback() {
            @Override
            public void onSuccess(String email) {
                System.out.println("Connexion réussie, email : " + email);
                if(userService.check2FAUser(email)) {
                    sessionManager.setTempemail(email);
                    userService.sendCode2FA(email);
                    HelloApplication.changeScene("/org/example/demo/fxml/Security/Confirm2FACode.fxml");
                    return;
                }

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

    @FXML
    void onSeeMdp() {
        isPasswordVisible = !isPasswordVisible;

        if (isPasswordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            seeMdp.setImage(new Image(getClass().getResourceAsStream("/icons/hide.png")));
        } else {
            passwordField.setText(visiblePasswordField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            seeMdp.setImage(new Image(getClass().getResourceAsStream("/icons/view1.png")));

        }
    }

    private void saveCredentials(String username, String password) {
        try (FileOutputStream out = new FileOutputStream(credentialsPath)) {
            Properties props = new Properties();
            props.setProperty("username", username);
            props.setProperty("password", password);
            props.store(out, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadSavedCredentials() {
        File file = new File(credentialsPath);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(in);
                usernameField.setText(props.getProperty("username"));
                String savedPassword = props.getProperty("password");
                passwordField.setText(savedPassword);
                visiblePasswordField.setText(savedPassword);
                rememberMeCheckBox.setSelected(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void clearCredentials() {
        File file = new File(credentialsPath);
        if (file.exists()) {
            file.delete();
        }
    }

    @FXML
    void onLoginFaceIdClicked() {
        try {
            ProcessBuilder builder = new ProcessBuilder("python", "C:\\Users\\user\\Desktop\\test face\\detect_face_video.py");
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = null;
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("PYTHON: " + line);  // Debugging
                output = line;  // Save the last printed line
            }

            int exitCode = process.waitFor();
            if (exitCode == 0 && output != null) {
                if (output.startsWith("email:")) {
                    String[] parts = output.split(",");
                    String email = parts[0].split(":")[1];
                    String password = parts[1].split(":")[1];

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


                    // Call your login logic here
                    HelloApplication.succes("Logged in as: " + email);
                } else if (output.equals("Unknown")) {
                    HelloApplication.error("Unknown user. Please register.");
                } else {
                    HelloApplication.error("Unexpected output from face recognition: " + output);
                }
            } else {
                HelloApplication.error("Face recognition failed.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            HelloApplication.error("An error occurred while running face recognition.");
        }
    }





}