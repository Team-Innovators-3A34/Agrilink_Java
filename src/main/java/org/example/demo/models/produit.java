package org.example.demo.models;

public class produit {
    private int id;
    private String nom;
    private recyclingpoint recyclingpoint;
    private String description;
    private String image;
    private int quantity;
    private int user_id;

    public produit(int id, String nom , recyclingpoint recyclingpoint) {
        this.id = id;
        this.nom = nom;
        this.recyclingpoint = recyclingpoint;
    }

    public produit(int id, String nom , recyclingpoint recyclingpoint, int quantity,String description, String image) {
        this.id = id;
        this.nom = nom;
        this.recyclingpoint = recyclingpoint;
        this.quantity = quantity;
        this.description = description;
        this.image = image;
    }

    public produit(String nom) {
        this.nom = nom;
    }

    public produit(){}

    public int getQuantity() {
        return quantity;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public recyclingpoint getRecyclingpoint() {
        return recyclingpoint;
    }
    public void setRecyclingpoint(recyclingpoint recyclingpoint) {
        this.recyclingpoint = recyclingpoint;
    }

    @Override
    public String toString() {
        return "produit{" +
                "recyclingpoint=" + recyclingpoint +
                ", nom='" + nom + '\'' +
                '}';
    }
}
