# Stock Adjustment on Attention Edit — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** When a user edits medication details (Lote, Cantidad) in an existing attention, properly adjust lot stock and create audit trail movements.

**Architecture:** Create a new `EditarAtencionUseCase` that follows the existing reversal pattern (see `EditarMovimientoUseCase`). The approach: reverse old stock effects → apply new stock effects → update detail records → create movement records. Also fix `eliminarAtencion()` to properly reverse stock.

**Tech Stack:** Java, JDBC, TransactionManager, existing repository infrastructure

## Current State Analysis

**The Problem:**
- `updateDetalle()` in `JdbcAtencionRepository` only updates the `atencion_detalles` row — NO stock adjustment
- `deleteAtencion()` in `JdbcAtencionRepository` only deletes records — NO stock restoration
- Movements created during attention creation are NEVER cleaned up

**Data Flow (Current — Broken):**
```
Edit modal → onSaveEditar() → atencionAdapter.editarDetalle() → UPDATE atencion_detalles (no stock change)
Delete → onEliminarAtencion() → atencionAdapter.eliminarAtencion() → DELETE records (no stock change)
```

**Data Flow (Target — Fixed):**
```
Edit modal → onSaveEditar() → atencionAdapter.editarAtencionCompleta() → EditarAtencionUseCase
  → transaction:
    1. Load original details
    2. For each changed detail: reverse old stock, apply new stock
    3. Update detail records
    4. Create reversal + new exit movement records
    5. Update parent atencion (receta, medico)
```

## Global Constraints

- Follow existing hexagonal architecture patterns
- Use `TransactionManager.execute()` for all multi-step DB operations
- Use existing `loteRepository.aumentarStock()` / `reducirStock()` methods
- Movement records must use `TipoMovimientoEnum.SALIDA` + `MotivoMovimientoEnum.ATENCION`
- Stock validation: check `cantidad >= requested` before reducing (existing `reducirStock` does this)
- All changes must compile with `./mvnw compile -q`

---

## File Map

| File | Action | Purpose |
|------|--------|---------|
| `EditarAtencionUseCase.java` | **Create** | New use case for editing attention with stock adjustment |
| `AtencionAdapter.java` | Modify | Add `editarAtencionCompleta()` method that calls the new use case |
| `AtencionController.java` | Modify | Store original details, call new adapter method |
| `eliminarAtencion()` in `AtencionAdapter.java` | Modify | Add stock reversal before deletion |
| `JdbcAtencionRepository.java` | Modify | Add `deleteDetallesByAtencionId()` method |

---

### Task 1: Create `EditarAtencionUseCase`

**Files:**
- Create: `src/main/java/com/utp/meditrackapp/domain/services/dispensacion/EditarAtencionUseCase.java`

**Interfaces:**
- Consumes: `AtencionRepository`, `LoteRepository`, `MovimientoRepository`, `TransactionManager`
- Produces: `editarAtencion(Atencion, List<AtencionDetalle> originales, List<AtencionDetalle> nuevas)` → `String`

- [ ] **Step 1: Create the use case class**

