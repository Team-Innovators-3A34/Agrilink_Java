package org.example.demo.models;

import java.util.ArrayList;
import java.util.List;

public class Posts {
    private int id;
    private int user_id_id;
    private String type;
    private String title;
    private String description;
    private String created_at;
    private String status;
    private String images;

    // Add a list to store comments
    private List<Comment> comments = new ArrayList<>();

    // Add methods to manage comments
    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public void addComment(Comment comment) {
        comment.setPost(this);  // Set the bidirectional relationship
        this.comments.add(comment);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
    }


    // Default constructor
    public Posts() {
        this.status = "active";
    }

    // Constructor without id for new posts
    public Posts(int user_id_id, String type, String title, String description, String status, String images) {
        this.user_id_id = user_id_id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.status = status;
        this.images = images;
    }

    // Full constructor
    public Posts(int id, int user_id_id, String type, String title, String description,
                 String created_at, String status, String images) {
        this.id = id;
        this.user_id_id = user_id_id;
        this.type = type;
        this.title = title;
        this.description = description;
        this.created_at = created_at;
        this.status = status;
        this.images = images;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id_id() {
        return user_id_id;
    }

    public void setUser_id_id(int user_id_id) {
        this.user_id_id = user_id_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    // Helper methods for handling images
    public List<String> getImagesList() {
        if (images == null || images.isEmpty() || images.equals("[]")) {
            return new ArrayList<>();
        }

        // Parse JSON array string - simple approach
        String withoutBrackets = images.substring(1, images.length() - 1);
        if (withoutBrackets.isEmpty()) {
            return new ArrayList<>();
        }

        // Split by commas and remove quotes
        String[] imageArray = withoutBrackets.split(",");
        List<String> result = new ArrayList<>();

        for (String img : imageArray) {
            result.add(img.trim().replace("\"", ""));
        }

        return result;
    }

    public void setImagesList(List<String> imagesList) {
        // Convert List to JSON array string
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < imagesList.size(); i++) {
            sb.append("\"").append(imagesList.get(i)).append("\"");
            if (i < imagesList.size() - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        this.images = sb.toString();
    }

    @Override
    public String toString() {
        return "Posts{" +
                "id=" + id +
                ", user_id_id=" + user_id_id +
                ", type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", created_at='" + created_at + '\'' +
                ", status='" + status + '\'' +
                ", images='" + images + '\'' +
                '}';
    }
}