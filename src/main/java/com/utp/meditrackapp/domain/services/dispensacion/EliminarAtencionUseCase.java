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
