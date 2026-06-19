package com.utp.meditrackapp.domain.ports.out;

import java.io.File;
import java.util.Map;

/**
 * Puerto de salida para generación de reportes PDF.
 * Centraliza la lógica que estaba en HtmlReportService.
 */
public interface ReportService {

    /**
     * Genera un reporte PDF a partir de una plantilla HTML y variables.
     *
     * @param templateName Nombre de la plantilla Thymeleaf (sin extensión)
     * @param variables    Variables para la plantilla
     * @param outputFile   Archivo de salida PDF
     */
    void generarPdf(String templateName, Map<String, Object> variables, File outputFile) throws Exception;

    /**
     * Genera un gráfico de barras en Base64.
     *
     * @param data   Mapa de categorías a valores
     * @param title  Título del gráfico
     * @param xLabel Etiqueta del eje X
     * @param yLabel Etiqueta del eje Y
     * @return String en formato data:image/png;base64,...
     */
    String generarGraficoBarras(Map<String, Double> data, String title, String xLabel, String yLabel) throws Exception;

    /**
     * Genera un gráfico circular en Base64.
     *
     * @param data  Mapa de categorías a valores
     * @param title Título del gráfico
     * @return String en formato data:image/png;base64,...
     */
    String generarGraficoCircular(Map<String, Double> data, String title) throws Exception;
}
