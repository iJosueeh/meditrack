# Requerimientos Funcionales - MediTrack

Este documento enumera los requerimientos funcionales del sistema MediTrack y su estado actual de implementación.

| ID | Módulo | Rol | Descripción | Estado |
|:---|:---|:---|:---|:---|
| RF-01 | M1 – Acceso y Seguridad | Técnico, Jefe de Sede, Admin | Validar credenciales del usuario al iniciar sesión | **Implementado** |
| RF-02 | M1 – Acceso y Seguridad | Técnico, Jefe de Sede, Admin | Restringir el acceso a pantallas y operaciones según el rol autenticado; ocultar funcionalidades no permitidas en la interfaz. | **Implementado** |
| RF-03 | M1 – Acceso y Seguridad | Técnico, Jefe de Sede, Admin | Segmentar la información por sede: cada usuario solo visualiza y modifica datos de la posta a la que pertenece (sede_id). | **Implementado** |
| RF-04 | M2 – Gestión de Sedes | Admin global | Registrar nuevas sedes con nombre, dirección y estado activo/inactivo. | Pendiente |
| RF-05 | M2 – Gestión de Sedes | Admin global | Editar datos de una sede existente y aplicar borrado lógico (is_activa = 0) sin eliminar su historial. | Pendiente |
| RF-06 | M3 – Catálogo de Productos | Admin global | Registrar productos con código DIGEMID, nombre, detalle, unidad de medida, categoría y estado activo/inactivo. | Pendiente |
| RF-07 | M3 – Catálogo de Productos | Admin global | Gestionar categorías de productos: crear, editar y listar categorías disponibles. | Pendiente |
| RF-08 | M4 – Lotes e Inventario | Jefe de Sede, Admin | Registrar lotes vinculados a producto y sede, con número de lote del proveedor, fechas de fabricación y vencimiento, y cantidad inicial. | **Parcial** (UI lista, lógica pendiente) |
| RF-09 | M4 – Lotes e Inventario | Técnico, Jefe de Sede, Admin | Visualizar el stock actual agrupado por lote y sede, con indicador de cantidad disponible en tiempo real. | **Implementado** |
| RF-10 | M4 – Lotes e Inventario | Técnico, Jefe de Sede, Admin | Generar alertas visuales en el dashboard para lotes próximos a vencer, con umbral configurable de 30, 60 o 90 días. | **Implementado** |
| RF-11 | M5 – Entradas de Inventario | Jefe de Sede | Registrar movimientos de entrada (recepción de proveedor, transferencia entrante) especificando tipo, motivo, lote, cantidad y observación. | **Parcial** (UI lista, lógica pendiente) |
| RF-12 | M5 – Entradas de Inventario | Jefe de Sede | Actualizar automáticamente la cantidad del lote afectado al confirmar la entrada, dentro de una transacción ACID. | Pendiente |
| RF-13 | M6 – Salidas y Ajustes | Técnico, Jefe de Sede | Registrar movimientos de salida manualmente (merma, vencimiento, ajuste) especificando tipo, motivo, lote, cantidad y observación. | **Parcial** (UI lista, lógica pendiente) |
| RF-14 | M6 – Salidas y Ajustes | Técnico, Jefe de Sede | Aplicar lógica FEFO en salidas manuales: el sistema sugiere el lote con fecha de vencimiento más próxima y lo resalta en la interfaz. | **Implementado** |
| RF-15 | M6 – Salidas y Ajustes | Técnico, Jefe de Sede | Descontar automáticamente la cantidad del lote al confirmar la salida, dentro de una transacción ACID. | Pendiente |
| RF-16 | M7 – Gestión de Pacientes | Técnico, Jefe de Sede | Registrar pacientes con tipo de documento, número de documento, nombres, apellidos y teléfono. | **Parcial** (DAO/Controller base listo) |
| RF-17 | M7 – Gestión de Pacientes | Técnico, Jefe de Sede | Editar datos de un paciente existente y aplicar borrado lógico (is_activo = 0) para preservar su historial de atenciones. | **Parcial** (DAO base listo) |
| RF-18 | M8 – Registro de Atenciones | Técnico | Registrar la cabecera de una atención vinculando paciente, sede, usuario responsable, número de receta y fecha de atención. | **Parcial** (UI en progreso) |
| RF-19 | M8 – Registro de Atenciones | Técnico | Buscar atenciones previas por paciente o número de receta para consulta o modificación del registro. | Pendiente |
| RF-20 | M9 – Dispensación | Técnico | Registrar el detalle de medicamentos entregados por atención: lote dispensado y cantidad entregada. | **Parcial** (UI en progreso) |
| RF-21 | M9 – Dispensación | Técnico | Aplicar FEFO automáticamente durante la dispensación: mostrar explícitamente el lote sugerido y su fecha de vencimiento antes de confirmar. | **Implementado** |
| RF-22 | M9 – Dispensación | Técnico | Descontar el stock del lote correspondiente únicamente tras la confirmación explícita del usuario, dentro de una transacción ACID. | Pendiente |
| RF-23 | M10 – Reportes y Dashboard | Jefe de Sede, Admin | Mostrar dashboard con indicadores: stock crítico, lotes por vencer, movimientos recientes y resumen de atenciones del día. | **Implementado** |
| RF-24 | M10 – Reportes y Dashboard | Jefe de Sede, Admin | Generar reporte PDF de movimientos de inventario filtrado por sede, rango de fechas y tipo de movimiento. | **Parcial** (Generación PDF lista, filtros específicos en progreso) |
| RF-25 | M10 – Reportes y Dashboard | Jefe de Sede, Admin | Generar reporte PDF de dispensaciones filtrado por paciente, sede y rango de fechas. | Pendiente |
