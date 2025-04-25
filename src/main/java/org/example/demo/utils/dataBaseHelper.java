package org.example.demo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dataBaseHelper {

    private static final String URL = "jdbc:mysql://localhost:3306/agrilink";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static dataBaseHelper instance;
    private Connection connection;

    private dataBaseHelper() {

        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données : " + e.getMessage());
        }

    }

    public static dataBaseHelper getInstance() {
        if (instance == null) {
            instance = new dataBaseHelper();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

}
