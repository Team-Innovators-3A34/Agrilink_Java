package org.example.demo.controller.frontOffice.recycling;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.example.demo.services.recyclingpoint.ImageRecognitionService;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;

public class Recycling {
    @FXML private Button uploadBtn;
    @FXML private Button cameraBtn;
    @FXML private ImageView imageView;
    @FXML private TextArea descriptionArea;
    @FXML private Label recyclableLabel;

    private final ImageRecognitionService imgService = new ImageRecognitionService();

    @FXML
    public void onUpload() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(uploadBtn.getScene().getWindow());
        if (file == null) return;
        imageView.setImage(new Image(file.toURI().toString()));
        analyze(file);
    }

    @FXML
    public void onOpenCamera() {
        new Thread(() -> {
            try {
                Webcam webcam = Webcam.getDefault();
                if (webcam == null) {
                    showError("No webcam detected.");
                    return;
                }
                webcam.setViewSize(new Dimension(640,480));
                webcam.open();
                WebcamPanel panel = new WebcamPanel(webcam);
                panel.setPreferredSize(new Dimension(640,480));
                JButton capBtn = new JButton("Capture Photo");
                JFrame window = new JFrame("Camera Preview");
                window.setLayout(new BorderLayout());
                window.add(panel, BorderLayout.CENTER);
                window.add(capBtn, BorderLayout.SOUTH);
                window.pack();
                window.setVisible(true);

                capBtn.addActionListener(e -> {
                    BufferedImage img = webcam.getImage();
                    try {
                        File tmp = File.createTempFile("cap-", ".jpg");
                        ImageIO.write(img, "JPG", tmp);
                        webcam.close();
                        window.dispose();
                        Platform.runLater(() -> {
                            imageView.setImage(new Image(tmp.toURI().toString()));
                            analyze(tmp);
                        });
                    } catch (Exception ex) {
                        showError("Failed to capture image.");
                    }
                });

            } catch (Exception e) {
                showError("Camera error: " + e.getMessage());
            }
        }).start();
    }

    private void analyze(File file) {
        descriptionArea.setText("Analyzing image...");
        recyclableLabel.setText("Recyclability: Processing...");
        new Thread(() -> {
            try {
                String desc = imgService.analyze(file);
                boolean rec = imgService.isRecyclable(desc);
                Platform.runLater(() -> {
                    descriptionArea.setText(desc);
                    recyclableLabel.setText("Recyclability: " + (rec ? "♻️ Recyclable" : "🚯 Not recyclable"));
                });
            } catch (Exception e) {
                showError(e.getMessage());
            }
        }).start();
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, msg);
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }
}
