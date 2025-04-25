package org.example.demo.controller.backOffice.claim;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.demo.models.TypeRec;
import org.example.demo.services.claim.TypeRecService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListCategoryController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private TableView<TypeRec> categorieTable;

    @FXML
    private TableColumn<TypeRec, String> nameColumn;

    @FXML
    private TableColumn<TypeRec, String> descriptionColumn;

    @FXML
    private TableColumn<TypeRec, TypeRec> actionsColumn;

    private final TypeRecService typeRecService = new TypeRecService();
    private final ObservableList<TypeRec> categoriesData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        setupTableView();
    }

    private void loadCategories() {
        List<TypeRec> list = typeRecService.getAllCategories();
        categoriesData.setAll(list);
        categorieTable.setItems(categoriesData);
    }

    private void setupTableView() {
        nameColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));

        actionsColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionsColumn.setCellFactory(new Callback<TableColumn<TypeRec, TypeRec>, TableCell<TypeRec, TypeRec>>() {
            @Override
            public TableCell<TypeRec, TypeRec> call(TableColumn<TypeRec, TypeRec> param) {
                return new TableCell<>() {
                    private final Button modifyBtn = makeBtn("pencil.png", "Modifier", "bg-success");
                    private final Button deleteBtn = makeBtn("trash.png", "Supprimer", "bg-warning");
                    private final HBox container = new HBox(5);

                    {
                        container.setAlignment(Pos.CENTER);
                        container.setPadding(new Insets(5));
                        container.getChildren().addAll(modifyBtn, deleteBtn);

                        modifyBtn.setOnAction(event -> {
                            TypeRec typeRec = getTableView().getItems().get(getIndex());
                            openModifyCategoryPage(typeRec);
                        });

                        deleteBtn.setOnAction(event -> {
                            TypeRec typeRec = getTableView().getItems().get(getIndex());
                            boolean deleted = typeRecService.supprimerTypeRec(typeRec.getId());
                            if (deleted) {
                                categoriesData.remove(typeRec);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(TypeRec item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            setGraphic(container);
                        }
                    }
                };
            }
        });
    }

    /**
     * Méthode utilitaire pour créer un bouton avec icône, style et tooltip.
     */
    private Button makeBtn(String iconFile, String tooltipText, String styleClass) {
        ImageView iv = new ImageView(loadIcon(iconFile));
        iv.setFitWidth(14);
        iv.setFitHeight(14);
        Button btn = new Button();
        btn.setGraphic(iv);
        btn.setTooltip(new Tooltip(tooltipText));
        btn.getStyleClass().addAll("badge", styleClass);
        btn.setPrefSize(20, 20);
        btn.setMinSize(20, 20);
        btn.setMaxSize(20, 20);
        return btn;
    }

    /**
     * Charge une icône depuis le dossier /icons des ressources.
     */
    private Image loadIcon(String name) {
        InputStream is = getClass().getResourceAsStream("/icons/" + name);
        if (is == null) {
            System.err.println("Icône non trouvée : /icons/" + name);
            return null;
        }
        return new Image(is);
    }

    // Remplace le centre du BorderPane par la vue d’ajout
    @FXML
    private void categoryAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/demo/fxml/Backoffice/claim/ajouterCategorie.fxml"));
            Parent addCateRoot = loader.load();
            root.setCenter(addCateRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Remplace le centre du BorderPane par la vue de modification
    private void openModifyCategoryPage(TypeRec typeRec) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/org/example/demo/fxml/Backoffice/claim/modifierCategorie.fxml"));
            Parent modifyRoot = loader.load();
            org.example.demo.controller.backOffice.claim.ModifierCategorieController controller = loader.getController();
            controller.setTypeRec(typeRec);
            root.setCenter(modifyRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
