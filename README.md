<h1 align="center">🏥 MediTrack</h1>
<p align="center">
  <strong>Sistema Especializado de Gestión de Inventario para Postas de Salud</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge" alt="JavaFX">
  <img src="https://img.shields.io/badge/Maven-Build-green?style=for-the-badge&logo=apache-maven" alt="Maven">
  <img src="https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-brightgreen?style=for-the-badge&logo=github-actions" alt="CI/CD">
</p>

---

### 📝 Descripción
**MediTrack** es una solución robusta diseñada específicamente para la administración eficiente de recursos médicos en **Postas de Salud**. Su objetivo es garantizar la disponibilidad constante de suministros críticos, optimizar la trazabilidad de lotes y facilitar la toma de decisiones mediante una interfaz intuitiva de alta fidelidad visual.

---

### ✨ Características Destacadas

| Característica | Detalle |
| :--- | :--- |
| **📦 Catálogo de Productos** | Registro completo de medicamentos con códigos DIGEMID, categorías y unidades de medida. |
| **🧪 Dispensación Inteligente** | Algoritmo **FEFO** (First Expire, First Out) automático para asegurar el uso eficiente de lotes. |
| **💊 Transacciones ACID** | Motor de movimientos de inventario (Entradas/Salidas/Atenciones) con integridad total de datos. |
| **👥 Gestión de Pacientes** | Registro clínico y consulta de historial de atenciones integrado en el flujo de dispensación. |
| **🆔 IDs Secuenciales** | Sistema de identificadores legibles y descriptivos por sede (`CAT-001`, `PAC-001-000001`). |
| **🛡️ Seguridad & Usuarios** | Control de acceso basado en roles (RBAC) con identificación dinámica de jefaturas. |
| **📊 Dashboard Dinámico** | Métricas en tiempo real de salud de inventario, stock crítico y actividad diaria. |
| **🏢 Gestión de Sedes** | Control centralizado de múltiples postas médicas y sus responsables. |
| **🖥️ Interfaz Moderna** | Experiencia de usuario optimizada con *AtlantaFX* (Primer Theme) y soporte completo para **UTF-8**. |

---

### 🛠️ Stack Tecnológico

- **Core:** Java 21 (LTS)
- **UI:** JavaFX 21 + **AtlantaFX** (Fluent/Minimalist Design)
- **Iconos:** Ikonli (FontAwesome 5 Packs)
- **Database:** SQL Server (Docker Container)
- **Testing:** JUnit 5 (Pruebas de Integración y Lógica)
- **Build & CI:** Maven + GitHub Actions

---

### 🚀 Guía de Inicio Rápido

#### Requisitos
- [JDK 21+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/download.cgi)
- [Docker & Docker Compose](https://www.docker.com/)

#### Configuración de la Base de Datos (Docker)
Este proyecto utiliza SQL Server en un contenedor Docker para facilitar el desarrollo.

```bash
# 1. Crear archivo .env a partir del template (ya realizado por el asistente)
# cp .env.template .env

# 2. Iniciar el contenedor de la base de datos
docker-compose up -d
```
*Nota: La primera vez, el contenedor ejecutara automaticamente `init.sql` (esquema + datos) para preparar la base de datos. Espere unos 45 segundos antes de ejecutar la aplicacion.*

#### Instalación y Ejecución
```bash
# 1. Compilar el proyecto
./mvnw clean compile

# 2. Ejecutar la aplicación
./mvnw javafx:run
```

#### Ejecución de Pruebas
```bash
./mvnw test
```

---

### 📂 Documentación Técnica
Para una inmersión profunda en el desarrollo, consulta nuestras guías:

*   📘 [**Arquitectura**](./docs/ARCHITECTURE.md): Estructura de paquetes, estrategia de IDs y transaccionalidad.
*   📋 [**Requerimientos Funcionales**](./docs/FUNCTIONAL_REQUIREMENTS.md): Estado actual de implementación de los módulos.
*   🎨 [**Guía de Diseño**](./docs/UI_GUIDE.md): Paleta de colores y componentes UI.
*   🧪 [**Testing y CI**](./docs/TESTING_CI.md): Estrategia de pruebas y automatización.

---
<p align="center">
  Diseñado con 💙 para la mejora de la Salud Pública | 2026
</p>
