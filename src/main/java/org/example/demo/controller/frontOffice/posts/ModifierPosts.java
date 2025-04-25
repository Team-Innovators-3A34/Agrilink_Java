package org.example.demo.controller.frontOffice.posts;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.demo.models.Posts;
import org.example.demo.services.posts.PostsService;
import org.example.demo.utils.sessionManager;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ModifierPosts implements Initializable {

    @FXML
    private TextField titleField;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private TextArea descriptionField;

    @FXML
    private ComboBox<String> statusComboBox;

    @FXML
    private TextField imageField;

    @FXML
    private Label imageErrorLabel;

    private File selectedImageFile;
    private PostsService postsService = new PostsService();
    private Posts currentPost;
    private String originalImageName;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up type dropdown options
        typeComboBox.getItems().addAll("question", "discussion", "article");

        // Set up status dropdown options
        statusComboBox.getItems().addAll("active", "draft");

        // Add tooltips for better user experience
        Tooltip typeTooltip = new Tooltip("Choose post type: question, discussion, or article");
        typeComboBox.setTooltip(typeTooltip);

        Tooltip statusTooltip = new Tooltip("Choose post status: active or draft");
        statusComboBox.setTooltip(statusTooltip);
        // Set up validation listeners
        titleField.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateFields());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
        statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateFields());
    }

    public void setPost(Posts post) {
        this.currentPost = post;
        titleField.setText(post.getTitle());
        typeComboBox.setValue(post.getType());
        descriptionField.setText(post.getDescription());
        statusComboBox.setValue(post.getStatus());

        // Handle image field
        if (post.getImages() != null && !post.getImages().isEmpty()) {
            // Extract image name from format ["image-name.jpg"]
            String imageStr = post.getImages();
            originalImageName = imageStr.substring(imageStr.indexOf("\"") + 1, imageStr.lastIndexOf("\""));
            imageField.setText(originalImageName);
        }
    }

    @FXML
    private void browseImage(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image File");

        // Set file extension filters
        FileChooser.ExtensionFilter imageFilter =
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png");
        fileChooser.getExtensionFilters().add(imageFilter);

        // Show open file dialog
        Stage stage = (Stage) titleField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            selectedImageFile = file;
            imageField.setText(file.getName());
            validateImageFile(file);
        }
    }

    private void validateImageFile(File file) {
        String fileName = file.getName().toLowerCase();

        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg") && !fileName.endsWith(".png")) {
            imageErrorLabel.setText("Seuls les formats JPG et PNG sont acceptés");
            imageErrorLabel.setVisible(true);
            return;
        }

        if (file.length() > 5 * 1024 * 1024) { // 5MB limit
            imageErrorLabel.setText("L'image ne doit pas dépasser 5MB");
            imageErrorLabel.setVisible(true);
            return;
        }

        // Clear error if validation passes
        imageErrorLabel.setVisible(false);
    }

    private boolean validateFields() {
        boolean isValid = true;

        // Validate title
        if (titleField.getText().trim().isEmpty()) {
            isValid = false;
        }

        // Validate type
        if (typeComboBox.getValue() == null) {
            isValid = false;
        }

        // Validate description
        if (descriptionField.getText().trim().isEmpty()) {
            isValid = false;
        }

        // Validate status
        if (statusComboBox.getValue() == null) {
            isValid = false;
        }

        return isValid;
    }

    @FXML
    private void save(ActionEvent event) {
        if (!validateFields()) {
            showAlert("Validation Error", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        try {
            String imageFileName = currentPost.getImages(); // Keep original by default

            // Handle image file if a new one was selected
            if (selectedImageFile != null) {
                // Generate unique filename using timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String extension = selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf('.'));
                String newImageName = "R-1-" + timestamp + extension;

                // Define target directory (adjust path as needed)
                String uploadDir = "src/main/resources/images/posts/";
                Path targetPath = Paths.get(uploadDir + newImageName);

                // Create directory if it doesn't exist
                Files.createDirectories(Paths.get(uploadDir));

                // Copy file to target location
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Format image name as required by your model
                imageFileName = "[\"" + newImageName + "\"]";
            }

            // Update post
            currentPost.setUser_id_id(sessionManager.getInstance().getUser().getId());
            currentPost.setTitle(titleField.getText().trim());
            currentPost.setType(typeComboBox.getValue());
            currentPost.setDescription(descriptionField.getText().trim());
            currentPost.setStatus(statusComboBox.getValue());
            currentPost.setImages(imageFileName);

            postsService.modifier(currentPost);

            showAlert("Success", "Publication mise à jour avec succès!");

        } catch (Exception e) {
            showAlert("Error", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}