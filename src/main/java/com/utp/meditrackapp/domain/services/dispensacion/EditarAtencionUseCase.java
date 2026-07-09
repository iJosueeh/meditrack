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
