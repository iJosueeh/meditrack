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
