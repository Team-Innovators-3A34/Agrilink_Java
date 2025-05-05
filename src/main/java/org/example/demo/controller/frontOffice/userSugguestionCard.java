package org.example.demo.controller.frontOffice;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.profile.userProfileController;
import org.example.demo.models.User;

import java.io.IOException;

public class userSugguestionCard {

    @FXML
    private Text email;

    @FXML
    private ImageView imageprofile;

    @FXML
    private Text nometprenom;

    private User user;

    public void setUserData(User user) {
        this.user = user; // <--- Store the user
        nometprenom.setText(user.getNom() + " " + user.getPrenom());
        email.setText(user.getEmail());
        String imageFileName = user.getImage();
        if (imageFileName != null && !imageFileName.isEmpty()) {
            String imagePath = "/images/" + imageFileName;
            try {
                Image image = new Image(getClass().getResource(imagePath).toString());
                imageprofile.setImage(image);
            } catch (NullPointerException e) {
                System.out.println("Image profile not found");
            }
        } else {
            System.out.println("Image profile not found");
        }
    }


    @FXML
    void seeProfile() {
        try {
            HelloApplication.changeSceneWithController(
                    "/org/example/demo/fxml/frontOffice/profile/userProfile.fxml",
                    controller -> {
                        if (controller instanceof userProfileController) {
                            ((userProfileController) controller).setUser(user);
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
