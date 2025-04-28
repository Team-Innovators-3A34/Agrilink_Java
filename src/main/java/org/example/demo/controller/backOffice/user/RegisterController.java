package org.example.demo.controller.backOffice.user;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import org.example.demo.services.user.GeoLocationService;
import org.mindrot.jbcrypt.BCrypt;
import javafx.scene.control.TextField;
import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.services.user.userService;

import java.net.URL;
import java.util.ResourceBundle;

public class
RegisterController implements Initializable {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private TextField telephoneField;
    @FXML private TextField adressField;
    @FXML private TextField passwordField;
    @FXML private TextField comfirmpasswordField;
    @FXML private ChoiceBox<String> role;
    userService userService = new userService();


    private final String[] roles = {"Agricultural", "Resource Investor","Recycling Investor"};

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        role.getItems().addAll(roles);
    }

    @FXML
    public void OnLoginClick() {
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
    }

    @FXML
    public void OnRegisterClick() {

        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email = emailField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String adresse = adressField.getText().trim();
        String password = passwordField.getText().trim();
        String comfirmpassword = comfirmpasswordField.getText().trim();
        String selectedRole = role.getValue();
        String roleValue;
        String image;
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(13));


        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || telephone.isEmpty() ||
                adresse.isEmpty() || password.isEmpty() || comfirmpassword.isEmpty() || selectedRole == null) {
            HelloApplication.error("Veuillez remplir tous les champs !");
            return;
        }

        if (!nom.matches("[a-zA-Z]+")) {
            HelloApplication.error("Le nom ne doit contenir que des lettres !");
            return;
        }

        if (!prenom.matches("[a-zA-Z]+")) {
            HelloApplication.error("Le prénom ne doit contenir que des lettres !");
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            HelloApplication.error("Veuillez entrer un email valide !");
            return;
        }

        if (!telephone.matches("\\d{8}")) {
            HelloApplication.error("Le numéro de téléphone doit contenir exactement 8 chiffres !");
            return;
        }

        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            HelloApplication.error("Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre !");
            return;
        }

        if (!password.equals(comfirmpassword)) {
            HelloApplication.error("Les mots de passe ne correspondent pas !");
            return;
        }

        if (userService.checkUniqueEmail(email)){
            HelloApplication.error("Email deja utilise");
            return;
        }

        switch (selectedRole) {
            case "Agricultural":
                roleValue = "[\"ROLE_AGRICULTURE\",\"ROLE_USER\"]";
                image="agriculteur.png";
                break;
            case "Resource Investor":
                roleValue = "[\"ROLE_RESOURCE_INVESTOR\",\"ROLE_USER\"]";
                image="ressource.png";
                break;
            case "Recycling Investor":
                roleValue = "[\"ROLE_RECYCLING_INVESTOR\",\"ROLE_USER\"]";
                image="pointRecyclage.png";
                break;
            default:
                HelloApplication.error("Rôle invalide !");
                return;
        }

        GeoLocationService geoLocationService = new GeoLocationService();
        String[] geoCoordinates = geoLocationService.getLatitudeLongitude();
        String latitude = geoCoordinates[0];
        String longitude = geoCoordinates[1];
        String city = geoCoordinates[2];
        String country = geoCoordinates[3];
        User user = new User(email, adresse, nom, prenom, telephone, roleValue, hashedPassword);
        user.setImage(image);
        user.setLatitude(Float.parseFloat(latitude));
        user.setLongitude(Float.parseFloat(longitude));
        user.setCity(city);
        user.setCountry(country);
        if (userService.Register(user)) {
            HelloApplication.succes("Votre compte a ete cree! merci de le verifier avec votre mail!");
            HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
        }
    }


}
