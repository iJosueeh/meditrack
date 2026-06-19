package com.utp.meditrackapp.domain.services.reports;

import com.utp.meditrackapp.domain.ports.out.DispensacionRepository;
import com.utp.meditrackapp.domain.ports.out.ReportService;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Caso de uso: Generar reporte de dispensaciones.
 * Centraliza la lógica que estaba en ReportsController.onGenerateDispensations().
 */
public class GenerarReporteDispensacionesUseCase {
    private final DispensacionRepository dispensacionRepository;
    private final ReportService reportService;

    public GenerarReporteDispensacionesUseCase(DispensacionRepository dispensacionRepository,
                                                ReportService reportService) {
        this.dispensacionRepository = dispensacionRepository;
        this.reportService = reportService;
    }

    /**
     * Genera el reporte de dispensaciones y lo guarda en el archivo especificado.
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
        List<Map<String, Object>> items = dispensacionRepository.findByRangoFechas(sedeId, desde, hasta);

        if (items.isEmpty()) {
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

        // Calcular totales
        long totalAtenciones = items.stream()
                .map(m -> m.get("numeroReceta"))
                .distinct()
                .count();
        int totalMedicamentos = items.stream()
                .mapToInt(m -> ((Number) m.get("cantidad")).intValue())
                .sum();

        vars.put("TOTAL_ATTENTIONS", String.valueOf(totalAtenciones));
        vars.put("TOTAL_MEDICINES", String.valueOf(totalMedicamentos));
        vars.put("items", items);

        reportService.generarPdf("dispensaciones", vars, outputFile);
        return true;
    }
}
