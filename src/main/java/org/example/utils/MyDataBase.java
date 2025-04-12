package org.example.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static final String URL = "jdbc:mysql://localhost:3306/agrilinkk?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Mot de passe vide
    private static Connection connection;

    // Constructeur privé pour Singleton
    private MyDataBase() {}

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Vérifier si le driver est disponible
                Class.forName("com.mysql.cj.jdbc.Driver");
                System.out.println("Driver MySQL trouvé !");

                // Tenter la connexion
                System.out.println("🔄 Tentative de connexion à MySQL...");
                System.out.println("URL: " + URL);
                System.out.println("Utilisateur: " + USER);

                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Connexion à la base de données établie !");

            } catch (SQLException e) {
                System.err.println("Erreur de connexion à la base de données !");
                System.err.println("Message d'erreur : " + e.getMessage());
                System.err.println("Code d'erreur SQL : " + e.getErrorCode());
                System.err.println("État SQL : " + e.getSQLState());

                // Suggestions basées sur le code d'erreur
                if (e.getErrorCode() == 1045) {
                    System.err.println("Le mot de passe ou l'utilisateur est incorrect.");
                    System.err.println("   Vérifiez vos identifiants MySQL dans le code.");
                } else if (e.getErrorCode() == 1049) {
                    System.err.println("La base de données 'gestion_reclamations' n'existe pas.");
                    System.err.println("   Créez-la avec la commande : CREATE DATABASE gestion_reclamations;");
                }

                e.printStackTrace();
                return null; // Retourner null en cas d'erreur
            } catch (ClassNotFoundException e) {
                System.err.println("Driver MySQL non trouvé !");
                System.err.println("Vérifiez que mysql-connector-java est dans votre pom.xml");
                e.printStackTrace();
                return null; // Retourner null en cas d'erreur
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null; // Réinitialiser la connexion
                System.out.println("Connexion à la base de données fermée !");
            } catch (SQLException e) {
                System.err.println("Erreur lors de la fermeture de la connexion !");
                e.printStackTrace();
            }
        }
    }
}


