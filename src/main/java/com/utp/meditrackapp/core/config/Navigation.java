package com.utp.meditrackapp.core.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Navigation {
    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadFeature(String featureName) {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage has not been set. Call Navigation.setStage() before loading features.");
        }

        String fxmlPath = "/com/utp/meditrackapp/features/" + featureName + "/" + featureName.toLowerCase() + "-view.fxml";
        java.net.URL resource = Navigation.class.getResource(fxmlPath);
        if (resource == null) {
            throw new IllegalArgumentException("FXML resource not found at path: " + fxmlPath);
        }

        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException error) {
            System.err.println("Error: No se pudo cargar la feature '" + featureName + "'");
            error.printStackTrace();
        }
    }


}
