-- =====================================================================
-- MediTrack - Script de Inicialización y Actualización de Base de Datos
-- Versión: 3.0 - RBAC con Sistema de Permisos + Bloqueo de Sedes
-- =====================================================================
-- Este script es idempotente: puede ejecutarse múltiples veces sin errores.
-- - Si las tablas no existen, las crea
-- - Si existen pero faltan columnas, las agrega
-- - Si existen las tablas y columnas, no hace cambios
-- =====================================================================
-- CREDENCIALES DE ACCESO (contraseña: admin123 para todos)
-- =====================================================================
-- ROL                     | DNI       | USUARIO   | CONTRASEÑA
-- ------------------------|-----------|-----------|------------
-- Administrador           | 12345678  | USR-001   | admin123
-- Químico Farmacéutico    | 22222222  | USR-002   | admin123
-- Técnico de Farmacia     | 33333333  | USR-003   | admin123
-- =====================================================================
-- NOTAS:
-- - El Administrador tiene acceso global a todas las sedes
-- - El Químico Farmacéutico tiene acceso de gestión a su sede
-- - El Técnico de Farmacia solo tiene acceso operativo a su sede
-- - Las sedes bloqueadas impiden a sus usuarios realizar operaciones
-- =====================================================================

USE [meditrack_db];
GO

-- =====================================================================
-- FASE 1: CREAR TABLAS QUE NO EXISTEN
-- =====================================================================

-- Tabla: roles
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='roles' AND xtype='U')
BEGIN
    CREATE TABLE [roles] (
        [id] varchar(50) PRIMARY KEY,
        [nombre] varchar(100) UNIQUE NOT NULL,
        [descripcion] varchar(255),
        [nivel] int DEFAULT 99,
        [is_sistema] int DEFAULT 0,
        [is_activo] int DEFAULT 1
    );
    PRINT 'Tabla [roles] creada.';
END
ELSE
BEGIN
    PRINT 'Tabla [roles] ya existe. Verificando columnas...';
END
GO

-- Tabla: permisos
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='permisos' AND xtype='U')
BEGIN
    CREATE TABLE [permisos] (
        [id] varchar(50) PRIMARY KEY,
        [codigo] varchar(50) UNIQUE NOT NULL,
        [nombre] varchar(100) NOT NULL,
        [descripcion] varchar(255),
        [modulo] varchar(50) NOT NULL,
        [orden] int DEFAULT 0,
        [is_activo] int DEFAULT 1
    );
    PRINT 'Tabla [permisos] creada.';
END
GO

-- Tabla: rol_permisos
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='rol_permisos' AND xtype='U')
BEGIN
    CREATE TABLE [rol_permisos] (
        [rol_id] varchar(50),
        [permiso_id] varchar(50),
        [fecha_asignacion] datetime DEFAULT GETDATE(),
        PRIMARY KEY ([rol_id], [permiso_id]),
        FOREIGN KEY ([rol_id]) REFERENCES [roles]([id]) ON DELETE CASCADE,
        FOREIGN KEY ([permiso_id]) REFERENCES [permisos]([id]) ON DELETE CASCADE
    );
    PRINT 'Tabla [rol_permisos] creada.';
END
GO

-- Tabla: sedes
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='sedes' AND xtype='U')
BEGIN
    CREATE TABLE [sedes] (
        [id] varchar(50) PRIMARY KEY,
        [nombre] varchar(255) NOT NULL,
        [direccion] varchar(500),
        [telefono] varchar(50),
        [ubigeo] varchar(20),
        [tipo_sede] varchar(50),
        [capacidad_almacen] int DEFAULT 0,
        [administrador_id] varchar(50),
        [is_activa] int DEFAULT 1,
        [is_bloqueada] int DEFAULT 0,
        [motivo_bloqueo] varchar(500),
        [fecha_bloqueo] datetime NULL
    );
    PRINT 'Tabla [sedes] creada.';
END
GO

-- Tabla: usuarios
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='usuarios' AND xtype='U')
BEGIN
    CREATE TABLE [usuarios] (
        [id] varchar(50) PRIMARY KEY,
        [sede_id] varchar(50),
        [rol_id] varchar(50),
        [tipo_documento] varchar(50),
        [numero_documento] varchar(50) UNIQUE,
        [nombres] varchar(255),
        [apellidos] varchar(255),
        [telefono] varchar(50),
        [ubigeo] varchar(20),
        [password] varchar(max),
        [is_activo] int DEFAULT 1
    );
    PRINT 'Tabla [usuarios] creada.';
