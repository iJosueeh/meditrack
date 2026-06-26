package com.utp.meditrackapp.domain.services.dispensacion;

import com.utp.meditrackapp.application.config.TransactionManager;
import com.utp.meditrackapp.domain.entities.Atencion;
import com.utp.meditrackapp.domain.entities.AtencionDetalle;
import com.utp.meditrackapp.domain.entities.Lote;
import com.utp.meditrackapp.domain.entities.MotivoMovimiento;
import com.utp.meditrackapp.domain.entities.Movimiento;
import com.utp.meditrackapp.domain.entities.TipoMovimiento;
import com.utp.meditrackapp.domain.ports.out.AtencionRepository;
import com.utp.meditrackapp.domain.ports.out.LoteRepository;
import com.utp.meditrackapp.domain.ports.out.MotivoMovimientoRepository;
import com.utp.meditrackapp.domain.ports.out.MovimientoRepository;
import com.utp.meditrackapp.domain.ports.out.TipoMovimientoRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Caso de uso: Dispensar medicamentos siguiendo FEFO.
 * Centraliza la lógica que estaba en AtencionService.registrarAtencion()
 */
public class DispensarMedicamentoUseCase {
    private final AtencionRepository atencionRepository;
    private final LoteRepository loteRepository;
    private final MovimientoRepository movimientoRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final MotivoMovimientoRepository motivoMovimientoRepository;
    private final TransactionManager transactionManager;

    public DispensarMedicamentoUseCase(AtencionRepository atencionRepository,
                                       LoteRepository loteRepository,
                                       MovimientoRepository movimientoRepository,
                                       TipoMovimientoRepository tipoMovimientoRepository,
                                       MotivoMovimientoRepository motivoMovimientoRepository,
                                       TransactionManager transactionManager) {
        this.atencionRepository = atencionRepository;
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.tipoMovimientoRepository = tipoMovimientoRepository;
        this.motivoMovimientoRepository = motivoMovimientoRepository;
        this.transactionManager = transactionManager;
    }

    /**
     * Registra una atención completa (cabecera + detalles) y descuenta stock FEFO.
     */
    public String registrarAtencion(Atencion atencion, List<AtencionDetalle> detalles) {
        String validation = validateDispensacion(atencion, detalles);
        if (validation != null) {
            return validation;
        }

        if (atencionRepository.existeReceta(atencion.getSedeId(), atencion.getNumeroReceta().trim())) {
            return "Ya existe una atención registrada con el número de receta " + atencion.getNumeroReceta().trim() + " en esta sede.";
        }

        // Buscar IDs reales por nombre para evitar problemas con FK
        String tipoSalidaId = tipoMovimientoRepository.findByNombre("salida")
            .map(TipoMovimiento::getId)
            .orElseThrow(() -> new RuntimeException("No se encontró el tipo de movimiento 'salida'"));
        String motivoAtencionId = motivoMovimientoRepository.findByNombre("atencion")
            .map(MotivoMovimiento::getId)
            .orElseThrow(() -> new RuntimeException("No se encontró el motivo de movimiento 'atencion'"));

        try {
            transactionManager.execute(conn -> {
                atencionRepository.save(conn, atencion);

                for (AtencionDetalle det : detalles) {
                    det.setAtencionId(atencion.getId());
                    atencionRepository.saveDetalle(conn, det);

                    loteRepository.reducirStock(conn, det.getLoteId(), det.getCantidadEntregada());

                    Movimiento mov = new Movimiento();
                    mov.setTipoId(tipoSalidaId);
                    mov.setMotivoId(motivoAtencionId);
                    mov.setSedeId(atencion.getSedeId());
                    mov.setUsuarioId(atencion.getUsuarioId());
                    mov.setLoteId(det.getLoteId());
                    mov.setCantidad(det.getCantidadEntregada());
                    mov.setObservacion("Atención Médica - Receta: " + atencion.getNumeroReceta());
                    movimientoRepository.save(conn, mov);
                }
            });
            return "OK";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public List<AtencionDetalle> sugerirDispensacion(String sedeId, String productoId, int cantidadSolicitada) {
        List<Lote> lotesFefo = loteRepository.findFefo(sedeId, productoId);
        List<AtencionDetalle> sugerencia = new ArrayList<>();
        int restante = cantidadSolicitada;

        for (Lote lote : lotesFefo) {
            if (restante <= 0) break;

            int aTomar = Math.min(lote.getCantidad(), restante);
            AtencionDetalle det = new AtencionDetalle();
            det.setLoteId(lote.getId());
            det.setCantidadEntregada(aTomar);
            det.setLoteNumero(lote.getNumeroLote());
            det.setProductoNombre(lote.getProductoNombre());

            sugerencia.add(det);
            restante -= aTomar;
        }

        if (restante > 0) {
            throw new IllegalArgumentException("Stock insuficiente para cubrir la cantidad solicitada (" + cantidadSolicitada + ").");
        }

        return sugerencia;
    }

    public List<Atencion> buscarHistorialPorReceta(String sedeId, String numeroReceta) {
        return atencionRepository.findByReceta(sedeId, numeroReceta);
    }

    public List<Atencion> buscarHistorialPorPaciente(String pacienteId) {
        return atencionRepository.findByPaciente(pacienteId);
    }

    private String validateDispensacion(Atencion atencion, List<AtencionDetalle> detalles) {
        if (atencion == null) return "La atención es obligatoria.";
        if (atencion.getSedeId() == null || atencion.getSedeId().isEmpty()) return "La sede es obligatoria.";
        if (atencion.getPacienteId() == null || atencion.getPacienteId().isEmpty()) return "El paciente es obligatorio.";
        if (atencion.getUsuarioId() == null || atencion.getUsuarioId().isEmpty()) return "El usuario es obligatorio.";
        if (atencion.getNumeroReceta() == null || atencion.getNumeroReceta().trim().isEmpty()) return "La receta es obligatoria.";
        if (detalles == null || detalles.isEmpty()) return "Debe agregar medicamentos.";

        for (AtencionDetalle det : detalles) {
            String detValidation = det.validate();
            if (detValidation != null) return detValidation;
        }
        return null;
    }
}
