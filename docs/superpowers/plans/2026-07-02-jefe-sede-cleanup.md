# Jefe de Sede Coverage + Dead Code Cleanup Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix permission gaps for Jefe de Sede (ROL-002) and clean up dead code across the codebase.

**Architecture:** All role restrictions must use the permission-based system (`SessionManager.tienePermiso()`) not hardcoded role checks. Sede scoping uses `SedeAccessValidator` or `user.getSedeId()`. Dead code includes unused role-checking methods in SessionManager, Rol, and Usuario.

**Tech Stack:** Java 21, JavaFX, SQL Server, Maven

## Global Constraints

- Java 21, JavaFX, SQL Server via `meditrack_init.sql` (idempotent)
- DB credentials from `.env` file
- All role restrictions use `SessionManager.tienePermiso(codigoPermiso)`
- FontIcon instances must NOT be shared between TableCell instances
- All Dialog/Alert windows must use `initModality(APPLICATION_MODAL)` + `initOwner` + CSS overlay
- Modals use `.modal-content` CSS class from `global.css`

---

## Task 1: Fix UsuarioController Sede Scoping

**Files:**
- Modify: `C:\Proyectos\meditrack\src\main\java\com\utp\meditrackapp\features\users\ui\UsuarioController.java`
- Modify: `C:\Proyectos\meditrack\src\main\java\com\utp\meditrackapp\domain\ports\out\UsuarioRepository.java`
- Modify: `C:\Proyectos\meditrack\src\main\java\com\utp\meditrackapp\infrastructure\persistence\jdbc\JdbcUsuarioRepository.java`

**Problem:** Jefe de Sede can see and manage users from ALL sedes. The SQL requirement says "gestiona usuarios de su sede" but there's no sede filtering.

**Solution:** When the current user is Jefe de Sede (has `USUARIOS` permission but NOT `M2_SEDES`), filter the user list by the user's own `sede_id`. Also restrict sede assignment to only the user's own sede.

- [ ] **Step 1: Add `findAllBySedeId()` to UsuarioRepository interface**

```java
// In UsuarioRepository.java, add:
List<Usuario> findAllBySedeId(String sedeId);
```

- [ ] **Step 2: Implement `findAllBySedeId()` in JdbcUsuarioRepository**

```java
@Override
public List<Usuario> findAllBySedeId(String sedeId) {
    String sql = "SELECT u.*, s.nombre as sede_nombre, r.nombre as rol_nombre " +
                 "FROM usuarios u " +
                 "LEFT JOIN sedes s ON u.sede_id = s.id " +
                 "LEFT JOIN roles r ON u.rol_id = r.id " +
                 "WHERE u.sede_id = ? " +
                 "ORDER BY u.nombres ASC";
    // ... standard JDBC execution
}
```

- [ ] **Step 3: Update UsuarioController.loadData() to filter by sede**

```java
private void loadData() {
    List<Usuario> users;
    if (sessionManager.tienePermiso("M2_SEDES")) {
        // Admin Global sees all users
        users = userAdapter.listarUsuarios();
    } else {
        // Jefe de Sede sees only their sede's users
        Usuario currentUser = sessionManager.getCurrentUser();
        if (currentUser != null && currentUser.getSedeId() != null) {
            users = userAdapter.listarUsuariosPorSede(currentUser.getSedeId());
        } else {
            users = userAdapter.listarUsuarios();
        }
    }
    usersTable.setItems(FXCollections.observableArrayList(users));
}
```

- [ ] **Step 4: Add `listarUsuariosPorSede()` to UserAdapter**

```java
public List<Usuario> listarUsuariosPorSede(String sedeId) {
    return usuarioRepository.findAllBySedeId(sedeId);
}
```

- [ ] **Step 5: Restrict sede combo in user form for Jefe de Sede**

In `UsuarioController`, when setting up the sede combo, if the user doesn't have `M2_SEDES` permission, only show the user's own sede:

```java
// In setupForm() or loadData(), after setting up sedeCombo:
if (!sessionManager.tienePermiso("M2_SEDES")) {
    Usuario currentUser = sessionManager.getCurrentUser();
    if (currentUser != null && currentUser.getSedeId() != null) {
        // Find and select only the user's own sede
        sedeCombo.getItems().stream()
            .filter(s -> s.getId().equals(currentUser.getSedeId()))
            .findFirst()
            .ifPresent(s -> sedeCombo.getSelectionModel().select(s));
        sedeCombo.setDisable(true); // Lock the combo
    }
}
```

