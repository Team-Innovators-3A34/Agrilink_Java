package org.example.demo.controller.backOffice.posts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.example.demo.controller.frontOffice.posts.ModifierPosts;
import org.example.demo.models.Posts;
import org.example.demo.services.posts.IService;
import org.example.demo.services.posts.PostsService;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;

public class AfficherPosts {

    @FXML
    private TableView<Posts> postsTable;

    @FXML
    private TableColumn<Posts, Integer> idColumn;

    @FXML
    private TableColumn<Posts, String> titleColumn;

    @FXML
    private TableColumn<Posts, String> typeColumn;

    @FXML
    private TableColumn<Posts, String> descriptionColumn;

    @FXML
    private TableColumn<Posts, String> createdAtColumn;

    @FXML
    private TableColumn<Posts, String> statusColumn;

    @FXML
    private TableColumn<Posts, Void> actionsColumn;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> typeFilter;

    @FXML
    private ComboBox<String> statusFilter;

    @FXML
    private DatePicker fromDatePicker;

    @FXML
    private DatePicker toDatePicker;

    @FXML
    private Button applyFiltersBtn;

    @FXML
    private Button resetFiltersBtn;

    @FXML
    private Button addPostBtn;

    private IService<Posts> postsService = new PostsService();
    private ObservableList<Posts> allPostsList = FXCollections.observableArrayList();
    private FilteredList<Posts> filteredPostsList;
    private SortedList<Posts> sortedPostsList;

