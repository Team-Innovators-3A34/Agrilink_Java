package org.example.demo.controller.frontOffice.profile;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
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
import org.example.demo.controller.frontOffice.claim.detailClaim;
import org.example.demo.controller.frontOffice.posts.DetailsPosts;
import org.example.demo.controller.frontOffice.posts.ModifierPosts;
import org.example.demo.controller.frontOffice.ressource.addDemandes;
import org.example.demo.models.*;
import org.example.demo.services.claim.ReclamationService;
import org.example.demo.services.posts.PostSentimentAPI;
import org.example.demo.services.recyclingpoint.recyclingpointService;
import org.example.demo.services.ressource.DemandesService;
import org.example.demo.services.ressource.RessourcesService;
import org.example.demo.services.user.userService;
import org.example.demo.utils.sessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
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

    private static final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private IService<Posts> postsService = new PostsService();
    private ObservableList<Posts> postsList = FXCollections.observableArrayList();
    @FXML
    private TabPane profilePane;
    @FXML
    private Tab tabRecyclingPoints;
    @FXML
    private Tab resourceTab;


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
    private GridPane pointList;

    @FXML
    private ImageView profileimage;
    @FXML
    private Button btnDisponibilite;

    @FXML
    private final userService userService = new userService();

    @FXML
    private Button downloadPdfButton;


    User user = new User();
    RessourcesService ressourcesService = new RessourcesService();
    ReclamationService reclamationService = new ReclamationService();
    recyclingpointService recyclingpointservice =new recyclingpointService();
    private ProfilePdfGeneratorService pdfGeneratorService;
    private PostSentimentAPI sentimentAPI = new PostSentimentAPI();

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
        downloadPdfButton.setOnAction(this::handleDownloadPdf);
        //  btnDisponibilite.setOnAction(e -> afficherPopupCalendrier());

        if (user.getRoles().equals("[\"ROLE_RECYCLING_INVESTOR\",\"ROLE_USER\"]")) {
            profilePane.getTabs().remove(resourceTab);
        }

        if (!user.getRoles().equals("[\"ROLE_RECYCLING_INVESTOR\",\"ROLE_USER\"]")) {
            profilePane.getTabs().remove(tabRecyclingPoints);
        }

        ///////////////////////////////////////
        // USER DATA
        nomprenomText.setText(user.getNom() + " " + user.getPrenom());
        emailText.setText(user.getEmail());
        String imageFileName = user.getImage();
        if (imageFileName != null && !imageFileName.isEmpty()) {
            String imagePath = "/images/" + imageFileName;
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
        // List<Ressources> ressources = ressourcesService.RessourceParUser(user.getId());
        List<Ressources> ressources = ressourcesService.RessourceParUser(sessionManager.getInstance().getUser().getId());
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

        ///////////////////////////////////////
        //LOAD POINTS
        List<recyclingpoint> recyclingpoints = recyclingpointservice.pointPerPoint(user);
        int colum = 0, ro = 0;
        pointList.getChildren().clear();
        for (recyclingpoint recyclingpoint : recyclingpoints) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/profile/pointCard.fxml"));
                AnchorPane card = loader.load();
                pointCard controller = loader.getController();
                controller.setPoint(recyclingpoint, user);
                card.setPrefWidth(190);
                card.setPrefHeight(230);
                pointList.add(card, colum, ro);
                GridPane.setMargin(card, new Insets(10));
                colum++;
                if (colum == 3) { colum = 0; ro++; }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
                wrapper.setAlignment(Pos.CENTER);
                return wrapper;
            }

            private final HBox buttonsBox = new HBox(10);

            {
                buttonsBox.setAlignment(Pos.CENTER);
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
                                try {
                                    HelloApplication.changeSceneWithController(
                                            "/org/example/demo/fxml/Frontoffice/claim/claimDetail.fxml",
                                            controller -> {
                                                if (controller instanceof detailClaim) {
                                                    ((detailClaim) controller).setReclamation(rec);
                                                }
                                            }
                                    );
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    throw new RuntimeException(e); // Good practice to re-throw
                                }
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
                                // Action pour supprimer la réclamation
                                getTableView().getItems().remove(rec);
                                getTableView().refresh();
                            }),
                            createIconButton("/icons/downloads.png", () -> {
                                Reclamation recl = getTableView().getItems().get(getIndex());
                                FileChooser fileChooser = new FileChooser();
                                fileChooser.setTitle("Enregistrer le PDF");
                                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                                fileChooser.setInitialFileName("reclamation_" + recl.getId() + ".pdf");
                                File file = fileChooser.showSaveDialog(recalamationTable.getScene().getWindow());

                                if (file != null) {
                                    try {
                                        // Création et ouverture du document PDF avec des marges améliorées
                                        com.itextpdf.text.Document document = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4, 36, 36, 54, 36);
                                        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(document, new FileOutputStream(file));
                                        document.open();

                                        // Définition des couleurs
                                        com.itextpdf.text.BaseColor primaryColor = new com.itextpdf.text.BaseColor(30, 116, 253); // Bleu principal (#1E74FD)
                                        com.itextpdf.text.BaseColor secondaryColor = new com.itextpdf.text.BaseColor(102, 102, 102); // Gris
                                        com.itextpdf.text.BaseColor accentColor = new com.itextpdf.text.BaseColor(242, 242, 242); // Gris clair

                                        // Définition des polices
                                        com.itextpdf.text.Font titleFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 20, primaryColor);
                                        com.itextpdf.text.Font subtitleFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 14, primaryColor);
                                        com.itextpdf.text.Font headerFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 12, secondaryColor);
                                        com.itextpdf.text.Font bodyFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 11, com.itextpdf.text.BaseColor.BLACK);
                                        com.itextpdf.text.Font labelFont = com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, primaryColor);

                                        // Ajout d'un en-tête avec logo (si disponible)
                                        try {
                                            // Chemin vers votre logo - à ajuster selon l'emplacement réel
                                            String logoPath = getClass().getResource("/icons/logo.png").getPath();
                                            com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoPath);
                                            logo.scaleToFit(120, 60);
                                            logo.setAlignment(com.itextpdf.text.Element.ALIGN_RIGHT);
                                            document.add(logo);
                                        } catch (Exception e) {
                                            // Continuer sans logo en cas d'erreur
                                            System.out.println("Logo non trouvé: " + e.getMessage());
                                        }

                                        // Ajouter un titre principal avec ligne de séparation
                                        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph("DÉTAIL DE LA RÉCLAMATION", titleFont);
                                        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                                        title.setSpacingAfter(10);
                                        document.add(title);

                                        // Ajouter une ligne de séparation
                                        com.itextpdf.text.pdf.draw.LineSeparator lineSeparator = new com.itextpdf.text.pdf.draw.LineSeparator(1, 100, primaryColor, com.itextpdf.text.Element.ALIGN_CENTER, -8);
                                        document.add(lineSeparator);
                                        document.add(new com.itextpdf.text.Paragraph(" "));

                                        // Créer une section "Informations générales"
                                        com.itextpdf.text.Paragraph infoTitle = new com.itextpdf.text.Paragraph("Informations générales", subtitleFont);
                                        infoTitle.setSpacingAfter(10);
                                        document.add(infoTitle);

                                        // Créer une mise en page à deux colonnes pour les informations principales
                                        com.itextpdf.text.pdf.PdfPTable infoTable = new com.itextpdf.text.pdf.PdfPTable(2);
                                        infoTable.setWidthPercentage(100);
                                        infoTable.setSpacingAfter(15);

                                        // Configurer les largeurs relatives des colonnes
                                        float[] columnWidths = {1f, 3f};
                                        infoTable.setWidths(columnWidths);

                                        // Ajouter les informations de réclamation avec séparation claire entre label et valeur
                                        addInfoRow(infoTable, "Identifiant:", String.valueOf(rec.getId()), labelFont, bodyFont);
                                        addInfoRow(infoTable, "Titre:", rec.getTitle(), labelFont, bodyFont);
                                        addInfoRow(infoTable, "Utilisateur:", rec.getNomUser(), labelFont, bodyFont);
                                        addInfoRow(infoTable, "Email:", rec.getMailUser(), labelFont, bodyFont);
                                        addInfoRow(infoTable, "Statut:", rec.getStatus(), labelFont, bodyFont);
                                        addInfoRow(infoTable, "Type:", rec.getTypeNom(), labelFont, bodyFont);
                                        addInfoRow(infoTable, "Priorité:", String.valueOf(rec.getPriorite()), labelFont, bodyFont);
                                        addInfoRow(infoTable, "Date:", (rec.getDate() != null ? rec.getDate().format(dateFormatter) : "N/A"), labelFont, bodyFont);

                                        document.add(infoTable);

                                        // Section contenu avec fond coloré
                                        com.itextpdf.text.pdf.PdfPTable contentTable = new com.itextpdf.text.pdf.PdfPTable(1);
                                        contentTable.setWidthPercentage(100);
                                        contentTable.setSpacingAfter(20);

                                        com.itextpdf.text.pdf.PdfPCell contentHeaderCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph("Contenu de la réclamation", headerFont));
                                        contentHeaderCell.setBackgroundColor(primaryColor);
                                        contentHeaderCell.setPadding(8);
                                        contentHeaderCell.setBorderWidth(0);
                                        contentTable.addCell(contentHeaderCell);

                                        com.itextpdf.text.pdf.PdfPCell contentCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph(rec.getContent(), bodyFont));
                                        contentCell.setPadding(10);
                                        contentCell.setBackgroundColor(accentColor);
                                        contentCell.setBorderWidth(0);
                                        contentTable.addCell(contentCell);

                                        document.add(contentTable);

                                        // Récupérer et ajouter les réponses associées
                                        document.add(new com.itextpdf.text.Paragraph("RÉPONSES ASSOCIÉES", subtitleFont));
                                        document.add(new com.itextpdf.text.Paragraph(" "));

                                        List<org.example.demo.models.Reponses> responses =
                                                new org.example.demo.services.claim.ReponsesService().getAnswersByReclamation(rec.getId());

                                        if (responses != null && !responses.isEmpty()) {
                                            // Création d'un tableau pour les réponses avec style amélioré
                                            com.itextpdf.text.pdf.PdfPTable table = new com.itextpdf.text.pdf.PdfPTable(4);
                                            table.setWidthPercentage(100);
                                            table.setSpacingBefore(10f);
                                            table.setSpacingAfter(10f);

                                            // Définir les largeurs relatives des colonnes
                                            float[] responseColumnWidths = {2f, 2f, 1f, 1f};
                                            table.setWidths(responseColumnWidths);

                                            // Style pour les en-têtes des colonnes
                                            com.itextpdf.text.pdf.PdfPCell headerCell = new com.itextpdf.text.pdf.PdfPCell();
                                            headerCell.setBackgroundColor(primaryColor);
                                            headerCell.setPadding(8);
                                            headerCell.setBorderWidth(0);

                                            // Définir les en-têtes de colonnes
                                            headerCell.setPhrase(new com.itextpdf.text.Paragraph("Contenu",
                                                    com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.WHITE)));
                                            table.addCell(headerCell);

                                            headerCell.setPhrase(new com.itextpdf.text.Paragraph("Solution",
                                                    com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.WHITE)));
                                            table.addCell(headerCell);

                                            headerCell.setPhrase(new com.itextpdf.text.Paragraph("Statut",
                                                    com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.WHITE)));
                                            table.addCell(headerCell);

                                            headerCell.setPhrase(new com.itextpdf.text.Paragraph("Date",
                                                    com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA_BOLD, 11, com.itextpdf.text.BaseColor.WHITE)));
                                            table.addCell(headerCell);

                                            // Style pour les cellules de données
                                            com.itextpdf.text.pdf.PdfPCell dataCell = new com.itextpdf.text.pdf.PdfPCell();
                                            dataCell.setPadding(7);

                                            // Remplir le tableau avec les réponses (alternance de couleurs)
                                            boolean alternate = false;
                                            for (org.example.demo.models.Reponses rep : responses) {
                                                // Alterner les couleurs de fond
                                                if (alternate) {
                                                    dataCell.setBackgroundColor(accentColor);
                                                } else {
                                                    dataCell.setBackgroundColor(com.itextpdf.text.BaseColor.WHITE);
                                                }

                                                dataCell.setPhrase(new com.itextpdf.text.Paragraph(rep.getContent(), bodyFont));
                                                table.addCell(dataCell);

                                                dataCell.setPhrase(new com.itextpdf.text.Paragraph(rep.getSolution(), bodyFont));
                                                table.addCell(dataCell);

                                                dataCell.setPhrase(new com.itextpdf.text.Paragraph(rep.getStatus(), bodyFont));
                                                table.addCell(dataCell);

                                                dataCell.setPhrase(new com.itextpdf.text.Paragraph(rep.getDate() != null ?
                                                        rep.getDate().format(dateFormatter) : "Non spécifiée", bodyFont));
                                                table.addCell(dataCell);

                                                alternate = !alternate; // Inverser pour la prochaine ligne
                                            }
                                            document.add(table);
                                        } else {
                                            com.itextpdf.text.Paragraph noResponse = new com.itextpdf.text.Paragraph("Aucune réponse associée à cette réclamation.",
                                                    com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 11, secondaryColor));
                                            noResponse.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                                            document.add(noResponse);
                                        }

                                        // Ajout d'un pied de page
                                        com.itextpdf.text.Paragraph footer = new com.itextpdf.text.Paragraph("Document généré le " +
                                                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm")),
                                                com.itextpdf.text.FontFactory.getFont(com.itextpdf.text.FontFactory.HELVETICA, 8, secondaryColor));
                                        footer.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
                                        document.add(footer);

                                        document.close();

                                        // Afficher une confirmation
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                        alert.setTitle("PDF Généré");
                                        alert.setHeaderText(null);
                                        alert.setContentText("Le fichier PDF a été généré avec succès : " + file.getAbsolutePath());
                                        alert.showAndWait();

                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        Alert alert = new Alert(Alert.AlertType.ERROR);
                                        alert.setTitle("Erreur lors de la génération du PDF");
                                        alert.setHeaderText(null);
                                        alert.setContentText("Erreur : " + ex.getMessage());
                                        alert.showAndWait();
                                    }
                                }
                            })
                    );

                    setGraphic(buttonsBox);
                }
            }
        });
    }

    // Méthode d'aide pour ajouter des lignes d'information (à ajouter à la classe profileController)
    private void addInfoRow(com.itextpdf.text.pdf.PdfPTable table, String label, String value,
                            com.itextpdf.text.Font labelFont, com.itextpdf.text.Font valueFont) {
        com.itextpdf.text.pdf.PdfPCell labelCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph(label, labelFont));
        labelCell.setPadding(5);
        labelCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        table.addCell(labelCell);

        com.itextpdf.text.pdf.PdfPCell valueCell = new com.itextpdf.text.pdf.PdfPCell(new com.itextpdf.text.Paragraph(value, valueFont));
        valueCell.setPadding(5);
        valueCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    private void loadPosts() {
        try {
            // Clear existing data
            postsList.clear();
            postsContainer.getChildren().clear();

            // Get all posts from the service
            List<Posts> allPosts = postsService.rechercher();

            // Filter posts to only show those from the current profile user
            List<Posts> userPosts = allPosts.stream()
                    .filter(post -> post.getUser_id_id() == user.getId())
                    .toList();

            // Analyze sentiment for each post
            for (Posts post : userPosts) {
                if (post.getSentiment() == null || post.getSentiment().equals(Posts.SENTIMENT_UNKNOWN)) {
                    // Use the new method that updates the post directly
                    sentimentAPI.analyzeAndUpdatePostSentiment(post);

                    // Update the post in the database with the sentiment
                    postsService.modifier(post);
                }
            }
            postsList.addAll(userPosts);

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

        // post sentiment
        Label sentimentLabel = new Label("Sentiment: " + (post.getSentiment() != null ? post.getSentiment() : "Unknown"));
        String sentimentColor;
        switch (post.getSentiment() != null ? post.getSentiment().toLowerCase() : "") {
            case "positive":
                sentimentColor = "#4CAF50"; // Green
                break;
            case "negative":
                sentimentColor = "#F44336"; // Red
                break;
            case "neutral":
                sentimentColor = "#9E9E9E"; // Gray
                break;
            default:
                sentimentColor = "#9E9E9E"; // Gray for unknown
        }

       // Style both labels
        sentimentLabel.setStyle("-fx-background-color: " + sentimentColor + "20; -fx-text-fill: " + sentimentColor +
                "; -fx-padding: 2 8; -fx-background-radius: 10; -fx-font-size: 11px;");

        HBox tagsBox = new HBox(10);
        tagsBox.getChildren().addAll(typeLabel, sentimentLabel);
        //title
        Label TitreLabel = new Label(post.getTitle());
        TitreLabel.setWrapText(true);
        TitreLabel.setStyle("-fx-font-size: 13px; -fx-background-color: #ffffff; -fx-text-fill: black;");


        // Description

        Label descriptionLabel = new Label(post.getDescription());
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 13px; -fx-background-color: #ffffff; -fx-text-fill: black;");

      // Test temporaire pour forcer la taille
        descriptionLabel.setPrefHeight(60);
        contentBox.getChildren().addAll(tagsBox, descriptionLabel,TitreLabel);

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
            // Load the DetailsPosts.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Frontoffice/posts/DetailsPosts.fxml"));
            Node detailsView = loader.load();

            // Get the controller and set the post data
            DetailsPosts detailsController = loader.getController();
            detailsController.setPost(post);

            // Pass a reference to this controller to allow going back
            detailsController.setParentController(this);

            // Clear the current postsContainer content and add the details view
            postsContainer.getChildren().clear();
            postsContainer.getChildren().add(detailsView);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Failed to load DetailsPosts.fxml: " + e.getMessage());
        }
    }

    public void showPostsList() {
        postsContainer.getChildren().clear();
        loadPosts();
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

    public void addPointClicked(){
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/pointRecyclage/addRecyclingPoint.fxml");
    }

    //fonctionelle
    public void afficherCalendrierAvecDemandes(List<Demandes> demandes, Dialog<Void> dialog) {
        VBox conteneur = new VBox(30);
        conteneur.setPadding(new Insets(20));
        conteneur.setStyle("-fx-background-color: #1e1e1e;"); // Thème sombre

        Year anneeActuelle = Year.now();

        for (int mois = 1; mois <= 12; mois++) {
            YearMonth moisActuel = YearMonth.of(anneeActuelle.getValue(), mois);

            Label labelMois = new Label(moisActuel.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH).toUpperCase());
            labelMois.setStyle("-fx-font-size: 22px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");

            GridPane calendrier = new GridPane();
            calendrier.setHgap(10);
            calendrier.setVgap(10);
            calendrier.setPadding(new Insets(10));

            String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
            for (int i = 0; i < jours.length; i++) {
                Label jourHeader = new Label(jours[i]);
                jourHeader.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14px;");
                calendrier.add(jourHeader, i, 0);
            }

            LocalDate premierJour = moisActuel.atDay(1);
            int decalage = premierJour.getDayOfWeek().getValue();
            int joursDansMois = moisActuel.lengthOfMonth();

            int ligne = 1;
            int colonne = decalage - 1;

            for (int jour = 1; jour <= joursDansMois; jour++) {
                LocalDate dateCourante = moisActuel.atDay(jour);

                Label jourLabel = new Label(String.valueOf(jour));
                jourLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
                jourLabel.setPadding(new Insets(5));

                StackPane cellule = new StackPane(jourLabel);
                cellule.setPrefSize(60, 40);
                cellule.setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #444444; -fx-border-radius: 5; -fx-background-radius: 5;");
                cellule.setEffect(new DropShadow(2, Color.BLACK));

                cellule.setOnMouseEntered(e -> cellule.setStyle(cellule.getStyle() + "-fx-scale-x: 1.03; -fx-scale-y: 1.03; -fx-background-color: #383838;"));
                cellule.setOnMouseExited(e -> cellule.setStyle(cellule.getStyle().replace("-fx-scale-x: 1.03; -fx-scale-y: 1.03; -fx-background-color: #383838;", "")));

                for (Demandes demande : demandes) {
                    if ((dateCourante.isEqual(demande.getCreatedAt()) || dateCourante.isAfter(demande.getCreatedAt())) &&
                            (dateCourante.isEqual(demande.getExpireDate()) || dateCourante.isBefore(demande.getExpireDate()))) {

                        String couleur = getCouleurParRessource(demande.getRessourceId());
                        cellule.setStyle(couleur + " -fx-border-radius: 5; -fx-background-radius: 5;");
                        Tooltip tooltip = new Tooltip("Demande du " + demande.getCreatedAt() + " au " + demande.getExpireDate());
                        Tooltip.install(cellule, tooltip);
                        break;
                    }
                }

                calendrier.add(cellule, colonne, ligne);
                colonne++;
                if (colonne > 6) {
                    colonne = 0;
                    ligne++;
                }
            }

            conteneur.getChildren().addAll(labelMois, calendrier);
        }

        ScrollPane scrollPane = new ScrollPane(conteneur);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #1e1e1e;");

        dialog.getDialogPane().setContent(scrollPane);
    }


    @FXML
    private void onAfficherCalendrierClick(ActionEvent event) {
        List<Demandes> demandes = DemandesService.getDemandesApprouveesByUserId(5);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📅 Calendrier des Demandes Approuvées");
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);

        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setContent(scrollPane);
        dialogPane.setPrefWidth(800);
        dialogPane.setPrefHeight(600);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        afficherCalendrierAvecDemandes(demandes, dialog); // avec Dialog en paramètre

        dialog.showAndWait();
    }


    private String getCouleurParRessource(int ressourceId) {
        switch (ressourceId) {
            case 1:
                return "-fx-background-color: lightblue; -fx-border-color: darkblue;";
            case 2:
                return "-fx-background-color: lightyellow; -fx-border-color: goldenrod;";
            case 3:
                return "-fx-background-color: lightcoral; -fx-border-color: darkred;";
            default:
                return "-fx-background-color: lightgray; -fx-border-color: gray;";
        }
    }

}
