package org.example.demo.controller.frontOffice.RecyclingChat;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.example.demo.services.recyclingpoint.GPTService;

public class Chat {

    @FXML
    private VBox chatBox;

    @FXML
    private TextField userInput;

    @FXML
    private void sendMessage() {
        String msg = userInput.getText().trim();
        if (msg.isEmpty()) {
            return;
        }

        // Affiche le message utilisateur
        addMessage("🧍‍♂️ Vous : " + msg, false);
        userInput.clear();

        // Appel asynchrone à GPT
        new Thread(() -> {
            try {
                String reply = GPTService.askGPT(msg);
                Platform.runLater(() -> addMessage("♻️ EcoBot : " + reply, true));
            } catch (Exception e) {
                Platform.runLater(() ->
                        addMessage("❌ EcoBot erreur : " + e.getMessage(), true)
                );
            }
        }).start();
    }

    private void addMessage(String text, boolean fromBot) {
        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setMaxWidth(400);

        if (fromBot) {
            lbl.setStyle("-fx-background-color:#E0F7FA; -fx-padding:8; -fx-background-radius:5;");
        } else {
            lbl.setStyle("-fx-background-color:#FFFFFF; -fx-padding:8; -fx-background-radius:5;");
        }

        chatBox.getChildren().add(lbl);
        // Assure que le VBox se réactualise et affiche le dernier message
        chatBox.layout();
    }
}
