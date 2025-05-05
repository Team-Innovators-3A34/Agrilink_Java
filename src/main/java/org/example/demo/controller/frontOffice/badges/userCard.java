package org.example.demo.controller.frontOffice.badges;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.ressource.addRessources;
import org.example.demo.models.User;
import org.example.demo.controller.frontOffice.profile.userProfileController;

import java.io.IOException;

public class userCard {

    @FXML
    private ImageView profileimage;

    @FXML
    private AnchorPane ratingPane;

    @FXML
    private ImageView star1, star2, star3, star4, star5;

    @FXML
    private HBox starsBox;

    @FXML
    private Text userEmail;

    @FXML
    private Text username;

    @FXML
    private AnchorPane viewProfile;

    private User user;

    @FXML
    private ImageView badge1;

    @FXML
    private ImageView badge2;

    @FXML
    private ImageView badge3;

    @FXML
    private Text score;

    public void setUser(User user) {
        this.user = user;
        username.setText(user.getNom());
        userEmail.setText(user.getEmail());
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

        score.setText(String.valueOf(user.getScore()));

        badge1.setVisible(false);
        badge2.setVisible(false);
        badge3.setVisible(false);

        // Score logic for badges
        System.out.println(user.getScore());
        if (user.getScore() <= 100) {
            badge1.setVisible(true);
        } else if (user.getScore() <= 180) {
            badge1.setVisible(true);
            badge2.setVisible(true);
        } else {
            badge1.setVisible(true);
            badge2.setVisible(true);
            badge3.setVisible(true);
        }

    }

    @FXML
    void openProfile(MouseEvent event) {
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
