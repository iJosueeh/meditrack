# Guía de Estilo de Interfaz (UI)

MediTrack utiliza un sistema de diseño moderno basado en el estilo **Fluent / Minimalista**.

## Paleta de Colores

- **Azul Corporativo:** `#1f4e7a` (Botones primarios, Sidebar, Branding).
- **Azul Éxito:** `#16a34a` (Indicadores de stock saludable).
- **Rojo Peligro:** `#ef4444` (Alertas críticas, Botón salir).
- **Fondo:** `#f8fafc` (Gris ultra-claro para descanso visual).

## Componentes Reutilizables (CSS)

Los estilos se encuentran en `src/main/resources/com/utp/meditrackapp/styles/`:
- `.card-base`: Clase base para todas las tarjetas (bordes de 16px, sombras suaves).
- `.nav-button`: Estilo para botones de navegación con efecto de borde activo.
- `.icon-btn-nav`: Botones de icono transparentes con efecto hover.

## Iconografía (Ikonli)

Usamos la librería **Ikonli** con el pack **FontAwesome 5**.
Para añadir un icono en FXML:
```xml
<FontIcon iconLiteral="fas-nombre-icono" iconSize="20" />
```
*Tip: Puedes buscar nombres de iconos en el sitio oficial de FontAwesome 5.*

## Responsividad

Se utiliza `FlowPane` para los contenedores de tarjetas, lo que permite que se reorganicen automáticamente según el tamaño de la ventana. Siempre usar `wrapText="true"` en etiquetas descriptivas dentro de tarjetas.
