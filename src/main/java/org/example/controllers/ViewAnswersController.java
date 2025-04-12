package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.entities.Reponses;
import org.example.entities.Reclamation;
import org.example.services.ReponsesService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewAnswersController implements Initializable {

    @FXML
    private Label reclamationTitleLabel;

    @FXML
    private TableView<Reponses> answersTableView;

    @FXML
    private TableColumn<Reponses, Integer> idColumn;

    @FXML
    private TableColumn<Reponses, String> contentColumn;

    @FXML
    private TableColumn<Reponses, String> statusColumn;

    @FXML
    private TableColumn<Reponses, String> dateColumn;

    @FXML
    private TableColumn<Reponses, String> solutionColumn;

    @FXML
    private TableColumn<Reponses, Void> actionColumn;

    private DashboardController dashboardController;
    private Reclamation reclamation;
    private ReponsesService reponsesService = new ReponsesService();
    private ObservableList<Reponses> answersData = FXCollections.observableArrayList();

    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        reclamationTitleLabel.setText("Réponses pour : " + reclamation.getTitle());
        loadAnswers();
    }

    public void setDashboardController(DashboardController dashboardController) {
        this.dashboardController = dashboardController;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        contentColumn.setCellValueFactory(new PropertyValueFactory<>("content"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        solutionColumn.setCellValueFactory(new PropertyValueFactory<>("solution"));

        addActionButtons();

        answersTableView.setItems(answersData);
    }

    public void loadAnswers() {
        if (reclamation != null) {
            List<Reponses> list = reponsesService.getAnswersByReclamation(reclamation.getId());
            answersData.setAll(list);
            System.out.println("Nombre de réponses chargées : " + list.size());
        }
    }

    private void addActionButtons() {
        actionColumn.setCellFactory(new Callback<TableColumn<Reponses, Void>, TableCell<Reponses, Void>>() {
            @Override
            public TableCell<Reponses, Void> call(final TableColumn<Reponses, Void> param) {
                return new TableCell<Reponses, Void>() {
                    private final Button detailButton = new Button("Détails");
                    private final Button editButton = new Button("Modifier");
                    private final Button deleteButton = new Button("Supprimer");

                    {
                        // Action pour le bouton "Détails"
                        detailButton.setOnAction(event -> {
                            Reponses reponse = getTableView().getItems().get(getIndex());
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/detailReponse.fxml"));
                                Parent detailRoot = loader.load();

                                DetailReponseController detailController = loader.getController();
                                detailController.setReponse(reponse);
                                detailController.setDashboardController(dashboardController);

                                // Affiche la vue détail dans la zone centrale du BorderPane
                                dashboardController.getRoot().setCenter(detailRoot);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        // Action pour le bouton "Modifier"
                        editButton.setOnAction(event -> {
                            Reponses reponse = getTableView().getItems().get(getIndex());
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/editReponse.fxml"));
                                Parent editRoot = loader.load();

                                EditReponseController editController = loader.getController();
                                editController.setReponse(reponse);
                                editController.setDashboardController(dashboardController);
                                editController.setViewAnswersController(ViewAnswersController.this); // Rafraîchir après modification

                                // Affiche l'interface d'édition
                                dashboardController.getRoot().setCenter(editRoot);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        // Action pour le bouton "Supprimer"
                        deleteButton.setOnAction(event -> {
                            Reponses reponse = getTableView().getItems().get(getIndex());

                            // Demande de confirmation avant suppression
                            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                            alert.setTitle("Confirmation de suppression");
                            alert.setHeaderText("Supprimer la réponse ?");
                            alert.setContentText("Voulez-vous vraiment supprimer cette réponse ?");

                            alert.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    reponsesService.supprimerReponse(reponse.getId());
                                    loadAnswers(); // Recharger la liste après suppression
                                }
                            });
                        });
                    }

                    @Override
                    public void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox container = new HBox(10.0, detailButton, editButton, deleteButton);
                            setGraphic(container);
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void handleBack() {
        if (dashboardController != null) {
            dashboardController.restoreCenter();
        }
    }
}
