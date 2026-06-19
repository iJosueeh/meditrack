-- =============================================
-- SCRIPT DE DATOS DE PRUEBA (SEED) PARA MEDITRACK
-- Consolidado: init.sql crea estructura, este script populate datos
-- =============================================

USE meditrack_db;
GO

-- 1. CATEGORIAS
IF NOT EXISTS (SELECT 1 FROM categorias WHERE id = 'CAT-01')
INSERT INTO [categorias] ([id], [nombre]) VALUES 
('CAT-01', 'Analgésicos'),
('CAT-02', 'Antibióticos'),
('CAT-03', 'Antiinflamatorios'),
('CAT-04', 'Suministros Médicos');
GO

-- 2. PRODUCTOS (con stock_minimo y precio_unitario)
IF NOT EXISTS (SELECT 1 FROM productos WHERE id = 'PRD-01')
INSERT INTO [productos] ([id], [categoria_id], [codigo_digemid], [nombre], [detalle], [unidad_medida], [stock_minimo], [precio_unitario], [is_activo]) VALUES 
('PRD-01', 'CAT-01', 'DIG-001', 'Paracetamol 500mg', 'Tabletas para el dolor', 'Caja x 100', 10, 3.50, 1),
('PRD-02', 'CAT-02', 'DIG-002', 'Amoxicilina 500mg', 'Cápsulas antibióticas', 'Frasco', 15, 12.00, 1),
('PRD-03', 'CAT-03', 'DIG-003', 'Ibuprofeno 400mg', 'Antiinflamatorio potente', 'Caja x 50', 10, 5.00, 1),
('PRD-04', 'CAT-04', 'DIG-004', 'Alcohol en Gel 70%', 'Desinfectante de manos', 'Botella 500ml', 20, 8.00, 1),
('PRD-05', 'CAT-01', 'DIG-005', 'Aspirina 100mg', 'Protector cardiovascular', 'Caja x 30', 10, 2.75, 1),
('PRD-06', 'CAT-02', 'DIG-006', 'Azitromicina 500mg', 'Tratamiento respiratorio', 'Caja x 3', 5, 15.00, 1);
GO

-- 3. SEDES (con campos nuevos: ubigeo, tipo_sede, capacidad_almacen)
IF NOT EXISTS (SELECT 1 FROM sedes WHERE id = 'SED-001')
INSERT INTO [sedes] ([id], [nombre], [direccion], [telefono], [ubigeo], [tipo_sede], [capacidad_almacen], [is_activa]) VALUES 
('SED-001', 'Sede Central Lima', 'Av. Principal 123', '012345678', '150101', 'Posta Médica', 1000, 1),
('SED-002', 'Posta San Juan', 'Av. Peru 456', '012345679', '150102', 'Centro de Salud', 500, 1);
GO

-- 4. USUARIOS (con campos nuevos: telefono, ubigeo)
-- Password: 'admin123' (hash BCrypt)
IF NOT EXISTS (SELECT 1 FROM usuarios WHERE id = 'USR-001')
INSERT INTO [usuarios] ([id], [sede_id], [rol_id], [tipo_documento], [numero_documento], [nombres], [apellidos], [telefono], [ubigeo], [password], [is_activo])
VALUES 
('USR-001', 'SED-001', 'ROL-001', 'DNI', '12345678', 'Admin', 'Sistema', '988111222', '150101', 'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU=', 1),
('USR-002', 'SED-001', 'ROL-002', 'DNI', '22222222', 'Jefe', 'Farmacia', '988222333', '150101', 'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU=', 1),
('USR-003', 'SED-001', 'ROL-003', 'DNI', '33333333', 'Tecnico', 'Operativo', '988333444', '150101', 'gtLXTLxK5ju8hjct2v5uiQ==:9QRw+doH87Pe5YkHZtBI8cge8dLt79pBdkyRwck6LqU=', 1);
GO