- [ ] **Step 6: Verify behavior**

- Login as Jefe de Sede (ROL-002)
- Navigate to Gestion de Usuarios
- Verify only users from own sede are visible
- Verify sede combo is locked to own sede when creating/editing users
- Login as Admin Global (ROL-001)
- Verify all users are visible
- Verify sede combo shows all sedes

---

## Task 2: Fix Inventory Edit/Anular Permission Gaps

**Files:**
- Modify: `C:\Proyectos\meditrack\src\main\java\com\utp\meditrackapp\features\inventory\ui\InventoryController.java`

**Problem:** Jefe de Sede (ROL-002) can anular and edit SALIDA movements even though they don't have `M6_SALIDAS` permission. The edit dialog also allows changing movement type without permission filtering.

**Solution:** Restrict edit/anular actions based on the movement's type and the user's permissions. Filter the edit dialog's type combo by permissions.

- [ ] **Step 1: Fix action button visibility per movement type**

In `setupTable()` (around line 206), change the action column cell factory to check permissions based on the movement type:

```java
colMovActions.setCellFactory(col -> new TableCell<>() {
    @Override
    protected void updateItem(Movimiento item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        FontIcon iconEdit = new FontIcon("fas-edit");
        FontIcon iconAnular = new FontIcon("fas-ban");
        Button btnEdit = new Button("", iconEdit);
        Button btnAnular = new Button("", iconAnular);
        // ... styling ...
        
        // Check permissions based on movement type
        boolean canModify;
        if (item.isEntrada()) {
            canModify = sessionManager.tienePermiso("M5_ENTRADAS");
        } else {
            canModify = sessionManager.tienePermiso("M6_SALIDAS");
        }
        
        btnEdit.setVisible(canModify);
        btnEdit.setManaged(canModify);
        btnAnular.setVisible(canModify);
        btnAnular.setManaged(canModify);
        
        // ... rest of click handlers
    }
});
```

- [ ] **Step 2: Filter edit dialog type combo by permissions**

In `abrirModalEdicion()` (around line 324), filter the type combo:

```java
ComboBox<TipoMovimiento> cmbTipo = new ComboBox<>();
List<TipoMovimiento> tiposPermitidos = inventoryAdapter.listarTiposMovimiento().stream()
    .filter(t -> {
        if (t.getId().equals(TipoMovimientoEnum.ENTRADA.getId())) {
            return sessionManager.tienePermiso("M5_ENTRADAS");
        }
        if (t.getId().equals(TipoMovimientoEnum.SALIDA.getId())) {
            return sessionManager.tienePermiso("M6_SALIDAS");
        }
        return true;
    })
    .collect(Collectors.toList());
cmbTipo.getItems().addAll(tiposPermitidos);
```

- [ ] **Step 3: Verify behavior**

- Login as Jefe de Sede (ROL-002)
- Navigate to Inventario
- Verify SALIDA movement edit/anular buttons are hidden
- Verify ENTRADA movement edit/anular buttons are visible
- Verify edit dialog only shows ENTRADA type
- Login as Tecnico de Farmacia (ROL-003)
- Verify SALIDA movement edit/anular buttons are visible
- Verify ENTRADA movement edit/anular buttons are hidden
- Verify edit dialog only shows SALIDA type
- Login as Admin Global (ROL-001)
- Verify all movement types are editable

---

## Task 3: Clean Up Dead Code in SessionManager

**Files:**
- Modify: `C:\Proyectos\meditrack\src\main\java\com\utp\meditrackapp\core\config\SessionManager.java`

**Problem:** `isTecnico()`, `isQuimico()`, and `isAdmin()` methods are marked for removal and have zero callers. They're part of the old hardcoded role-checking system that's been replaced by `tienePermiso()`.

**Solution:** Remove the dead methods and the compatibility comment block.

- [ ] **Step 1: Remove dead methods from SessionManager**

Delete the following block (lines 145-158):

