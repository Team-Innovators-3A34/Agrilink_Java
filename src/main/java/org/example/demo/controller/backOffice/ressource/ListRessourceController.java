package org.example.demo.controller.backOffice.ressource;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.demo.HelloApplication;
import org.example.demo.models.Ressources;
import org.example.demo.services.ressource.RessourcesService;
import org.example.demo.utils.sessionManager;

import java.net.URL;

public class ListRessourceController {

    @FXML
    private TableColumn<Ressources, Void> actionsColumn;

    @FXML
    private TableColumn<Ressources, Void> detailColumn;

    @FXML
    private TableColumn<Ressources, String> imageColumn;

    @FXML
    private TableColumn<Ressources, String> nomRessourceColumn;

    @FXML
    private TableView<Ressources> ressourcesTable;

    @FXML
    private TableColumn<Ressources, String> satutsColumn;

    @FXML
    private TableColumn<Ressources, String> typeColumn;

    private final RessourcesService ressourcesService = new RessourcesService();
    private final ObservableList<Ressources> ressources = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Charger les ressources depuis le service
        ressources.addAll(ressourcesService.rechercher());

        // Associer les colonnes aux attributs
        nomRessourceColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        satutsColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        detailColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Colonne "Supprimer"
        actionsColumn.setCellFactory(getDeleteCellFactory());

        ressourcesTable.setItems(ressources);
    }

    private Callback<TableColumn<Ressources, Void>, TableCell<Ressources, Void>> getDeleteCellFactory() {
        return column -> new TableCell<>() {

            private HBox createIconButton(String imagePath, String tooltipText, Runnable action) {
                AnchorPane buttonContainer = new AnchorPane();
                buttonContainer.setMinSize(38, 30);
                buttonContainer.setPrefSize(38, 30);
                buttonContainer.setMaxSize(38, 30);
                buttonContainer.setStyle("-fx-background-color: #ff4d4d; -fx-background-radius: 15px;");
                buttonContainer.setCursor(javafx.scene.Cursor.HAND);

                URL resource = getClass().getResource(imagePath);
                if (resource == null) {
                    System.err.println("❌ Image not found: " + imagePath);
                    return new HBox(); // sécurité
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
                buttonsBox.setPadding(new javafx.geometry.Insets(5));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Ressources ressource = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();

                    buttonsBox.getChildren().add(
                            createIconButton("/icons/trash.png", "Supprimer", () -> {
                                System.out.println("🗑️ Deleting: " + ressource.getName());
                                ressourcesService.supprimer(ressource);
                                getTableView().getItems().remove(ressource);
                                getTableView().refresh();
                            })
                    );

                    setGraphic(buttonsBox);
                }
            }
        };
    }



    public void showDemande() {
        if (sessionManager.getInstance().isAdmin() && sessionManager.getInstance().isSessionActive()) {
            HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/ressource/listDemande.fxml");
        }
    }
}
