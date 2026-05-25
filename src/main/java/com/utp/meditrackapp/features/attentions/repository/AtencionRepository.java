package com.utp.meditrackapp.features.attentions.repository;

import com.utp.meditrackapp.core.models.entity.Atencion;
import com.utp.meditrackapp.core.models.entity.AtencionDetalle;
import java.util.List;

/**
 * Contrato para la persistencia transaccional de Atenciones.
 */
public interface AtencionRepository {
    /**
     * Registra una atención completa junto con sus detalles y actualiza stock.
     * Implementa transaccionalidad ACID.
     */
    boolean registrarAtencionCompleta(Atencion atencion, List<AtencionDetalle> detalles);
    
    List<Atencion> findAll();
    Atencion findById(String id);
}
