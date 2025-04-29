package org.example.demo.models;

public class TypeRec {
    // L'ID sera généré en base (auto-increment)
    private int id;
    private String nom;
    private String description;

    public TypeRec() {
    }

    public TypeRec(String nom, String description) {
        this.nom = nom;
        this.description = description;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    // Pas de setter pour l'ID, car il est auto-généré

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
