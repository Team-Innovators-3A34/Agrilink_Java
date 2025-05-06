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

    @FXML
    public void OnProfileClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
    }

    @FXML
    public void onFeaturesClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/features/features.fxml");
    }

    @FXML
    public void onTikTokClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/claim/Tiktok.fxml");
    }

    @FXML
    public void onJobClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/claim/Job.fxml");
    }

    @FXML
    public void onNewClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/claim/news.fxml");
    }

    @FXML
    public void onHomeClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/home/home.fxml");
    }

    @FXML
    public void onAudioClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/audio/audio.fxml");
    }

    @FXML
    public void onVirtualRealityClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/recycling/Recycling.fxml");

    }
    @FXML
    public void onChatbot() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/Recyclingchat/Chat.fxml");

    }
}