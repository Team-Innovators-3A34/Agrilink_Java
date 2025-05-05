package org.example.demo.controller.frontOffice.claim;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import org.example.demo.models.TikTokVideo;
import org.example.demo.services.claim.TikTokAPIService;

import java.util.List;

public class TikTokVideoViewerController {

    @FXML
    private AnchorPane prevPreview;
    @FXML
    private AnchorPane mainVideoPane;
    @FXML
    private AnchorPane nextPreview;
    @FXML
    private MediaView mainMediaView;
    @FXML
    private Label videoTitleLabel;
    @FXML
    private Label videoDescriptionLabel;
    @FXML
    private ImageView prevThumbnail;
    @FXML
    private ImageView nextThumbnail;
    @FXML
    private ImageView rec1;
    @FXML
    private ImageView rec2;
    @FXML
    private ImageView rec3;
    @FXML
    private ImageView rec4;
    @FXML
    private ImageView rec5;
    @FXML
    private ImageView rec6;
    @FXML
    private ImageView rec7;

    private TikTokAPIService apiService = new TikTokAPIService();
    private List<TikTokVideo> videos;
    private List<TikTokVideo> recommendedVideos;
    private int currentIndex = 0;
    private MediaPlayer mediaPlayer;

    @FXML
    private void initialize() {
        // Appel à l'API pour récupérer 5 vidéos à partir du curseur 0
        videos = apiService.getAgricultureVideos(5, 0);
        if (videos != null && !videos.isEmpty()) {
            loadVideo(currentIndex);
            loadRecommendations();
        } else {
            Label noVideo = new Label("Aucune vidéo trouvée.");
            mainVideoPane.getChildren().add(noVideo);
        }
    }

    /**
     * Charge les vidéos recommandées pour affichage dans la section recommandation
     */
    private void loadRecommendations() {
        // Récupérer des vidéos supplémentaires pour les recommandations
        recommendedVideos = apiService.getAgricultureVideos(7, 5);

        // Si des vidéos sont trouvées, les afficher dans les vignettes de recommandation
        if (recommendedVideos != null && !recommendedVideos.isEmpty()) {
            ImageView[] recommendationViews = {rec1, rec2, rec3, rec4, rec5, rec6, rec7};

            for (int i = 0; i < Math.min(recommendedVideos.size(), recommendationViews.length); i++) {
                TikTokVideo video = recommendedVideos.get(i);
                if (video.getCover() != null && !video.getCover().isEmpty()) {
                    try {
                        recommendationViews[i].setImage(new Image(video.getCover()));
                    } catch (Exception e) {
                        System.err.println("Erreur lors du chargement de l'image de recommandation : " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Charge la vidéo courante et met à jour les aperçus.
     */
    private void loadVideo(int index) {
        if (videos == null || videos.isEmpty()) {
            return;
        }
        currentIndex = index;
        TikTokVideo currentVideo = videos.get(currentIndex);

        // Mise à jour des labels d'information
        if (videoTitleLabel != null) {
            videoTitleLabel.setText(currentVideo.getTitle());
        }
        if (videoDescriptionLabel != null) {
            videoDescriptionLabel.setText("Vidéo partagée par @" + currentVideo.getUsername());
        }

        // Préparation du média et du MediaPlayer pour la vidéo principale
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        try {
            Media media = new Media(currentVideo.getVideoUrl());
            mediaPlayer = new MediaPlayer(media);
            mainMediaView.setMediaPlayer(mediaPlayer);
            mediaPlayer.setAutoPlay(true);
        } catch (Exception e) {
            Label errorLabel = new Label("Erreur lors de la lecture de la vidéo.");
            mainVideoPane.getChildren().clear();
            mainVideoPane.getChildren().add(errorLabel);
            System.err.println("Erreur de lecture : " + e.getMessage());
        }

        // Mise à jour de la zone de prévisualisation précédente
        prevPreview.getChildren().clear();
        if (currentIndex > 0) {
            TikTokVideo prevVideo = videos.get(currentIndex - 1);
            if (prevThumbnail != null && prevVideo.getCover() != null) {
                try {
                    prevThumbnail.setImage(new Image(prevVideo.getCover()));
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image précédente : " + e.getMessage());
                }
            } else {
                Label noPrev = new Label("Aucune vidéo précédente");
                prevPreview.getChildren().add(noPrev);
            }
        } else {
            Label noPrev = new Label("Aucune vidéo précédente");
            prevPreview.getChildren().add(noPrev);
        }

        // Mise à jour de la zone de prévisualisation suivante
        nextPreview.getChildren().clear();
        if (currentIndex < videos.size() - 1) {
            TikTokVideo nextVideo = videos.get(currentIndex + 1);
            if (nextThumbnail != null && nextVideo.getCover() != null) {
                try {
                    nextThumbnail.setImage(new Image(nextVideo.getCover()));
                } catch (Exception e) {
                    System.err.println("Erreur lors du chargement de l'image suivante : " + e.getMessage());
                }
            } else {
                Label noNext = new Label("Aucune vidéo suivante");
                nextPreview.getChildren().add(noNext);
            }
        } else {
            Label noNext = new Label("Aucune vidéo suivante");
            nextPreview.getChildren().add(noNext);
        }
    }

    // Gestion du clic sur la zone précédente
    @FXML
    private void handlePrevClick(MouseEvent event) {
        if (currentIndex > 0) {
            loadVideo(currentIndex - 1);
        }
    }

    // Gestion du clic sur la zone suivante
    @FXML
    private void handleNextClick(MouseEvent event) {
        if (currentIndex < videos.size() - 1) {
            loadVideo(currentIndex + 1);
        }
    }

    /**
     * Méthode pour gérer les clics sur les vidéos recommandées
     */
    @FXML
    private void handleRecommendationClick(MouseEvent event) {
        // Identifier quelle recommandation a été cliquée
        AnchorPane source = (AnchorPane) event.getSource();
        int recommendationIndex = -1;

        // Déterminer l'index de la recommandation cliquée
        if (source.getChildren().contains(rec1)) recommendationIndex = 0;
        else if (source.getChildren().contains(rec2)) recommendationIndex = 1;
        else if (source.getChildren().contains(rec3)) recommendationIndex = 2;
        else if (source.getChildren().contains(rec4)) recommendationIndex = 3;
        else if (source.getChildren().contains(rec5)) recommendationIndex = 4;
        else if (source.getChildren().contains(rec6)) recommendationIndex = 5;
        else if (source.getChildren().contains(rec7)) recommendationIndex = 6;

        // Si une recommandation valide a été cliquée et que nous avons des vidéos recommandées
        if (recommendationIndex >= 0 && recommendedVideos != null
                && recommendationIndex < recommendedVideos.size()) {

            // Ajouter la vidéo recommandée à la liste principale et la charger
            TikTokVideo selectedVideo = recommendedVideos.get(recommendationIndex);
            videos.add(currentIndex + 1, selectedVideo);
            loadVideo(currentIndex + 1);
        }
    }
}