# MediTrack - Requisitos Funcionales

## Descripción de Roles

### Administrador Global (Nivel 1)
Accede a **todos los módulos sin restricción de sede**. Gestiona el catálogo global de productos, categorías, tipos y motivos de movimiento, y tiene acceso consolidado a reportes de todas las postas. Es el único con permisos de escritura sobre productos (M3) y gestión de roles y usuarios globales.

### Jefe de Sede (Nivel 3)
Supervisa el inventario de su posta. Registra entradas de proveedor (M5), gestiona lotes (M4), pacientes (M7), categorías (CATEGORIAS), productos de solo lectura (M3) y usuarios de su sede (USUARIOS). Accede a reportes de su sede (M10).

### Técnico de Farmacia (Nivel 4)
Responsable de las operaciones diarias. Registra atenciones (M8), dispensaciones (M9), salidas y ajustes de inventario (M6), y consulta pacientes (M7). Accede a reportes de su sede (M10).

---

## Matriz de Permisos por Rol

| Módulo | Permiso | Admin | Jefe de Sede | Técnico |
|--------|---------|:---:|:---:|:---:|
| M1 – Inicio de Sesión | `M1_LOGIN` | ✅ | ✅ | ✅ |
| M2 – Gestión de Sedes | `M2_SEDES` | ✅ | ❌ | ❌ |
| M3 – Catálogo de Productos | `M3_PRODUCTOS` | ✅ (lectura/escritura) | ✅ (solo lectura) | ❌ |
| M4 – Gestión de Lotes | `M4_LOTES` | ✅ | ✅ | ✅ (solo lectura) |
| M5 – Entradas de Inventario | `M5_ENTRADAS` | ✅ | ✅ | ❌ |
| M6 – Salidas y Ajustes | `M6_SALIDAS` | ✅ | ✅ | ✅ |
| M7 – Gestión de Pacientes | `M7_PACIENTES` | ✅ | ✅ | ✅ |
| M8 – Registro de Atenciones | `M8_ATENCIONES` | ✅ | ❌ | ✅ |
| M9 – Dispensación | `M9_DISPENSACION` | ✅ | ❌ | ✅ |
| M10 – Reportes y Dashboard | `M10_REPORTES` | ✅ (consolidado) | ✅ (su sede) | ✅ (su sede) |
| Categorías | `CATEGORIAS` | ✅ (lectura/escritura) | ✅ (solo lectura) | ✅ (solo lectura) |
| Usuarios | `USUARIOS` | ✅ (global) | ✅ (parcial: su sede) | ❌ |
| Catálogos de Movimiento | `MOV_CATALOGOS` | ✅ | ❌ | ❌ |
| Roles | `ROLES` | ✅ | ❌ | ❌ |

---

## Requisitos Funcionales Detallados

### M1 – Inicio de Sesión
> Roles: Todos

- **RF-01**: Validar credenciales del usuario al iniciar sesión.
- **RF-02**: Restringir acceso a vistas y operaciones según el rol autenticado; ocultar funcionalidades no permitidas en la interfaz. Segmentar información por sede: cada usuario solo visualiza y modifica datos de la posta a la que pertenece (sede_id). Excepción: el Administrador accede a datos de todas las sedes.

---

### M2 – Gestión de Sedes
> Roles: Administrador (único con acceso)

- **RF-03**: Registrar nuevas sedes con nombre, dirección y estado activo/inactivo.
- **RF-04**: Editar datos de una sede existente. Aplicar borrado lógico (is_activa = 0) sin eliminar su historial o borrado físico únicamente cuando no posea información asociada.
- **RF-05**: Bloquear/desbloquear sedes para impedir operaciones (entradas, salidas, atenciones, dispensación) cuando una sede no está operativa.

---

### M3 – Catálogo de Productos
> Roles: Administrador (lectura/escritura), Jefe de Sede (solo lectura)

- **RF-06**: Registrar productos con código DIGEMID, nombre, detalle, unidad de medida, categoría y estado activo/inactivo.
- **RF-07**: Editar y eliminar productos. La eliminación es únicamente borrado lógico (is_activo = 0) si el producto tiene lotes asociados.

---

### M4 – Gestión de Lotes
> Roles: Administrador, Jefe de Sede (registro); Todos (visualización)

- **RF-08**: Registrar lotes vinculados a producto y sede, con número de lote del proveedor, fechas de fabricación y vencimiento, y cantidad inicial.
- **RF-09**: Visualizar el stock actual agrupado por lote y sede, con indicador de cantidad disponible en tiempo real.
- **RF-10**: Mostrar en el dashboard los lotes próximos a vencer (alertas configurables: 30, 60 o 90 días antes del vencimiento).
- **RF-11**: Editar observación de un movimiento de inventario; no se permite modificar tipo, motivo o cantidad una vez confirmado. La anulación revierte el stock.

---

### M5 – Entradas de Inventario
> Roles: Administrador, Jefe de Sede

- **RF-12**: Registrar movimientos de entrada (recepción de proveedor, transferencia entrante) especificando tipo, motivo, lote, cantidad y observación.
- **RF-13**: Actualizar automáticamente la cantidad del lote afectado al confirmar la entrada, dentro de una transacción ACID.
- **RF-14**: Anular movimientos de entrada: revierte la cantidad agregada al lote y marca el movimiento como anulado.

---

