package com.utp.meditrackapp.core.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import com.utp.meditrackapp.core.config.SessionManager;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class TopbarController {

    @FXML
    private FontIcon themeIcon;

    @FXML
    private void initialize() {
        if (themeIcon != null) {
            themeIcon.setIconLiteral(NavigationService.isDarkThemeEnabled() ? "fas-sun" : "fas-moon");
        }
    }

    @FXML
    protected void onToggleTheme() {
        Scene scene = themeIcon.getScene();
        if (scene == null) return;
        
        javafx.scene.Parent root = scene.getRoot();
        StackPane rootContainer;
        if (root instanceof StackPane) {
            rootContainer = (StackPane) root;
        } else {
            rootContainer = new StackPane();
            rootContainer.getChildren().add(root);
            scene.setRoot(rootContainer);
        }

        boolean currentlyDark = rootContainer.getStyleClass().contains("dark-theme") || root.getStyleClass().contains("dark-theme");
        Rectangle transitionOverlay = new Rectangle();
        transitionOverlay.setManaged(false);
        transitionOverlay.setMouseTransparent(true);
        transitionOverlay.setWidth(scene.getWidth());
        transitionOverlay.setHeight(scene.getHeight());
        transitionOverlay.widthProperty().bind(scene.widthProperty());
        transitionOverlay.heightProperty().bind(scene.heightProperty());
        transitionOverlay.setFill(currentlyDark ? Color.web("#f8fafc") : Color.web("#0f172a"));

        rootContainer.getChildren().add(transitionOverlay);

        Platform.runLater(() -> {
            if (currentlyDark) {
                rootContainer.getStyleClass().remove("dark-theme");
                if (root != rootContainer) {
                    root.getStyleClass().remove("dark-theme");
                }
                themeIcon.setIconLiteral("fas-moon");
                NavigationService.setDarkThemeEnabled(false);
            } else {
                rootContainer.getStyleClass().add("dark-theme");
                if (root != rootContainer) {
                    root.getStyleClass().add("dark-theme");
                }
                themeIcon.setIconLiteral("fas-sun");
                NavigationService.setDarkThemeEnabled(true);
            }

            FadeTransition fade = new FadeTransition(Duration.millis(260), transitionOverlay);
            fade.setFromValue(0.92);
            fade.setToValue(0.0);
            fade.setOnFinished(e -> {
                rootContainer.getChildren().remove(transitionOverlay);
                transitionOverlay.widthProperty().unbind();
                transitionOverlay.heightProperty().unbind();
            });
            fade.play();
        });
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
}
