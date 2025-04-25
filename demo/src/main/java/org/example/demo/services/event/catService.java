package org.example.demo.services.event;

import org.example.demo.models.categorie;
import org.example.demo.utils.dataBaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class catService implements Interface<categorie> {

    private Connection connection = dataBaseHelper.getInstance().getConnection();

    @Override
    public void ajouter(categorie categorie) {
        String req = "INSERT INTO categorie (nom) VALUES (?)";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setString(1, categorie.getNom());
            pst.executeUpdate();
            System.out.println("Categorie ajoutée");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void modifier(categorie categorie) {
        String req = "UPDATE categorie SET nom=? WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setString(1, categorie.getNom());
            pst.setInt(2, categorie.getId());
            pst.executeUpdate();
            System.out.println("categorie modifiée");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void supprimer(categorie categorie) {
        String req = "DELETE from categorie WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, categorie.getId());
            pst.executeUpdate();
            System.out.println("categorie supprimée");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public List<categorie> rechercher() {
        List<categorie> categories = new ArrayList<>();

        String req = "SELECT * FROM categorie";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            ResultSet rs = pst.executeQuery(req);
            while (rs.next()) {
                categories.add(new categorie(rs.getInt("id"), rs.getString("nom")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return categories;
    }
}
