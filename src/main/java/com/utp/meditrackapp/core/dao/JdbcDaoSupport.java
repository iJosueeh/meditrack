package com.utp.meditrackapp.core.dao;

import com.utp.meditrackapp.core.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;

abstract class JdbcDaoSupport {
    private final DatabaseConfig databaseConfig = DatabaseConfig.getInstance();

    protected Connection getConnection() throws SQLException {
        return databaseConfig.getConnection();
    }
}