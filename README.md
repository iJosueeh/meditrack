# MediTrack

Sistema de gestión de inventario para postas médicas desarrollado en JavaFX con Maven.

## Descripción del Proyecto

MediTrack es una aplicación diseñada para optimizar el control de suministros médicos, medicamentos y equipamiento en centros de salud de atención primaria (postas médicas). 

### Objetivos Actuales:
- Mantener un inventario actualizado de insumos médicos.
- Facilitar la gestión de entradas y salidas de materiales.
- Proporcionar una interfaz amigable para el personal de salud.

## Requisitos
- **Java 21** o superior.
- **Maven** para la gestión de dependencias.
- **JavaFX SDK** (gestionado por Maven).

## Estructura del Proyecto
- `src/main/java`: Código fuente de la aplicación.
- `src/main/resources`: Archivos FXML, CSS e imágenes.
- `database/`: Archivos locales de base de datos.
- `docs/`: Documentación adicional del proyecto.

## Cómo Ejecutar
Para iniciar la aplicación, ejecuta el siguiente comando en la terminal:
```bash
mvn javafx:run
```

## Próximas Funcionalidades
- Reportes automáticos de stock crítico.
- Gestión de fechas de vencimiento de medicamentos.
- Autenticación de usuarios por roles.
