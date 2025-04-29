package org.example.demo.models;

public class Ressources {
    private int id;
    private int userId;
    private String type;
    private String description;
    private String status;
    private String image;
    private String name;
    private String marque;
    private String etat;
    private double prixLocation;
    private double superficie;
    private String adresse;
    private double rating;
    private int ratingCount;

    // Getters & Setters
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }

    // Constructeur
    public Ressources(int id, int userId, String type, String description, String status, String image, String name,
                     String marque, String etat, double prixLocation, double superficie, String adresse) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.description = description;
        this.status = status;
        this.image = image;
        this.name = name;
        this.marque = marque;
        this.etat = etat;
        this.prixLocation = prixLocation;
        this.superficie = superficie;
        this.adresse = adresse;
    }
    public Ressources() {}
    public Ressources( int userId, String type, String description, String status, String image, String name,
                      String marque, String etat, double prixLocation, double superficie, String adresse) {

        this.userId = userId;
        this.type = type;
        this.description = description;
        this.status = status;
        this.image = image;
        this.name = name;
        this.marque = marque;
        this.etat = etat;
        this.prixLocation = prixLocation;
        this.superficie = superficie;
        this.adresse = adresse;
    }
    public Ressources(String type, String description, String status, String name,String marque,double prixLocation, double superficie, String adresse) {


        this.type = type;
        this.description = description;
        this.status = status;
        this.name = name;
       this.marque=marque;
        this.prixLocation =prixLocation;
        this.superficie = superficie;
        this.adresse = adresse;
    }

    public Ressources(String type, String description, String status, String name,String marque, double prixLocation, double superficie, String adresse,String image) {
        this.type = type;
        this.description = description;
        this.status = status;
        this.name = name;
        this.marque=marque;
        this.prixLocation =prixLocation;
        this.superficie = superficie;
        this.adresse = adresse;
        this.image=image;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getEtat() {
        return etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public double getPrixLocation() {
        return prixLocation;
    }

    public void setPrixLocation(double prixLocation) {
        this.prixLocation = prixLocation;
    }

    public double getSuperficie() {
        return superficie;
    }

    public void setSuperficie(double superficie) {
        this.superficie = superficie;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
}
