-- =============================================
-- MEDITRACK - SCRIPT UNIFICADO DE BASE DE DATOS
-- Esquema + Datos iniciales + Datos de prueba
-- =============================================

USE [meditrack_db];
GO

-- =============================================
-- LIMPIEZA (orden inverso de dependencias)
-- =============================================

DROP TABLE IF EXISTS [dbo].[roles_permisos];
DROP TABLE IF EXISTS [dbo].[permisos];
DROP TABLE IF EXISTS [dbo].[atencion_detalles];
DROP TABLE IF EXISTS [dbo].[movimientos];
DROP TABLE IF EXISTS [dbo].[atenciones];
DROP TABLE IF EXISTS [dbo].[lotes];
DROP TABLE IF EXISTS [dbo].[usuarios];
DROP TABLE IF EXISTS [dbo].[productos];
DROP TABLE IF EXISTS [dbo].[sedes];
DROP TABLE IF EXISTS [dbo].[pacientes];
DROP TABLE IF EXISTS [dbo].[motivos_movimiento];
DROP TABLE IF EXISTS [dbo].[tipos_movimiento];
DROP TABLE IF EXISTS [dbo].[categorias];
DROP TABLE IF EXISTS [dbo].[roles];
GO

-- =============================================
-- TABLAS BASE
-- =============================================

CREATE TABLE [dbo].[roles] (
    [id]        varchar(50)  NOT NULL,
    [nombre]    varchar(100) NOT NULL,
    [is_activo] int          NOT NULL DEFAULT 1,
    CONSTRAINT [PK_roles]        PRIMARY KEY ([id]),
    CONSTRAINT [UQ_roles_nombre] UNIQUE ([nombre])
);

CREATE TABLE [dbo].[categorias] (
    [id]        varchar(50)  NOT NULL,
    [nombre]    varchar(100) NOT NULL,
    [is_activo] int          NOT NULL DEFAULT 1,
    CONSTRAINT [PK_categorias]        PRIMARY KEY ([id]),
    CONSTRAINT [UQ_categorias_nombre] UNIQUE ([nombre])
);

CREATE TABLE [dbo].[tipos_movimiento] (
    [id]        varchar(50)  NOT NULL,
    [nombre]    varchar(50)  NOT NULL,
    [is_activo] int          NOT NULL DEFAULT 1,
    CONSTRAINT [PK_tipos_movimiento]        PRIMARY KEY ([id]),
    CONSTRAINT [UQ_tipos_movimiento_nombre] UNIQUE ([nombre])
);

CREATE TABLE [dbo].[motivos_movimiento] (
    [id]        varchar(50)  NOT NULL,
    [nombre]    varchar(100) NOT NULL,
    [is_activo] int          NOT NULL DEFAULT 1,
    CONSTRAINT [PK_motivos_movimiento]        PRIMARY KEY ([id]),
    CONSTRAINT [UQ_motivos_movimiento_nombre] UNIQUE ([nombre])
);

-- =============================================
-- SEDES
-- =============================================

CREATE TABLE [dbo].[sedes] (
    [id]                  varchar(50)  NOT NULL,
    [nombre]              varchar(255) NOT NULL,
    [direccion]           varchar(500) NULL,
    [telefono]            varchar(50)  NULL,
    [ubigeo]              varchar(20)  NULL,
    [tipo_sede]           varchar(50)  NULL,
    [capacidad_almacen]   int          NOT NULL DEFAULT 0,
    [administrador_id]    varchar(50)  NULL,
    [is_activa]           int          NOT NULL DEFAULT 1,
    CONSTRAINT [PK_sedes] PRIMARY KEY ([id])
);

-- =============================================
-- USUARIOS
-- =============================================

