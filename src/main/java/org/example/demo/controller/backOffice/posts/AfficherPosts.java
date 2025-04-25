package org.example.demo.controller.backOffice.posts;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.demo.models.Posts;
import org.example.demo.services.posts.IService;
import org.example.demo.services.posts.PostsService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

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
    private TableColumn<Posts, String> imagesColumn;

    @FXML
    private TableColumn<Posts, Void> actionsColumn;

    private IService<Posts> postsService = new PostsService();
    private ObservableList<Posts> postsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTableColumns();
        loadPosts();
    }

    private void configureTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        createdAtColumn.setCellValueFactory(new PropertyValueFactory<>("created_at"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));


        // Configuration de la colonne d'actions
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("Voir");
            private final Button editBtn = new Button("Modifier");
            private final Button deleteBtn = new Button("Supprimer");
            private final HBox buttonsBox = new HBox(5, viewBtn, editBtn, deleteBtn);

            {
                // Style des boutons
                viewBtn.setStyle("-fx-background-color: #005eff; -fx-text-fill: white; -fx-font-size: 12px;");
                editBtn.setStyle("-fx-background-color: #ffc107; -fx-text-fill: white; -fx-font-size: 12px;");
                deleteBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 12px;");

                // Taille des boutons
                viewBtn.setPrefWidth(60);
                editBtn.setPrefWidth(70);
                deleteBtn.setPrefWidth(80);

                // Actions des boutons
                viewBtn.setOnAction(event -> {
                    Posts post = getTableView().getItems().get(getIndex());
                    openPostDetails(post);
                });

                editBtn.setOnAction(event -> {
                    Posts post = getTableView().getItems().get(getIndex());
                    openModifierPosts(post);
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

    private void loadPosts() {
        try {
            postsList.clear();
            List<Posts> posts = postsService.rechercher();
            postsList.addAll(posts);
            postsTable.setItems(postsList);

            // Debug: Afficher les chemins d'images chargés
            System.out.println("Posts loaded with images:");
            posts.forEach(post -> System.out.println("ID: " + post.getId() + " | Image: " + post.getImages()));

        } catch (SQLException e) {
            showAlert("Erreur", "Erreur lors du chargement des publications",
                    "Impossible de charger les publications: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    void refreshPosts(ActionEvent event) {
        loadPosts();
    }

    @FXML
    void addNewPost(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/posts/AjoutPosts.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Publication");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> refreshPosts(null));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur d'ouverture",
                    "Impossible d'ouvrir le formulaire d'ajout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openPostDetails(Posts post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/posts/DetailsPosts.fxml"));
            Parent root = loader.load();

            DetailsPosts controller = loader.getController();
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle("Détails de la Publication: " + post.getTitle());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur d'ouverture",
                    "Impossible d'ouvrir les détails: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void openModifierPosts(Posts post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/posts/ModifierPosts.fxml"));
            Parent root = loader.load();

            ModifierPosts controller = loader.getController();
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle("Modifier Publication: " + post.getTitle());
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> refreshPosts(null));
            stage.show();
        } catch (IOException e) {
            showAlert("Erreur", "Erreur d'ouverture",
                    "Impossible d'ouvrir l'éditeur: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void deletePost(Posts post) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer la publication: " + post.getTitle());
        confirmDialog.setContentText("Cette action est irréversible. Continuer ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                postsService.supprimer(post);
                loadPosts();
                showAlert("Succès", "Suppression réussie",
                        "La publication a été supprimée avec succès.", Alert.AlertType.INFORMATION);
            } catch (SQLException e) {
                showAlert("Erreur", "Erreur de suppression",
                        "Impossible de supprimer la publication: " + e.getMessage(), Alert.AlertType.ERROR);
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