package com.utp.meditrackapp.features.dashboard.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.entity.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

import com.utp.meditrackapp.features.dashboard.Dao.DashboardDao;
import com.utp.meditrackapp.features.dashboard.service.HtmlReportService;
import com.utp.meditrackapp.features.dashboard.models.MedicamentoResumen;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    private final DashboardDao dashboardDao = new DashboardDao();
    private final HtmlReportService reportService = new HtmlReportService();

    @FXML private BorderPane rootPane;
    @FXML private Label welcomeLabel;
    
    // ... rest of @FXML fields

    @FXML
    protected void onGoToReports() {
        try {
            NavigationService.toReports();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        Usuario user = SessionManager.getInstance().getCurrentUser();
        if (welcomeLabel != null) {
            String name = (user != null) ? user.getNombres() : "Usuario";
            welcomeLabel.setText("¡Bienvenido de nuevo, " + name + "!");
        }
        
        if (rootPane != null && rootPane.getParent() instanceof StackPane) {
            StackPane parent = (StackPane) rootPane.getParent();
            rootPane.prefHeightProperty().bind(parent.heightProperty());
            rootPane.prefWidthProperty().bind(parent.widthProperty());
        }
    }

    @FXML
    protected void onGoToInventory() throws IOException {
        NavigationService.toInventory();
    }

    @FXML
    protected void onGoToAttentions() throws IOException {
        NavigationService.toAttention();
    }

    @FXML
    protected void onLogout() throws IOException {
        SessionManager.getInstance().logout();
        NavigationService.toLogin();
    }
}