CREATE TABLE [dbo].[usuarios] (
    [id]               varchar(50)  NOT NULL,
    [sede_id]          varchar(50)  NULL,
    [rol_id]           varchar(50)  NULL,
    [tipo_documento]   varchar(50)  NOT NULL,
    [numero_documento] varchar(50)  NOT NULL,
    [telefono]         varchar(50)  NOT NULL,
    [ubigeo]           varchar(50)  NOT NULL,
    [nombres]          varchar(255) NOT NULL,
    [apellidos]        varchar(255) NOT NULL,
    [password]         varchar(max) NOT NULL,
    [is_activo]        int          NOT NULL DEFAULT 1,
    CONSTRAINT [PK_usuarios]               PRIMARY KEY ([id]),
    CONSTRAINT [UQ_usuarios_numero_doc]    UNIQUE ([numero_documento])
);

-- =============================================
-- PACIENTES
-- =============================================

CREATE TABLE [dbo].[pacientes] (
    [id]               varchar(50)  NOT NULL,
    [tipo_documento]   varchar(50)  NOT NULL,
    [numero_documento] varchar(50)  NOT NULL,
    [nombres]          varchar(255) NOT NULL,
    [apellidos]        varchar(255) NOT NULL,
    [telefono]         varchar(50)  NULL,
    [is_activo]        int          NOT NULL DEFAULT 1,
    CONSTRAINT [PK_pacientes]               PRIMARY KEY ([id]),
    CONSTRAINT [UQ_pacientes_numero_doc]    UNIQUE ([numero_documento])
);

-- =============================================
-- PRODUCTOS
-- =============================================

CREATE TABLE [dbo].[productos] (
    [id]               varchar(50)    NOT NULL,
    [categoria_id]     varchar(50)    NOT NULL,
    [codigo_digemid]   varchar(100)   NOT NULL,
    [nombre]           varchar(255)   NOT NULL,
    [detalle]          varchar(255)   NULL,
    [unidad_medida]    varchar(50)    NOT NULL,
    [stock_minimo]     int            NOT NULL DEFAULT 10,
    [precio_unitario]  decimal(18,2)  NOT NULL DEFAULT 0,
    [is_activo]        int            NOT NULL DEFAULT 1,
    CONSTRAINT [PK_productos]              PRIMARY KEY ([id]),
    CONSTRAINT [UQ_productos_codigo_dig]   UNIQUE ([codigo_digemid])
);

-- =============================================
-- LOTES
-- =============================================

CREATE TABLE [dbo].[lotes] (
    [id]                  varchar(50)  NOT NULL,
    [producto_id]         varchar(50)  NOT NULL,
    [sede_id]             varchar(50)  NOT NULL,
    [numero_lote]         varchar(100) NOT NULL,
    [fecha_vencimiento]   date         NOT NULL,
    [fecha_fabricacion]   date         NOT NULL,
    [cantidad]            int          NOT NULL,
    CONSTRAINT [PK_lotes] PRIMARY KEY ([id])
);

-- =============================================
-- MOVIMIENTOS
-- =============================================

CREATE TABLE [dbo].[movimientos] (
    [id]              varchar(50)    NOT NULL,
    [tipo_id]         varchar(50)    NOT NULL,
    [motivo_id]       varchar(50)    NOT NULL,
    [sede_id]         varchar(50)    NOT NULL,
    [usuario_id]      varchar(50)    NOT NULL,
    [lote_id]         varchar(50)    NOT NULL,
    [cantidad]        int            NOT NULL,
    [observacion]     varchar(max)   NULL,
    [fecha_registro]  datetime       NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_movimientos] PRIMARY KEY ([id])
);

-- =============================================
-- ATENCIONES
-- =============================================

CREATE TABLE [dbo].[atenciones] (
    [id]              varchar(50)    NOT NULL,
    [sede_id]         varchar(50)    NOT NULL,
    [paciente_id]     varchar(50)    NOT NULL,
    [usuario_id]      varchar(50)    NOT NULL,
    [numero_receta]   varchar(100)   NOT NULL,
    [medico]          varchar(255)   NULL,
    [fecha_atencion]  datetime       NOT NULL DEFAULT GETDATE(),
    CONSTRAINT [PK_atenciones] PRIMARY KEY ([id])
);

CREATE TABLE [dbo].[atencion_detalles] (
    [id]                 varchar(50) NOT NULL,
    [atencion_id]        varchar(50) NOT NULL,
    [lote_id]            varchar(50) NOT NULL,
    [cantidad_entregada] int         NOT NULL,
    CONSTRAINT [PK_atencion_detalles] PRIMARY KEY ([id])
);

