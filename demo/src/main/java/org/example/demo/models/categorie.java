package org.example.demo.models;

public class categorie {
    private int id;
    private String nom;

    public categorie(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public categorie(String nom) {
        this.nom = nom;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Override
    public String toString() {
        return nom;
    }



}