END
GO

-- Tabla: pacientes
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='pacientes' AND xtype='U')
BEGIN
    CREATE TABLE [pacientes] (
        [id] varchar(50) PRIMARY KEY,
        [tipo_documento] varchar(50),
        [numero_documento] varchar(50) UNIQUE,
        [nombres] varchar(255),
        [apellidos] varchar(255),
        [telefono] varchar(50),
        [is_activo] int DEFAULT 1
    );
    PRINT 'Tabla [pacientes] creada.';
END
GO

-- Tabla: categorias
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='categorias' AND xtype='U')
BEGIN
    CREATE TABLE [categorias] (
        [id] varchar(50) PRIMARY KEY,
        [nombre] varchar(100) UNIQUE NOT NULL,
        [is_activo] int DEFAULT 1
    );
    PRINT 'Tabla [categorias] creada.';
END
GO

-- Tabla: productos
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='productos' AND xtype='U')
BEGIN
    CREATE TABLE [productos] (
        [id] varchar(50) PRIMARY KEY,
        [categoria_id] varchar(50),
        [codigo_digemid] varchar(100) UNIQUE,
        [nombre] varchar(255),
        [detalle] varchar(255),
        [unidad_medida] varchar(50),
        [stock_minimo] int DEFAULT 10,
        [precio_unitario] decimal(18,2) DEFAULT 0,
        [is_activo] int DEFAULT 1
    );
    PRINT 'Tabla [productos] creada.';
END
GO

-- Tabla: lotes
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='lotes' AND xtype='U')
BEGIN
    CREATE TABLE [lotes] (
        [id] varchar(50) PRIMARY KEY,
        [producto_id] varchar(50),
        [sede_id] varchar(50),
        [numero_lote] varchar(100),
        [fecha_vencimiento] date,
        [fecha_fabricacion] date,
        [cantidad] int
    );
    PRINT 'Tabla [lotes] creada.';
END
GO

-- Tabla: tipos_movimiento
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='tipos_movimiento' AND xtype='U')
BEGIN
    CREATE TABLE [tipos_movimiento] (
        [id] varchar(50) PRIMARY KEY,
        [nombre] varchar(50) NOT NULL,
        [is_activo] int DEFAULT 1
    );
    PRINT 'Tabla [tipos_movimiento] creada.';
END
GO

-- Tabla: motivos_movimiento
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='motivos_movimiento' AND xtype='U')
BEGIN
    CREATE TABLE [motivos_movimiento] (
        [id] varchar(50) PRIMARY KEY,
        [nombre] varchar(100) NOT NULL,
        [is_activo] int DEFAULT 1
    );
    PRINT 'Tabla [motivos_movimiento] creada.';
END
GO

-- Tabla: movimientos
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='movimientos' AND xtype='U')
BEGIN
    CREATE TABLE [movimientos] (
        [id] varchar(50) PRIMARY KEY,
        [tipo_id] varchar(50),
        [motivo_id] varchar(50),
        [sede_id] varchar(50),
        [usuario_id] varchar(50),
        [lote_id] varchar(50),
        [cantidad] int,
        [observacion] varchar(max),
        [fecha_registro] datetime DEFAULT GETDATE()
    );
    PRINT 'Tabla [movimientos] creada.';
END
GO

-- Tabla: atenciones
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='atenciones' AND xtype='U')
BEGIN
    CREATE TABLE [atenciones] (
        [id] varchar(50) PRIMARY KEY,
        [sede_id] varchar(50),
        [paciente_id] varchar(50),
        [usuario_id] varchar(50),
        [numero_receta] varchar(100),
        [medico] varchar(255),
        [fecha_atencion] datetime DEFAULT GETDATE()
    );
    PRINT 'Tabla [atenciones] creada.';
END
GO

-- Tabla: atencion_detalles
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='atencion_detalles' AND xtype='U')
BEGIN
    CREATE TABLE [atencion_detalles] (
        [id] varchar(50) PRIMARY KEY,
        [atencion_id] varchar(50),
        [lote_id] varchar(50),
        [cantidad_entregada] int
    );
    PRINT 'Tabla [atencion_detalles] creada.';
END
GO

