<h1 align="center">🏥 MediTrack</h1>
<p align="center">
  <strong>Sistema Especializado de Gestión de Inventario para Postas de Salud</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/JavaFX-21-blue?style=flat-square" alt="JavaFX">
  <img src="https://img.shields.io/badge/AtlantaFX-Primer-6C63FF?style=flat-square" alt="AtlantaFX">
  <img src="https://img.shields.io/badge/SQL%20Server-CC2927?style=flat-square&logo=microsoft-sql-server&logoColor=white" alt="SQL Server">
  <img src="https://img.shields.io/badge/Maven-3.9-C71A36?style=flat-square&logo=apache-maven&logoColor=white" alt="Maven">
  <img src="https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-2088FF?style=flat-square&logo=github-actions&logoColor=white" alt="CI/CD">
</p>

<p align="center">
  <a href="#-características">Características</a> •
  <a href="#️-stack-tecnológico">Stack</a> •
  <a href="#-guía-de-inicio-rápido">Inicio Rápido</a> •
  <a href="#-documentación-técnica">Docs</a>
</p>

---

## ✨ Características

<table>
  <tr>
    <td width="50%">

#### 📦 Inventario Inteligente
- Registro de medicamentos con **códigos DIGEMID**
- Algoritmo **FEFO** (First Expire, First Out) automático
- Control de lotes con trazabilidad completa

#### 💊 Dispensación y Atención
- Flujo integrado de **atención → dispensación**
- Selección automática de lotes por vencimiento
- Registro de pacientes con historial clínico

#### 🏢 Multi-Sede
- Gestión centralizada de múltiples postas
- Control de acceso por sede (RBAC)
- IDs secuenciales descriptivos (`PAC-001-000001`)

    </td>
    <td width="50%">

#### 📊 Dashboard en Tiempo Real
- Métricas de salud de inventario
- Alertas de stock crítico y vencimiento
- Actividad diaria de dispensación

#### 🛡️ Seguridad y Roles
- Control de acceso basado en **roles (RBAC)**
- Identificación dinámica de jefaturas
- Auditoría de movimientos

#### 🖥️ Interfaz Moderna
- Diseño **Fluent/Minimalist** con AtlantaFX
- Iconografía **FontAwesome 5** (Ikonli)
- Soporte completo **UTF-8** y responsive

    </td>
  </tr>
</table>

---

## 🛠️ Stack Tecnológico

| Capa | Tecnología | Propósito |
| :--- | :--- | :--- |
| **Core** | Java 21 (Eclipse Adoptium) | Lenguaje y runtime LTS |
| **UI** | JavaFX 21 + AtlantaFX Primer | Interfaz de escritorio moderna |
| **Iconos** | Ikonli (FontAwesome 5) | Iconografía nativa |
| **Base de Datos** | SQL Server (Docker) | Persistencia transaccional |
| **Testing** | JUnit 5 | Pruebas de integración |
| **Build** | Maven 3.9 | Compilación y dependencias |
| **CI/CD** | GitHub Actions | Integración continua |

---

## 🚀 Guía de Inicio Rápido

### Requisitos

| Requisito | Versión mínima | Enlace |
| :--- | :--- | :--- |
| JDK | 21+ | [Eclipse Adoptium](https://adoptium.net/) |
| Maven | 3.8+ | [Apache Maven](https://maven.apache.org/download.cgi) |
| Docker | Latest | [Docker Desktop](https://www.docker.com/) |

### 1. Base de Datos (Docker)

```bash
# Iniciar SQL Server en contenedor
docker-compose up -d

# Verificar estado
docker-compose ps
```

> **Nota:** El primer inicio ejecuta automáticamente `meditrack_init.sql` que crea las tablas y carga datos iniciales. Espere ~45 segundos antes de usar la app.

### 2. Compilar y Ejecutar

```bash
# Compilar
./mvnw clean compile

# Ejecutar
./mvnw javafx:run
```

> En Windows: usar `mvnw.cmd` en lugar de `./mvnw`

### 3. Ejecutar Pruebas

```bash
./mvnw test
```

---

## 📂 Estructura del Proyecto

```
src/main/java/com/utp/meditrackapp/
├── application/        # Casos de uso y orquestación
├── core/               # Config, utilidades, servicios compartidos
├── domain/             # Entidades, puertos y lógica de negocio
├── features/           # Módulos por funcionalidad
│   ├── attentions/     # Atención y dispensación
│   ├── auth/           # Login y sesión
│   ├── catalogs/       # Roles, categorías, movimientos
│   ├── dashboard/      # Panel principal y reportes
│   ├── inventory/      # Inventario de sede
│   ├── patients/       # Gestión de pacientes
│   ├── products/       # Catálogo de productos
│   ├── sedes/          # Gestión de sedes
│   └── users/          # Gestión de usuarios
├── infrastructure/     # Adaptadores JDBC y persistencia
└── shared/             # Componentes compartidos (sidebar, navbar)

src/main/resources/com/utp/meditrackapp/
├── *.fxml              # Vistas JavaFX
├── styles/
│   ├── global.css      # Estilos compartidos
│   └── dashboard.css   # Estilos del dashboard
└── templates/pdf/      # Plantillas de reportes
```

---

## 📖 Documentación Técnica

| Documento | Descripción |
| :--- | :--- |
| 📘 [Arquitectura](./docs/ARCHITECTURE.md) | Estructura de paquetes, IDs secuenciales y transaccionalidad |
| 📋 [Requerimientos](./docs/FUNCTIONAL_REQUIREMENTS.md) | Estado de implementación por módulo |
| 🎨 [Guía de Diseño](./docs/UI_GUIDE.md) | Paleta de colores, componentes y convenciones UI |
| 🧪 [Testing y CI](./docs/TESTING_CI.md) | Estrategia de pruebas y automatización |

---

<p align="center">
  <sub>Diseñado con 💙 para la mejora de la Salud Pública</sub><br>
  <sub>© 2026 MediTrack</sub>
</p>
