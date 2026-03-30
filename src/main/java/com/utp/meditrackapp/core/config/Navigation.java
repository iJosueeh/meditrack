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
        try {
            String fxmlPath = "/com/utp/meditrackapp/features/" + featureName + "/" + featureName.toLowerCase() + "-view.fxml";

            FXMLLoader loader = new FXMLLoader(Navigation.class.getResource(fxmlPath));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException error) {
            System.err.println("Error: No se pudo cargar la feature '" + featureName + "'");
            error.printStackTrace();
        } catch (NullPointerException error) {
            System.err.println("Error: No se encontró el archivo FXML en la ruta especificada.");
        }
    }


}
