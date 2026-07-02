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

- **RF-01**: Validar credenciales (DNI y contraseña) al iniciar sesión. Si son correctas, cargar el perfil del usuario con su rol y sede asignada. Si fallan, mostrar mensaje de error sin indicar cuál campo es incorrecto por seguridad.

- **RF-02**: Restringir acceso a vistas y operaciones según el rol autenticado; ocultar funcionalidades no permitidas en la barra lateral. Segmentar información por sede: cada usuario solo visualiza y modifica datos de la posta a la que pertenece. Excepción: el Administrador accede a datos de todas las sedes sin filtro.

---

### M2 – Gestión de Sedes
> Roles: Administrador (único con acceso)

- **RF-03**: Registrar nuevas sedes con nombre, dirección, teléfono (9 dígitos), ubigeo, tipo de sede (Posta Médica, Hospital, Almacén Central, Centro de Salud) y estado activo/inactivo.

- **RF-04**: Editar datos de una sede existente (nombre, dirección, teléfono, ubigeo, tipo, estado). Respecto a la eliminación:
  - Si la sede **NO** tiene usuarios, lotes, movimientos de inventario ni atenciones asociados → se elimina **permanentemente** (borrado físico de la fila en la base de datos).
  - Si la sede **SÍ** tiene historial asociado → el sistema muestra un mensaje de advertencia indicando qué datos asociados existen y **no permite eliminar**. En este caso se debe usar la opción **"Desactivar"** (borrado lógico: `is_activa = 0`), que mantiene todos los datos pero impide el uso de la sede.
  - La eliminación permanente requiere confirmación explícita del usuario antes de ejecutarse.

- **RF-05**: Bloquear/desbloquear sedes. Cuando una sede está bloqueada, impide todas las operaciones de inventario (entradas, salidas, atenciones, dispensación). El bloqueo solicita un motivo obligatorio y muestra el estado "BLOQUEADA" en el listado.

---

### M3 – Catálogo de Productos
> Roles: Administrador (lectura/escritura), Jefe de Sede (solo lectura)

- **RF-06**: Registrar productos con código DIGEMID, nombre comercial, nombre genérico, detalle, unidad de medida, categoría asignada y estado activo/inactivo. Los productos activos se muestran en los listados de selección.

- **RF-07**: Editar datos de un producto existente. La eliminación es únicamente **borrado lógico** (`is_activo = 0`): si el producto tiene lotes asociados, no se puede borrar físicamente porque se perdería la trazabilidad del inventario. Al desactivarlo ya no aparece en listados activos pero sus lotes y movimientos se preservan.

---

### M4 – Gestión de Lotes
> Roles: Administrador, Jefe de Sede (registro); Todos (visualización)

- **RF-08**: Registrar lotes vinculados a un producto y una sede, con número de lote del proveedor, fechas de fabricación y vencimiento, cantidad inicial y observación.

- **RF-09**: Visualizar el stock actual agrupado por lote y sede, con indicador de cantidad disponible en tiempo real. Muestra el estado del lote (Activo / Próximo a vencer / Vencido).

- **RF-10**: Mostrar en el dashboard los lotes próximos a vencer según umbral configurable (30, 60 o 90 días antes del vencimiento). Cada lote vencido o por vencer muestra color de alerta en el indicador.

- **RF-11**: Editar únicamente la observación de un movimiento de inventario; no se permite modificar tipo, motivo o cantidad una vez confirmado. La anulación revierte el stock del lote correspondiente.

---

### M5 – Entradas de Inventario
> Roles: Administrador, Jefe de Sede

- **RF-12**: Registrar movimientos de entrada con tipo (Recepciones de Proveedor, Donaciones, Transferencias Entrantes, Otros), motivo, lote destino, cantidad y observación. El lote debe existir y estar activo.

- **RF-13**: Al confirmar la entrada, automáticamente aumenta la cantidad disponible del lote afectado, todo dentro de una transacción ACID (si falla algo, se revierte todo).

- **RF-14**: Anular movimientos de entrada: revierte la cantidad agregada al lote y marca el movimiento como "Anulado". No se elimina el registro, solo cambia su estado.

---

### M6 – Salidas y Ajustes
> Roles: Técnico de Farmacia, Jefe de Sede