```java
package com.utp.meditrackapp.domain.services.dispensacion;

import com.utp.meditrackapp.application.config.TransactionManager;
import com.utp.meditrackapp.core.models.enums.MotivoMovimientoEnum;
import com.utp.meditrackapp.core.models.enums.TipoMovimientoEnum;
import com.utp.meditrackapp.domain.entities.Atencion;
import com.utp.meditrackapp.domain.entities.AtencionDetalle;
import com.utp.meditrackapp.domain.entities.Movimiento;
import com.utp.meditrackapp.domain.ports.out.AtencionRepository;
import com.utp.meditrackapp.domain.ports.out.LoteRepository;
import com.utp.meditrackapp.domain.ports.out.MovimientoRepository;

import java.util.List;

/**
 * Caso de uso: Editar una atención existente ajustando stock y creando movimientos de auditoría.
 * Invierte el efecto de stock original y aplica el nuevo efecto.
 */
public class EditarAtencionUseCase {
    private final AtencionRepository atencionRepository;
    private final LoteRepository loteRepository;
    private final MovimientoRepository movimientoRepository;
    private final TransactionManager transactionManager;

    public EditarAtencionUseCase(AtencionRepository atencionRepository,
                                  LoteRepository loteRepository,
                                  MovimientoRepository movimientoRepository,
                                  TransactionManager transactionManager) {
        this.atencionRepository = atencionRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.transactionManager = transactionManager;
    }

    /**
     * Edita una atención ajustando stock y detalles.
     *
     * @param atencion   La atención con campos actualizados (receta, médico)
     * @param originales Los detalles originales (antes de la edición)
     * @param nuevas     Los detalles nuevos (después de la edición)
     * @return "OK" si éxito, o mensaje de error
     */
    public String editarAtencion(Atencion atencion, List<AtencionDetalle> originales, List<AtencionDetalle> nuevas) {
        if (atencion == null || atencion.getId() == null) {
            return "La atención es inválida.";
        }
        if (nuevas == null || nuevas.isEmpty()) {
            return "Debe haber al menos un medicamento.";
        }

        // Validate new details
        for (AtencionDetalle det : nuevas) {
            String val = det.validate();
            if (val != null) return val;
        }

        try {
            transactionManager.execute(conn -> {
                // 1. Update parent atencion (receta, medico)
                atencionRepository.updateAtencion(atencion);

                // 2. Delete old details
                atencionRepository.deleteDetallesByAtencionId(conn, atencion.getId());

                // 3. For each original detail: reverse stock + create reversal movement
                for (AtencionDetalle orig : originales) {
                    loteRepository.aumentarStock(conn, orig.getLoteId(), orig.getCantidadEntregada());

                    Movimiento reversa = new Movimiento();
                    reversa.setTipoId(TipoMovimientoEnum.ENTRADA.getId());
                    reversa.setMotivoId(MotivoMovimientoEnum.ATENCION.getId());
                    reversa.setSedeId(atencion.getSedeId());
                    reversa.setUsuarioId(atencion.getUsuarioId());
                    reversa.setLoteId(orig.getLoteId());
                    reversa.setCantidad(orig.getCantidadEntregada());
                    reversa.setObservacion("Reversa por edición - Receta: " + atencion.getNumeroReceta());
                    movimientoRepository.save(conn, reversa);
                }

                // 4. For each new detail: save detail + reduce stock + create exit movement
                for (AtencionDetalle det : nuevas) {
                    det.setAtencionId(atencion.getId());
                    atencionRepository.saveDetalle(conn, det);

                    loteRepository.reducirStock(conn, det.getLoteId(), det.getCantidadEntregada());

                    Movimiento salida = new Movimiento();
                    salida.setTipoId(TipoMovimientoEnum.SALIDA.getId());
                    salida.setMotivoId(MotivoMovimientoEnum.ATENCION.getId());
                    salida.setSedeId(atencion.getSedeId());
                    salida.setUsuarioId(atencion.getUsuarioId());
                    salida.setLoteId(det.getLoteId());
                    salida.setCantidad(det.getCantidadEntregada());
                    salida.setObservacion("Atención Médica (editada) - Receta: " + atencion.getNumeroReceta());
                    movimientoRepository.save(conn, salida);
                }
            });
            return "OK";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
```

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -q`
Expected: No errors

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/utp/meditrackapp/domain/services/dispensacion/EditarAtencionUseCase.java
git commit -m "feat(inventory): add EditarAtencionUseCase with stock reversal logic"
```

---

### Task 2: Add `deleteDetallesByAtencionId()` to Repository

**Files:**
- Modify: `src/main/java/com/utp/meditrackapp/domain/ports/out/AtencionRepository.java:40`
- Modify: `src/main/java/com/utp/meditrackapp/infrastructure/persistence/jdbc/JdbcAtencionRepository.java:307`

**Interfaces:**
- Produces: `deleteDetallesByAtencionId(Connection conn, String atencionId)` → `void`

- [ ] **Step 1: Add method to interface**

In `AtencionRepository.java`, add before the closing brace:

```java
void deleteDetallesByAtencionId(java.sql.Connection conn, String atencionId);
```

- [ ] **Step 2: Implement in JdbcAtencionRepository**

In `JdbcAtencionRepository.java`, add after `deleteAtencion()`:

