package com.utp.meditrackapp.features.dashboard.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.infrastructure.adapters.ReportAdapter;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.time.LocalDate;

public class ReportsController {

    private final ReportAdapter reportAdapter = new ReportAdapter();
    private final SessionManager sessionManager = SessionManager.getInstance();

    @FXML private BorderPane rootPane;
    @FXML private DatePicker dpMovFrom, dpMovTo, dpDispFrom, dpDispTo;
    @FXML private Label lblTotalProducts, lblCriticalStock, lblExpiringSoon, lblHealthScore, lblInventoryValue, lblMovements;

    @javafx.fxml.FXML
    public void initialize() {
        loadKPIs();
    }

    private void loadKPIs() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            String sedeId = (user != null) ? user.getSedeId() : null;

            var kpis = reportAdapter.obtenerKpis(sedeId);

            if (lblTotalProducts != null) lblTotalProducts.setText(String.valueOf(kpis.totalProductos()));
            if (lblCriticalStock != null) lblCriticalStock.setText(String.valueOf(kpis.stockCritico()));
            if (lblExpiringSoon != null) lblExpiringSoon.setText(String.valueOf(kpis.lotesPorVencer()));
            if (lblHealthScore != null) lblHealthScore.setText(kpis.saludInventario() + "%");
            if (lblInventoryValue != null) lblInventoryValue.setText(String.format("S/ %,.2f", kpis.valorInventario()));
            if (lblMovements != null) lblMovements.setText(String.valueOf(kpis.volumenMovimientos()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onGenerateConsolidated() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte Consolidado");
            fileChooser.setInitialFileName("reporte_consolidado_" + LocalDate.now() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());

            if (file != null) {
                String sedeName = user.getSedeNombre() != null ? user.getSedeNombre() : "";
                reportAdapter.generarReporteConsolidado(
                    user.getSedeId(),
                    user.getNombres() + " " + user.getApellidos(),
                    sedeName,
                    file
                );
                showAlert("Reporte Generado", "El documento se ha guardado en:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) { showException(e); }
    }

    @FXML
    public void onGenerateMovements() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;
            
            LocalDate desde = dpMovFrom.getValue();
            LocalDate hasta = dpMovTo.getValue();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Movimientos");
            fileChooser.setInitialFileName("reporte_movimientos_" + LocalDate.now() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());

            if (file != null) {
                String sedeName = user.getSedeNombre() != null ? user.getSedeNombre() : "";
                boolean generated = reportAdapter.generarReporteMovimientos(
                    user.getSedeId(),
                    user.getNombres() + " " + user.getApellidos(),
                    sedeName,
                    desde, hasta,
                    file
                );
                
                if (generated) {
                    showAlert("Reporte Generado", "El documento se ha guardado en:\n" + file.getAbsolutePath());
                } else {
                    showAlert("Sin Datos", "No hay movimientos en el rango seleccionado.");
                }
            }
        } catch (Exception e) { showException(e); }
    }

    @FXML
    public void onGenerateDispensations() {
        try {
            Usuario user = sessionManager.getCurrentUser();
            if (user == null) return;
            
            LocalDate desde = dpDispFrom.getValue();
            LocalDate hasta = dpDispTo.getValue();

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Dispensaciones");
            fileChooser.setInitialFileName("reporte_dispensaciones_" + LocalDate.now() + ".pdf");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());

            if (file != null) {
                String sedeName = user.getSedeNombre() != null ? user.getSedeNombre() : "";
                boolean generated = reportAdapter.generarReporteDispensaciones(
                    user.getSedeId(),
                    user.getNombres() + " " + user.getApellidos(),
                    sedeName,
                    desde, hasta,
                    file
                );
                
                if (generated) {
                    showAlert("Reporte Generado", "El documento se ha guardado en:\n" + file.getAbsolutePath());
                } else {
                    showAlert("Sin Datos", "No hay dispensaciones en el rango seleccionado.");
                }
            }
        } catch (Exception e) { showException(e); }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(msg); alert.showAndWait();
    }

    private void showException(Exception e) {
        e.printStackTrace();
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Reporte"); alert.setContentText(e.getMessage()); alert.showAndWait();
    }
}
