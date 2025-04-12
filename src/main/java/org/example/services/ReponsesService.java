package org.example.services;

import org.example.entities.Reponses;
import org.example.entities.Reclamation;
import org.example.utils.MyDataBase;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReponsesService {
    private Connection connection;

    public ReponsesService() {
        connection = MyDataBase.getConnection();
    }

    // Obtenir le prochain ID disponible pour Reponses
    private int getNextId() {
        String query = "SELECT MAX(id) FROM reponses";
        try (PreparedStatement pst = connection.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1) + 1;
            }
            return 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return 1;
        }
    }

    // Ajouter une réponse
    public void ajouterReponse(Reponses rep) {
        String query = "INSERT INTO reponses (id, content, `date`, is_auto, id_reclamation_id, status, solution) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            int nextId = getNextId();
            pst.setInt(1, nextId);
            pst.setString(2, rep.getContent());
            pst.setTimestamp(3, rep.getDate() != null ? Timestamp.valueOf(rep.getDate()) : null);
            pst.setBoolean(4, rep.getIsAuto() != null ? rep.getIsAuto() : false);
            pst.setInt(5, rep.getReclamation() != null ? rep.getReclamation().getId() : 0);
            pst.setString(6, rep.getStatus());
            pst.setString(7, rep.getSolution());
            pst.executeUpdate();
            rep.setId(nextId);
            System.out.println("Réponse ajoutée avec succès avec l'ID: " + nextId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Afficher toutes les réponses
    public List<Reponses> afficherReponses() {
        List<Reponses> reponsesList = new ArrayList<>();
        String query = "SELECT * FROM reponses";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String content = rs.getString("content");
                LocalDateTime date = rs.getTimestamp("date") != null ? rs.getTimestamp("date").toLocalDateTime() : null;
                Boolean isAuto = rs.getBoolean("is_auto");
                // Récupération de l'ID de la réclamation
                int reclamationId = rs.getInt("id_reclamation_id");
                Reclamation reclamation = new Reclamation();
                reclamation.setId(reclamationId);
                String status = rs.getString("status");
                String solution = rs.getString("solution");

                Reponses rep = new Reponses(id, content, date, isAuto, reclamation, status, solution);
                reponsesList.add(rep);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reponsesList;
    }

    // Modifier une réponse
    public boolean modifierReponse(Reponses rep) {
        String query = "UPDATE reponses SET content = ?, `date` = ?, is_auto = ?, status = ?, solution = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, rep.getContent());
            pst.setTimestamp(2, rep.getDate() != null ? Timestamp.valueOf(rep.getDate()) : null);
            pst.setBoolean(3, rep.getIsAuto() != null ? rep.getIsAuto() : false);
            pst.setString(4, rep.getStatus());
            pst.setString(5, rep.getSolution());
            pst.setInt(6, rep.getId()); // Assurez-vous que cet ID est correct

            int rowsUpdated = pst.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Réponse modifiée avec succès.");
                return true;
            } else {
                System.out.println("Aucune réponse trouvée avec l'ID: " + rep.getId());
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    // Supprimer une réponse
    public void supprimerReponse(int id) {
        String query = "DELETE FROM reponses WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, id);
            int rowsDeleted = pst.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Réponse supprimée avec succès.");
            } else {
                System.out.println("Aucune réponse trouvée avec l'ID: " + id);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupère toutes les réponses associées à une réclamation dont l'ID est donné.
     *
     * @param reclamationId L'identifiant de la réclamation.
     * @return Une liste de Reponses.
     */
    public List<Reponses> getAnswersByReclamation(int reclamationId) {
        List<Reponses> answers = new ArrayList<>();
        System.out.println("Recherche des réponses pour la réclamation ID = " + reclamationId);
        String query = "SELECT * FROM reponses WHERE id_reclamation_id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, reclamationId);
            ResultSet rs = pst.executeQuery();
            int count = 0;
            while (rs.next()) {
                Reponses rep = new Reponses();
                rep.setId(rs.getInt("id"));
                rep.setContent(rs.getString("content"));
                Timestamp ts = rs.getTimestamp("date");
                if (ts != null) {
                    rep.setDate(ts.toLocalDateTime());
                }
                rep.setIsAuto(rs.getBoolean("is_auto"));
                rep.setStatus(rs.getString("status"));
                rep.setSolution(rs.getString("solution"));
                answers.add(rep);
                count++;
            }
            System.out.println("Nombre de réponses trouvées : " + count);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }
}
