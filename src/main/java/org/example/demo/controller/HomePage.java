package org.example.demo.controller;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.userSugguestionCard;
import org.example.demo.models.Posts;
import org.example.demo.models.User;
import org.example.demo.services.posts.PostsService;
import org.example.demo.controller.frontOffice.posts.AjouterPosts;
import org.example.demo.services.posts.PostSentimentAPI;
import org.example.demo.services.user.MatchService;
import org.example.demo.services.user.userService;
import org.example.demo.services.ressource.MeteoService;
import org.example.demo.utils.sessionManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class HomePage implements Initializable {

    @FXML
    private TextField ville;
    @FXML
    private Text meteo;
    @FXML
    private VBox postsContainer;
    @FXML
    private ListView<AnchorPane> userSugguested;

    private MeteoService meteoservice = new MeteoService(); // Initialize the service
    private List<Posts> postsList = new ArrayList<>();
    private PostsService postsService = new PostsService();
    private userService userService = new userService();
    private PostSentimentAPI sentimentAPI = new PostSentimentAPI();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        userService userService = new userService();
        MatchService matchService = new MatchService(userService);

        System.out.println("HomePage initializing...");
        Platform.runLater(() -> {
            try {
                // Add debugging to check your database
                List<Posts> testPosts = postsService.rechercher();
                if (!testPosts.isEmpty()) {
                    for (int i = 0; i < Math.min(3, testPosts.size()); i++) {
                        Posts p = testPosts.get(i);
                    }
                }

                loadHomepagePosts();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        List<User> users = matchService.getMatchedUsers(sessionManager.getInstance().getUser().getId()); // méthode pour charger les utilisateurs

        for (User user : users) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/userSugguestionCard.fxml"));
                AnchorPane card = loader.load();
                userSugguestionCard controller = loader.getController();

                controller.setUserData(user);

                userSugguested.getItems().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void onLogoutButtonClick() {
        sessionManager.getInstance().clearSession();
        HelloApplication.changeScene("/org/example/demo/fxml/Security/Login.fxml");
    }

    @FXML
    public void createpost() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/posts/AjouterPosts.fxml");
    }

    @FXML
    public void showMeteo() {
        if (ville.getText() == null || ville.getText().isEmpty()) {
            showError("Please enter a city name");
            return;
        }
        meteo.setText(meteoservice.getMeteo(ville.getText()));
    }

    public void loadHomepagePosts() {
        try {
            System.out.println("Loading homepage posts...");

            postsList.clear();
            postsContainer.getChildren().clear();

            // Get all posts
            List<Posts> posts = postsService.rechercher();

            if (posts.isEmpty()) {
                System.out.println("No posts found in database");
                Label noPostsLabel = new Label("No posts available");
                noPostsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #65676b; -fx-padding: 20;");
                postsContainer.getChildren().add(noPostsLabel);
                return;
            }


            for (Posts post : posts) {
                if (post.getSentiment() == null || post.getSentiment().equals(Posts.SENTIMENT_UNKNOWN)) {
                    try {
                        sentimentAPI.analyzeAndUpdatePostSentiment(post);
                        postsService.modifier(post);
                    } catch (Exception e) {
                    }
                }
            }
            postsList.addAll(posts);

            for (int i = 0; i < postsList.size(); i++) {
                final int index = i;
                Platform.runLater(() -> {
                    Posts post = postsList.get(index);
                    VBox postCard = createSimplePostCard(post);
                    postsContainer.getChildren().add(postCard);
                    animatePostCard(postCard, index * 50);
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading posts: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            showError("Unexpected error: " + e.getMessage());
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

    private VBox createSimplePostCard(Posts post) {
        User postAuthor = null;
        try {
            postAuthor = this.userService.getUserById(post.getUser_id_id());
        } catch (Exception e) {
            System.out.println("Error getting user: " + e.getMessage());
            // Create a fallback user
            postAuthor = new User();
            postAuthor.setNom("Unknown");
            postAuthor.setPrenom("User");
        }

        // Main post container
        VBox postCard = new VBox();
        postCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        postCard.setSpacing(10);
        postCard.setMaxWidth(Double.MAX_VALUE);
        postCard.setPrefWidth(Region.USE_COMPUTED_SIZE);

        // Header section with profile pic, name, date
        StackPane headerStack = new StackPane();

        // Left side: profile and name
        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        headerBox.setPadding(new Insets(12, 12, 5, 12));

        // Profile picture (circle with first letter)
        StackPane profilePic = new StackPane();
        Circle circle = new Circle(20);
        circle.setFill(Color.valueOf("#1877f2"));

        // Safely get first letter
        String titleInitial = "?";
        if (post.getTitle() != null && !post.getTitle().isEmpty()) {
            titleInitial = post.getTitle().substring(0, 1).toUpperCase();
        }

        Label initial = new Label(titleInitial);
        initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        profilePic.getChildren().addAll(circle, initial);

        // Name and date container
        VBox nameDate = new VBox();
        nameDate.setSpacing(2);

        // Name
        Label nameLabel = new Label(postAuthor.getNom() + " " + postAuthor.getPrenom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: black;");

        // Date (created_at)
        String dateText = post.getCreated_at() != null ? post.getCreated_at() : "Unknown date";
        Label dateLabel = new Label(dateText);
        dateLabel.setStyle("-fx-text-fill: #65676b; -fx-font-size: 12px;");

        nameDate.getChildren().addAll(nameLabel, dateLabel);
        headerBox.getChildren().addAll(profilePic, nameDate);

        // Right side: just view details button
        HBox actionsBox = new HBox();
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setPadding(new Insets(8));

        // Only include view details button
        Button detailsBtn = createIconButton("arrowicon.png", "Voir le profil", "#005eff");
        detailsBtn.setOnAction(e -> openPostDetails(post));
        actionsBox.getChildren().add(detailsBtn);

        // Stack the header components
        StackPane.setAlignment(headerBox, Pos.CENTER_LEFT);
        StackPane.setAlignment(actionsBox, Pos.CENTER_RIGHT);
        headerStack.getChildren().addAll(headerBox, actionsBox);

        // Content section
        VBox contentBox = new VBox();
        contentBox.setSpacing(8);
        contentBox.setPadding(new Insets(0, 12, 12, 12));

        // Type tag (as a badge)
        String typeValue = post.getType() != null ? post.getType() : "Unspecified";
        Label typeLabel = new Label(typeValue);
        typeLabel.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #1877f2; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px;");

        //sentiment
        String sentimentValue = post.getSentiment() != null ? post.getSentiment() : "Unknown";
        Label sentimentLabel = new Label(sentimentValue);
        String sentimentColor;
        switch (sentimentValue.toLowerCase()) {
            case "positive":
                sentimentColor = "#4CAF50"; // Green
                break;
            case "negative":
                sentimentColor = "#F44336"; // Red
                break;
            case "neutral":
                sentimentColor = "#9E9E9E"; // Gray
                break;
            default:
                sentimentColor = "#9E9E9E"; // Gray for unknown
        }

        // Style sentiment label
        sentimentLabel.setStyle("-fx-background-color: " + sentimentColor + "20; -fx-text-fill: " + sentimentColor +
                "; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px;");

        HBox tagsBox = new HBox(10);
        tagsBox.getChildren().addAll(typeLabel, sentimentLabel);

        // Title
        String titleText = post.getTitle() != null ? post.getTitle() : "No title";
        Label titleLabel = new Label(titleText);
        titleLabel.setWrapText(true);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: black;");

        // Description
        String descText = post.getDescription() != null ? post.getDescription() : "No description";
        Label descriptionLabel = new Label(descText);
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: black;");

        contentBox.getChildren().addAll(tagsBox, titleLabel, descriptionLabel);

        // Image section (if available)
        if (post.getImages() != null && !post.getImages().isEmpty() && !post.getImages().equals("null")) {
            try {
                ImageView imageView = createPostImage(post);

                StackPane imageContainer = new StackPane(imageView);
                imageContainer.setAlignment(Pos.CENTER);
                imageContainer.setStyle("-fx-border-color: #e4e6eb; -fx-border-radius: 4;");
                imageContainer.setPadding(new Insets(5));

                contentBox.getChildren().add(imageContainer);
            } catch (Exception e) {
            }
        }

        // Footer section with status indicator
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.setPadding(new Insets(8, 12, 8, 12));
        footerBox.setStyle("-fx-border-color: transparent transparent transparent transparent; -fx-border-width: 1 0 0 0; -fx-border-style: solid;");

        // Status indicator
        String status = post.getStatus() != null ? post.getStatus() : "Inactive";
        Circle statusDot = new Circle(4);
        statusDot.setFill("Active".equals(status) ? Color.valueOf("#42b72a") : Color.GRAY);

        Label statusLabel = new Label(status);
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


    private ImageView createPostImage(Posts post) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(500.0);
        imageView.setFitHeight(300.0);
        imageView.setPreserveRatio(true);

        try {
            String imagePath = post.getImages();
            if (imagePath != null) {
                imagePath = imagePath.replaceAll("\\[\"", "").replaceAll("\"\\]", "");
            }

            if (imagePath == null || imagePath.isEmpty() || imagePath.equals("null")) {
                throw new Exception("Invalid image path");
            }

            // Try loading from resources folder
            try {
                String resourcePath = "/images/posts/" + imagePath;
                InputStream resourceStream = getClass().getResourceAsStream(resourcePath);

                if (resourceStream != null) {
                    Image image = new Image(resourceStream);
                    imageView.setImage(image);
                    return imageView;
                } else {
                }
            } catch (Exception e) {
            }

            // Fallback to placeholder
            throw new Exception("Could not locate image file");

        } catch (Exception e) {
            // Create a placeholder
            Rectangle placeholder = new Rectangle(500, 300);
            placeholder.setFill(Color.LIGHTGRAY);

            Text noImageText = new Text("Image Not Found");
            noImageText.setFill(Color.GRAY);

            StackPane placeholderPane = new StackPane(placeholder, noImageText);

            SnapshotParameters params = new SnapshotParameters();
            WritableImage writableImage = placeholderPane.snapshot(params, null);
            imageView.setImage(writableImage);
        }

        return imageView;
    }

    private void openPostDetails(Posts post) {
        try {
            // Instead of showing post details, navigate to the user's profile
            // First get the user who created this post
            User postAuthor = this.userService.getUserById(post.getUser_id_id());

            // Store this user in the session manager to view their profile
            // Note: This assumes your app structure allows viewing other profiles
            // by setting the user in the session manager
            sessionManager.getInstance().setUser(postAuthor);

            // Navigate to the profile page, which will load with the post author's data
            HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to navigate to user profile: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
