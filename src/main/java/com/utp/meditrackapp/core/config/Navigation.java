package com.utp.meditrackapp.core.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Navigation {
    private static Stage primaryStage;

    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    public static void loadFeature(String featureName) {
        try {
            String fxmlPath = "/com/utp/meditrackapp/features/" + featureName + "/" + featureName.toLowerCase() + "-view.fxml";

            URL resourceUrl = Navigation.class.getResource(fxmlPath);
            if (resourceUrl == null) {
                System.err.println("No se encontró el archivo FXML en la ruta: " + fxmlPath);
                return;
            }

            FXMLLoader loader = new FXMLLoader(resourceUrl);
            Parent root = loader.load();

            if (primaryStage == null) {
                System.err.println("[UI ERROR] El primaryStage es null. ¿Olvidaste llamar a Navigation.setStage() en App.java?");
                return;
            }

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException error) {
            System.err.println("[UI ERROR] Ocurrió un problema al cargar la vista de la feature '" + featureName + "'");
            error.printStackTrace();
        }
    }

}