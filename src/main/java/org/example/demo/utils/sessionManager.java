package org.example.demo.utils;

import org.example.demo.HelloApplication;
import org.example.demo.models.User;

public class sessionManager {

    private static sessionManager instance;
    private static String tempemail;
    private User user;

    private sessionManager() {}

    public static sessionManager getInstance() {
        if (instance == null) {
            instance = new sessionManager();
        }
        return instance;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public User getUser(){
        if(isSessionActive()){
            return user;
        }
        return null;
    }

    public void clearSession() {
        user = null;
        System.out.println("Utilisateur deconnecte!");
    }

    public boolean isSessionActive() {
        return user != null;
    }

    public static String getTempemail() {
        return tempemail;
    }

    public static void setTempemail(String tmpemail) {
        tempemail = tmpemail;
    }

    public boolean isAdmin() {

        if (isSessionActive() && user.getRoles().equals("[\"ROLE_ADMIN\"]")) {
            System.out.println("marhbe admin");
            return true;
        }
        return false;

    }
}
