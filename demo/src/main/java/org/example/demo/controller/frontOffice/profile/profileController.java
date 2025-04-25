package org.example.demo.controller.frontOffice.profile;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.claim.addClaim;
import org.example.demo.controller.frontOffice.posts.DetailsPosts;
import org.example.demo.controller.frontOffice.posts.ModifierPosts;
import org.example.demo.controller.frontOffice.ressource.addDemandes;
import org.example.demo.models.Demandes;
import org.example.demo.models.Reclamation;
import org.example.demo.models.User;
import org.example.demo.models.Ressources;
import org.example.demo.services.claim.ReclamationService;
import org.example.demo.services.ressource.DemandesService;
import org.example.demo.services.ressource.RessourcesService;
import org.example.demo.services.user.userService;
import org.example.demo.utils.sessionManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javafx.collections.ObservableList;
import javafx.scene.layout.*;
import org.example.demo.models.Posts;
import org.example.demo.services.posts.IService;
import org.example.demo.services.posts.PostsService;
import org.example.demo.services.posts.ProfilePdfGeneratorService;


public class profileController {
    @FXML
    private VBox postsContainer;

    private IService<Posts> postsService = new PostsService();
    private ObservableList<Posts> postsList = FXCollections.observableArrayList();

    @FXML
    private TableColumn<Reclamation, LocalDate> dateColumnReclamation;

    @FXML
    private TableColumn<Reclamation, String> statusColumnReclamation;

    @FXML
    private TableColumn<Reclamation, String> textColumnReclamation;

    @FXML
    private TableView<Reclamation> recalamationTable;

    @FXML
    private TableColumn<Reclamation, Void> actionsColumnReclamation;


    ///////////////////////////////////////////////////////////////////////////////////////

    @FXML
    private TableColumn<Demandes, Void> actionarepondre;

    @FXML
    private AnchorPane addRessoruce;

    @FXML
    private TableColumn<Demandes, LocalDate> datearepondre;

    @FXML
    private TableColumn<Demandes, String> demanderColumn;

    @FXML
    private TableColumn<Demandes, String> messagecolumnarepondre;

    @FXML
    private TableColumn<Demandes, String> prioritearepondre;

    @FXML
    private TableColumn<Demandes, String> etatColumn;

    @FXML
    private TableColumn<Demandes, String> messageColumn;

    @FXML
    private TableColumn<Demandes, String> priopriteColumn;

    @FXML
    private TableColumn<Demandes, LocalDate> DateColumn;

    @FXML
    private TableColumn<Demandes, Void> actionsColumn;

    @FXML
    private Text emailText;

    @FXML
    private Text nomprenomText;

    @FXML
    private TableView<Demandes> demandeUser;

    @FXML
    private TableView<Demandes> demandearepondreuser;

    @FXML
    private GridPane ressourceList;

    @FXML
    private ImageView profileimage;

    @FXML
    private final userService userService = new userService();

    @FXML
    private Button downloadPdfButton;

    User user = new User();
    RessourcesService ressourcesService = new RessourcesService();
    ReclamationService reclamationService = new ReclamationService();
    private ProfilePdfGeneratorService pdfGeneratorService;

