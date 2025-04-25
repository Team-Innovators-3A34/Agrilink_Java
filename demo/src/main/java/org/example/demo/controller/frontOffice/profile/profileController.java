package org.example.demo.controller.frontOffice.profile;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.example.demo.HelloApplication;
import org.example.demo.controller.frontOffice.claim.addClaim;
import org.example.demo.controller.frontOffice.ressource.addDemandes;
import org.example.demo.models.*;
import org.example.demo.services.claim.ReclamationService;
import org.example.demo.services.recyclingpoint.recyclingpointService;
import org.example.demo.services.ressource.DemandesService;
import org.example.demo.services.ressource.RessourcesService;
import org.example.demo.utils.sessionManager;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class profileController {

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
 /* public void afficherCalendrierAvecDemandes(List<Demandes> demandes) {
        VBox conteneur = new VBox(30);
        conteneur.setPadding(new Insets(20));
        conteneur.setStyle("-fx-background-color: #1e1e1e;");

        Year anneeActuelle = Year.now();

        for (int mois = 1; mois <= 12; mois++) {
            YearMonth moisActuel = YearMonth.of(anneeActuelle.getValue(), mois);

            Label labelMois = new Label(moisActuel.getMonth().toString());
            labelMois.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

            GridPane calendrier = new GridPane();
            calendrier.setHgap(10);
            calendrier.setVgap(10);
            calendrier.setPadding(new Insets(10));
            calendrier.setStyle("-fx-background-color: white; -fx-border-color: #ced4da; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.2, 0, 2);");

            // En-tête jours
            String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
            for (int i = 0; i < jours.length; i++) {
                Label jourLabel = new Label(jours[i]);
                jourLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #495057;");
                StackPane headerCell = new StackPane(jourLabel);
                headerCell.setPrefSize(60, 30);
                headerCell.setStyle("-fx-background-color: #dee2e6; -fx-border-radius: 5px;");
                calendrier.add(headerCell, i, 0);
            }

            LocalDate premierJour = moisActuel.atDay(1);
            int decalage = premierJour.getDayOfWeek().getValue();
            int joursDansMois = moisActuel.lengthOfMonth();

            int ligne = 1;
            int colonne = decalage - 1;

            for (int jour = 1; jour <= joursDansMois; jour++) {
                LocalDate dateCourante = moisActuel.atDay(jour);

                Label jourLabel = new Label(String.valueOf(jour));
                jourLabel.setStyle("-fx-text-fill: #212529;");
                jourLabel.setPadding(new Insets(5));

                StackPane cellule = new StackPane(jourLabel);
                cellule.setPrefSize(60, 40);
                cellule.setStyle("-fx-background-color: #f1f3f5; -fx-border-color: #dee2e6; -fx-background-radius: 5px;");
                cellule.setOnMouseEntered(e -> cellule.setStyle(cellule.getStyle() + "-fx-background-color: #e9ecef;"));
                cellule.setOnMouseExited(e -> cellule.setStyle(cellule.getStyle().replace("-fx-background-color: #e9ecef;", "-fx-background-color: #f1f3f5;")));

                for (Demandes demande : demandes) {
                    if ((dateCourante.isEqual(demande.getCreatedAt()) || dateCourante.isAfter(demande.getCreatedAt())) &&
                            (dateCourante.isEqual(demande.getExpireDate()) || dateCourante.isBefore(demande.getExpireDate()))) {

                        String couleur = getCouleurParRessource(demande.getRessourceId());
                        cellule.setStyle(couleur + " -fx-background-radius: 6px; -fx-border-radius: 6px;");
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
        scrollPane.setStyle("-fx-background: #e9ecef;");

        Scene scene = new Scene(scrollPane, 800, 600);
        Stage stage = new Stage();
        stage.setTitle("Calendrier Annuel des Demandes Approuvées");
        stage.setScene(scene);
        stage.show();
    }*/

/*public void afficherCalendrierAnnuelAvecDemandes(List<Demandes> demandes) {
    VBox conteneur = new VBox(20);
    conteneur.setPadding(new Insets(15));

    Year anneeActuelle = Year.now();

    for (int mois = 1; mois <= 12; mois++) {
        YearMonth moisActuel = YearMonth.of(anneeActuelle.getValue(), mois);

        Label labelMois = new Label(moisActuel.getMonth().toString());
        labelMois.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane calendrier = new GridPane();
        calendrier.setHgap(10);
        calendrier.setVgap(10);
        calendrier.setPadding(new Insets(10));

        // En-tête jours
        String[] jours = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (int i = 0; i < jours.length; i++) {
            calendrier.add(new Label(jours[i]), i, 0);
        }

        LocalDate premierJour = moisActuel.atDay(1);
        int decalage = premierJour.getDayOfWeek().getValue();
        int joursDansMois = moisActuel.lengthOfMonth();

        int ligne = 1;
        int colonne = decalage - 1;

        for (int jour = 1; jour <= joursDansMois; jour++) {
            LocalDate dateCourante = moisActuel.atDay(jour);

            Label jourLabel = new Label(String.valueOf(jour));
            jourLabel.setPadding(new Insets(5));
            StackPane cellule = new StackPane(jourLabel);
            cellule.setPrefSize(60, 40);
            cellule.setStyle("-fx-border-color: lightgrey;");

            for (Demandes demande : demandes) {
                if ((dateCourante.isEqual(demande.getCreatedAt()) || dateCourante.isAfter(demande.getCreatedAt())) &&
                        (dateCourante.isEqual(demande.getExpireDate()) || dateCourante.isBefore(demande.getExpireDate()))) {

                    String couleur = getCouleurParRessource(demande.getRessourceId());
                    cellule.setStyle(couleur);
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

    // Affichage dans une nouvelle fenêtre avec ScrollPane
    ScrollPane scrollPane = new ScrollPane(conteneur);
    scrollPane.setFitToWidth(true);
    Scene scene = new Scene(scrollPane, 700, 600);
    Stage stage = new Stage();
    stage.setTitle("Calendrier Annuel des Demandes Approuvées");
    stage.setScene(scene);
    stage.show();
}
*/



    User user = new User();
    RessourcesService ressourcesService = new RessourcesService();
    ReclamationService reclamationService = new ReclamationService();
    recyclingpointService recyclingpointservice =new recyclingpointService();


    @FXML
    public void initialize() {
        user = sessionManager.getInstance().getUser();
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
       List<Ressources> ressources = ressourcesService.rechercher();
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

    public void addPointClicked(){
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/pointRecyclage/addRecyclingPoint.fxml");
    }

}