-- =====================================================================
-- FASE 2: AGREGAR COLUMNAS FALTANTES (para bases de datos existentes)
-- =====================================================================

-- Columnas faltantes en [roles]
IF EXISTS (SELECT * FROM sysobjects WHERE name='roles' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('roles') AND name = 'descripcion')
    BEGIN
        ALTER TABLE [roles] ADD [descripcion] varchar(255);
        PRINT 'Columna [roles].[descripcion] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('roles') AND name = 'nivel')
    BEGIN
        ALTER TABLE [roles] ADD [nivel] int DEFAULT 99;
        PRINT 'Columna [roles].[nivel] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('roles') AND name = 'is_sistema')
    BEGIN
        ALTER TABLE [roles] ADD [is_sistema] int DEFAULT 0;
        PRINT 'Columna [roles].[is_sistema] agregada.';
    END
END
GO

-- Columnas faltantes en [sedes]
IF EXISTS (SELECT * FROM sysobjects WHERE name='sedes' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('sedes') AND name = 'telefono')
    BEGIN
        ALTER TABLE [sedes] ADD [telefono] varchar(50);
        PRINT 'Columna [sedes].[telefono] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('sedes') AND name = 'ubigeo')
    BEGIN
        ALTER TABLE [sedes] ADD [ubigeo] varchar(20);
        PRINT 'Columna [sedes].[ubigeo] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('sedes') AND name = 'tipo_sede')
    BEGIN
        ALTER TABLE [sedes] ADD [tipo_sede] varchar(50);
        PRINT 'Columna [sedes].[tipo_sede] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('sedes') AND name = 'capacidad_almacen')
    BEGIN
        ALTER TABLE [sedes] ADD [capacidad_almacen] int DEFAULT 0;
        PRINT 'Columna [sedes].[capacidad_almacen] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('sedes') AND name = 'administrador_id')
    BEGIN
        ALTER TABLE [sedes] ADD [administrador_id] varchar(50);
        PRINT 'Columna [sedes].[administrador_id] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('sedes') AND name = 'is_bloqueada')
    BEGIN
        ALTER TABLE [sedes] ADD [is_bloqueada] int DEFAULT 0;
        PRINT 'Columna [sedes].[is_bloqueada] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('sedes') AND name = 'motivo_bloqueo')
    BEGIN
        ALTER TABLE [sedes] ADD [motivo_bloqueo] varchar(500);
        PRINT 'Columna [sedes].[motivo_bloqueo] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('sedes') AND name = 'fecha_bloqueo')
    BEGIN
        ALTER TABLE [sedes] ADD [fecha_bloqueo] datetime NULL;
        PRINT 'Columna [sedes].[fecha_bloqueo] agregada.';
    END
END
GO

-- Columnas faltantes en [usuarios]
IF EXISTS (SELECT * FROM sysobjects WHERE name='usuarios' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('usuarios') AND name = 'telefono')
    BEGIN
        ALTER TABLE [usuarios] ADD [telefono] varchar(50);
        PRINT 'Columna [usuarios].[telefono] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('usuarios') AND name = 'ubigeo')
    BEGIN
        ALTER TABLE [usuarios] ADD [ubigeo] varchar(20);
        PRINT 'Columna [usuarios].[ubigeo] agregada.';
    END
END
GO

-- Columnas faltantes en [productos]
IF EXISTS (SELECT * FROM sysobjects WHERE name='productos' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('productos') AND name = 'stock_minimo')
    BEGIN
        ALTER TABLE [productos] ADD [stock_minimo] int DEFAULT 10;
        PRINT 'Columna [productos].[stock_minimo] agregada.';
    END
END
GO

-- Columnas faltantes en [atenciones]
IF EXISTS (SELECT * FROM sysobjects WHERE name='atenciones' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('atenciones') AND name = 'medico')
    BEGIN
        ALTER TABLE [atenciones] ADD [medico] varchar(255);
        PRINT 'Columna [atenciones].[medico] agregada.';
    END
END
GO

-- Columnas faltantes en [categorias]
IF EXISTS (SELECT * FROM sysobjects WHERE name='categorias' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('categorias') AND name = 'is_activo')
    BEGIN
        ALTER TABLE [categorias] ADD [is_activo] int DEFAULT 1;
        PRINT 'Columna [categorias].[is_activo] agregada.';
    END
END
GO

