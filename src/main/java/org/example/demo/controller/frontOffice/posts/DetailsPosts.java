package org.example.demo.controller.frontOffice.posts;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.demo.controller.frontOffice.profile.profileController;
import org.example.demo.models.Comment;
import org.example.demo.models.Posts;
import org.example.demo.models.Reaction;
import org.example.demo.models.User;
import org.example.demo.services.posts.CommentService;
import org.example.demo.services.posts.IService;
import org.example.demo.services.posts.ProfanityFilterService;
import org.example.demo.services.posts.ReactionService;
import org.example.demo.services.user.userService;
import org.example.demo.utils.sessionManager;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Text;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Window;



import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class DetailsPosts {

    @FXML
    private Label titleLabel;

    @FXML
    private Label typeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label createdAtLabel;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private ImageView postImageView;

    @FXML
    private TextArea newCommentTextArea;

    @FXML
    private Button addCommentButton;

    @FXML
    private VBox commentsContainer;

    @FXML
    private Label noCommentsLabel;

    @FXML
    private Label commentsCountLabel;

    @FXML
    private VBox commentsSectionContainer;

    @FXML
    private Button commentToggleButton;

    @FXML
    private Button shareButton;

    @FXML
    private Button likeButton;
    @FXML
    private HBox reactionsContainer;
    private ReactionService reactionService;
    private Map<String, String> reactionEmojis;
    private Map<String, Integer> reactionCounts;

    @FXML private Label authorLabel;
    @FXML private Label profanityWarningLabel;

    private Posts currentPost;
    private profileController parentController;
    private IService<Comment> commentService = new CommentService();
    private Comment commentBeingEdited = null;
    User user = new User();
    private ProfanityFilterService profanityFilter;

    @FXML
    private final userService userService = new userService();

    @FXML
    public void initialize() {
        // Initialize the profanity filter
        profanityFilter = new ProfanityFilterService();

        // Create profanity warning label if it doesn't exist
        if (profanityWarningLabel == null) {
            profanityWarningLabel = new Label("Comment contains inappropriate language");
            profanityWarningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");
            profanityWarningLabel.setVisible(false);
            profanityWarningLabel.setManaged(false);

            // Add to UI, assuming commentControlsBox is the VBox containing comment controls
            commentsSectionContainer.getChildren().add(profanityWarningLabel);
        }

        // Set up real-time profanity checking in the text area
        setupProfanityChecker();
        // Initialize the comments section
        commentsSectionContainer.setVisible(false);
        commentsSectionContainer.setManaged(false);

        newCommentTextArea.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                event.consume();
                addComment(new ActionEvent());
            }
        });

        // Make the textarea auto-expand
        newCommentTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            String[] lines = newValue.split("\n");
            int rowCount = Math.min(5, Math.max(1, lines.length));
            newCommentTextArea.setPrefRowCount(rowCount);
        });

        if (shareButton != null) {
            shareButton.setOnAction(this::showShareOptions);
        }
        // Initialize reaction service
        reactionService = new ReactionService();

        // Initialize reaction emojis map
        reactionEmojis = new HashMap<>();
        reactionEmojis.put("like", "👍");
        reactionEmojis.put("bravo", "👏");
        reactionEmojis.put("soutien", "❤️");
        reactionEmojis.put("instructif", "💡");
        reactionEmojis.put("drole", "😂");

        // reaction counts
        reactionCounts = new HashMap<>();

        // click handler
        if (likeButton != null) {
            likeButton.setOnAction(this::showReactionOptions);
        }
    }

    private void setupProfanityChecker() {
        // Add listener to the text area
        newCommentTextArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (profanityFilter.containsProfanity(newValue)) {
                profanityWarningLabel.setText("Comment contains inappropriate language");
                profanityWarningLabel.setVisible(true);
                profanityWarningLabel.setManaged(true);
                addCommentButton.setDisable(true); // Disable post button when profanity detected
            } else {
                profanityWarningLabel.setVisible(false);
                profanityWarningLabel.setManaged(false);
                addCommentButton.setDisable(false);
            }
        });
    }

    @FXML
    private void showShareOptions(ActionEvent event) {
        Stage shareStage = new Stage();
        shareStage.initModality(Modality.APPLICATION_MODAL);
        shareStage.initStyle(StageStyle.DECORATED);
        shareStage.setTitle("Share Post");
        shareStage.setResizable(false);

        // container lel share options
        VBox shareContainer = new VBox(10);
        shareContainer.setAlignment(Pos.CENTER);
        shareContainer.setPadding(new Insets(20));
        shareContainer.setStyle("-fx-background-color: #f2f2f2;");


        Label titleLabel = new Label("Share this post");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");

        // our options
        HBox shareOptions = new HBox(15);
        shareOptions.setAlignment(Pos.CENTER);
        shareOptions.setPadding(new Insets(10));

        // Fb
        Button facebookButton = new Button("Facebook");
        facebookButton.setStyle("-fx-background-color: #3b5998; -fx-text-fill: white;");
        facebookButton.setOnAction(e -> shareToFacebook());

        // LinkedIn
        Button linkedInButton = new Button("LinkedIn");
        linkedInButton.setStyle("-fx-background-color: #0077b5; -fx-text-fill: white;");
        linkedInButton.setOnAction(e -> shareToLinkedIn());

        // WhatsApp
        Button whatsAppButton = new Button("WhatsApp");
        whatsAppButton.setStyle("-fx-background-color: #25D366; -fx-text-fill: white;");
        whatsAppButton.setOnAction(e -> shareToWhatsApp());

        // Copy link
        Button copyLinkButton = new Button("Copy Link");
        copyLinkButton.setStyle("-fx-background-color: #444444; -fx-text-fill: white;");
        copyLinkButton.setOnAction(e -> {
            copyLinkToClipboard();
            showAlert(Alert.AlertType.INFORMATION, "Link Copied", "Link has been copied to clipboard.");
        });

        //buttons in container
        shareOptions.getChildren().addAll(facebookButton, linkedInButton, whatsAppButton, copyLinkButton);

        // Close button
        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> shareStage.close());

        // Add all elements to main container
        shareContainer.getChildren().addAll(titleLabel, shareOptions, closeButton);

        Scene scene = new Scene(shareContainer);
        shareStage.setScene(scene);
        shareStage.show();
    }

    private void shareToFacebook() {
        String postUrl = generatePostUrl();
        String shareUrl = "https://www.facebook.com/sharer/sharer.php?u=" + encodeURIComponent(postUrl);
        openInBrowser(shareUrl);
    }

    private void shareToLinkedIn() {
        String postUrl = generatePostUrl();
        String shareUrl = "https://www.linkedin.com/sharing/share-offsite/?url=" + encodeURIComponent(postUrl);
        openInBrowser(shareUrl);
    }

    private void shareToWhatsApp() {
        String postUrl = generatePostUrl();
        String postTitle = currentPost != null ? currentPost.getTitle() : "Post";
        String shareUrl = "https://api.whatsapp.com/send?text=" + encodeURIComponent(postTitle + " - " + postUrl);
        openInBrowser(shareUrl);
    }

    private void copyLinkToClipboard() {
        String postUrl = generatePostUrl();
        StringSelection stringSelection = new StringSelection(postUrl);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private static final String BASE_URL = "http://localhost:8080"; //najem nbadelha

    private String generatePostUrl() {
        if (currentPost != null) {
            return "http://localhost:8080/agrilink/posts/" + currentPost.getId();
        }
        return BASE_URL + "http://localhost:8080/agrilink/posts/";
    }

    private String encodeURIComponent(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (java.io.UnsupportedEncodingException e) {
            return s;
        }
    }

    private void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("xdg-open " + url);
            }
        } catch (IOException | URISyntaxException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not open browser: " + e.getMessage());
        }
    }

    //reaction section
    @FXML
    private void showReactionOptions(ActionEvent event) {
        // popup for reactions
        Popup reactionPopup = new Popup();
        reactionPopup.setAutoHide(true);

        // container for reaction buttons
        HBox reactionOptions = new HBox(10);
        reactionOptions.setAlignment(Pos.CENTER);
        reactionOptions.setPadding(new Insets(10));
        reactionOptions.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        // buttons for each reaction type
        for (Map.Entry<String, String> entry : reactionEmojis.entrySet()) {
            String type = entry.getKey();
            String emoji = entry.getValue();

            Button reactionBtn = createReactionButton(type, emoji);
            reactionOptions.getChildren().add(reactionBtn);
        }

        // zid lcontainer lel popup
        reactionPopup.getContent().add(reactionOptions);

        // Show the popup
        Node source = (Node) event.getSource();
        Window window = source.getScene().getWindow();
        reactionPopup.show(window,
                source.localToScreen(source.getBoundsInLocal()).getMinX(),
                source.localToScreen(source.getBoundsInLocal()).getMaxY() + 5);
    }

    private Button createReactionButton(String type, String emoji) {
        Button button = new Button(emoji);

        button.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 22));

        button.setStyle("-fx-background-color: transparent; -fx-text-fill: #333;");

        // Hover effect
        button.setOnMouseEntered(e -> {
            button.setScaleX(1.2);
            button.setScaleY(1.2);
            button.setStyle("-fx-background-color: #f0f2f5; -fx-text-fill: #000; -fx-background-radius: 50%;");
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
            button.setStyle("-fx-background-color: transparent; -fx-text-fill: #333;");
        });

        // Click action
        button.setOnAction(e -> addReaction(type));

        Tooltip tooltip = new Tooltip(capitalizeFirstLetter(type));
        Tooltip.install(button, tooltip);

        return button;
    }


    private void addReaction(String type) {
        try {
            // nchoufou ken luser has already reacted to this post
            int userId = sessionManager.getInstance().getUser().getId();
            List<Reaction> existingReactions = reactionService.getReactionsByPostAndUser(currentPost.getId(), userId);

            if (!existingReactions.isEmpty()) {
                // ken ey : update the reaction
                Reaction existingReaction = existingReactions.get(0);
                existingReaction.setType(type);
                reactionService.modifier(existingReaction);
            } else {
                // sinn create new reaction
                Reaction reaction = new Reaction();
                reaction.setPost_id(currentPost.getId());
                reaction.setUser_id(userId);
                reaction.setType(type);

                //timestamp
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                reaction.setCreated_at(now.format(formatter));

                reactionService.ajouter(reaction);
            }

            updateReactionCounts();
            updateReactionDisplay();

        } catch (SQLException e) {
            showError("Error adding reaction: " + e.getMessage());
        }
    }

    private void updateReactionCounts() {
        try {
            // na7y current count
            reactionCounts.clear();

            // get all reactions for this post
            List<Reaction> reactions = reactionService.getReactionsByPostId(currentPost.getId());

            // Count reactions by type
            for (Reaction reaction : reactions) {
                String type = reaction.getType();
                reactionCounts.put(type, reactionCounts.getOrDefault(type, 0) + 1);
            }
        } catch (SQLException e) {
            showError("Error updating reaction counts: " + e.getMessage());
        }
    }

    private void updateReactionDisplay() {
        reactionsContainer.getChildren().clear();

        // 5aly top 3 reactions with counts
        List<Map.Entry<String, Integer>> sortedReactions = reactionCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        for (Map.Entry<String, Integer> entry : sortedReactions) {
            String type = entry.getKey();
            Integer count = entry.getValue();

            // Create reaction indicator
            HBox reactionBox = new HBox(5);
            reactionBox.setAlignment(Pos.CENTER);

            // Emoji
            Label emojiLabel = new Label(reactionEmojis.get(type));
            emojiLabel.setStyle("-fx-font-size: 14px;");

            // Count
            Label countLabel = new Label(count.toString());
            countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #65676B;");

            reactionBox.getChildren().addAll(emojiLabel, countLabel);
            reactionsContainer.getChildren().add(reactionBox);
        }

        // Add total count if there are more than 3 reaction types
        int totalReactions = reactionCounts.values().stream().mapToInt(Integer::intValue).sum();
        if (totalReactions > 0) {
            Label totalLabel = new Label(totalReactions + " reactions");
            totalLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #65676B; -fx-padding: 0 0 0 10;");
            reactionsContainer.getChildren().add(totalLabel);
        }
    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input.substring(0, 1).toUpperCase() + input.substring(1);
    }


    public void setPost(Posts post) {
        this.currentPost = post;
        displayPostDetails();
        loadComments();
        updateReactionCounts();
        updateReactionDisplay();

    }

    public void setParentController(profileController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void goBack() {
        if (parentController != null) {
            parentController.showPostsList();
        }
    }

    private void displayPostDetails() {
        if (currentPost != null) {
            titleLabel.setText(currentPost.getTitle());
            typeLabel.setText(currentPost.getType());
            statusLabel.setText(currentPost.getStatus());
            createdAtLabel.setText(currentPost.getCreated_at());
            descriptionTextArea.setText(currentPost.getDescription());

            // Set Author Name
            if (currentPost.getUser_id_id() != 0) {
                try {
                    User postAuthor = userService.getUserById(currentPost.getUser_id_id());
                    if (postAuthor != null) {
                        String authorName = postAuthor.getNom() + " " + postAuthor.getPrenom();
                        authorLabel.setText(authorName);
                        authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
                        System.out.println("Author set to: " + authorName);
                    } else {
                        authorLabel.setText("Unknown Author");
                        authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
                    }
                } catch (Exception e) {
                    System.err.println("Erreur en récupérant l'auteur : " + e.getMessage());
                    authorLabel.setText("Unknown Author");
                    authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
                }
            } else {
                authorLabel.setText("Unknown Author");
                authorLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
            }

            // Load image if available
            if (currentPost.getImages() != null && !currentPost.getImages().isEmpty()) {
                try {
                    Image image = new Image(currentPost.getImages());
                    postImageView.setImage(image);
                } catch (Exception e) {
                    // Handle image loading error
                    System.err.println("Error loading image: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    void toggleCommentsSection(ActionEvent event) {
        boolean isVisible = !commentsSectionContainer.isVisible();
        commentsSectionContainer.setVisible(isVisible);
        commentsSectionContainer.setManaged(isVisible);

        if (isVisible) {
            newCommentTextArea.requestFocus();
        }
    }

    private void loadComments() {
        try {
            commentsContainer.getChildren().clear();
            if (noCommentsLabel != null) {
                commentsContainer.getChildren().add(noCommentsLabel);
            }

            List<Comment> comments = ((CommentService) commentService).getCommentsByPostId(currentPost.getId());


            commentsCountLabel.setText(comments.size() + " comments");

            for (Comment comment : comments) {
                addVisualComment(comment);
            }

            if (noCommentsLabel != null) {
                noCommentsLabel.setVisible(comments.isEmpty());
            }

        } catch (SQLException e) {
            showError("Error loading comments: " + e.getMessage());
        }
    }

    @FXML
    void addComment(ActionEvent event) {
        String commentContent = newCommentTextArea.getText().trim();
        if (commentContent.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Le commentaire ne peut pas être vide.");
            return;
        }

        // Check for profanity before posting
        if (profanityFilter.containsProfanity(commentContent)) {
            // Get a suggested clean version of the text
            String suggestedText = profanityFilter.getSuggestedText(commentContent);

            // Show alert with options
            Alert profanityAlert = new Alert(Alert.AlertType.WARNING);
            profanityAlert.setTitle("Let's mind our language dear user :)");
            profanityAlert.setHeaderText("Agrilink is a platform for all ages !");

            // Create content with options
            VBox content = new VBox(10);
            content.setPadding(new Insets(10, 10, 10, 10));

            Label messageLabel = new Label("Please remove inappropriate language before posting.");
            Label suggestedLabel = new Label("Would you like to proceed with the words censored ?");
            Label suggestedTextLabel = new Label(suggestedText);
            suggestedTextLabel.setStyle("-fx-font-weight: bold;");

            content.getChildren().addAll(messageLabel, suggestedLabel, suggestedTextLabel);
            profanityAlert.getDialogPane().setContent(content);

            // Add custom buttons
            ButtonType useButton = new ButtonType("Yes");
            ButtonType editButton = new ButtonType("Edit Comment");
            profanityAlert.getButtonTypes().setAll(useButton, editButton);

            Optional<ButtonType> result = profanityAlert.showAndWait();
            if (result.isPresent() && result.get() == useButton) {
                // Use the censored version
                commentContent = suggestedText;
            } else {
                // Let the user edit their comment
                return;
            }
        }

        try {
            if (commentBeingEdited != null) {

                commentBeingEdited.setContent(commentContent);
                commentBeingEdited.setUser_commented_id(sessionManager.getInstance().getUser().getId());

                commentService.modifier(commentBeingEdited);

                // Reset the comment being edited
                commentBeingEdited = null;
                addCommentButton.setGraphic(new Label("➤"));
            } else {

                Comment comment = new Comment();
                comment.setPost_id_id(currentPost.getId());
                comment.setContent(commentContent);
                comment.setUser_commented_id(sessionManager.getInstance().getUser().getId()); // Default user ID

                //  current timestamp
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                comment.setCreated_at(now.format(formatter));

                // save the comment
                commentService.ajouter(comment);
            }

            newCommentTextArea.clear();

            // Refresh comments
            loadComments();

        } catch (SQLException e) {
            showError("Error with comment: " + e.getMessage());
        }
    }

    private void addVisualComment(Comment comment) {
        // Create visual comment component (Facebook style)
        HBox commentBox = new HBox(8);
        commentBox.setAlignment(Pos.TOP_LEFT);
        commentBox.setPadding(new Insets(4, 0, 4, 0));

        // User profile picture
        Circle userCircle = new Circle(16, Color.LIGHTGRAY);

        // Comment content container
        VBox contentBox = new VBox(3);
        contentBox.setMaxWidth(500);

        // Comment bubble with user info and content
        VBox bubble = new VBox(3);
        bubble.setPadding(new Insets(8, 12, 8, 12));
        bubble.setStyle("-fx-background-color: #f0f2f5; -fx-background-radius: 18;");

        // User name
        Label nameLabel = new Label("User " + comment.getUser_commented_id());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        // Comment text
        Text contentText = new Text(comment.getContent());
        contentText.setWrappingWidth(450);

        bubble.getChildren().addAll(nameLabel, contentText);

        // Interaction buttons
        HBox interactionBox = new HBox(15);
        interactionBox.setPadding(new Insets(2, 0, 2, 12));

        // Like, Reply buttons
        Button likeBtn = new Button("Like");
        likeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676B; -fx-font-size: 12px; -fx-font-weight: bold;");

        Button replyBtn = new Button("Reply");
        replyBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676B; -fx-font-size: 12px; -fx-font-weight: bold;");

        // Edit and Delete buttons (only for user's own comments)
        Button editBtn = new Button("Edit");
        editBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676B; -fx-font-size: 12px; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> startEditingComment(comment));

        Button deleteBtn = new Button("Delete");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #65676B; -fx-font-size: 12px; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> deleteComment(comment));

        // Time stamp
        Label timeLabel = new Label(formatCommentTime(comment.getCreated_at()));
        timeLabel.setStyle("-fx-text-fill: #65676B; -fx-font-size: 12px;");

        interactionBox.getChildren().addAll(likeBtn, replyBtn, editBtn, deleteBtn, timeLabel);

        // Add components to containers
        contentBox.getChildren().addAll(bubble, interactionBox);
        commentBox.getChildren().addAll(userCircle, contentBox);

        // Add to the comments container
        commentsContainer.getChildren().add(commentBox);

        // Hide the no comments label
        if (noCommentsLabel != null) {
            noCommentsLabel.setVisible(false);
        }
    }

    private String formatCommentTime(String timestamp) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime commentTime = LocalDateTime.parse(timestamp, inputFormatter);
            LocalDateTime now = LocalDateTime.now();

            long minutes = ChronoUnit.MINUTES.between(commentTime, now);
            if (minutes < 1) return "Just now";
            if (minutes < 60) return minutes + "m";

            long hours = ChronoUnit.HOURS.between(commentTime, now);
            if (hours < 24) return hours + "h";

            long days = ChronoUnit.DAYS.between(commentTime, now);
            if (days < 7) return days + "d";

            // Default format for older comments
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMM d");
            return commentTime.format(outputFormatter);

        } catch (Exception e) {
            return timestamp;
        }
    }

    private void startEditingComment(Comment comment) {
        commentBeingEdited = comment;

        // n7ot acomment content to the textarea
        newCommentTextArea.setText(comment.getContent());

        // Make the comments section visible if it's not
        commentsSectionContainer.setVisible(true);
        commentsSectionContainer.setManaged(true);

        // Change the send button to indicate editing mode
        Label editIcon = new Label("✎");
        editIcon.setStyle("-fx-text-fill: #E65100; -fx-font-weight: bold;");
        addCommentButton.setGraphic(editIcon);

        newCommentTextArea.requestFocus();
    }

    private void deleteComment(Comment comment) {
        // confirmy
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer le commentaire");
        confirmDialog.setContentText("Voulez-vous vraiment supprimer ce commentaire ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                commentService.supprimer(comment);

                loadComments();

                // If the deleted comment was being edited, reset the edit state
                if (commentBeingEdited != null && commentBeingEdited.getId() == comment.getId()) {
                    commentBeingEdited = null;
                    newCommentTextArea.clear();
                    Label sendIcon = new Label("➤");
                    sendIcon.setStyle("-fx-text-fill: #1877f2; -fx-font-weight: bold;");
                    addCommentButton.setGraphic(sendIcon);
                }
            } catch (SQLException e) {
                showError("Error deleting comment: " + e.getMessage());
            }
        }
    }

    private void showError(String message) {
        showAlert(Alert.AlertType.ERROR, "Erreur", message);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

}