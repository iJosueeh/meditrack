package com.utp.meditrackapp.features.attentions.ui;

import com.utp.meditrackapp.core.models.entity.Atencion;
import com.utp.meditrackapp.core.models.entity.AtencionDetalle;
import com.utp.meditrackapp.features.attentions.service.AtencionService;

import com.utp.meditrackapp.core.models.entity.Lote;
import com.utp.meditrackapp.core.service.InventarioService;
import com.utp.meditrackapp.core.config.SessionManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller para la gestión de Atenciones.
 * Maneja la lógica de negocio para la vista de atenciones (backend logic).
 */
public class AtencionController {

    private final AtencionService atencionService;
    private final InventarioService inventarioService;
    private final List<AtencionDetalle> detallesTemporales;
    private final SessionManager sessionManager;

    public AtencionController() {
        this.atencionService = new AtencionService();
        this.inventarioService = new InventarioService();
        this.detallesTemporales = new ArrayList<>();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Obtiene los lotes sugeridos para un producto específico siguiendo la lógica FEFO
     * (First Expired, First Out) de la sede actual del usuario.
     */
    public List<Lote> obtenerLotesSugeridos(String productoId) {
        if (!sessionManager.isLoggedIn()) return new ArrayList<>();
        
        try {
            String sedeId = sessionManager.getCurrentUser().getSedeId();
            return inventarioService.listarLotesFefo(sedeId, productoId);
        } catch (SQLException e) {
            System.err.println("[ATENCION ERROR] Error al obtener lotes FEFO: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Agrega un detalle de medicamento a la lista temporal.
     * Valida que el usuario tenga un rol autorizado antes de permitir la edición local.
     */
    public String agregarDetalle(String loteId, int cantidad) {
        if (!sessionManager.isLoggedIn()) return "Sesión no iniciada.";
        
        // Verificación preventiva de rol (la validación final ocurre en el Service)
        if (!(sessionManager.isAdmin() || sessionManager.isQuimico() || sessionManager.isTecnico())) {
            return "No tiene permisos para dispensar medicamentos.";
        }

        AtencionDetalle detalle = new AtencionDetalle();
        detalle.setLoteId(loteId);
        detalle.setCantidadEntregada(cantidad);
        detallesTemporales.add(detalle);
        return "OK";
    }

    /**
     * Limpia la lista de detalles temporales.
     */
    public void limpiarDetalles() {
        detallesTemporales.clear();
    }

    /**
     * Procesa el registro de la atención completa.
     * La validación de roles y transaccionalidad ACID ocurre en el Service.
     */
    public String procesarRegistroAtencion(String pacienteId, String numeroReceta) {
        Atencion atencion = new Atencion();
        atencion.setPacienteId(pacienteId);
        atencion.setNumeroReceta(numeroReceta);

        // El Service se encarga de:
        // 1. Validar Roles (Admin, Químico, Técnico)
        // 2. Iniciar Transacción ACID
        // 3. Reducir Stock en LoteDAO
        // 4. Registrar Movimiento en MovimientoDAO
        String resultado = atencionService.registrarAtencion(atencion, detallesTemporales);
        
        if ("OK".equals(resultado)) {
            limpiarDetalles();
        }
        
        return resultado;
    }

    public List<AtencionDetalle> getDetallesTemporales() {
        return detallesTemporales;
    }

    /**
     * Obtiene el historial de atenciones registradas.
     */
    public List<Atencion> obtenerHistorial() {
        return atencionService.listarHistorial();
    }
}
