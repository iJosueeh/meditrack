-- ========================
-- TABLAS BASE
-- ========================

CREATE TABLE [roles] (
[id] varchar(50) PRIMARY KEY,
[nombre] varchar(100) UNIQUE NOT NULL
);

CREATE TABLE [sedes] (
[id] varchar(50) PRIMARY KEY,
[nombre] varchar(255) NOT NULL,
[direccion] varchar(500),
[is_activa] int DEFAULT 1
);

CREATE TABLE [usuarios] (
[id] varchar(50) PRIMARY KEY,
[sede_id] varchar(50),
[rol_id] varchar(50),
[tipo_documento] varchar(50), 
[numero_documento] varchar(50) UNIQUE,
[nombres] varchar(255),
[apellidos] varchar(255),
[password] varchar(max),
[is_activo] int DEFAULT 1
);

CREATE TABLE [pacientes] (
[id] varchar(50) PRIMARY KEY,
[tipo_documento] varchar(50),
[numero_documento] varchar(50) UNIQUE,
[nombres] varchar(255),
[apellidos] varchar(255),
[telefono] varchar(50),
[is_activo] int DEFAULT 1
);

CREATE TABLE [categorias] (
[id] varchar(50) PRIMARY KEY,
[nombre] varchar(100) UNIQUE NOT NULL
);

CREATE TABLE [productos] (
[id] varchar(50) PRIMARY KEY,
[categoria_id] varchar(50),
[codigo_digemid] varchar(100) UNIQUE,
[nombre] varchar(255),
[detalle] varchar(255),
[unidad_medida] varchar(50),
[is_activo] int DEFAULT 1
);

-- ========================
-- LOTES
-- ========================

CREATE TABLE [lotes] (
[id] varchar(50) PRIMARY KEY,
[producto_id] varchar(50),
[sede_id] varchar(50),
[numero_lote] varchar(100),
[fecha_vencimiento] date,
[fecha_fabricacion] date,
[cantidad] int
);

-- ========================
-- MOVIMIENTOS (CATÁLOGOS)
-- ========================

CREATE TABLE [tipos_movimiento] (
[id] varchar(50) PRIMARY KEY,
[nombre] varchar(50) NOT NULL
);

CREATE TABLE [motivos_movimiento] (
[id] varchar(50) PRIMARY KEY,
[nombre] varchar(100) NOT NULL
);

-- ========================
-- MOVIMIENTOS
-- ========================

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

-- ========================
-- ATENCIONES
-- ========================

CREATE TABLE [atenciones] (
[id] varchar(50) PRIMARY KEY,
[sede_id] varchar(50),
[paciente_id] varchar(50),
[usuario_id] varchar(50),
[numero_receta] varchar(100),
[fecha_atencion] datetime DEFAULT GETDATE()
);

CREATE TABLE [atencion_detalles] (
[id] varchar(50) PRIMARY KEY,
[atencion_id] varchar(50),
[lote_id] varchar(50),
[cantidad_entregada] int
);

-- ========================
-- FOREIGN KEYS
-- ========================

ALTER TABLE [usuarios] ADD CONSTRAINT [FK_usuarios_sedes] FOREIGN KEY ([sede_id]) REFERENCES [sedes] ([id]);
ALTER TABLE [usuarios] ADD CONSTRAINT [FK_usuarios_roles] FOREIGN KEY ([rol_id]) REFERENCES [roles] ([id]);
ALTER TABLE [productos] ADD CONSTRAINT [FK_productos_categorias] FOREIGN KEY ([categoria_id]) REFERENCES [categorias] ([id]);
ALTER TABLE [lotes] ADD CONSTRAINT [FK_lotes_productos] FOREIGN KEY ([producto_id]) REFERENCES [productos] ([id]);
ALTER TABLE [lotes] ADD CONSTRAINT [FK_lotes_sedes] FOREIGN KEY ([sede_id]) REFERENCES [sedes] ([id]);
ALTER TABLE [movimientos] ADD CONSTRAINT [FK_movimientos_sedes] FOREIGN KEY ([sede_id]) REFERENCES [sedes] ([id]);
ALTER TABLE [movimientos] ADD CONSTRAINT [FK_movimientos_usuarios] FOREIGN KEY ([usuario_id]) REFERENCES [usuarios] ([id]);
ALTER TABLE [movimientos] ADD CONSTRAINT [FK_movimientos_lotes] FOREIGN KEY ([lote_id]) REFERENCES [lotes] ([id]);
ALTER TABLE [movimientos] ADD CONSTRAINT [FK_mov_tipo] FOREIGN KEY ([tipo_id]) REFERENCES [tipos_movimiento] ([id]);
ALTER TABLE [movimientos] ADD CONSTRAINT [FK_mov_motivo] FOREIGN KEY ([motivo_id]) REFERENCES [motivos_movimiento] ([id]);
ALTER TABLE [atenciones] ADD CONSTRAINT [FK_atenciones_sedes] FOREIGN KEY ([sede_id]) REFERENCES [sedes] ([id]);
ALTER TABLE [atenciones] ADD CONSTRAINT [FK_atenciones_pacientes] FOREIGN KEY ([paciente_id]) REFERENCES [pacientes] ([id]);
ALTER TABLE [atenciones] ADD CONSTRAINT [FK_atenciones_usuarios] FOREIGN KEY ([usuario_id]) REFERENCES [usuarios] ([id]);
ALTER TABLE [atencion_detalles] ADD CONSTRAINT [FK_atencion_detalles_atenciones] FOREIGN KEY ([atencion_id]) REFERENCES [atenciones] ([id]);
ALTER TABLE [atencion_detalles] ADD CONSTRAINT [FK_atencion_detalles_lotes] FOREIGN KEY ([lote_id]) REFERENCES [lotes] ([id]);

-- ========================
-- DATA INICIAL (SEED)
-- ========================

INSERT INTO [roles] ([id], [nombre]) VALUES ('ROL-001', 'Administrador');
INSERT INTO [roles] ([id], [nombre]) VALUES ('ROL-002', 'Farmacéutico');

INSERT INTO [sedes] ([id], [nombre], [direccion]) VALUES ('SED-001', 'Sede Central Lima', 'Av. Principal 123');

-- El password es 'admin123' (hash BCrypt)
INSERT INTO [usuarios] ([id], [sede_id], [rol_id], [tipo_documento], [numero_documento], [nombres], [apellidos], [password], [is_activo])
VALUES ('USR-001', 'SED-001', 'ROL-001', 'DNI', '12345678', 'Admin', 'Sistema', 'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU=', 1);

INSERT INTO [tipos_movimiento] ([id], [nombre]) VALUES ('MOV-T-01', 'entrada'), ('MOV-T-02', 'salida');
INSERT INTO [motivos_movimiento] ([id], [nombre]) VALUES ('MOV-M-01', 'compra'), ('MOV-M-02', 'transferencia'), ('MOV-M-03', 'atencion'), ('MOV-M-04', 'merma');
