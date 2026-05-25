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

        System.out.println("[DB DEBUG] Intentando conectar a: " + dbHost + ":" + dbPort + " BD: " + dbName + " como " + dbUser);

        // URL simplificada para evitar problemas de cifrado/certificados en local
        String url = String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=false;loginTimeout=10;",
                dbHost, dbPort, dbName
        );

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(dbUser);
        config.setPassword(dbPassword);
        config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(10000);
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

    /**
     * Verifica si la base de datos está disponible.
     * Útil para saltar tests de integración si no hay un servidor activo.
     */
    public boolean isReachable() {
        try (Connection conn = getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}

