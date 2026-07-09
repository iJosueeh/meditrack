# MediTrack - Diagrama de Clases UML

Diagrama de clases generado con [PlantUML](https://plantuml.com/) que representa la arquitectura hexagonal del sistema.

## Cómo visualizarlo

1. Copiar el bloque PlantUML en https://www.plantuml.com/plantuml/uml/
2. O instalar la extensión PlantUML en VS Code y previsualizar con `Alt+D`
3. O usar `plantuml -tpng docs/class-diagram.puml`

---

## Código PlantUML

```
@startuml
' ============================================================
' MediTrack - Diagrama de Clases (Arquitectura Hexagonal)
' ============================================================

skinparam defaultFontName Helvetica
skinparam roundcorner 8
skinparam shadowing false
skinparam linetype ortho
skinparam packageStyle rectangle

' ============================================================
' COLORES POR CAPA
' ============================================================
!define ENTITY_COLOR #E8F5E9/white
!define PORT_COLOR #E3F2FD/white
!define USECASE_COLOR #FFF3E0/white
!define ADAPTER_COLOR #F3E5F5/white
!define CONTROLLER_COLOR #FCE4EC/white
!define CORE_COLOR #F5F5F5/white
!define DTO_COLOR #E0F7FA/white

' ============================================================
' PACKAGE: DOMAIN ENTITIES
' ============================================================
package "domain.entities" #ENTITY_COLOR {
  class Sede {
    + id: String
    + nombre: String
    + direccion: String
    + telefono: String
    + ubigeo: String
    + tipoSede: String
    + administradorId: String
    + capacidadAlmacen: int
    + isActiva: int
    + isBloqueada: int
    + motivoBloqueo: String
    + fechaBloqueo: LocalDateTime
    ..
    + isOperativa(): boolean
    + bloquear(motivo): void
    + desbloquear(): void
    + validate(): String
  }

  class Usuario {
    + id: String
    + sedeId: String
    + rolId: String
    + tipoDocumento: String
    + numeroDocumento: String
    + nombres: String
    + apellidos: String
    + password: String
    + isActivo: int
    ..
    + getNombreCompleto(): String
    + validate(): String
  }

  class Rol {
    + id: String
    + nombre: String
    + descripcion: String
    + nivel: int
    + isSistema: int
    + isActivo: int
    ..
    + tienePermiso(codigo): boolean
    + puedeGestionarRol(otro): boolean
    + esSistema(): boolean
  }

  class Permiso {
    + id: String
    + codigo: String
    + nombre: String
    + descripcion: String
    + modulo: String
    + orden: int
    + isActivo: int
  }

  class Paciente {
    + id: String
    + tipoDocumento: String
    + numeroDocumento: String
    + nombres: String
    + apellidos: String
    + telefono: String
    + isActivo: int
  }

  class Producto {
    + id: String
    + categoriaId: String
    + codigoDigemid: String
    + nombre: String
    + detalle: String
    + unidadMedida: String
    + stockMinimo: int
    + precioUnitario: BigDecimal
    + isActivo: int
  }

  class Categoria {
    + id: String
    + nombre: String
    + isActivo: int
  }

  class Lote {
    + id: String
    + productoId: String
    + sedeId: String
    + numeroLote: String
    + fechaFabricacion: LocalDate
    + fechaVencimiento: LocalDate
    + cantidad: int
    ..
    + isVencido(): boolean
    + diasParaVencer(): long
    + stockDisponible(): int
  }

  class Movimiento {
    + id: String
    + tipoId: String
    + motivoId: String
    + sedeId: String
    + usuarioId: String
    + loteId: String
    + cantidad: int
    + observacion: String
    + fechaRegistro: LocalDateTime
  }

  class Atencion {
    + id: String
    + sedeId: String
    + pacienteId: String
    + usuarioId: String
    + numeroReceta: String
    + medico: String
    + fechaAtencion: LocalDateTime
  }

  class AtencionDetalle {
    + id: String
    + atencionId: String
    + loteId: String
    + cantidadEntregada: int
  }

  class TipoMovimiento {
    + id: String
    + nombre: String
    + isActivo: int
  }

  class MotivoMovimiento {
    + id: String
    + nombre: String
    + isActivo: int
  }
}

' ============================================================
' RELACIONES ENTRE ENTIDADES
' ============================================================
Rol "1" *--> "permisos" Permiso
Usuario "n" ---> "1" Rol
Usuario "n" ---> "1" Sede
Sede "1" ---> "1" Usuario : administrador
Producto "n" ---> "1" Categoria
Lote "n" ---> "1" Producto
Lote "n" ---> "1" Sede
Movimiento "n" ---> "1" TipoMovimiento
Movimiento "n" ---> "1" MotivoMovimiento
Movimiento "n" ---> "1" Sede
Movimiento "n" ---> "1" Usuario
Movimiento "n" ---> "1" Lote
Atencion "n" ---> "1" Sede
Atencion "n" ---> "1" Paciente
Atencion "n" ---> "1" Usuario
AtencionDetalle "n" ---> "1" Atencion
AtencionDetalle "n" ---> "1" Lote

' ============================================================
' PACKAGE: APPLICATION DTO
' ============================================================
package "application.dto" #DTO_COLOR {
  class DashboardKpiDTO {
    + totalProductos: int
    + stockCritico: int
    + lotesPorVencer: int
    + saludInventario: int
    + valorInventario: double
    + volumenMovimientos: int
  }

  class ReporteConsolidadoDTO {
    + valorInventario: String
    + crecimientoMesAnterior: String
    + alertasStock: String
    + eficiencia: String
    + items: List
  }

  class DispensacionReportDTO {
    + fecha: LocalDateTime
    + paciente: String
    + producto: String
    + lote: String
    + cantidad: int
    + receta: String
  }

  class MovimientoDTO {
    + fecha: LocalDateTime
    + tipo: String
    + producto: String
    + lote: String
    + cantidad: int
    + motivo: String
  }
}

' ============================================================
' PACKAGE: DOMAIN PORTS (INTERFACES)
' ============================================================
package "domain.ports.out" #PORT_COLOR {
  interface SedeRepository {
    + findAll(): List<Sede>
    + findById(id): Optional<Sede>
    + save(sede): String
    + update(sede): void
    + delete(id): void
    + toggleEstado(id): void
    + countTotalEmployees(): int
    + assignUserToSede(...): boolean
    + countUsuariosBySede(id): int
    + countLotesBySede(id): int
    + countMovimientosBySede(id): int
  }

  interface UsuarioRepository {
    + findAll(): List<Usuario>
    + findBySede(sedeId): List<Usuario>
    + findById(id): Optional<Usuario>
    + save(usuario): String
    + update(usuario): void
    + delete(id): void
    + toggleEstado(id): void
    + countMovimientosByUsuario(id): int
    + countAtencionesByUsuario(id): int
  }

  interface LoteRepository {
    + findBySedeProducto(sedeId, productoId): List<Lote>
    + save(lote): String
    + updateCantidad(id, cantidad): void
  }

  interface MovimientoRepository {
    + save(movimiento): String
    + findWithFilters(...): List<Movimiento>
    + anular(id): void
    + editar(movimiento): void
  }

  interface PacienteRepository {
    + findAll(): List<Paciente>
    + findBySede(sedeId): List<Paciente>
    + findById(id): Optional<Paciente>
    + save(paciente): String
    + softDelete(id): boolean
    + hardDelete(id): boolean
    + reactivar(id): boolean
  }

  interface AtencionRepository {
    + save(atencion): String
    + findByPaciente(id): List<Atencion>
    + findByReceta(receta): List<Atencion>
    + saveDetalle(detalle): void
  }

  interface DashboardRepository {
    + getStockCriticoCount(umbral): int
    + getLotesPorVencerCount(dias): int
    + getSaludInventario(sedeId): int
    + getValorInventario(): double
    + getVolumenMovimientos(dias): int
    + getTendenciaInventario(meses): List
    + getDistribucionPorCategoria(): List
  }

  interface CategoriaRepository {
    + findAll(): List<Categoria>
    + delete(id): void
    + toggleEstado(id): void
  }

  interface ProductoRepository {
    + findAll(): List<Producto>
    + save(producto): String
    + update(producto): void
    + delete(id): void
  }
}

' ============================================================
' PACKAGE: DOMAIN SERVICES (USE CASES)
' ============================================================
package "domain.services" #USECASE_COLOR {
  class GestionarSedeUseCase {
    + listarSedes(): List<Sede>
    + guardarSede(sede): String
    + toggleEstado(id): String
    + eliminarSede(id): String
    + asignarUsuarioASede(...): String
  }

  class GestionarUsuarioUseCase {
    + listarUsuarios(): List<Usuario>
    + guardarUsuario(usuario, password): String
    + toggleEstado(id): String
    + eliminarUsuario(id): String
    + actualizarPassword(id, hash): void
  }

  class GestionarPacienteUseCase {
    + listarPacientes(): List<Paciente>
    + guardarPaciente(paciente): String
    + eliminarPaciente(id): String
    + desactivarPaciente(id): String
    + reactivarPaciente(id): String
  }

  class ObtenerKpisUseCase {
    + obtenerKpis(sedeId): DashboardKpiDTO
  }

  class GenerarReporteConsolidadoUseCase {
    + generar(sedeId, usuarioName, sedeName, outputFile): void
  }

  class AutenticarUsuarioUseCase {
    + autenticar(dni, password): Usuario
  }
}

' ============================================================
' DEPENDENCIAS USE CASE -> REPOSITORY
' ============================================================
GestionarSedeUseCase .l.> SedeRepository
GestionarSedeUseCase .l.> UsuarioRepository
GestionarUsuarioUseCase .l.> UsuarioRepository
GestionarPacienteUseCase .l.> PacienteRepository
GestionarPacienteUseCase .l.> AtencionRepository
ObtenerKpisUseCase .l.> DashboardRepository
GenerarReporteConsolidadoUseCase .l.> DashboardRepository
GenerarReporteConsolidadoUseCase .l.> ReportService
AutenticarUsuarioUseCase .l.> UsuarioRepository

' ============================================================
' PACKAGE: INFRASTRUCTURE ADAPTERS
' ============================================================
package "infrastructure.adapters" #ADAPTER_COLOR {
  class SedeAdapter {
    + listarSedes(): List<Sede>
    + guardarSede(sede): void
    + toggleEstado(id): String
    + eliminarSede(id): String
    + bloquear(id, motivo): void
    + desbloquear(id): void
  }

  class UserAdapter {
    + listarUsuarios(): List<Usuario>
    + guardarUsuario(usuario, password): String
    + toggleEstado(id): String
    + eliminarUsuario(id): String
    + resetearPassword(id): void
  }

  class PacienteAdapter {
    + listarPacientes(): List<Paciente>
    + guardarPaciente(paciente): String
    + eliminarPaciente(id): String
    + desactivarPaciente(id): String
    + reactivarPaciente(id): String
  }

  class ReportAdapter {
    + obtenerKpis(sedeId): DashboardKpiDTO
    + generarReporteConsolidado(...): boolean
    + generarReporteMovimientos(...): boolean
    + generarReporteDispensaciones(...): boolean
  }

  class CatalogAdapter {
    + listarRoles(): List<Rol>
    + guardarRol(rol, permisos): String
    + eliminarRol(id): String
    + listarCategorias(): List<Categoria>
    + eliminarCategoria(id): String
  }

  class AuthAdapter {
    + autenticar(dni, password): String
  }

  class InventoryAdapter {
    + registrarMovimiento(...): String
    + anularMovimiento(mov): void
    + listarMovimientosConFiltros(...): List
  }

  class AtencionAdapter {
    + registrarAtencion(...): String
    + listarDispensacionesReporte(...): DispensacionReportDTO[]
  }

  class ProductoAdapter {
    + listarProductos(): List<Producto>
    + guardarProducto(p): String
    + desactivarProducto(id): void
    + obtenerStockTotal(sedeId, productoId): int
  }
}

' ============================================================
' DEPENDENCIAS ADAPTER -> USE CASE
' ============================================================
SedeAdapter .d.> GestionarSedeUseCase : Crea instancias
UserAdapter .d.> GestionarUsuarioUseCase
PacienteAdapter .d.> GestionarPacienteUseCase
ReportAdapter .d.> ObtenerKpisUseCase
ReportAdapter .d.> GenerarReporteConsolidadoUseCase
CatalogAdapter .d.> GestionarCatalogoUseCase
AuthAdapter .d.> AutenticarUsuarioUseCase

' ============================================================
' PACKAGE: FEATURES (CONTROLLERS)
' ============================================================
package "features.*.ui" #CONTROLLER_COLOR {
  class LoginController {
    - dniField: TextField
    - passwordField: PasswordField
    ..
    + onLogin(): void
    + onForgotPassword(): void
  }

  class DashboardController {
    + initialize(): void
    + onGoToReports(): void
    + onGoToInventory(): void
    + onLogout(): void
  }

  class ReportsController {
    - dpMovFrom, dpMovTo: DatePicker
    - lblTotalProducts: Label
    - lblInventoryValue: Label
    ..
    + onGenerateConsolidated(): void
    + onGenerateMovements(): void
    + onGenerateDispensations(): void
  }

  class UsuarioController {
    - usersTable: TableView<Usuario>
    - formOverlay: StackPane
    ..
    + onSearch(): void
    + onNewUser(): void
    + onSaveUser(): void
    + handleDeleteUser(u): void
    + handleToggleStatus(u): void
  }

  class SedeController {
    - tableSedes: TableView<Sede>
    - pagination: Pagination
    - modalSede: StackPane
    - cmbManager: ComboBox<Usuario>
    ..
    + onOpenRegisterModal(): void
    + onSaveSede(): void
    + handleDeleteSede(s): void
    + confirmToggle(s): void
    + confirmBlock(s): void
  }

  class PacienteController {
    - patientsTable: TableView<Paciente>
    - searchField: TextField
    - formOverlay: StackPane
    ..
    + onSearch(): void
    + onNewPatient(): void
    + onSavePatient(): void
    + handleDelete(p): void
    + handleToggleBlock(p): void
  }

  class InventoryController {
    - inventoryTabPane: TabPane
    - tableMovements: TableView<Movimiento>
    - tableBatches: TableView<Lote>
    - modalMovement: StackPane
    - cmbMovementType: ComboBox
    ..
    + onFilterMovements(): void
    + onOpenMovementModal(): void
    + onSaveMovement(): void
    + onQuickUpdate(): void
    + onGenerateReport(): void
  }

  class AtencionController {
    - cmbPaciente: ComboBox<Paciente>
    - cmbProducto: ComboBox<Producto>
    - txtReceta: TextField
    - tableBasket: TableView<AtencionDetalle>
    ..
    + onPacienteSelected(): void
    + onSearchByReceta(): void
    + onAddToBasket(): void
    + onConfirmarEntrega(): void
    + onGenerateReport(): void
  }

  class ProductoController {
    - tableProductos: TableView<Producto>
    - modalProducto: StackPane
    - cmbCategoria: ComboBox<Categoria>
    ..
    + onOpenRegisterModal(): void
    + onSave(): void
    + confirmDelete(p): void
  }

  class CategoriaController {
    - tableCategorias: TableView<Categoria>
    - modalCategoria: StackPane
    - txtNombre: TextField
    ..
    + onOpenRegisterModal(): void
    + onSave(): void
    + confirmDelete(cat): void
  }

  class RolController {
    - tableRoles: TableView<Rol>
    - modalRol: StackPane
    - chkPermisos: 13 checkboxes
    - cmbNivel: ComboBox
    ..
    + onOpenRegisterModal(): void
    + onSave(): void
    + confirmDelete(rol): void
  }

  class ProfileController {
    - profileRootPane: BorderPane
    - editModal: VBox
    - passwordModal: VBox
    ..
    + onSaveChanges(): void
    + onChangePassword(): void
    + onUpdatePassword(): void
  }
}

' ============================================================
' DEPENDENCIAS CONTROLLER -> ADAPTER
' ============================================================
LoginController .d.> AuthAdapter
DashboardController .d.> NavigationService
ReportsController .d.> ReportAdapter
UsuarioController .d.> UserAdapter
SedeController .d.> SedeAdapter
PacienteController .d.> PacienteAdapter
InventoryController .d.> InventoryAdapter
AtencionController .d.> AtencionAdapter
ProductoController .d.> ProductoAdapter
CategoriaController .d.> CatalogAdapter
RolController .d.> CatalogAdapter
ProfileController .d.> ProfileAdapter

' ============================================================
' PACKAGE: CORE (CROSS-CUTTING)
' ============================================================
package "core" #CORE_COLOR {
  class SessionManager {
    - currentUser: Usuario
    - currentRol: Rol
    {static} - instance: SessionManager
    ..
    {static} + getInstance(): SessionManager
    + login(usuario): void
    + logout(): void
    + getCurrentUser(): Usuario
    + tienePermiso(codigo): boolean
    + getSedeId(): String
  }

  class NavigationService {
    {static} + toDashboard(): void
    {static} + toSedes(): void
    {static} + toUsuarios(): void
    {static} + toPacientes(): void
    {static} + toAtenciones(): void
    {static} + toInventory(): void
    {static} + toReportes(): void
    {static} + toProfile(): void
  }

  class DatabaseConfig {
    + getConnection(): Connection
  }

  class IdGenerator {
    {static} + generateId(prefix): String
    {static} + generateId(conn, table, prefix, padding): String
    {static} + generateSedeDependentId(...): String
  }

  class PasswordHasher {
    {static} + hash(password): String
    {static} + verify(password, hash): boolean
  }

  class PdfTemplateEngine {
    + generarPdf(template, vars, output): void
  }
}

' ============================================================
' DEPENDENCIAS CORE -> CONTROLLER
' ============================================================
NavigationService .r.> SessionManager

LoginController ..> SessionManager
DashboardController ..> SessionManager
ReportsController ..> SessionManager
UsuarioController ..> SessionManager
SedeController ..> SessionManager
PacienteController ..> SessionManager
InventoryController ..> SessionManager
AtencionController ..> SessionManager
ProductoController ..> SessionManager
CategoriaController ..> SessionManager
RolController ..> SessionManager
ProfileController ..> SessionManager

' ============================================================
' NOTE: RESUMEN CAPAS
' ============================================================
note "ARQUITECTURA HEXAGONAL (Puertos y Adaptadores)\n\n
  features (Controllers)\n
    | dependen de\n
    v\n
  infrastructure.adapters (Adapters)\n
    | delegan en\n
    v\n
  domain.services (Use Cases)\n
    | implementan\n
    v\n
  domain.ports.out (Repository Interfaces)\n
    ^\n
    |  implementadas por\n
    |\n
  infrastructure.persistence.jdbc (Impl)" as arch_note

@enduml
```

---

## Resumen de Capas

| Capa | Paquetes | Rol |
|---|---|---|
| **Domain (Núcleo)** | `entities/`, `ports.out/`, `services/` | Lógica de negocio, modelos, contratos de repositorios |
| **Application** | `dto/` | Objetos de transferencia de datos entre capas |
| **Infrastructure** | `adapters/`, `persistence/jdbc/` | Implementación de repositorios, adaptadores para UI |
| **Features** | `features/*/ui/` | Controladores JavaFX con lógica de UI |
| **Core** | `config/`, `util/`, `models/enums/` | Configuración transversal (DB, sesión, PDF) |

### Flujo de datos

```
Controller → Adapter → UseCase → Repository (interfaz)
                                   ↑ implementa
                             JdbcRepository (infrastructure)
```