-- Columnas faltantes en [tipos_movimiento]
IF EXISTS (SELECT * FROM sysobjects WHERE name='tipos_movimiento' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('tipos_movimiento') AND name = 'is_activo')
    BEGIN
        ALTER TABLE [tipos_movimiento] ADD [is_activo] int DEFAULT 1;
        PRINT 'Columna [tipos_movimiento].[is_activo] agregada.';
    END
END
GO

-- Columnas faltantes en [motivos_movimiento]
IF EXISTS (SELECT * FROM sysobjects WHERE name='motivos_movimiento' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('motivos_movimiento') AND name = 'is_activo')
    BEGIN
        ALTER TABLE [motivos_movimiento] ADD [is_activo] int DEFAULT 1;
        PRINT 'Columna [motivos_movimiento].[is_activo] agregada.';
    END
END
GO

-- =====================================================================
-- FASE 3: AGREGAR FOREIGN KEYS FALTANTES
-- =====================================================================

-- FKs para [sedes]
IF EXISTS (SELECT * FROM sysobjects WHERE name='sedes' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_sedes_administrador')
    BEGIN
        ALTER TABLE [sedes] ADD CONSTRAINT [FK_sedes_administrador] 
            FOREIGN KEY ([administrador_id]) REFERENCES [usuarios] ([id]);
        PRINT 'FK [FK_sedes_administrador] agregada.';
    END
END
GO

-- FKs para [usuarios]
IF EXISTS (SELECT * FROM sysobjects WHERE name='usuarios' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_usuarios_sedes')
    BEGIN
        ALTER TABLE [usuarios] ADD CONSTRAINT [FK_usuarios_sedes] 
            FOREIGN KEY ([sede_id]) REFERENCES [sedes] ([id]);
        PRINT 'FK [FK_usuarios_sedes] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_usuarios_roles')
    BEGIN
        ALTER TABLE [usuarios] ADD CONSTRAINT [FK_usuarios_roles] 
            FOREIGN KEY ([rol_id]) REFERENCES [roles] ([id]);
        PRINT 'FK [FK_usuarios_roles] agregada.';
    END
END
GO

-- FKs para [productos]
IF EXISTS (SELECT * FROM sysobjects WHERE name='productos' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_productos_categorias')
    BEGIN
        ALTER TABLE [productos] ADD CONSTRAINT [FK_productos_categorias] 
            FOREIGN KEY ([categoria_id]) REFERENCES [categorias] ([id]);
        PRINT 'FK [FK_productos_categorias] agregada.';
    END
END
GO

-- FKs para [lotes]
IF EXISTS (SELECT * FROM sysobjects WHERE name='lotes' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_lotes_productos')
    BEGIN
        ALTER TABLE [lotes] ADD CONSTRAINT [FK_lotes_productos] 
            FOREIGN KEY ([producto_id]) REFERENCES [productos] ([id]);
        PRINT 'FK [FK_lotes_productos] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_lotes_sedes')
    BEGIN
        ALTER TABLE [lotes] ADD CONSTRAINT [FK_lotes_sedes] 
            FOREIGN KEY ([sede_id]) REFERENCES [sedes] ([id]);
        PRINT 'FK [FK_lotes_sedes] agregada.';
    END
END
GO

-- FKs para [movimientos]
IF EXISTS (SELECT * FROM sysobjects WHERE name='movimientos' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_movimientos_sedes')
    BEGIN
        ALTER TABLE [movimientos] ADD CONSTRAINT [FK_movimientos_sedes] 
            FOREIGN KEY ([sede_id]) REFERENCES [sedes] ([id]);
        PRINT 'FK [FK_movimientos_sedes] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_movimientos_usuarios')
    BEGIN
        ALTER TABLE [movimientos] ADD CONSTRAINT [FK_movimientos_usuarios] 
            FOREIGN KEY ([usuario_id]) REFERENCES [usuarios] ([id]);
        PRINT 'FK [FK_movimientos_usuarios] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_movimientos_lotes')
    BEGIN
        ALTER TABLE [movimientos] ADD CONSTRAINT [FK_movimientos_lotes] 
            FOREIGN KEY ([lote_id]) REFERENCES [lotes] ([id]);
        PRINT 'FK [FK_movimientos_lotes] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_mov_tipo')
    BEGIN
        ALTER TABLE [movimientos] ADD CONSTRAINT [FK_mov_tipo] 
            FOREIGN KEY ([tipo_id]) REFERENCES [tipos_movimiento] ([id]);
        PRINT 'FK [FK_mov_tipo] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_mov_motivo')
    BEGIN
        ALTER TABLE [movimientos] ADD CONSTRAINT [FK_mov_motivo] 
            FOREIGN KEY ([motivo_id]) REFERENCES [motivos_movimiento] ([id]);
        PRINT 'FK [FK_mov_motivo] agregada.';
    END
