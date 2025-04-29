package org.example.demo.controller.frontOffice.badges;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import org.example.demo.models.User;
import org.example.demo.services.user.userService;

import java.io.IOException;
import java.util.List;

public class badgesController {

    @FXML
    private GridPane userList;

    private userService userService = new userService();

    public void initialize() {
        List<User> allUsers = userService.getAllUsers();
        displayUsers(allUsers);
    }

    private void displayUsers(List<User> users) {
        userList.getChildren().clear();
        userList.setHgap(20);
        userList.setVgap(20);
        userList.setStyle("-fx-padding: 20;");

        int column = 0;
        int row = 0;

        try {
            for (User user : users) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/badges/userCard.fxml"));
                AnchorPane userCardPane = loader.load();

                userCard controller = loader.getController();
                controller.setUser(user);

                userCardPane.setPrefWidth(250);
                userCardPane.setPrefHeight(180);

                userList.add(userCardPane, column++, row);

                if (column == 3) {
                    column = 0;
                    row++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
