package org.example.demo.services.ressource;

import org.example.demo.models.Demandes;
import org.example.demo.models.User;
import org.example.demo.utils.dataBaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class DemandesService implements IService <Demandes>  {
    private Connection connection;

    public DemandesService() {
        this.connection = dataBaseHelper.getInstance().getConnection();
    }

    public void ajouter(Demandes demande) {
        String sql = "INSERT INTO demandes (created_at, expire_date,message, priorite, ressource_id_id,user_id_id,to_user_id) VALUES (?, ?, ?, ? , ?, ?, ?)";
        try {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setDate(1, Date.valueOf(demande.getCreatedAt())); // OK
            ps.setDate(2, demande.getExpireDate() != null ? Date.valueOf(demande.getExpireDate()) : null); // CORRIGÉ

            ps.setString(3, demande.getMessage());
            ps.setString(4, demande.getPriorite());
            ps.setInt(5, demande.getRessourceId());
            ps.setInt(6, demande.getUserId());
            ps.setInt(7, demande.getToUserId());
            ps.executeUpdate();
            System.out.println("✅ Demande ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout de la demande : " + e.getMessage());
        }
    }
    @Override
    public void modifier(Demandes demande) {
        String sql = "UPDATE demandes SET expire_date=?, message=?, priorite=? WHERE demande_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, demande.getExpireDate() != null ? Date.valueOf(demande.getExpireDate()) : null);
            ps.setString(2, demande.getMessage());
            ps.setString(3, demande.getPriorite());
            ps.setInt(4, demande.getDemandeId());
            ps.executeUpdate();
            System.out.println("✅ Demande modifiée avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la modification de la demande : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(Demandes demande) {
        String sql = "DELETE FROM demandes WHERE demande_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, demande.getDemandeId());
            ps.executeUpdate();
            System.out.println("🗑️ Demande supprimée avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public List<Demandes> rechercher() {
        List<Demandes> demandes = new ArrayList<>();
        String sql = "SELECT * FROM demandes";

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Demandes d = new Demandes();
                d.setDemandeId(rs.getInt("demande_id"));
                d.setCreatedAt(rs.getDate("created_at").toLocalDate());
                d.setExpireDate(rs.getDate("expire_date").toLocalDate());
                d.setMessage(rs.getString("message"));
                d.setStatus(rs.getString("status"));
                d.setPriorite(rs.getString("priorite"));
                d.setNomDemandeur(rs.getString("nomdemandeur")); // ✅ champ existe
                d.setNomOwner(rs.getString("nomowner")); // ✅ champ existe

                demandes.add(d);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des demandes : " + e.getMessage());
        }

        return demandes;
    }


    public void changerStatut(int id, String statut) {
        String sql = "UPDATE demandes SET status=? WHERE demande_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
            System.out.println("🔄 Statut modifié : " + statut);
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors du changement de statut : " + e.getMessage());
        }
    }

    public void updateStatutTerminee() {
        String sql = "UPDATE demandes SET status='terminée' WHERE expire_date = CURDATE() AND status != 'terminée'";
        try (Statement stmt = connection.createStatement()) {
            int rows = stmt.executeUpdate(sql);
            if (rows > 0) {
                System.out.println("📅 Statut mis à jour pour les demandes expirées.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la mise à jour des statuts terminée : " + e.getMessage());
        }
    }


    public List<Demandes> getDemandesByUserId(User user) {
        List<Demandes> demandes = new ArrayList<>();
        String sql = "SELECT * FROM demandes WHERE user_id_id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, user.getId());

            ResultSet rs = ps.executeQuery(); // ✅ Use executeQuery() for SELECT

            while (rs.next()) {
                Demandes d = new Demandes();
                d.setDemandeId(rs.getInt("demande_id"));
                d.setCreatedAt(rs.getDate("created_at").toLocalDate());
                d.setExpireDate(rs.getDate("expire_date").toLocalDate());
                d.setMessage(rs.getString("message"));
                d.setStatus(rs.getString("status")); // or "etat", depending on your DB
                d.setPriorite(rs.getString("priorite"));
                d.setNomDemandeur(rs.getString("nomdemandeur"));
                d.setNomOwner(rs.getString("nomowner"));

                demandes.add(d);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des demandes : " + e.getMessage());
        }

        return demandes;
    }

    public List<Demandes> getDemandesToAcceptByUserId(User user) {
        List<Demandes> demandes = new ArrayList<>();
        String sql = "SELECT * FROM demandes WHERE to_user_id = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setInt(1, user.getId());

            ResultSet rs = ps.executeQuery(); // ✅ Use executeQuery() for SELECT

            while (rs.next()) {
                Demandes d = new Demandes();
                d.setDemandeId(rs.getInt("demande_id"));
                d.setCreatedAt(rs.getDate("created_at").toLocalDate());
                d.setExpireDate(rs.getDate("expire_date").toLocalDate());
                d.setMessage(rs.getString("message"));
                d.setStatus(rs.getString("status")); // or "etat", depending on your DB
                d.setPriorite(rs.getString("priorite"));
                d.setNomDemandeur(rs.getString("nomdemandeur"));
                d.setNomOwner(rs.getString("nomowner"));

                demandes.add(d);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des demandes : " + e.getMessage());
        }

        return demandes;
    }

}