END
GO

-- FKs para [atenciones]
IF EXISTS (SELECT * FROM sysobjects WHERE name='atenciones' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_atenciones_sedes')
    BEGIN
        ALTER TABLE [atenciones] ADD CONSTRAINT [FK_atenciones_sedes] 
            FOREIGN KEY ([sede_id]) REFERENCES [sedes] ([id]);
        PRINT 'FK [FK_atenciones_sedes] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_atenciones_pacientes')
    BEGIN
        ALTER TABLE [atenciones] ADD CONSTRAINT [FK_atenciones_pacientes] 
            FOREIGN KEY ([paciente_id]) REFERENCES [pacientes] ([id]);
        PRINT 'FK [FK_atenciones_pacientes] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_atenciones_usuarios')
    BEGIN
        ALTER TABLE [atenciones] ADD CONSTRAINT [FK_atenciones_usuarios] 
            FOREIGN KEY ([usuario_id]) REFERENCES [usuarios] ([id]);
        PRINT 'FK [FK_atenciones_usuarios] agregada.';
    END
END
GO

-- FKs para [atencion_detalles]
IF EXISTS (SELECT * FROM sysobjects WHERE name='atencion_detalles' AND xtype='U')
BEGIN
    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_atencion_detalles_atenciones')
    BEGIN
        ALTER TABLE [atencion_detalles] ADD CONSTRAINT [FK_atencion_detalles_atenciones] 
            FOREIGN KEY ([atencion_id]) REFERENCES [atenciones] ([id]);
        PRINT 'FK [FK_atencion_detalles_atenciones] agregada.';
    END

    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_atencion_detalles_lotes')
    BEGIN
        ALTER TABLE [atencion_detalles] ADD CONSTRAINT [FK_atencion_detalles_lotes] 
            FOREIGN KEY ([lote_id]) REFERENCES [lotes] ([id]);
        PRINT 'FK [FK_atencion_detalles_lotes] agregada.';
    END
END
GO

-- =====================================================================
-- FASE 4: DATOS INICIALES (solo si no existen)
-- =====================================================================

-- Roles base del sistema
IF NOT EXISTS (SELECT 1 FROM [roles] WHERE [id] = 'ROL-001')
BEGIN
    INSERT INTO [roles] ([id], [nombre], [descripcion], [nivel], [is_sistema]) 
    VALUES ('ROL-001', 'Administrador', 'Acceso total al sistema', 1, 1);
END

IF NOT EXISTS (SELECT 1 FROM [roles] WHERE [id] = 'ROL-002')
BEGIN
    INSERT INTO [roles] ([id], [nombre], [descripcion], [nivel], [is_sistema]) 
    VALUES ('ROL-002', 'Jefe de Sede', 'Supervisión de inventario y gestión de su posta', 3, 1);
END

IF NOT EXISTS (SELECT 1 FROM [roles] WHERE [id] = 'ROL-003')
BEGIN
    INSERT INTO [roles] ([id], [nombre], [descripcion], [nivel], [is_sistema]) 
    VALUES ('ROL-003', 'Técnico de Farmacia', 'Atención al paciente, dispensación y salidas de inventario', 4, 1);
END

-- Corregir datos de roles existentes (por si se insertaron con valores incorrectos)
UPDATE [roles] SET [nivel] = 1, [is_sistema] = 1, [descripcion] = 'Acceso total al sistema sin restricción de sede' WHERE [id] = 'ROL-001' AND ([nivel] IS NULL OR [nivel] != 1);
UPDATE [roles] SET [nivel] = 3, [is_sistema] = 1, [descripcion] = 'Supervisión de inventario y gestión de su posta' WHERE [id] = 'ROL-002' AND ([nivel] IS NULL OR [nivel] != 3);
UPDATE [roles] SET [nivel] = 4, [is_sistema] = 1, [descripcion] = 'Atención al paciente, dispensación y salidas de inventario' WHERE [id] = 'ROL-003' AND ([nivel] IS NULL OR [nivel] != 4);

