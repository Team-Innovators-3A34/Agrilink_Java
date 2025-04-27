package org.example.demo.controller.backOffice.posts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.demo.models.Comment;
import org.example.demo.models.Posts;
import org.example.demo.services.posts.CommentService;
import org.example.demo.services.posts.IService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
    private TableView<Comment> commentsTable;

    @FXML
    private TableColumn<Comment, Integer> commentIdColumn;

    @FXML
    private TableColumn<Comment, String> commentContentColumn;

    @FXML
    private TableColumn<Comment, String> commentAuthorColumn;

    @FXML
    private TableColumn<Comment, String> commentDateColumn;

    @FXML
    private TableColumn<Comment, Void> commentActionsColumn;

    @FXML
    private TextArea newCommentTextArea;

    @FXML
    private Button addCommentButton;

    private Posts currentPost;
    private IService<Comment> commentService = new CommentService();
    private ObservableList<Comment> commentsList = FXCollections.observableArrayList();
    private Comment commentBeingEdited = null;

    @FXML
    public void initialize() {
        // Set up the comments table columns
        commentIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        commentContentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
        commentAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("user_commented_id")); // Using your field name
        commentDateColumn.setCellValueFactory(new PropertyValueFactory<>("created_at"));


        commentContentColumn.setCellFactory(column -> {
            return new TableCell<Comment, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                        setTooltip(null);
                    } else {
                        String shortText = item.length() > 30 ? item.substring(0, 30) + "..." : item;
                        setText(shortText);
                        setTooltip(new Tooltip(item));
                    }
                }
            };
        });

        // Add edit and delete buttons to the actions column
        addButtonsToCommentsTable();
    }

    public void setPost(Posts post) {
        this.currentPost = post;
        displayPostDetails();
        loadComments();
    }

    private void displayPostDetails() {
        if (currentPost != null) {
            titleLabel.setText(currentPost.getTitle());
            typeLabel.setText(currentPost.getType());
            statusLabel.setText(currentPost.getStatus());
            createdAtLabel.setText(currentPost.getCreated_at());
            descriptionTextArea.setText(currentPost.getDescription());

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

    private void addButtonsToCommentsTable() {
        Callback<TableColumn<Comment, Void>, TableCell<Comment, Void>> cellFactory = new Callback<>() {
            @Override
            public TableCell<Comment, Void> call(final TableColumn<Comment, Void> param) {
                return new TableCell<>() {
                    private final Button editBtn = new Button("Modifier");
                    private final Button deleteBtn = new Button("Supprimer");
                    private final HBox pane = new HBox(5, editBtn, deleteBtn);

                    {
                        // Style buttons
                        editBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white;");
                        deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");

                        // Edit button action
                        editBtn.setOnAction(event -> {
                            Comment comment = getTableView().getItems().get(getIndex());
                            startEditingComment(comment);
                        });

                        // Delete button action
                        deleteBtn.setOnAction(event -> {
                            Comment comment = getTableView().getItems().get(getIndex());
                            deleteComment(comment);
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(pane);
                        }
                    }
                };
            }
        };

        commentActionsColumn.setCellFactory(cellFactory);
    }

    private void loadComments() {
        try {
            // Clear the existing list
            commentsList.clear();

            // Using your method to get comments for this specific post
            List<Comment> comments = ((CommentService) commentService).getCommentsByPostId(currentPost.getId());

            // Add all comments to the observable list
            commentsList.addAll(comments);

            // Set the items to the table
            commentsTable.setItems(commentsList);
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

        try {
            if (commentBeingEdited != null) {
                // Update existing comment
                commentBeingEdited.setContent(commentContent);

                // Save the updated comment
                commentService.modifier(commentBeingEdited);

                // Reset the comment being edited
                commentBeingEdited = null;
                addCommentButton.setText("Ajouter un commentaire");
                addCommentButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
            } else {
                // Create a new comment
                Comment comment = new Comment();
                comment.setPost_id_id(currentPost.getId());
                comment.setContent(commentContent);
                // Set current user ID - you may need to get this from a session or user management system
                comment.setUser_commented_id(1); // Default user ID

                // Set current timestamp
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                comment.setCreated_at(now.format(formatter));

                // Save the comment
                commentService.ajouter(comment);
            }

            // Clear the input field
            newCommentTextArea.clear();

            // Refresh comments
            loadComments();

            showAlert(Alert.AlertType.INFORMATION, "Succès",
                    commentBeingEdited != null ? "Commentaire modifié avec succès." : "Commentaire ajouté avec succès.");

        } catch (SQLException e) {
            showError("Error with comment: " + e.getMessage());
        }
    }

    private void startEditingComment(Comment comment) {
        // Set the comment being edited
        commentBeingEdited = comment;

        // Set the comment content to the textarea
        newCommentTextArea.setText(comment.getContent());

        // Update the button text and style
        addCommentButton.setText("Mettre à jour le commentaire");
        addCommentButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white;");

        // Focus the textarea
        newCommentTextArea.requestFocus();
    }

    private void deleteComment(Comment comment) {
        // Confirm deletion
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer le commentaire");
        confirmDialog.setContentText("Voulez-vous vraiment supprimer ce commentaire ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete the comment
                commentService.supprimer(comment);

                // Refresh the comments table
                loadComments();

                // Show success message
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Commentaire supprimé avec succès !");

                // If the deleted comment was being edited, reset the edit state
                if (commentBeingEdited != null && commentBeingEdited.getId() == comment.getId()) {
                    commentBeingEdited = null;
                    newCommentTextArea.clear();
                    addCommentButton.setText("Ajouter un commentaire");
                    addCommentButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");
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