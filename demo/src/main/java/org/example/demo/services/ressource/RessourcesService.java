package org.example.demo.services.ressource;

import org.example.demo.models.Ressources;
import org.example.demo.utils.dataBaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RessourcesService implements IService<Ressources> {
    private Connection connection;

    public RessourcesService() {
        this.connection = dataBaseHelper.getInstance().getConnection();
    }
    public void updateRatingInDatabase(int ressourceId, double newRating) {
        String sql = "UPDATE ressource SET rating = ? WHERE id = ?";

        try (Connection conn = dataBaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newRating); // ou stmt.setInt si rating est entier
            stmt.setInt(2, ressourceId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour du rating : " + e.getMessage());
        }
    }


    @Override
    public void ajouter(Ressources ressource) {
        String sql = "INSERT INTO ressources (type, description, status,  name_r, marque,  prix_location, superficie, adresse,images,user_id_id) " +
                "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, ressource.getType());
            ps.setString(2, ressource.getDescription());
            ps.setString(3, ressource.getStatus());
            ps.setString(4, ressource.getName());
            ps.setString(5, ressource.getMarque());
            ps.setDouble(6, ressource.getPrixLocation());
            ps.setDouble(7, ressource.getSuperficie());
            ps.setString(8, ressource.getAdresse());
            ps.setString(9, ressource.getImage());
            ps.setInt(10, ressource.getUserId());
            ps.executeUpdate();
            System.out.println("✅ Ressource ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout de la ressource : " + e.getMessage());
        }
    }

    @Override
    public void modifier(Ressources ressource) {
        String sql = "UPDATE ressources SET type=?, description=?, status=?,  name_r=?, marque=?,  prix_location=?, superficie=?, adresse=?,images=? " +
                "WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, ressource.getType());
            ps.setString(2, ressource.getDescription());
            ps.setString(3, ressource.getStatus());
            ps.setString(4, ressource.getName());
            ps.setString(5, ressource.getMarque());
            ps.setDouble(6, ressource.getPrixLocation());
            ps.setDouble(7, ressource.getSuperficie());
            ps.setString(8, ressource.getAdresse());
            ps.setString(9, ressource.getImage());
            ps.setInt(10, ressource.getId());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Ressource modifiée avec succès !");
            } else {
                System.out.println("❌ Ressource introuvable.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la modification de la ressource : " + e.getMessage());
        }

    }

    @Override
    public void supprimer(Ressources ressource) {
        String sql = "DELETE FROM ressources WHERE id=?";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, ressource.getId());

            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("✅ Ressource supprimée avec succès !");
            } else {
                System.out.println("❌ Ressource introuvable.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression de la ressource : " + e.getMessage());
        }
    }

    @Override

    public List<Ressources> rechercher() {
        List<Ressources> ressources = new ArrayList<>();
        String sql = "SELECT * FROM ressources";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {
                Ressources ressource = new Ressources(
                        rs.getInt("id"),
                        rs.getInt("user_id_id"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("images"),
                        rs.getString("name_r"),
                        rs.getString("marque"),
                        rs.getString("etat"),
                        rs.getDouble("prix_location"),
                        rs.getDouble("superficie"),
                        rs.getString("adresse")
                );
                // 💥 Ajout des valeurs manquantes
                ressource.setRating(rs.getDouble("rating"));
                ressource.setRatingCount(rs.getInt("rating_count"));

                ressources.add(ressource);
            }
            System.out.println("✅ Recherche terminée, " + ressources.size() + " ressources trouvées.");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la recherche des ressources : " + e.getMessage());
        }
        return ressources;
    }


    public void ajouter(int i, String type, String description, String status, String nom, String marque, String prix, String superficie, String adresse) {
    }
    /*public List<Ressources> afficher() throws SQLException {
        List<Ressources> liste = new ArrayList<>();
        String req = "SELECT * FROM ressources";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);

        while (rs.next()) {
            Ressources r = new Ressources(
                    rs.getString("name_r"),
                    rs.getString("type"),
                    rs.getString("status"),
                    rs.getString("adresse"),
                    rs.getString("description"),
                    rs.getDouble("prix_location"),
                    rs.getDouble("superficie"),
                    rs.getString("marque")
            );
            liste.add(r);
        }
        return liste;
    }*/

    public List<Ressources> RessourceParUser(int userId) {
        List<Ressources> ressources = new ArrayList<>();
        String sql = "SELECT * FROM ressources WHERE user_id_id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Ressources ressource = new Ressources(
                        rs.getInt("id"),
                        rs.getInt("user_id_id"),
                        rs.getString("type"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getString("images"),
                        rs.getString("name_r"),
                        rs.getString("marque"),
                        rs.getString("etat"),
                        rs.getDouble("prix_location"),
                        rs.getDouble("superficie"),
                        rs.getString("adresse")
                );
                // 💥 Ajout des valeurs manquantes
                ressource.setRating(rs.getDouble("rating"));
                ressource.setRatingCount(rs.getInt("rating_count"));

                ressources.add(ressource);
            }
            System.out.println("✅ Recherche terminée, " + ressources.size() + " ressources trouvées pour l'utilisateur ID " + userId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la recherche des ressources : " + e.getMessage());
        }

        return ressources;
    }


    public void modifierRating(Ressources ressource) throws SQLException {
        String sql = "UPDATE ressources SET rating = ?, rating_count = ? WHERE id = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setDouble(1, ressource.getRating());
        ps.setInt(2, ressource.getRatingCount());
        ps.setInt(3, ressource.getId());
        ps.executeUpdate();
    }

}