package org.example.demo.services.posts;

import org.example.demo.models.Comment;
import org.example.demo.utils.dataBaseHelper;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CommentService implements IService<Comment> {


    private Connection connection = dataBaseHelper.getInstance().getConnection();


    @Override
    public void ajouter(Comment comment) throws SQLException {
        String query = "INSERT INTO comment (post_id_id, content, user_commented_id, created_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pst = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pst.setInt(1, comment.getPost_id_id());
            pst.setString(2, comment.getContent());

            // Utilise 1 comme valeur par défaut si aucun ID utilisateur n'est fourni
            int userId = comment.getUser_commented_id();
            if (userId == 0) {
                userId = 1; // valeur par défaut
            }
            pst.setInt(3, userId);
            // Timestamp actuel si null
            String created_at = comment.getCreated_at();
            if (created_at == null) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                created_at = now.format(formatter);
            }
            pst.setString(4, created_at);

            pst.executeUpdate();

            ResultSet generatedKeys = pst.getGeneratedKeys();
            if (generatedKeys.next()) {
                comment.setId(generatedKeys.getInt(1));
            }
        }
    }

    @Override
    public void modifier(Comment comment) throws SQLException {
        String query = "UPDATE comment SET content = ?, user_commented_id = ? WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setString(1, comment.getContent());

            int userId = comment.getUser_commented_id();
            if (userId == 0) {
                userId = 1; // valeur par défaut
            }
            pst.setInt(2, userId);

            pst.setInt(3, comment.getId());

            pst.executeUpdate();
        }
    }

    @Override
    public void supprimer(Comment comment) throws SQLException {
        String query = "DELETE FROM comment WHERE id = ?";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, comment.getId());

            pst.executeUpdate();
        }
    }

    @Override
    public List<Comment> rechercher() throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comment";

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setPost_id_id(rs.getInt("post_id_id"));
                comment.setContent(rs.getString("content"));
                comment.setUser_commented_id(rs.getInt("user_commented_id"));
                comment.setCreated_at(rs.getString("created_at"));

                comments.add(comment);
            }
        }

        return comments;
    }

    public List<Comment> getCommentsByPostId(int postId) throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT * FROM comment WHERE post_id_id = ? ORDER BY created_at DESC";

        try (PreparedStatement pst = connection.prepareStatement(query)) {
            pst.setInt(1, postId);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Comment comment = new Comment();
                    comment.setId(rs.getInt("id"));
                    comment.setPost_id_id(rs.getInt("post_id_id"));
                    comment.setContent(rs.getString("content"));
                    comment.setUser_commented_id(rs.getInt("user_commented_id"));
                    comment.setCreated_at(rs.getString("created_at"));

                    comments.add(comment);
                }
            }
        }

        return comments;
    }
}
