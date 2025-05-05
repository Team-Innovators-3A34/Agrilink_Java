package org.example.demo.services.posts;

import org.example.demo.models.Comment;
import org.example.demo.models.Posts;

import org.example.demo.utils.dataBaseHelper;


import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostsService implements IService<Posts> {
    private Connection connection = dataBaseHelper.getInstance().getConnection();
    private CommentService commentService = new CommentService();
    private PostSentimentAPI sentimentAPI = new PostSentimentAPI();

    @Override
    public void ajouter(Posts post) throws SQLException {
        // First check if title and description are not empty
        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            throw new SQLException("Post title cannot be empty");
        }

        if (post.getDescription() == null || post.getDescription().trim().isEmpty()) {
            throw new SQLException("Post description cannot be empty");
        }

        // Check for duplicate posts
        if (isDuplicatePost(post.getTitle(), post.getDescription())) {
            throw new SQLException("DUPLICATE_POST");
        }

        // Analyze sentiment before saving
        sentimentAPI.analyzeAndUpdatePostSentiment(post);

        // Continue with the existing code to add the post
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        String req = "INSERT INTO posts (user_id_id, type, title, description, created_at, status, images, sentiment, sentiment_score) VALUES ('"
                + post.getUser_id_id() + "', '"
                + post.getType() + "', '"
                + post.getTitle() + "', '"
                + post.getDescription() + "', '"
                + currentTime + "', '"
                + post.getStatus() + "', '"
                + post.getImages() + "', '"
                + post.getSentiment() + "', '"
                + post.getSentimentScore() + "')";

        Statement st = null;
        try {
            st = connection.createStatement();
            st.executeUpdate(req, Statement.RETURN_GENERATED_KEYS);

            // Get the generated post ID
            ResultSet generatedKeys = st.getGeneratedKeys();
            if (generatedKeys.next()) {
                post.setId(generatedKeys.getInt(1));

                // If there are any comments in the post, save them
                if (post.getComments() != null && !post.getComments().isEmpty()) {
                    for (Comment comment : post.getComments()) {
                        comment.setPost_id_id(post.getId());
                        commentService.ajouter(comment);
                    }
                }
            }

            System.out.println("Post ajouté avec sentiment: " + post.getSentiment());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (st != null) {
                st.close();
            }
        }
    }

    @Override
    public void modifier(Posts post) throws SQLException {
        // Analyze sentiment before updating
        sentimentAPI.analyzeAndUpdatePostSentiment(post);

        PreparedStatement ps = null;
        try {
            String req = "UPDATE posts SET user_id_id = ?, type = ?, title = ?, description = ?, status = ?, images = ?, sentiment = ?, sentiment_score = ? WHERE id = ?";

            ps = connection.prepareStatement(req);
            ps.setInt(1, post.getUser_id_id());
            ps.setString(2, post.getType());
            ps.setString(3, post.getTitle());
            ps.setString(4, post.getDescription());
            ps.setString(5, post.getStatus());
            ps.setString(6, post.getImages());
            ps.setString(7, post.getSentiment());
            ps.setDouble(8, post.getSentimentScore());
            ps.setInt(9, post.getId());

            ps.executeUpdate();
            System.out.println("Post modifié avec sentiment: " + post.getSentiment());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    @Override
    public void supprimer(Posts post) throws SQLException {
        PreparedStatement ps = null;
        try {
            // First delete all comments associated with this post
            String deleteComments = "DELETE FROM comment WHERE post_id_id = ?";
            ps = connection.prepareStatement(deleteComments);
            ps.setInt(1, post.getId());
            ps.executeUpdate();
            ps.close();

            // Then delete the post
            String req = "DELETE FROM posts WHERE id = ?";
            ps = connection.prepareStatement(req);
            ps.setInt(1, post.getId());
            ps.executeUpdate();

            System.out.println("Post and its comments supprimé");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    @Override
    public List<Posts> rechercher() throws SQLException {
        ArrayList<Posts> postsList = new ArrayList<>();

        String req = "SELECT * FROM posts";
        Statement st = null;
        ResultSet rs = null;

        try {
            st = connection.createStatement();
            rs = st.executeQuery(req);

            while (rs.next()) {
                Posts post = new Posts(
                        rs.getInt("id"),
                        rs.getInt("user_id_id"),
                        rs.getString("type"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("created_at"),
                        rs.getString("status"),
                        rs.getString("images")
                );

                // Load sentiment if available
                try {
                    post.setSentiment(rs.getString("sentiment"));
                    post.setSentimentScore(rs.getDouble("sentiment_score"));
                } catch (SQLException e) {
                    // If sentiment columns don't exist, set default values
                    post.setSentiment(Posts.SENTIMENT_NEUTRAL);
                    post.setSentimentScore(0.5);
                }

                // Load comments for this post
                List<Comment> comments = commentService.getCommentsByPostId(post.getId());
                post.setComments(comments);

                // Set bidirectional relationship
                for (Comment comment : comments) {
                    comment.setPost(post);
                }

                postsList.add(post);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
        }

        return postsList;
    }

    /**
     * Updates the sentiment of a post in the database
     * @param post The post with updated sentiment information
     * @throws SQLException If there's a database error
     */
    public void updateSentiment(Posts post) throws SQLException {
        PreparedStatement ps = null;
        try {
            // Check if the post already has sentiment set
            if (post.getSentiment() == null || post.getSentiment().isEmpty()) {
                // If not, analyze it first
                sentimentAPI.analyzeAndUpdatePostSentiment(post);
            }

            String req = "UPDATE posts SET sentiment = ?, sentiment_score = ? WHERE id = ?";
            ps = connection.prepareStatement(req);
            ps.setString(1, post.getSentiment());
            ps.setDouble(2, post.getSentimentScore());
            ps.setInt(3, post.getId());

            int rowsUpdated = ps.executeUpdate();
            System.out.println("Post sentiment updated: " + post.getSentiment() +
                    " with score: " + post.getSentimentScore() +
                    " (Rows affected: " + rowsUpdated + ")");

            if (rowsUpdated == 0) {
                System.out.println("WARNING: No rows were updated. Post ID might not exist: " + post.getId());
            }
        } catch (SQLException e) {
            System.out.println("Error updating sentiment: " + e.getMessage());
            throw e;
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    /**
     * Analyzes and updates sentiment for all posts in the database
     * @return Number of posts updated
     * @throws SQLException If there's a database error
     */
    public int updateAllPostsSentiment() throws SQLException {
        List<Posts> posts = getAllPosts();
        int updatedCount = 0;

        for (Posts post : posts) {
            try {
                // Analyze the sentiment
                boolean analyzed = sentimentAPI.analyzeAndUpdatePostSentiment(post);

                if (analyzed) {
                    // Update the database
                    updateSentiment(post);
                    updatedCount++;
                }
            } catch (Exception e) {
                System.out.println("Error processing sentiment for post ID " + post.getId() + ": " + e.getMessage());
                // Continue with next post even if one fails
            }
        }

        System.out.println("Updated sentiment for " + updatedCount + " out of " + posts.size() + " posts");
        return updatedCount;
    }

    // Additional methods

    public void updateTitle(Posts post) throws SQLException {
        PreparedStatement ps = null;
        try {
            String req = "UPDATE posts SET title = ? WHERE id = ?";

            ps = connection.prepareStatement(req);
            ps.setString(1, post.getTitle());
            ps.setInt(2, post.getId());

            ps.executeUpdate();
            System.out.println("Post title updated");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }

    public boolean isDuplicatePost(String title, String description) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) FROM posts WHERE title = ? AND description = ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, title);
            ps.setString(2, description);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    }

    public boolean isDuplicatePostExcludingCurrent(String title, String description, int currentPostId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) FROM posts WHERE title = ? AND description = ? AND id != ?";
            ps = connection.prepareStatement(query);
            ps.setString(1, title);
            ps.setString(2, description);
            ps.setInt(3, currentPostId);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        }
    }

    // Close the connection when finished with the service
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public List<Posts> getAllPosts() throws SQLException {
        List<Posts> postsList = new ArrayList<>();
        String req = "SELECT * FROM posts";
        Statement st = null;
        ResultSet rs = null;

        try {
            st = connection.createStatement();
            rs = st.executeQuery(req);

            while (rs.next()) {
                Posts post = new Posts(
                        rs.getInt("id"),
                        rs.getInt("user_id_id"),
                        rs.getString("type"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("created_at"),
                        rs.getString("status"),
                        rs.getString("images")
                );

                // Try to get sentiment data if available
                try {
                    post.setSentiment(rs.getString("sentiment"));
                    post.setSentimentScore(rs.getDouble("sentiment_score"));
                } catch (SQLException e) {
                    // If columns don't exist, set defaults
                    post.setSentiment(Posts.SENTIMENT_NEUTRAL);
                    post.setSentimentScore(0.5);
                }

                postsList.add(post);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (st != null) st.close();
        }

        return postsList;
    }

    /**
     * Gets posts filtered by sentiment
     * @param sentiment The sentiment to filter by (e.g., "positive", "negative", "neutral")
     * @return List of posts with the specified sentiment
     * @throws SQLException If there's a database error
     */
    public List<Posts> getPostsBySentiment(String sentiment) throws SQLException {
        List<Posts> postsList = new ArrayList<>();
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String req = "SELECT * FROM posts WHERE sentiment = ?";
            ps = connection.prepareStatement(req);
            ps.setString(1, sentiment);
            rs = ps.executeQuery();

            while (rs.next()) {
                Posts post = new Posts(
                        rs.getInt("id"),
                        rs.getInt("user_id_id"),
                        rs.getString("type"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("created_at"),
                        rs.getString("status"),
                        rs.getString("images")
                );

                post.setSentiment(rs.getString("sentiment"));
                post.setSentimentScore(rs.getDouble("sentiment_score"));

                postsList.add(post);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
        }

        return postsList;
    }
}