PRINT 'Roles base verificados.';
GO

-- Permisos base del sistema
IF NOT EXISTS (SELECT 1 FROM [permisos] WHERE [id] = 'PERM-001')
BEGIN
    INSERT INTO [permisos] ([id], [codigo], [nombre], [descripcion], [modulo], [orden]) VALUES
    ('PERM-001', 'M1_LOGIN', 'Inicio de Sesión', 'Acceso básico al sistema', 'OPERACIONES', 1),
    ('PERM-002', 'M4_LOTES', 'Gestión de Lotes', 'Ver y gestionar inventario', 'OPERACIONES', 2),
    ('PERM-003', 'M5_ENTRADAS', 'Entradas de Inventario', 'Registrar movimientos de entrada', 'OPERACIONES', 3),
    ('PERM-004', 'M6_SALIDAS', 'Salidas y Ajustes', 'Registrar movimientos de salida', 'OPERACIONES', 4),
    ('PERM-005', 'M8_ATENCIONES', 'Registro de Atenciones', 'Gestionar atenciones de pacientes', 'OPERACIONES', 5),
    ('PERM-006', 'M9_DISPENSACION', 'Dispensación', 'Dispensar medicamentos', 'OPERACIONES', 6),
    ('PERM-007', 'M2_SEDES', 'Gestión de Sedes', 'Crear, editar, eliminar sedes', 'MANTENIMIENTO', 10),
    ('PERM-008', 'M3_PRODUCTOS', 'Catálogo de Productos', 'Gestionar productos', 'MANTENIMIENTO', 11),
    ('PERM-009', 'M7_PACIENTES', 'Gestión de Pacientes', 'Registrar y editar pacientes', 'MANTENIMIENTO', 12),
    ('PERM-010', 'USUARIOS', 'Gestión de Usuarios', 'Administrar usuarios del sistema', 'MANTENIMIENTO', 13),
    ('PERM-011', 'CATEGORIAS', 'Catálogo de Categorías', 'Gestionar categorías de productos', 'MANTENIMIENTO', 14),
    ('PERM-012', 'MOV_CATALOGOS', 'Catálogos de Movimiento', 'Gestionar tipos y motivos', 'MANTENIMIENTO', 15),
    ('PERM-013', 'M10_REPORTES', 'Reportes y Dashboard', 'Ver reportes y dashboard', 'SISTEMA', 20),
    ('PERM-014', 'ROLES', 'Gestión de Roles', 'Administrar roles y permisos', 'SISTEMA', 21);
    PRINT 'Permisos base insertados.';
END
GO

-- Asignar permisos al Administrador Global: acceso total sin restricción de sede
IF NOT EXISTS (SELECT 1 FROM [rol_permisos] WHERE [rol_id] = 'ROL-001')
BEGIN
    INSERT INTO [rol_permisos] ([rol_id], [permiso_id]) VALUES
    ('ROL-001', 'PERM-001'),  -- M1_LOGIN
    ('ROL-001', 'PERM-007'),  -- M2_SEDES (crear/editar sedes)
    ('ROL-001', 'PERM-008'),  -- M3_PRODUCTOS (catálogo de productos)
    ('ROL-001', 'PERM-002'),  -- M4_LOTES
    ('ROL-001', 'PERM-003'),  -- M5_ENTRADAS
    ('ROL-001', 'PERM-004'),  -- M6_SALIDAS
    ('ROL-001', 'PERM-005'),  -- M8_ATENCIONES
    ('ROL-001', 'PERM-006'),  -- M9_DISPENSACION
    ('ROL-001', 'PERM-009'),  -- M7_PACIENTES
    ('ROL-001', 'PERM-010'),  -- USUARIOS
    ('ROL-001', 'PERM-011'),  -- CATEGORIAS
    ('ROL-001', 'PERM-012'),  -- MOV_CATALOGOS
    ('ROL-001', 'PERM-013'),  -- M10_REPORTES (reportes consolidados)
    ('ROL-001', 'PERM-014');  -- ROLES
    PRINT 'Permisos asignados al Administrador Global.';
END
GO

