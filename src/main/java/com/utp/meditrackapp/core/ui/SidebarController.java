package com.utp.meditrackapp.core.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.entity.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.io.IOException;

public class SidebarController {

    @FXML private Label userNameLabel;
    @FXML private Label userSedeLabel;
    
    @FXML private Button btnDashboard;
    @FXML private Button btnPatients;
    @FXML private Button btnAttentions;
    @FXML private Button btnInventory;
    @FXML private Button btnCatalog;
    @FXML private Button btnUsers;
    @FXML private Button btnCategorias;
    @FXML private Button btnProductos;
    @FXML private Button btnRoles;
    @FXML private Button btnMovCatalog;
    @FXML private Button btnReports;

    @FXML
    public void initialize() {
        Usuario user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getNombres() + " " + user.getApellidos());
            String sede = user.getSedeNombre();
            userSedeLabel.setText(sede != null ? sede : "Sin Sede");
            
            applyRolePermissions();
        }
    }

    private void applyRolePermissions() {
        SessionManager session = SessionManager.getInstance();
        
        // 1. Técnico de Farmacia — M7, M8, M9, M6 (salidas)
        // No tiene: M2 (sedes), M3 (productos), M5 (entradas), M10 (reportes), usuarios
        if (session.isTecnico()) {
            hideButton(btnCatalog);      // Sedes
            hideButton(btnUsers);        // Usuarios
            hideButton(btnReports);      // Reportes
            hideMaintenanceButtons();    // Categorias, Productos, Roles, Catálogos de Mov
        }
        
        // 2. Químico Farmacéutico (Jefe de Sede) — M2 parcial, M4, M5, M6, M7, M8, M9, M10
        // No tiene: M3 (catálogo productos — solo admin)
        if (session.isQuimico()) {
            hideButton(btnCatalog);      // Sedes (solo admin)
            hideButton(btnProductos);    // Catálogo productos (solo admin)
            hideButton(btnRoles);        // Roles (solo admin)
            hideButton(btnMovCatalog);   // Catálogos de movimiento (solo admin)
        }
        
        // 3. Administrador — acceso total, sin restricciones
    }

    private void hideButton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
    }

    private void hideMaintenanceButtons() {
        hideButton(btnCategorias);
        hideButton(btnProductos);
        hideButton(btnRoles);
        hideButton(btnMovCatalog);
    }

    @FXML
    protected void onGoToDashboard() throws IOException {
        NavigationService.toDashboard();
    }

    @FXML
    protected void onGoToPatients() throws IOException {
        NavigationService.toPatients();
    }

    @FXML
    protected void onGoToAttentions() throws IOException {
        NavigationService.toAttention();
        System.out.println("[NAV] Navegando a Atenciones...");
    }

    @FXML
    protected void onGoToInventory() throws IOException {
        NavigationService.toInventory();
        System.out.println("[NAV] Navegando a Inventario...");
    }

    @FXML
    protected void onGoToCatalog() throws IOException {
        NavigationService.toSedes();
        System.out.println("[NAV] Navegando a Gestión de Sedes...");
    }

    @FXML
    protected void onGoToUsers() throws IOException {
        NavigationService.toUsers();
    }

    @FXML
    protected void onGoToCategorias() throws IOException {
        NavigationService.toCategorias();
    }

    @FXML
    protected void onGoToProductos() throws IOException {
        NavigationService.toProductos();
    }

    @FXML
    protected void onGoToRoles() throws IOException {
        NavigationService.toRoles();
    }

    @FXML
    protected void onGoToMovCatalog() throws IOException {
        NavigationService.toCatalogosMovimiento();
    }

    @FXML
    protected void onGoToReports() throws IOException {
        NavigationService.toReports();
        System.out.println("[NAV] Navegando a Centro de Reportes...");
    }

    @FXML
    protected void onGoToProfile() throws IOException {
        NavigationService.toProfile();
    }

    @FXML
    protected void onLogout() throws IOException {
        com.utp.meditrackapp.core.config.SessionManager.getInstance().logout();
        NavigationService.toLogin();
    }
}
