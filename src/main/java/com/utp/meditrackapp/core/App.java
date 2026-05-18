package com.utp.meditrackapp.core;

import com.utp.meditrackapp.core.config.NavigationService;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        NavigationService.setPrimaryStage(stage);
        
        // Iniciamos la ventana maximizada
        stage.setMaximized(true);
        stage.setTitle("MediTrack - Gestión de Inventario Médico");
        stage.show();
        
        NavigationService.toLogin();
    }
}