-- Asignar permisos al Jefe de Sede: supervisa inventario, registra entradas, gestiona usuarios, reportes de sede
IF NOT EXISTS (SELECT 1 FROM [rol_permisos] WHERE [rol_id] = 'ROL-002')
BEGIN
    INSERT INTO [rol_permisos] ([rol_id], [permiso_id]) VALUES
    ('ROL-002', 'PERM-001'),  -- M1_LOGIN
    ('ROL-002', 'PERM-008'),  -- M3_PRODUCTOS (supervisa catálogo de productos)
    ('ROL-002', 'PERM-002'),  -- M4_LOTES (supervisa inventario)
    ('ROL-002', 'PERM-003'),  -- M5_ENTRADAS (entradas de proveedor)
    ('ROL-002', 'PERM-009'),  -- M7_PACIENTES (supervisa pacientes de su sede)
    ('ROL-002', 'PERM-011'),  -- CATEGORIAS (gestiona categorías de su sede)
    ('ROL-002', 'PERM-010'),  -- USUARIOS (gestiona usuarios de su sede)
    ('ROL-002', 'PERM-013');  -- M10_REPORTES (reportes de su sede)
    PRINT 'Permisos asignados al Jefe de Sede.';
END
GO

-- Asignar permisos al Técnico de Farmacia: atención al paciente, dispensación y salidas
IF NOT EXISTS (SELECT 1 FROM [rol_permisos] WHERE [rol_id] = 'ROL-003')
BEGIN
    INSERT INTO [rol_permisos] ([rol_id], [permiso_id]) VALUES
    ('ROL-003', 'PERM-001'),  -- M1_LOGIN
    ('ROL-003', 'PERM-002'),  -- M4_LOTES (ver inventario para dispensar)
    ('ROL-003', 'PERM-004'),  -- M6_SALIDAS (salidas y ajustes)
    ('ROL-003', 'PERM-009'),  -- M7_PACIENTES (atención al paciente)
    ('ROL-003', 'PERM-005'),  -- M8_ATENCIONES (registro de atenciones)
    ('ROL-003', 'PERM-006'),  -- M9_DISPENSACION (dispensar medicamentos)
    ('ROL-003', 'PERM-013');  -- M10_REPORTES (reportes de su sede)
    PRINT 'Permisos asignados al Técnico de Farmacia.';
END
GO

-- Catálogos de movimiento
IF NOT EXISTS (SELECT 1 FROM [tipos_movimiento] WHERE [id] = 'MOV-T-01')
BEGIN
    INSERT INTO [tipos_movimiento] ([id], [nombre]) VALUES
    ('MOV-T-01', 'entrada'),
    ('MOV-T-02', 'salida');
END

IF NOT EXISTS (SELECT 1 FROM [motivos_movimiento] WHERE [id] = 'MOV-M-01')
BEGIN
    INSERT INTO [motivos_movimiento] ([id], [nombre]) VALUES
    ('MOV-M-01', 'compra'),
    ('MOV-M-02', 'transferencia'),
    ('MOV-M-03', 'atencion'),
    ('MOV-M-04', 'merma');
END
PRINT 'Catálogos de movimiento verificados.';
GO

-- =====================================================================
-- FASE 5: DATOS DE PRUEBA (solo si la base está vacía)
-- =====================================================================

