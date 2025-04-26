package org.example.demo.services.user;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.utils.EmailSender;
import org.example.demo.utils.dataBaseHelper;
import org.example.demo.utils.sessionManager;
import org.mindrot.jbcrypt.BCrypt;

import javax.mail.MessagingException;


public class userService implements userInterface {

    public boolean login(String username, String password) {

        if (sessionManager.getInstance().isSessionActive()) {
            System.out.println("Un utilisateur est déjà connecté !");
            return false;
        }

        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Retrieve the hashed password, failed login attempts, and lock status from the database
                String storedHashedPassword = rs.getString("password");
                Integer failed_login_attempts = rs.getInt("failed_login_attempts");

                // If failed_login_attempts is NULL, treat it as 0
                if (failed_login_attempts == null) {
                    failed_login_attempts = 0;
                }

                Timestamp lockUntilTimestamp = rs.getTimestamp("lock_until");

                // Check if the account is locked and if 15 minutes have passed
                if (lockUntilTimestamp != null) {
                    long lockUntilMillis = lockUntilTimestamp.getTime();
                    long currentTimeMillis = System.currentTimeMillis();
                    // If the account is locked, check if the lock period has passed
                    if (currentTimeMillis < lockUntilMillis) {
                        long remainingLockTime = (lockUntilMillis - currentTimeMillis) / 1000;
                        HelloApplication.error("Votre compte est verrouillé. Veuillez réessayer dans " + remainingLockTime + " secondes.");
                        return false;
                    } else {
                        // Lock period has passed, reset failed login attempts and remove lock
                        String resetFailedAttemptsQuery = "UPDATE user SET failed_login_attempts = 0, lock_until = NULL WHERE email = ?";
                        try (PreparedStatement stmtReset = conn.prepareStatement(resetFailedAttemptsQuery)) {
                            stmtReset.setString(1, username);
                            stmtReset.executeUpdate();
                        }
                    }
                }

                // Now check the password
                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    // Successful login, reset failed login attempts and lock_until
                    String resetFailedAttemptsQuery = "UPDATE user SET failed_login_attempts = 0, lock_until = NULL WHERE email = ?";
                    try (PreparedStatement stmtReset = conn.prepareStatement(resetFailedAttemptsQuery)) {
                        stmtReset.setString(1, username);
                        stmtReset.executeUpdate();
                    }

                    User user = new User(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("adresse"),
                            rs.getString("nom"),
                            rs.getString("prenom"),
                            rs.getString("telephone"),
                            rs.getString("roles"),
                            rs.getString("status"),
                            rs.getString("description"),
                            rs.getString("bio"),
                            rs.getString("image"),
                            rs.getString("account_verification"),
                            rs.getString("country"),
                            rs.getString("city"),
                            rs.getFloat("longitude"),
                            rs.getFloat("latitude"),
                            rs.getBoolean("is2_fa"),
                            rs.getInt("score"),
                            rs.getTimestamp("create_at").toLocalDateTime()
                    );

                    // Set session details
                    /*sessionManager.getInstance().setUser(
                            rs.getInt("id"),
                            rs.getString("email"),
                            rs.getString("roles"),
                            rs.getString("status"),
                            rs.getString("account_verification"),
                            rs.getBoolean("is2_fa")
                    );*/

                    sessionManager.getInstance().setUser(user);

                    return true;
                } else {
                    // Increment failed login attempts
                    String updateFailedAttemptsQuery = "UPDATE user SET failed_login_attempts = failed_login_attempts + 1 WHERE email = ?";
                    try (PreparedStatement stmtUpdate = conn.prepareStatement(updateFailedAttemptsQuery)) {
                        stmtUpdate.setString(1, username);
                        stmtUpdate.executeUpdate();
                    }

                    // If failed login attempts reach 3, lock the account for 15 minutes
                    if (failed_login_attempts + 1 >= 3) {
                        String lockAccountQuery = "UPDATE user SET lock_until = ? WHERE email = ?";
                        try (PreparedStatement stmtLock = conn.prepareStatement(lockAccountQuery)) {
                            long lockDuration = 15 * 60 * 1000; // 15 minutes in milliseconds
                            Timestamp lockUntil = new Timestamp(System.currentTimeMillis() + lockDuration);
                            stmtLock.setTimestamp(1, lockUntil);
                            stmtLock.setString(2, username);
                            stmtLock.executeUpdate();
                        }
                        HelloApplication.error("Votre compte est temporairement bloqué ! Veuillez réessayer plus tard.");
                    } else {
                        HelloApplication.error("Mot de passe incorrect !");
                    }
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean loginGoogle(String email) {

        if (sessionManager.getInstance().isSessionActive()) {
            System.out.println("Un utilisateur est déjà connecté !");
            return false;
        }

        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("telephone"),
                        rs.getString("roles"),
                        rs.getString("status"),
                        rs.getString("description"),
                        rs.getString("bio"),
                        rs.getString("image"),
                        rs.getString("accountVerification"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getFloat("longitude"),
                        rs.getFloat("latitude"),
                        rs.getBoolean("is2FA"),
                        rs.getInt("score"),
                        rs.getTimestamp("createAt").toLocalDateTime()
                );
                /*sessionManager.getInstance().setUser(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("roles"),
                        rs.getString("status"),
                        rs.getString("account_verification"),
                        rs.getBoolean("is2_fa")
                    );*/
                sessionManager.getInstance().setUser(user);

                return true;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean Register(User user) {

        if (sessionManager.getInstance().isSessionActive()) {
            System.out.println("Un utilisateur est déjà connecté !");
            return false;
        }

        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "INSERT INTO user (email,nom,prenom,adresse,roles,telephone,status,account_verification,is2_fa,score,password,image,create_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getNom());
            stmt.setString(3, user.getPrenom());
            stmt.setString(4, user.getAdresse());
            stmt.setString(5, user.getRoles());
            stmt.setString(6, user.getTelephone());
            stmt.setString(7, "show");
            stmt.setString(8, "pending");
            stmt.setBoolean(9, false);
            stmt.setInt(10, 50);
            stmt.setString(11, user.getPassword());
            stmt.setString(12, user.getImage());
            stmt.setTimestamp(13, new Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            return true;

        }catch (SQLException e) {
            e.printStackTrace();
        }
            return false;
    }

    public boolean checkUniqueEmail(String email) {

        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);

            if (stmt.executeQuery().next()) {
                return true;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkVerifyUser(String email) {

        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT account_verification FROM user WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String accountVerification = rs.getString("account_verification");
                if ("approved".equals(accountVerification)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkBannedUser(String email) {

        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT status FROM user WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String accountVerification = rs.getString("status");
                if ("hide".equals(accountVerification)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void SendCodeValidationAccount(String email) {
        Connection conn = dataBaseHelper.getInstance().getConnection();

        Random random = new Random();
        String code = String.format("%04d", random.nextInt(10000));  // Generates a 4-digit string (e.g., "0071", "1234")

        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expirationDate = expirationTime.format(formatter);

        String query = "UPDATE user SET verification_code = ?, code_expiration_date = ? WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, code);
            stmt.setString(2, expirationDate);
            stmt.setString(3, email);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                EmailSender.send(email,"Votre code de validation de compte est: " + code + ", ne le partage avec personne!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean ConfirmCodeValidationAccount(String email, String code) {

        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT verification_code, code_expiration_date FROM user WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String verificationCode = rs.getString("verification_code");
                Timestamp expirationTimestamp = rs.getTimestamp("code_expiration_date");

                LocalDateTime expirationDate = expirationTimestamp.toLocalDateTime();


                LocalDateTime now = LocalDateTime.now();


                if (verificationCode.equals(code) && expirationDate.isAfter(now)) {
                    String updateQuery = "UPDATE user SET account_verification = ? , verification_code = NULL , code_expiration_date = NULL  WHERE email = ?";
                    try (PreparedStatement stmt2 = conn.prepareStatement(updateQuery)) {
                        stmt2.setString(1, "approved");
                        stmt2.setString(2, email);
                        stmt2.executeUpdate();
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void SendCodeResetPassword(String email) {
        Connection conn = dataBaseHelper.getInstance().getConnection();

        Random random = new Random();
        String code = String.format("%04d", random.nextInt(10000));

        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String expirationDate = expirationTime.format(formatter);

        String query = "UPDATE user SET reset_password_code = ?, reset_password_code_expires_at = ? WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, code);
            stmt.setString(2, expirationDate);
            stmt.setString(3, email);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                EmailSender.send(email,"Votre code de reinitialisation du mot de passe est: " + code + ", ne le partage avec personne!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    public Boolean ResetPassword(String email, String code, String password) {

        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT reset_password_code, reset_password_code_expires_at FROM user WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String verificationCode = rs.getString("reset_password_code");
                Timestamp expirationTimestamp = rs.getTimestamp("reset_password_code_expires_at");

                LocalDateTime expirationDate = expirationTimestamp.toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();


                if (verificationCode.equals(code) && expirationDate.isAfter(now)) {
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(13));
                    String updateQuery = "UPDATE user SET password = ? , reset_password_code = NULL , reset_password_code_expires_at = NULL  WHERE email = ?";
                    try (PreparedStatement stmt2 = conn.prepareStatement(updateQuery)) {
                        stmt2.setString(1, hashedPassword);
                        stmt2.setString(2, email);
                        stmt2.executeUpdate();
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT id, email, nom, prenom, adresse, roles, telephone, status, account_verification, is2_fa, score, image FROM user";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            // Process the result set
            while (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String adresse = rs.getString("adresse");
                String roles = rs.getString("roles");
                String telephone = rs.getString("telephone");
                String status = rs.getString("status");
                String accountVerification = rs.getString("account_verification");
                boolean is2FA = rs.getBoolean("is2_fa");
                int score = rs.getInt("score");
                String image = rs.getString("image");

                // Create a new User object and add it to the list
                users.add(new User(email, nom, prenom, adresse, roles, telephone, accountVerification, image));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public void DeleteUser(String email) {
        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "DELETE FROM user WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.executeUpdate(); // Corrected to executeUpdate() for delete operation
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateUser(User user) {
        try {
            Connection conn = dataBaseHelper.getInstance().getConnection();
            String query = "UPDATE user SET nom = ?, prenom = ?, email = ?, adresse = ?, telephone = ?, bio = ?, description = ?, is2_fa = ? WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, user.getNom());
            stmt.setString(2, user.getPrenom());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getAdresse());
            stmt.setString(5, user.getTelephone());
            stmt.setString(6, user.getBio());
            stmt.setString(7, user.getDescription());
            stmt.setBoolean(8, user.getIs2FA());
            stmt.setInt(9, user.getId());

            stmt.executeUpdate();
            stmt.close();

            System.out.println("Mise à jour réussie !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyPassword(User user, String oldPassword) {
        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT password FROM user WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");
                return BCrypt.checkpw(oldPassword, storedHashedPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updatePassword(User user, String newPassword) {
        Connection conn = dataBaseHelper.getInstance().getConnection();
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(13));
        String query = "UPDATE user SET password = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, user.getId());
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateProfileImage(User user,String newImage){
        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "UPDATE user SET image = ? WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, newImage);
            stmt.setInt(2, user.getId());
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User searchUserByEmail(String email) {
        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT * FROM user WHERE email = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("telephone"),
                        rs.getString("roles"),
                        rs.getString("status"),
                        rs.getString("description"),
                        rs.getString("bio"),
                        rs.getString("image"),
                        rs.getString("account_verification"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getFloat("longitude"),
                        rs.getFloat("latitude"),
                        rs.getBoolean("is2_fa"),
                        rs.getInt("score"),
                        rs.getTimestamp("create_at").toLocalDateTime()
                );
                return user;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //added by chirine to display the user's name on posts
    public User getUserById(int userId) {
        Connection conn = dataBaseHelper.getInstance().getConnection();
        String query = "SELECT * FROM user WHERE id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("adresse"),
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("telephone"),
                        rs.getString("roles"),
                        rs.getString("status"),
                        rs.getString("description"),
                        rs.getString("bio"),
                        rs.getString("image"),
                        rs.getString("account_verification"),
                        rs.getString("country"),
                        rs.getString("city"),
                        rs.getFloat("longitude"),
                        rs.getFloat("latitude"),
                        rs.getBoolean("is2_fa"),
                        rs.getInt("score"),
                        rs.getTimestamp("create_at").toLocalDateTime()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
