package com.utp.meditrackapp.features.dashboard.service;

import com.utp.meditrackapp.features.dashboard.models.MedicamentoResumen;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ReportService {
    private JasperReport compiledReport;

    public ReportService() throws JRException {
        try (InputStream jrxml = getClass().getResourceAsStream("/reports/mediTrack_reporte.jrxml")) {
            if (jrxml == null) throw new JRException("No se encontró la plantilla JRXML en /reports/mediTrack_reporte.jrxml");
            this.compiledReport = JasperCompileManager.compileReport(jrxml);
        } catch (Exception e) {
            throw new JRException("Error compilando plantilla JRXML", e);
        }
    }

    public void generateDashboardPdf(Map<String, Object> params, List<MedicamentoResumen> topProducts, List<?> trend, List<?> categories, File outFile) throws JRException {
        // Preparar DataSources para los charts
        params.put("DS_TENDENCIAS", new JRBeanCollectionDataSource(trend));
        params.put("DS_DISTRIBUCION", new JRBeanCollectionDataSource(categories));

        // DataSource principal (Tabla de productos)
        JRBeanCollectionDataSource mainDs = new JRBeanCollectionDataSource(topProducts);

        // Llenar y exportar
        JasperPrint jp = JasperFillManager.fillReport(compiledReport, params, mainDs);
        JasperExportManager.exportReportToPdfFile(jp, outFile.getAbsolutePath());
    }
}
