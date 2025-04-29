package org.example.demo.services.posts;

import org.example.demo.models.Reaction;
import org.example.demo.utils.dataBaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReactionService implements IService<Reaction> {

    private Connection connection = dataBaseHelper.getInstance().getConnection();

    @Override
    public void ajouter(Reaction reaction) throws SQLException {
        String query = "INSERT INTO reaction (post_id, user_id, type, created_at) VALUES (?, ?, ?, ?)";

        PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        ps.setInt(1, reaction.getPost_id());
        ps.setInt(2, reaction.getUser_id());
        ps.setString(3, reaction.getType());
        ps.setString(4, reaction.getCreated_at());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            reaction.setId(rs.getInt(1));
        }

        ps.close();
    }

    @Override
    public void modifier(Reaction reaction) throws SQLException {
        String query = "UPDATE reaction SET post_id = ?, user_id = ?, type = ?, created_at = ? WHERE id = ?";

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, reaction.getPost_id());
        ps.setInt(2, reaction.getUser_id());
        ps.setString(3, reaction.getType());
        ps.setString(4, reaction.getCreated_at());
        ps.setInt(5, reaction.getId());

        ps.executeUpdate();
        ps.close();
    }

    @Override
    public void supprimer(Reaction reaction) throws SQLException {
        String query = "DELETE FROM reaction WHERE id = ?";

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, reaction.getId());

        ps.executeUpdate();
        ps.close();
    }

    @Override
    public List<Reaction> rechercher() throws SQLException {
        List<Reaction> reactions = new ArrayList<>();
        String query = "SELECT * FROM reaction";

        Statement st = connection.createStatement();
        ResultSet rs = st.executeQuery(query);

        while (rs.next()) {
            Reaction reaction = new Reaction();
            reaction.setId(rs.getInt("id"));
            reaction.setPost_id(rs.getInt("post_id"));
            reaction.setUser_id(rs.getInt("user_id"));
            reaction.setType(rs.getString("type"));
            reaction.setCreated_at(rs.getString("created_at"));

            reactions.add(reaction);
        }

        rs.close();
        st.close();
        return reactions;
    }

    public List<Reaction> getReactionsByPostId(int postId) throws SQLException {
        List<Reaction> reactions = new ArrayList<>();
        String query = "SELECT * FROM reaction WHERE post_id = ?";

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, postId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Reaction reaction = new Reaction();
            reaction.setId(rs.getInt("id"));
            reaction.setPost_id(rs.getInt("post_id"));
            reaction.setUser_id(rs.getInt("user_id"));
            reaction.setType(rs.getString("type"));
            reaction.setCreated_at(rs.getString("created_at"));

            reactions.add(reaction);
        }

        rs.close();
        ps.close();
        return reactions;
    }

    public List<Reaction> getReactionsByPostAndUser(int postId, int userId) throws SQLException {
        List<Reaction> reactions = new ArrayList<>();
        String query = "SELECT * FROM reaction WHERE post_id = ? AND user_id = ?";

        PreparedStatement ps = connection.prepareStatement(query);
        ps.setInt(1, postId);
        ps.setInt(2, userId);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Reaction reaction = new Reaction();
            reaction.setId(rs.getInt("id"));
            reaction.setPost_id(rs.getInt("post_id"));
            reaction.setUser_id(rs.getInt("user_id"));
            reaction.setType(rs.getString("type"));
            reaction.setCreated_at(rs.getString("created_at"));

            reactions.add(reaction);
        }

        rs.close();
        ps.close();
        return reactions;
    }
}