### M6 – Salidas y Ajustes
> Roles: Técnico de Farmacia, Jefe de Sede

- **RF-15**: Registrar movimientos de salida manualmente (merma, vencimiento, ajuste) especificando tipo, motivo, lote, cantidad y observación.
- **RF-16**: Aplicar lógica FEFO en salidas: el sistema sugiere el lote con fecha de vencimiento más próxima y lo resalta en la interfaz.
- **RF-17**: Descontar automáticamente la cantidad del lote al confirmar la salida, dentro de una transacción ACID.
- **RF-18**: Anular movimientos de salida: revierte la cantidad descontada del lote y marca el movimiento como anulado.

---

### M7 – Gestión de Pacientes
> Roles: Técnico de Farmacia, Jefe de Sede, Administrador

- **RF-19**: Registrar pacientes con tipo de documento, número de documento, nombres, apellidos y teléfono.
- **RF-20**: Editar datos de un paciente existente. Aplicar borrado lógico (is_activo = 0) para preservar su historial de atenciones o borrado físico únicamente cuando no posea información asociada.
- **RF-21**: Listar pacientes de forma global (sin filtro por sede); todos los usuarios ven el catálogo completo de pacientes activos.

---

### M8 – Registro de Atenciones
> Roles: Técnico de Farmacia

- **RF-22**: Registrar la cabecera de una atención vinculando paciente, sede, usuario responsable, número de receta y médico prescriptor (opcional).
- **RF-23**: Auto-generar el número de receta en formato `REC-AAAA-XXXX` (correlativo por sede y año), editable por el usuario.
- **RF-24**: Buscar atenciones previas por paciente o número de receta para consulta o modificación del registro.

---

### M9 – Dispensación
> Roles: Técnico de Farmacia

- **RF-25**: Registrar el detalle de medicamentos entregados por atención: lote dispensado y cantidad entregada.
- **RF-26**: Aplicar FEFO automáticamente durante la dispensación: mostrar el lote sugerido con su fecha de vencimiento antes de confirmar.
- **RF-27**: Descontar el stock del lote correspondiente únicamente tras la confirmación explícita del usuario, dentro de una transacción ACID.

---

### M10 – Reportes y Dashboard
> Roles: Administrador (consolidado), Jefe de Sede (su sede), Técnico (su sede)

- **RF-28**: Mostrar dashboard con indicadores: stock crítico, lotes por vencer, movimientos recientes y resumen de atenciones del día.
- **RF-29**: Generar reporte PDF de movimientos de inventario filtrado por sede, rango de fechas y tipo de movimiento.
- **RF-30**: Generar reporte PDF de dispensaciones filtrado por paciente, sede y rango de fechas.

---

### Gestión de Roles
> Roles: Administrador (único con acceso)

- **RF-31**: Crear, editar y activar/desactivar roles. Validar jerarquía: no se permite crear ni editar roles de nivel igual o menor al del usuario autenticado.
- **RF-32**: Asignar permisos a roles mediante checkboxes organizados por módulo (Operaciones, Mantenimiento, Sistema).

---

### Gestión de Categorías
> Roles: Administrador (lectura/escritura), Jefe de Sede, Técnico (solo lectura)

- **RF-33**: Registrar categorías de productos especificando nombre y estado activo/inactivo.
- **RF-34**: Editar y activar/desactivar categorías. No se permite eliminar categorías con productos asociados.

---

### Gestión de Usuarios
> Roles: Administrador (global), Jefe de Sede (su sede)

- **RF-35**: Crear usuarios con nombre, tipo y número de documento, teléfono y sede asignada.
- **RF-36**: Editar, resetear contraseña y activar/desactivar usuarios. El Jefe de Sede solo gestiona usuarios de su propia sede.

---

## Roles del Sistema

| ID | Nombre | Nivel | Descripción |
|---|---|:---:|---|
| ROL-001 | Administrador global | 1 | Acceso total sin restricción de sede |
| ROL-002 | Jefe de Sede | 3 | Supervisa su posta: entradas, lotes, pacientes, reportes |
| ROL-003 | Técnico de Farmacia | 4 | Operaciones diarias: atenciones, dispensación, salidas |
| ROL-XXX | Auxiliar de Farmacia | 5 | Operador de apoyo (configurable) |

---

## Formato de Identificadores

| Entidad | Formato | Ejemplo |
|---------|---------|---------|
| Sede | `SED-XXX` | `SED-001`, `SED-002` |
| Usuario | `USR-XXX-XXXX` | `USR-001-0001`, `USR-002-0001` |
| Paciente | `PAC-XXX-XXX` | `PAC-001-001` |
| Producto | `PRD-XXX-XXX` | `PRD-001-001` |
| Categoría | `CAT-XXX-XXX` | `CAT-001-001` |
| Lote | `LT-XXX-XXX` | `LT-001-001` |
| Atención | `ATN-XXX-XXX` | `ATN-001-001` |
| Receta | `REC-AAAA-XXXX` | `REC-2026-0001` |

---

## Credenciales de Prueba

| Rol | DNI | Usuario | Contraseña |
|---|---|---|---|
| Administrador global | 12345678 | `USR-001-0002` | admin123 |
| Jefe de Sede | 22222222 | `USR-002-0001` | admin123 |
| Técnico de Farmacia | 33333333 | `USR-001-0001` | admin123 |
