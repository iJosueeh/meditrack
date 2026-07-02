package com.utp.meditrackapp.domain.services.inventario;

import com.utp.meditrackapp.application.config.TransactionManager;
import com.utp.meditrackapp.domain.entities.Movimiento;
import com.utp.meditrackapp.domain.ports.out.LoteRepository;
import com.utp.meditrackapp.domain.ports.out.MovimientoRepository;

import java.sql.SQLException;

public class AnularMovimientoUseCase {
    private final LoteRepository loteRepository;
    private final MovimientoRepository movimientoRepository;
    private final TransactionManager transactionManager;

    public AnularMovimientoUseCase(LoteRepository loteRepository,
                                    MovimientoRepository movimientoRepository,
                                    TransactionManager transactionManager) {
        this.loteRepository = loteRepository;
        this.movimientoRepository = movimientoRepository;
        this.transactionManager = transactionManager;
    }

    public void anular(Movimiento movimiento) {
        if (movimiento == null || movimiento.getId() == null) {
            throw new IllegalArgumentException("El movimiento es inválido.");
        }

        try {
            transactionManager.execute(conn -> {
                if (movimiento.isEntrada()) {
                    int stockActual = loteRepository.findStockByLote(conn, movimiento.getLoteId());
                    if (stockActual < movimiento.getCantidad()) {
                        throw new IllegalStateException(
                            "No se puede anular la entrada. Stock actual (" + stockActual +
                            ") es menor que la cantidad a revertir (" + movimiento.getCantidad() + ").");
                    }
                    loteRepository.reducirStock(conn, movimiento.getLoteId(), movimiento.getCantidad());
                } else {
                    loteRepository.aumentarStock(conn, movimiento.getLoteId(), movimiento.getCantidad());
                }

                movimientoRepository.deleteById(conn, movimiento.getId());
            });
        } catch (SQLException e) {
            throw new RuntimeException("Error al anular movimiento: " + e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw e;
        }
    }
}