- **RF-15**: Registrar movimientos de salida con tipo (Salidas por Merma, Salidas por Vencimiento, Ajustes de Inventario, Transferencias Salientes, Otros), motivo, lote origen, cantidad y observación.

- **RF-16**: Aplicar lógica **FEFO** (First Expire, First Out) en salidas: el sistema sugiere automáticamente el lote con fecha de vencimiento más próxima y lo resalta en la interfaz para que el usuario lo confirme o elija otro.

- **RF-17**: Al confirmar la salida, descontar automáticamente la cantidad del lote origen, todo dentro de una transacción ACID.

- **RF-18**: Anular movimientos de salida: revierte la cantidad descontada del lote y marca el movimiento como "Anulado". No se elimina el registro.

---

### M7 – Gestión de Pacientes
> Roles: Técnico de Farmacia, Jefe de Sede, Administrador

- **RF-19**: Registrar pacientes con tipo de documento (DNI, Carné de Extranjería, Pasaporte, Otros), número de documento, nombres, apellidos y teléfono (9 dígitos). Todos los campos son obligatorios excepto teléfono.

- **RF-20**: Editar datos de un paciente existente. Respecto a la eliminación:
  - Si el paciente **NO** tiene movimientos de inventario ni atenciones registradas → se elimina **permanentemente** (borrado físico).
  - Si el paciente **SÍ** tiene historial asociado → el sistema muestra un mensaje de advertencia y **no permite eliminar**. Se debe contactar al administrador para evaluar la desactivación, que aplica borrado lógico (`is_activo = 0`) preservando todos los datos.
  - La eliminación permanente requiere confirmación explícita antes de ejecutarse.

- **RF-21**: Listar pacientes de forma global (sin filtro por sede); todos los usuarios ven el catálogo completo de pacientes activos del sistema.

---

### M8 – Registro de Atenciones
> Roles: Técnico de Farmacia

- **RF-22**: Registrar la cabecera de una atención vinculando paciente (buscado por número de documento), sede del usuario, usuario responsable, número de receta y nombre del médico prescriptor (opcional). Fecha y hora se registran automáticamente.

- **RF-23**: Auto-generar el número de receta en formato `REC-AAAA-XXXX` (correlativo por sede y año), editable por el usuario antes de confirmar.

- **RF-24**: Buscar atenciones previas por paciente o número de receta para consulta o modificación del registro. Muestra fecha, sede, usuario responsable, paciente y número de receta.

---

### M9 – Dispensación
> Roles: Técnico de Farmacia

- **RF-25**: Registrar el detalle de medicamentos entregados por atención: producto dispensado, lote utilizado, cantidad entregada y observación. Cada línea representa un medicamento entregueado en esa atención.

- **RF-26**: Aplicar **FEFO** automáticamente durante la dispensación: antes de confirmar, el sistema muestra el lote sugerido (el que vence primero) con su fecha de vencimiento para que el usuario confirme o elija otro lote del mismo producto.

- **RF-27**: Descontar el stock del lote correspondiente únicamente tras la confirmación explícita del usuario, dentro de una transacción ACID. Si no hay stock disponible, se muestra advertencia.

---

### M10 – Reportes y Dashboard
> Roles: Administrador (consolidado), Jefe de Sede (su sede), Técnico (su sede)

- **RF-28**: Mostrar dashboard con indicadores: stock crítico (lotes con cantidad 0 o bajo umbral), lotes por vencer (según umbral configurado), últimos 5 movimientos de inventario y resumen de atenciones del día (cantidad de atenciones registradas).

- **RF-29**: Generar reporte PDF de movimientos de inventario. El usuario selecciona sede (una o todas), rango de fechas (fecha inicio y fin) y tipo de movimiento (Entrada, Salida, Todos). El PDF exporta una tabla con las columnas: **Fecha y hora**, **Tipo** (Entrada/Salida), **Motivo**, **Producto** (nombre del producto del lote), **Lote** (número de lote), **Cantidad** (unidades movidas), **Usuario responsable** (nombre completo), **Sede**, **Observación**. Si no hay movimientos en el periodo, el PDF muestra mensaje de "Sin registros".

