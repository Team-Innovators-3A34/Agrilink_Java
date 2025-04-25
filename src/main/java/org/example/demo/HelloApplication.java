package org.example.demo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;
import org.example.demo.services.user.userService;

import java.io.IOException;
import java.util.function.Consumer;

public class HelloApplication extends Application {

    private static Stage primaryStage;
    userService userService = new userService();


    @Override
    public void start(Stage stage)  {
        primaryStage = stage;
        primaryStage.setTitle("Connexion");
        //userService.login("mehdiboughdiri75@gmail.com", "Mehdiboughdiri75");
        //userService.login("monsef.boughdiri@esprit.tn", "Mehdiboughdiri75");//point investor
        //userService.login("jemaimedmalek@gmail.com", "Mehdiboughdiri75");//admin
        userService.login("aaa@esprit.tn", "Aaabbb123");
        changeScene("/org/example/demo/fxml/Frontoffice/HomePage.fxml");
        //changeScene("/org/example/demo/fxml/Frontoffice/posts/AjoutPosts.fxml");
        //changeScene("/org/example/demo/fxml/Security/Login.fxml");
        //changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/listproduit.fxml");
        //changeScene("/org/example/demo/fxml/Frontoffice/pointRecyclage/addRecyclingPoint.fxml");
        primaryStage.show();
    }

    public static void changeScene(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(HelloApplication.class.getResource(fxmlFile));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void changeSceneWithController(String fxmlPath, Consumer<Object> controllerConsumer) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(fxmlPath));
        Scene scene = new Scene(fxmlLoader.load());
        Object controller = fxmlLoader.getController();
        controllerConsumer.accept(controller);
        primaryStage.setScene(scene);
    }

    public static void error(String msg) {
        Image img = new Image("error.png");
        Notifications notification = Notifications.create()
                .title("Error")
                .text(msg)
                .hideAfter(Duration.seconds(3))
                .position(Pos.TOP_RIGHT);
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(50);  // Set the height of the image to match the size of the default icon
        imageView.setFitWidth(50);
        notification.graphic(imageView);
        notification.show();
    }

    public static void succes(String msg) {
        Image img = new Image("check.png");
        Notifications notification = Notifications.create()
                .title("Great")
                .text(msg)
                .hideAfter(Duration.seconds(6))
                .position(Pos.TOP_RIGHT);
        ImageView imageView = new ImageView(img);
        imageView.setFitHeight(50);  // Set the height of the image to match the size of the default icon
        imageView.setFitWidth(50);
        notification.graphic(imageView);
        notification.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch();
    }
    
}