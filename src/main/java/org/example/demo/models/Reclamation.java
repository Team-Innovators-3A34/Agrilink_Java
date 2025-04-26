package org.example.demo.models;

//import org.example.services.ReponsesService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reclamation {
    private int id;
    private int idUser;
    private String nomUser;
    private String mailUser;
    private String title;
    private String content;
    private String status;
    private LocalDateTime date;
    private int type_id;
    private int priorite;
    private String image;
    private String archive;
    private String etat_rec;
    private String etat_user;
    private String typeNom; // Nouvelle propriété


    private List<Reponses> reponses = new ArrayList<>();

    // Constructeur complet
    public Reclamation(int id, int idUser, String nomUser, String mailUser,
                       String title, String content, String status, LocalDateTime date,
                       int type_id, int priorite, String image, String archive,
                       String etat_rec, String etat_user) {
        this.id = id;
        this.idUser = idUser;
        this.nomUser = nomUser;
        this.mailUser = mailUser;
        this.title = title;
        this.content = content;
        this.status = status;
        this.date = date;
        this.type_id = type_id;
        this.priorite = priorite;
        this.image = image;
        this.archive = archive;
        this.etat_rec = etat_rec;
        this.etat_user = etat_user;
    }

    // Constructeur sans ID (pour insertion)
    public Reclamation(int idUser, String nomUser, String mailUser,
                       String title, String content, String status, LocalDateTime date,
                       int type_id, int priorite, String image, String archive,
                       String etat_rec, String etat_user) {
        this.idUser = idUser;
        this.nomUser = nomUser;
        this.mailUser = mailUser;
        this.title = title;
        this.content = content;
        this.status = status;
        this.date = date;
        this.type_id = type_id;
        this.priorite = priorite;
        this.image = image;
        this.archive = archive;
        this.etat_rec = etat_rec;
        this.etat_user = etat_user;
    }

    public Reclamation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdUser() { return idUser; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public String getNomUser() { return nomUser; }
    public void setNomUser(String nomUser) { this.nomUser = nomUser; }
    public String getMailUser() { return mailUser; }
    public void setMailUser(String mailUser) { this.mailUser = mailUser; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    public int getType() { return type_id; }
    public void setType(int type) { this.type_id = type; }
    public int getPriorite() { return priorite; }
    public void setPriorite(int priorite) { this.priorite = priorite; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String isArchive() { return archive; }
    public void setArchive(String archive) { this.archive = archive; }
    public String getEtatRec() { return etat_rec; }
    public void setEtatRec(String etat_rec) { this.etat_rec = etat_rec; }
    public String getEtatUser() { return etat_user; }
    public void setEtatUser(String etat_user) { this.etat_user = etat_user; }

    public String getTypeNom() {
        return typeNom;
    }

    public void setTypeNom(String typeNom) {
        this.typeNom = typeNom;
    }
}
