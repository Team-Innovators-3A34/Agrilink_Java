package org.example.services;

import org.example.entities.TypeRec;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TypeRecService {

    private Connection connection;

    public TypeRecService() {
        connection = MyDataBase.getConnection();
    }

    // Insertion : l'ID est auto-incrémenté en base
    public boolean ajouterTypeRec(TypeRec typeRec) {
        String query = "INSERT INTO type_rec (nom, description) VALUES (?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, typeRec.getNom());
            pst.setString(2, typeRec.getDescription());
            int rowsInserted = pst.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Récupérer toutes les catégories
    public List<TypeRec> getAllCategories() {
        List<TypeRec> list = new ArrayList<>();
        String query = "SELECT * FROM type_rec";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                TypeRec t = new TypeRec();
                t.setId(rs.getInt("id"));
                t.setNom(rs.getString("nom"));
                t.setDescription(rs.getString("description"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Supprimer une catégorie par ID
    public boolean supprimerTypeRec(int id) {
        String query = "DELETE FROM type_rec WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            int rowsDeleted = pst.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Modifier une catégorie
    public boolean modifierTypeRec(TypeRec typeRec) {
        String query = "UPDATE type_rec SET nom = ?, description = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, typeRec.getNom());
            pst.setString(2, typeRec.getDescription());
            pst.setInt(3, typeRec.getId());
            int rowsUpdated = pst.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Récupérer une catégorie par ID
    public TypeRec getTypeRecById(int id) {
        String query = "SELECT * FROM type_rec WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                TypeRec t = new TypeRec();
                t.setId(rs.getInt("id"));
                t.setNom(rs.getString("nom"));
                t.setDescription(rs.getString("description"));
                return t;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
