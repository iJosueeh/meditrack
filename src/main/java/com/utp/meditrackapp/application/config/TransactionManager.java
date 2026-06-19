package com.utp.meditrackapp.application.config;

import com.utp.meditrackapp.core.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestor centralizado de transacciones ACID.
 * Centraliza la lógica de rollback/close duplicada en los Services.
 */
public class TransactionManager {
    private final DatabaseConfig dbConfig;

    public TransactionManager() {
        this.dbConfig = DatabaseConfig.getInstance();
    }

    /**
     * Ejecuta una operación dentro de una transacción ACID.
     * Maneja automáticamente begin, commit, rollback y cleanup.
     *
     * @param operation La operación a ejecutar. Recibe la Connection activa.
     */
    public void execute(TransactionOperation operation) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);

            operation.execute(conn);

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            throw e;
        } finally {
            close(conn);
        }
    }

    /**
     * Ejecuta una operación transaccional y retorna un resultado.
     */
    public <T> T executeWithResult(TransactionResultOperation<T> operation) throws SQLException {
        Connection conn = null;
        try {
            conn = dbConfig.getConnection();
            conn.setAutoCommit(false);

            T result = operation.execute(conn);

            conn.commit();
            return result;
        } catch (SQLException e) {
            rollback(conn);
            throw e;
        } finally {
            close(conn);
        }
    }

    private void rollback(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void close(Connection conn) {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FunctionalInterface
    public interface TransactionOperation {
        void execute(Connection conn) throws SQLException;
    }

    @FunctionalInterface
    public interface TransactionResultOperation<T> {
        T execute(Connection conn) throws SQLException;
    }
}
