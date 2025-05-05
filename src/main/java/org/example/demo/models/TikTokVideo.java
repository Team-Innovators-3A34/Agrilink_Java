// TikTokVideo.java
package org.example.demo.models;

public class TikTokVideo {
    private String id;
    private String username;
    private String title;
    private String embedUrl;
    private String cover;    // URL de l'image d'aperçu
    private String videoUrl; // URL directe de la vidéo

    public TikTokVideo(String id, String username, String title, String embedUrl, String cover, String videoUrl) {
        this.id = id;
        this.username = username;
        this.title = title;
        this.embedUrl = embedUrl;
        this.cover = cover;
        this.videoUrl = videoUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getTitle() { return title; }
    public String getEmbedUrl() { return embedUrl; }
    public String getCover() { return cover; }
    public String getVideoUrl() { return videoUrl; }

    // Vous pouvez ajouter des setters si nécessaire.
}
