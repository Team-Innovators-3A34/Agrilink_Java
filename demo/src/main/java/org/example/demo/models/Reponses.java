package org.example.demo.models;

import java.time.LocalDateTime;

public class Reponses {
    private int id;
    private String content;
    private LocalDateTime date;
    private Boolean isAuto;
    private Reclamation reclamation;
    private String status;
    private String solution;

    // Constructeur complet
    public Reponses(int id, String content, LocalDateTime date, Boolean isAuto,
                    Reclamation reclamation, String status, String solution) {
        this.id = id;
        this.content = content;
        this.date = date;
        this.isAuto = isAuto;
        this.reclamation = reclamation;
        this.status = status;
        this.solution = solution;
    }

    // Constructeur sans ID (pour insertion)
    public Reponses(String content, LocalDateTime date, Boolean isAuto,
                    Reclamation reclamation, String status, String solution) {
        this.content = content;
        this.date = date;
        this.isAuto = isAuto;
        this.reclamation = reclamation;
        this.status = status;
        this.solution = solution;
    }

    // Constructeur vide
    public Reponses() { }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public Boolean getIsAuto() { return isAuto; }
    public void setIsAuto(Boolean isAuto) { this.isAuto = isAuto; }
    public Reclamation getReclamation() { return reclamation; }
    public void setReclamation(Reclamation reclamation) { this.reclamation = reclamation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }

    @Override
    public String toString() {
        return "Reponses{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", date=" + date +
                ", isAuto=" + isAuto +
                ", reclamation=" + (reclamation != null ? reclamation.getId() : "null") +
                ", status='" + status + '\'' +
                ", solution='" + solution + '\'' +
                '}';
    }
}
