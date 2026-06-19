package com.utp.meditrackapp.domain.ports.out;

import com.utp.meditrackapp.domain.entities.Lote;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Puerto de salida para la persistencia de Lotes.
 * Define el contrato que la capa de infraestructura debe implementar.
 */
public interface LoteRepository {

    Optional<Lote> findById(String id);

    List<Lote> findBySede(String sedeId);

    List<Lote> findBySedeAndProducto(String sedeId, String productoId);

    List<Lote> findFefo(String sedeId, String productoId);

    Map<String, Integer> findStockTotalBySede(String sedeId);

    int findStockTotal(String sedeId, String productoId);

    Lote save(Connection conn, Lote lote);

    void aumentarStock(Connection conn, String loteId, int cantidad);

    void reducirStock(Connection conn, String loteId, int cantidad);

    /**
     * Versión sin Connection para operaciones simples que manejan su propia conexión.
     */
    Lote save(Lote lote);
}
