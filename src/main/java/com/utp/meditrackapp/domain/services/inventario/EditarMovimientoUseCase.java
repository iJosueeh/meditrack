package com.utp.meditrackapp.domain.services.inventario;

import com.utp.meditrackapp.application.config.TransactionManager;
import com.utp.meditrackapp.domain.entities.Movimiento;
import com.utp.meditrackapp.domain.ports.out.LoteRepository;
import com.utp.meditrackapp.domain.ports.out.MovimientoRepository;

import java.sql.SQLException;

public class EditarMovimientoUseCase {
    private final LoteRepository loteRepository;
    private final MovimientoRepository movimientoRepository;
    private final TransactionManager transactionManager;

    public EditarMovimientoUseCase(LoteRepository loteRepository,
                                    MovimientoRepository movimientoRepository,
                                    TransactionManager transactionManager) {
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.transactionManager = transactionManager;
    }

    public void editar(Movimiento original, String nuevoTipoId, String nuevoMotivoId,
                       int nuevaCantidad, String nuevaObservacion) {
        if (original == null || original.getId() == null) {
            throw new IllegalArgumentException("El movimiento original es inválido.");
        }
        if (nuevaCantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero.");
        }

        boolean cambioTipo = !original.getTipoId().equals(nuevoTipoId);
        boolean cambioCantidad = original.getCantidad() != nuevaCantidad;

        try {
            transactionManager.execute(conn -> {
                if (cambioTipo || cambioCantidad) {
                    if (original.isEntrada()) {
                        int stockActual = loteRepository.findStockByLote(conn, original.getLoteId());
                        if (stockActual < original.getCantidad()) {
                            throw new IllegalStateException(
                                "No se puede editar. Stock actual (" + stockActual +
                                ") es menor que la cantidad original a revertir (" + original.getCantidad() + ").");
                        }
                        loteRepository.reducirStock(conn, original.getLoteId(), original.getCantidad());
                    } else {
                        loteRepository.aumentarStock(conn, original.getLoteId(), original.getCantidad());
                    }
                }

                movimientoRepository.deleteById(conn, original.getId());

                Movimiento nuevo = new Movimiento();
                nuevo.setTipoId(nuevoTipoId);
                nuevo.setMotivoId(nuevoMotivoId);
                nuevo.setSedeId(original.getSedeId());
                nuevo.setUsuarioId(original.getUsuarioId());
                nuevo.setLoteId(original.getLoteId());
                nuevo.setCantidad(nuevaCantidad);
                nuevo.setObservacion(nuevaObservacion);
                movimientoRepository.save(conn, nuevo);
            });
        } catch (SQLException e) {
            throw new RuntimeException("Error al editar movimiento: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw e;
        }
    }
}