- **RF-30**: Generar reporte PDF de dispensaciones. El usuario selecciona paciente (opcional, si se deja en blanco aplica a todos), sede (una o todas) y rango de fechas. El PDF exporta una tabla con las columnas: **Fecha y hora de atención**, **Número de Receta**, **Paciente** (nombre completo), **Sede**, **Producto dispensado**, **Lote** (número), **Fecha de vencimiento** (del lote), **Cantidad dispensada**, **Usuario responsable** (técnico que dispensó), **Observación**. Al final del PDF se incluye un **resumen**: total de atenciones, total de productos dispensados y total de unidades entregadas.

---

### Gestión de Roles
> Roles: Administrador (único con acceso)

- **RF-31**: Crear y editar roles especificando nombre, nivel de jerarquía y estado activo/inactivo. **Validación de jerarquía**: no se permite crear ni editar roles cuyo nivel sea igual o menor al nivel del usuario autenticado (ejemplo: un Admin nivel 1 no puede crear un rol nivel 1 ni nivel 3, solo nivel inferior como 5). Tampoco se permite desactivar el propio rol ni el único rol restante de nivel 1.

- **RF-32**: Asignar permisos a roles mediante checkboxes organizados por módulo (Operaciones, Mantenimiento, Sistema). Cada permiso es un código como `M1_LOGIN`, `M2_SEDES`, etc. Al guardar, se reemplazan todos los permisos del rol por la nueva selección.

---

### Gestión de Categorías
> Roles: Administrador (lectura/escritura), Jefe de Sede, Técnico (solo lectura)

- **RF-33**: Registrar categorías de productos especificando nombre y estado activo/inactivo. Las categorías activas aparecen en el combo de selección al registrar o editar productos.

- **RF-34**: Editar y activar/desactivar categorías existentes. **No se permite eliminar** categorías que tienen productos asociados — el sistema muestra advertencia y sugiere usar "Desactivar" en su lugar. La desactivación preserva los productos ya que la categoría solo cambia a inactiva y ya no aparece en los listados activos.

---

### Gestión de Usuarios
> Roles: Administrador (global), Jefe de Sede (su sede)

- **RF-35**: Crear usuarios con tipo y número de documento, nombres, apellidos, teléfono, rol asignado y sede asignada. La contraseña inicial se define en la creación y el usuario debe cambiarla en su primer inicio de sesión.

- **RF-36**: Editar datos de un usuario (nombre, documento, rol, sede), resetear contraseña (vuelve a `admin123`) y activar/desactivar usuarios. El Jefe de Sede solo puede gestionar usuarios de su propia sede; el Administrador gestiona todos. Respecto a la **eliminación permanente**:
  - Si el usuario **NO** tiene movimientos de inventario ni atenciones registradas → se elimina **permanentemente** (borrado físico de la fila).
  - Si el usuario **SÍ** tiene historial asociado → el sistema muestra un mensaje de advertencia indicando que tiene movimientos o atenciones y **no permite eliminar**. Se debe usar **"Desactivar"** (borrado lógico: `is_activo = 0`) para bloquearlo sin perder su historial.
  - La eliminación permanente requiere confirmación explícita (clic en botón Eliminar + diálogo de confirmación Aceptar/Cancelar).

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
| Paciente | `PAC-XXX-XXXXXX` | `PAC-001-000001`, `PAC-002-000001` |
| Producto | `PRD-XXX-XXX` | `PRD-001-001` |
| Categoría | `CAT-XXX-XXX` | `CAT-001-001` |
| Lote | `LOT-XXX-XXXXX` | `LOT-001-00001`, `LOT-002-00001` |
| Movimiento | `MOV-XXX-XXXXXX` | `MOV-001-000001` |
| Atención | `ATE-XXX-XXXXXX` | `ATE-001-000001` |
| Detalle de Atención | `ATD-XXX-XXXXXXXX` | `ATD-001-00000001` |
| Receta | `REC-AAAA-XXXX` | `REC-2026-0001` |

---

## Credenciales de Prueba

| Rol | DNI | Usuario | Contraseña |
|---|---|---|---|
| Administrador global | 12345678 | `USR-001-0002` | admin123 |
| Jefe de Sede | 22222222 | `USR-002-0001` | admin123 |
| Técnico de Farmacia | 33333333 | `USR-001-0001` | admin123 |
