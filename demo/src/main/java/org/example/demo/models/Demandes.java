package org.example.demo.models;

import java.time.LocalDate;
import java.util.Objects;

public class Demandes {
    private int demandeId;
    private int userId;
    private int ressourceId;
    private int toUserId;
    private LocalDate createdAt;
    private LocalDate expireDate;
    private String status;
    private String message;
    private String proposition;
    private String reponse;
    private String nomDemandeur;
    private String nomOwner;
    private String priorite;

    public Demandes(int demandeId, int userId, int ressourceId, int toUserId, LocalDate createdAt, LocalDate expireDate, String status, String message, String proposition, String reponse, String nomDemandeur, String nomOwner, String priorite) {
        this.demandeId = demandeId;
        this.userId = userId;
        this.ressourceId = ressourceId;
        this.toUserId = toUserId;
        this.createdAt = createdAt;
        this.expireDate = expireDate;
         this.status = status;
        this.message = message;
        this.proposition = proposition;
        this.reponse = reponse;
        this.nomDemandeur = nomDemandeur;
        this.nomOwner = nomOwner;
        this.priorite = priorite;
    }
    public Demandes( int userId, int ressourceId, int toUserId, LocalDate createdAt, LocalDate expireDate, String status, String message, String proposition, String reponse, String nomDemandeur, String nomOwner, String priorite) {

        this.userId = userId;
        this.ressourceId = ressourceId;
        this.toUserId = toUserId;
        this.createdAt = createdAt;
        this.expireDate = expireDate;
        this.status = status;
        this.message = message;
        this.proposition = proposition;
        this.reponse = reponse;
        this.nomDemandeur = nomDemandeur;
        this.nomOwner = nomOwner;
        this.priorite = priorite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Demandes demandes = (Demandes) o;
        return demandeId == demandes.demandeId && userId == demandes.userId && ressourceId == demandes.ressourceId && toUserId == demandes.toUserId && Objects.equals(createdAt, demandes.createdAt) && Objects.equals(expireDate, demandes.expireDate) && Objects.equals(status, demandes.status) && Objects.equals(message, demandes.message) && Objects.equals(proposition, demandes.proposition) && Objects.equals(reponse, demandes.reponse) && Objects.equals(nomDemandeur, demandes.nomDemandeur) && Objects.equals(nomOwner, demandes.nomOwner) && Objects.equals(priorite, demandes.priorite);
    }

    @Override
    public int hashCode() {
        return Objects.hash(demandeId, userId, ressourceId, toUserId, createdAt, expireDate, status, message, proposition, reponse, nomDemandeur, nomOwner, priorite);
    }


    public Demandes(LocalDate createdAt , LocalDate expireDate, String message, String priorite) {

        this.createdAt = createdAt;
        this.expireDate = expireDate;
        this.message = message;
        this.priorite = priorite;
    }

    public Demandes() {

    }


    public int getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(int demandeId) {
        this.demandeId = demandeId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRessourceId() {
        return ressourceId;
    }

    public void setRessourceId(int ressourceId) {
        this.ressourceId = ressourceId;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProposition() {
        return proposition;
    }

    public void setProposition(String proposition) {
        this.proposition = proposition;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getNomDemandeur() {
        return nomDemandeur;
    }

    public void setNomDemandeur(String nomDemandeur) {
        this.nomDemandeur = nomDemandeur;
    }

    public String getNomOwner() {
        return nomOwner;
    }

    public void setNomOwner(String nomOwner) {
        this.nomOwner = nomOwner;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }
}
