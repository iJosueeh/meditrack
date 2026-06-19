package com.utp.meditrackapp.domain.services.reports;

import com.utp.meditrackapp.domain.entities.Movimiento;
import com.utp.meditrackapp.domain.ports.out.MovimientoRepository;
import com.utp.meditrackapp.domain.ports.out.ReportService;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso: Generar reporte de movimientos de inventario.
 * Centraliza la lógica que estaba en ReportsController.onGenerateMovements().
 */
public class GenerarReporteMovimientosUseCase {
    private final MovimientoRepository movimientoRepository;
    private final ReportService reportService;

    public GenerarReporteMovimientosUseCase(MovimientoRepository movimientoRepository,
                                             ReportService reportService) {
        this.movimientoRepository = movimientoRepository;
        this.reportService = reportService;
    }

    /**
     * Genera el reporte de movimientos y lo guarda en el archivo especificado.
     *
     * @param sedeId      ID de la sede del usuario
     * @param usuarioName Nombre completo del usuario
     * @param sedeName    Nombre de la sede
     * @param desde       Fecha de inicio (puede ser null)
     * @param hasta       Fecha de fin (puede ser null)
     * @param outputFile  Archivo de salida PDF
     * @return true si se generó correctamente, false si no hay datos
     */
    public boolean generar(String sedeId, String usuarioName, String sedeName,
                           LocalDate desde, LocalDate hasta, File outputFile) throws Exception {
        List<Movimiento> movements = movimientoRepository.findByFilters(sedeId, null, null, desde, hasta);

        if (movements.isEmpty()) {
            return false;
        }

        Map<String, Object> vars = new HashMap<>();
        vars.put("REPORT_DATE", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        vars.put("GENERATED_BY", usuarioName);
        vars.put("SEDE", sedeName);

        String period = (desde != null ? desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Inicio")
                      + " al "
                      + (hasta != null ? hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Hoy");
        vars.put("DATE_RANGE", period);

        vars.put("TOTAL_MOVEMENTS", String.valueOf(movements.size()));
        vars.put("TOTAL_QUANTITY", String.valueOf(movements.stream().mapToInt(Movimiento::getCantidad).sum()));
        vars.put("items", movements);

        reportService.generarPdf("movimientos", vars, outputFile);
        return true;
    }
}
