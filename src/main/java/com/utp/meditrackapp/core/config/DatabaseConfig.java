package com.utp.meditrackapp.core.config;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private static DatabaseConfig instance;
    private final String connectionUrl;

    private DatabaseConfig() {
        Dotenv dotenv = Dotenv.load();

        String dbHost = dotenv.get("DB_HOST", "localhost");
        String dbPort = dotenv.get("DB_PORT", "1433");
        String dbName = dotenv.get("DB_NAME");
        String dbUser = dotenv.get("DB_USER");
        String dbPassword = dotenv.get("DB_PASSWORD");

        String trustCert = dotenv.get("DB_TRUST_SERVER_CERTIFICATE", "false");

        this.connectionUrl = String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;user=%s;password=%s;encrypt=true;trustServerCertificate=%s;",
                dbHost, dbPort, dbName, dbUser, dbPassword, trustCert
        );
   }

    public String getConnectionUrl() {
        return this.connectionUrl;
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(getConnectionUrl());
        } catch (SQLException error) {
            System.err.println("[DB ERROR] Falló la conexión: " + error.getMessage());
            throw error;
        }
    }
}
