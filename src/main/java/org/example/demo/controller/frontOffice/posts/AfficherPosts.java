package org.example.demo.controller.frontOffice.posts;

import javafx.geometry.*;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.demo.models.Posts;
import org.example.demo.services.posts.IService;
import org.example.demo.services.posts.PostsService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AfficherPosts {

    @FXML
    private VBox postsContainer;

    private IService<Posts> postsService = new PostsService();
    private ObservableList<Posts> postsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadPosts();
    }

    private void loadPosts() {

        try {
            // Clear existing data
            postsList.clear();
            postsContainer.getChildren().clear();

            // Get all posts from the service
            List<Posts> posts = postsService.rechercher();
            postsList.addAll(posts);

            // Create post cards with staggered animations
            for (int i = 0; i < postsList.size(); i++) {
                Posts post = postsList.get(i);
                VBox postCard = createFacebookStylePost(post);
                postsContainer.getChildren().add(postCard);

                // Animate with staggered timing
                animatePostCard(postCard, i * 50);
            }
        } catch (SQLException e) {
            showError("Error loading posts: " + e.getMessage());
        }
    }

    private void animatePostCard(VBox postCard, int delayMs) {
        postCard.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), postCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.millis(delayMs));
        fadeIn.play();
    }

    private Button createIconButton(String iconFileName, String tooltipText, String colorHex) {
        Button button = new Button();

        // Load the icon
        InputStream iconStream = getClass().getResourceAsStream("/icons/" + iconFileName);
        if (iconStream != null) {
            Image icon = new Image(iconStream);
            ImageView iconView = new ImageView(icon);
            iconView.setFitWidth(16); // taille de l’icône
            iconView.setFitHeight(16);
            button.setGraphic(iconView);
        } else {
            System.out.println("Icon not found: " + iconFileName);
            button.setText("?"); // fallback
        }

        // Style the button
        button.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 30px;" +
                        "-fx-min-height: 30px;" +
                        "-fx-max-width: 30px;" +
                        "-fx-max-height: 30px;" +
                        "-fx-padding: 0;"
        );

        // Add tooltip
        if (tooltipText != null) {
            button.setTooltip(new Tooltip(tooltipText));
        }

        return button;
    }


    private VBox createFacebookStylePost(Posts post) {
        // Main post container
        VBox postCard = new VBox();
        postCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        postCard.setSpacing(10);
        postCard.setMaxWidth(Double.MAX_VALUE);

        // Header section with profile pic, name, date, and action buttons
        StackPane headerStack = new StackPane();

        // Left side: profile and name
        HBox headerBox = new HBox();
       // headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);
        headerBox.setPadding(new Insets(12, 12, 5, 12));

        // Profile picture (circle with first letter)
        StackPane profilePic = new StackPane();
        Circle circle = new Circle(20);
        circle.setFill(Color.valueOf("#1877f2"));

        Label initial = new Label(post.getTitle().substring(0, 1).toUpperCase());
        initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        profilePic.getChildren().addAll(circle, initial);

        // Name and date container
        VBox nameDate = new VBox();
        nameDate.setSpacing(2);

        // Name (using title as name)
        Label nameLabel = new Label(post.getTitle());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        // Date (using created_at)
        Label dateLabel = new Label(post.getCreated_at());
        dateLabel.setStyle("-fx-text-fill: #65676b; -fx-font-size: 12px;");

        nameDate.getChildren().addAll(nameLabel, dateLabel);

        headerBox.getChildren().addAll(profilePic, nameDate);

        // Right side: action buttons
        HBox actionsBox = new HBox();
    //  actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setSpacing(5);
        actionsBox.setPadding(new Insets(8));

        // Create action buttons with icons
        Button detailsBtn = createIconButton("arrowicon.png", "Voir les détails", "#005eff");
        detailsBtn.setOnAction(e -> openPostDetails(post));

        Button editBtn = createIconButton("editicon.png", "Modifier", "#005eff");
        editBtn.setOnAction(e -> openModifierPosts(post));

        Button deleteBtn = createIconButton("deleteicon.png", "Supprimer", "#ff4c4c");
        deleteBtn.setOnAction(e -> deletePost(post));


        actionsBox.getChildren().addAll(detailsBtn, editBtn, deleteBtn);

        // Stack the header components
       StackPane.setAlignment(headerBox, Pos.CENTER_LEFT);
       StackPane.setAlignment(actionsBox, Pos.CENTER_RIGHT);
        headerStack.getChildren().addAll(headerBox, actionsBox);

        // Content section
        VBox contentBox = new VBox();
        contentBox.setSpacing(8);
        contentBox.setPadding(new Insets(0, 12, 12, 12));

        // Type tag (as a badge)
        Label typeLabel = new Label(post.getType());
        typeLabel.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #1877f2; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px;");

        // Description
        Label descriptionLabel = new Label(post.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 13px;");

        contentBox.getChildren().addAll(typeLabel, descriptionLabel);

        // Image section (if available)
        if (post.getImages() != null && !post.getImages().isEmpty() && !post.getImages().equals("null")) {
            try {
                ImageView imageView = createPostImage(post);

                VBox imageBox = new VBox(imageView);
                imageBox.setAlignment(Pos.CENTER);
                imageBox.setPadding(new Insets(5, 0, 5, 0));

                contentBox.getChildren().add(imageBox);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
            }
        }

        // Footer section with status indicator
        HBox footerBox = new HBox();
      footerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.setPadding(new Insets(8, 12, 8, 12));
        footerBox.setStyle("-fx-border-color: transparent transparent transparent transparent; -fx-border-width: 1 0 0 0; -fx-border-style: solid;");

        // Status indicator
        Circle statusDot = new Circle(4);
        statusDot.setFill("Active".equals(post.getStatus()) ? Color.valueOf("#42b72a") : Color.GRAY);

        Label statusLabel = new Label(post.getStatus());
        statusLabel.setStyle("-fx-text-fill: #65676b; -fx-font-size: 12px;");
        statusLabel.setPadding(new Insets(0, 0, 0, 5));

        // Add small ID indicator (subtle)
        Label idLabel = new Label("ID: " + post.getId());
        idLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footerBox.getChildren().addAll(statusDot, statusLabel, spacer, idLabel);

        // Add all sections to the post card
        postCard.getChildren().addAll(headerStack, contentBox, footerBox);

        return postCard;
    }

    private Button createSocialButton(String icon) {
        Button button = new Button(icon);
        button.setStyle("-fx-background-color: #e4e6eb; -fx-background-radius: 50%; -fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px; -fx-padding: 0;");
        return button;
    }

    private ImageView createPostImage(Posts post) {

        // Create a container for better visibility
        BorderPane imageContainer = new BorderPane();
        imageContainer.setStyle("-fx-border-color: red; -fx-border-width: 2; -fx-background-color: #f8f8f8;");
        imageContainer.setPrefSize(300, 250);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(250);
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(true);

        // Set the alignment of the image within the container
         BorderPane.setAlignment(imageView, Pos.CENTER);
        imageContainer.setCenter(imageView);
        try {
            // Clean up the path - remove brackets and quotes if present
            String imagePath = post.getImages();
            if (imagePath != null) {
                imagePath = imagePath.replaceAll("\\[\"", "").replaceAll("\"\\]", "");
            }

            System.out.println("Cleaned image path: " + imagePath);

            if (imagePath == null || imagePath.isEmpty() || imagePath.equals("null")) {
                throw new Exception("Invalid image path");
            }

            // Try loading the image with absolute path first
            try {
                // Check if file exists on disk
                File imageFile = new File(imagePath);
                if (imageFile.exists() && imageFile.isFile()) {
                    System.out.println("Found image file on disk: " + imageFile.getAbsolutePath());
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);
                    return imageView;
                }
            } catch (Exception e) {
                System.out.println("Failed to load as direct file: " + e.getMessage());
            }

            // Try loading from resources folder
            try {
                String resourcePath = "ressources/images/posts/" + imagePath;
                System.out.println("Trying resource path: " + resourcePath);
                InputStream resourceStream = getClass().getResourceAsStream(resourcePath);

                if (resourceStream != null) {
                    Image image = new Image(resourceStream);
                    imageView.setImage(image);
                    System.out.println("Loaded from resources successfully");
                    return imageView;
                } else {
                    System.out.println("Resource not found: " + resourcePath);
                }
            } catch (Exception e) {
                System.out.println("Failed to load from resources: " + e.getMessage());
            }

            // If we reach here, we couldn't load the image
            throw new Exception("Could not locate image file");

        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());

            // Create a visual placeholder
            StackPane placeholderPane = new StackPane();
            placeholderPane.setPrefSize(250, 250);
            placeholderPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd; -fx-border-width: 1;");

            Label noImageLabel = new Label("Image Not Found\n" + post.getImages());
            noImageLabel.setTextFill(Color.GRAY);
            noImageLabel.setWrapText(true);
            noImageLabel.setTextAlignment(TextAlignment.CENTER);

            placeholderPane.getChildren().add(noImageLabel);

            // Take snapshot of the placeholder
            SnapshotParameters params = new SnapshotParameters();
            WritableImage writableImage = placeholderPane.snapshot(params, null);
            imageView.setImage(writableImage);
        }

        return imageView;
    }

    @FXML
    void refreshPosts(ActionEvent event) {
        loadPosts();
    }

    @FXML
    void addNewPost(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/posts/AjoutPosts.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Publication");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> refreshPosts(null));
            stage.show();
        } catch (IOException e) {
            showError("Error opening add posts form: " + e.getMessage());
        }
    }

    private void openPostDetails(Posts post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/posts/DetailsPosts.fxml"));
            Parent root = loader.load();

            DetailsPosts controller = loader.getController();
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle("Détails de la Publication");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error opening details view: " + e.getMessage());
        }
    }

    private void openModifierPosts(Posts post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/posts/ModifierPosts.fxml"));
            Parent root = loader.load();

            ModifierPosts controller = loader.getController();
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle("Modifier Publication");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> refreshPosts(null));
            stage.show();
        } catch (IOException e) {
            showError("Error opening edit form: " + e.getMessage());
        }
    }

    private void deletePost(Posts post) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer la publication");
        confirmDialog.setContentText("Voulez-vous vraiment supprimer cette publication ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                postsService.supprimer(post);
                loadPosts();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Publication supprimée avec succès !");
                alert.showAndWait();
            } catch (SQLException e) {
                showError("Error deleting post: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }
}