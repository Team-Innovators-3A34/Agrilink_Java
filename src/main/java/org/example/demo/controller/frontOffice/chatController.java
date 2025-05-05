package org.example.demo.controller.frontOffice;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.demo.services.event.ChatBotService;

public class chatController {

    @FXML private VBox chatBox;
    @FXML private TextField userInput;
    @FXML private ScrollPane scrollPane;

    @FXML
    public void onSendMessage() {
        String userText = userInput.getText().trim();
        if (userText.isEmpty()) return;

        addMessage("🧑 " + userText, "#DCF8C6", Pos.CENTER_RIGHT);
        userInput.clear();

        new Thread(() -> {
            try {
                String botReply = ChatBotService.askGPT(userText);
                Platform.runLater(() ->
                        addMessage("🤖 " + botReply, "#F1F0F0", Pos.CENTER_LEFT)
                );
            } catch (Exception e) {
                Platform.runLater(() ->
                        addMessage("⚠️ Erreur lors de la réponse du bot.", "#FFCCCC", Pos.CENTER_LEFT)
                );
            }
        }).start();
    }

    private void addMessage(String text, String backgroundColor, Pos alignment) {
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(240);
        messageLabel.setStyle("-fx-background-color: " + backgroundColor + ";"
                + "-fx-padding: 10 14;"
                + "-fx-background-radius: 12;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 4, 0, 0, 2);"
                + "-fx-font-size: 13px;");

        HBox container = new HBox(messageLabel);
        container.setPadding(new Insets(4, 0, 4, 0));
        container.setAlignment(alignment);
        container.setFillHeight(true);

        chatBox.getChildren().add(container);

        // Scroll to bottom smoothly
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }
}
