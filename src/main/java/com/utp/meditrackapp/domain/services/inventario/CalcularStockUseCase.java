package com.utp.meditrackapp.domain.services.inventario;

import com.utp.meditrackapp.application.dto.StockCriticoDTO;
import com.utp.meditrackapp.domain.entities.Lote;
import com.utp.meditrackapp.domain.ports.out.LoteRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Caso de uso: Calcular y consultar stock.
 * Centraliza la lógica de cálculo que estaba dispersa en LoteDAO y InventarioService.
 */
public class CalcularStockUseCase {
    private final LoteRepository loteRepository;

    public CalcularStockUseCase(LoteRepository loteRepository) {
        this.loteRepository = loteRepository;
    }

    /**
     * Obtiene el stock total de un producto en una sede.
     */
    public int obtenerStockTotal(String sedeId, String productoId) {
        return loteRepository.findStockTotal(sedeId, productoId);
    }

    /**
     * Obtiene el mapa de stock por producto para una sede.
     */
    public Map<String, Integer> obtenerStockPorSede(String sedeId) {
        return loteRepository.findStockTotalBySede(sedeId);
    }

    /**
     * Obtiene lotes ordenados por FEFO (First Expire, First Out).
     */
    public List<Lote> obtenerLotesFefo(String sedeId, String productoId) {
        return loteRepository.findFefo(sedeId, productoId);
    }

    /**
     * Calcula el ítems de stock crítico para una sede.
     * Combina stock bajo y lotes por vencer.
     *
     * @param sedeId         ID de la sede
     * @param stockMinimo    Umbral de stock mínimo
     * @return Lista de ítems con stock crítico
     */
    public List<StockCriticoDTO> calcularStockCritico(String sedeId, int stockMinimo) {
        List<Lote> todosLotes = loteRepository.findBySede(sedeId);

        // Agrupar por producto
        Map<String, List<Lote>> lotesPorProducto = todosLotes.stream()
                .filter(l -> l.getCantidad() > 0)
                .collect(Collectors.groupingBy(Lote::getProductoId));

        return lotesPorProducto.entrySet().stream()
                .map(entry -> buildStockCritico(entry.getKey(), entry.getValue(), stockMinimo))
                .filter(item -> item.isCritico())
                .sorted(Comparator.comparingInt(StockCriticoDTO::getStockActual)
                        .thenComparing(StockCriticoDTO::getDiasParaVencer))
                .collect(Collectors.toList());
    }

    /**
     * Calcula la salud del inventario (porcentaje).
     *
     * @param sedeId         ID de la sede
     * @param stockMinimo    Umbral de stock mínimo
     * @return Porcentaje de salud (0-100)
     */
    public int calcularSaludInventario(String sedeId, int stockMinimo) {
        List<StockCriticoDTO> criticos = calcularStockCritico(sedeId, stockMinimo);
        List<Lote> todosLotes = loteRepository.findBySede(sedeId);

        int totalLotes = (int) todosLotes.stream().filter(l -> l.getCantidad() > 0).count();
        int lotesCriticos = criticos.size();

        if (totalLotes == 0) return 100;

        int salud = (int) ((1.0 - (double) lotesCriticos / totalLotes) * 100);
        return Math.max(0, Math.min(100, salud));
    }

    private StockCriticoDTO buildStockCritico(String productoId, List<Lote> lotes, int stockMinimo) {
        int stockActual = lotes.stream().mapToInt(Lote::getCantidad).sum();

        LocalDate fechaVencimientoMinima = lotes.stream()
                .map(Lote::getFechaVencimiento)
                .filter(f -> f != null)
                .min(LocalDate::compareTo)
                .orElse(null);

        long diasParaVencer = fechaVencimientoMinima != null ?
                ChronoUnit.DAYS.between(LocalDate.now(), fechaVencimientoMinima) : Long.MAX_VALUE;

        Lote primerLote = lotes.get(0);

        return new StockCriticoDTO(
                productoId,
                primerLote.getCodigoDigemid(),
                primerLote.getProductoNombre(),
                "",  // categoriaNombre se puede resolver si se necesita
                stockActual,
                stockMinimo,
                fechaVencimientoMinima,
                (int) diasParaVencer
        );
    }
}