    @FXML
    public void initialize() {
        System.out.println(sessionManager.getInstance().getUser().getId()+"iddddddddddd");
        // Verify injection
        if (postsContainer == null) {
            System.err.println("Error: postsContainer not injected!");
            return;
        }
        loadPosts();
        user = sessionManager.getInstance().getUser();

        // Load posts - even if this returns empty, the buttons should still show
        refreshPosts(null);
        // Initialize the PDF service
        pdfGeneratorService = new ProfilePdfGeneratorService();
        // Set up the download button handler
        downloadPdfButton.setOnAction(this::handleDownloadPdf);

        ///////////////////////////////////////
        // USER DATA
        nomprenomText.setText(user.getNom() + " " + user.getPrenom());
        emailText.setText(user.getEmail());
        String imageFileName = user.getImage();
        if (imageFileName != null && !imageFileName.isEmpty()) {
            String imagePath = "/images/posts/" + imageFileName;
            try {
                Image image = new Image(getClass().getResource(imagePath).toString());
                profileimage.setImage(image);
            } catch (NullPointerException e) {
                System.out.println("Image profile not found");
            }
        } else {
            System.out.println("Image profile not found");
        }

        ///////////////////////////////////////
        // LOAD RESSOURCES
        List<Ressources> ressources = ressourcesService.RessourceParUser(user.getId());
        int column = 0, row = 0;
        ressourceList.getChildren().clear();
        for (Ressources ressource : ressources) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/profile/ressourceCard.fxml"));
                AnchorPane card = loader.load();
                ressourceCard controller = loader.getController();
                controller.setRessource(ressource, user);
                card.setPrefWidth(190);
                card.setPrefHeight(230);
                ressourceList.add(card, column, row);
                GridPane.setMargin(card, new Insets(10));
                column++;
                if (column == 3) { column = 0; row++; }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ///////////////////////////////////////
        // LOAD DEMANDES
        DemandesService demandeService = new DemandesService();
        List<Demandes> demandes = demandeService.getDemandesByUserId(user);
        List<Demandes> demandesARepondre = demandeService.getDemandesToAcceptByUserId(user);

        etatColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));
        priopriteColumn.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        DateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        demandeUser.setItems(FXCollections.observableArrayList(demandes));

        demanderColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        messagecolumnarepondre.setCellValueFactory(new PropertyValueFactory<>("message"));
        prioritearepondre.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        datearepondre.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        demandearepondreuser.setItems(FXCollections.observableArrayList(demandesARepondre));

        addActionButtonsToTableDemande();
        addActionButtonsToTableMyDemande();

        ///////////////////////////////////////
        // LOAD RECLAMATIONS
        List<Reclamation> reclamations = reclamationService.getReclamationsByUser(user.getId());

        textColumnReclamation.setCellValueFactory(new PropertyValueFactory<>("content"));
        statusColumnReclamation.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumnReclamation.setCellValueFactory(new PropertyValueFactory<>("date"));

        recalamationTable.setItems(FXCollections.observableArrayList(reclamations));
        addActionButtonsToReclamationTable();

    }

    private void refreshGrid() {
        ressourceList.getChildren().clear();
        initialize();
    }

    public void addRessoruceClicked() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/ressources/addRessource.fxml");
        System.out.println("Add resource clicked");
    }

    private void addActionButtonsToTableDemande() {
        actionsColumn.setCellFactory(column -> new TableCell<Demandes, Void>() {
            private final HBox buttonsBox = new HBox(10);

            private HBox createIconButton(String imagePath, String tooltipText, Runnable action) {
                AnchorPane buttonContainer = new AnchorPane();
                buttonContainer.setMinSize(30, 22);
                buttonContainer.setStyle("-fx-background-color: #fe9341; -fx-background-radius: 15px;");
                buttonContainer.setCursor(Cursor.HAND);

                URL resource = getClass().getResource(imagePath);
                if (resource == null) return new HBox();

                ImageView icon = new ImageView(resource.toExternalForm());
                icon.setFitHeight(14); icon.setFitWidth(14);
                icon.setLayoutX(9); icon.setLayoutY(4);
                buttonContainer.getChildren().add(icon);
                buttonContainer.setOnMouseClicked(e -> action.run());

                return new HBox(buttonContainer);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Demandes demande = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();
                    buttonsBox.getChildren().addAll(
                            createIconButton("/icons/pencil (1).png", "Edit", () -> {
                                try {
                                    HelloApplication.changeSceneWithController(
                                            "/org/example/demo/fxml/Frontoffice/ressources/addDemande.fxml",
                                            controller -> {
                                                if (controller instanceof addDemandes)
                                                    ((addDemandes) controller).setDemande(demande);
                                            }
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }),
                            createIconButton("/icons/trash.png", "Delete", () -> {
                                DemandesService service = new DemandesService();
                                service.supprimer(demande);
                                getTableView().getItems().remove(demande);
                                getTableView().refresh();
                            })
                    );
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void addActionButtonsToTableMyDemande() {
        actionarepondre.setCellFactory(column -> new TableCell<Demandes, Void>() {
            private final HBox buttonsBox = new HBox(10);

            private HBox createIconButton(String imagePath, String tooltipText, Runnable action) {
                AnchorPane buttonContainer = new AnchorPane();
                buttonContainer.setMinSize(35, 27);
                buttonContainer.setStyle("-fx-background-color: #1E74FD; -fx-background-radius: 15px;");
                buttonContainer.setCursor(Cursor.HAND);

                URL resource = getClass().getResource(imagePath);
                if (resource == null) return new HBox();

                ImageView icon = new ImageView(resource.toExternalForm());
                icon.setFitHeight(18); icon.setFitWidth(18);
                icon.setLayoutX(9); icon.setLayoutY(4);
                buttonContainer.getChildren().add(icon);
                buttonContainer.setOnMouseClicked(e -> action.run());

                return new HBox(buttonContainer);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    Demandes demande = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();
                    buttonsBox.getChildren().addAll(
                            createIconButton("/icons/accept.png", "Accept", () -> {
                                DemandesService service = new DemandesService();
                                service.changerStatut(demande.getDemandeId(), "approuvée");
                                demande.setStatus("Accepted");
                                getTableView().refresh();
                            }),
                            createIconButton("/icons/rejected.png", "Reject", () -> {
                                DemandesService service = new DemandesService();
                                service.changerStatut(demande.getDemandeId(), "refusée");
                                demande.setStatus("Rejected");
                                getTableView().refresh();
                            })
                    );
                    setGraphic(buttonsBox);
                }
            }
        });
    }

    private void addActionButtonsToReclamationTable() {
        actionsColumnReclamation.setCellFactory(column -> new TableCell<Reclamation, Void>() {

            private HBox createIconButton(String imagePath, Runnable action) {
                AnchorPane buttonContainer = new AnchorPane();
                buttonContainer.setMinSize(30, 22);
                buttonContainer.setPrefSize(30, 22);
                buttonContainer.setMaxSize(30, 22);
                if(imagePath.equals("/icons/view.png")) {
                    buttonContainer.setStyle("-fx-background-color: #10D876; -fx-background-radius: 8px;");
                } else if(imagePath.equals("/icons/pencil (1).png")) {
                    buttonContainer.setStyle("-fx-background-color: #FE9431; -fx-background-radius: 8px;");
                } else if(imagePath.equals("/icons/trash.png")) {
                    buttonContainer.setStyle("-fx-background-color: #2754E6; -fx-background-radius: 8px;");
                } else if(imagePath.equals("/icons/downloads.png")) {
                    buttonContainer.setStyle("-fx-background-color: #1E74FD; -fx-background-radius: 8px;");
                }
                buttonContainer.setCursor(Cursor.HAND);

                URL resource = getClass().getResource(imagePath);
                if (resource == null) {
                    System.err.println("❌ Image not found: " + imagePath);
                    return new HBox();
                }

                ImageView icon = new ImageView(resource.toExternalForm());
                icon.setFitHeight(14);
                icon.setFitWidth(14);
                icon.setLayoutX(8);
                icon.setLayoutY(4);
                icon.setPreserveRatio(true);

                buttonContainer.getChildren().add(icon);
                buttonContainer.setOnMouseClicked(e -> action.run());

                HBox wrapper = new HBox(buttonContainer);
               // wrapper.setAlignment(Pos.CENTER);
                return wrapper;
            }

            private final HBox buttonsBox = new HBox(10);

            {
              //  buttonsBox.setAlignment(Pos.CENTER);
                buttonsBox.setPadding(new Insets(3));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    Reclamation rec = getTableView().getItems().get(getIndex());
                    buttonsBox.getChildren().clear();

                    // Add your buttons here
                    buttonsBox.getChildren().addAll(
                            createIconButton("/icons/view.png", () -> {

                                System.out.println("View clicked for Reclamation: " + rec.getId());
                            }),
                            createIconButton("/icons/pencil (1).png", () -> {
                                try {
                                    HelloApplication.changeSceneWithController(
                                            "/org/example/demo/fxml/Frontoffice/claim/addClaim.fxml",
                                            controller -> {
                                                if (controller instanceof addClaim)
                                                    ((addClaim) controller).setClaimPourModification(rec);
                                            }
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                System.out.println("Edit clicked for Reclamation: " + rec.getId());
                            }),
                            createIconButton("/icons/trash.png", () -> {
                                reclamationService.supprimerReclamation(rec.getId());
                                HelloApplication.succes("Reclamation Supprime! ");
                            }),
                            createIconButton("/icons/downloads.png", () -> {
                                // TODO: Delete action
                                System.out.println("Downloads clicked for Reclamation: " + rec.getId());
                            })
                    );

                    setGraphic(buttonsBox);
                }
            }
        });
    }


    private void loadPosts() {
        try {
            // Clear existing data
            postsList.clear();
            postsContainer.getChildren().clear();

            // Get all posts from the service
            List<Posts> posts = postsService.rechercher();
            postsList.addAll(posts);

            // Create post cards with staggered animations
            for (int i = 0; i < postsList.size(); i++) {
                Posts post = postsList.get(i);

                VBox postCard = createFacebookStylePost(post);
                postsContainer.getChildren().add(postCard);

                // Animate with staggered timing
                animatePostCard(postCard, i * 50);


            }
        } catch (SQLException e) {
            showError("Error loading posts: " + e.getMessage());
        }
    }

    private void animatePostCard(VBox postCard, int delayMs) {
        postCard.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), postCard);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setDelay(Duration.millis(delayMs));
        fadeIn.play();
    }

    private Button createIconButton(String iconFileName, String tooltipText, String colorHex) {
        Button button = new Button();

        // Load the icon
        InputStream iconStream = getClass().getResourceAsStream("/icons/" + iconFileName);
        if (iconStream != null) {
            Image icon = new Image(iconStream);
            ImageView iconView = new ImageView(icon);
            iconView.setFitWidth(16); // taille de l’icône
            iconView.setFitHeight(16);
            button.setGraphic(iconView);
        } else {
            System.out.println("Icon not found: " + iconFileName);
            button.setText("?"); // fallback
        }

        // Style the button
        button.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-background-radius: 50%;" +
                        "-fx-min-width: 30px;" +
                        "-fx-min-height: 30px;" +
                        "-fx-max-width: 30px;" +
                        "-fx-max-height: 30px;" +
                        "-fx-padding: 0;"
        );

        // Add tooltip
        if (tooltipText != null) {
            button.setTooltip(new Tooltip(tooltipText));
        }

        return button;
    }


    private VBox createFacebookStylePost(Posts post) {
        // Get the user who created this post
        //User postAuthor = userService.getUserById(post.getUser_id_id());
        User postAuthor = this.userService.getUserById(post.getUser_id_id());

        // Main post container
        VBox postCard = new VBox();
        postCard.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        postCard.setSpacing(10);
        postCard.setMaxWidth(Double.MAX_VALUE);

        // Header section with profile pic, name, date, and action buttons
        StackPane headerStack = new StackPane();

        // Left side: profile and name
        HBox headerBox = new HBox();
        //headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setSpacing(10);
        headerBox.setPadding(new Insets(12, 12, 5, 12));

        // Profile picture (circle with first letter)
        StackPane profilePic = new StackPane();
        Circle circle = new Circle(20);
        circle.setFill(Color.valueOf("#1877f2"));

        Label initial = new Label(post.getTitle().substring(0, 1).toUpperCase());
        initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        profilePic.getChildren().addAll(circle, initial);

        // Name and date container
        VBox nameDate = new VBox();
        nameDate.setSpacing(2);

        // Name (using user's name)
        Label nameLabel = new Label(postAuthor.getNom() + " " + postAuthor.getPrenom());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: black;");
        System.out.println("Setting name label to: " + nameLabel.getText());

        // Date (using created_at)
        Label dateLabel = new Label(post.getCreated_at());
        dateLabel.setStyle("-fx-text-fill: #65676b; -fx-font-size: 12px;");

        nameDate.getChildren().addAll(nameLabel, dateLabel);

        headerBox.getChildren().addAll(profilePic, nameDate);

        // Right side: action buttons
        HBox actionsBox = new HBox();
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setSpacing(5);
        actionsBox.setPadding(new Insets(8));

        // Create action buttons with icons
        Button detailsBtn = createIconButton("arrowicon.png", "Voir les détails", "#005eff");
        detailsBtn.setOnAction(e -> openPostDetails(post));

        Button editBtn = createIconButton("editicon.png", "Modifier", "#005eff");
        editBtn.setOnAction(e -> openModifierPosts(post));

        Button deleteBtn = createIconButton("deleteicon.png", "Supprimer", "#ff4c4c");
        deleteBtn.setOnAction(e -> deletePost(post));


        actionsBox.getChildren().addAll(detailsBtn, editBtn, deleteBtn);

        // Stack the header components
         StackPane.setAlignment(headerBox, Pos.CENTER_LEFT);
         StackPane.setAlignment(actionsBox, Pos.CENTER_RIGHT);
        headerStack.getChildren().addAll(headerBox, actionsBox);

        // Content section
        VBox contentBox = new VBox();
        contentBox.setSpacing(8);
        contentBox.setPadding(new Insets(0, 12, 12, 12));

        // Type tag (as a badge)
        Label typeLabel = new Label(post.getType());
        typeLabel.setStyle("-fx-background-color: #e7f3ff; -fx-text-fill: #1877f2; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px;");

        Label TitreLabel = new Label(post.getTitle());
        TitreLabel.setWrapText(true);
        TitreLabel.setStyle("-fx-font-size: 13px; -fx-background-color: #ffffff; -fx-text-fill: black;");

        // Description

        Label descriptionLabel = new Label(post.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 13px; -fx-background-color: #ffffff; -fx-text-fill: black;");

// Test temporaire pour forcer la taille
        descriptionLabel.setPrefHeight(60);
        contentBox.getChildren().addAll(typeLabel, descriptionLabel,TitreLabel);

        // Image section (if available)
        if (post.getImages() != null && !post.getImages().isEmpty() && !post.getImages().equals("null")) {
            try {
                ImageView imageView = createPostImage(post);

                VBox imageBox = new VBox(imageView);
                imageBox.setAlignment(Pos.CENTER);
                imageBox.setPadding(new Insets(5, 0, 5, 0));

                contentBox.getChildren().add(imageBox);
            } catch (Exception e) {
                System.out.println("Error loading image: " + e.getMessage());
            }
        }

        // Footer section with status indicator
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.setPadding(new Insets(8, 12, 8, 12));
        footerBox.setStyle("-fx-border-color: transparent transparent transparent transparent; -fx-border-width: 1 0 0 0; -fx-border-style: solid;");

        // Status indicator
        Circle statusDot = new Circle(4);
        statusDot.setFill("Active".equals(post.getStatus()) ? Color.valueOf("#42b72a") : Color.GRAY);

        Label statusLabel = new Label(post.getStatus());
        statusLabel.setStyle("-fx-text-fill: #65676b; -fx-font-size: 12px;");
        statusLabel.setPadding(new Insets(0, 0, 0, 5));

        // Add small ID indicator (subtle)
        Label idLabel = new Label("ID: " + post.getId());
        idLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 10px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        footerBox.getChildren().addAll(statusDot, statusLabel, spacer, idLabel);

        // Add all sections to the post card
        postCard.getChildren().addAll(headerStack, contentBox, footerBox);

        return postCard;
    }

    private Button createSocialButton(String icon) {
        Button button = new Button(icon);
        button.setStyle("-fx-background-color: #e4e6eb; -fx-background-radius: 50%; -fx-min-width: 30px; -fx-min-height: 30px; -fx-max-width: 30px; -fx-max-height: 30px; -fx-padding: 0;");
        return button;
    }

    private ImageView createPostImage(Posts post) {

        // Create a container for better visibility
        BorderPane imageContainer = new BorderPane();
        imageContainer.setStyle("-fx-border-color: red; -fx-border-width: 2; -fx-background-color: #f8f8f8;");
        imageContainer.setPrefSize(300, 250);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(250);
        imageView.setFitHeight(250);
        imageView.setPreserveRatio(true);

        // Set the alignment of the image within the container
       BorderPane.setAlignment(imageView, Pos.CENTER);
        imageContainer.setCenter(imageView);
        try {
            // Clean up the path - remove brackets and quotes if present
            String imagePath = post.getImages();
            if (imagePath != null) {
                imagePath = imagePath.replaceAll("\\[\"", "").replaceAll("\"\\]", "");
            }

            System.out.println("Cleaned image path: " + imagePath);

            if (imagePath == null || imagePath.isEmpty() || imagePath.equals("null")) {
                throw new Exception("Invalid image path");
            }

            // Try loading the image with absolute path first
            try {
                // Check if file exists on disk
                File imageFile = new File(imagePath);
                if (imageFile.exists() && imageFile.isFile()) {
                    System.out.println("Found image file on disk: " + imageFile.getAbsolutePath());
                    Image image = new Image(imageFile.toURI().toString());
                    imageView.setImage(image);
                    return imageView;
                }
            } catch (Exception e) {
                System.out.println("Failed to load as direct file: " + e.getMessage());
            }

            // Try loading from resources folder
            try {
                String resourcePath = "/images/posts/" + imagePath;
                System.out.println("Trying resource path: " + resourcePath);
                InputStream resourceStream = getClass().getResourceAsStream(resourcePath);

                if (resourceStream != null) {
                    Image image = new Image(resourceStream);
                    imageView.setImage(image);
                    System.out.println("Loaded from resources successfully");
                    return imageView;
                } else {
                    System.out.println("Resource not found: " + resourcePath);
                }
            } catch (Exception e) {
                System.out.println("Failed to load from resources: " + e.getMessage());
            }

            // If we reach here, we couldn't load the image
            throw new Exception("Could not locate image file");

        } catch (Exception e) {
            System.out.println("Error loading image: " + e.getMessage());

            // Create a visual placeholder
            StackPane placeholderPane = new StackPane();
            placeholderPane.setPrefSize(250, 250);
            placeholderPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #ddd; -fx-border-width: 1;");

            Label noImageLabel = new Label("Image Not Found\n" + post.getImages());
            noImageLabel.setTextFill(Color.GRAY);
            noImageLabel.setWrapText(true);
            noImageLabel.setTextAlignment(TextAlignment.CENTER);

            placeholderPane.getChildren().add(noImageLabel);

            // Take snapshot of the placeholder
            SnapshotParameters params = new SnapshotParameters();
            WritableImage writableImage = placeholderPane.snapshot(params, null);
            imageView.setImage(writableImage);
        }

        return imageView;
    }

    @FXML
    void refreshPosts(ActionEvent event) {
        loadPosts();
    }

    @FXML
    void addNewPost(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/posts/AjoutPosts.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ajouter une Publication");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> refreshPosts(null));
            stage.show();
        } catch (IOException e) {
            showError("Error opening add posts form: " + e.getMessage());
        }
    }

    private void openPostDetails(Posts post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/posts/DetailsPosts.fxml"));
            Parent root = loader.load();

            DetailsPosts controller = loader.getController();
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle("Détails de la Publication");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showError("Error opening details view: " + e.getMessage());
        }
    }

    private void openModifierPosts(Posts post) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/posts/ModifierPosts.fxml"));
            Parent root = loader.load();

            ModifierPosts controller = loader.getController();
            controller.setPost(post);

            Stage stage = new Stage();
            stage.setTitle("Modifier Publication");
            stage.setScene(new Scene(root));
            stage.setOnHidden(e -> refreshPosts(null));
            stage.show();
        } catch (IOException e) {
            showError("Error opening edit form: " + e.getMessage());
        }
    }

    private void deletePost(Posts post) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmer la suppression");
        confirmDialog.setHeaderText("Supprimer la publication");
        confirmDialog.setContentText("Voulez-vous vraiment supprimer cette publication ?");

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                postsService.supprimer(post);
                loadPosts();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setContentText("Publication supprimée avec succès !");
                alert.showAndWait();
            } catch (SQLException e) {
                showError("Error deleting post: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDownloadPdf(ActionEvent event) {
        try {
            // Get the user's posts and resources
            List<Posts> posts = postsService.rechercher().stream()
                    .filter(post -> post.getUser_id_id() == user.getId())
                    .toList();

            List<Ressources> resources = ressourcesService.rechercher().stream()
                    .filter(resource -> resource.getUserId() == user.getId())
                    .toList();

            // Generate temporary PDF file
            String tempPdfPath = pdfGeneratorService.generateProfilePdf(user, posts, resources);

            // Set up file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Profile PDF");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );

            // Set suggested filename with user name
            String fileName = String.format(
                    "profile_%s_%s.pdf",
                    user.getNom().toLowerCase(),
                    user.getPrenom().toLowerCase()
            );
            fileChooser.setInitialFileName(fileName);

            // Show save dialog
            Stage stage = (Stage) downloadPdfButton.getScene().getWindow();
            File destinationFile = fileChooser.showSaveDialog(stage);

            if (destinationFile != null) {
                // Copy from temp file to user selected location
                Files.copy(
                        Paths.get(tempPdfPath),
                        destinationFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING
                );

                // Show success message
                showAlert(
                        Alert.AlertType.INFORMATION,
                        "PDF Generated",
                        "Profile PDF was successfully generated and saved."
                );

                // Cleanup temp file
                Files.deleteIfExists(Paths.get(tempPdfPath));
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(
                    Alert.AlertType.ERROR,
                    "PDF Generation Failed",
                    "An error occurred while generating the PDF: " + e.getMessage()
            );
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setContentText(message);
        alert.showAndWait();
    }

}
