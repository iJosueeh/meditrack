package com.utp.meditrackapp.core;

import com.utp.meditrackapp.core.config.NavigationService;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        NavigationService.setPrimaryStage(stage);
        stage.setWidth(1000);
        stage.setHeight(700);
        stage.show();
        
        // TODO: Check if user is logged in; if so, go to dashboard; otherwise, go to login
        NavigationService.toLogin();
    }
}
