package com.utp.meditrackapp.domain.services.reports;

import com.utp.meditrackapp.application.dto.DashboardKpiDTO;
import com.utp.meditrackapp.domain.ports.out.DashboardRepository;

/**
 * Caso de uso: Obtener KPIs del Dashboard.
 * Centraliza la lógica que estaba en ReportsController.loadKPIs().
 */
public class ObtenerKpisUseCase {
    private final DashboardRepository dashboardRepository;

    public ObtenerKpisUseCase(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    /**
     * Obtiene todos los KPIs del dashboard para una sede.
     *
     * @param sedeId ID de la sede (puede ser null para datos globales)
     * @return DTO con los KPIs calculados
     */
    public DashboardKpiDTO obtenerKpis(String sedeId) {
        int totalProductos = dashboardRepository.getStockCriticoCount(Integer.MAX_VALUE);
        int stockCritico = dashboardRepository.getStockCriticoCount(10);
        int lotesPorVencer = dashboardRepository.getLotesPorVencerCount(30);
        int saludInventario = dashboardRepository.getSaludInventario(sedeId);
        double valorInventario = dashboardRepository.getValorInventario();
        int volumenMovimientos = dashboardRepository.getVolumenMovimientos(30);

        return new DashboardKpiDTO(
            totalProductos,
            stockCritico,
            lotesPorVencer,
            saludInventario,
            valorInventario,
            volumenMovimientos
        );
    }
}
