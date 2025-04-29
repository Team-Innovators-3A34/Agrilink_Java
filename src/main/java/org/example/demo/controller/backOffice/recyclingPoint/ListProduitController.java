package org.example.demo.controller.backOffice.recyclingPoint;

import javafx.application.Platform;
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
import javafx.beans.property.ReadOnlyStringWrapper;
import org.example.demo.HelloApplication;
import org.example.demo.models.produit;
import org.example.demo.models.recyclingpoint;
import org.example.demo.services.recyclingpoint.produitService;
import org.example.demo.utils.sessionManager;

import java.io.IOException;
import java.net.URL;

public class ListProduitController {

    @FXML
    private TableColumn<produit, Void> actions;

    @FXML
    private TableColumn<produit, String> pointname;

    @FXML
    private TableColumn<produit, String> productname;

    @FXML
    private TableView<produit> producttable;

    @FXML
    private TextField searchField;

    private final produitService service = new produitService();

    private ObservableList<produit> produitsList;

    @FXML
    public void initialize() {

        productname.setCellValueFactory(new PropertyValueFactory<>("nom"));

        // Affichage du nom du point de recyclage à partir de l'objet recyclingpoint
        pointname.setCellValueFactory(cellData -> {
            recyclingpoint rp = cellData.getValue().getRecyclingpoint();
            return new ReadOnlyStringWrapper(rp != null ? rp.getNom() : "");
        });

        produitsList = FXCollections.observableArrayList(service.rechercher());
        producttable.setItems(produitsList);

        ajouterBoutonsActions();
    }

    private void ajouterBoutonsActions() {
        actions.setCellFactory(column -> new TableCell<>() {

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
                    produit p = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();

                    buttonsBox.getChildren().addAll(
                            createIconButton("/icons/pencil (1).png", "Modifier", () -> {
                                System.out.println("🛠️ Modifier produit : " + p.getImage());
                                try {
                                    HelloApplication.changeSceneWithController(
                                            "/org/example/demo/fxml/Backoffice/recyclingPoint/addProduit.fxml",
                                            controller -> {
                                                // Passe le produit au contrôleur de modification ici
                                                if (controller instanceof addproduit) {
                                                    ((addproduit) controller).setProduitPourModification(p);
                                                }
                                            }
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }, "#fe9341"),

                            createIconButton("/icons/trash.png", "Supprimer", () -> {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Suppression");
                                alert.setHeaderText("Supprimer le produit \"" + p.getNom() + "\" ?");
                                alert.setContentText("Êtes-vous sûr ?");
                                alert.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.OK) {
                                        service.supprimer(p);
                                        producttable.getItems().remove(p);
                                        HelloApplication.succes("✅ Produit supprimé !");
                                    }
                                });
                            }, "#0055ff")
                    );

                    setGraphic(buttonsBox);
                }
            }
        });
    }

    public void addproduct(){
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/addproduit.fxml");
    }
}
