package org.example.demo.models;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;

public class User {

    private int id;
    private  String email ;
    private  String password ;
    private  String adresse ;
    private  String nom ;
    private  String prenom ;
    private  String telephone ;
    private  String roles;
    private  String resetToken ;
    private  LocalDateTime resetTokenExpiresAt ;
    private  String verificationCode ;
    private  LocalDateTime codeExpirationDate ;
    private  String status ;
    private  String description ;
    private  String bio ;
    private  String image ;
    private  String accountVerification ;
    private  String country ;

    private StringProperty emailtable;
    private StringProperty adressetable;
    private StringProperty nomtable;
    private StringProperty prenomtable;
    private StringProperty telephonetable;
    private StringProperty rolestable;
    private StringProperty imagetable;
    private StringProperty accountVerificationtable;


    public User(int id, String email, String adresse, String nom, String prenom, String telephone, String roles, String status, String description, String bio, String image, String accountVerification, String country, String city, float longitude, float latitude, Boolean is2FA, int score, LocalDateTime createAt) {
        this.id = id;
        this.email = email;
        this.adresse = adresse;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.roles = roles;
        this.status = status;
        this.description = description;
        this.bio = bio;
        this.image = image;
        this.accountVerification = accountVerification;
        this.country = country;
        this.city = city;
        this.longitude = longitude;
        this.latitude = latitude;
        this.is2FA = is2FA;
        this.score = score;
        this.createAt = createAt;
    }

    public User(String email, String adresse, String nom, String prenom, String telephone, String roles, String password) {
        this.email = email;
        this.adresse = adresse;
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
        this.password = password;
        this.roles = roles;
    }

    public User(){}


    public User(String email, String nom, String prenom, String adresse, String roles, String telephone, String accountVerification, String image) {
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.roles = roles;
        this.telephone = telephone;
        this.accountVerification = accountVerification;
        this.image = image;

        this.emailtable = new SimpleStringProperty(email);
        this.adressetable = new SimpleStringProperty(adresse);
        this.nomtable = new SimpleStringProperty(nom);
        this.prenomtable = new SimpleStringProperty(prenom);
        this.telephonetable = new SimpleStringProperty(telephone);
        this.rolestable = new SimpleStringProperty(roles);
        this.accountVerificationtable = new SimpleStringProperty(accountVerification);
        this.imagetable = new SimpleStringProperty(image);
    }

    public User(int id ,String email, String nom, String prenom, String adresse, String roles, String telephone, String accountVerification, String image) {
        this.email = email;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
        this.roles = roles;
        this.telephone = telephone;
        this.accountVerification = accountVerification;
        this.image = image;
        this.id=id;

        this.emailtable = new SimpleStringProperty(email);
        this.adressetable = new SimpleStringProperty(adresse);
        this.nomtable = new SimpleStringProperty(nom);
        this.prenomtable = new SimpleStringProperty(prenom);
        this.telephonetable = new SimpleStringProperty(telephone);
        this.rolestable = new SimpleStringProperty(roles);
        this.accountVerificationtable = new SimpleStringProperty(accountVerification);
        this.imagetable = new SimpleStringProperty(image);
    }

    public StringProperty emailProperty() {
        return emailtable == null ? emailtable = new SimpleStringProperty() : emailtable;
    }

    public StringProperty nomProperty() {
        return nomtable == null ? nomtable = new SimpleStringProperty() : nomtable;
    }

    public StringProperty prenomProperty() {
        return prenomtable == null ? prenomtable = new SimpleStringProperty() : prenomtable;
    }

    public StringProperty adresseProperty() {
        return adressetable == null ? adressetable= new SimpleStringProperty() : adressetable;
    }

    public StringProperty rolesProperty() {
        return rolestable == null ? rolestable = new SimpleStringProperty() : rolestable;
    }

    public StringProperty telephoneProperty() {
        return telephonetable == null ? telephonetable = new SimpleStringProperty() : telephonetable;
    }

    public StringProperty accountVerificationProperty() {
        return accountVerificationtable == null ? accountVerificationtable = new SimpleStringProperty() : accountVerificationtable;
    }

    public StringProperty ImageProperty() {
        return imagetable == null ? imagetable = new SimpleStringProperty() : imagetable;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiresAt() {
        return resetTokenExpiresAt;
    }

    public void setResetTokenExpiresAt(LocalDateTime resetTokenExpiresAt) {
        this.resetTokenExpiresAt = resetTokenExpiresAt;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public LocalDateTime getCodeExpirationDate() {
        return codeExpirationDate;
    }

    public void setCodeExpirationDate(LocalDateTime codeExpirationDate) {
        this.codeExpirationDate = codeExpirationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAccountVerification() {
        return accountVerification;
    }

    public void setAccountVerification(String accountVerification) {
        this.accountVerification = accountVerification;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockUntil() {
        return lockUntil;
    }

    public void setLockUntil(LocalDateTime lockUntil) {
        this.lockUntil = lockUntil;
    }

    public LocalDateTime getCreateAt() {
        return createAt;
    }

    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    public Boolean getIs2FA() {
        return is2FA;
    }

    public void setIs2FA(Boolean is2FA) {
        this.is2FA = is2FA;
    }

    public String getCode2FA() {
        return code2FA;
    }

    public void setCode2FA(String code2FA) {
        this.code2FA = code2FA;
    }

    public LocalDateTime getCode2FAexpiry() {
        return code2FAexpiry;
    }

    public void setCode2FAexpiry(LocalDateTime code2FAexpiry) {
        this.code2FAexpiry = code2FAexpiry;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    private  String city ;
    private  float longitude;
    private  float latitude ;
    private  int failedLoginAttempts ;
    private  LocalDateTime lockUntil ;
    private  LocalDateTime createAt ;
    private  Boolean is2FA ;
    private  String code2FA ;
    private  LocalDateTime code2FAexpiry;
    private  int score ;

}
