package com.utp.meditrackapp.features.auth.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Hyperlink;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField dniField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button toggleViewBtn;

    @FXML
    private FontIcon toggleIcon;

    private boolean isPasswordVisible = false;

    @FXML
    protected void onTogglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordTextField.setVisible(false);
            toggleIcon.setIconLiteral("fas-eye");
            isPasswordVisible = false;
        } else {
            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordField.setVisible(false);
            toggleIcon.setIconLiteral("fas-eye-slash");
            isPasswordVisible = true;
        }
    }

    @FXML
    protected void onForgotPassword() {
        // TODO: Implementar recuperación de contraseña
    }

    @FXML
    protected void onLogin() throws IOException {
        // Redirección directa al Dashboard por ahora según lo solicitado
        NavigationService.toDashboard();
    }
}
