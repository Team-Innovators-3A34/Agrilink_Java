package org.example.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.util.Callback;
import org.example.entities.Reclamation;
import org.example.services.ReclamationService;
import org.example.services.TranslationService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

import static java.io.File.separator;

public class DashboardController implements Initializable {

    // Racine de l'interface (BorderPane principal)
    @FXML private BorderPane root;

    // KPIs
    @FXML private Label totalReclamationsLabel;
    @FXML private Label enCoursLabel;
    @FXML private Label resoluesLabel;
    @FXML private Label satisfactionLabel;
    @FXML private Label tempsTraitementLabel;

    // Filtres
    @FXML private ComboBox<String> periodeComboBox;
    @FXML private ComboBox<String> statutComboBox;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> regionComboBox;

    // Graphiques
    @FXML private BarChart<String, Number> statutChart;
    @FXML private LineChart<String, Number> evolutionChart;
    @FXML private PieChart categorieChart;
    @FXML private AreaChart<String, Number> delaiChart;

    // TableView pour afficher les réclamations
    @FXML private TableView<Reclamation> reclamationsTableView;

    // VBox pour le menu réclamation (sidebar)
    @FXML private Node reclamationMenu;

    // Instance du service et données
    private ReclamationService reclamationService = new ReclamationService();
    private ObservableList<Reclamation> reclamationsData = FXCollections.observableArrayList();

    // Formatter pour la date
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Sauvegarde de la vue centrale d'origine afin de pouvoir y revenir
    private Node originalCenter;

    // Mode d'affichage : false = réclamations non archivées, true = archivées
    private boolean archiveView = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Sauvegarder le contenu central initial (tableau et graphiques)
        originalCenter = root.getCenter();

