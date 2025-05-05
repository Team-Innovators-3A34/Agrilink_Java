package org.example.demo.services.claim;

import org.example.demo.models.Reclamation;
import org.example.demo.utils.dataBaseHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReclamationService {
    private Connection connection;
    private HttpClient httpClient;
    private ObjectMapper mapper;

    public ReclamationService() {
        connection = dataBaseHelper.getInstance().getConnection();
        httpClient = HttpClient.newHttpClient();
        mapper = new ObjectMapper();
    }

    // Méthode pour appeler une API Flask et extraire le résultat JSON en utilisant Jackson
    private String callFlaskAPI(String url, String jsonPayload, String resultKey, int index) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 200) {
            JsonNode root = mapper.readTree(response.body());
            JsonNode node = root.get(resultKey);
            if (node != null) {
                if (index >= 0 && node.isArray()) {
                    return node.get(index).asText();
                } else {
                    return node.asText();
                }
            }
            return "inconnu";
        } else {
            return "inconnu";
        }
    }

    // Ajouter une réclamation avec analyse d'IA
    public boolean ajouterReclamation(Reclamation r) {
        // Appels aux API Flask pour obtenir etat_rec et etat_user
        try {
            // Pour predictetat_rec : on envoie le contenu original et on récupère la chaîne "positive" ou "negative"
            String recResponse = callFlaskAPI(
                    "http://localhost:5001/predictetat_rec",
                    "{\"text\":\"" + r.getContent() + "\"}",
                    "etat_rec",
                    -1
            );
            r.setEtatRec(recResponse);

            // Pour predictetat_user : on envoie le contenu dans un tableau et on récupère le premier élément de "predictions"
            String userResponse = callFlaskAPI(
                    "http://localhost:5001/predictetat_user",
                    "{\"texts\":[\"" + r.getContent() + "\"]}",
                    "predictions",
                    0
            );
            r.setEtatUser(userResponse);
        } catch (Exception e) {
            System.err.println("Erreur lors des appels aux API : " + e.getMessage());
            r.setEtatRec("inconnu");
            r.setEtatUser("inconnu");
        }

        String query = "INSERT INTO reclamation (id_user, nom_user, mail_user, title, content, status, date, type_id, priorite, image, archive, etat_rec, etat_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, r.getIdUser());
            pst.setString(2, r.getNomUser());
            pst.setString(3, r.getMailUser());
            pst.setString(4, r.getTitle());
            pst.setString(5, r.getContent());
            pst.setString(6, r.getStatus());
            pst.setTimestamp(7, Timestamp.valueOf(r.getDate()));
            pst.setInt(8, r.getType());
            pst.setInt(9, r.getPriorite());
            pst.setString(10, r.getImage());
            pst.setString(11, r.isArchive());
            pst.setString(12, r.getEtatRec());
            pst.setString(13, r.getEtatUser());

            pst.executeUpdate();
            System.out.println("Réclamation ajoutée avec succès.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Ajouter une réclamation sans analyse d'IA (méthode originale conservée au cas où)
    public boolean ajouterReclamationSansIA(Reclamation r) {
        String query = "INSERT INTO reclamation (id_user, nom_user, mail_user, title, content, status, date, type_id, priorite, image, archive, etat_rec, etat_user) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, r.getIdUser());
            pst.setString(2, r.getNomUser());
            pst.setString(3, r.getMailUser());
            pst.setString(4, r.getTitle());
            pst.setString(5, r.getContent());
            pst.setString(6, r.getStatus());
            pst.setTimestamp(7, Timestamp.valueOf(r.getDate()));
            pst.setInt(8, r.getType());
            pst.setInt(9, r.getPriorite());
            pst.setString(10, r.getImage());
            pst.setString(11, r.isArchive());
            pst.setString(12, r.getEtatRec());
            pst.setString(13, r.getEtatUser());

            pst.executeUpdate();
            System.out.println("Réclamation ajoutée avec succès.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Afficher les réclamations non archivées (archive = "non")
    public List<Reclamation> afficherReclamations() {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, t.nom as type_nom FROM reclamation r " +
                "LEFT JOIN type_rec t ON r.type_id = t.id " +
                "WHERE r.archive = 'non'";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Reclamation r = new Reclamation(
                        rs.getInt("id"),
                        rs.getInt("id_user"),
                        rs.getString("nom_user"),
                        rs.getString("mail_user"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("status"),
                        rs.getTimestamp("date").toLocalDateTime(),
                        rs.getInt("type_id"),
                        rs.getInt("priorite"),
                        rs.getString("image"),
                        rs.getString("archive"),
                        rs.getString("etat_rec"),
                        rs.getString("etat_user")
                );
                r.setTypeNom(rs.getString("type_nom")); // Nouvelle ligne
                reclamations.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reclamations;
    }

    // Afficher les réclamations archivées (archive = "oui")
    public List<Reclamation> afficherReclamationsArchive() {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, t.nom as type_nom FROM reclamation r " +
                "LEFT JOIN type_rec t ON r.type_id = t.id " +
                "WHERE r.archive = 'oui'";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                Reclamation r = new Reclamation(
                        rs.getInt("id"),
                        rs.getInt("id_user"),
                        rs.getString("nom_user"),
                        rs.getString("mail_user"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("status"),
                        rs.getTimestamp("date").toLocalDateTime(),
                        rs.getInt("type_id"),
                        rs.getInt("priorite"),
                        rs.getString("image"),
                        rs.getString("archive"),
                        rs.getString("etat_rec"),
                        rs.getString("etat_user")
                );
                r.setTypeNom(rs.getString("type_nom")); // Nouvelle ligne
                reclamations.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reclamations;
    }

    // Modifier une réclamation avec analyse d'IA à nouveau
    public boolean modifierReclamation(Reclamation r) {
        // Refaire l'analyse IA lors de la modification
        try {
            // Pour predictetat_rec : on envoie le contenu original et on récupère la chaîne "positive" ou "negative"
            String recResponse = callFlaskAPI(
                    "http://localhost:5001/predictetat_rec",
                    "{\"text\":\"" + r.getContent() + "\"}",
                    "etat_rec",
                    -1
            );
            r.setEtatRec(recResponse);

            // Pour predictetat_user : on envoie le contenu dans un tableau et on récupère le premier élément de "predictions"
            String userResponse = callFlaskAPI(
                    "http://localhost:5001/predictetat_user",
                    "{\"texts\":[\"" + r.getContent() + "\"]}",
                    "predictions",
                    0
            );
            r.setEtatUser(userResponse);
        } catch (Exception e) {
            System.err.println("Erreur lors des appels aux API : " + e.getMessage());
            // Ne pas changer les valeurs existantes en cas d'erreur
        }

        String query = "UPDATE reclamation SET type_id = ?, id_user = ?, nom_user = ?, mail_user = ?, title = ?, content = ?, status = ?, date = ?, priorite = ?, image = ?, archive = ?, etat_rec = ?, etat_user = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, r.getType());
            pst.setInt(2, r.getIdUser());
            pst.setString(3, r.getNomUser());
            pst.setString(4, r.getMailUser());
            pst.setString(5, r.getTitle());
            pst.setString(6, r.getContent());
            pst.setString(7, r.getStatus());
            pst.setTimestamp(8, Timestamp.valueOf(r.getDate()));
            pst.setInt(9, r.getPriorite());
            pst.setString(10, r.getImage());
            pst.setString(11, r.isArchive());
            pst.setString(12, r.getEtatRec());
            pst.setString(13, r.getEtatUser());
            pst.setInt(14, r.getId());

            pst.executeUpdate();
            System.out.println("Réclamation modifiée avec succès.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Modifier une réclamation sans mise à jour de l'analyse IA
    public boolean modifierReclamationSansIA(Reclamation r) {
        String query = "UPDATE reclamation SET type_id = ?, id_user = ?, nom_user = ?, mail_user = ?, title = ?, content = ?, status = ?, date = ?, priorite = ?, image = ?, archive = ?, etat_rec = ?, etat_user = ? WHERE id = ?";
        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, r.getType());
            pst.setInt(2, r.getIdUser());
            pst.setString(3, r.getNomUser());
            pst.setString(4, r.getMailUser());
            pst.setString(5, r.getTitle());
            pst.setString(6, r.getContent());
            pst.setString(7, r.getStatus());
            pst.setTimestamp(8, Timestamp.valueOf(r.getDate()));
            pst.setInt(9, r.getPriorite());
            pst.setString(10, r.getImage());
            pst.setString(11, r.isArchive());
            pst.setString(12, r.getEtatRec());
            pst.setString(13, r.getEtatUser());
            pst.setInt(14, r.getId());

            pst.executeUpdate();
            System.out.println("Réclamation modifiée avec succès.");
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Supprimer une réclamation
    public void supprimerReclamation(int id) {
        String deleteReponsesQuery = "DELETE FROM reponses WHERE id_reclamation_id = ?";
        String deleteReclamationQuery = "DELETE FROM reclamation WHERE id = ?";

        try {
            // Désactiver l'auto-commit pour faire une transaction
            connection.setAutoCommit(false);

            // Supprimer les réponses liées
            try (PreparedStatement pst1 = connection.prepareStatement(deleteReponsesQuery)) {
                pst1.setInt(1, id);
                pst1.executeUpdate();
            }

            // Supprimer la réclamation
            try (PreparedStatement pst2 = connection.prepareStatement(deleteReclamationQuery)) {
                pst2.setInt(1, id);
                int rowsDeleted = pst2.executeUpdate();

                if (rowsDeleted > 0) {
                    System.out.println("Réclamation et ses réponses supprimées avec succès.");
                } else {
                    System.out.println("Aucune réclamation trouvée avec l'ID: " + id);
                }
            }

            // Valider la transaction
            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback(); // Annuler en cas d'erreur
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                connection.setAutoCommit(true); // Revenir au comportement par défaut
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Récupérer les réclamations d'un utilisateur spécifique
    public List<Reclamation> getReclamationsByUser(int idUser) {
        List<Reclamation> reclamations = new ArrayList<>();
        String query = "SELECT r.*, t.nom as type_nom FROM reclamation r " +
                "LEFT JOIN type_rec t ON r.type_id = t.id " +
                "WHERE r.id_user = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, idUser); // Bind the user ID

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reclamation r = new Reclamation(
                            rs.getInt("id"),
                            rs.getInt("id_user"),
                            rs.getString("nom_user"),
                            rs.getString("mail_user"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("status"),
                            rs.getTimestamp("date").toLocalDateTime(),
                            rs.getInt("type_id"),
                            rs.getInt("priorite"),
                            rs.getString("image"),
                            rs.getString("archive"),
                            rs.getString("etat_rec"),
                            rs.getString("etat_user")
                    );
                    r.setTypeNom(rs.getString("type_nom")); // Récupération du nom du type
                    reclamations.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reclamations;
    }

    // Méthode pour analyser seulement le contenu sans créer/modifier une réclamation
    public void analyserContenu(Reclamation r) {
        try {
            // Pour predictetat_rec
            String recResponse = callFlaskAPI(
                    "http://localhost:5000/predict/sentiment_rec",
                    "{\"text\":\"" + r.getContent() + "\"}",
                    "etat_rec",
                    -1
            );
            r.setEtatRec(recResponse);

            // Pour predictetat_user
            String userResponse = callFlaskAPI(
                    "http://localhost:5000/predict/sentiment_user",
                    "{\"texts\":[\"" + r.getContent() + "\"]}",
                    "predictions",
                    0
            );
            r.setEtatUser(userResponse);

            System.out.println("Analyse effectuée - État réclamation: " + recResponse + ", État utilisateur: " + userResponse);
        } catch (Exception e) {
            System.err.println("Erreur lors des appels aux API : " + e.getMessage());
            r.setEtatRec("inconnu");
            r.setEtatUser("inconnu");
        }
    }
}