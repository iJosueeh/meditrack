# MediTrack - Diagrama de Base de Datos
# Generado para dbdiagram.io (https://dbdiagram.io/d)
# Sintaxis: https://dbdiagram.io/docs

// =====================================================================
// ROLES Y PERMISOS (RBAC)
// =====================================================================

Table roles {
  id varchar(50) [pk]
  nombre varchar(100] [unique, not null]
  descripcion varchar(255)
  nivel int [default: 99]
  is_sistema int [default: 0]
  is_activo int [default: 1]
}

Table permisos {
  id varchar(50) [pk]
  codigo varchar(50) [unique, not null]
  nombre varchar(100) [not null]
  descripcion varchar(255)
  modulo varchar(50) [not null]
  orden int [default: 0]
  is_activo int [default: 1]
}

Table rol_permisos {
  rol_id varchar(50) [pk]
  permiso_id varchar(50) [pk]
  fecha_asignacion datetime [default: 'now()']
  
  Note: 'Tabla pivote N:N entre roles y permisos'
}

Ref: rol_permisos.rol_id > roles.id
Ref: rol_permisos.permiso_id > permisos.id

// =====================================================================
// SEDES Y USUARIOS
// =====================================================================

Table sedes {
  id varchar(50) [pk]
  nombre varchar(255) [not null]
  direccion varchar(500)
  telefono varchar(50)
  ubigeo varchar(20)
  tipo_sede varchar(50)
  capacidad_almacen int [default: 0]
  administrador_id varchar(50)
  is_activa int [default: 1]
  is_bloqueada int [default: 0]
  motivo_bloqueo varchar(500)
  fecha_bloqueo datetime
}

Table usuarios {
  id varchar(50) [pk]
  sede_id varchar(50)
  rol_id varchar(50)
  tipo_documento varchar(50)
  numero_documento varchar(50) [unique]
  nombres varchar(255)
  apellidos varchar(255)
  telefono varchar(50)
  ubigeo varchar(20)
  password varchar(max)
  is_activo int [default: 1]
}

Ref: usuarios.sede_id > sedes.id
Ref: usuarios.rol_id > roles.id
Ref: sedes.administrador_id > usuarios.id

// =====================================================================
// PACIENTES
// =====================================================================

Table pacientes {
  id varchar(50) [pk]
  tipo_documento varchar(50)
  numero_documento varchar(50) [unique]
  nombres varchar(255)
  apellidos varchar(255)
  telefono varchar(50)
  is_activo int [default: 1]
}

// =====================================================================
// CATÁLOGOS DE PRODUCTOS
// =====================================================================

Table categorias {
  id varchar(50) [pk]
  nombre varchar(100) [unique, not null]
  is_activo int [default: 1]
}

Table productos {
  id varchar(50) [pk]
  categoria_id varchar(50)
  codigo_digemid varchar(100) [unique]
  nombre varchar(255)
  detalle varchar(255)
  unidad_medida varchar(50)
  stock_minimo int [default: 10]
  precio_unitario decimal(18,2) [default: 0]
  is_activo int [default: 1]
}

Ref: productos.categoria_id > categorias.id

// =====================================================================
// LOTES Y MOVIMIENTOS DE INVENTARIO
// =====================================================================

Table lotes {
  id varchar(50) [pk]
  producto_id varchar(50)
  sede_id varchar(50)
  numero_lote varchar(100)
  fecha_vencimiento date
  fecha_fabricacion date
  cantidad int
}

Ref: lotes.producto_id > productos.id
Ref: lotes.sede_id > sedes.id

Table tipos_movimiento {
  id varchar(50) [pk]
  nombre varchar(50) [not null]
  is_activo int [default: 1]
  
  Note: 'Ej: entrada, salida'
}

Table motivos_movimiento {
  id varchar(50) [pk]
  nombre varchar(100) [not null]
  is_activo int [default: 1]
  
  Note: 'Ej: compra, transferencia, merma, atencion'
}

Table movimientos {
  id varchar(50) [pk]
  tipo_id varchar(50)
  motivo_id varchar(50)
  sede_id varchar(50)
  usuario_id varchar(50)
  lote_id varchar(50)
  cantidad int
  observacion varchar(max)
  fecha_registro datetime [default: 'now()']
}

Ref: movimientos.tipo_id > tipos_movimiento.id
Ref: movimientos.motivo_id > motivos_movimiento.id
Ref: movimientos.sede_id > sedes.id
Ref: movimientos.usuario_id > usuarios.id
Ref: movimientos.lote_id > lotes.id

// =====================================================================
// ATENCIONES Y DISPENSACIÓN
// =====================================================================

Table atenciones {
  id varchar(50) [pk]
  sede_id varchar(50)
  paciente_id varchar(50)
  usuario_id varchar(50)
  numero_receta varchar(100)
  medico varchar(255)
  fecha_atencion datetime [default: 'now()']
}

Ref: atenciones.sede_id > sedes.id
Ref: atenciones.paciente_id > pacientes.id
Ref: atenciones.usuario_id > usuarios.id

Table atencion_detalles {
  id varchar(50) [pk]
  atencion_id varchar(50)
  lote_id varchar(50)
  cantidad_entregada int
}

Ref: atencion_detalles.atencion_id > atenciones.id
Ref: atencion_detalles.lote_id > lotes.id