-- =============================================
-- FOREIGN KEYS
-- =============================================

-- sedes -> usuarios (administrador)
ALTER TABLE [dbo].[sedes]
    ADD CONSTRAINT [FK_sedes_administrador]
    FOREIGN KEY ([administrador_id]) REFERENCES [dbo].[usuarios] ([id]);

-- usuarios -> sedes, roles
ALTER TABLE [dbo].[usuarios]
    ADD CONSTRAINT [FK_usuarios_sede]
    FOREIGN KEY ([sede_id]) REFERENCES [dbo].[sedes] ([id]),
    ADD CONSTRAINT [FK_usuarios_rol]
    FOREIGN KEY ([rol_id]) REFERENCES [dbo].[roles] ([id]);

-- productos -> categorias
ALTER TABLE [dbo].[productos]
    ADD CONSTRAINT [FK_productos_categoria]
    FOREIGN KEY ([categoria_id]) REFERENCES [dbo].[categorias] ([id]);

-- lotes -> productos, sedes
ALTER TABLE [dbo].[lotes]
    ADD CONSTRAINT [FK_lotes_producto]
    FOREIGN KEY ([producto_id]) REFERENCES [dbo].[productos] ([id]),
    ADD CONSTRAINT [FK_lotes_sede]
    FOREIGN KEY ([sede_id]) REFERENCES [dbo].[sedes] ([id]);

-- movimientos -> tipos, motivos, sedes, usuarios, lotes
ALTER TABLE [dbo].[movimientos]
    ADD CONSTRAINT [FK_movimientos_tipo]
    FOREIGN KEY ([tipo_id]) REFERENCES [dbo].[tipos_movimiento] ([id]),
    ADD CONSTRAINT [FK_movimientos_motivo]
    FOREIGN KEY ([motivo_id]) REFERENCES [dbo].[motivos_movimiento] ([id]),
    ADD CONSTRAINT [FK_movimientos_sede]
    FOREIGN KEY ([sede_id]) REFERENCES [dbo].[sedes] ([id]),
    ADD CONSTRAINT [FK_movimientos_usuario]
    FOREIGN KEY ([usuario_id]) REFERENCES [dbo].[usuarios] ([id]),
    ADD CONSTRAINT [FK_movimientos_lote]
    FOREIGN KEY ([lote_id]) REFERENCES [dbo].[lotes] ([id]);

-- atenciones -> sedes, pacientes, usuarios
ALTER TABLE [dbo].[atenciones]
    ADD CONSTRAINT [FK_atenciones_sede]
    FOREIGN KEY ([sede_id]) REFERENCES [dbo].[sedes] ([id]),
    ADD CONSTRAINT [FK_atenciones_paciente]
    FOREIGN KEY ([paciente_id]) REFERENCES [dbo].[pacientes] ([id]),
    ADD CONSTRAINT [FK_atenciones_usuario]
    FOREIGN KEY ([usuario_id]) REFERENCES [dbo].[usuarios] ([id]);

-- atencion_detalles -> atenciones, lotes
ALTER TABLE [dbo].[atencion_detalles]
    ADD CONSTRAINT [FK_atencion_detalle_atencion]
    FOREIGN KEY ([atencion_id]) REFERENCES [dbo].[atenciones] ([id]),
    ADD CONSTRAINT [FK_atencion_detalle_lote]
    FOREIGN KEY ([lote_id]) REFERENCES [dbo].[lotes] ([id]);
GO

-- =============================================
-- DATOS INICIALES - CATALOGOS
-- Formato de IDs: XXX-00-0000001
-- =============================================

-- Roles
INSERT INTO [dbo].[roles] ([id], [nombre]) VALUES
('ROL-00-0000001', 'Administrador'),
('ROL-00-0000002', 'Quimico Farmaceutico'),
('ROL-00-0000003', 'Tecnico de Farmacia'),
('ROL-00-0000004', 'Medico');

