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