-- 5. PACIENTES
IF NOT EXISTS (SELECT 1 FROM pacientes WHERE id = 'PAC-01')
INSERT INTO [pacientes] ([id], [tipo_documento], [numero_documento], [nombres], [apellidos], [telefono], [is_activo]) VALUES 
('PAC-01', 'DNI', '44556677', 'Juan', 'Perez Garcia', '988777666', 1),
('PAC-02', 'CE', '22334455', 'Maria', 'Lopez Sosa', '911222333', 1),
('PAC-03', 'DNI', '55443322', 'Carlos', 'Mendez Ruiz', '987654321', 1);
GO

-- 6. LOTES (con fechas fijas para testing - basados en fecha: 19 Jun 2026)
-- Lotes con Stock Saludable (vence en 2027)
IF NOT EXISTS (SELECT 1 FROM lotes WHERE id = 'LT-01')
INSERT INTO [lotes] ([id], [producto_id], [sede_id], [numero_lote], [fecha_fabricacion], [fecha_vencimiento], [cantidad]) VALUES 
('LT-01', 'PRD-01', 'SED-001', 'L2024-001', '2024-01-01', '2027-06-01', 150),
('LT-02', 'PRD-04', 'SED-001', 'L2024-002', '2024-02-15', '2027-08-20', 80);

-- Lotes con STOCK CRITICO (Menos de 10)
IF NOT EXISTS (SELECT 1 FROM lotes WHERE id = 'LT-03')
INSERT INTO [lotes] ([id], [producto_id], [sede_id], [numero_lote], [fecha_fabricacion], [fecha_vencimiento], [cantidad]) VALUES 
('LT-03', 'PRD-02', 'SED-001', 'L2024-CRIT', '2024-01-10', '2027-05-15', 3),
('LT-04', 'PRD-06', 'SED-001', 'L2024-LOW', '2024-03-01', '2027-04-10', 5);

-- Lotes PROXIMOS A VENCER (30 dias o menos desde Jun 2026)
IF NOT EXISTS (SELECT 1 FROM lotes WHERE id = 'LT-05')
INSERT INTO [lotes] ([id], [producto_id], [sede_id], [numero_lote], [fecha_fabricacion], [fecha_vencimiento], [cantidad]) VALUES 
('LT-05', 'PRD-03', 'SED-001', 'L-VENC-1', '2024-05-01', '2026-07-15', 45),
('LT-06', 'PRD-05', 'SED-001', 'L-VENC-2', '2024-06-01', '2026-06-25', 20);
GO

-- 7. TIPOS Y MOTIVOS DE MOVIMIENTO
IF NOT EXISTS (SELECT 1 FROM tipos_movimiento WHERE id = 'MOV-T-01')
INSERT INTO [tipos_movimiento] ([id], [nombre]) VALUES 
('MOV-T-01', 'entrada'),
('MOV-T-02', 'salida');

IF NOT EXISTS (SELECT 1 FROM motivos_movimiento WHERE id = 'MOV-M-01')
INSERT INTO [motivos_movimiento] ([id], [nombre]) VALUES 
('MOV-M-01', 'compra'),
('MOV-M-02', 'transferencia'),
('MOV-M-03', 'atencion'),
('MOV-M-04', 'merma');
GO

-- 8. ATENCIONES DE EJEMPLO
IF NOT EXISTS (SELECT 1 FROM atenciones WHERE id = 'ATN-001')
INSERT INTO [atenciones] ([id], [sede_id], [paciente_id], [usuario_id], [numero_receta], [medico], [fecha_atencion]) VALUES 
('ATN-001', 'SED-001', 'PAC-01', 'USR-002', 'REC-001', 'Dr. Garcia', '2026-06-15 10:30:00'),
('ATN-002', 'SED-001', 'PAC-02', 'USR-003', 'REC-002', 'Dra. Lopez', '2026-06-16 14:00:00');
GO

-- 9. DETALLES DE ATENCION
IF NOT EXISTS (SELECT 1 FROM atencion_detalles WHERE id = 'ATN-D-001')
INSERT INTO [atencion_detalles] ([id], [atencion_id], [lote_id], [cantidad_entregada]) VALUES 
('ATN-D-001', 'ATN-001', 'LT-01', 2),
('ATN-D-002', 'ATN-001', 'LT-02', 1),
('ATN-D-003', 'ATN-002', 'LT-03', 3);
GO

PRINT 'Seed data loaded successfully';
