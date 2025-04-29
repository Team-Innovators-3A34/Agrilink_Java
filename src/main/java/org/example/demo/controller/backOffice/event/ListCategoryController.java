package org.example.demo.controller.backOffice.event;

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
import org.example.demo.HelloApplication;
import org.example.demo.services.event.catService;
import org.example.demo.models.categorie;
import javafx.scene.layout.HBox;

import java.net.URL;

public class ListCategoryController {

    @FXML
    private TableView<categorie> categorieTable; // Correspond au fx:id dans le FXML

    @FXML
    private TableColumn<categorie, String> nameColumn; // Colonne pour le nom

    @FXML
    private TableColumn<categorie, Void> actionsColumn; // Colonne pour les actions

    @FXML
    private TextField searchField; // Champ de recherche



    private catService service = new catService();
    private ObservableList<categorie> catList = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Configuration de la colonne Name
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));

        // Configuration de la colonne Actions avec boutons
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
                    return new HBox(); // évite un crash
                }

                ImageView icon = new ImageView(resource.toExternalForm());
                icon.setFitHeight(16);
                icon.setFitWidth(14);
                icon.setPreserveRatio(true);

                // Centrer l'icône dans le bouton
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
                            createIconButton("/icons/pencil (1).png", "Modifier", () -> {
                                System.out.println("✏️ Modifier : " + cat);
                                editCategory(cat);
                            }, "#fe9341"),

                            createIconButton("/icons/trash.png", "Supprimer", () -> {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Suppression");
                                alert.setHeaderText("Supprimer la catégorie \"" + cat.getNom() + "\" ?");
                                alert.setContentText("Êtes-vous sûr ?");
                                alert.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.OK) {
                                        deleteCategory(cat);
                                    }
                                });
                            }, "#0055ff")
                    );

                    setGraphic(buttonsBox);
                }
            }
        });

        // Chargement des données
        loadCategories();
    }

    private void loadCategories() {
        catList.clear();
        catList.addAll(service.rechercher());
        categorieTable.setItems(catList);
    }

    private void editCategory(categorie cat) {
        TextInputDialog dialog = new TextInputDialog(cat.getNom());
        dialog.setTitle("Modifier la catégorie");
        dialog.setHeaderText("Modification de la catégorie");
        dialog.setContentText("Nouveau nom:");

        dialog.showAndWait().ifPresent(newName -> {
            cat.setNom(newName);
            service.modifier(cat);
            loadCategories(); // Rafraîchir la table
        });
    }

    private void deleteCategory(categorie cat) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer la catégorie " + cat.getNom() + "?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer cette catégorie?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                service.supprimer(cat);
                loadCategories(); // Rafraîchir la table
            }
        });
    }

    public void categoryAdd(){
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/event/addCategorie.fxml");

    }
}
