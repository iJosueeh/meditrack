package com.utp.meditrackapp.core.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Navigation service to handle view transitions.
 * TODO: Integrate with session/auth service to manage state between views.
 */
public class NavigationService {

    private static Stage primaryStage;
    private static Scene loginScene;
    private static Scene dashboardScene;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Load and navigate to login view.
     */
    public static void toLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource("/com/utp/meditrackapp/login-view.fxml"));
        loginScene = new Scene(loader.load(), 1000, 700);
        
        if (primaryStage != null) {
            primaryStage.setScene(loginScene);
            primaryStage.setTitle("MediTrack — Iniciar Sesión");
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
        }
    }

    /**
     * Load and navigate to dashboard view.
     */
    public static void toDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(NavigationService.class.getResource("/com/utp/meditrackapp/dashboard-view.fxml"));
        dashboardScene = new Scene(loader.load());
        
        if (primaryStage != null) {
            primaryStage.setScene(dashboardScene);
            primaryStage.setTitle("MediTrack — Panel de Control");
            primaryStage.setMaximized(true);
        }
    }

    /**
     * TODO: Add methods for other views (Atención, Inventario, Catálogo, Usuarios)
     */
    public static void toAttention() throws IOException {
        // TODO: Load attention-view.fxml
    }

    public static void toInventory() throws IOException {
        // TODO: Load inventory-view.fxml
    }

    public static void toCatalog() throws IOException {
        // TODO: Load catalog-view.fxml
    }

    public static void toUsers() throws IOException {
        // TODO: Load users-view.fxml
    }
}
