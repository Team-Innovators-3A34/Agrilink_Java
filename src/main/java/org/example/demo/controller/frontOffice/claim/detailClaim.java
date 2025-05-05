package org.example.demo.controller.frontOffice.claim;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.example.demo.HelloApplication;
import org.example.demo.models.Reclamation;
import org.example.demo.models.Reponses;
import org.example.demo.models.TypeRec;
import org.example.demo.services.claim.ReponsesService;
import org.example.demo.services.claim.TypeRecService;

public class detailClaim {

    @FXML
    private Text contenutxt;

    @FXML
    private Text datetxt;

    @FXML
    private Text emailtxt;

    @FXML
    private ImageView iamge;

    @FXML
    private Text nomtxt;

    @FXML
    private Text prioritetxt;

    @FXML
    private Text typetxt;

    // ListView pour afficher les réponses
    @FXML
    private ListView<Reponses> responsesListView;

    // Bouton "Retour"
    @FXML
    private Button btnRetour;

    // Services
    private ReponsesService responsesService = new ReponsesService();
    private TypeRecService typeRecService = new TypeRecService();

    /**
     * Remplit les informations de la réclamation et charge les réponses associées.
     * @param rec La réclamation à afficher.
     */
    public void setReclamation(Reclamation rec) {
        contenutxt.setText(rec.getContent());
        datetxt.setText(rec.getDate().toString());
        emailtxt.setText(rec.getMailUser());
        nomtxt.setText(rec.getNomUser());
        prioritetxt.setText(String.valueOf(rec.getPriorite()));

        // Récupération de l'objet TypeRec et affichage de son nom
        TypeRec typeRec = typeRecService.getTypeRecById(rec.getType());
        if (typeRec != null) {
            typetxt.setText(typeRec.getNom());
        } else {
            typetxt.setText("Non spécifié");
        }

        // Chargement de l'image depuis le dossier /images/
        String imagePath = "/images/" + rec.getImage();
        try {
            Image image = new Image(getClass().getResource(imagePath).toString());
            iamge.setImage(image);
        } catch (NullPointerException e) {
            System.out.println("Image profile not found");
        }

        // Chargement des réponses associées à la réclamation
        loadResponses(rec.getId());
    }

    /**
     * Charge et affiche les réponses associées à la réclamation dans la ListView.
     * Chaque cellule affiche le numéro de réponse (en bleu) et des titres en gras pour
     * les propriétés Contenu, Solution et Statut.
     * @param reclamationId L'identifiant de la réclamation.
     */
    private void loadResponses(int reclamationId) {
        var responses = responsesService.getAnswersByReclamation(reclamationId);
        responsesListView.setItems(FXCollections.observableArrayList(responses));

        responsesListView.setCellFactory(param -> new ListCell<Reponses>() {
            @Override
            protected void updateItem(Reponses response, boolean empty) {
                super.updateItem(response, empty);
                if (empty || response == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);
                    vbox.setPadding(new Insets(5));

                    // Numérotation de la réponse en bleu
                    Label responseTitle = new Label("Réponse " + (getIndex() + 1));
                    responseTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: blue; -fx-font-size: 12px;");

                    // Création des étiquettes pour chaque propriété (titre en gras suivi de la valeur)
                    HBox contentBox = new HBox(5);
                    Label contentLabel = new Label("Contenu :");
                    contentLabel.setStyle("-fx-font-weight: bold;");
                    Label contentValue = new Label(response.getContent());
                    contentBox.getChildren().addAll(contentLabel, contentValue);

                    HBox solutionBox = new HBox(5);
                    Label solutionLabel = new Label("Solution :");
                    solutionLabel.setStyle("-fx-font-weight: bold;");
                    Label solutionValue = new Label(response.getSolution());
                    solutionBox.getChildren().addAll(solutionLabel, solutionValue);

                    HBox statusBox = new HBox(5);
                    Label statusLabel = new Label("Statut :");
                    statusLabel.setStyle("-fx-font-weight: bold;");
                    Label statusValue = new Label(response.getStatus());
                    statusBox.getChildren().addAll(statusLabel, statusValue);

                    vbox.getChildren().addAll(responseTitle, contentBox, solutionBox, statusBox);

                    // Affichage des boutons si le statut est "En attente"
                    if ("En attente".equalsIgnoreCase(response.getStatus())) {
                        HBox buttonsBox = new HBox(10);
                        buttonsBox.setAlignment(Pos.CENTER_LEFT);

                        Button satisfaitBtn = new Button("✅ Satisfait");
                        satisfaitBtn.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 4 2 4;");
                        satisfaitBtn.setPrefWidth(20);
                        satisfaitBtn.setPrefHeight(20);

                        Button nonSatisfaitBtn = new Button("❌ Non satisfait");
                        nonSatisfaitBtn.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 2 4 2 4;");
                        nonSatisfaitBtn.setPrefWidth(20);
                        nonSatisfaitBtn.setPrefHeight(20);

                        satisfaitBtn.setOnAction(e -> updateResponseStatus(response, "Satisfait"));
                        nonSatisfaitBtn.setOnAction(e -> updateResponseStatus(response, "Non satisfait"));

                        buttonsBox.getChildren().addAll(satisfaitBtn, nonSatisfaitBtn);
                        vbox.getChildren().add(buttonsBox);
                    }
                    setGraphic(vbox);
                }
            }
        });
    }

    /**
     * Met à jour le statut d'une réponse et rafraîchit l'affichage.
     * @param response La réponse concernée.
     * @param newStatus Le nouveau statut ("Satisfait" ou "Non satisfait").
     */
    private void updateResponseStatus(Reponses response, String newStatus) {
        response.setStatus(newStatus);
        boolean success = responsesService.modifierReponse(response);
        if (success) {
            responsesListView.refresh();
        }
    }

    /**
     * Méthode de retour à la scène précédente.
     */
    @FXML
    public void backToDetail() {
        HelloApplication.changeScene("/org/example/demo/fxml/Frontoffice/profile/profile.fxml");
    }
}
