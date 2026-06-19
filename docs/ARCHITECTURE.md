# Arquitectura del Proyecto MediTrack

Este documento describe la estructura y organización del código para el equipo de desarrollo.

## Estructura de Paquetes (Feature-based)

El proyecto sigue una arquitectura orientada a características (features) para facilitar la escalabilidad:

- `com.utp.meditrackapp.core`: Contiene la lógica transversal y configuración global.
  - `config`: Servicios de navegación y configuración de base de datos.
  - `models`: Entidades y Enums globales.
  - `util`: Clases de utilidad (ej. generadores de IDs).
- `com.utp.meditrackapp.features`: Contiene los módulos específicos de la aplicación.
  - `auth`: Todo lo relacionado con el inicio de sesión.
  - `dashboard`: Panel de control principal y métricas de gestión.
  - *Futuras features*: `patients`, `inventory`, etc.

## Navegación

La navegación entre pantallas se gestiona centralizadamente mediante `NavigationService.java`. 
- **Regla**: Nunca instanciar Scenes manualmente en los controladores; llamar siempre a los métodos estáticos de `NavigationService`.

## Configuración de Módulos (Java 9+)

El archivo `module-info.java` es crítico. Cada vez que se cree un nuevo paquete de UI, debe "abrirse" a FXML:
```java
opens com.utp.meditrackapp.features.nueva_feature.ui to javafx.fxml;
```

## Estrategia de Identificadores (IDs)

MediTrack utiliza un sistema de **IDs Secuenciales Descriptivos** para mejorar la legibilidad y trazabilidad:
- **Catálogos Globales**: Formato `[PREFIJO]-[SECUENCIA]` (ej. `CAT-001`, `PRD-00001`).
- **Entidades por Sede**: Formato `[PREFIJO]-[COD_SEDE]-[SECUENCIA]` (ej. `USR-001-0001`, `PAC-001-000001`).
- **Lógica**: Se utiliza la estrategia `SELECT MAX(id)` sobre la conexión activa para garantizar el orden correlativo sin depender de campos Identity.

## Identificación Dinámica de Roles (Semantic Matching)

Para evitar dependencias rígidas de IDs que podrían ser eliminados por error, la identificación de cargos jerárquicos (como Jefes de Sede) se realiza mediante **mapeo semántico**:
- El sistema busca roles cuyo nombre contenga patrones clave como **'ADMIN'** o **'JEFE'**.
- Esto permite que el sistema sea resiliente a cambios en los IDs de la base de datos siempre que se mantenga la nomenclatura descriptiva en la tabla `roles`.

## Transaccionalidad (ACID)

Todas las operaciones que involucren cambios en el stock (entradas, salidas, dispensación) deben ejecutarse dentro de una transacción JDBC manual:
1. `conn.setAutoCommit(false)`
2. Ejecución de operaciones.
3. `conn.commit()`
4. `conn.rollback()` en caso de excepción.

> **Nota**: Ahora se centraliza mediante `TransactionManager` para evitar duplicación de código.

---

## Arquitectura Hexagonal (Clean Architecture)

### Estructura Actual (Feature-based)

```
com.utp.meditrackapp/
├── core/          # Configuración global, DAOs, entidades anémicas
├── features/      # Módulos por funcionalidad (auth, inventory, patients, etc.)
└── tools/         # Utilidades de testing
```

### Nueva Estructura (Hexagonal)

```
com.utp.meditrackapp/
├── domain/                          # CORE - Sin dependencias externas
│   ├── entities/                    # Entidades ricas con comportamiento
│   │   ├── Lote.java               # + isVencido(), diasParaVencer()
│   │   ├── Paciente.java           # + tieneHistorial()
│   │   └── Atencion.java
│   ├── services/                    # Casos de uso
│   │   ├── inventario/
│   │   │   └── RegistrarMovimientoUseCase.java
│   │   ├── dispensacion/
│   │   │   └── DispensarMedicamentoUseCase.java
│   │   └── paciente/
│   │       └── GestionarPacienteUseCase.java
│   └── ports/                       # Interfaces (contratos)
│       ├── out/                     # Salida hacia infraestructura
│       │   ├── LoteRepository.java
│       │   ├── MovimientoRepository.java
│       │   └── PacienteRepository.java
│       └── in/                      # Entrada desde UI
│           └── InventarioPort.java
│
├── application/                     # Orquestación
│   ├── config/
│   │   ├── DependencyInjection.java # Wire-up de dependencias
│   │   └── TransactionManager.java  # Gestión centralizada de transacciones
│   └── dto/                         # DTOs para UI
│       ├── StockCriticoDTO.java
│       └── DispensacionReportDTO.java
│
├── infrastructure/                  # Implementaciones concretas
│   ├── persistence/                 # Adaptadores de salida
│   │   └── jdbc/
│   │       ├── JdbcLoteRepository.java
│   │       ├── JdbcMovimientoRepository.java
│   │       └── JdbcPacienteRepository.java
│   ├── ui/                          # Adaptadores de entrada
│   │   └── javafx/
│   └── external/                    # Servicios externos
│       └── PdfReportService.java
│
└── shared/                          # Utilidades compartidas
    ├── util/
    └── config/
```

### Principios Clave

1. **Dependencia hacia adentro**: `domain` no depende de `infrastructure`
2. **Puertos como contratos**: Las interfaces definen qué se necesita, no cómo se implementa
3. **Entidades ricas**: Las entidades contienen comportamiento de negocio, no solo datos
4. **TransactionManager centralizado**: Evita duplicación de lógica ACID en cada service

### Migración Incremental

| Fase | Módulos | Estado |
|:---|:---|:---|
| 1. Foundation | Estructura, TransactionManager, Ports, Entidades | ✅ Completado |
| 2. Inventory | LoteRepository, MovimientoRepository, UseCases | 🔄 Pendiente |
| 3. Patients | PacienteRepository, UseCases | 🔄 Pendiente |
| 4. Auth & Sedes | Autenticación, Sedes | 🔄 Pendiente |
| 5. Reports | Servicios de reportes | 🔄 Pendiente |
