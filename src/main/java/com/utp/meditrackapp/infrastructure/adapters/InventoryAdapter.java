package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.application.config.TransactionManager;
import com.utp.meditrackapp.application.dto.StockCriticoDTO;
import com.utp.meditrackapp.domain.entities.Lote;
import com.utp.meditrackapp.domain.entities.Movimiento;
import com.utp.meditrackapp.domain.entities.Producto;
import com.utp.meditrackapp.domain.ports.out.LoteRepository;
import com.utp.meditrackapp.domain.ports.out.MovimientoRepository;
import com.utp.meditrackapp.domain.services.inventario.CalcularStockUseCase;
import com.utp.meditrackapp.domain.services.inventario.RegistrarMovimientoUseCase;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcLoteRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcMotivoMovimientoRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcMovimientoRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcProductoRepository;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.JdbcTipoMovimientoRepository;
import com.utp.meditrackapp.domain.entities.MotivoMovimiento;
import com.utp.meditrackapp.domain.entities.TipoMovimiento;

import java.time.LocalDate;
import java.util.List;

/**
 * Adaptador para InventoryController.
 * Delega a los casos de uso y repositorios del dominio.
 */
public class InventoryAdapter {
    private final JdbcProductoRepository productoRepository;
    private final JdbcTipoMovimientoRepository tipoMovimientoRepository;
    private final JdbcMotivoMovimientoRepository motivoMovimientoRepository;
    private final JdbcMovimientoRepository movimientoRepository;
    private final LoteRepository loteRepository;
    private final CalcularStockUseCase calcularStockUseCase;
    private final RegistrarMovimientoUseCase registrarMovimientoUseCase;

    public InventoryAdapter() {
        this.productoRepository = new JdbcProductoRepository();
        this.tipoMovimientoRepository = new JdbcTipoMovimientoRepository();
        this.motivoMovimientoRepository = new JdbcMotivoMovimientoRepository();
        this.movimientoRepository = new JdbcMovimientoRepository();
        this.loteRepository = new JdbcLoteRepository();
        this.calcularStockUseCase = new CalcularStockUseCase(new JdbcLoteRepository());
        this.registrarMovimientoUseCase = new RegistrarMovimientoUseCase(
            new JdbcLoteRepository(),
            new JdbcMovimientoRepository(),
            new TransactionManager()
        );
    }

    public List<Producto> listarProductosActivos() {
        return productoRepository.findActivos();
    }

    public List<TipoMovimiento> listarTiposMovimiento() {
        return tipoMovimientoRepository.findAll();
    }

    public List<MotivoMovimiento> listarMotivosMovimiento() {
        return motivoMovimientoRepository.findAll();
    }

    public List<Movimiento> listarMovimientosConFiltros(String sedeId, String tipoId, String buscar,
                                                          LocalDate desde, LocalDate hasta) {
        return movimientoRepository.findByFilters(sedeId, tipoId, buscar, desde, hasta);
    }

    public List<Lote> listarLotesConProducto(String sedeId) {
        return loteRepository.findBySede(sedeId);
    }

    public List<Lote> listarLotesFefo(String sedeId, String productoId) {
        return loteRepository.findFefo(sedeId, productoId);
    }

    public List<StockCriticoDTO> obtenerStockCritico(String sedeId) {
        return calcularStockUseCase.calcularStockCritico(sedeId, 10);
    }

    public void registrarMovimiento(Lote lote, String usuarioId, String tipoId, String motivoId,
                                     int cantidad, String observacion) {
        registrarMovimientoUseCase.registrarMovimiento(lote, usuarioId, tipoId, motivoId, cantidad, observacion);
    }
}