```java
@Override
public void deleteDetallesByAtencionId(java.sql.Connection conn, String atencionId) {
    String sql = "DELETE FROM atencion_detalles WHERE atencion_id = ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, atencionId);
        ps.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException("Error al eliminar detalles: " + e.getMessage(), e);
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./mvnw compile -q`
Expected: No errors

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/utp/meditrackapp/domain/ports/out/AtencionRepository.java src/main/java/com/utp/meditrackapp/infrastructure/persistence/jdbc/JdbcAtencionRepository.java
git commit -m "feat(inventory): add deleteDetallesByAtencionId to repository"
```

---

### Task 3: Wire `EditarAtencionUseCase` into `AtencionAdapter`

**Files:**
- Modify: `src/main/java/com/utp/meditrackapp/infrastructure/adapters/AtencionAdapter.java:40-60`

**Interfaces:**
- Consumes: `EditarAtencionUseCase` (from Task 1)
- Produces: `editarAtencionCompleta(Atencion, List<AtencionDetalle>, List<AtencionDetalle>)` → `String`

- [ ] **Step 1: Add field and constructor wiring**

In `AtencionAdapter.java`, find the field declarations and add:

```java
private final EditarAtencionUseCase editarAtencionUseCase;
```

In the constructor, add initialization:

```java
this.editarAtencionUseCase = new EditarAtencionUseCase(
    atencionRepository,
    new JdbcLoteRepository(),
    new JdbcMovimientoRepository(),
    new TransactionManager()
);
```

- [ ] **Step 2: Add the new method**

In `AtencionAdapter.java`, add after `editarAtencion()`:

```java
public String editarAtencionCompleta(Atencion atencion, List<AtencionDetalle> originales, List<AtencionDetalle> nuevas) {
    return editarAtencionUseCase.editarAtencion(atencion, originales, nuevas);
}
```

- [ ] **Step 3: Add imports**

Add to the imports section:

```java
import com.utp.meditrackapp.domain.services.dispensacion.EditarAtencionUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcMovimientoRepository;
import com.utp.meditrackapp.application.config.TransactionManager;
import java.util.List;
```

- [ ] **Step 4: Verify compilation**

Run: `./mvnw compile -q`
Expected: No errors

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/utp/meditrackapp/infrastructure/adapters/AtencionAdapter.java
git commit -m "feat(inventory): wire EditarAtencionUseCase into AtencionAdapter"
```

---

### Task 4: Update `AtencionController` to Use Stock-Aware Edit

**Files:**
- Modify: `src/main/java/com/utp/meditrackapp/features/attentions/ui/AtencionController.java:68-72,290-332`

**Interfaces:**
- Consumes: `atencionAdapter.editarAtencionCompleta()` (from Task 3)
- Produces: Proper stock adjustment on save

- [ ] **Step 1: Add field to store original details**

In `AtencionController.java`, find the field declarations and add:

```java
private List<AtencionDetalle> originalDetalles = new ArrayList<>();
```

- [ ] **Step 2: Update `onEditarAtencion()` to store original details**

Find `onEditarAtencion()` and update the detail loading section. Replace the try-catch block that loads details:

```java
// Load details for editing
editDetalleItems.clear();
originalDetalles.clear();
try {
    List<AtencionDetalle> detalles = atencionAdapter.buscarDetallesAtencion(atencion.getId());
    originalDetalles.addAll(detalles);  // Store originals for stock reversal
    Usuario user = sessionManager.getCurrentUser();
    String sedeId = user != null ? user.getSedeId() : null;
    for (AtencionDetalle det : detalles) {
        EditableDetalleRow row = new EditableDetalleRow();
        row.detalleIdProperty().set(det.getId());
        row.productoIdProperty().set(det.getProductoId());
        row.productoNombreProperty().set(det.getProductoNombre());
        row.loteIdProperty().set(det.getLoteId());
        row.loteNumeroProperty().set(det.getLoteNumero());
        row.cantidadEntregadaProperty().set(det.getCantidadEntregada());
        // Load available lots for this product
        if (det.getProductoId() != null && sedeId != null) {
            List<Lote> lotes = atencionAdapter.listarLotesFefo(sedeId, det.getProductoId());
            row.setLotesDisponibles(FXCollections.observableArrayList(lotes));
        }
        editDetalleItems.add(row);
    }
} catch (Exception e) {
    e.printStackTrace();
}
```

- [ ] **Step 3: Update `onSaveEditar()` to use stock-aware method**

Replace the entire `onSaveEditar()` method:

