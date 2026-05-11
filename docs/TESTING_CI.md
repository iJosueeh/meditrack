# Pruebas y CI/CD

Información sobre el ciclo de vida de desarrollo y calidad de software.

## Tests Unitarios

Usamos **JUnit 5** para las pruebas de lógica de negocio.
- **Ubicación:** `src/test/java/`.
- **Ejecución local:** 
  ```bash
  ./mvnw test
  ```
- **Convención:** Los nombres de los métodos deben ser descriptivos (ej. `shouldGenerateUniqueIds`).

## Integración Continua (CI)

Hemos configurado **GitHub Actions** (`.github/workflows/maven.yml`). 
Cada vez que se sube código a `main` o `dev`, el servidor:
1. Verifica que el código compile.
2. Ejecuta todos los tests unitarios.
3. El commit solo se considera "válido" si el build es exitoso.

## Flujo de Trabajo (Git)

1. Crear rama desde `dev`.
2. Implementar cambios y **añadir tests**.
3. Ejecutar `./mvnw clean verify` localmente.
4. Crear Pull Request hacia `dev`.
