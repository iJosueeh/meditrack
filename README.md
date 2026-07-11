<h1 align="center">🏥 MediTrack</h1>
<p align="center">
  <em>Sistema de Gestión de Inventario para Postas de Salud</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk" alt="Java">
  <img src="https://img.shields.io/badge/JavaFX-21-blue?style=flat-square" alt="JavaFX">
  <img src="https://img.shields.io/badge/AtlantaFX-Primer-6C63FF?style=flat-square" alt="AtlantaFX">
  <img src="https://img.shields.io/badge/SQL%20Server-CC2927?style=flat-square&logo=microsoft-sql-server&logoColor=white" alt="SQL Server">
  <img src="https://img.shields.io/badge/Maven-3.9-C71A36?style=flat-square&logo=apache-maven&logoColor=white" alt="Maven">
</p>

---

## Descripción

MediTrack administra recursos médicos en postas de salud. Garantiza disponibilidad de suministros, trazabilidad de lotes y toma de decisiones mediante una interfaz de escritorio moderna.

## Stack

- **Core:** Java 21 (Eclipse Adoptium)
- **UI:** JavaFX 21 + AtlantaFX (Primer Theme)
- **Iconos:** Ikonli (FontAwesome 5)
- **Base de datos:** SQL Server (Docker)
- **Build:** Maven + GitHub Actions

## Inicio Rápido

**Requisitos:** JDK 21+, Maven 3.8+, Docker

```bash
# 1. Levantar SQL Server
docker-compose up -d

# 2. Compilar y ejecutar
./mvnw clean compile
./mvnw javafx:run
```

> En Windows usar `mvnw.cmd`. El primer inicio tarda ~45s en inicializar la BD.

## Estructura

```
src/main/java/.../meditrackapp/
├── application/        # Casos de uso
├── core/               # Config y utilidades
├── domain/             # Entidades y lógica de negocio
├── features/           # Módulos (auth, inventory, patients, users...)
├── infrastructure/     # Persistencia JDBC
└── shared/             # Sidebar, navbar, componentes comunes
```

## Documentación

- [Arquitectura](./docs/ARCHITECTURE.md)
- [Requerimientos Funcionales](./docs/FUNCTIONAL_REQUIREMENTS.md)
- [Guía de Diseño UI](./docs/UI_GUIDE.md)
- [Testing y CI](./docs/TESTING_CI.md)

---

<p align="center"><sub>© 2026 MediTrack — Diseñado con 💙 para la Salud Pública</sub></p>
