package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.Objects;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charge le fichier FXML qui définit un BorderPane
            BorderPane root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/Dashboard.fxml")));
            // Crée la scène avec le root chargé
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setWidth(1550);
            primaryStage.setHeight(850);
            primaryStage.setTitle("Gestion des Réclamations");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
