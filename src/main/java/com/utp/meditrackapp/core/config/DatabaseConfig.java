package com.utp.meditrackapp.core.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {

    private static DatabaseConfig instance;
    private final HikariDataSource dataSource;

    private DatabaseConfig() {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String dbHost = dotenv.get("DB_HOST", "localhost");
        String dbPort = dotenv.get("DB_PORT", "1433");
        String dbName = dotenv.get("DB_NAME");
        String dbUser = dotenv.get("DB_USER");
        String dbPassword = dotenv.get("DB_PASSWORD");
        boolean dbEncrypt = Boolean.parseBoolean(dotenv.get("DB_ENCRYPT", "false"));
        boolean dbTrustCert = Boolean.parseBoolean(dotenv.get("DB_TRUST_SERVER_CERTIFICATE", "true"));
        String hostNameInCert = dotenv.get("DB_HOST_NAME_IN_CERTIFICATE", "");

        System.out.println("[DB] Conectando a: " + dbHost + ":" + dbPort + " BD: " + dbName);

        StringBuilder urlBuilder = new StringBuilder(String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=%s;loginTimeout=30;sendStringParametersAsUnicode=true;characterEncoding=UTF-8;",
                dbHost, dbPort, dbName, dbEncrypt, dbTrustCert
        ));

        if (hostNameInCert != null && !hostNameInCert.isEmpty()) {
            urlBuilder.append("hostNameInCertificate=").append(hostNameInCert).append(";");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(urlBuilder.toString());
        config.setUsername(dbUser);
        config.setPassword(dbPassword);
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(15000);
        config.setPoolName("MediTrackPool");
        config.setInitializationFailTimeout(0);
        
        this.dataSource = new HikariDataSource(config);
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (SQLException error) {
            System.err.println("[DB ERROR] Falló la conexión: " + error.getMessage());
            throw error;
        }
    }

    public boolean isReachable() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}

