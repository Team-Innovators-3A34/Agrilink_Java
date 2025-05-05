package org.example.demo.controller.backOffice.claim;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.demo.models.Reclamation;
import org.example.demo.services.claim.ReclamationService;

import java.io.File;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListClaimArchiveController {

    @FXML private TableView<Reclamation> claimsTable;
    @FXML private TableColumn<Reclamation, Number> indexColumn;
    @FXML private TableColumn<Reclamation, Reclamation> claimColumn;
    @FXML private TableColumn<Reclamation, String> etatUserColumn;
    @FXML private TableColumn<Reclamation, String> etatRecColumn;
    @FXML private TableColumn<Reclamation, String> statusColumn;
    @FXML private TableColumn<Reclamation, String> typeColumn;
    @FXML private TableColumn<Reclamation, Number> prioriteColumn;
    @FXML private TableColumn<Reclamation, String> dateColumn;
    @FXML private TableColumn<Reclamation, Void> actionsColumn;

    // Liste observable regroupant les réclamations archivées
    private ObservableList<Reclamation> reclamationList = FXCollections.observableArrayList();

    // Service de gestion des réclamations
    private ReclamationService reclamationService = new ReclamationService();

    // Formatter pour l'affichage de la date
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        claimsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupIndexColumn();
        setupClaimColumn();
        setupEtatUserColumn();
        setupEtatRecColumn();
        setupStatusColumn();
        setupTypeColumn();
        setupPrioriteColumn();
        setupDateColumn();
        addActionButtonsToTable();
        loadArchivedReclamations();
    }

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
                    // Chemin vers le répertoire des images (à adapter)
                    String uploadsDirPath = "C:" + File.separator + "Users" + File.separator + "user"
                            + File.separator + "IdeaProjects" + File.separator + "reclamattion"
                            + File.separator + "uploads" + File.separator + "images";
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

    private void setupEtatUserColumn() {
        etatUserColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEtatUser()));
    }

    private void setupEtatRecColumn() {
        etatRecColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEtatRec()));
    }

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

    private void setupTypeColumn() {
        typeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTypeNom()));
    }

    private void setupPrioriteColumn() {
        prioriteColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getPriorite()));
    }

    private void setupDateColumn() {
        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new SimpleStringProperty(cellData.getValue().getDate().format(dateFormatter));
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
    }

    private void loadArchivedReclamations() {
        // Appel au service pour récupérer les réclamations archivées
        List<Reclamation> list = reclamationService.afficherReclamationsArchive();
        reclamationList.setAll(list);
        claimsTable.setItems(reclamationList);
    }

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

    private Image loadIcon(String name) {
        InputStream is = getClass().getResourceAsStream("/icons/" + name);
        if (is == null) {
            System.err.println("Icône non trouvée : /icons/" + name);
        }
        return (is != null) ? new Image(is) : null;
    }

    private void addActionButtonsToTable() {
        actionsColumn.setCellFactory(new Callback<TableColumn<Reclamation, Void>, TableCell<Reclamation, Void>>() {
            @Override
            public TableCell<Reclamation, Void> call(TableColumn<Reclamation, Void> param) {
                return new TableCell<Reclamation, Void>() {

                    private final Button viewButton = makeBtn("eye.png", "Voir détails", "badge-info");
                    private final Button answerButton = makeBtn("pencil.png", "Répondre", "bg-success");
                    private final Button deleteButton = makeBtn("trash.png", "Supprimer", "bg-warning");
                    private final Button viewAnswersButton = makeBtn("answers.png", "Voir réponses", "bg-primary");
                    // Pour les réclamations archivées, les boutons Accepter/Refuser peuvent être masqués ou avoir une autre logique.
                    // Ici, nous affichons uniquement un bouton "Désarchiver"
                    private final Button disarchiveButton = makeBtn("archive.png", "Désarchiver", "bg-secondary");

                    private final HBox buttonsBox = new HBox(5);

                    {
                        buttonsBox.setAlignment(Pos.CENTER);
                        buttonsBox.setPadding(new Insets(5));

                        viewButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            System.out.println("Voir la réclamation : " + rec.getTitle());
                            // Ajoutez ici votre logique pour afficher les détails
                        });

                        answerButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            System.out.println("Répondre à la réclamation : " + rec.getTitle());
                            // Ajoutez ici votre logique pour répondre
                        });

                        deleteButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            System.out.println("Supprimer la réclamation : " + rec.getTitle());
                            reclamationService.supprimerReclamation(rec.getId());
                            getTableView().getItems().remove(rec);
                            claimsTable.refresh();
                        });

                        viewAnswersButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            System.out.println("Voir réponses pour : " + rec.getTitle());
                            // Ajoutez ici votre logique pour afficher les réponses
                        });

                        disarchiveButton.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            System.out.println("Désarchiver la réclamation : " + rec.getTitle());
                            rec.setArchive("non");
                            reclamationService.modifierReclamation(rec);
                            // Retirer l'élément de la liste si vous souhaitez le masquer de la vue archive
                            getTableView().getItems().remove(rec);
                            claimsTable.refresh();
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            buttonsBox.getChildren().clear();
                            buttonsBox.getChildren().addAll(viewButton, answerButton, deleteButton, viewAnswersButton);
                            // Ici, pour les éléments archivés, nous affichons uniquement le bouton Désarchiver
                            buttonsBox.getChildren().add(disarchiveButton);
                            setGraphic(buttonsBox);
                        }
                    }
                };
            }
        });
    }
}
