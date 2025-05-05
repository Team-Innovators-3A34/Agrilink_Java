package org.example.demo.controller.backOffice.event;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import org.example.demo.HelloApplication;
import org.example.demo.services.event.catService;
import org.example.demo.models.categorie;

import java.net.URL;

public class ListCategoryController {

    @FXML private TableView<categorie> categorieTable;
    @FXML private TableColumn<categorie, String> nameColumn;
    @FXML private TableColumn<categorie, Void> actionsColumn;
    @FXML private TextField searchField;

    private final catService service = new catService();
    private final ObservableList<categorie> catList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));

        // Charger les catégories
        catList.setAll(service.rechercher());

        // Liste filtrée
        FilteredList<categorie> filteredData = new FilteredList<>(catList, p -> true);

        // Recherche dynamique
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(cat -> {
                if (newValue == null || newValue.isEmpty()) return true;
                return cat.getNom().toLowerCase().contains(newValue.toLowerCase());
            });
        });

        categorieTable.setItems(filteredData);

        // Boutons d'action
        actionsColumn.setCellFactory(column -> new TableCell<>() {
            private HBox createIconButton(String imagePath, Runnable action, String color) {
                AnchorPane buttonContainer = new AnchorPane();
                buttonContainer.setMinSize(38, 30);
                buttonContainer.setPrefSize(38, 30);
                buttonContainer.setMaxSize(38, 30);
                buttonContainer.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15px;");
                buttonContainer.setCursor(Cursor.HAND);

                URL resource = getClass().getResource(imagePath);
                if (resource == null) return new HBox();

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
                    categorie cat = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();

                    buttonsBox.getChildren().addAll(
                            createIconButton("/icons/pencil (1).png", () -> editCategory(cat), "#fe9341"),
                            createIconButton("/icons/trash.png", () -> deleteCategory(cat), "#0055ff")
                    );
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void editCategory(categorie cat) {
        TextInputDialog dialog = new TextInputDialog(cat.getNom());
        dialog.setTitle("Modifier la catégorie");
        dialog.setHeaderText("Modification de la catégorie");
        dialog.setContentText("Nouveau nom :");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty()) {
                cat.setNom(newName.trim());
                service.modifier(cat);
                catList.setAll(service.rechercher()); // met à jour la liste sans perdre le filtre actif
            }
        });
    }

    private void deleteCategory(categorie cat) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Suppression");
        alert.setHeaderText("Supprimer la catégorie \"" + cat.getNom() + "\" ?");
        alert.setContentText("Êtes-vous sûr ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.supprimer(cat);
                catList.setAll(service.rechercher()); // recharge la liste après suppression
            }
        });
    }

    public void categoryAdd() {
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/event/addCategorie.fxml");
    }
}
