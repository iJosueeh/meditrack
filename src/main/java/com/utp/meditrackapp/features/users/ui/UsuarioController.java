package com.utp.meditrackapp.features.users.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.domain.entities.Rol;
import com.utp.meditrackapp.domain.entities.Sede;
import com.utp.meditrackapp.domain.entities.Usuario;
import com.utp.meditrackapp.core.util.IdGenerator;
import com.utp.meditrackapp.core.models.enums.EntidadPrefix;
import com.utp.meditrackapp.infrastructure.adapters.UserAdapter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UsuarioController {

    @FXML private TextField searchField;
    @FXML private TableView<Usuario> usersTable;
    @FXML private TableColumn<Usuario, String> colDni, colNombres, colRol, colSede;
    @FXML private TableColumn<Usuario, Integer> colEstado;
    @FXML private TableColumn<Usuario, Void> colActions;

    @FXML private StackPane formOverlay;
    @FXML private Label formTitle;
    @FXML private ComboBox<String> typeDocCombo;
    @FXML private TextField numDocField, firstNameField, lastNameField;
    @FXML private ComboBox<Rol> rolCombo;
    @FXML private ComboBox<Sede> sedeCombo;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private FontIcon toggleIcon;
    @FXML private VBox passwordContainer;

    @FXML private StackPane resetPasswordOverlay;
    @FXML private Label resetPasswordTitle;
    @FXML private PasswordField resetPasswordField;
    @FXML private TextField resetPasswordTextField;
    @FXML private FontIcon resetToggleIcon;

    private final UserAdapter userAdapter = new UserAdapter();
    private Usuario currentUsuario;

    @FXML
    public void initialize() {
        setupTable();
        setupForm();
        loadData();
    }

    private void setupTable() {
        colDni.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        colNombres.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rolNombre"));
        colSede.setCellValueFactory(new PropertyValueFactory<>("sedeNombre"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("isActivo"));
        
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(item == 1 ? "ACTIVO" : "INACTIVO");
                    label.getStyleClass().addAll("badge", item == 1 ? "success" : "danger");
                    setGraphic(label);
                }
            }
        });

        setupActionsColumn();
    }

    private void setupForm() {
        typeDocCombo.setItems(FXCollections.observableArrayList("DNI", "CE"));
        
        rolCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Rol r) { return r != null ? r.getNombre() : ""; }
            @Override public Rol fromString(String s) { return null; }
        });
        
        sedeCombo.setConverter(new StringConverter<>() {
            @Override public String toString(Sede s) { return s != null ? s.getNombre() : ""; }
            @Override public Sede fromString(String s) { return null; }
        });

        // Filtrar roles: solo mostrar roles de menor jerarquía (mayor nivel) que el usuario actual
        var session = com.utp.meditrackapp.core.config.SessionManager.getInstance();
        var rolActual = session.getRolUsuario();
        
        List<Rol> todosLosRoles = userAdapter.listarRoles();
        List<Rol> rolesDisponibles;
        
        if (rolActual != null) {
            rolesDisponibles = todosLosRoles.stream()
                .filter(r -> r.getNivel() > rolActual.getNivel() || r.getId().equals(rolActual.getId()))
                .filter(r -> r.getIsActivo() == 1)
                .collect(Collectors.toList());
        } else {
            rolesDisponibles = todosLosRoles;
        }
        
        rolCombo.setItems(FXCollections.observableArrayList(rolesDisponibles));

        List<Sede> sedesDisponibles = userAdapter.listarSedes();
        if (!session.tienePermiso("M2_SEDES")) {
            Usuario currentUser = session.getCurrentUser();
            if (currentUser != null && currentUser.getSedeId() != null) {
                sedesDisponibles = sedesDisponibles.stream()
                    .filter(s -> s.getId().equals(currentUser.getSedeId()))
                    .collect(Collectors.toList());
            }
        }
        sedeCombo.setItems(FXCollections.observableArrayList(sedesDisponibles));
    }

    private void loadData() {
        SessionManager session = SessionManager.getInstance();
        List<Usuario> users;
        if (session.tienePermiso("M2_SEDES")) {
            users = userAdapter.listarUsuarios();
        } else {
            Usuario currentUser = session.getCurrentUser();
            if (currentUser != null && currentUser.getSedeId() != null) {
                users = userAdapter.listarUsuariosPorSede(currentUser.getSedeId());
            } else {
                users = userAdapter.listarUsuarios();
            }
        }
        usersTable.setItems(FXCollections.observableArrayList(users));
    }

    @FXML
    protected void onSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            loadData();
            return;
        }

        SessionManager session = SessionManager.getInstance();
        List<Usuario> baseUsers;
        if (session.tienePermiso("M2_SEDES")) {
            baseUsers = userAdapter.listarUsuarios();
        } else {
            Usuario currentUser = session.getCurrentUser();
            if (currentUser != null && currentUser.getSedeId() != null) {
                baseUsers = userAdapter.listarUsuariosPorSede(currentUser.getSedeId());
            } else {
                baseUsers = userAdapter.listarUsuarios();
            }
        }

        String[] terms = query.split("\\s+");
        List<Usuario> filtered = baseUsers.stream()
            .filter(u -> {
                String fullData = (u.getNombreCompleto() + " " + u.getNumeroDocumento()).toLowerCase();
                for (String term : terms) {
                    if (!fullData.contains(term)) return false;
                }
                return true;
            })
            .collect(Collectors.toList());
        usersTable.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    protected void onNewUser() {
        currentUsuario = null;
        formTitle.setText("Registrar Nuevo Usuario");
        clearForm();
        passwordContainer.setVisible(true);
        passwordContainer.setManaged(true);
        formOverlay.setVisible(true);
    }

    @FXML
    protected void onSaveUser() {
        if (validateForm()) {
            boolean isNew = (currentUsuario == null);
            if (isNew) {
                currentUsuario = new Usuario();
            }

            currentUsuario.setTipoDocumento(typeDocCombo.getValue());
            currentUsuario.setNumeroDocumento(numDocField.getText());
            currentUsuario.setNombres(firstNameField.getText());
            currentUsuario.setApellidos(lastNameField.getText());
            currentUsuario.setRolId(rolCombo.getValue().getId());
            currentUsuario.setSedeId(sedeCombo.getValue().getId());

            boolean success;
            if (isNew) {
                success = "OK".equals(userAdapter.guardarUsuario(currentUsuario, passwordField.getText()));
            } else {
                success = "OK".equals(userAdapter.actualizarUsuario(currentUsuario));
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Usuario guardado correctamente.");
                formOverlay.setVisible(false);
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo guardar el usuario.");
            }
        }
    }

    @FXML
    protected void onCancelForm() {
        formOverlay.setVisible(false);
    }

    @FXML
    protected void onTogglePasswordVisibility() {
        boolean isVisible = passwordTextField.isVisible();
        if (isVisible) {
            passwordField.setText(passwordTextField.getText());
            passwordTextField.setVisible(false);
            passwordField.setVisible(true);
            toggleIcon.setIconLiteral("fas-eye");
        } else {
            passwordTextField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordTextField.setVisible(true);
            toggleIcon.setIconLiteral("fas-eye-slash");
        }
    }

    private void showResetPasswordForm(Usuario u) {
        currentUsuario = u;
        resetPasswordTitle.setText("Restablecer Contraseña: " + u.getNombreCompleto());
        resetPasswordField.clear();
        resetPasswordTextField.clear();
        resetPasswordField.setVisible(true);
        resetPasswordTextField.setVisible(false);
        resetToggleIcon.setIconLiteral("fas-eye");
        resetPasswordOverlay.setVisible(true);
    }

    @FXML
    protected void onSaveResetPassword() {
        String newPassword = resetPasswordField.isVisible() ? resetPasswordField.getText() : resetPasswordTextField.getText();
        
        if (newPassword.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Seguridad", "La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        String hashedPassword = com.utp.meditrackapp.core.util.PasswordHasher.hashPassword(newPassword);
        if ("OK".equals(userAdapter.actualizarPassword(currentUsuario.getId(), hashedPassword))) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Contraseña restablecida correctamente.");
            resetPasswordOverlay.setVisible(false);
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo actualizar la contraseña.");
        }
    }

    @FXML
    protected void onCancelResetPassword() {
        resetPasswordOverlay.setVisible(false);
    }

    @FXML
    protected void onToggleResetPasswordVisibility() {
        boolean isVisible = resetPasswordTextField.isVisible();
        if (isVisible) {
            resetPasswordField.setText(resetPasswordTextField.getText());
            resetPasswordTextField.setVisible(false);
            resetPasswordField.setVisible(true);
            resetToggleIcon.setIconLiteral("fas-eye");
        } else {
            resetPasswordTextField.setText(resetPasswordField.getText());
            resetPasswordField.setVisible(false);
            resetPasswordTextField.setVisible(true);
            resetToggleIcon.setIconLiteral("fas-eye-slash");
        }
    }

    private boolean validateForm() {
        if (numDocField.getText().isEmpty() || firstNameField.getText().isEmpty() || 
            rolCombo.getValue() == null || sedeCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Complete los campos obligatorios.");
            return false;
        }
        if (currentUsuario == null && passwordField.getText().length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Seguridad", "La contraseña debe tener al menos 6 caracteres.");
            return false;
        }
        return true;
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnResetPwd = new Button();
            private final Button btnToggle = new Button();
            private final HBox box = new HBox(3, btnEdit, btnResetPwd, btnToggle);

            {
                btnEdit.setGraphic(new FontIcon("fas-edit"));
                btnEdit.getStyleClass().addAll("button", "flat", "sm");
                btnEdit.setTooltip(new Tooltip("Editar"));
                btnEdit.setMinWidth(26);
                btnEdit.setMaxWidth(26);
                btnEdit.setOnAction(e -> showEditForm(getTableView().getItems().get(getIndex())));

                btnResetPwd.setGraphic(new FontIcon("fas-key"));
                btnResetPwd.getStyleClass().addAll("button", "flat", "sm");
                btnResetPwd.setTooltip(new Tooltip("Resetear pass"));
                btnResetPwd.setMinWidth(26);
                btnResetPwd.setMaxWidth(26);
                btnResetPwd.setOnAction(e -> showResetPasswordForm(getTableView().getItems().get(getIndex())));

                btnToggle.getStyleClass().addAll("button", "flat", "sm");
                btnToggle.setMinWidth(26);
                btnToggle.setMaxWidth(26);
                btnToggle.setOnAction(e -> handleToggleStatus(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    if (u.getIsActivo() == 1) {
                        btnToggle.setGraphic(new FontIcon("fas-user-minus"));
                        btnToggle.getStyleClass().removeAll("success");
                        btnToggle.getStyleClass().add("danger");
                        btnToggle.setTooltip(new Tooltip("Desactivar"));
                    } else {
                        btnToggle.setGraphic(new FontIcon("fas-user-check"));
                        btnToggle.getStyleClass().removeAll("danger");
                        btnToggle.getStyleClass().add("success");
                        btnToggle.setTooltip(new Tooltip("Activar"));
                    }
                    box.setAlignment(javafx.geometry.Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private void showEditForm(Usuario u) {
        currentUsuario = u;
        formTitle.setText("Editar Usuario");
        typeDocCombo.setValue(u.getTipoDocumento());
        numDocField.setText(u.getNumeroDocumento());
        firstNameField.setText(u.getNombres());
        lastNameField.setText(u.getApellidos());
        
        rolCombo.getItems().stream().filter(r -> r.getId().equals(u.getRolId())).findFirst().ifPresent(rolCombo::setValue);
        sedeCombo.getItems().stream().filter(s -> s.getId().equals(u.getSedeId())).findFirst().ifPresent(sedeCombo::setValue);

        passwordContainer.setVisible(false);
        passwordContainer.setManaged(false);
        formOverlay.setVisible(true);
    }

    private void handleToggleStatus(Usuario u) {
        boolean deactivating = u.getIsActivo() == 1;
        String action = deactivating ? "Desactivar" : "Activar";
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿" + action + " usuario " + u.getNombreCompleto() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                if ("OK".equals(userAdapter.toggleEstado(u.getId()))) {
                    loadData();
                }
            }
        });
    }

    private void clearForm() {
        numDocField.clear();
        firstNameField.clear();
        lastNameField.clear();
        passwordField.clear();
        typeDocCombo.getSelectionModel().selectFirst();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
