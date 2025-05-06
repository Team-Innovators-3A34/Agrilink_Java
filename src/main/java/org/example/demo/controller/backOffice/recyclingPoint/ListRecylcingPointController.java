package org.example.demo.controller.backOffice.recyclingPoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import okhttp3.*;
import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.models.recyclingpoint;
import org.example.demo.services.recyclingpoint.GPTService;
import org.example.demo.services.recyclingpoint.recyclingpointService;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ListRecylcingPointController {
    private recyclingpointService recyclingPointService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // TODO: Set your RapidAPI host/key here
// ✅ Corrected Constants (match your working test)
    private static final String RAPIDAPI_HOST = "ai-api-photo-description.p.rapidapi.com";
    private static final String RAPIDAPI_KEY  = "c4286c5e8cmshf69303343b44530p13d059jsn5101130016d8";
    private static final String RAPIDAPI_URL  = "https://ai-api-photo-description.p.rapidapi.com/description-from-url";

    @FXML private PieChart categoryPieChart;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label totalPointsLabel;
    @FXML private VBox chatBox;
    @FXML private TextField userInput;
    @FXML private Label mostActiveCategoryLabel;
    @FXML private TableView<recyclingpoint> pointTable;
    @FXML private TableColumn<recyclingpoint, String> categoryColumn;
    @FXML private TableColumn<recyclingpoint, String> pointColumn;
    @FXML private TableColumn<recyclingpoint, LocalDate> dateColumn;
    @FXML private TableColumn<recyclingpoint, Void> actionsColumn;
    @FXML private TextField searchField;

    // Image analysis UI
    @FXML private Button uploadButton;
    @FXML private Button cameraButton;
    @FXML private ImageView analyzedImageView;
    @FXML private TextArea descriptionArea;
    @FXML private Label recyclableLabel;

    private static recyclingpoint selectedRecyclingpoint;

    public static void setSelectedRecyclingPoint(recyclingpoint rc) {
        selectedRecyclingpoint = rc;
    }

    public static recyclingpoint getSelectedRecyclingPoint() {
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
                                try {
                                    HelloApplication.changeSceneWithController(
                                            "/org/example/demo/fxml/Backoffice/recyclingPoint/addRecyclingpoint.fxml",
                                            controller -> {
                                                if (controller instanceof addRecyclingpoint) {
                                                    ((addRecyclingpoint) controller).setRecyclingPointPourModification(rp);
                                                }
                                            }
                                    );
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }, "#fe9341"),
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

            private HBox createIconButton(String imagePath, String tooltipText, Runnable action, String color) {
                AnchorPane container = new AnchorPane();
                container.setMinSize(38, 30);
                container.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15px;");
                container.setCursor(Cursor.HAND);

                URL res = getClass().getResource(imagePath);
                if (res != null) {
                    ImageView icon = new ImageView(res.toExternalForm());
                    icon.setFitHeight(16);
                    icon.setFitWidth(14);
                    AnchorPane.setTopAnchor(icon, 9.0);
                    AnchorPane.setLeftAnchor(icon, 12.5);
                    container.getChildren().add(icon);
                }

                container.setOnMouseClicked(e -> action.run());
                return new HBox(container);
            }
        });
    }

    @FXML
    private void onMicClick() {
        System.out.println("Mic button clicked!");
        new Thread(() -> {
            System.out.println("Starting recording thread...");
            recordAndRecognize();
        }).start();
    }

    private void recordAndRecognize() {
        System.out.println("recordAndRecognize() entered");
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine mic = (TargetDataLine) AudioSystem.getLine(info);
            mic.open(format);
            mic.start();
            System.out.println("Recording...");

            ByteArrayOutputStream rawOut = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            long endTime = System.currentTimeMillis() + 3000;
            while (System.currentTimeMillis() < endTime) {
                int count = mic.read(buffer, 0, buffer.length);
                rawOut.write(buffer, 0, count);
            }
            mic.stop();
            mic.close();
            System.out.println("Recording finished.");

            byte[] rawAudio = rawOut.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(rawAudio);
            long frameSize = format.getFrameSize();
            long frameCount = rawAudio.length / frameSize;
            AudioInputStream ais = new AudioInputStream(bais, format, frameCount);

            ByteArrayOutputStream wavOut = new ByteArrayOutputStream();
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, wavOut);
            byte[] wavBytes = wavOut.toByteArray();

            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(wavBytes, MediaType.parse("audio/wav"));
            Request req = new Request.Builder()
                    .url("https://api.deepgram.com/v1/listen?model=nova-3&smart_format=true")
                    .addHeader("Authorization", "Token 2c072f7b1fcbd408b682dbd5242c30bc5425b11c")
                    .post(body)
                    .build();

            try (Response resp = client.newCall(req).execute()) {
                String json = resp.body().string();
                String transcript = extractTranscript(json);
                Platform.runLater(() -> searchField.setText(transcript));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractTranscript(String json) {
        int i = json.indexOf("\"transcript\":\"") + 14;
        int j = json.indexOf("\"", i);
        return (i > 13 && j > i) ? json.substring(i, j) : "";
    }

    public void addpoint() {
        HelloApplication.changeScene("/org/example/demo/fxml/Backoffice/recyclingPoint/addRecyclingPoint.fxml");
    }

    private void setupSearch(List<recyclingpoint> points) {
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String filter = newV.toLowerCase().trim();
            if (filter.isEmpty()) {
                pointTable.getItems().setAll(points);
            } else {
                List<recyclingpoint> filtered = points.stream()
                        .filter(p -> p.getNom().toLowerCase().contains(filter)
                                || p.getType().toLowerCase().contains(filter)
                                || (p.getDate() != null && p.getDate().toString().contains(filter)))
                        .toList();
                pointTable.getItems().setAll(filtered);
            }
        });
    }

    private void setupStatistics(List<recyclingpoint> points) {
        totalPointsLabel.setText("Total Points: " + points.size());
        Map<String, Long> counts = points.stream()
                .collect(Collectors.groupingBy(recyclingpoint::getType, Collectors.counting()));
        categoryPieChart.getData().clear();
        counts.forEach((cat, cnt) -> categoryPieChart.getData().add(new PieChart.Data(cat, cnt)));
        String top = counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("None");
        mostActiveCategoryLabel.setText("Most Active Category: " + top);
    }






    @FXML
    private void importCsv() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(null);
        if (file == null) return;
        List<recyclingpoint> imported = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            reader.readNext();
            String[] row;
            while ((row = reader.readNext()) != null) {
                recyclingpoint rp = new recyclingpoint();
                rp.setNom(row[0].trim());
                rp.setType(row[1].trim());
                rp.setDate(row[2].trim());
                recyclingPointService.ajouter(rp);
                imported.add(rp);
            }
            pointTable.getItems().addAll(imported);
            setupStatistics(pointTable.getItems());
            new Alert(Alert.AlertType.INFORMATION, imported.size() + " points imported.", ButtonType.OK).showAndWait();
        } catch (IOException | CsvValidationException e) {
            new Alert(Alert.AlertType.ERROR, "Import failed: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }
}