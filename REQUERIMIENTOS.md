# MediTrack - Requisitos Funcionales

## Descripción de Roles

### Administrador Global (Nivel 1)
Accede a **todos los módulos sin restricción de sede**. Es el único que puede crear y editar sedes (M2), gestionar el catálogo de productos (M3) y ver reportes consolidados de todas las postas (M10). Gestiona roles, usuarios, categorías y catálogos de movimiento.

### Químico Farmacéutico / Jefe de Sede (Nivel 3)
Supervisa el inventario de su posta, registra entradas de proveedor (M5), gestiona usuarios de su sede (M2 parcial) y accede a los reportes de su sede (M10). También gestiona lotes (M4), categorías y operaciones de salida (M6) dentro de su sede.

### Técnico de Farmacia (Nivel 4)
Opera los módulos de atención al paciente (M7, M8, M9) y registra salidas y ajustes de inventario (M6). Es el usuario de mayor volumen de operaciones diarias.

---

## Matriz de Permisos por Rol

| Módulo | Permiso | Administrador | Jefe de Sede | Técnico |
|--------|---------|:---:|:---:|:---:|
| M1 – Inicio de Sesión | `M1_LOGIN` | ✅ | ✅ | ✅ |
| M2 – Gestión de Sedes | `M2_SEDES` | ✅ (CRUD completo) | ❌ | ❌ |
| M3 – Catálogo de Productos | `M3_PRODUCTOS` | ✅ (CRUD completo) | ❌ | ❌ |
| M4 – Gestión de Lotes | `M4_LOTES` | ✅ | ✅ | ✅ (solo visualizar) |
| M5 – Entradas de Inventario | `M5_ENTRADAS` | ❌ | ✅ | ❌ |
| M6 – Salidas y Ajustes | `M6_SALIDAS` | ❌ | ✅ | ✅ |
| M7 – Gestión de Pacientes | `M7_PACIENTES` | ❌ | ✅ | ✅ |
| M8 – Registro de Atenciones | `M8_ATENCIONES` | ❌ | ❌ | ✅ |
| M9 – Dispensación | `M9_DISPENSACION` | ❌ | ❌ | ✅ |
| M10 – Reportes y Dashboard | `M10_REPORTES` | ✅ (consolidado) | ✅ (su sede) | ❌ |
| Categorías | `CATEGORIAS` | ✅ | ✅ | ❌ |
| Usuarios | `USUARIOS` | ✅ (global) | ✅ (parcial: su sede) | ❌ |
| Catálogos de Movimiento | `MOV_CATALOGOS` | ✅ | ❌ | ❌ |
| Roles | `ROLES` | ✅ | ❌ | ❌ |

---

## Requisitos Funcional es Detallados

### M1 – Inicio de Sesión
> Todos los roles

- **RF-01**: Validar credenciales del usuario al iniciar sesión.
- **RF-02**: Restringir el acceso a vistas y operaciones según el rol autenticado; ocultar funcionalidades no permitidas en la interfaz. Segmentar la información por sede: cada usuario solo visualiza y modifica datos de la posta a la que pertenece (sede_id). **Excepción**: El Administrador accede a datos de todas las sedes.

---

### M2 – Gestión de Sedes
> Roles: Administrador (único con acceso)

- **RF-03**: Registrar nuevas sedes con nombre, dirección y estado activo/inactivo.
- **RF-04**: Editar datos de una sede existente y aplicar borrado lógico (is_activa = 0) sin eliminar su historial o borrado físico (total) únicamente cuando no posea información asociada.
- **RF-04b**: Bloquear/desbloquear sedes para impedir operaciones (entradas, salidas, atenciones, dispensación) cuando una sede no está operativa.

---

### M3 – Catálogo de Productos
> Roles: Administrador (único con acceso)

- **RF-05**: Registrar productos con código DIGEMID, nombre, detalle, unidad de medida, categoría y estado activo/inactivo.

---

### M4 – Gestión de Lotes
> Roles: Administrador, Jefe de Sede (registro); Administrador, Jefe de Sede, Técnico (visualización)