    // Date formatter for comparing dates
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {
        configureTableColumns();
        configureFilters();
        setupSorting();
        loadPosts();
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("created_at"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Make columns sortable
        titleColumn.setSortable(true);
        typeColumn.setSortable(false);
        descriptionColumn.setSortable(false);
        createdAtColumn.setSortable(true);
        statusColumn.setSortable(false);
        idColumn.setSortable(false);

        // Configuration de la colonne d'actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button deleteBtn = new Button("Delete");
            private final HBox buttonsBox = new HBox(5, viewBtn,deleteBtn);

            {
                // Style des boutons
                viewBtn.setStyle("-fx-background-color: #32BDEA; -fx-text-fill: white; -fx-font-size: 12px;");
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px;");

                // Taille des boutons
                viewBtn.setPrefWidth(55);
                deleteBtn.setPrefWidth(60);

                // Actions des boutons
                viewBtn.setOnAction(event -> {
                    Posts post = getTableView().getItems().get(getIndex());
                    openPostDetails(post);
                });


                deleteBtn.setOnAction(event -> {
                    Posts post = getTableView().getItems().get(getIndex());
                    deletePost(post);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void setupSorting() {
        // Create sorted list from filtered list
        sortedPostsList = new SortedList<>(filteredPostsList);

        // Bind sorting to the table view
        sortedPostsList.comparatorProperty().bind(postsTable.comparatorProperty());

        // Set the sorted list as items to the table
        postsTable.setItems(sortedPostsList);
    }

    private void configureFilters() {
        // Initialize FilteredList
        filteredPostsList = new FilteredList<>(allPostsList, p -> true);

        // Configure date pickers
        configDatePickers();

        // Configure filter ComboBoxes
        typeFilter.getItems().add("All Types");
        typeFilter.getSelectionModel().selectFirst();

        statusFilter.getItems().add("All Status");
        statusFilter.getSelectionModel().selectFirst();

        // Configure search field to filter as you type (optional)
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            applyFiltersAction(null);
        });
    }

    private void configDatePickers() {
        // Set prompt text
        fromDatePicker.setPromptText("From Date");
        toDatePicker.setPromptText("To Date");

        // Set converters to handle null values
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        };

        fromDatePicker.setConverter(converter);
        toDatePicker.setConverter(converter);

        // Set validation (ensure from date is not after to date)
        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && toDatePicker.getValue() != null && newVal.isAfter(toDatePicker.getValue())) {
                toDatePicker.setValue(newVal);
            }
        });

        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && fromDatePicker.getValue() != null && newVal.isBefore(fromDatePicker.getValue())) {
                fromDatePicker.setValue(newVal);
            }
        });
    }

    private void populateFilterOptions() {
        // Get unique types and statuses from posts
        Set<String> types = new HashSet<>();
        Set<String> statuses = new HashSet<>();

        for (Posts post : allPostsList) {
            if (post.getType() != null && !post.getType().isEmpty()) {
                types.add(post.getType());
            }
            if (post.getStatus() != null && !post.getStatus().isEmpty()) {
                statuses.add(post.getStatus());
            }
        }

        // Save current selections
        String currentType = typeFilter.getValue();
        String currentStatus = statusFilter.getValue();

        // Update type filter options
        typeFilter.getItems().clear();
        typeFilter.getItems().add("All Types");
        typeFilter.getItems().addAll(types);

        // Restore previous selection or default to "All Types"
        if (currentType != null && typeFilter.getItems().contains(currentType)) {
            typeFilter.setValue(currentType);
        } else {
            typeFilter.getSelectionModel().selectFirst();
        }

        // Update status filter options
        statusFilter.getItems().clear();
        statusFilter.getItems().add("All Status");
        statusFilter.getItems().addAll(statuses);

        // Restore previous selection or default to "All Status"
        if (currentStatus != null && statusFilter.getItems().contains(currentStatus)) {
            statusFilter.setValue(currentStatus);
        } else {
            statusFilter.getSelectionModel().selectFirst();
        }
    }

    @FXML
    void applyFiltersAction(ActionEvent event) {
        applyFilters();
    }

    @FXML
    void resetFilters(ActionEvent event) {
        searchField.clear();
        typeFilter.getSelectionModel().selectFirst();
        statusFilter.getSelectionModel().selectFirst();
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);

        // Apply the cleared filters
        applyFilters();
    }

    private void applyFilters() {
        String searchText = searchField.getText().toLowerCase();
        String selectedType = typeFilter.getValue();
        String selectedStatus = statusFilter.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        filteredPostsList.setPredicate(post -> {
            boolean matchesSearch = true;
            boolean matchesType = true;
            boolean matchesStatus = true;
            boolean matchesDateRange = true;

            // Search filter
            if (searchText != null && !searchText.isEmpty()) {
                matchesSearch = post.getTitle().toLowerCase().contains(searchText) ||
                        (post.getDescription() != null && post.getDescription().toLowerCase().contains(searchText));
            }

            // Type filter
            if (selectedType != null && !selectedType.equals("All Types")) {
                matchesType = post.getType().equals(selectedType);
            }

            // Status filter
            if (selectedStatus != null && !selectedStatus.equals("All Status")) {
                matchesStatus = post.getStatus().equals(selectedStatus);
            }

            // Date range filter
            if (fromDate != null || toDate != null) {
                try {
                    LocalDate postDate = LocalDate.parse(post.getCreated_at().split(" ")[0], dateFormatter);

                    if (fromDate != null && toDate != null) {
                        matchesDateRange = !postDate.isBefore(fromDate) && !postDate.isAfter(toDate);
                    } else if (fromDate != null) {
                        matchesDateRange = !postDate.isBefore(fromDate);
                    } else if (toDate != null) {
                        matchesDateRange = !postDate.isAfter(toDate);
                    }
                } catch (Exception e) {
                    // If date parsing fails, we'll assume it doesn't match
                    matchesDateRange = false;
                }
            }

            return matchesSearch && matchesType && matchesStatus && matchesDateRange;
        });
    }

    private void loadPosts() {
        try {
            allPostsList.clear();
            List<Posts> posts = postsService.rechercher();
            allPostsList.addAll(posts);

            // Populate filter dropdown options with actual data
            populateFilterOptions();

            // Apply current filters to the new data
            applyFilters();

            // Debug: Afficher les chemins d'images chargés
            System.out.println("Posts loaded with images:");
            posts.forEach(post -> System.out.println("ID: " + post.getId() + " | Image: " + post.getImages()));

        } catch (SQLException e) {
            showAlert("Error", "Error loading posts",
                    "Unable to load posts: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void refreshPosts(ActionEvent event) {
        loadPosts();
    }

    @FXML
    void addPost(ActionEvent event) {
        try {
            // Use getResource with the correct path
            URL fxmlLocation = getClass().getResource("/org/example/demo/fxml/Backoffice/posts/AjouterPosts.fxml");

            // Check if the location is null before proceeding
            if (fxmlLocation == null) {
                showAlert("Error", "Resource not found",
                        "Could not find the FXML file: /org/example/demo/fxml/Backoffice/posts/AjouterPosts.fxml",
                        Alert.AlertType.ERROR);
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Add New Post");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Refresh posts list after adding
            loadPosts();
        } catch (IOException e) {
            showAlert("Error", "Form error",
                    "Unable to open add form: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace(); // Print the stack trace for debugging
        }
    }


    private void openPostDetails(Posts post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/posts/DetailsPosts.fxml"));
            Parent root = loader.load();

            DetailsPosts controller = loader.getController();
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle("Post Details: " + post.getTitle());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Display error",
                    "Unable to open details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deletePost(Posts post) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Deletion");
        confirmDialog.setHeaderText("Delete post: " + post.getTitle());
        confirmDialog.setContentText("This action cannot be undone. Continue?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                postsService.supprimer(post);
                loadPosts();
                showAlert("Success", "Deletion successful",
                        "The post was successfully deleted.", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Error", "Deletion error",
                        "Unable to delete post: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
