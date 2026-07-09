package com.utp.meditrackapp.domain.services.inventario;

import com.utp.meditrackapp.application.config.TransactionManager;
import com.utp.meditrackapp.core.models.enums.TipoMovimientoEnum;
import com.utp.meditrackapp.domain.entities.Lote;
import com.utp.meditrackapp.domain.entities.Movimiento;
import com.utp.meditrackapp.domain.ports.out.LoteRepository;
import com.utp.meditrackapp.domain.ports.out.MovimientoRepository;

import java.sql.SQLException;

/**
 * Caso de uso: Registrar un movimiento de inventario (entrada o salida).
 * Centraliza la lógica que estaba en InventarioService.registrarMovimiento()
 */
public class RegistrarMovimientoUseCase {
    private final LoteRepository loteRepository;
    private final MovimientoRepository movimientoRepository;
    private final TransactionManager transactionManager;

    public RegistrarMovimientoUseCase(LoteRepository loteRepository,
                                      MovimientoRepository movimientoRepository,
                                      TransactionManager transactionManager) {
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.transactionManager = transactionManager;
    }

    public void registrarMovimiento(Lote lote, String usuarioId, String tipoId, String motivoId,
                                    int cantidad, String observacion) {
        boolean isEntrada = isTipoEntrada(tipoId);
        if (isEntrada) {
            registrarEntrada(lote, usuarioId, motivoId, cantidad, observacion);
        } else {
            registrarSalida(lote.getId(), usuarioId, motivoId, cantidad, observacion, lote.getSedeId());
        }
    }

    public void registrarEntrada(Lote lote, String usuarioId, String motivoId, int cantidad, String observacion) {
        String validation = validateEntrada(lote, cantidad);
        if (validation != null) {
            throw new IllegalArgumentException(validation);
        }

        try {
            transactionManager.execute(conn -> {
                if (lote.getId() == null || lote.getId().isEmpty()) {
                    loteRepository.save(conn, lote);
                } else {
                    loteRepository.aumentarStock(conn, lote.getId(), cantidad);
                }

                Movimiento mov = buildMovimiento(lote, usuarioId, motivoId, cantidad, observacion);
                movimientoRepository.save(conn, mov);
            });
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar entrada: " + e.getMessage(), e);
        }
    }

    public void registrarSalida(String loteId, String usuarioId, String motivoId,
                                int cantidad, String observacion, String sedeId) {
        if (loteId == null || loteId.isEmpty()) {
            throw new IllegalArgumentException("El lote es obligatorio.");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        try {
            transactionManager.execute(conn -> {
                loteRepository.reducirStock(conn, loteId, cantidad);

                Movimiento mov = new Movimiento();
                mov.setLoteId(loteId);
                mov.setSedeId(sedeId);
                mov.setUsuarioId(usuarioId);
                mov.setTipoId(TipoMovimientoEnum.SALIDA.getId());
                mov.setMotivoId(motivoId);
                mov.setCantidad(cantidad);
                mov.setObservacion(observacion);
                movimientoRepository.save(conn, mov);
            });
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar salida: " + e.getMessage(), e);
        }
    }

    private boolean isTipoEntrada(String tipoId) {
        if (tipoId == null) return false;
        return TipoMovimientoEnum.ENTRADA.getId().equals(tipoId);
    }

    private Movimiento buildMovimiento(Lote lote, String usuarioId, String motivoId,
                                       int cantidad, String observacion) {
        Movimiento mov = new Movimiento();
        mov.setSedeId(lote.getSedeId());
        mov.setUsuarioId(usuarioId);
        mov.setTipoId(TipoMovimientoEnum.ENTRADA.getId());
        mov.setMotivoId(motivoId);
        mov.setLoteId(lote.getId());
        mov.setCantidad(cantidad);
        mov.setObservacion(observacion);
        return mov;
    }

    private String validateEntrada(Lote lote, int cantidad) {
        if (lote == null) return "El lote es obligatorio.";
        if (lote.getProductoId() == null || lote.getProductoId().isEmpty()) return "El producto es obligatorio.";
        if (lote.getSedeId() == null || lote.getSedeId().isEmpty()) return "La sede es obligatoria.";
        if (cantidad <= 0) return "La cantidad debe ser mayor a cero.";
        if (lote.getFechaVencimiento() == null) return "La fecha de vencimiento es obligatoria.";
        if (lote.getNumeroLote() == null || lote.getNumeroLote().isBlank()) return "El N° de Lote es obligatorio.";
        if (loteRepository.existsByNumeroLoteProductoSede(
                lote.getNumeroLote().trim(), lote.getProductoId(), lote.getSedeId())) {
            return "Ya existe un lote con el N° \"" + lote.getNumeroLote().trim()
                    + "\" para este producto en esta sede. Verifique el número e intente nuevamente.";
        }
        return null;
    }
}
