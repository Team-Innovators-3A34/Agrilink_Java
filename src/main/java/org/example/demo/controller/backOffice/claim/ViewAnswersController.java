package org.example.demo.controller.backOffice.claim;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.example.demo.models.Reponses;
import org.example.demo.models.Reclamation;
import org.example.demo.services.claim.ReponsesService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ViewAnswersController implements Initializable {

    @FXML
    private Label reclamationTitleLabel;

    @FXML
    private TableView<Reponses> answersTableView;

    // Colonne pour la numérotation (numéro de ligne)
    @FXML
    private TableColumn<Reponses, Number> idColumn;

    @FXML
    private TableColumn<Reponses, String> contentColumn;
    @FXML
    private TableColumn<Reponses, String> statusColumn;
    @FXML
    private TableColumn<Reponses, String> dateColumn;
    @FXML
    private TableColumn<Reponses, String> solutionColumn;
    @FXML
    private TableColumn<Reponses, Void> actionColumn;

    // Service qui récupère les réponses
    private ReponsesService reponsesService = new ReponsesService();

    // La réclamation dont on souhaite afficher les réponses
    private Reclamation reclamation;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Forcer l'ajustement automatique des colonnes
        answersTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Configuration de la colonne de numérotation (affiche getIndex()+1)
        idColumn.setCellFactory(col -> new TableCell<Reponses, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });

        // Configuration des autres colonnes en utilisant les noms des propriétés
        contentColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("content"));
        statusColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("date"));
        solutionColumn.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("solution"));

        // Ajout des boutons d'action
        addActionButtons();
    }

    /**
     * Méthode permettant de transmettre la réclamation et d'actualiser l'affichage des réponses.
     *
     * @param reclamation la réclamation dont on souhaite afficher les réponses.
     */
    public void setReclamation(Reclamation reclamation) {
        this.reclamation = reclamation;
        reclamationTitleLabel.setText("Réponses pour : " + reclamation.getTitle());
        loadAnswers();
    }

    /**
     * Charge les réponses associées à la réclamation via le service et affecte une nouvelle ObservableList au TableView.
     */
    public void loadAnswers() {
        if (reclamation != null) {
            List<Reponses> list = reponsesService.getAnswersByReclamation(reclamation.getId());
            System.out.println("Nombre de réponses chargées : " + list.size());
            // Affecter une nouvelle liste observable pour forcer le rafraîchissement
            answersTableView.setItems(FXCollections.observableArrayList(list));
        }
    }

    /**
     * Méthode utilitaire pour créer un bouton avec icône, style et tooltip.
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
     * Charge une icône depuis le dossier /icons des ressources.
     */
    private Image loadIcon(String name) {
        InputStream is = getClass().getResourceAsStream("/icons/" + name);
        if (is == null) {
            System.err.println("Icône non trouvée : /icons/" + name);
        }
        return (is != null) ? new Image(is) : null;
    }

    /**
     * Ajoute une colonne d'actions au TableView avec des boutons d'édition et de suppression.
     */
    private void addActionButtons() {
        actionColumn.setCellFactory(new Callback<TableColumn<Reponses, Void>, TableCell<Reponses, Void>>() {
            @Override
            public TableCell<Reponses, Void> call(TableColumn<Reponses, Void> param) {
                return new TableCell<Reponses, Void>() {
                    private final Button editButton = makeBtn("pencil.png", "Modifier", "bg-success");
                    private final Button deleteButton = makeBtn("trash.png", "Supprimer", "bg-warning");
                    private final HBox buttonsBox = new HBox(5);

                    {
                        buttonsBox.setAlignment(Pos.CENTER);
                        buttonsBox.setPadding(new Insets(5));

                        // Action pour le bouton Modifier
                        editButton.setOnAction(event -> {
                            Reponses reponse = getTableView().getItems().get(getIndex());
                            if (reponse != null) {
                                try {
                                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/claim/editReponse.fxml"));
                                    Parent editRoot = loader.load();
                                    EditReponseController controller = loader.getController();
                                    controller.setReponse(reponse);
                                    // Remplacer la scène par la vue d'édition
                                    editButton.getScene().setRoot(editRoot);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        // Action pour le bouton Supprimer
                        deleteButton.setOnAction(event -> {
                            Reponses reponse = getTableView().getItems().get(getIndex());
                            if (reponse != null) {
                                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                                alert.setTitle("Confirmation de suppression");
                                alert.setHeaderText("Supprimer la réponse ?");
                                alert.setContentText("Voulez-vous vraiment supprimer cette réponse ?");
                                alert.showAndWait().ifPresent(response -> {
                                    if (response == ButtonType.OK) {
                                        reponsesService.supprimerReponse(reponse.getId());
                                        loadAnswers();
                                    }
                                });
                            }
                        });

                        buttonsBox.getChildren().addAll(editButton, deleteButton);
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
                };
            }
        });
    }

    /**
     * Méthode appelée lors du clic sur le bouton "Retour à la liste" pour revenir à la vue des réclamations.
     */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/demo/fxml/Backoffice/claim/ListClaim.fxml"));
            Parent root = loader.load();
            answersTableView.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
