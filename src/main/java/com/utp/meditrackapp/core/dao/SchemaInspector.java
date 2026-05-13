package com.utp.meditrackapp.core.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

final class SchemaInspector {
    private SchemaInspector() {
    }

    static boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(connection.getCatalog(), "dbo", tableName, columnName)) {
            return columns.next();
        }
    }
}