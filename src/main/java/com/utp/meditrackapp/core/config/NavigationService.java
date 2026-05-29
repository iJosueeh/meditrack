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
 * Refactored to swap roots instead of scenes to prevent window resizing/flickering.
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
     * Load and navigate to site management view.
     */
    public static void toSedes() throws IOException {
        switchRoot("/com/utp/meditrackapp/sede-view.fxml", "MediTrack — Gestión de Sedes", true);
    }

    public static void toProfile() throws IOException {
        switchRoot("/com/utp/meditrackapp/profile-view.fxml", "MediTrack — Mi Perfil", true);
    }

    private static String patientInitialSearch;

    public static void toPatients() throws IOException {
        toPatients(null);
    }

    public static void toPatients(String initialSearch) throws IOException {
        patientInitialSearch = initialSearch;
        switchRoot("/com/utp/meditrackapp/pacientes-view.fxml", "MediTrack — Gestión de Pacientes", true);
    }

    public static String getPatientInitialSearch() {
        String search = patientInitialSearch;
        patientInitialSearch = null; // Clear after read
        return search;
    }

    public static void toAttention() throws IOException {
        switchRoot("/com/utp/meditrackapp/atencion-view.fxml", "MediTrack — Registro de Atenciones", true);
    }

    public static void toInventory() throws IOException {
        switchRoot("/com/utp/meditrackapp/inventory-view.fxml", "MediTrack — Inventario", true);
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

    public static void toCatalog() throws IOException {
        toSedes();
    }

    public static void toUsers() throws IOException {
        switchRoot("/com/utp/meditrackapp/users-view.fxml", "MediTrack — Gestión de Usuarios", true);
    }
}
