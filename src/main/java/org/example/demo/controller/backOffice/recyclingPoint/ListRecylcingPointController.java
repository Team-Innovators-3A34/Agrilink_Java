package org.example.demo.controller.backOffice.recyclingPoint;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.models.recyclingpoint;
import org.example.demo.services.recyclingpoint.GPTService;
import org.example.demo.services.recyclingpoint.recyclingpointService;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ListRecylcingPointController {

    private User user;
    private recyclingpointService recyclingPointService;
    @FXML
    private PieChart categoryPieChart;
    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;
    private List<recyclingpoint> allPoints;

    @FXML
    private Label totalPointsLabel;
    @FXML
    private VBox chatBox;
    @FXML
    private TextField userInput;

    @FXML
    private Label mostActiveCategoryLabel;

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
    @FXML
    private TextField searchField;


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

        setupSearch(points);
        setupStatistics(points);
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
    private void setupSearch(List<recyclingpoint> points) {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String lowerCaseFilter = newValue.toLowerCase().trim();

            if (lowerCaseFilter.isEmpty()) {
                pointTable.getItems().setAll(points);
            } else {
                List<recyclingpoint> filteredList = points.stream()
                        .filter(p -> p.getNom().toLowerCase().contains(lowerCaseFilter) ||
                                p.getType().toLowerCase().contains(lowerCaseFilter) ||
                                (p.getDate() != null && p.getDate().toString().contains(lowerCaseFilter)))
                        .toList();
                pointTable.getItems().setAll(filteredList);
            }
        });
    }
    private void setupStatistics(List<recyclingpoint> points) {
        totalPointsLabel.setText("Total Points: " + points.size());

        Map<String, Long> categoryCount = points.stream()
                .collect(Collectors.groupingBy(recyclingpoint::getType, Collectors.counting()));

        categoryPieChart.getData().clear();
        categoryCount.forEach((category, count) -> {
            categoryPieChart.getData().add(new PieChart.Data(category, count));
        });

        // Find the most active category
        String mostActiveCategory = categoryCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");

        mostActiveCategoryLabel.setText("Most Active Category: " + mostActiveCategory);
    }
    @FXML
    private void sendMessage() {
        String message = userInput.getText().trim();
        if (message.isEmpty()) return;

        addMessageToChat("🧍‍♂️ You: " + message);
        userInput.clear();

        new Thread(() -> {
            try {
                String ecoBotReply = GPTService.askGPT(message);
                Platform.runLater(() -> handleGPTResponse(ecoBotReply));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> addMessageToChat("❌ EcoBot error: " + e.getMessage()));
            }
        }).start();
    }

    private void handleGPTResponse(String response) {
        addMessageToChat("♻️ EcoBot: " + response);

        if (response.toLowerCase().contains("add point")) {
            addpoint();
        } else if (response.toLowerCase().contains("show points")) {
            pointTable.getItems().setAll(recyclingPointService.rechercher());
        }
    }

    private void addMessageToChat(String message) {
        Label label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(380);
        // Optional styling:
        if (message.startsWith("♻️")) {
            label.setStyle("-fx-background-color: #E0F7FA; -fx-padding: 8; -fx-background-radius: 5;");
        } else {
            label.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 8; -fx-background-radius: 5;");
        }
        chatBox.getChildren().add(label);
    }
    @FXML
    private void importCsv() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(null);
        if (file == null) return;

        List<recyclingpoint> imported = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] row;
            // Assuming header: nom,type,date (as String)
            reader.readNext(); // skip header
            while ((row = reader.readNext()) != null) {
                String nom = row[0].trim();
                String type = row[1].trim();
                String dateStr = row[2].trim(); // treat date as String

                recyclingpoint rp = new recyclingpoint();
                rp.setNom(nom);
                rp.setType(type);
                rp.setDate(dateStr);
                recyclingPointService.ajouter(rp);
                imported.add(rp);
            }

            pointTable.getItems().addAll(imported);
            setupStatistics(pointTable.getItems());
            new Alert(Alert.AlertType.INFORMATION, imported.size() + " points imported successfully.", ButtonType.OK).showAndWait();
        } catch (IOException | CsvValidationException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to import CSV: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

}
