package com.utp.meditrackapp.core.util;

import com.utp.meditrackapp.core.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbInitializer {
    public static void initialize() {
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 1. Verificar en qué BD estamos realmente
            ResultSet rsDb = stmt.executeQuery("SELECT DB_NAME()");
            if (rsDb.next()) {
                System.out.println("[DB INIT] Conectado a la base de datos: " + rsDb.getString(1));
            }

            // 2. Crear tablas si no existen (una por una para evitar errores de bloque)
            stmt.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'roles') CREATE TABLE [roles] ([id] varchar(50) PRIMARY KEY, [nombre] varchar(100) UNIQUE NOT NULL)");
            stmt.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'sedes') CREATE TABLE [sedes] ([id] varchar(50) PRIMARY KEY, [nombre] varchar(255) NOT NULL, [direccion] varchar(500), [is_activa] int DEFAULT 1)");
            stmt.executeUpdate("IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'usuarios') CREATE TABLE [usuarios] ([id] varchar(50) PRIMARY KEY, [sede_id] varchar(50), [rol_id] varchar(50), [tipo_documento] varchar(50), [numero_documento] varchar(50) UNIQUE, [nombres] varchar(255), [apellidos] varchar(255), [password] varchar(max), [is_activo] int DEFAULT 1)");

            // 3. Insertar datos base si no existen
            stmt.executeUpdate("IF NOT EXISTS (SELECT 1 FROM roles WHERE id = 'ROL-001') INSERT INTO [roles] ([id], [nombre]) VALUES ('ROL-001', 'Administrador')");
            stmt.executeUpdate("IF NOT EXISTS (SELECT 1 FROM sedes WHERE id = 'SED-001') INSERT INTO [sedes] ([id], [nombre], [direccion]) VALUES ('SED-001', 'Sede Central Lima', 'Av. Principal 123')");
            
            // 4. EL PASO CRÍTICO: Asegurar el usuario admin
            stmt.executeUpdate("IF NOT EXISTS (SELECT 1 FROM usuarios WHERE numero_documento = '12345678') " +
                               "INSERT INTO [usuarios] ([id], [sede_id], [rol_id], [tipo_documento], [numero_documento], [nombres], [apellidos], [password], [is_activo]) " +
                               "VALUES ('USR-001', 'SED-001', 'ROL-001', 'DNI', '12345678', 'Admin', 'Sistema', 'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU=', 1)");

            // 5. Verificar conteo final
            ResultSet rsCount = stmt.executeQuery("SELECT COUNT(*) FROM usuarios");
            if (rsCount.next()) {
                System.out.println("[DB INIT] Total de usuarios en la tabla: " + rsCount.getInt(1));
            }

        } catch (Exception e) {
            System.err.println("[DB INIT] Error crítico: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
