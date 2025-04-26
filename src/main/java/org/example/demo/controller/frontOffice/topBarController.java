package org.example.demo.controller.frontOffice;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.utils.sessionManager;

import java.io.File;

public class topBarController {

    private User user;

    @FXML
    private ImageView profileimage;

    @FXML
    public void initialize() {
        user = sessionManager.getInstance().getUser();
        refreshProfileImage();
    }

    public void refreshProfileImage() {
        String imageFileName = user.getImage();
        if (imageFileName != null && !imageFileName.isEmpty()) {
            String imagePath = "/images/" + imageFileName;
            try {
                Image image = new Image(getClass().getResource(imagePath).toString());
                profileimage.setImage(image);
            } catch (NullPointerException e) {
                System.out.println("Image profile not found");
            }
        } else {
            System.out.println("Image profile not found");
        }
    }
    public void OnProfileClicked(){
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
    }
    public void onFeaturesClicked(){
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/features/features.fxml");
    }
}
