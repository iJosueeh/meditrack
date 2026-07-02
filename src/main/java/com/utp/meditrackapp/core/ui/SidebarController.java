package com.utp.meditrackapp.core.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.domain.entities.Usuario;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class SidebarController {

    @FXML private Label userNameLabel;
    @FXML private Label userSedeLabel;
    
    // Operaciones
    @FXML private Button btnDashboard;
    @FXML private Button btnAttentions;
    @FXML private Button btnInventory;
    
    // Mantenimiento
    @FXML private Button btnPatients;
    @FXML private Button btnCatalog;
    @FXML private Button btnUsers;
    @FXML private Button btnCategorias;
    @FXML private Button btnProductos;
    @FXML private Button btnRoles;
    @FXML private Button btnMovCatalog;
    
    // Sistema
    @FXML private Button btnReports;

    // Section headers and spacers
    @FXML private Label lblOperaciones;
    @FXML private Region sepOpRegion;
    @FXML private Label lblMantenimiento;
    @FXML private Region sepManRegion;
    @FXML private Label lblSistema;

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
        
        // === OPERACIONES ===
        boolean hasDashboard = session.tienePermiso("M10_REPORTES");
        boolean hasAttentions = session.tienePermiso("M8_ATENCIONES");
        boolean hasInventory = session.tienePermiso("M4_LOTES");
        
        if (!hasDashboard) hideButton(btnDashboard);
        if (!hasAttentions) hideButton(btnAttentions);
        if (!hasInventory) hideButton(btnInventory);
        
        boolean hasAnyOperacion = hasDashboard || hasAttentions || hasInventory;
        if (!hasAnyOperacion) hideSection(lblOperaciones, sepOpRegion);
        
        // === MANTENIMIENTO ===
        boolean hasSedes = session.tienePermiso("M2_SEDES");
        boolean hasPacientes = session.tienePermiso("M7_PACIENTES");
        boolean hasUsuarios = session.tienePermiso("USUARIOS");
        boolean hasCategorias = session.tienePermiso("CATEGORIAS");
        boolean hasProductos = session.tienePermiso("M3_PRODUCTOS");
        boolean hasRoles = session.tienePermiso("ROLES");
        boolean hasMovCatalog = session.tienePermiso("MOV_CATALOGOS");
        
        if (!hasPacientes) hideButton(btnPatients);
        if (!hasSedes) hideButton(btnCatalog);
        if (!hasUsuarios) hideButton(btnUsers);
        if (!hasCategorias) hideButton(btnCategorias);
        if (!hasProductos) hideButton(btnProductos);
        if (!hasRoles) hideButton(btnRoles);
        if (!hasMovCatalog) hideButton(btnMovCatalog);
        
        boolean hasAnyMantenimiento = hasSedes || hasPacientes || hasUsuarios 
            || hasCategorias || hasProductos || hasRoles || hasMovCatalog;
        if (!hasAnyMantenimiento) hideSection(lblMantenimiento, sepManRegion);
        
        // === SISTEMA ===
        boolean hasReportes = session.tienePermiso("M10_REPORTES");
        
        if (!hasReportes) hideButton(btnReports);
    }

    private void hideButton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false);
        }
    }

    private void hideSection(Label label, Region spacer) {
        if (label != null) {
            label.setVisible(false);
            label.setManaged(false);
        }
        if (spacer != null) {
            spacer.setVisible(false);
            spacer.setManaged(false);
        }
    }

    @FXML
    protected void onGoToDashboard() throws IOException {
        if (!checkPermission("M10_REPORTES")) return;
        NavigationService.toDashboard();
    }

    @FXML
    protected void onGoToPatients() throws IOException {
        if (!checkPermission("M7_PACIENTES")) return;
        NavigationService.toPatients();
    }

    @FXML
    protected void onGoToAttentions() throws IOException {
        if (!checkPermission("M8_ATENCIONES")) return;
        NavigationService.toAttention();
    }

    @FXML
    protected void onGoToInventory() throws IOException {
        if (!checkPermission("M4_LOTES")) return;
        NavigationService.toInventory();
    }

    @FXML
    protected void onGoToCatalog() throws IOException {
        if (!checkPermission("M2_SEDES")) return;
        NavigationService.toSedes();
    }

    @FXML
    protected void onGoToUsers() throws IOException {
        if (!checkPermission("USUARIOS")) return;
        NavigationService.toUsers();
    }

    @FXML
    protected void onGoToCategorias() throws IOException {
        if (!checkPermission("CATEGORIAS")) return;
        NavigationService.toCategorias();
    }

    @FXML
    protected void onGoToProductos() throws IOException {
        if (!checkPermission("M3_PRODUCTOS")) return;
        NavigationService.toProductos();
    }

    @FXML
    protected void onGoToRoles() throws IOException {
        if (!checkPermission("ROLES")) return;
        NavigationService.toRoles();
    }

    @FXML
    protected void onGoToMovCatalog() throws IOException {
        if (!checkPermission("MOV_CATALOGOS")) return;
        NavigationService.toCatalogosMovimiento();
    }

    @FXML
    protected void onGoToReports() throws IOException {
        if (!checkPermission("M10_REPORTES")) return;
        NavigationService.toReports();
    }

    @FXML
    protected void onGoToProfile() throws IOException {
        NavigationService.toProfile();
    }

    @FXML
    protected void onLogout() throws IOException {
        SessionManager.getInstance().logout();
        NavigationService.toLogin();
    }

    private boolean checkPermission(String permiso) {
        if (!SessionManager.getInstance().tienePermiso(permiso)) {
            return false;
        }
        return true;
    }
}
