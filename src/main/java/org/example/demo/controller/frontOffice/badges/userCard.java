package org.example.demo.controller.frontOffice.badges;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    public void setUser(User user) {
        this.user = user;
        username.setText(user.getNom());
        userEmail.setText(user.getEmail());
        // Later you can set profile image, rating stars here too
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
