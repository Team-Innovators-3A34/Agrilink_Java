package org.example.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.example.entities.Reponses;
import org.example.services.ReponsesService;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EditReponseController {

    @FXML
    private Label titleLabel;

    @FXML
    private TextArea contentTextArea;

    @FXML
    private TextField solutionTextField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private CheckBox isAutoCheckBox;

    private Reponses reponse;
    private DashboardController dashboardController;
    private ViewAnswersController viewAnswersController;
    private ReponsesService reponsesService = new ReponsesService();

    public void setReponse(Reponses reponse) {
        this.reponse = reponse;
        titleLabel.setText("Modifier la réponse #" + reponse.getId());
        contentTextArea.setText(reponse.getContent());
        solutionTextField.setText(reponse.getSolution());
        // Assurez-vous que reponse.getDate() n'est pas null avant d'appeler toLocalDate()
        if(reponse.getDate() != null){
            datePicker.setValue(reponse.getDate().toLocalDate());
        }
        // Si isAuto n'est pas null, le définir dans le checkbox
        if(reponse.getIsAuto() != null){
            isAutoCheckBox.setSelected(reponse.getIsAuto());
        }
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    public void setViewAnswersController(ViewAnswersController viewAnswersController) {
        this.viewAnswersController = viewAnswersController;
    }

    @FXML
    private void handleEdit() {
        if (reponse != null) {
            // Set the content and solution from the input fields
            reponse.setContent(contentTextArea.getText());
            reponse.setSolution(solutionTextField.getText());

            // Set the date from the date picker, defaulting to now if not set
            reponse.setDate(datePicker.getValue() != null ? datePicker.getValue().atStartOfDay() : LocalDateTime.now());

            // Set the isAuto property based on the checkbox state
            reponse.setIsAuto(isAutoCheckBox.isSelected());

            // Attempt to update the response in the database
            boolean updated = reponsesService.modifierReponse(reponse); // Check if the update was successful

            // Handle the result of the update
            if (updated) {
                // Refresh the list of answers if the update was successful
                if (viewAnswersController != null) {
                    viewAnswersController.loadAnswers();
                }

                // Return to the list of answers
                dashboardController.restoreCenter();
            } else {
                System.out.println("Échec dde la modification de la réponse.");
            }
        }
        dashboardController.restoreCenter();
    }

    @FXML
    private void handleCancel() {
        dashboardController.restoreCenter();
    }

    @FXML
    private void toggleContentField() {
        boolean isAuto = isAutoCheckBox.isSelected();
        contentTextArea.setDisable(isAuto);
    }
}
