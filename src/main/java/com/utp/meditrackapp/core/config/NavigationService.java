package com.utp.meditrackapp.core.config;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Navigation service to handle view transitions.
 * All navigation methods validate permissions before switching views.
 */
public class NavigationService {

    private static Stage primaryStage;
    private static boolean darkThemeEnabled = isDarkThemeConfigured();

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    private static void switchRoot(String fxmlPath, String title, boolean maximize) throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource(fxmlPath));
        Parent root = loader.load();

        if (primaryStage.getScene() == null) {
            Scene scene = new Scene(root, 1100, 750);
            applyTheme(scene);
            primaryStage.setScene(scene);
        } else {
            primaryStage.getScene().setRoot(root);
        }

        primaryStage.setTitle(title);

        if (maximize) {
            if (!primaryStage.isMaximized()) {
                primaryStage.setMaximized(true);
            }
        } else {
            primaryStage.setMaximized(false);
            primaryStage.setWidth(1100);
            primaryStage.setHeight(750);
            primaryStage.centerOnScreen();
        }
    }

    /**
     * Verifica si el usuario actual tiene un permiso antes de navegar.
     * Si no tiene permiso, redirige al dashboard.
     */
    private static boolean checkAndNavigate(String permiso, String fxmlPath, String title, boolean maximize) throws IOException {
        SessionManager session = SessionManager.getInstance();
        if (!session.tienePermiso(permiso)) {
            switchRoot("/com/utp/meditrackapp/dashboard-view.fxml", "MediTrack — Panel de Control", true);
            return false;
        }
        switchRoot(fxmlPath, title, maximize);
        return true;
    }

    /**
     * Load and navigate to login view.
     */
    public static void toLogin() throws IOException {
        switchRoot("/com/utp/meditrackapp/login-view.fxml", "MediTrack — Iniciar Sesión", false);
    }

    /**
     * Load and navigate to dashboard view.
     */
    public static void toDashboard() throws IOException {
        switchRoot("/com/utp/meditrackapp/dashboard-view.fxml", "MediTrack — Panel de Control", true);
    }

    /**
     * Load and navigate to site management view. Requires M2_SEDES.
     */
    public static void toSedes() throws IOException {
        checkAndNavigate("M2_SEDES", "/com/utp/meditrackapp/sede-view.fxml", "MediTrack — Gestión de Sedes", true);
    }

    public static void toProfile() throws IOException {
        switchRoot("/com/utp/meditrackapp/profile-view.fxml", "MediTrack — Mi Perfil", true);
    }

    private static String patientInitialSearch;

    public static void toPatients() throws IOException {
        toPatients(null);
    }

    /**
     * Load and navigate to patients view. Requires M7_PACIENTES.
     */
    public static void toPatients(String initialSearch) throws IOException {
        patientInitialSearch = initialSearch;
        checkAndNavigate("M7_PACIENTES", "/com/utp/meditrackapp/pacientes-view.fxml", "MediTrack — Gestión de Pacientes", true);
    }

    public static String getPatientInitialSearch() {
        String search = patientInitialSearch;
        patientInitialSearch = null;
        return search;
    }

    /**
     * Load and navigate to attentions view. Requires M8_ATENCIONES.
     */
    public static void toAttention() throws IOException {
        checkAndNavigate("M8_ATENCIONES", "/com/utp/meditrackapp/atencion-view.fxml", "MediTrack — Registro de Atenciones", true);
    }

    /**
     * Load and navigate to reports view. Requires M10_REPORTES.
     */
    public static void toReports() throws IOException {
        checkAndNavigate("M10_REPORTES", "/com/utp/meditrackapp/reports-view.fxml", "MediTrack — Centro de Reportes", true);
    }

    private static String inventoryInitialSearch;

    public static void toInventory() throws IOException {
        toInventory(null);
    }

    /**
     * Load and navigate to inventory view. Requires M4_LOTES.
     */
    public static void toInventory(String initialSearch) throws IOException {
        inventoryInitialSearch = initialSearch;
        checkAndNavigate("M4_LOTES", "/com/utp/meditrackapp/inventory-view.fxml", "MediTrack — Inventario", true);
    }

    public static String getInventoryInitialSearch() {
        String search = inventoryInitialSearch;
        inventoryInitialSearch = null;
        return search;
    }

    private static boolean isDarkThemeConfigured() {
        String prop = System.getProperty("app.theme", "");
        String env = System.getenv("APP_THEME");
        return "dark".equalsIgnoreCase(prop) || "dark".equalsIgnoreCase(env);
    }

    public static void setDarkThemeEnabled(boolean enabled) {
        darkThemeEnabled = enabled;
        System.setProperty("app.theme", enabled ? "dark" : "light");
        if (primaryStage != null && primaryStage.getScene() != null) {
            applyTheme(primaryStage.getScene());
        }
    }

    public static boolean isDarkThemeEnabled() {
        return darkThemeEnabled;
    }

    private static void applyTheme(Scene scene) {
        if (darkThemeEnabled) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        }
    }

    /**
     * Load and navigate to users view. Requires USUARIOS.
     */
    public static void toUsers() throws IOException {
        checkAndNavigate("USUARIOS", "/com/utp/meditrackapp/users-view.fxml", "MediTrack — Gestión de Usuarios", true);
    }

    /**
     * Load and navigate to categories view. Requires CATEGORIAS.
     */
    public static void toCategorias() throws IOException {
        checkAndNavigate("CATEGORIAS", "/com/utp/meditrackapp/categorias-view.fxml", "MediTrack — Gestión de Categorías", true);
    }

    /**
     * Load and navigate to roles view. Requires ROLES.
     */
    public static void toRoles() throws IOException {
        checkAndNavigate("ROLES", "/com/utp/meditrackapp/roles-view.fxml", "MediTrack — Gestión de Roles", true);
    }

    /**
     * Load and navigate to movement catalogs view. Requires MOV_CATALOGOS.
     */
    public static void toCatalogosMovimiento() throws IOException {
        checkAndNavigate("MOV_CATALOGOS", "/com/utp/meditrackapp/catalogos-mov-view.fxml", "MediTrack — Catálogos de Movimiento", true);
    }

    private static String productInitialSearch;

    public static void toProductos() throws IOException {
        toProductos(null);
    }

    /**
     * Load and navigate to products view. Requires M3_PRODUCTOS.
     */
    public static void toProductos(String initialSearch) throws IOException {
        productInitialSearch = initialSearch;
        checkAndNavigate("M3_PRODUCTOS", "/com/utp/meditrackapp/productos-view.fxml", "MediTrack — Catálogo de Productos", true);
    }

    public static String getProductInitialSearch() {
        String search = productInitialSearch;
        productInitialSearch = null;
        return search;
    }
}
