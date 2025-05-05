package org.example.demo.controller.frontOffice.claim;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class JobController implements Initializable {

    @FXML private GridPane jobsGridPane;
    @FXML private TextField countryField;
    @FXML private Button filterButton;
    @FXML private Label statusLabel;
    @FXML private Label totalCountLabel;
    @FXML private Button loadMoreButton;

    private final String apiUrl = "https://jooble.org/api/";
    // Remplacez par votre clé API Jooble
    private final String apiKey = "bbfdbec0-7070-4112-a635-d87b2f5e6c07";

    private int currentPage = 1;
    private String currentCountry = "";
    private int totalCount = 0;
    private int itemsPerPage = 0;

    // Nombre de colonnes dans la grille
    private final int GRID_COLUMNS = 3;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Masquer le bouton "Voir plus" jusqu'à ce qu'on sache qu'il y a plus d'offres
        loadMoreButton.setVisible(false);

        // Configurer la grille dès le départ
        setupGridPane();

        // Charger les offres d'emploi
        loadJobs(1, "");
    }

    private void setupGridPane() {
        // Effacer les contraintes existantes
        jobsGridPane.getColumnConstraints().clear();
        jobsGridPane.getRowConstraints().clear();

        // Configurer les colonnes de la grille avec des largeurs égales
        for (int i = 0; i < GRID_COLUMNS; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / GRID_COLUMNS);
            colConstraints.setHgrow(Priority.SOMETIMES);
            colConstraints.setFillWidth(true);
            jobsGridPane.getColumnConstraints().add(colConstraints);
        }
    }

    @FXML
    private void handleFilter() {
        String country = countryField.getText().trim();
        currentCountry = country;
        currentPage = 1;

        // Effacer la grille avant de charger de nouveaux résultats
        jobsGridPane.getChildren().clear();
        setupGridPane();
        loadJobs(currentPage, currentCountry);
    }

    @FXML
    private void handleLoadMore() {
        currentPage++;
        loadJobs(currentPage, currentCountry);
    }

    private void loadJobs(int page, String country) {
        statusLabel.setText("Chargement des offres...");

        // Préparer le corps de la requête JSON
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("keywords", "agriculture");
            requestBody.put("page", page);

            if (!country.isEmpty()) {
                requestBody.put("location", country);
            }
        } catch (JSONException e) {
            handleError("Erreur lors de la préparation de la requête: " + e.getMessage());
            return;
        }

        // Appel asynchrone à l'API
        CompletableFuture.runAsync(() -> {
            try {
                String responseBody = postRequest(apiUrl + apiKey, requestBody.toString());
                Platform.runLater(() -> processResponse(responseBody, page == 1));
            } catch (IOException e) {
                Platform.runLater(() -> handleError("Erreur lors de la communication avec l'API: " + e.getMessage()));
            }
        });
    }

    private String postRequest(String apiUrl, String jsonBody) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoOutput(true);

        // Écrire le corps de la requête
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Vérifier le code de statut HTTP
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Erreur HTTP: " + responseCode);
        }

        // Lire la réponse
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            return response.toString();
        }
    }

    private void processResponse(String responseBody, boolean clearGrid) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);

            // Mettre à jour le total des offres
            totalCount = jsonResponse.getInt("totalCount");
            totalCountLabel.setText("Total d'offres : " + totalCount);

            // Récupérer les offres
            JSONArray jobs = jsonResponse.getJSONArray("jobs");
            itemsPerPage = jobs.length();

            // Effacer la grille si c'est la première page
            if (clearGrid) {
                jobsGridPane.getChildren().clear();
            }

            // Calculer le nombre d'offres déjà affichées
            int startIndex = clearGrid ? 0 : jobsGridPane.getChildren().size();

            // Ajouter les offres d'emploi à la grille
            for (int i = 0; i < jobs.length(); i++) {
                JSONObject job = jobs.getJSONObject(i);

                // Calculer la position dans la grille
                int index = startIndex + i;
                int col = index % GRID_COLUMNS;
                int row = index / GRID_COLUMNS;

                // Créer et ajouter la carte d'offre d'emploi
                VBox jobCard = createJobCard(job);

                // Définir des contraintes pour que chaque carte occupe toute la largeur de sa cellule
                GridPane.setFillWidth(jobCard, true);
                GridPane.setHgrow(jobCard, Priority.ALWAYS);

                // Ajouter à la grille
                jobsGridPane.add(jobCard, col, row);
            }

            // Mettre à jour l'état de chargement
            statusLabel.setText("");

            // Afficher ou masquer le bouton "Voir plus"
            boolean hasMoreItems = (currentPage * itemsPerPage) < totalCount;
            loadMoreButton.setVisible(hasMoreItems);

        } catch (JSONException e) {
            handleError("Erreur lors du traitement de la réponse JSON: " + e.getMessage());
        }
    }

    private VBox createJobCard(JSONObject job) throws JSONException {
        // Créer une carte pour l'offre d'emploi
        VBox jobCard = new VBox();
        jobCard.getStyleClass().add("job-post-card");
        jobCard.setStyle("-fx-background-color: white; -fx-border-radius: 8px; " +
                "-fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                "-fx-transition: transform 0.3s ease, -fx-effect 0.3s ease;");
        jobCard.setPadding(new Insets(0, 0, 10, 0));
        jobCard.setSpacing(0);

        // Importante : fixer une largeur uniforme pour toutes les cartes
        jobCard.setPrefWidth(Region.USE_COMPUTED_SIZE);
        jobCard.setMinWidth(250);
        jobCard.setMaxWidth(Double.MAX_VALUE);

        // En-tête avec le titre
        VBox header = new VBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(12, 15, 12, 15));
        header.setStyle("-fx-background-color: linear-gradient(45deg, #007bff, #00d4ff); " +
                "-fx-background-radius: 8px 8px 0 0;");

        Label titleLabel = new Label(job.getString("title"));
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: white; -fx-wrap-text: true;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        header.getChildren().add(titleLabel);
        header.setMaxWidth(Double.MAX_VALUE);
        jobCard.getChildren().add(header);

        // Corps avec les détails
        VBox body = new VBox();
        body.setPadding(new Insets(15));
        body.setSpacing(10);
        body.setMaxWidth(Double.MAX_VALUE);

        // Créer une boîte horizontale pour les badges
        HBox badgesBox = new HBox();
        badgesBox.setSpacing(8);
        badgesBox.setPadding(new Insets(0, 0, 10, 0));

        // Badge pour l'emplacement
        String location = job.getString("location");
        Label locationBadge = createBadge(location, "danger");
        badgesBox.getChildren().add(locationBadge);

        // Badge pour le numéro d'emploi (si disponible)
        if (job.has("jobNumber")) {
            String jobNumber = job.getString("jobNumber");
            Label jobNumberBadge = createBadge("#" + jobNumber, "secondary");
            badgesBox.getChildren().add(jobNumberBadge);
        }

        body.getChildren().add(badgesBox);

        // Description de l'offre
        String snippet = job.getString("snippet");
        Label snippetLabel = new Label(snippet);
        snippetLabel.setWrapText(true);
        snippetLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px; -fx-line-spacing: 1.5;");
        snippetLabel.setMaxWidth(Double.MAX_VALUE);
        body.getChildren().add(snippetLabel);

        jobCard.getChildren().add(body);

        // Footer avec bouton pour voir l'offre
        VBox footer = new VBox();
        footer.setPadding(new Insets(0, 15, 10, 15));
        footer.setAlignment(Pos.CENTER);
        footer.setMaxWidth(Double.MAX_VALUE);

        Button viewButton = new Button("Voir l'offre");
        viewButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; " +
                "-fx-padding: 8px 12px; -fx-background-radius: 4px; -fx-font-weight: bold; " +
                "-fx-cursor: hand;");
        viewButton.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(viewButton, Priority.ALWAYS);

        // Action du bouton pour ouvrir l'URL
        String link = job.getString("link");
        viewButton.setOnAction(event -> openJobLink(link));

        footer.getChildren().add(viewButton);
        jobCard.getChildren().add(footer);

        // Ajouter des effets au survol
        jobCard.setOnMouseEntered(e ->
                jobCard.setStyle("-fx-background-color: white; -fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 12, 0, 0, 5); " +
                        "-fx-transform: translateY(-5px);")
        );

        jobCard.setOnMouseExited(e ->
                jobCard.setStyle("-fx-background-color: white; -fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3); " +
                        "-fx-transform: translateY(0px);")
        );

        return jobCard;
    }

    private Label createBadge(String text, String type) {
        Label badge = new Label(" " + text + " ");
        badge.setStyle("-fx-background-color: " + getBadgeColor(type) + "; " +
                "-fx-text-fill: white; -fx-background-radius: 4px; " +
                "-fx-padding: 3px 8px; -fx-font-size: 11px; -fx-font-weight: bold;");
        return badge;
    }

    private String getBadgeColor(String type) {
        return switch (type) {
            case "danger" -> "#dc3545";
            case "secondary" -> "#6c757d";
            case "success" -> "#28a745";
            case "warning" -> "#ffc107";
            case "info" -> "#17a2b8";
            default -> "#007bff";
        };
    }

    private void openJobLink(String url) {
        try {
            // Ouvrir l'URL dans le navigateur par défaut
            java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
        } catch (Exception e) {
            showAlert("Erreur lors de l'ouverture du lien: " + e.getMessage());
        }
    }

    private void handleError(String message) {
        statusLabel.setText("Erreur: " + message);
        showAlert(message);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText("Une erreur est survenue");
        alert.setContentText(message);
        alert.showAndWait();
    }
}