-- Categorias
INSERT INTO [dbo].[categorias] ([id], [nombre]) VALUES
('CAT-00-0000001', 'Analgesicos'),
('CAT-00-0000002', 'Antibioticos'),
('CAT-00-0000003', 'Antiinflamatorios'),
('CAT-00-0000004', 'Suministros Medicos');

-- Tipos de movimiento
INSERT INTO [dbo].[tipos_movimiento] ([id], [nombre]) VALUES
('MOV-T-00-0000001', 'entrada'),
('MOV-T-00-0000002', 'salida');

-- Motivos de movimiento
INSERT INTO [dbo].[motivos_movimiento] ([id], [nombre]) VALUES
('MOV-M-00-0000001', 'compra'),
('MOV-M-00-0000002', 'transferencia'),
('MOV-M-00-0000003', 'atencion'),
('MOV-M-00-0000004', 'merma');
GO

-- =============================================
-- DATOS DE PRUEBA (SEED)
-- =============================================

-- Sedes
INSERT INTO [dbo].[sedes] ([id], [nombre], [direccion], [telefono], [ubigeo], [tipo_sede], [capacidad_almacen], [is_activa]) VALUES
('SED-00-0000001', 'Sede Central Lima',   'Av. Principal 123',  '012345678', '150101', 'Posta Medica',     1000, 1),
('SED-00-0000002', 'Posta San Juan',      'Av. Peru 456',      '012345679', '150102', 'Centro de Salud',  500,  1);

-- Usuarios (password: admin123)
INSERT INTO [dbo].[usuarios] ([id], [sede_id], [rol_id], [tipo_documento], [numero_documento], [nombres], [apellidos], [password], [is_activo]) VALUES
('USR-00-0000001', 'SED-00-0000001', 'ROL-00-0000001', 'DNI', '12345678', 'Admin',   'Sistema',   'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU=', 1),
('USR-00-0000002', 'SED-00-0000001', 'ROL-00-0000002', 'DNI', '22222222', 'Jefe',    'Farmacia',  'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU=', 1),
('USR-00-0000003', 'SED-00-0000001', 'ROL-00-0000003', 'DNI', '33333333', 'Tecnico', 'Operativo', 'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU=', 1);

-- Pacientes
INSERT INTO [dbo].[pacientes] ([id], [tipo_documento], [numero_documento], [nombres], [apellidos], [telefono], [is_activo]) VALUES
('PAC-00-0000001', 'DNI', '44556677', 'Juan',   'Perez Garcia', '988777666', 1),
('PAC-00-0000002', 'CE',  '22334455', 'Maria',  'Lopez Sosa',   '911222333', 1),
('PAC-00-0000003', 'DNI', '55443322', 'Carlos', 'Mendez Ruiz',  '987654321', 1);

-- Productos
INSERT INTO [dbo].[productos] ([id], [categoria_id], [codigo_digemid], [nombre], [detalle], [unidad_medida], [stock_minimo], [precio_unitario], [is_activo]) VALUES
('PRD-00-0000001', 'CAT-00-0000001', 'DIG-001', 'Paracetamol 500mg',    'Tabletas para el dolor',      'Caja x 100',    10, 3.50,  1),
('PRD-00-0000002', 'CAT-00-0000002', 'DIG-002', 'Amoxicilina 500mg',    'Capsulas antibioticas',       'Frasco',        15, 12.00, 1),
('PRD-00-0000003', 'CAT-00-0000003', 'DIG-003', 'Ibuprofeno 400mg',     'Antiinflamatorio potente',     'Caja x 50',    10, 5.00,  1),
('PRD-00-0000004', 'CAT-00-0000004', 'DIG-004', 'Alcohol en Gel 70%',   'Desinfectante de manos',      'Botella 500ml', 20, 8.00,  1),
('PRD-00-0000005', 'CAT-00-0000001', 'DIG-005', 'Aspirina 100mg',       'Protector cardiovascular',    'Caja x 30',    10, 2.75,  1),
('PRD-00-0000006', 'CAT-00-0000002', 'DIG-006', 'Azitromicina 500mg',   'Tratamiento respiratorio',    'Caja x 3',      5, 15.00, 1);

