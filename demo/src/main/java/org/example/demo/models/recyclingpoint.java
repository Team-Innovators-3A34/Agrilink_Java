package org.example.demo.models;

public class recyclingpoint {
    private int id;
    private String nom;
    private String date;
    private String adresse;
    private String description;
    private String image;
    private double longitude;
    private double latitude;
    private String type;
    private int owner_id;

    public recyclingpoint() {}

    public recyclingpoint(int id, String nom, String adresse, double longitude, double latitude, String date, String type,  String image, String description) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.longitude = longitude;
        this.latitude = latitude;
        this.date = date;
        this.type = type;
        this.image = image;
        this.description = description;
    }

    public recyclingpoint(String nom, String adresse, double longitude, double latitude, String type,  String image, String description) {
        this.nom = nom;
        this.adresse = adresse;
        this.longitude = longitude;
        this.latitude = latitude;
        this.type = type;
        this.image = image;
        this.description = description;
    }
    public recyclingpoint(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }



    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }



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



    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getOwner_id() { return owner_id; }

    public void setOwner_id(int owner_id) { this.owner_id = owner_id; }

    @Override
    public String toString() {
        return "event{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", date='" + date + '\'' +
                ", adresse='" + adresse + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", type='" + type + '\'' +
                '}';
    }


}
