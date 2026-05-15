package com.utp.meditrackapp.tools;

import com.utp.meditrackapp.features.dashboard.Dao.DashboardDao;
import com.utp.meditrackapp.features.dashboard.models.MedicamentoResumen;
import com.utp.meditrackapp.features.dashboard.service.ReportService;
import com.utp.meditrackapp.features.dashboard.service.SeedDataLoader;
import net.sf.jasperreports.engine.JRException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGeneratorRunner {
    public static void main(String[] args) {
        try {
            SeedDataLoader seed = new SeedDataLoader();
            boolean seeded = seed.ensureSeedData(6, 6); // umbrales mínimos
            System.out.println("Seed result: " + seeded);

            DashboardDao dao = new DashboardDao();
            Map<String,Object> params = new HashMap<>();
            params.put("reportDate", java.time.LocalDate.now().toString());
            params.put("generatedBy", System.getProperty("user.name"));
            params.put("site", "Sede Hospital Central");
            params.put("valorInventario", dao.getInventoryValue());
            params.put("alertasStock", dao.getStockCriticoCount(10));
            params.put("volumenMovimientos", dao.getMovementsVolume(30));
            params.put("eficienciaOperativa", dao.getSaludInventario());

            List<MedicamentoResumen> items = dao.getTopBajoStock();
            List<?> trend = dao.getInventoryTrendMonths(6);
            List<?> categories = dao.getCategoryDistribution();

            ReportService svc = new ReportService();
            File out = new File("target/meditrack_report_sample.pdf");
            svc.generateDashboardPdf(params, items, trend, categories, out);
            System.out.println("Reporte generado en: " + out.getAbsolutePath());
        } catch (JRException e) {
            System.err.println("Error Jasper: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error general: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
