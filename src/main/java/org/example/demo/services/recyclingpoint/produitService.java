package org.example.demo.services.recyclingpoint;

import org.example.demo.models.produit;
import org.example.demo.models.recyclingpoint;
import org.example.demo.utils.dataBaseHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class produitService implements Interface<produit> {

    private final Connection connection = dataBaseHelper.getInstance().getConnection();

    @Override
    public boolean ajouter(produit produit) {
        String req = "INSERT INTO produitrecyclage (name, pointderecyclage_id, user_id, recycled_at, quantite, description, image) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setString(1, produit.getNom());
            pst.setInt(2, produit.getRecyclingpoint().getId()); // ou produit.getRecyclingpoint().getId() si tu stockes un objet
            pst.setInt(3, produit.getUser_id()); // à ajouter depuis la session si besoin
            pst.setDate(4, new java.sql.Date(System.currentTimeMillis())); // ou produit.getRecycled_at() si c’est défini
            pst.setInt(5, produit.getQuantity());
            pst.setString(6, produit.getDescription());
            pst.setString(7, produit.getImage());

            pst.executeUpdate();
            System.out.println("✅ Produit ajouté");
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout du produit : " + e.getMessage());
            return false;
        }
    }


    @Override
    public boolean modifier(produit produit) {
        String req = "UPDATE produitrecyclage SET name = ?, pointderecyclage_id = ?, quantite = ?, description = ?, image = ? WHERE id = ?";
        try {
            // Prépare la requête SQL
            PreparedStatement pst = connection.prepareStatement(req);

            // Associe les paramètres dans l'ordre correct
            pst.setString(1, produit.getNom());  // Nom du produit
            pst.setInt(2, produit.getRecyclingpoint().getId());  // ID du point de recyclage
            pst.setInt(3, produit.getQuantity());  // Quantité du produit
            pst.setString(4, produit.getDescription());  // Description du produit
            pst.setString(5, produit.getImage());  // Image du produit
            pst.setInt(6, produit.getId());  // ID du produit à modifier

            // Exécute la mise à jour
            int rowsUpdated = pst.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Produit modifié avec succès");
                return true;
            } else {
                System.out.println("Aucun produit n'a été modifié");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du produit : " + e.getMessage());
            return false;
        }
    }


    @Override
    public void supprimer(produit produit) {
        String req = "DELETE FROM produitrecyclage WHERE id = ?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, produit.getId());
            pst.executeUpdate();
            System.out.println("Produit supprimé");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du produit : " + e.getMessage());
        }
    }

    @Override
    public List<produit> rechercher() {
        List<produit> produits = new ArrayList<>();
        String req = "SELECT p.id, p.name, rp.id as rp_id, rp.name as rp_nom,p.image,p.description,p.quantite " +
                "FROM produitrecyclage p " +
                "JOIN pointrecyclage rp ON p.id = rp.id";

        try {
            PreparedStatement pst = connection.prepareStatement(req);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                recyclingpoint rp = new recyclingpoint(
                        rs.getInt("rp_id"),
                        rs.getString("rp_nom")
                );

                produit prod = new produit(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rp,
                        rs.getInt("quantite"),
                        rs.getString("description"),
                        rs.getString("image")
                );

                produits.add(prod);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des produits : " + e.getMessage());
        }

        return produits;
    }
}
