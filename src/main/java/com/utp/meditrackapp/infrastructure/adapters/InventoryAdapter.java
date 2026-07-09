package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.application.config.TransactionManager;
import com.utp.meditrackapp.application.dto.StockCriticoDTO;
import com.utp.meditrackapp.core.cache.ReferenceCacheManager;
import com.utp.meditrackapp.core.cache.ReferenceCacheManager.CacheType;
import com.utp.meditrackapp.domain.entities.Lote;
import com.utp.meditrackapp.domain.entities.Movimiento;
import com.utp.meditrackapp.domain.entities.Producto;
import com.utp.meditrackapp.domain.ports.out.LoteRepository;
import com.utp.meditrackapp.domain.ports.out.MovimientoRepository;
import com.utp.meditrackapp.domain.services.inventario.AnularMovimientoUseCase;
import com.utp.meditrackapp.domain.services.inventario.CalcularStockUseCase;
import com.utp.meditrackapp.domain.services.inventario.EditarMovimientoUseCase;
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

public class InventoryAdapter {
    private final JdbcProductoRepository productoRepository;
    private final JdbcTipoMovimientoRepository tipoMovimientoRepository;
    private final JdbcMotivoMovimientoRepository motivoMovimientoRepository;
    private final JdbcMovimientoRepository movimientoRepository;
    private final LoteRepository loteRepository;
    private final CalcularStockUseCase calcularStockUseCase;
    private final RegistrarMovimientoUseCase registrarMovimientoUseCase;
    private final AnularMovimientoUseCase anularMovimientoUseCase;
    private final EditarMovimientoUseCase editarMovimientoUseCase;
    private final ReferenceCacheManager cache = ReferenceCacheManager.getInstance();

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
        this.anularMovimientoUseCase = new AnularMovimientoUseCase(
            new JdbcLoteRepository(),
            new JdbcMovimientoRepository(),
            new TransactionManager()
        );
        this.editarMovimientoUseCase = new EditarMovimientoUseCase(
            new JdbcLoteRepository(),
            new JdbcMovimientoRepository(),
            new TransactionManager()
        );
    }

    public List<Producto> listarProductosActivos() {
        return cache.get(CacheType.PRODUCTOS, () -> productoRepository.findActivos());
    }

    public List<TipoMovimiento> listarTiposMovimiento() {
        return cache.get(CacheType.TIPOS_MOVIMIENTO, () -> tipoMovimientoRepository.findAll());
    }

    public List<MotivoMovimiento> listarMotivosMovimiento() {
        return cache.get(CacheType.MOTIVOS_MOVIMIENTO, () -> motivoMovimientoRepository.findAll());
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

    public boolean existeLote(String numeroLote, String productoId, String sedeId) {
        return loteRepository.existsByNumeroLoteProductoSede(numeroLote, productoId, sedeId);
    }

    public List<StockCriticoDTO> obtenerStockCritico(String sedeId) {
        return calcularStockUseCase.calcularStockCritico(sedeId, 10);
    }

    public void registrarMovimiento(Lote lote, String usuarioId, String tipoId, String motivoId,
                                     int cantidad, String observacion) {
        registrarMovimientoUseCase.registrarMovimiento(lote, usuarioId, tipoId, motivoId, cantidad, observacion);
    }

    public void anularMovimiento(Movimiento movimiento) {
        anularMovimientoUseCase.anular(movimiento);
    }

    public void editarMovimiento(Movimiento original, String nuevoTipoId, String nuevoMotivoId,
                                  int nuevaCantidad, String nuevaObservacion) {
        editarMovimientoUseCase.editar(original, nuevoTipoId, nuevoMotivoId, nuevaCantidad, nuevaObservacion);
    }

    public void actualizarObservacion(String movimientoId, String observacion) {
        var tx = new TransactionManager();
        try {
            tx.execute(conn -> movimientoRepository.updateObservacion(conn, movimientoId, observacion));
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error al actualizar observación: " + e.getMessage(), e);
        }
    }

    public void invalidateCatalogCache() {
        cache.invalidate(CacheType.TIPOS_MOVIMIENTO, CacheType.MOTIVOS_MOVIMIENTO);
    }

    public void invalidateProductCache() {
        cache.invalidate(CacheType.PRODUCTOS);
    }
}
