package com.utp.meditrackapp.features.auth.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.IOException;

import com.utp.meditrackapp.infrastructure.adapters.AuthAdapter;

public class LoginController {

    private AuthAdapter authAdapter;

    @FXML
    public void initialize() {
        authAdapter = new AuthAdapter();
    }

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
        // Implementación futura: Recuperación de contraseña
    }

    @FXML
    protected void onDniAction() {
        if (isPasswordVisible) {
            passwordTextField.requestFocus();
        } else {
            passwordField.requestFocus();
        }
    }

    @FXML
    protected void onLogin() throws IOException {
        String dni = dniField.getText();
        String password = isPasswordVisible ? passwordTextField.getText() : passwordField.getText();

        if (dni == null || dni.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor, ingrese sus credenciales.");
            return;
        }

        if (authAdapter.authenticate(dni, password)) {
            try {
                NavigationService.toDashboard();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar el panel de control. Detalles: " + e.getMessage());
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error de Autenticación", "Número de documento o contraseña incorrectos.");
        }  
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
