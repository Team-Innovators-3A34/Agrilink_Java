package org.example.demo.controller.backOffice.claim;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.example.demo.models.Reclamation;
import org.example.demo.services.claim.ReclamationService;
import org.example.demo.controller.backOffice.claim.RepondreReclamationController;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListClaimController {

    @FXML
    private TableView<Reclamation> claimsTable;
    @FXML
    private TableColumn<Reclamation, Number> indexColumn;
    @FXML
    private TableColumn<Reclamation, Reclamation> claimColumn;
    @FXML
    private TableColumn<Reclamation, String> etatUserColumn;
    @FXML
    private TableColumn<Reclamation, String> etatRecColumn;
    @FXML
    private TableColumn<Reclamation, String> statusColumn;
    @FXML
    private TableColumn<Reclamation, String> typeColumn;
    @FXML
    private TableColumn<Reclamation, Number> prioriteColumn;
    @FXML
    private TableColumn<Reclamation, String> dateColumn;
    @FXML
    private TableColumn<Reclamation, Void> actionsColumn;

    // Liste observable regroupant les réclamations non archivées
    private ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList();

    // Service de gestion des réclamations
    private ReclamationService reclamationService = new ReclamationService();

    // Formatter pour l'affichage de la date
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        // Forcer l'ajustement automatique des colonnes
        claimsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Initialisation de la TableView et de ses colonnes
        setupIndexColumn();
        setupClaimColumn();
        setupEtatUserColumn();
        setupEtatRecColumn();
        setupStatusColumn();
        setupTypeColumn();
        setupPrioriteColumn();
        setupDateColumn();

        // Ajout des boutons d'action (View, Répondre, Supprimer, Voir Réponses, Accepter, Refuser, Archiver)
        addActionButtonsToTable();

        // Chargement des réclamations non archivées
        loadReclamations();
    }

    // Mise en place de la colonne d'index (numérotation automatique)
    private void setupIndexColumn() {
        indexColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(claimsTable.getItems().indexOf(cellData.getValue()) + 1));
        indexColumn.setCellFactory(col -> new TableCell<Reclamation, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
    }

    // Colonne Claim : affiche l'image et le titre
    private void setupClaimColumn() {
        claimColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        claimColumn.setCellFactory(col -> new TableCell<Reclamation, Reclamation>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
            }
            @Override
            protected void updateItem(Reclamation rec, boolean empty) {
                super.updateItem(rec, empty);
                if (empty || rec == null) {
                    setGraphic(null);
                } else {
                    // Définir le chemin vers le répertoire des images (à adapter selon votre environnement)
                    String uploadsDirPath = "C:" + File.separator + "Users" + File.separator + "user" + File.separator +
                            "IdeaProjects" + File.separator + "reclamattion" + File.separator +
                            "uploads" + File.separator + "images";
                    File imgFile = new File(uploadsDirPath, rec.getImage());
                    Image img = null;
                    if (rec.getImage() != null && !rec.getImage().isEmpty() && imgFile.exists()) {
                        img = new Image(imgFile.toURI().toString());
                    } else {
                        File defaultFile = new File(uploadsDirPath, "default.png");
                        if (defaultFile.exists()) {
                            img = new Image(defaultFile.toURI().toString());
                        }
                    }
                    imageView.setImage(img);
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);
                    hbox.getChildren().addAll(imageView, new Label(rec.getTitle()));
                    setGraphic(hbox);
                }
            }
        });
    }

    // Colonne État User
    private void setupEtatUserColumn() {
        etatUserColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEtatUser()));
    }

    // Colonne État Rec
    private void setupEtatRecColumn() {
        etatRecColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEtatRec()));
    }

    // Colonne Status : création d'un badge stylisé selon le statut
    private void setupStatusColumn() {
        statusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getStatus()));
        statusColumn.setCellFactory(col -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label lbl = new Label(status);
                    lbl.getStyleClass().add("badge");
                    if ("En cours".equalsIgnoreCase(status)) {
                        lbl.getStyleClass().add("badge-warning");
                    } else if ("Résolu".equalsIgnoreCase(status) || "Terminé".equalsIgnoreCase(status)) {
                        lbl.getStyleClass().add("badge-success");
                    } else {
                        lbl.getStyleClass().add("badge-danger");
                    }
                    setGraphic(lbl);
                }
            }
        });
    }

    // Colonne Type
    private void setupTypeColumn() {
        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeNom()));
    }

    // Colonne Priorité
    private void setupPrioriteColumn() {
        prioriteColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getPriorite()));
    }

    // Colonne Date, formatée au format dd/MM/yyyy
    private void setupDateColumn() {
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDate().format(dateFormatter));
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
    }

    // Chargement des réclamations non archivées depuis le service
    private void loadReclamations() {
        List<Reclamation> list = reclamationService.afficherReclamations();
        reclamationList.setAll(list);
        claimsTable.setItems(reclamationList);
    }

    /**
     * Crée un bouton avec une icône, un style et un Tooltip.
     *
     * @param iconFile le nom du fichier icône (ex : "eye.png")
     * @param tooltip  le texte de l'infobulle
     * @param style    la classe CSS à appliquer (ex : "bg-success")
     * @return un Button configuré
     */
    private Button makeBtn(String iconFile, String tooltip, String style) {
        ImageView iv = new ImageView(loadIcon(iconFile));
        iv.setFitWidth(14);
        iv.setFitHeight(14);
        Button btn = new Button();
        btn.setGraphic(iv);
        btn.setTooltip(new Tooltip(tooltip));
        btn.getStyleClass().addAll("badge", style);
        btn.setPrefSize(20, 20);
        btn.setMinSize(20, 20);
        btn.setMaxSize(20, 20);
        return btn;
    }

    /**
     * Charge une icône depuis le dossier /icons de resources.
     *
     * @param name le nom de l'icône (ex : "eye.png")
     * @return l'image chargée ou null si introuvable
     */
    private Image loadIcon(String name) {
        InputStream is = getClass().getResourceAsStream("/icons/" + name);
        if (is == null) {
            System.err.println("Icône non trouvée : /icons/" + name);
        }
        return (is != null) ? new Image(is) : null;
    }

    /**
     * Ajoute la colonne d'actions à la TableView.
     * Les boutons affichés sont :
     * - Toujours : View, Répondre, Supprimer, Voir Réponses
     * - Si le statut est "En cours" : Accepter et Refuser
     * - Si la réclamation n'est pas archivée ("non") : Archiver
     */
    private void addActionButtonsToTable() {
        actionsColumn.setCellFactory(new Callback<TableColumn<Reclamation, Void>, TableCell<Reclamation, Void>>() {
            @Override
            public TableCell<Reclamation, Void> call(TableColumn<Reclamation, Void> param) {
                return new TableCell<Reclamation, Void>() {

                    // Création des boutons avec leurs icônes et infobulles
                    private final Button viewButton = makeBtn("view.png", "Voir détails", "badge-info");
                    private final Button answerButton = makeBtn("pencil.png", "Répondre", "bg-success");
                    private final Button deleteButton = makeBtn("trash.png", "Supprimer", "bg-warning");
                    private final Button viewAnswersButton = makeBtn("correct.png", "Voir réponses", "bg-primary");
                    private final Button acceptButton = makeBtn("check (1).png", "Accepter", "bg-success");
                    private final Button refuseButton = makeBtn("close (1).png", "Refuser", "bg-danger");
                    private final Button archiveButton = makeBtn("archive.png", "Archiver", "bg-secondary");

                    private final HBox buttonsBox = new HBox(5);

                    {
                        buttonsBox.setAlignment(Pos.CENTER);
                        buttonsBox.setPadding(new Insets(5));

                        // Action pour le bouton Voir détails
                        viewButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            try {
                                // Charger le fichier FXML de la vue détail
                                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/claim/DetailsReclamation.fxml"));
                                javafx.scene.Parent root = loader.load();

                                // Récupérer le contrôleur et lui passer la réclamation
                                org.example.demo.controller.backOffice.claim.DetailsReclamationController controller = loader.getController();
                                controller.initData(rec);

                                // Deux options possibles :
                                // Option 1 : Remplacer la racine de la scène actuelle
                                viewButton.getScene().setRoot(root);

                            } catch (java.io.IOException e) {
                                e.printStackTrace();
                            }
                        });

                        // Action pour le bouton Répondre
                        answerButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            System.out.println("Répondre à la réclamation : " + rec.getTitle());
                            try {
                                // Chargement du fichier FXML du formulaire de réponse
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/claim/RepondreReclamation.fxml"));
                                Parent root = loader.load();
                                // Récupération du contrôleur du formulaire et passage de la réclamation
                                RepondreReclamationController controller = loader.getController();
                                controller.setReclamation(rec);
                                // Remplacement de la racine de la scène par la nouvelle vue
                                answerButton.getScene().setRoot(root);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });


                        // Action pour le bouton Supprimer
                        deleteButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            System.out.println("Supprimer la réclamation : " + rec.getTitle());
                            reclamationService.supprimerReclamation(rec.getId());
                            getTableView().getItems().remove(rec);
                        });

                        // Action pour le bouton Voir réponses
                        viewAnswersButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/claim/view_answers.fxml"));
                                Parent root = loader.load();
                                // Récupération du contrôleur et passage de la réclamation
                                ViewAnswersController controller = loader.getController();
                                controller.setReclamation(rec);
                                // Ici, nous remplaçons la scène actuelle par la vue des réponses
                                viewAnswersButton.getScene().setRoot(root);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        // Action pour le bouton Accepter
                        acceptButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            System.out.println("Accepter la réclamation : " + rec.getTitle());
                            rec.setStatus("Accepter");
                            reclamationService.modifierReclamation(rec);
                            claimsTable.refresh();
                        });

                        // Action pour le bouton Refuser
                        refuseButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            System.out.println("Refuser la réclamation : " + rec.getTitle());
                            rec.setStatus("Rejeter");
                            reclamationService.modifierReclamation(rec);
                            claimsTable.refresh();
                        });

                        // Action pour le bouton Archiver
                        archiveButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            System.out.println("Archiver la réclamation : " + rec.getTitle());
                            rec.setArchive("oui");
                            reclamationService.modifierReclamation(rec);
                            // Retrait de la réclamation de la liste une fois archivée
                            getTableView().getItems().remove(rec);
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            buttonsBox.getChildren().clear();
                            // Boutons fixes toujours affichés
                            buttonsBox.getChildren().addAll(viewButton, answerButton, deleteButton, viewAnswersButton);
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            // Si le statut est "En cours", afficher les boutons Accepter et Refuser
                            if ("En cours".equalsIgnoreCase(rec.getStatus())) {
                                buttonsBox.getChildren().addAll(acceptButton, refuseButton);
                            }
                            // Si la réclamation n'est pas archivée ("non"), afficher le bouton Archiver
                            if ("non".equalsIgnoreCase(rec.isArchive())) {
                                buttonsBox.getChildren().add(archiveButton);
                            }
                            setGraphic(buttonsBox);
                        }
                    }
                };
            }
        });
    }
}