-- Solo insertar datos de prueba si no hay usuarios (base vacía)
IF NOT EXISTS (SELECT 1 FROM [usuarios])
BEGIN
    PRINT 'Insertando datos de prueba...';

    -- Categorías
    INSERT INTO [categorias] ([id], [nombre]) VALUES
    ('CAT-01', 'Analgésicos'),
    ('CAT-02', 'Antibióticos'),
    ('CAT-03', 'Antiinflamatorios'),
    ('CAT-04', 'Suministros Médicos');

    -- Productos
    INSERT INTO [productos] ([id], [categoria_id], [codigo_digemid], [nombre], [detalle], [unidad_medida], [stock_minimo], [precio_unitario]) VALUES
    ('PRD-01', 'CAT-01', 'DIG-001', 'Paracetamol 500mg', 'Tabletas para el dolor', 'Caja x 100', 10, 3.50),
    ('PRD-02', 'CAT-02', 'DIG-002', 'Amoxicilina 500mg', 'Cápsulas antibióticas', 'Frasco', 15, 12.00),
    ('PRD-03', 'CAT-03', 'DIG-003', 'Ibuprofeno 400mg', 'Antiinflamatorio potente', 'Caja x 50', 10, 5.00),
    ('PRD-04', 'CAT-04', 'DIG-004', 'Alcohol en Gel 70%', 'Desinfectante de manos', 'Botella 500ml', 20, 8.00),
    ('PRD-05', 'CAT-01', 'DIG-005', 'Aspirina 100mg', 'Protector cardiovascular', 'Caja x 30', 10, 2.75),
    ('PRD-06', 'CAT-02', 'DIG-006', 'Azitromicina 500mg', 'Tratamiento respiratorio', 'Caja x 3', 5, 15.00);

    -- Sedes
    INSERT INTO [sedes] ([id], [nombre], [direccion], [telefono], [ubigeo], [tipo_sede], [capacidad_almacen]) VALUES
    ('SED-001', 'Sede Central Lima', 'Av. Principal 123', '012345678', '150101', 'Posta Médica', 1000),
    ('SED-002', 'Posta San Juan', 'Av. Peru 456', '012345679', '150102', 'Centro de Salud', 500);

    -- Usuarios (contraseña: admin123)
    INSERT INTO [usuarios] ([id], [sede_id], [rol_id], [tipo_documento], [numero_documento], [nombres], [apellidos], [telefono], [ubigeo], [password]) VALUES
    ('USR-001', 'SED-001', 'ROL-001', 'DNI', '12345678', 'Admin', 'Sistema', '988111222', '150101', 'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU='),
    ('USR-002', 'SED-001', 'ROL-002', 'DNI', '22222222', 'Jefe', 'Farmacia', '988222333', '150101', 'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU='),
    ('USR-003', 'SED-001', 'ROL-003', 'DNI', '33333333', 'Tecnico', 'Operativo', '988333444', '150101', 'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU=');
    -- Pacientes
    INSERT INTO [pacientes] ([id], [tipo_documento], [numero_documento], [nombres], [apellidos], [telefono]) VALUES
    ('PAC-01', 'DNI', '44556677', 'Juan', 'Perez Garcia', '988777666'),
    ('PAC-02', 'CE', '22334455', 'Maria', 'Lopez Sosa', '911222333'),
    ('PAC-03', 'DNI', '55443322', 'Carlos', 'Mendez Ruiz', '987654321');

    -- Lotes
    INSERT INTO [lotes] ([id], [producto_id], [sede_id], [numero_lote], [fecha_fabricacion], [fecha_vencimiento], [cantidad]) VALUES
    ('LT-01', 'PRD-01', 'SED-001', 'L2024-001', '2024-01-01', '2027-06-01', 150),
    ('LT-02', 'PRD-04', 'SED-001', 'L2024-002', '2024-02-15', '2027-08-20', 80),
    ('LT-03', 'PRD-02', 'SED-001', 'L2024-CRIT', '2024-01-10', '2027-05-15', 3),
    ('LT-04', 'PRD-06', 'SED-001', 'L2024-LOW', '2024-03-01', '2027-04-10', 5),
    ('LT-05', 'PRD-03', 'SED-001', 'L-VENC-1', '2024-05-01', '2026-07-15', 45),
    ('LT-06', 'PRD-05', 'SED-001', 'L-VENC-2', '2024-06-01', '2026-06-25', 20);

    -- Atenciones
    INSERT INTO [atenciones] ([id], [sede_id], [paciente_id], [usuario_id], [numero_receta], [medico], [fecha_atencion]) VALUES
    ('ATN-001', 'SED-001', 'PAC-01', 'USR-002', 'REC-001', 'Dr. Garcia', '2026-06-15 10:30:00'),
    ('ATN-002', 'SED-001', 'PAC-02', 'USR-003', 'REC-002', 'Dra. Lopez', '2026-06-16 14:00:00');

    -- Detalles de atención
    INSERT INTO [atencion_detalles] ([id], [atencion_id], [lote_id], [cantidad_entregada]) VALUES
    ('ATN-D-001', 'ATN-001', 'LT-01', 2),
    ('ATN-D-002', 'ATN-001', 'LT-02', 1),
    ('ATN-D-003', 'ATN-002', 'LT-03', 3);

    PRINT 'Datos de prueba insertados correctamente.';
END
ELSE
BEGIN
    PRINT 'Base de datos ya contiene datos. Saltando datos de prueba.';
END
GO

PRINT '=====================================================================';
PRINT 'MediTrack - Inicialización/Actualización completada exitosamente.';
PRINT '=====================================================================';
GO
