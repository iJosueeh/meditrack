package com.utp.meditrackapp.domain.ports.in;

import com.utp.meditrackapp.domain.entities.Lote;
import com.utp.meditrackapp.domain.entities.TipoMovimiento;
import com.utp.meditrackapp.domain.entities.MotivoMovimiento;

import java.util.List;

/**
 * Puerto de entrada para operaciones de inventario.
 * Define los casos de uso disponibles desde la UI.
 */
public interface InventarioPort {

    void registrarEntrada(Lote lote, String usuarioId, String motivoId, int cantidad, String observacion);

    void registrarSalida(String loteId, String usuarioId, String motivoId, int cantidad, String observacion);

    List<Lote> listarLotesPorSede(String sedeId);

    List<Lote> listarLotesFefo(String sedeId, String productoId);

    List<TipoMovimiento> listarTiposMovimiento();

    List<MotivoMovimiento> listarMotivosMovimiento();
}
