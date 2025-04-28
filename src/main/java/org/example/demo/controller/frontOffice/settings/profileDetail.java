package org.example.demo.controller.frontOffice.settings;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import org.apache.hc.core5.http2.H2Error;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.topBarController;
import org.example.demo.models.User;
import org.example.demo.services.user.userService;
import org.example.demo.utils.sessionManager;
import javafx.scene.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;


public class profileDetail {
    @FXML
    private TextField adresseField;

    @FXML
    private TextField bioField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private TextField emailField;

    @FXML
    private RadioButton is2FAField;

    @FXML
    private TextField nomField;

    @FXML
    private TextField prenomField;

    @FXML
    private TextField telephoneField;

    @FXML
    private Text nometprenomField;

    @FXML
    private Button updateProfile;

    private User user ;

    userService userService = new userService();

    @FXML
    private ImageView profileimage;

    @FXML
    private topBarController topBarController;



    @FXML
    public void initialize() throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/topBar.fxml"));
        AnchorPane topBar = loader.load();
        topBarController = loader.getController();

        user = sessionManager.getInstance().getUser();
        nomField.setText(user.getNom());
        prenomField.setText(user.getPrenom());
        emailField.setText(user.getEmail());
        adresseField.setText(user.getAdresse());
        telephoneField.setText(user.getTelephone());
        bioField.setText(user.getBio());
        descriptionField.setText(user.getDescription());
        is2FAField.setSelected(user.getIs2FA());
        nometprenomField.setText(user.getNom() + " " + user.getPrenom());

        String imageFileName = user.getImage();
        if (imageFileName != null && !imageFileName.isEmpty()) {
            String imagePath = "/images/" + imageFileName;
            try {
                Image image = new Image(getClass().getResource(imagePath).toString());
                profileimage.setImage(image);
            } catch (NullPointerException e) {
                System.out.println("Image profile not found");;
            }
        } else {
            System.out.println("Image profile not found");;
        }
    }

    public void onUpdateProfileClicked() {
        String newNom = nomField.getText();
        String newPrenom = prenomField.getText();
        String newEmail = emailField.getText();
        String newAdresse = adresseField.getText();
        String newTelephone = telephoneField.getText();
        String newBio = bioField.getText();
        String newDescription = descriptionField.getText();
        boolean new2FA = is2FAField.isSelected();

        if (!newEmail.equals(user.getEmail())) {
            if (userService.checkUniqueEmail(newEmail)) {
                HelloApplication.error("Cet email est déjà utilisé !");
                return;
            }
        }

        user.setNom(newNom);
        user.setPrenom(newPrenom);
        user.setEmail(newEmail);
        user.setAdresse(newAdresse);
        user.setTelephone(newTelephone);
        user.setBio(newBio);
        user.setDescription(newDescription);
        user.setIs2FA(new2FA);

        userService.updateUser(user);
        HelloApplication.succes("Profile Modifie!");
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/settings.fxml");
    }

    public void backtosettings(){
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/settings.fxml");
    }

    @FXML
    void updateProfileImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                // 📁 Dossier destination dans resources
                File destDir = new File("src/main/resources/images");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }

                // 📂 Fichier de destination
                File destFile = new File(destDir, selectedFile.getName());

                // 🛠️ Copier l'image
                Files.copy(
                        selectedFile.toPath(),
                        destFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                // 🖼️ Charger l'image depuis le fichier copié
                Image image = new Image(destFile.toURI().toString());
                profileimage.setImage(image);

                // ✅ Mise à jour en base de données
                if (userService.updateProfileImage(user, selectedFile.getName())) {
                    user.setImage(selectedFile.getName());
                    HelloApplication.succes("Image du profil mise à jour !");
                    topBarController.refreshProfileImage();
                } else {
                    HelloApplication.error("Erreur lors de la mise à jour de l'image du profil.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                HelloApplication.error("Erreur lors de la sauvegarde de l'image : " + e.getMessage());
            }
        }
    }





}
