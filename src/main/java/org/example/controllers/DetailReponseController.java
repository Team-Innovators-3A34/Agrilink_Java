package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.example.entities.Reponses;

import java.time.format.DateTimeFormatter;

public class DetailReponseController {

    @FXML
    private Label idLabel;
    @FXML
    private Label contentLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label solutionLabel;
    @FXML
    private Button backButton;

    private DashboardController dashboardController;

    /**
     * Remplit l'interface avec les informations de la réponse.
     */
    public void setReponse(Reponses reponse) {
        contentLabel.setText(reponse.getContent());
        statusLabel.setText(reponse.getStatus());
        solutionLabel.setText(reponse.getSolution());
        if (reponse.getDate() != null) {
            dateLabel.setText(reponse.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            dateLabel.setText("Date : N/A");
        }
    }

    /**
     * Reçoit la référence du DashboardController pour pouvoir revenir à la vue principale.
     */
    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @FXML
    private void handleBack() {
        if (dashboardController != null) {
            dashboardController.restoreCenter();
        }
    }
}
