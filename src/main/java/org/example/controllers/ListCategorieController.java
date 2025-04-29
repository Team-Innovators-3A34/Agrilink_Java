package org.example.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.entities.TypeRec;
import org.example.services.TypeRecService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ListCategorieController implements Initializable {

    @FXML
    private BorderPane root;
    @FXML
    private TableView<TypeRec> categoriesTableView;
    @FXML
    private TableColumn<TypeRec, Integer> idColumn;
    @FXML
    private TableColumn<TypeRec, String> nomColumn;
    @FXML
    private TableColumn<TypeRec, String> descriptionColumn;
    @FXML
    private TableColumn<TypeRec, TypeRec> actionColumn;

    private TypeRecService typeRecService = new TypeRecService();
    private ObservableList<TypeRec> categoriesData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadCategories();
        setupTableView();
    }

    private void loadCategories() {
        List<TypeRec> list = typeRecService.getAllCategories();
        categoriesData.setAll(list);
        categoriesTableView.setItems(categoriesData);
    }

    private void setupTableView() {
        idColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nom"));
        descriptionColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("description"));

        actionColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionColumn.setCellFactory(new Callback<TableColumn<TypeRec, TypeRec>, TableCell<TypeRec, TypeRec>>() {
            @Override
            public TableCell<TypeRec, TypeRec> call(TableColumn<TypeRec, TypeRec> param) {
                return new TableCell<TypeRec, TypeRec>() {
                    private final Button deleteBtn = new Button("Supprimerr");
                    private final Button modifyBtn = new Button("Modifier");
                    private final HBox container = new HBox(5);

                    {
                        container.getChildren().addAll(modifyBtn, deleteBtn);

                        deleteBtn.setOnAction(event -> {
                            TypeRec typeRec = getTableView().getItems().get(getIndex());
                            boolean deleted = typeRecService.supprimerTypeRec(typeRec.getId());
                            if (deleted) {
                                categoriesData.remove(typeRec);
                            }
                        });

                        modifyBtn.setOnAction(event -> {
                            TypeRec typeRec = getTableView().getItems().get(getIndex());
                            openModifyCategoriePage(typeRec);
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

    private void openModifyCategoriePage(TypeRec typeRec) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/modifyCategorie.fxml"));
            Parent modifyRoot = loader.load();
            ModifyCategorieController controller = loader.getController();
            controller.setRoot(root);
            controller.setTypeRec(typeRec);
            root.setCenter(modifyRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
