package org.example.demo.services.event;

import org.example.demo.models.event;
import org.example.demo.models.categorie;
import org.example.demo.utils.dataBaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class eventService implements Interface<event> {

    private final Connection connection = dataBaseHelper.getInstance().getConnection();
    private final catService catService = new catService();

    @Override
    public void ajouter(event event) {
        String req = "INSERT INTO event (categorie_id, nom, date, adresse, description, image, nbr_places, longitude, latitude, type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, event.getCategorie().getId());
            pst.setString(2, event.getNom());
            pst.setString(3, event.getDate());
            pst.setString(4, event.getAdresse());
            pst.setString(5, event.getDescription());
            pst.setString(6, event.getImage());
            pst.setInt(7, event.getNbrPlaces());
            pst.setDouble(8, event.getLongitude());
            pst.setDouble(9, event.getLatitude());
            pst.setString(10, event.getType());

            pst.executeUpdate();
            System.out.println("✅ Événement ajouté !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout de l'événement : " + e.getMessage());
        }
    }

    @Override
    public void modifier(event event) {
        String req = "UPDATE event SET categorie_id=?, nom=?, date=?, adresse=?, description=?, image=?, nbr_places=?, longitude=?, latitude=?, type=? WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, event.getCategorie().getId());
            pst.setString(2, event.getNom());
            pst.setString(3, event.getDate());
            pst.setString(4, event.getAdresse());
            pst.setString(5, event.getDescription());
            pst.setString(6, event.getImage());
            pst.setInt(7, event.getNbrPlaces());
            pst.setDouble(8, event.getLongitude());
            pst.setDouble(9, event.getLatitude());
            pst.setString(10, event.getType());
            pst.setInt(11, event.getId());

            pst.executeUpdate();
            System.out.println("✅ Événement modifié !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }

    @Override
    public void supprimer(event event) {
        String req = "DELETE FROM event WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, event.getId());
            pst.executeUpdate();
            System.out.println("🗑️ Événement supprimé !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public List<event> rechercher() {
        List<event> events = new ArrayList<>();
        String req = "SELECT * FROM event";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                int id = rs.getInt("id");
                int catId = rs.getInt("categorie_id");
                String nom = rs.getString("nom");
                String date = rs.getString("date");
                String adresse = rs.getString("adresse");
                String description = rs.getString("description");
                String image = rs.getString("image");
                int nbrPlaces = rs.getInt("nbr_places");
                double longitude = rs.getDouble("longitude");
                double latitude = rs.getDouble("latitude");
                String type = rs.getString("type");

                // Récupérer la catégorie correspondante
                categorie cat = catService.rechercher().stream()
                        .filter(c -> c.getId() == catId)
                        .findFirst()
                        .orElse(null);

                events.add(new event(id, nom, adresse, longitude, latitude, date, type, nbrPlaces, image, description, cat));
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des événements : " + e.getMessage());
        }

        return events;
    }
}
