package com.utp.meditrackapp.features.attentions.ui;

import com.utp.meditrackapp.core.models.entity.Atencion;
import com.utp.meditrackapp.core.models.entity.AtencionDetalle;
import com.utp.meditrackapp.features.attentions.service.AtencionService;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller para la gestión de Atenciones.
 * Maneja la lógica de negocio para la vista de atenciones (backend logic).
 */
public class AtencionController {

    private final AtencionService atencionService;
    private final List<AtencionDetalle> detallesTemporales;

    public AtencionController() {
        this.atencionService = new AtencionService();
        this.detallesTemporales = new ArrayList<>();
    }

    /**
     * Agrega un detalle de medicamento a la lista temporal de la atención actual.
     */
    public void agregarDetalle(String loteId, int cantidad) {
        AtencionDetalle detalle = new AtencionDetalle();
        detalle.setLoteId(loteId);
        detalle.setCantidadEntregada(cantidad);
        detallesTemporales.add(detalle);
    }

    /**
     * Limpia la lista de detalles temporales.
     */
    public void limpiarDetalles() {
        detallesTemporales.clear();
    }

    /**
     * Procesa el registro de la atención completa.
     */
    public String procesarRegistroAtencion(String pacienteId, String numeroReceta) {
        Atencion atencion = new Atencion();
        atencion.setPacienteId(pacienteId);
        atencion.setNumeroReceta(numeroReceta);

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
