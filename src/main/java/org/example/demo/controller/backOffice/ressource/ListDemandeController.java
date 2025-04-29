package org.example.demo.controller.backOffice.ressource;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.example.demo.models.Demandes;
import org.example.demo.services.ressource.DemandesService;

import java.net.URL;

public class ListDemandeController {

    @FXML
    private TableView<Demandes> demandeTable;

    @FXML
    private TableColumn<Demandes, String> nomDemandeurColumn;

    @FXML
    private TableColumn<Demandes, String> nomProprietaireColumn;

    @FXML
    private TableColumn<Demandes, String> statusColumn;

    @FXML
    private TableColumn<Demandes, Void> actionsColumn;

    @FXML
    private TableColumn<Demandes, String> detailColumn;

    private DemandesService demandesService = new DemandesService();
    private ObservableList<Demandes> demandeList;

    @FXML
    public void initialize() {
        nomDemandeurColumn.setCellValueFactory(new PropertyValueFactory<>("nomDemandeur"));
        nomProprietaireColumn.setCellValueFactory(new PropertyValueFactory<>("nomOwner"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        detailColumn.setCellValueFactory(new PropertyValueFactory<>("message"));

        demandeList = FXCollections.observableArrayList(demandesService.rechercher());
        demandeTable.setItems(demandeList);

        addDeleteButtonToTable();
    }


    private void addDeleteButtonToTable() {
        actionsColumn.setCellFactory(column -> new TableCell<Demandes, Void>() {

            private HBox createIconButton(String imagePath, String tooltipText, Runnable action) {
                AnchorPane buttonContainer = new AnchorPane();
                buttonContainer.setMinSize(38, 30);
                buttonContainer.setPrefSize(38, 30);
                buttonContainer.setMaxSize(38, 30);
                buttonContainer.setStyle("-fx-background-color: #ff4d4d; -fx-background-radius: 15px;");
                buttonContainer.setCursor(Cursor.HAND);

                URL resource = getClass().getResource(imagePath);
                if (resource == null) {
                    System.err.println("❌ Image not found: " + imagePath);
                    return new HBox(); // Prevent null crash
                }

                ImageView icon = new ImageView(resource.toExternalForm());
                icon.setFitHeight(16);
                icon.setFitWidth(16);
                icon.setLayoutX(12);
                icon.setLayoutY(5);
                icon.setPreserveRatio(true);

                buttonContainer.getChildren().add(icon);
                buttonContainer.setOnMouseClicked(e -> action.run());

                HBox wrapper = new HBox(buttonContainer);
                wrapper.setAlignment(Pos.CENTER);
                return wrapper;
            }

            private final HBox buttonsBox = new HBox();

            {
                buttonsBox.setAlignment(Pos.CENTER);
                buttonsBox.setPadding(new Insets(5));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Demandes demande = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();

                    buttonsBox.getChildren().add(
                            createIconButton("/icons/trash.png", "Supprimer", () -> {
                                System.out.println("🗑️ Deleting: " + demande.getNomDemandeur());
                                demandesService.supprimer(demande); // Replace with your logic
                                getTableView().getItems().remove(demande);
                                getTableView().refresh();
                            })
                    );

                    setGraphic(buttonsBox);
                }
            }
        });
    }

}
