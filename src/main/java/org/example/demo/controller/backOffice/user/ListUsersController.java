package org.example.demo.controller.backOffice.user;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.example.demo.models.User;
import org.example.demo.services.user.userService;

import java.net.URL;
import java.util.List;

public class ListUsersController {

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> nomprenomColumn;

    @FXML
    private TableColumn<User, String> telephoneColumn;

    @FXML
    private TableColumn<User, String> adressColumn;

    @FXML
    private TableColumn<User, String> typeColumn;

    @FXML
    private TableColumn<User, String> statusColumn;

    @FXML
    private TableColumn<User, String> imageColumn;

    @FXML
    private TableColumn<User, Void> actionsColumn;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    userService userService = new userService();

    @FXML
    private TextField rechercheField;


    @FXML
    public void initialize() {

        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        nomprenomColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().nomProperty().get() + " " + cellData.getValue().prenomProperty().get())
        );

        telephoneColumn.setCellValueFactory(cellData -> cellData.getValue().telephoneProperty());
        adressColumn.setCellValueFactory(cellData -> cellData.getValue().adresseProperty());
        typeColumn.setCellValueFactory(cellData -> cellData.getValue().rolesProperty());
        typeColumn.setCellFactory(column -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    if ("[\"ROLE_AGRICULTURE\",\"ROLE_USER\"]".equals(item)) {
                        setText("Agriculteur");
                    } else if ("[\"ROLE_RECYCLING_INVESTOR\",\"ROLE_USER\"]".equals(item)) {
                        setText("Recycling Investor");
                    } else if ("[\"ROLE_RESOURCE_INVESTOR\",\"ROLE_USER\"]".equals(item)) {
                        setText("Ressource Investor");
                    }
                }
            }
        });
        statusColumn.setCellValueFactory(cellData -> {
            String status = cellData.getValue().accountVerificationProperty().get();
            if (status != null && !status.isEmpty()) {
                status = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
            }
            return new SimpleStringProperty(status);
        });
        imageColumn.setCellValueFactory(cellData -> cellData.getValue().ImageProperty());

        loadUsers();
        addActionButtonsToTable();
    }

    private void loadUsers() {
        List<User> users = userService.getAllUsers();

        userList.setAll(users);
        usersTable.setItems(userList);
    }

    private void addActionButtonsToTable() {
        actionsColumn.setCellFactory(column -> new TableCell<User, Void>() {

            private HBox createIconButton(String imagePath, String tooltipText, Runnable action) {
                AnchorPane buttonContainer = new AnchorPane();
                buttonContainer.setMinSize(38, 30);
                buttonContainer.setPrefSize(38, 30);
                buttonContainer.setMaxSize(38, 30);
                if (imagePath.equals("/icons/pencil (1).png")){
                    buttonContainer.setStyle("-fx-background-color: #fe9341; -fx-background-radius: 15px;");

                }else {
                    buttonContainer.setStyle("-fx-background-color: #0055ff; -fx-background-radius: 15px;");
                }
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
                    User user = getTableView().getItems().get(getIndex());

                    buttonsBox.getChildren().clear();

                    buttonsBox.getChildren().addAll(
                            createIconButton("/icons/view.png", "View", () -> {
                                System.out.println("View: " + user.getEmail());
                                // Add view logic here
                            }),
                            createIconButton("/icons/pencil (1).png", "Edit", () -> {
                                System.out.println("Edit: " + user.getEmail());
                                // Add edit logic here
                            }),
                            createIconButton("/icons/trash.png", "Delete", () -> {
                                System.out.println("Delete: " + user.getEmail());
                                userService.DeleteUser(user.getEmail());
                                getTableView().getItems().remove(user);
                                getTableView().refresh();
                            })
                    );

                    setGraphic(buttonsBox);
                }
            }
        });
    }
}
