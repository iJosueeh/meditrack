package com.utp.meditrackapp.features.dashboard.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.utp.meditrackapp.core.config.PdfTemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Map;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.awt.Color;
import org.jfree.chart.plot.PiePlot;

import java.awt.GradientPaint;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;

public class HtmlReportService {
    private final TemplateEngine templateEngine = PdfTemplateEngine.getInstance();

    public void generatePdf(String templateName, Map<String, Object> variables, File outputFile) throws Exception {
        Context context = new Context();
        context.setVariables(variables);

        String htmlContent = templateEngine.process(templateName, context);

        try (OutputStream os = new FileOutputStream(outputFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, "/"); 
            builder.useSVGDrawer(new com.openhtmltopdf.svgsupport.BatikSVGDrawer());
            builder.toStream(os);
            builder.run();
        }
    }

    public String generateBarChartBase64(Map<String, Double> data, String title, String xLabel, String yLabel) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        if (data == null || data.isEmpty()) {
            dataset.addValue(0, "Sin Datos", "N/A");
        } else {
            data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> dataset.addValue(valueOrZero(e.getValue()), "Inventario", e.getKey()));
        }

        JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataset, PlotOrientation.VERTICAL, false, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(new Color(226, 232, 240)); 
        plot.setOutlineVisible(false);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, new GradientPaint(0f, 0f, new Color(2, 132, 199), 0f, 0f, new Color(56, 189, 248))); 

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelPaint(new Color(100, 116, 139)); 
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelPaint(new Color(100, 116, 139));

        return chartToBase64(chart, 480, 260);
    }

    public String generatePieChartBase64(Map<String, Double> data, String title) throws Exception {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        if (data == null || data.isEmpty()) {
            dataset.setValue("Sin Datos", 1.0);
        } else {
            data.forEach((k, v) -> dataset.setValue(k, valueOrZero(v)));
        }

        JFreeChart chart = ChartFactory.createPieChart(title, dataset, true, false, false);
        chart.setBackgroundPaint(Color.WHITE);
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        
        // Show labels with name, value and percentage
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})"));
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 0)); // Transparent
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelPaint(new Color(30, 41, 59)); // Slate-800
        
        Color[] colors = { new Color(30, 41, 59), new Color(2, 132, 199), new Color(16, 185, 129), new Color(245, 158, 11) };
        int i = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable)key, colors[i % colors.length]);
            i++;
        }

        return chartToBase64(chart, 380, 260);
    }

    private Double valueOrZero(Double v) { return v != null ? v : 0.0; }

    private String chartToBase64(JFreeChart chart, int width, int height) throws Exception {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(bos, chart, width, height);
            byte[] bytes = bos.toByteArray();
            String b64 = Base64.getEncoder().encodeToString(bytes);
            return "data:image/png;base64," + b64;
        }
    }
}