        setupFilters();
        setupChartsEmpty();
        setupTableView();
        loadReclamationsFromDatabase();
    }

    private void setupFilters() {
        periodeComboBox.setItems(FXCollections.observableArrayList(
                "Aujourd'hui", "Cette semaine", "Ce mois", "Année en cours"
        ));
        statutComboBox.setItems(FXCollections.observableArrayList(
                "Nouveau", "En cours", "Résolu", "Fermé"
        ));
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Technique", "Facturation", "Livraison", "Autre"
        ));
        regionComboBox.setItems(FXCollections.observableArrayList(
                "Nord", "Sud", "Est", "Ouest"
        ));
    }

    // Mise à jour des graphiques en fonction des données de la base
    private void updateCharts() {
        // BarChart : Nombre de réclamations par statut
        statutChart.getData().clear();
        Map<String, Integer> statusCounts = new HashMap<>();
        for (Reclamation r : reclamationsData) {
            String status = r.getStatus();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }
        XYChart.Series<String, Number> statusSeries = new XYChart.Series<>();
        statusSeries.setName("Réclamations");
        for (String status : statusCounts.keySet()) {
            statusSeries.getData().add(new XYChart.Data<>(status, statusCounts.get(status)));
        }
        statutChart.getData().add(statusSeries);

        // LineChart : Evolution des réclamations par mois
        evolutionChart.getData().clear();
        Map<String, Integer> monthCounts = new TreeMap<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM");
        for (Reclamation r : reclamationsData) {
            String month = r.getDate().format(monthFormatter);
            monthCounts.put(month, monthCounts.getOrDefault(month, 0) + 1);
        }
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        lineSeries.setName("Évolution");
        for (String month : monthCounts.keySet()) {
            lineSeries.getData().add(new XYChart.Data<>(month, monthCounts.get(month)));
        }
        evolutionChart.getData().add(lineSeries);

        // PieChart : Distribution des réclamations par type
        Map<String, Integer> typeCounts = new HashMap<>();
        for (Reclamation r : reclamationsData) {
            String type = String.valueOf(r.getType());
            typeCounts.put(type, typeCounts.getOrDefault(type, 0) + 1);
        }
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (String type : typeCounts.keySet()) {
            pieData.add(new PieChart.Data(type, typeCounts.get(type)));
        }
        categorieChart.setData(pieData);

        // AreaChart : Délai moyen (en heures) de traitement par mois
        delaiChart.getData().clear();
        Map<String, List<Long>> monthDelays = new HashMap<>();
        for (Reclamation r : reclamationsData) {
            String month = r.getDate().format(monthFormatter);
            long delayHours = Duration.between(r.getDate(), java.time.LocalDateTime.now()).toHours();
            monthDelays.computeIfAbsent(month, k -> new ArrayList<>()).add(delayHours);
        }
        XYChart.Series<String, Number> areaSeries = new XYChart.Series<>();
        areaSeries.setName("Délais");
        for (String month : monthDelays.keySet()) {
            List<Long> delays = monthDelays.get(month);
            long sum = 0;
            for (Long d : delays) {
                sum += d;
            }
            long avg = delays.size() > 0 ? sum / delays.size() : 0;
            areaSeries.getData().add(new XYChart.Data<>(month, avg));
        }
        delaiChart.getData().add(areaSeries);
    }

    private void setupChartsEmpty() {
        // Initialisation des graphiques avec des données vides
        statutChart.getData().clear();
        evolutionChart.getData().clear();
        categorieChart.setData(FXCollections.observableArrayList());
        delaiChart.getData().clear();
    }

    private void setupTableView() {
        reclamationsTableView.setItems(reclamationsData);

        // Colonne 1 : Claim (Image + Titre)
        TableColumn<Reclamation, Reclamation> claimCol = new TableColumn<>("Claim");
        claimCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        claimCol.setCellFactory(param -> new TableCell<Reclamation, Reclamation>() {
            private final HBox container = new HBox(5);
            private final ImageView imageView = new ImageView();
            private final Label titleLabel = new Label();
            {
                imageView.setFitWidth(50);
                imageView.setFitHeight(50);
                container.getChildren().addAll(imageView, titleLabel);
            }
            @Override
            protected void updateItem(Reclamation rec, boolean empty) {
                super.updateItem(rec, empty);
                if (empty || rec == null) {
                    setGraphic(null);
                } else {
                    // Ici, on utilise le même chemin absolu utilisé lors de l'upload
                    String uploadsDirPath = "C:" + separator + "Users" + separator + "user" + separator +
                            "IdeaProjects" + separator + "reclamattion" + separator + "uploads" + separator + "images";
                    if (rec.getImage() != null && !rec.getImage().isEmpty()) {
                        File imgFile = new File(uploadsDirPath, rec.getImage());
                        if (imgFile.exists()) {
                            imageView.setImage(new Image(imgFile.toURI().toString()));
                        } else {
                            // Si l'image n'existe pas, on affiche l'image par défaut
                            File defaultFile = new File(uploadsDirPath, "default.png");
                            if (defaultFile.exists()) {
                                imageView.setImage(new Image(defaultFile.toURI().toString()));
                            }
                        }
                    } else {
                        File defaultFile = new File(uploadsDirPath, "default.png");
                        if (defaultFile.exists()) {
                            imageView.setImage(new Image(defaultFile.toURI().toString()));
                        }
                    }
                    titleLabel.setText(rec.getTitle());
                    setGraphic(container);
                }
            }
        });
        claimCol.setPrefWidth(200);

        // Colonne 2 : État User
        TableColumn<Reclamation, String> etatUserCol = new TableColumn<>("État User");
        etatUserCol.setCellValueFactory(new PropertyValueFactory<>("etatUser"));
        etatUserCol.setPrefWidth(100);

        // Colonne 3 : État Rec
        TableColumn<Reclamation, String> etatRecCol = new TableColumn<>("État Rec");
        etatRecCol.setCellValueFactory(new PropertyValueFactory<>("etatRec"));
        etatRecCol.setPrefWidth(100);

        // Colonne 4 : Statut
        TableColumn<Reclamation, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statutCol.setCellFactory(col -> new TableCell<Reclamation, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if ("En cours".equalsIgnoreCase(status)) {
                        setStyle("-fx-background-color: orange; -fx-text-fill: white;");
                    } else if ("Résolu".equalsIgnoreCase(status)) {
                        setStyle("-fx-background-color: green; -fx-text-fill: white;");
                    } else if ("Accepter".equalsIgnoreCase(status)) {
                        setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                    } else if ("Rejeter".equalsIgnoreCase(status)) {
                        setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
                    } else {
                        setStyle("-fx-background-color: red; -fx-text-fill: white;");
                    }
                }
            }
        });
        statutCol.setPrefWidth(100);

        // Colonne 5 : Type
        TableColumn<Reclamation, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeNom"));
        typeCol.setPrefWidth(150);

        // Colonne 6 : Priorité
        TableColumn<Reclamation, String> prioriteCol = new TableColumn<>("Priorité");
        prioriteCol.setCellValueFactory(new PropertyValueFactory<>("priorite"));
        prioriteCol.setPrefWidth(80);

        // Colonne 7 : Date (formatée)
        TableColumn<Reclamation, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDate().format(dateFormatter));
            } else {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        dateCol.setPrefWidth(100);

        // Colonne 8 : Actions (boutons)
        TableColumn<Reclamation, Reclamation> actionCol = new TableColumn<>("Action");
        actionCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));
        actionCol.setCellFactory(new Callback<TableColumn<Reclamation, Reclamation>, TableCell<Reclamation, Reclamation>>() {
            @Override
            public TableCell<Reclamation, Reclamation> call(TableColumn<Reclamation, Reclamation> param) {
                return new TableCell<Reclamation, Reclamation>() {
                    // Boutons d'actions communs
                    private final Button viewBtn = new Button("View");
                    private final Button acceptBtn = new Button("Accepter");
                    private final Button rejectBtn = new Button("Rejeter");
                    private final Button answerBtn = new Button("Answer");
                    private final Button viewAnswerBtn = new Button("View Answer");
                    private final Button deleteBtn = new Button("Delete");
                    // Boutons spécifiques en fonction du mode
                    private final Button archiverBtn = new Button("Archiver");
                    private final Button disarchiverBtn = new Button("Désarchiver");
                    private final HBox container = new HBox(5);
                    {
                        container.setAlignment(Pos.CENTER);
                        // Action pour le bouton View
                        viewBtn.setOnAction(event -> {
                            // Récupère la réclamation associée à la ligne du bouton
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/detailsReclamation.fxml"));
                                Parent detailsRoot = loader.load();
                                DetailsReclamationController detailsController = loader.getController();

                                // La logique de traduction est désormais gérée dans la vue détail
                                detailsController.initData(rec, DashboardController.this);

                                // Place la vue des détails dans la zone centrale du BorderPane du Dashboard
                                root.setCenter(detailsRoot);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

                        // Action pour le bouton Accepter
                        acceptBtn.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            rec.setStatus("Accepter");
                            reclamationService.modifierReclamation(rec);
                            reclamationsTableView.refresh();
                            updateCharts();
                        });
                        // Action pour le bouton Rejeter
                        rejectBtn.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            rec.setStatus("Rejeter");
                            reclamationService.modifierReclamation(rec);
                            reclamationsTableView.refresh();
                            updateCharts();
                        });
                        // Action pour le bouton Answer
                        answerBtn.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterReponse.fxml"));
                                Parent answerRoot = loader.load();
                                RepondreReclamationController answerController = loader.getController();
                                answerController.setReclamation(rec);
                                answerController.setDashboardController(DashboardController.this);
                                root.setCenter(answerRoot);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        // Action pour le bouton View Answer
                        viewAnswerBtn.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            try {
                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/viewAnswers.fxml"));
                                Parent viewAnswersRoot = loader.load();
                                ViewAnswersController viewAnswersController = loader.getController();
                                viewAnswersController.setReclamation(rec);
                                viewAnswersController.setDashboardController(DashboardController.this);
                                root.setCenter(viewAnswersRoot);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        // Action pour le bouton Delete
                        deleteBtn.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            reclamationService.supprimerReclamation(rec.getId());
                            reclamationsData.remove(rec);
                        });
                        // Action pour le bouton Archiver (mode non archivées)
                        archiverBtn.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            rec.setArchive("oui");
                            reclamationService.modifierReclamation(rec);
                            reclamationsData.remove(rec);
                            updateCharts();
                        });
                        // Action pour le bouton Désarchiver (mode archivées)
                        disarchiverBtn.setOnAction(event -> {
                            Reclamation rec = getTableView().getItems().get(getIndex());
                            rec.setArchive("non");
                            reclamationService.modifierReclamation(rec);
                            reclamationsData.remove(rec);
                            updateCharts();
                        });
                    }
                    @Override
                    protected void updateItem(Reclamation rec, boolean empty) {
                        super.updateItem(rec, empty);
                        if (empty || rec == null) {
                            setGraphic(null);
                        } else {
                            container.getChildren().clear();
                            container.getChildren().add(viewBtn);
                            if ("En cours".equalsIgnoreCase(rec.getStatus())) {
                                container.getChildren().addAll(acceptBtn, rejectBtn);
                            }
                            container.getChildren().addAll(answerBtn, viewAnswerBtn, deleteBtn);
                            // Afficher soit le bouton Archiver, soit le bouton Désarchiver selon le mode
                            if (!archiveView && "non".equalsIgnoreCase(rec.isArchive())) {
                                container.getChildren().add(0, archiverBtn);
                            } else if (archiveView && "oui".equalsIgnoreCase(rec.isArchive())) {
                                container.getChildren().add(0, disarchiverBtn);
                            }
                            setGraphic(container);
                        }
                    }
                };
            }
        });
        actionCol.setPrefWidth(220);

        reclamationsTableView.getColumns().setAll(
                claimCol, etatUserCol, etatRecCol, statutCol, typeCol, prioriteCol, dateCol, actionCol
        );
    }

    // Charge les réclamations en fonction du mode d'affichage et met à jour les statistiques
    private void loadReclamationsFromDatabase() {
        reclamationsData.clear();
        List<Reclamation> list;
        if (!archiveView) {
            list = reclamationService.afficherReclamations();
        } else {
            list = reclamationService.afficherReclamationsArchive();
        }
        reclamationsData.addAll(list);
        totalReclamationsLabel.setText(String.valueOf(list.size()));
        updateCharts();
    }

    @FXML
    private void appliquerFiltres() {
        System.out.println("Filtres appliqués");
    }

    @FXML
    private void toggleReclamationMenu() {
        boolean isVisible = reclamationMenu.isVisible();
        reclamationMenu.setVisible(!isVisible);
        reclamationMenu.setManaged(!isVisible);
    }

    // Bouton du sidebar pour afficher les réclamations non archivées
    @FXML
    private void afficherListeReclamation() {
        archiveView = false;
        loadReclamationsFromDatabase();
        restoreCenter();
    }

    // Bouton du sidebar pour afficher les réclamations archivées
    @FXML
    private void afficherListeArchive() {
        archiveView = true;
        loadReclamationsFromDatabase();
        restoreCenter();
    }

    // Bouton du sidebar pour afficher le formulaire d'ajout d'une catégorie
    @FXML
    private void afficherAddCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ajouterCategorie.fxml"));
            Parent addCategorieRoot = loader.load();
            // Récupérer le contrôleur et lui transmettre la racine pour pouvoir revenir en arrière
            AddCategorieController controller = loader.getController();
            controller.setRoot(root);
            root.setCenter(addCategorieRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Méthode pour restaurer la vue centrale initiale (tableau et graphiques)
    public void restoreCenter() {
        root.setCenter(originalCenter);
    }

    public BorderPane getRoot() {
        return root;
    }

    @FXML
    private void afficherListeCategorie() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/listCategorie.fxml"));
            Parent listCategorieRoot = loader.load();
            root.setCenter(listCategorieRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
