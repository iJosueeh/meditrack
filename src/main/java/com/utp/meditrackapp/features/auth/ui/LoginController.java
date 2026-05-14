package com.utp.meditrackapp.features.auth.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Hyperlink;
import org.kordamp.ikonli.javafx.FontIcon;
import java.io.IOException;

import javafx.scene.control.Alert;
import com.utp.meditrackapp.features.auth.Dao.UsuarioDao;
import com.utp.meditrackapp.core.models.entity.Usuario;
import com.utp.meditrackapp.core.config.SessionManager;

public class LoginController {
    @FXML
    private UsuarioDao usuarioDao;
    public void Initialize(){
        try{
        usuarioDao = new UsuarioDao();
        } catch (Exception e){
            showAlert(Alert.AlertType.ERROR, "Error de Conexión", "No se pudo conectar a la base de datos.");
            throw new RuntimeException("Error al inicializar UsuarioDao: " + e.getMessage());
        }
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
        // TODO: Implementar recuperación de contraseña
    }

    @FXML
    protected void onLogin() throws IOException {
        String dni = dniField.getText();
        String password = isPasswordVisible ? passwordTextField.getText() : passwordField.getText();

        if(dni.isEmpty() || password.isEmpty()){
            showAlert(Alert.AlertType.WARNING, "Campos Vacíos", "Por favor, ingrese sus credenciales.");
            return;
        }

        Usuario usuario = usuarioDao.login(dni, password);
        if (usuario != null) {
            SessionManager.getInstance().login(usuario);
            try {
                NavigationService.toDashboard();
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo cargar el panel de control.");
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
