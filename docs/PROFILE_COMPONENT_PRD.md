# PRD - Componente de Perfil de Usuario (Profile Component)

## 1. Descripción General
Interfaz de perfil de usuario para la aplicación MediTrack que permite a los usuarios visualizar y gestionar su información personal, configuración de seguridad y preferencias del sistema.

---

## 2. Estructura de Layout

### 2.1 Sidebar de Navegación
- **Posición**: Lateral izquierdo
- **Color de Fondo**: Azul Marino/Navy (#1B3C5F o similar)
- **Ancho**: ~120px (colapsable)
- **Items del Menú**:
  - Dashboard (icono de gráfico/home)
  - Atención (icono de corazón/médico)
  - Inventario (icono de caja/inventario)
  - Catálogo (icono de libro/catálogo)
  - Usuarios (icono de personas)
  - Reportes (icono de documento)
  - Ajustes (icono de engranaje)
  - Cerrar Sesión (icono de salida)

**Características del Sidebar**:
- Íconos en blanco/gris claro
- Texto en blanco (si está expandido)
- Hover effect: cambio de color de fondo a un azul más claro
- Estado activo: fondo destacado (azul más claro)

---

## 3. Encabezado Superior (Header)

### 3.1 Barra de Título
- **Sección Izquierda**:
  - Breadcrumb: "Perfil de Usuario" con opciones de pestañas
    - Pestaña: "Datos Personal" (activa)
    - Pestaña: "Configuración"
  
- **Sección Derecha**:
  - Campana de notificaciones (icono gris)
  - Icono de ayuda/información
  - Avatar de usuario (circular con iniciales o foto)
  - Dropdown menú con opciones:
    - Mi Cuenta
    - Mi Perfil
    - Configuración
    - Seguridad
    - Cerrar Sesión

---

## 4. Área Principal de Contenido

### 4.1 Sección de Información Personal

**Estructura Visual**:
```
┌─────────────────────────────────────────┐
│  [Foto de Usuario]  Dr. Alejandro Rivas │
│  (Avatar Circular)  Administrador       │
│                     Estado Central      │
└─────────────────────────────────────────┘
```

**Datos Mostrados**:
- **Foto**: Avatar circular con borde gris claro
- **Nombre Completo**: "Dr. Alejandro Rivas"
- **Rol**: "Administrador" (en gris/descriptivo)
- **Estado**: "Estado Central" (etiqueta con ícono)

**Información Detallada**:
- Nombre Completo: Alejandro Rivas Montesinos
- DMI / Identificación: 7844525D-X
- Correo Electrónico: alejandro.rivas@meditrackystem
- Teléfono de Contacto: +34 8 1677 60560

**Colores de Campos**:
- Labels: Gris oscuro (#333 o similar)
- Valores: Negro (#000)
- Íconos: Azul (similar al de navegación)

---

### 4.2 Sección de Resumen de Actividad

**Tarjetas de Estadísticas**:
```
┌──────────────────┬──────────────────┬──────────────────┐
│  Últimos Accesos │  Reportes Gener. │  Estado de Cuenta│
│  Hoy, 08:45 AM   │      124         │   ACTIVO         │
└──────────────────┴──────────────────┴──────────────────┘
```

**Estilos**:
- Tarjetas con fondo blanco
- Borde gris claro (#e0e0e0)
- Títulos en gris (#666)
- Valores en azul oscuro o resaltados
- Estado ACTIVO: Badge verde con texto blanco

---

### 4.3 Sección de Seguridad y Acceso

**Elementos**:
- **Título**: "Seguridad y Acceso" (con icono de candado 🔒)
- **Subsecciones**:

  1. **Contraseña de Usuario**
     - Estado: "Actualizada hace 45 días"
     - Botón: "Cambiar Contraseña" (color azul)
     - Icono: Candado cerrado ✓

  2. **Autenticación de Dos Factores (2FA)**
     - Estado: "Activada ayer 3:45 / App"
     - Botón: "Configurar" (color azul)
     - Icono: Escudo/doble factor ✓

---

### 4.4 Sección de Preferencias del Sistema

**Elementos**:

1. **Idioma del Interfaz**
   - Selector desplegable: "Español (Latinoamericano)"
   - Icono: Globo/idioma

2. **Frecuencia de Notificaciones**
   - Selector: "Inmediatas"
   - Icono: Campana

3. **Notificaciones por Correo**
   - Toggle: Activado (azul/verde)
   - Descripción: "Recibir alertas de inactividad bajo mi email"

4. **Modo de Alta Densidad**
   - Toggle: Desactivado (gris)
   - Descripción: "Optimizar táblas para visualización de grandes datos"

---

### 4.5 Sección de Acciones de Cuenta

**Elemento**: Tarjeta de alerta/warning
- **Ícono**: Triángulo de advertencia (⚠️ color naranja/rojo)
- **Título**: "Acciones de Cuenta"
- **Descripción**: "Peligro - todas las sesiones activas se pueden desactivar disponibles"
- **Botón**: "Cerrar Todas las Sesiones" (color rojo/peligro)

---

## 5. Paleta de Colores

| Componente | Color | Código Hex | Uso |
|-----------|-------|-----------|-----|
| Sidebar | Azul Navy | #1B3C5F | Fondo navegación |
| Accents Primarios | Azul Claro | #3B7CA8 | Botones, links, activos |
| Fondo General | Blanco | #FFFFFF | Contenido principal |
| Bordes | Gris Claro | #E0E0E0 | Separadores |
| Texto Principal | Negro | #000000 | Contenido |
| Texto Secundario | Gris | #666666 | Labels, descripciones |
| Estado Activo | Verde | #4CAF50 | Badges activos |
| Alerta/Peligro | Rojo | #F44336 | Acciones críticas |
| Warning | Naranja | #FF9800 | Advertencias |
| Éxito | Verde | #4CAF50 | Confirmaciones |

---

## 6. Elementos de Interfaz (UI Elements)

### 6.1 Íconos Utilizados
- **Navegación Sidebar**:
  - Dashboard: 📊
  - Atención: ❤️
  - Inventario: 📦
  - Catálogo: 📚
  - Usuarios: 👥
  - Reportes: 📄
  - Ajustes: ⚙️
  - Cerrar Sesión: 🚪

- **Encabezado**:
  - Notificaciones: 🔔
  - Ayuda: ℹ️
  - Cuenta: 👤
  - Menú Desplegable: ▼

- **Contenido Principal**:
  - Información Personal: 👤
  - Correo: ✉️
  - Teléfono: ☎️
  - Seguridad: 🔒
  - Acceso: 🔐
  - Sistema: ⚙️

### 6.2 Botones
- **Botón Primario**: Azul claro con texto blanco
  - Padding: 8px 16px
  - Border-radius: 4px
  - Hover: Azul más oscuro

- **Botón Peligro**: Rojo con texto blanco
  - "Cerrar Todas las Sesiones"

- **Botón Secundario**: Borde gris con texto oscuro

### 6.3 Toggles/Switches
- **Activos**: Color azul/verde (#3B7CA8)
- **Inactivos**: Color gris claro (#CCCCCC)
- **Tamaño**: 40px × 24px

### 6.4 Dropdowns
- Fondo blanco
- Borde gris
- Opciones con hover gris claro
- Animación suave

---

## 7. Scrollbar

**Características**:
- **Posición**: Lateral derecha del contenido
- **Ancho**: 8-10px
- **Color**: Gris oscuro (#999) con opacidad
- **Thumb (Barra deslizable)**:
  - Color: Gris oscuro
  - Hover: Gris más oscuro
  - Border-radius: 4px
  - Margen: 2px desde el borde

---

## 8. Responsive Design

### 8.1 Desktop (>1200px)
- Sidebar expandido
- Contenido con ancho máximo
- 2-3 columnas

### 8.2 Tablet (768px - 1199px)
- Sidebar colapsable
- Contenido adaptado
- Simplificación de tarjetas

### 8.3 Mobile (<768px)
- Sidebar oculto con menú hamburguesa
- Una sola columna
- Full width

---

## 9. Interacciones y Animaciones

### 9.1 Hover Effects
- Botones: Cambio de color + sombra ligera
- Tarjetas: Sombra suave
- Links: Subrayado azul

### 9.2 Transiciones
- Duración: 200-300ms
- Easing: ease-in-out

### 9.3 Estados
- **Cargando**: Spinner gris
- **Error**: Mensaje rojo
- **Éxito**: Confirmación verde

---

## 10. Componentes FXML Correspondientes

```
profile-view.fxml
├── BorderPane (raíz)
│   ├── Left: Sidebar (VBox con navegación)
│   └── Center: ScrollPane (contenido principal)
│       └── VBox
│           ├── HBox (Header/Tabs)
│           ├── HBox (Información Personal)
│           ├── VBox (Resumen de Actividad)
│           ├── VBox (Seguridad y Acceso)
│           ├── VBox (Preferencias del Sistema)
│           └── VBox (Acciones de Cuenta)
```

---

## 11. Notas Técnicas

- Implementar con FXML + CSS
- Archivo CSS separado: `profile.css`
- Usar Scene Builder para diseño
- Responsive con AnchorPane o BorderPane
- Datos vinculados mediante controlador ProfileController
