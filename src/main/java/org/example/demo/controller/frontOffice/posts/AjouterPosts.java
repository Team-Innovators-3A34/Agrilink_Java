package org.example.demo.controller.frontOffice.posts;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.demo.models.Posts;
import org.example.demo.services.posts.HuggingFaceImageService;
import org.example.demo.services.posts.HuggingFaceTipService;
import org.example.demo.services.posts.PostsService;
import org.example.demo.utils.sessionManager;
import org.example.demo.utils.ConfigUtil;

import javafx.scene.image.ImageView;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class AjouterPosts implements Initializable {

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

    @FXML
    private Button generateImageButton;

    @FXML
    private ImageView previewImageView;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private Button generateTip;

    private HuggingFaceTipService tipService;

    private File selectedImageFile;
    private String generatedImageFilename;
    private PostsService postsService = new PostsService();
    private HuggingFaceImageService imageService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Set up type dropdown options
        typeComboBox.getItems().addAll("question", "discussion", "article");
        typeComboBox.setValue("discussion"); // Set default value

        // Set up status dropdown options
        statusComboBox.getItems().addAll("active", "draft");
        statusComboBox.setValue("active"); // Set default value

        // Initialize image service
        imageService = new HuggingFaceImageService(ConfigUtil.getHuggingFaceApiKey());
        // Hide progress indicator initially
        if (progressIndicator != null) {
            progressIndicator.setVisible(false);
        }

        // Set up validation listeners
        titleField.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
        typeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateFields());
        descriptionField.textProperty().addListener((obs, oldVal, newVal) -> validateFields());
        statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateFields());

        tipService = new HuggingFaceTipService();
    }

    @FXML
    private void generateAgricultureTip() {
        progressIndicator.setVisible(true);

        CompletableFuture<String> future = tipService.generateAgricultureTip();
        future.thenAccept(tip -> {
            Platform.runLater(() -> {
                descriptionField.setText(tip);
                progressIndicator.setVisible(false);
            });
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                // Handle error
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Failed to generate agriculture tip");
                alert.setContentText("Please try again later: " + ex.getMessage());
                alert.showAndWait();
                progressIndicator.setVisible(false);
            });
            return null;
        });
    }

    @FXML
    private void generateImage(ActionEvent event) {
        String description = descriptionField.getText();
        if (description == null || description.trim().isEmpty()) {
            showAlert("Validation Error", "Veuillez entrer une description pour générer une image.");
            return;
        }

        // Show progress indicator
        if (progressIndicator != null) {
            progressIndicator.setVisible(true);
        }
        generateImageButton.setDisable(true);

        // Create task for background processing
        var task = imageService.generateImageFromDescriptionTask(description);

        task.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                try {
                    generatedImageFilename = task.getValue();
                    selectedImageFile = null; // Clear selected file since we're using generated image
                    imageField.setText("AI Generated: " + generatedImageFilename);

                    // Display the preview image
                    displayImagePreview(generatedImageFilename);

                    // Hide progress indicator
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    generateImageButton.setDisable(false);
                } catch (Exception ex) {
                    showAlert("Error", "Failed to load generated image: " + ex.getMessage());
                    if (progressIndicator != null) {
                        progressIndicator.setVisible(false);
                    }
                    generateImageButton.setDisable(false);
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                showAlert("Error", "Failed to generate image: " + task.getException().getMessage());
                if (progressIndicator != null) {
                    progressIndicator.setVisible(false);
                }
                generateImageButton.setDisable(false);
            });
        });

        // Start the task
        new Thread(task).start();
    }

    private void displayImagePreview(String filename) {
        try {
            String imagePath = "file:src/main/resources/images/posts/" + filename;
            Image image = new Image(imagePath);
            if (previewImageView != null) {
                previewImageView.setImage(image);
                previewImageView.setVisible(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
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
            String imageFileName = null;

            // Handle image file if selected
            if (selectedImageFile != null) {
                // Generate unique filename using timestamp
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                String extension = selectedImageFile.getName().substring(selectedImageFile.getName().lastIndexOf('.'));
                imageFileName = "R-1-" + timestamp + extension;

                // Define target directory (adjust path as needed)
                String uploadDir = "src/main/resources/images/posts/";
                Path targetPath = Paths.get(uploadDir + imageFileName);

                // Create directory if it doesn't exist
                Files.createDirectories(Paths.get(uploadDir));

                // Copy file to target location
                Files.copy(selectedImageFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Format image name as required by your model
                imageFileName = "[\"" + imageFileName + "\"]";
            }
            // Handle AI generated image
            else if (generatedImageFilename != null) {
                // Format image name as required by your model
                imageFileName = "[\"" + generatedImageFilename + "\"]";
            }

            // Create and save post
            Posts post = new Posts();
            post.setUser_id_id(sessionManager.getInstance().getUser().getId());
            post.setTitle(titleField.getText().trim());
            post.setType(typeComboBox.getValue());
            post.setDescription(descriptionField.getText().trim());
            post.setStatus(statusComboBox.getValue());
            post.setImages(imageFileName);

            postsService.ajouter(post);

            showAlert("Success", "Publication ajoutée avec succès!");
            clearFields();

        } catch (Exception e) {
            showAlert("Error", "Une erreur est survenue: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearFields() {
        titleField.clear();
        typeComboBox.setValue(null);
        descriptionField.clear();
        statusComboBox.setValue(null);
        imageField.clear();
        selectedImageFile = null;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}