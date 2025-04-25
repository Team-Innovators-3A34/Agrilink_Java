package org.example.demo.services.recyclingpoint;

import org.example.demo.models.recyclingpoint;
import org.example.demo.utils.dataBaseHelper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class recyclingpointService implements Interface<recyclingpoint> {

    private final Connection connection = dataBaseHelper.getInstance().getConnection();


    @Override
    public boolean ajouter(recyclingpoint recyclingpoint) {
        String req = "INSERT INTO pointrecyclage ( name, created_at, adresse, description, image, longitude, latitude, type,owner_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setString(1, recyclingpoint.getNom());
            pst.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(3, recyclingpoint.getAdresse());
            pst.setString(4, recyclingpoint.getDescription());
            pst.setString(5, recyclingpoint.getImage());
            pst.setDouble(6, recyclingpoint.getLongitude());
            pst.setDouble(7, recyclingpoint.getLatitude());
            pst.setString(8, recyclingpoint.getType());
            pst.setInt(9, recyclingpoint.getOwner_id());

            pst.executeUpdate();
            System.out.println("✅ Recycling Point ajouté !");
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout du point de recyclage : " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean modifier(recyclingpoint recyclingpoint) {
        String req = "UPDATE pointrecyclage SET  name=?,adresse=?, description=?, image=?, longitude=?, latitude=?, type=? WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setString(1, recyclingpoint.getNom());
            pst.setString(2, recyclingpoint.getAdresse());
            pst.setString(3, recyclingpoint.getDescription());
            pst.setString(4, recyclingpoint.getImage());
            pst.setDouble(5, recyclingpoint.getLongitude());
            pst.setDouble(6, recyclingpoint.getLatitude());
            pst.setString(7, recyclingpoint.getType());
            pst.setInt(8, recyclingpoint.getId());

            pst.executeUpdate();
            System.out.println("✅ Recycling Point modifié !");
            return true;
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la modification : " + e.getMessage());
            return false;
        }
    }

    @Override
    public void supprimer(recyclingpoint recyclingpoint) {
        String req = "DELETE FROM pointrecyclage WHERE id=?";
        try {
            PreparedStatement pst = connection.prepareStatement(req);
            pst.setInt(1, recyclingpoint.getId());
            pst.executeUpdate();
            System.out.println("🗑️ Recycling Point supprimé !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @Override
    public List<recyclingpoint> rechercher() {
        List<recyclingpoint> recyclingpoints = new ArrayList<>();
        String req = "SELECT * FROM pointrecyclage";
        try {
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(req);

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("name");
                String date = rs.getString("created_at");
                String adresse = rs.getString("adresse");
                String description = rs.getString("description");
                String image = rs.getString("image");
                double longitude = rs.getDouble("longitude");
                double latitude = rs.getDouble("latitude");
                String type = rs.getString("type");



                recyclingpoints.add(new recyclingpoint(id, nom, adresse, longitude, latitude, date, type, image, description));
            }

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des points de recyclage : " + e.getMessage());
        }

        return recyclingpoints;
    }
}