```java
@FXML
protected void onSaveEditar() {
    if (editingAtencion == null) return;
    String receta = txtEditReceta.getText();
    if (receta == null || receta.isBlank()) {
        showAlert(Alert.AlertType.WARNING, "Validación", "El número de receta es obligatorio.");
        return;
    }
    editingAtencion.setNumeroReceta(receta.trim());
    editingAtencion.setMedico(txtEditMedico.getText() != null ? txtEditMedico.getText().trim() : "");

    // Build new details list from edit table
    List<AtencionDetalle> nuevas = new ArrayList<>();
    for (EditableDetalleRow row : editDetalleItems) {
        if (row.getLoteId() == null || row.getLoteId().isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Validación",
                "Seleccione un lote para: " + row.getProductoNombre());
            return;
        }
        if (row.getCantidadEntregada() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Validación",
                "Ingrese una cantidad válida para: " + row.getProductoNombre());
            return;
        }
        AtencionDetalle det = new AtencionDetalle();
        det.setId(row.getDetalleId());
        det.setLoteId(row.getLoteId());
        det.setCantidadEntregada(row.getCantidadEntregada());
        det.setProductoId(row.getProductoId());
        det.setProductoNombre(row.getProductoNombre());
        nuevas.add(det);
    }

    try {
        String result = atencionAdapter.editarAtencionCompleta(editingAtencion, originalDetalles, nuevas);
        if ("OK".equals(result)) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Atención actualizada correctamente.");
            modalEditar.setVisible(false);
            editDetalleItems.clear();
            originalDetalles.clear();
            editingAtencion = null;
            onBuscarAtenciones();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", result);
        }
    } catch (Exception e) {
        showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar: " + e.getMessage());
    }
}
```

- [ ] **Step 4: Update `onCloseEditar()` to clear originals**

```java
@FXML
protected void onCloseEditar() {
    modalEditar.setVisible(false);
    editDetalleItems.clear();
    originalDetalles.clear();
    editingAtencion = null;
}
```

- [ ] **Step 5: Verify compilation**

Run: `./mvnw compile -q`
Expected: No errors

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/utp/meditrackapp/features/attentions/ui/AtencionController.java
git commit -m "feat(inventory): use stock-aware edit in AtencionController"
```

---

### Task 5: Fix `eliminarAtencion()` to Reverse Stock

**Files:**
- Modify: `src/main/java/com/utp/meditrackapp/infrastructure/adapters/AtencionAdapter.java:118-121`
- Modify: `src/main/java/com/utp/meditrackapp/domain/ports/out/AtencionRepository.java`
- Modify: `src/main/java/com/utp/meditrackapp/infrastructure/persistence/jdbc/JdbcAtencionRepository.java`

**Interfaces:**
- Produces: `eliminarAtencionCompleta(String atencionId, String sedeId, String usuarioId)` → `String`

- [ ] **Step 1: Add `deleteMovimientosByLotes()` to repository**

In `AtencionRepository.java`, add:

```java
void deleteMovimientosByAtencionId(java.sql.Connection conn, String atencionId);
```

In `JdbcAtencionRepository.java`, add implementation. This requires joining with `atencion_detalles` to find the movement records:

```java
@Override
public void deleteMovimientosByAtencionId(java.sql.Connection conn, String atencionId) {
    String sql = "DELETE FROM movimientos WHERE lote_id IN " +
        "(SELECT lote_id FROM atencion_detalles WHERE atencion_id = ?) " +
        "AND observacion LIKE ?";
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, atencionId);
        ps.setString(2, "%Receta:%");
        ps.executeUpdate();
    } catch (SQLException e) {
        throw new RuntimeException("Error al eliminar movimientos: " + e.getMessage(), e);
    }
}
```

- [ ] **Step 2: Create `EliminarAtencionUseCase`**

Create: `src/main/java/com/utp/meditrackapp/domain/services/dispensacion/EliminarAtencionUseCase.java`

```java
package com.utp.meditrackapp.domain.services.dispensacion;

import com.utp.meditrackapp.application.config.TransactionManager;
import com.utp.meditrackapp.domain.entities.AtencionDetalle;
import com.utp.meditrackapp.domain.ports.out.AtencionRepository;
import com.utp.meditrackapp.domain.ports.out.LoteRepository;
import com.utp.meditrackapp.domain.ports.out.MovimientoRepository;

import java.util.List;

/**
 * Caso de uso: Eliminar una atención devolviendo stock y limpiando movimientos.
 */
public class EliminarAtencionUseCase {
    private final AtencionRepository atencionRepository;
    private final LoteRepository loteRepository;
    private final MovimientoRepository movimientoRepository;
    private final TransactionManager transactionManager;