- **RF-06**: Registrar lotes vinculados a producto y sede, con número de lote del proveedor, fechas de fabricación y vencimiento, y cantidad inicial.
- **RF-07**: Visualizar el stock actual agrupado por lote y sede, con indicador de cantidad disponible en tiempo real.
- **RF-08**: Mostrar el estado del inventario en el dashboard para lotes próximos a vencer, con umbral configurable de 30, 60 o 90 días.

---

### M5 – Entradas de Inventario
> Roles: Jefe de Sede

- **RF-12**: Registrar movimientos de entrada (recepción de proveedor, transferencia entrante) especificando tipo, motivo, lote, cantidad y observación.
- **RF-13**: Actualizar automáticamente la cantidad del lote afectado al confirmar la entrada, dentro de una transacción ACID.
- **RF-14**: Eliminar movimientos de entrada de inventario. Al confirmar la anulación, el sistema deberá eliminar la cantidad agregada al lote afectado.

---

### M6 – Salidas y Ajustes
> Roles: Técnico de Farmacia, Jefe de Sede

- **RF-15**: Registrar movimientos de salida manualmente (merma, vencimiento, ajuste) especificando tipo, motivo, lote, cantidad y observación.
- **RF-16**: Aplicar lógica FEFO en salidas manuales: El sistema sugiere el lote con fecha de vencimiento más próxima y lo resalta en la interfaz.
- **RF-17**: Descontar automáticamente la cantidad del lote al confirmar la salida, dentro de una transacción ACID.

---

### M7 – Gestión de Pacientes
> Roles: Técnico de Farmacia, Jefe de Sede

- **RF-18**: Registrar pacientes con tipo de documento, número de documento, nombres, apellidos y teléfono.
- **RF-19**: Editar datos de un paciente existente y aplicar borrado lógico (is_activo = 0) para preservar su historial de atenciones o borrado físico (total) únicamente cuando no posea información asociada.

---

### M8 – Registro de Atenciones
> Roles: Técnico de Farmacia

- **RF-20**: Registrar la cabecera de una atención vinculando paciente, sede, usuario responsable, número de receta y fecha de atención.
- **RF-21**: Buscar atenciones previas por paciente o número de receta para consulta o modificación del registro.

---

### M9 – Dispensación
> Roles: Técnico de Farmacia

- **RF-22**: Registrar el detalle de medicamentos entregados por atención: lote dispensado y cantidad entregada.
- **RF-23**: Aplicar FEFO automáticamente durante la dispensación: mostrar explícitamente el lote sugerido y su fecha de vencimiento antes de confirmar.
- **RF-24**: Descontar el stock del lote correspondiente únicamente tras la confirmación explícita del usuario, dentro de una transacción ACID.

---

### M10 – Reportes y Dashboard
> Roles: Administrador (consolidado), Jefe de Sede (su sede)

- **RF-25**: Mostrar dashboard con indicadores, como gráficos y tablas: stock crítico, lotes por vencer, movimientos recientes y resumen de atenciones del día.
- **RF-26**: Generar reporte PDF de movimientos de inventario filtrado por sede, rango de fechas y tipo de movimiento.
- **RF-27**: Generar reporte PDF de dispensaciones filtrado por paciente, sede y rango de fechas.

---

## Roles del Sistema

| ID | Nombre | Nivel | Descripción |
|---|---|:---:|---|
| ROL-001 | Administrador | 1 | Mayor – Acceso total: sedes, productos, reportes consolidados, roles, usuarios globales |
| ROL-002 | Químico Farmacéutico (Jefe de Sede) | 3 | Media – Gestiona su sede: entradas, lotes, usuarios parcial, reportes de sede |
| ROL-003 | Técnico de Farmacia | 4 | Menor – Operaciones diarias: atenciones, dispensación, salidas, pacientes |

## Credenciales de Prueba

| Rol | DNI | Usuario | Contraseña |
|---|---|---|---|
| Administrador | 12345678 | USR-001 | admin123 |
| Químico Farmacéutico | 22222222 | USR-002 | admin123 |
| Técnico de Farmacia | 33333333 | USR-003 | admin123 |
