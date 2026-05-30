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
    @FXML private Button btnMovements;
    @FXML private Button btnInventory;
    @FXML private Button btnCatalog;
    @FXML private Button btnUsers;
    @FXML private Button btnCategorias;
    @FXML private Button btnProductos;
    @FXML private Button btnRoles;
    @FXML private Button btnMovCatalog;

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
        
        // 1. Técnico de Farmacia (Operativo)
        if (session.isTecnico()) {
            btnMovements.setVisible(false);
            btnMovements.setManaged(false);
            btnCatalog.setVisible(false);
            btnCatalog.setManaged(false);
            btnUsers.setVisible(false);
            btnUsers.setManaged(false);
            hideMaintenanceButtons();
        }
        
        // 2. Químico Farmacéutico (Táctico)
        if (session.isQuimico()) {
            btnCatalog.setVisible(false);
            btnCatalog.setManaged(false);
            btnUsers.setVisible(false);
            btnUsers.setManaged(false);
            hideMaintenanceButtons();
        }
        
        // 3. Administrador (Estratégico)
        if (session.isAdmin()) {
            btnAttentions.setVisible(false);
            btnAttentions.setManaged(false);
            btnMovements.setVisible(false);
            btnMovements.setManaged(false);
        }
    }

    private void hideMaintenanceButtons() {
        btnCategorias.setVisible(false);
        btnCategorias.setManaged(false);
        btnProductos.setVisible(false);
        btnProductos.setManaged(false);
        btnRoles.setVisible(false);
        btnRoles.setManaged(false);
        btnMovCatalog.setVisible(false);
        btnMovCatalog.setManaged(false);
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
    protected void onGoToMovements() throws IOException {
        // NavigationService.toMovements();
        System.out.println("[NAV] Navegando a Movimientos...");
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
    protected void onGoToProfile() throws IOException {
        NavigationService.toProfile();
    }

    @FXML
    protected void onLogout() throws IOException {
        com.utp.meditrackapp.core.config.SessionManager.getInstance().logout();
        NavigationService.toLogin();
    }
}