    public EliminarAtencionUseCase(AtencionRepository atencionRepository,
                                    LoteRepository loteRepository,
                                    MovimientoRepository movimientoRepository,
                                    TransactionManager transactionManager) {
        this.atencionRepository = atencionRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.transactionManager = transactionManager;
    }

    public String eliminar(String atencionId, String sedeId, String usuarioId) {
        if (atencionId == null || atencionId.isEmpty()) {
            return "ID de atención inválido.";
        }

        try {
            transactionManager.execute(conn -> {
                // 1. Load details to know what stock to return
                List<AtencionDetalle> detalles = atencionRepository.findDetallesByAtencionId(atencionId);

                // 2. Return stock for each detail
                for (AtencionDetalle det : detalles) {
                    loteRepository.aumentarStock(conn, det.getLoteId(), det.getCantidadEntregada());
                }

                // 3. Delete movement records linked to this attention
                atencionRepository.deleteMovimientosByAtencionId(conn, atencionId);

                // 4. Delete detail records
                atencionRepository.deleteDetallesByAtencionId(conn, atencionId);

                // 5. Delete parent attention
                atencionRepository.deleteAtencion(atencionId);
            });
            return "OK";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
```

- [ ] **Step 3: Wire into AtencionAdapter**

In `AtencionAdapter.java`, add field:

```java
private final EliminarAtencionUseCase eliminarAtencionUseCase;
```

In constructor, add:

```java
this.eliminarAtencionUseCase = new EliminarAtencionUseCase(
    atencionRepository,
    new JdbcLoteRepository(),
    new JdbcMovimientoRepository(),
    new TransactionManager()
);
```

Replace `eliminarAtencion()`:

```java
public String eliminarAtencion(String atencionId) {
    Usuario user = SessionManager.getInstance().getCurrentUser();
    return eliminarAtencionUseCase.eliminar(atencionId, user.getSedeId(), user.getId());
}
```

Add import:

```java
import com.utp.meditrackapp.domain.services.dispensacion.EliminarAtencionUseCase;
import com.utp.meditrackapp.core.config.SessionManager;
```

- [ ] **Step 4: Verify compilation**

Run: `./mvnw compile -q`
Expected: No errors

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/utp/meditrackapp/domain/services/dispensacion/EliminarAtencionUseCase.java src/main/java/com/utp/meditrackapp/infrastructure/adapters/AtencionAdapter.java src/main/java/com/utp/meditrackapp/domain/ports/out/AtencionRepository.java src/main/java/com/utp/meditrackapp/infrastructure/persistence/jdbc/JdbcAtencionRepository.java
git commit -m "feat(inventory): fix eliminarAtencion to reverse stock and clean movements"
```

---

### Task 6: Final Verification

- [ ] **Step 1: Full compilation**

Run: `./mvnw compile -q`
Expected: No errors

- [ ] **Step 2: Manual test scenarios**

Test the following in the app:

1. **Edit quantity only:** Create attention with 5 units from Lot A. Edit to 3 units. Verify Lot A stock increased by 2.
2. **Edit lot only:** Create attention with 3 units from Lot A. Change to Lot B. Verify Lot A +3, Lot B -3.
3. **Edit both:** Create attention with 5 from Lot A. Change to Lot B with 3 units. Verify Lot A +5, Lot B -3.
4. **Delete attention:** Create attention, then delete it. Verify all lots restored and movements cleaned.
5. **Check inventory tab:** Movements table should show reversal + new exit records.

- [ ] **Step 3: Commit final state**

```bash
git add -A
git commit -m "feat(inventory): complete stock adjustment on attention edit/delete"
```

---

## Stock Adjustment Logic Summary

| Scenario | Stock Action | Movement Records |
|----------|-------------|-----------------|
| Quantity changed (5→3, same lot) | Lot: +2 (return difference) | 1 reversal (ENTRADA, qty 2) |
| Lot changed (A→B, same qty) | Lot A: +qty, Lot B: -qty | 1 reversal (ENTRADA, A) + 1 exit (SALIDA, B) |
| Both changed (A/5 → B/3) | Lot A: +5, Lot B: -3 | 1 reversal (ENTRADA, A/5) + 1 exit (SALIDA, B/3) |
| Detail removed | Lot: +original qty | 1 reversal (ENTRADA, original qty) |
| Detail added | Lot: -new qty | 1 exit (SALIDA, new qty) |
| Attention deleted | All lots: +original qty each | All movements deleted |