-- Lotes
INSERT INTO [dbo].[lotes] ([id], [producto_id], [sede_id], [numero_lote], [fecha_fabricacion], [fecha_vencimiento], [cantidad]) VALUES
-- Stock saludable (vence 2027)
('LOT-00-0000001', 'PRD-00-0000001', 'SED-00-0000001', 'L2024-001',   '2024-01-01', '2027-06-01', 150),
('LOT-00-0000002', 'PRD-00-0000004', 'SED-00-0000001', 'L2024-002',   '2024-02-15', '2027-08-20', 80),
-- Stock critico (< 10)
('LOT-00-0000003', 'PRD-00-0000002', 'SED-00-0000001', 'L2024-CRIT',  '2024-01-10', '2027-05-15', 3),
('LOT-00-0000004', 'PRD-00-0000006', 'SED-00-0000001', 'L2024-LOW',   '2024-03-01', '2027-04-10', 5),
-- Proximos a vencer (julio 2026)
('LOT-00-0000005', 'PRD-00-0000003', 'SED-00-0000001', 'L-VENC-1',    '2024-05-01', '2026-07-15', 45),
('LOT-00-0000006', 'PRD-00-0000005', 'SED-00-0000001', 'L-VENC-2',    '2024-06-01', '2026-06-25', 20);

-- Movimientos de ejemplo
INSERT INTO [dbo].[movimientos] ([id], [tipo_id], [motivo_id], [sede_id], [usuario_id], [lote_id], [cantidad], [observacion], [fecha_registro]) VALUES
('MOV-00-0000001', 'MOV-T-00-0000001', 'MOV-M-00-0000001', 'SED-00-0000001', 'USR-00-0000001', 'LOT-00-0000001', 500,  'Ingreso inicial por compra a proveedor',              '2026-05-21 06:08:50'),
('MOV-00-0000002', 'MOV-T-00-0000002', 'MOV-M-00-0000003', 'SED-00-0000001', 'USR-00-0000001', 'LOT-00-0000001', 25,   'Entrega de medicamentos en consulta externa',         '2026-05-21 06:08:50'),
('MOV-00-0000003', 'MOV-T-00-0000002', 'MOV-M-00-0000004', 'SED-00-0000001', 'USR-00-0000001', 'LOT-00-0000002', 10,   'Productos vencidos retirados del inventario',         '2026-05-21 06:08:50'),
('MOV-00-0000004', 'MOV-T-00-0000001', 'MOV-M-00-0000002', 'SED-00-0000001', 'USR-00-0000001', 'LOT-00-0000003', 120,  'Transferencia recibida desde sede central',           '2026-05-21 06:08:50'),
('MOV-00-0000005', 'MOV-T-00-0000002', 'MOV-M-00-0000002', 'SED-00-0000001', 'USR-00-0000001', 'LOT-00-0000003', 50,   'Transferencia enviada a sede norte',                   '2026-05-21 06:08:50');

-- Atenciones de ejemplo
INSERT INTO [dbo].[atenciones] ([id], [sede_id], [paciente_id], [usuario_id], [numero_receta], [medico], [fecha_atencion]) VALUES
('ATN-00-0000001', 'SED-00-0000001', 'PAC-00-0000001', 'USR-00-0000002', 'REC-001', 'Dr. Garcia',  '2026-06-15 10:30:00'),
('ATN-00-0000002', 'SED-00-0000001', 'PAC-00-0000002', 'USR-00-0000003', 'REC-002', 'Dra. Lopez',  '2026-06-16 14:00:00');

-- Detalles de atencion
INSERT INTO [dbo].[atencion_detalles] ([id], [atencion_id], [lote_id], [cantidad_entregada]) VALUES
('ATN-D-0000001', 'ATN-00-0000001', 'LOT-00-0000001', 2),
('ATN-D-0000002', 'ATN-00-0000001', 'LOT-00-0000002', 1),
('ATN-D-0000003', 'ATN-00-0000002', 'LOT-00-0000003', 3);
GO

PRINT 'Meditrack DB: esquema y datos cargados correctamente';
GO
