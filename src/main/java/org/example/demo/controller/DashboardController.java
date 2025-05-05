package org.example.demo.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.HBox;
import org.example.demo.models.Reclamation;
import org.example.demo.services.claim.ReclamationService;

import java.util.ArrayList;
import java.util.List;

public class DashboardController {

    @FXML
    private HBox statsContainer;

    private final ReclamationService reclamationService = new ReclamationService();

    @FXML
    public void initialize() {
        afficherStatistiques();
    }

    private void afficherStatistiques() {
        // 1) Récupérer non-archivées et archivées
        List<Reclamation> nonArchList = reclamationService.afficherReclamations();
        List<Reclamation> archList    = reclamationService.afficherReclamationsArchive();

        // 2) Fusionner en une seule liste pour le total et les statuts
        List<Reclamation> allRecs = new ArrayList<>();
        allRecs.addAll(nonArchList);
        allRecs.addAll(archList);

        int total = allRecs.size();
        int countNonArch = nonArchList.size();
        int countArch    = archList.size();

        // 3) Comptage acceptées/rejetées/en attente sur allRecs
        long countAccepted = allRecs.stream()
                .filter(r -> "accepter".equalsIgnoreCase(r.getStatus()))
                .count();
        long countRejected = allRecs.stream()
                .filter(r -> "rejeter".equalsIgnoreCase(r.getStatus()))
                .count();
        long countPending  = total - countAccepted - countRejected;

        // 4) Camembert Archivage
        PieChart pieArchive = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data("Non archivées", countNonArch),
                new PieChart.Data("Archivées",    countArch)
        ));
        pieArchive.setTitle("Archivage");

        // 5) Camembert Statut
        PieChart pieStatus = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data("Acceptées", countAccepted),
                new PieChart.Data("Rejetées",  countRejected),
                new PieChart.Data("En attente",countPending)
        ));
        pieStatus.setTitle("État des réclamations");

        // 6) Afficher dans le HBox
        statsContainer.getChildren().setAll(pieArchive, pieStatus);
    }
}
