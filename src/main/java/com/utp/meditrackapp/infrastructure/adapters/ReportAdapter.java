package com.utp.meditrackapp.infrastructure.adapters;

import com.utp.meditrackapp.application.dto.DashboardKpiDTO;
import com.utp.meditrackapp.domain.services.reports.*;
import com.utp.meditrackapp.infrastructure.persistence.jdbc.*;
import com.utp.meditrackapp.infrastructure.reports.HtmlPdfReportService;

import java.io.File;
import java.time.LocalDate;

/**
 * Adaptador que puentea los servicios de reportes antiguos con los nuevos UseCases.
 * Permite que los controladores existentes sigan funcionando mientras se migra gradualmente.
 */
public class ReportAdapter {
    private final ObtenerKpisUseCase obtenerKpisUseCase;
    private final GenerarReporteConsolidadoUseCase reporteConsolidadoUseCase;
    private final GenerarReporteMovimientosUseCase reporteMovimientosUseCase;
    private final GenerarReporteDispensacionesUseCase reporteDispensacionesUseCase;

    public ReportAdapter() {
        JdbcDashboardRepository dashboardRepo = new JdbcDashboardRepository();
        HtmlPdfReportService reportService = new HtmlPdfReportService();

        this.obtenerKpisUseCase = new ObtenerKpisUseCase(dashboardRepo);
        this.reporteConsolidadoUseCase = new GenerarReporteConsolidadoUseCase(dashboardRepo, reportService);
        this.reporteMovimientosUseCase = new GenerarReporteMovimientosUseCase(
            new JdbcMovimientoRepository(), reportService);
        this.reporteDispensacionesUseCase = new GenerarReporteDispensacionesUseCase(
            new JdbcDispensacionRepository(), reportService);
    }

    /**
     * Obtiene los KPIs del dashboard.
     */
    public DashboardKpiDTO obtenerKpis(String sedeId) {
        return obtenerKpisUseCase.obtenerKpis(sedeId);
    }

    /**
     * Genera el reporte consolidado del dashboard.
     */
    public void generarReporteConsolidado(String sedeId, String usuarioName, String sedeName,
                                           File outputFile) throws Exception {
        reporteConsolidadoUseCase.generar(sedeId, usuarioName, sedeName, outputFile);
    }

    /**
     * Genera el reporte de movimientos.
     *
     * @return true si se generó correctamente, false si no hay datos
     */
    public boolean generarReporteMovimientos(String sedeId, String usuarioName, String sedeName,
                                              LocalDate desde, LocalDate hasta,
                                              File outputFile) throws Exception {
        return reporteMovimientosUseCase.generar(sedeId, usuarioName, sedeName, desde, hasta, outputFile);
    }

    /**
     * Genera el reporte de dispensaciones.
     *
     * @return true si se generó correctamente, false si no hay datos
     */
    public boolean generarReporteDispensaciones(String sedeId, String usuarioName, String sedeName,
                                                 LocalDate desde, LocalDate hasta,
                                                 File outputFile) throws Exception {
        return reporteDispensacionesUseCase.generar(sedeId, usuarioName, sedeName, desde, hasta, outputFile);
    }
}
