-- =============================================
-- SCRIPT DE DATOS DE PRUEBA (SEED) PARA MEDITRACK
-- =============================================

USE meditrack_db;
GO

-- Limpiar datos previos (opcional, cuidado con FKs)
-- DELETE FROM atencion_detalles; DELETE FROM atenciones; DELETE FROM movimientos; DELETE FROM lotes; DELETE FROM productos; DELETE FROM categorias;

-- 1. CATEGORIAS
INSERT INTO [categorias] ([id], [nombre]) VALUES 
('CAT-01', 'Analgésicos'),
('CAT-02', 'Antibióticos'),
('CAT-03', 'Antiinflamatorios'),
('CAT-04', 'Suministros Médicos');

-- 2. PRODUCTOS
INSERT INTO [productos] ([id], [categoria_id], [codigo_digemid], [nombre], [detalle], [unidad_medida], [is_activo]) VALUES 
('PRD-01', 'CAT-01', 'DIG-001', 'Paracetamol 500mg', 'Tabletas para el dolor', 'Caja x 100', 1),
('PRD-02', 'CAT-02', 'DIG-002', 'Amoxicilina 500mg', 'Cápsulas antibióticas', 'Frasco', 1),
('PRD-03', 'CAT-03', 'DIG-003', 'Ibuprofeno 400mg', 'Antinflamatorio potente', 'Caja x 50', 1),
('PRD-04', 'CAT-04', 'DIG-004', 'Alcohol en Gel 70%', 'Desinfectante de manos', 'Botella 500ml', 1),
('PRD-05', 'CAT-01', 'DIG-005', 'Aspirina 100mg', 'Protector cardiovascular', 'Caja x 30', 1),
('PRD-06', 'CAT-02', 'DIG-006', 'Azitromicina 500mg', 'Tratamiento respiratorio', 'Caja x 3', 1);

-- 3. LOTES (Aquí generamos la data dinámica para el Dashboard)

-- Lotes con Stock Saludable
INSERT INTO [lotes] ([id], [producto_id], [sede_id], [numero_lote], [fecha_fabricacion], [fecha_vencimiento], [cantidad]) VALUES 
('LT-01', 'PRD-01', 'SED-001', 'L2024-001', '2024-01-01', '2026-12-31', 150),
('LT-02', 'PRD-04', 'SED-001', 'L2024-002', '2024-02-15', '2025-08-20', 80);

-- Lotes con STOCK CRITICO (Menos de 10)
INSERT INTO [lotes] ([id], [producto_id], [sede_id], [numero_lote], [fecha_fabricacion], [fecha_vencimiento], [cantidad]) VALUES 
('LT-03', 'PRD-02', 'SED-001', 'L2024-CRIT', '2024-01-10', '2026-05-15', 3),
('LT-04', 'PRD-06', 'SED-001', 'L2024-LOW', '2024-03-01', '2026-04-10', 5);

-- Lotes PROXIMOS A VENCER (En los próximos 30 días respecto a Mayo 2026)
-- Nota: Usamos fechas relativas a hoy para que la prueba sea efectiva hoy 14 de mayo de 2026.
INSERT INTO [lotes] ([id], [producto_id], [sede_id], [numero_lote], [fecha_fabricacion], [fecha_vencimiento], [cantidad]) VALUES 
('LT-05', 'PRD-03', 'SED-001', 'L-VENC-1', '2024-05-01', '2026-05-30', 45),
('LT-06', 'PRD-05', 'SED-001', 'L-VENC-2', '2024-06-01', '2026-06-05', 20);

-- 4. PACIENTES (Para que no esté vacío)
INSERT INTO [pacientes] ([id], [tipo_documento], [numero_documento], [nombres], [apellidos], [telefono], [is_activo]) VALUES 
('PAC-01', 'DNI', '44556677', 'Juan', 'Perez Garcia', '988777666', 1),
('PAC-02', 'CE', '22334455', 'Maria', 'Lopez Sosa', '911222333', 1);

-- 5. TIPOS Y MOTIVOS (Ya están en init.sql pero aseguramos consistencia)
IF NOT EXISTS (SELECT 1 FROM tipos_movimiento WHERE id = 'MOV-T-01')
INSERT INTO [tipos_movimiento] ([id], [nombre]) VALUES ('MOV-T-01', 'entrada'), ('MOV-T-02', 'salida');

IF NOT EXISTS (SELECT 1 FROM motivos_movimiento WHERE id = 'MOV-M-01')
INSERT INTO [motivos_movimiento] ([id], [nombre]) VALUES ('MOV-M-01', 'compra'), ('MOV-M-02', 'transferencia'), ('MOV-M-03', 'atencion'), ('MOV-M-04', 'merma');
