package com.utp.meditrackapp.features.profile.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.entity.Usuario;
import com.utp.meditrackapp.core.util.PasswordHasher;
import com.utp.meditrackapp.features.auth.Dao.UsuarioDao;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class ProfileController {

    @FXML private BorderPane profileRootPane;
    @FXML private Label fullNameLabel, fullNameLabelSmall, roleLabel, roleLabelSmall, sedeLabel, lastAccessValue, accountStatusValue;
    @FXML private TextField nombresField, apellidosField, tipoDocField, numDocField;

    // Edit Modal
    @FXML private VBox editModal;
    @FXML private TextField editNombresField, editApellidosField, editTipoDocField, editNumDocField;

    // Password Modal
    @FXML private VBox passwordModal;
    @FXML private PasswordField currentPasswordField, newPasswordField, confirmPasswordField;
    @FXML private TextField currentPasswordTextField, newPasswordTextField, confirmPasswordTextField;
    @FXML private FontIcon currentEyeIcon, newEyeIcon, confirmEyeIcon;

    private boolean isCurrentVisible = false, isNewVisible = false, isConfirmVisible = false;
    private final UsuarioDao usuarioDao = new UsuarioDao();

    @FXML
    public void initialize() {
        loadUserData();
    }

    private void loadUserData() {
        Usuario user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            String full = user.getNombres() + " " + user.getApellidos();
            String rol = user.getRolNombre() != null ? user.getRolNombre() : "Usuario";
            String sede = user.getSedeNombre() != null ? user.getSedeNombre() : "Sin Sede";
            
            fullNameLabel.setText(full);
            fullNameLabelSmall.setText(full);
            roleLabel.setText(rol);
            roleLabelSmall.setText(rol + " | " + sede);
            sedeLabel.setText(sede);
            
            nombresField.setText(user.getNombres());
            apellidosField.setText(user.getApellidos());
            tipoDocField.setText(user.getTipoDocumento());
            numDocField.setText(user.getNumeroDocumento());

            lastAccessValue.setText(usuarioDao.getUltimaActividad(user.getId()));
            accountStatusValue.setText(user.getIsActivo() == 1 ? "ACTIVO" : "INACTIVO");
        }
    }

    // --- Visibility Toggles ---
    @FXML protected void onToggleCurrentVisibility() {
        isCurrentVisible = toggleVisibility(currentPasswordField, currentPasswordTextField, currentEyeIcon, isCurrentVisible);
    }
    @FXML protected void onToggleNewVisibility() {
        isNewVisible = toggleVisibility(newPasswordField, newPasswordTextField, newEyeIcon, isNewVisible);
    }
    @FXML protected void onToggleConfirmVisibility() {
        isConfirmVisible = toggleVisibility(confirmPasswordField, confirmPasswordTextField, confirmEyeIcon, isConfirmVisible);
    }

    private boolean toggleVisibility(PasswordField pf, TextField tf, FontIcon icon, boolean visible) {
        if (visible) {
            pf.setText(tf.getText());
            pf.setVisible(true);
            tf.setVisible(false);
            icon.setIconLiteral("fas-eye");
        } else {
            tf.setText(pf.getText());
            tf.setVisible(true);
            pf.setVisible(false);
            icon.setIconLiteral("fas-eye-slash");
        }
        return !visible;
    }

    // --- Modal Actions ---
    @FXML protected void onOpenEditModal() {
        Usuario user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            editNombresField.setText(user.getNombres());
            editApellidosField.setText(user.getApellidos());
            editTipoDocField.setText(user.getTipoDocumento());
            editNumDocField.setText(user.getNumeroDocumento());
        }
        showModal(editModal); 
    }
    @FXML protected void onCloseEditModal() { hideModal(editModal); }

    @FXML
    protected void onSaveChanges() {
        Usuario user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            user.setNombres(editNombresField.getText());
            user.setApellidos(editApellidosField.getText());
            user.setTipoDocumento(editTipoDocField.getText());
            user.setNumeroDocumento(editNumDocField.getText());
            
            if (usuarioDao.updateUser(user)) {
                loadUserData();
                showAlert("Éxito", "Sus datos han sido actualizados correctamente.");
            } else {
                showAlert("Error", "No se pudo actualizar la información en la base de datos.");
            }
        }
        onCloseEditModal();
    }

    @FXML
    protected void onChangePassword() {
        resetPasswordFields();
        showModal(passwordModal);
    }

    @FXML protected void onClosePasswordModal() { hideModal(passwordModal); }

    @FXML
    protected void onUpdatePassword() {
        String current = isCurrentVisible ? currentPasswordTextField.getText() : currentPasswordField.getText();
        String newPass = isNewVisible ? newPasswordTextField.getText() : newPasswordField.getText();
        String confirm = isConfirmVisible ? confirmPasswordTextField.getText() : confirmPasswordField.getText();

        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showAlert("Campos requeridos", "Por favor complete todos los campos.");
            return;
        }

        Usuario user = SessionManager.getInstance().getCurrentUser();
        if (user == null) return;

        // Validar contraseña actual
        if (!PasswordHasher.checkPassword(current, user.getPassword())) {
            showAlert("Error de validación", "La contraseña actual es incorrecta.");
            return;
        }

        if (!newPass.equals(confirm)) {
            showAlert("Error de coincidencia", "La nueva contraseña y la confirmación no coinciden.");
            return;
        }

        // Hashing y Guardado
        String newHash = PasswordHasher.hashPassword(newPass);
        if (usuarioDao.updatePassword(user.getId(), newHash)) {
            user.setPassword(newHash); // Actualizar en sesión
            showAlert("Éxito", "Su contraseña ha sido actualizada correctamente.");
            onClosePasswordModal();
        } else {
            showAlert("Error", "No se pudo actualizar la contraseña en el servidor.");
        }
    }

    private void resetPasswordFields() {
        currentPasswordField.clear(); currentPasswordTextField.clear();
        newPasswordField.clear(); newPasswordTextField.clear();
        confirmPasswordField.clear(); confirmPasswordTextField.clear();
        
        currentPasswordField.setVisible(true); currentPasswordTextField.setVisible(false);
        newPasswordField.setVisible(true); newPasswordTextField.setVisible(false);
        confirmPasswordField.setVisible(true); confirmPasswordTextField.setVisible(false);
        
        currentEyeIcon.setIconLiteral("fas-eye"); newEyeIcon.setIconLiteral("fas-eye"); confirmEyeIcon.setIconLiteral("fas-eye");
        isCurrentVisible = false; isNewVisible = false; isConfirmVisible = false;
    }

    private void showModal(VBox modal) {
        modal.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), modal);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    private void hideModal(VBox modal) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), modal);
        ft.setFromValue(1); ft.setToValue(0); ft.setOnFinished(e -> modal.setVisible(false)); ft.play();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    @FXML protected void onCloseAllSessions() { System.out.println("Closing sessions..."); }
}
