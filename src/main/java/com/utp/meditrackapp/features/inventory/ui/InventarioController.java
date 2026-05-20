package com.utp.meditrackapp.features.inventory.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.entity.Lote;
import com.utp.meditrackapp.core.models.entity.Producto;
import com.utp.meditrackapp.core.service.InventarioService;
import com.utp.meditrackapp.features.inventory.service.MovimientoService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller principal para la gestión de Inventario.
 * Orquesta la visualización de productos/lotes y la ejecución de movimientos.
 */
public class InventarioController {

    private final InventarioService inventarioService;
    private final MovimientoService movimientoService;
    private final SessionManager sessionManager;

    public InventarioController() {
        this.inventarioService = new InventarioService();
        this.movimientoService = new MovimientoService();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Lista los productos activos en el sistema.
     */
    public List<Producto> obtenerProductos() {
        try {
            return inventarioService.listarProductosActivos();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Lista los lotes de la sede actual del usuario.
     */
    public List<Lote> obtenerLotesSedeActual() {
        if (!sessionManager.isLoggedIn()) return new ArrayList<>();
        try {
            String sedeId = sessionManager.getCurrentUser().getSedeId();
            return inventarioService.listarLotesFefo(sedeId, null);
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // =========================================================================
    // LÓGICA DE MOVIMIENTOS CON REGLAS DE ROL
    // =========================================================================

    /**
     * Procesa la entrada de mercadería (Compra/Ingreso).
     * ROL: Administrador, Químico y Técnico pueden registrar ingresos.
     */
    public String registrarEntrada(Lote nuevoLote, String observacion) {
        if (!sessionManager.isLoggedIn()) return "Sesión no iniciada.";
        
        // El Técnico puede registrar nuevos ingresos
        String usuarioId = sessionManager.getCurrentUser().getId();
        boolean exito = movimientoService.registrarEntrada(nuevoLote, usuarioId, observacion);
        
        return exito ? "OK" : "Error al registrar la entrada de stock.";
    }

    /**
     * Procesa la merma de productos (Pérdida/Dañado).
     * ROL: Solo Administrador y Químico Farmacéutico.
     */
    public String registrarMerma(String loteId, int cantidad, String observacion) {
        if (!sessionManager.isLoggedIn()) return "Sesión no iniciada.";
        
        // REGLA DE ROL: Técnico NO puede registrar mermas (ajuste negativo crítico)
        if (sessionManager.isTecnico()) {
            return "No tiene permisos para registrar mermas. Operación reservada para Gestión.";
        }

        String usuarioId = sessionManager.getCurrentUser().getId();
        boolean exito = movimientoService.registrarMerma(loteId, cantidad, usuarioId, observacion);
        
        return exito ? "OK" : "Error al registrar la merma. Verifique el stock disponible.";
    }

    /**
     * Procesa la transferencia de stock a otra sede.
     * ROL: Solo Administrador y Químico Farmacéutico (Gestión de red).
     */
    public String registrarTransferencia(String loteId, String sedeDestinoId, int cantidad, String observacion) {
        if (!sessionManager.isLoggedIn()) return "Sesión no iniciada.";

        // REGLA DE ROL: Operativo (Técnico) no gestiona logística entre sedes.
        if (sessionManager.isTecnico()) {
            return "No tiene permisos para realizar transferencias entre sedes.";
        }

        String usuarioId = sessionManager.getCurrentUser().getId();
        boolean exito = movimientoService.registrarTransferencia(loteId, sedeDestinoId, cantidad, usuarioId, observacion);
        
        return exito ? "OK" : "Error al procesar la transferencia. Verifique stock origen.";
    }
}
