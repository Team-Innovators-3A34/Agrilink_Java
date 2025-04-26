package org.example.demo.controller.backOffice.event;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import org.example.demo.HelloApplication;
import org.example.demo.models.categorie;
import org.example.demo.models.event;
import org.example.demo.services.event.eventService;

import java.io.File;
import java.net.URL;

public class ListEventController {

    @FXML private TableView<event> eventTable;
    @FXML private TableColumn<event, String> nomColumn;
    @FXML private TableColumn<event, String> typeColumn;
    @FXML private TableColumn<event, String> dateColumn;
    @FXML private TableColumn<event, String> adresseColumn;
    @FXML private TableColumn<event, String> categorieColumn;
    @FXML private TableColumn<event, Integer> nbrPlacesColumn;
    @FXML private TableColumn<event, String> imageColumn;
    @FXML private TableColumn<event, Void> actionsColumn;



    private static event selectedEvent;
    private final eventService service = new eventService();
    private final ObservableList<event> eventList = FXCollections.observableArrayList();

    public static void setSelectedEvent(event ev) {
        selectedEvent = ev;
    }

    public static event getSelectedEvent() {
        return selectedEvent;
    }

    @FXML
    private void initialize() {
        // Remplissage des colonnes
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        nbrPlacesColumn.setCellValueFactory(new PropertyValueFactory<>("nbrPlaces"));

        categorieColumn.setCellValueFactory(cellData -> {
            categorie cat = cellData.getValue().getCategorie();
            return new javafx.beans.property.SimpleStringProperty(cat != null ? cat.getNom() : "");
        });

        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageColumn.setCellFactory(column -> new TableCell<>() {
            private final ImageView imageView = new ImageView();

            {
                imageView.setFitHeight(60);
                imageView.setFitWidth(90);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null || imagePath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        File file = new File(imagePath);
                        System.out.println("🖼️ Trying to load image: " + file.toURI());
                        Image img = new Image(file.toURI().toString(), true);
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        System.out.println("❌ Failed to load image: " + imagePath);
                        e.printStackTrace();
                        setGraphic(new Label("Image ❌"));
                    }
                }
            }

        });

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
                    return new HBox(); // pour éviter un crash
                }

                ImageView icon = new ImageView(resource.toExternalForm());
                icon.setFitHeight(16);
                icon.setFitWidth(14);
                icon.setPreserveRatio(true);

                // Centrer dans l'AnchorPane
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
                    event ev = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();

                    buttonsBox.getChildren().addAll(
                            createIconButton("/icons/pencil (1).png", "Modifier", () -> {
                                System.out.println("➡️ Modifier : " + ev);
                                setSelectedEvent(ev);
                                HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/event/modEvent.fxml");
                            }, "#fe9341"),

                            createIconButton("/icons/trash.png", "Supprimer", () -> {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Suppression");
                                alert.setHeaderText("Supprimer l'événement \"" + ev.getNom() + "\" ?");
                                alert.setContentText("Êtes-vous sûr ?");
                                alert.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.OK) {
                                        service.supprimer(ev);
                                        loadEvents();
                                    }
                                });
                            }, "#0055ff")
                    );

                    setGraphic(buttonsBox);
                }
            }
        });

        // Initialiser la table
        loadEvents();


    }

    private void loadEvents() {
        eventList.setAll(service.rechercher());
        eventTable.setItems(eventList);
    }

    public void addevent() {
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/event/addEvent.fxml");
    }

    public void categorieList() {
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/event/listCategorie.fxml");
    }
}