```java
// === Métodos de compatibilidad (serán removidos gradualmente) ===

public boolean isTecnico() {
    return currentUser != null && currentUser.isTecnico();
}

public boolean isQuimico() {
    return isLoggedIn() && "ROL-002".equals(currentUser.getRolId());
}

public boolean isAdmin() {
    return currentUser != null && currentUser.isAdmin();
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvnw.cmd compile -f C:\Proyectos\meditrack\pom.xml`
Expected: BUILD SUCCESS with no errors referencing the removed methods

- [ ] **Step 3: Verify no broken references**

Search the codebase for any remaining references to `isTecnico()`, `isQuimico()`, or `isAdmin()` on SessionManager:
- `grep -r "SessionManager.*isTecnico\|SessionManager.*isQuimico\|SessionManager.*isAdmin" src/`
- Expected: No matches

---

## Task 4: Clean Up Dead Code in Rol Entity

**Files:**
- Modify: `C:\Proyectos\meditrack\src\main\java\com\utp\meditrackapp\domain\entities\Rol.java`

**Problem:** `isAdmin()`, `isJefeSede()`, and `isTecnico()` methods on Rol are never called. Rol objects are always checked via `tienePermiso()`.

**Solution:** Remove the dead methods and the compatibility comment block.

- [ ] **Step 1: Remove dead methods from Rol**

Delete the following block (around line 77):

```java
// Métodos de compatibilidad con código existente (serán removidos gradualmente)

public boolean isAdmin() {
    return id != null && id.equals("ROL-001");
}

public boolean isJefeSede() {
    return nombre != null && nombre.toUpperCase().contains("JEFE");
}

public boolean isTecnico() {
    return id != null && id.equals("ROL-003");
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvnw.cmd compile -f C:\Proyectos\meditrack\pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 3: Verify no broken references**

Search for any remaining references to these methods on Rol objects:
- `grep -r "\.isAdmin()\|\.isJefeSede()\|\.isTecnico()" src/`
- Expected: Only matches on Usuario entity (which has its own methods)

---

## Task 5: Clean Up Dead Code in Usuario Entity

**Files:**
- Modify: `C:\Proyectos\meditrack\src\main\java\com\utp\meditrackapp\domain\entities\Usuario.java`

**Problem:** `isTecnico()` on Usuario is never called externally (only by the dead `SessionManager.isTecnico()`). `isAdmin()` and `isJefeSede()` are actively used in `GestionarSedeUseCase.java` and `SedeAccessValidator.java`, so they must stay.

**Solution:** Remove only `isTecnico()` from Usuario. Keep `isAdmin()` and `isJefeSede()`.

- [ ] **Step 1: Remove `isTecnico()` from Usuario**

Find and delete the method:

```java
public boolean isTecnico() {
    return rolId != null && rolId.equals("ROL-003");
}
```

- [ ] **Step 2: Verify compilation**

Run: `mvnw.cmd compile -f C:\Proyectos\meditrack\pom.xml`
Expected: BUILD SUCCESS

- [ ] **Step 3: Verify no broken references**

- `grep -r "\.isTecnico()" src/`
- Expected: No matches (the only caller was `SessionManager.isTecnico()` which was removed in Task 3)

---

## Task 6: Commit All Changes

- [ ] **Step 1: Stage all modified files**

```bash
git add src/main/java/com/utp/meditrackapp/features/users/ui/UsuarioController.java
git add src/main/java/com/utp/meditrackapp/domain/ports/out/UsuarioRepository.java
git add src/main/java/com/utp/meditrackapp/infrastructure/persistence/jdbc/JdbcUsuarioRepository.java
git add src/main/java/com/utp/meditrackapp/features/inventory/ui/InventoryController.java
git add src/main/java/com/utp/meditrackapp/core/config/SessionManager.java
git add src/main/java/com/utp/meditrackapp/domain/entities/Rol.java
git add src/main/java/com/utp/meditrackapp/domain/entities/Usuario.java
```

- [ ] **Step 2: Create commit**

```bash
git commit -m "fix: Jefe de Sede sede scoping + dead code cleanup

- UsuarioController: filter users by sede for Jefe de Sede
- InventoryController: restrict edit/anular by movement type permissions
- SessionManager: remove dead isTecnico/isQuimico/isAdmin methods
- Rol: remove dead isAdmin/isJefeSede/isTecnico methods
- Usuario: remove dead isTecnico method"
```
