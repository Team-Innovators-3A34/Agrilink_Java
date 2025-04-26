package org.example.demo.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class event {
    private int id;
    private categorie categorie;
    private String nom;
    private String date;
    private String adresse;
    private String description;
    private String image;
    private int nbr_Places;
    private double longitude;
    private double latitude;
    private String type;
    private LocalDate d;

    public event() {}

    public event(int id, String nom, String adresse, double longitude, double latitude, String date, String type, int nbrPlaces, String image, String description, categorie categorie) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.longitude = longitude;
        this.latitude = latitude;
        this.date = date;
        this.type = type;
        this.nbr_Places = nbrPlaces;
        this.image = image;
        this.description = description;
        this.categorie = categorie;
    }

    public LocalDate getD() {
        return d;
    }

    public void setD(LocalDate d) {
        this.d = d;
    }

    public event(String nom, String adresse, double longitude, double latitude, String date, String type, int nbrPlaces, String image, String description, categorie categorie) {
        this.nom = nom;
        this.adresse = adresse;
        this.longitude = longitude;
        this.latitude = latitude;
        this.date = date;
        this.type = type;
        this.nbr_Places = nbrPlaces;
        this.image = image;
        this.description = description;
        this.categorie = categorie;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public categorie getCategorie() { return categorie; }
    public void setCategorie(categorie categorie) { this.categorie = categorie; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getAdresse() { return adresse; }
    public void setAdresse(String adresse) { this.adresse = adresse; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getNbrPlaces() { return nbr_Places; }
    public void setNbrPlaces(int nbrPlaces) { this.nbr_Places = nbrPlaces; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @Override
    public String toString() {
        return "event{" +
                "id=" + id +
                ", categorie=" + categorie +
                ", nom='" + nom + '\'' +
                ", date='" + date + '\'' +
                ", adresse='" + adresse + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", nbr_Places=" + nbr_Places +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", type='" + type + '\'' +
                '}';
    }
}
