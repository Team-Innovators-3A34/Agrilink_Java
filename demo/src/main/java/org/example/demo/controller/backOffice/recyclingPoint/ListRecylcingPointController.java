package org.example.demo.controller.backOffice.recyclingPoint;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.ressource.addDemandes;
import org.example.demo.models.User;
import org.example.demo.models.event;
import org.example.demo.models.recyclingpoint;
import org.example.demo.services.recyclingpoint.recyclingpointService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;

public class ListRecylcingPointController {

    private User user;
    private recyclingpointService recyclingPointService;

    @FXML
    private TableView<recyclingpoint> pointTable;

    @FXML
    private TableColumn<recyclingpoint, String> categoryColumn;

    @FXML
    private TableColumn<recyclingpoint, String> pointColumn;

    @FXML
    private TableColumn<recyclingpoint, LocalDate> dateColumn;

    @FXML
    private TableColumn<recyclingpoint, Void> actionsColumn;

    private static recyclingpoint selectedRecyclingpoint;


    public static void setSelectedRecylingPoint(recyclingpoint rc) {
        selectedRecyclingpoint = rc;
    }

    public static recyclingpoint getSelectedRecylingPoint() {
        return selectedRecyclingpoint;
    }



    @FXML
    void initialize() {
        recyclingPointService = new recyclingpointService();

        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        pointColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        List<recyclingpoint> points = recyclingPointService.rechercher();
        pointTable.getItems().setAll(points);

        addActionButtonsToTable();
    }

    private void addActionButtonsToTable() {
        actionsColumn.setCellFactory(column -> new TableCell<>() {

            private HBox createIconButton(String imagePath, String tooltipText, Runnable action, String color) {
                AnchorPane buttonContainer = new AnchorPane();
                buttonContainer.setMinSize(38, 30);
                buttonContainer.setPrefSize(38, 30);
                buttonContainer.setMaxSize(38, 30);
                buttonContainer.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15px;");
                buttonContainer.setCursor(Cursor.HAND);

                URL resource = getClass().getResource(imagePath);
                if (resource == null) {
                    System.err.println("❌ Image not found: " + imagePath);
                    return new HBox(); // Évite le crash
                }

                ImageView icon = new ImageView(resource.toExternalForm());
                icon.setFitHeight(16);
                icon.setFitWidth(14);
                icon.setPreserveRatio(true);

                AnchorPane.setTopAnchor(icon, 9.0);
                AnchorPane.setLeftAnchor(icon, 12.5);

                buttonContainer.getChildren().add(icon);
                buttonContainer.setOnMouseClicked(e -> action.run());

                HBox wrapper = new HBox(buttonContainer);
                wrapper.setAlignment(Pos.CENTER);
                return wrapper;
            }

            private final HBox buttonsBox = new HBox(10);

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
                    recyclingpoint rp = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();

                    buttonsBox.getChildren().addAll(
                            createIconButton("/icons/pencil (1).png", "Modifier", () -> {
                                System.out.println("➡️ Modifier : " + rp.getNom());
                                try {
                                    HelloApplication.changeSceneWithController(
                                            "/org/example/demo/fxml/Backoffice/recyclingPoint/addRecyclingpoint.fxml",
                                            controller -> {
                                                if (controller instanceof addRecyclingpoint)
                                                    ((addRecyclingpoint) controller).setRecyclingPointPourModification(rp);
                                            }
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }                            }, "#fe9341"),

                            createIconButton("/icons/trash.png", "Supprimer", () -> {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Suppression");
                                alert.setHeaderText("Supprimer le point \"" + rp.getNom() + "\" ?");
                                alert.setContentText("Êtes-vous sûr ?");
                                alert.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.OK) {
                                        recyclingPointService.supprimer(rp);
                                        pointTable.getItems().remove(rp);
                                    }
                                });
                            }, "#0055ff")
                    );

                    setGraphic(buttonsBox);
                }
            }
        });
    }


    public void addpoint(){
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/addRecyclingPoint.fxml");
    }

}
