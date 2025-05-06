package org.example.demo.controller.frontOffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.profile.profileController;
import org.example.demo.models.User;
import org.example.demo.utils.sessionManager;

import java.io.IOException;

public class sideBarController {

    private User user;

    @FXML
    public void initialize() {
        user= sessionManager.getInstance().getUser();
        if (sessionManager.getInstance().isAdmin()) {
            Platform.runLater(() -> {
                HelloApplication.error("Access Denied: Insufficient permissions");
                sessionManager.getInstance().clearSession();
                HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
            });
            return;
        }
    }

    public void onParamtereClicked(){
        if (!sessionManager.getInstance().isAdmin() && sessionManager.getInstance().isSessionActive()) {
            HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/settings/settings.fxml");
        }
    }
    @FXML
    public void onVirtualRealityClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/recycling/Recycling.fxml");

    }
    public void onProfileClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
    }

    public void onEventClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/event/listEvent.fxml");
    }

    public void onBadgeCllicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/badges/badges.fxml");
    }


    @FXML
    private void onChatClicked(MouseEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/frontOffice/chatPage.fxml"));
            Parent root = loader.load();

            Scene currentScene = ((Node) event.getSource()).getScene();
            Stage stage = (Stage) currentScene.getWindow();
            stage.getScene().setRoot(root);

        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    @FXML
    void onHomeClicked(MouseEvent event) {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/HomePage.fxml");

    }

    @FXML
    void onPointClicked(MouseEvent event) {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/pointRecyclage/addRecyclingPoint.fxml");

    }







}
