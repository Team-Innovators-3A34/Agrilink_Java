package org.example.demo.controller.frontOffice.claim;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AgricultureNewsController {

    @FXML
    private GridPane newsGridPane;

    @FXML
    private Button loadMoreButton;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label statusLabel;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    private final String apiKey = "b87ca822cdmsh0a0c4c3c7567bd9p1824ddjsn90c4848fb6c4";
    private final String apiHost = "news-api14.p.rapidapi.com";
    private final String apiUrl = "https://news-api14.p.rapidapi.com/v2/search/articles";

    private int currentPage = 1;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final ObservableList<NewsArticle> newsArticles = FXCollections.observableArrayList();

    // Nombre de colonnes pour afficher les articles
    private final int NUM_COLUMNS = 3;

    @FXML
    private void initialize() {
        // Configure GridPane
        newsGridPane.setHgap(12);
        newsGridPane.setVgap(20);
        newsGridPane.setPadding(new Insets(10));
        newsGridPane.setAlignment(Pos.CENTER);

        // Make ScrollPane resize with content
        scrollPane.setFitToWidth(true);

        // Set status label
        if (statusLabel != null) {
            statusLabel.setText("Chargement des actualités agricoles...");
            statusLabel.setVisible(true);
        }

        // Style loadMoreButton
        loadMoreButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; " +
                "-fx-padding: 8px 20px; -fx-font-size: 13px; -fx-background-radius: 4px;");

        // Load first page of news
        loadAgricultureNews(1);
    }

    private void loadAgricultureNews(int page) {
        loadMoreButton.setDisable(true);
        loadMoreButton.setText("Chargement...");

        CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "?query=agriculture&language=fr&page=" + page))
                        .header("x-rapidapi-host", apiHost)
                        .header("x-rapidapi-key", apiKey)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                return response.body();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }, executor).thenAccept(responseBody -> {
            if (responseBody != null) {
                List<NewsArticle> articles = parseNewsResponse(responseBody);
                Platform.runLater(() -> {
                    if (!articles.isEmpty()) {
                        displayNewsArticles(articles);
                        currentPage++;
                        loadMoreButton.setDisable(false);
                        loadMoreButton.setText("Voir plus d'articles");
                        if (statusLabel != null) {
                            statusLabel.setVisible(false);
                        }
                    } else {
                        loadMoreButton.setDisable(true);
                        loadMoreButton.setText("Fin des articles");
                        if (statusLabel != null) {
                            statusLabel.setText("Tous les articles ont été chargés");
                            statusLabel.setVisible(true);
                        }
                    }
                });
            } else {
                Platform.runLater(() -> {
                    loadMoreButton.setDisable(false);
                    loadMoreButton.setText("Réessayer");
                    if (statusLabel != null) {
                        statusLabel.setText("Erreur lors du chargement des articles");
                        statusLabel.setVisible(true);
                    }
                });
            }
        });
    }

    private List<NewsArticle> parseNewsResponse(String responseBody) {
        List<NewsArticle> articles = new ArrayList<>();
        try {
            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

            if (jsonResponse.has("data") && jsonResponse.get("data").isJsonArray()) {
                JsonArray articlesArray = jsonResponse.getAsJsonArray("data");

                for (JsonElement element : articlesArray) {
                    JsonObject articleObj = element.getAsJsonObject();

                    String title = getStringValue(articleObj, "title");
                    String url = getStringValue(articleObj, "url");
                    String excerpt = getStringValue(articleObj, "excerpt");
                    String thumbnail = getStringValue(articleObj, "thumbnail");
                    String dateStr = getStringValue(articleObj, "date");
                    String source = getStringValue(articleObj, "source");

                    // Parse date if available
                    Date date = null;
                    if (!dateStr.isEmpty()) {
                        try {
                            // Assuming ISO format, adjust if needed
                            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                            //date = sdf.parse(dateStr);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    articles.add(new NewsArticle(title, url, excerpt, thumbnail, date, source));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return articles;
    }

    private String getStringValue(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return "";
    }

    private void displayNewsArticles(List<NewsArticle> articles) {
        // Calculate starting position
        int startIndex = newsArticles.size();

        // Add new articles to our collection
        newsArticles.addAll(articles);

        // Clear grid and rebuild it
        if (startIndex == 0) {
            newsGridPane.getChildren().clear();
        }

        // Arrange articles in grid
        for (int i = 0; i < articles.size(); i++) {
            NewsArticle article = articles.get(i);
            VBox articleCard = createArticleCard(article);

            int index = startIndex + i;
            int row = index / NUM_COLUMNS;
            int col = index % NUM_COLUMNS;

            newsGridPane.add(articleCard, col, row);
        }
    }

    private VBox createArticleCard(NewsArticle article) {
        // Main card container
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; " +
                "-fx-border-radius: 6px; " +
                "-fx-background-radius: 6px; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1); " +
                "-fx-min-width: 240px; " +
                "-fx-max-width: 240px; " +
                "-fx-min-height: 340px; " +   // Réduire de 360px à 340px
                "-fx-max-height: 340px;");    // Réduire de 360px à 340px

        // Source and date row
        if (article.getSource() != null && !article.getSource().isEmpty() || article.getDate() != null) {
            HBox metaBox = new HBox(10);
            metaBox.setAlignment(Pos.CENTER_LEFT);
            metaBox.setPrefWidth(220);

            if (article.getSource() != null && !article.getSource().isEmpty()) {
                Label sourceLabel = new Label(article.getSource().toUpperCase());
                sourceLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
                HBox.setHgrow(sourceLabel, Priority.ALWAYS);
                metaBox.getChildren().add(sourceLabel);
            }

            if (article.getDate() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Label dateLabel = new Label(sdf.format(article.getDate()));
                dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #95a5a6;");
                dateLabel.setAlignment(Pos.CENTER_RIGHT);
                metaBox.getChildren().add(dateLabel);
            }

            card.getChildren().add(metaBox);
        }

        // Thumbnail image
        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPrefHeight(130);
        imageContainer.setMinHeight(130);
        imageContainer.setMaxHeight(130);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 4px;");

        if (article.getThumbnail() != null && !article.getThumbnail().isEmpty()) {
            ImageView imageView = new ImageView();
            imageView.setFitWidth(220);
            imageView.setFitHeight(130);
            imageView.setPreserveRatio(true);

            // Load image asynchronously
            loadImageAsync(article.getThumbnail(), imageView);

            imageContainer.getChildren().add(imageView);
        } else {
            Label noImageLabel = new Label("Actualité Agricole");
            noImageLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            imageContainer.getChildren().add(noImageLabel);
        }

        card.getChildren().add(imageContainer);

        // Title
        Label titleLabel = new Label(article.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.TOP_LEFT);
        titleLabel.setPrefHeight(70);
        titleLabel.setMinHeight(70);
        titleLabel.setMaxHeight(70);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        card.getChildren().add(titleLabel);

        // Excerpt
        String truncatedExcerpt = article.getExcerpt();
        if (truncatedExcerpt != null) {
            if (truncatedExcerpt.length() > 100) {
                truncatedExcerpt = truncatedExcerpt.substring(0, 100) + "...";
            }

            Text excerptText = new Text(truncatedExcerpt);
            excerptText.setStyle("-fx-font-size: 12px; -fx-fill: #555;");
            TextFlow excerptFlow = new TextFlow(excerptText);
            excerptFlow.setTextAlignment(TextAlignment.LEFT);
            excerptFlow.setPrefHeight(70);    // Réduire de 80px à 70px
            excerptFlow.setMinHeight(70);     // Réduire de 80px à 70px
            excerptFlow.setMaxHeight(70);     // Réduire de 80px à 70px
            card.getChildren().add(excerptFlow);
        }

        // Spacer to push button to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        card.getChildren().add(spacer);

        // Read more button
        Button readMoreBtn = new Button("Lire l'article");
        readMoreBtn.setPrefWidth(200);
        readMoreBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-background-radius: 4px; -fx-font-size: 12px; -fx-padding: 7px 0px;");
        readMoreBtn.setOnAction(e -> openInBrowser(article.getUrl()));
        card.getChildren().add(readMoreBtn);

        // Make entire card clickable
        card.setOnMouseClicked(e -> openInBrowser(article.getUrl()));

        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() +
                    "-fx-effect: dropshadow(three-pass-box, rgba(52,152,219,0.4), 10, 0, 0, 0); " +
                    "-fx-translate-y: -2px;");
            titleLabel.setStyle(titleLabel.getStyle() + "-fx-text-fill: #3498db;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle()
                    .replace("-fx-effect: dropshadow(three-pass-box, rgba(52,152,219,0.4), 10, 0, 0, 0); -fx-translate-y: -2px;",
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 1);"));
            titleLabel.setStyle(titleLabel.getStyle().replace("-fx-text-fill: #3498db;", "-fx-text-fill: #2c3e50;"));
        });

        return card;
    }

    private void loadImageAsync(String imageUrl, ImageView imageView) {
        CompletableFuture.runAsync(() -> {
            try {
                Image image = new Image(imageUrl, true);
                if (!image.isError()) {
                    Platform.runLater(() -> {
                        imageView.setImage(image);
                        // Ensure image is properly centered
                        imageView.setPreserveRatio(true);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, executor);
    }

    private void openInBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLoadMore(ActionEvent event) {
        loadAgricultureNews(currentPage);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/afficherReclamations.fxml"));
            Parent mainView = loader.load();
            BorderPane root = (BorderPane) ((Button) event.getSource()).getScene().getRoot();
            root.setCenter(mainView);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // NewsArticle model class
    public static class NewsArticle {
        private final String title;
        private final String url;
        private final String excerpt;
        private final String thumbnail;
        private final Date date;
        private final String source;

        public NewsArticle(String title, String url, String excerpt, String thumbnail, Date date, String source) {
            this.title = title;
            this.url = url;
            this.excerpt = excerpt;
            this.thumbnail = thumbnail;
            this.date = date;
            this.source = source;
        }

        public String getTitle() { return title; }
        public String getUrl() { return url; }
        public String getExcerpt() { return excerpt; }
        public String getThumbnail() { return thumbnail; }
        public Date getDate() { return date; }
        public String getSource() { return source; }
    }
}