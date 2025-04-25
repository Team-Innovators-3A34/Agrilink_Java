package org.example.demo.controller.backOffice;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.demo.models.User;
import org.example.demo.utils.sessionManager;

public class TopBarController {

    private User user;

    @FXML
    private ImageView profileimage;

    @FXML
    private TextField search;

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


}
