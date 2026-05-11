# 🏥 MediTrack - Sistema de Gestión Hospitalaria e Inventario Médico

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-Build-green?style=for-the-badge&logo=apache-maven)
![Build Status](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-brightgreen?style=for-the-badge&logo=github-actions)

**MediTrack** es una solución integral de escritorio diseñada para optimizar la gestión de pacientes, el control de inventario médico y la administración operativa de sedes clínicas. Con una interfaz moderna basada en **Fluent Design**, MediTrack ofrece una experiencia de usuario fluida, segura y profesional.

---

## ✨ Características Principales

*   **🛡️ Autenticación Segura:** Acceso controlado por roles (Administrador, Médico, Personal).
*   **📊 Dashboard Inteligente:** Visualización en tiempo real de indicadores críticos:
    *   Niveles de stock crítico con alertas visuales.
    *   Monitoreo de lotes próximos a vencer.
    *   Salud general del inventario por sede.
*   **📦 Gestión de Inventario:** Control detallado de productos, lotes y categorías.
*   **👥 Gestión de Pacientes:** Registro clínico y seguimiento administrativo.
*   **📱 Diseño Responsivo:** Interfaz adaptable a diferentes resoluciones de pantalla.
*   **📑 Reportes Avanzados:** Integración futura con JasperReports para análisis de datos.

## 🛠️ Stack Tecnológico

- **Lenguaje:** Java 21 (LTS)
- **Framework UI:** JavaFX 21 con FXML y CSS3.
- **Iconografía:** Ikonli con FontAwesome 5.
- **Pruebas:** JUnit 5 (Jupiter).
- **Gestor de Dependencias:** Maven.
- **CI/CD:** GitHub Actions.

## 🚀 Configuración y Ejecución

### Requisitos Previos
- JDK 21 o superior.
- Maven 3.8+ instalado.

### Clonar y Ejecutar
1. Clona el repositorio:
   ```bash
   git clone https://github.com/tu-usuario/meditrack.git
   cd meditrack
   ```
2. Compila el proyecto:
   ```bash
   ./mvnw clean compile
   ```
3. Ejecuta la aplicación:
   ```bash
   ./mvnw javafx:run
   ```

## 🧪 Pruebas Unitarias
Para asegurar la calidad del código, puedes ejecutar la suite de pruebas con:
```bash
./mvnw test
```

## 📂 Estructura de Documentación
Encuentra guías detalladas para el desarrollo en la carpeta [`/docs`](./docs):
- [Arquitectura](./docs/ARCHITECTURE.md)
- [Guía de Estilo UI](./docs/UI_GUIDE.md)
- [Testing y CI/CD](./docs/TESTING_CI.md)

---
Developed with ❤️ by the MediTrack Team | 2026
