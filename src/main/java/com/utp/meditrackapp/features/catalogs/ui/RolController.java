package com.utp.meditrackapp.features.catalogs.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.infrastructure.adapters.CatalogAdapter;
import com.utp.meditrackapp.domain.entities.Permiso;
import com.utp.meditrackapp.domain.entities.Rol;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RolController {

    @FXML private TableView<Rol> tableRoles;
    @FXML private TableColumn<Rol, String> colId;
    @FXML private TableColumn<Rol, String> colNombre;
    @FXML private TableColumn<Rol, Integer> colNivel;
    @FXML private TableColumn<Rol, String> colEstado;
    @FXML private TableColumn<Rol, Void> colAcciones;

    @FXML private TextField txtSearch;
    @FXML private Label lblTotalRoles;
    @FXML private StackPane modalRol;
    @FXML private Label modalTitle;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDescripcion;
    @FXML private ComboBox<NivelInfo> cmbNivel;

    // Checkboxes de permisos
    @FXML private CheckBox chkM4Lotes, chkM5Entradas, chkM6Salidas, chkM8Atenciones, chkM9Dispensacion;
    @FXML private CheckBox chkM2Sedes, chkM3Productos, chkM7Pacientes, chkUsuarios, chkCategorias, chkMovCatalogos;
    @FXML private CheckBox chkM10Reportes, chkRoles;

    private final CatalogAdapter catalogAdapter = new CatalogAdapter();
    private final ObservableList<Rol> masterData = FXCollections.observableArrayList();
    private Rol selectedRol;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNivel.setCellValueFactory(new PropertyValueFactory<>("nivel"));
        colNivel.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    NivelInfo info = new NivelInfo(item);
                    Label label = new Label(info.getDescripcion());
                    label.setWrapText(true);
                    label.setStyle("-fx-font-size: 11px;");
                    setGraphic(label);
                }
            }
        });

        colEstado.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Rol rol = getTableRow().getItem();
                    Label label = new Label(rol.getIsActivo() == 1 ? "ACTIVO" : "INACTIVO");
                    label.getStyleClass().add("status-badge-base");
                    label.getStyleClass().add(rol.getIsActivo() == 1 ? "status-badge-active" : "status-badge-critico");
                    setGraphic(label);
                }
            }
        });

        colAcciones.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button toggleBtn = new Button();
            private final Button deleteBtn = new Button();
            {
                editBtn.setGraphic(new FontIcon("fas-edit"));
                editBtn.getStyleClass().addAll("button", "flat", "accent", "sm");
                editBtn.setTooltip(new Tooltip("Editar"));
                editBtn.setOnAction(event -> {
                    Rol rol = getTableRow().getItem();
                    if (rol != null) openEditModal(rol);
                });

                toggleBtn.getStyleClass().addAll("button", "flat", "sm");
                toggleBtn.setTooltip(new Tooltip("Activar/Desactivar"));
                toggleBtn.setOnAction(event -> {
                    Rol rol = getTableRow().getItem();
                    if (rol != null) confirmToggle(rol);
                });

                deleteBtn.setGraphic(new FontIcon("fas-trash"));
                deleteBtn.getStyleClass().addAll("button", "flat", "danger", "sm");
                deleteBtn.setTooltip(new Tooltip("Eliminar"));
                deleteBtn.setOnAction(event -> {
                    Rol rol = getTableRow().getItem();
                    if (rol != null) confirmDelete(rol);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Rol rol = getTableRow().getItem();
                    toggleBtn.setGraphic(new FontIcon(
                        rol.getIsActivo() == 1 ? "fas-user-minus" : "fas-user-check"));
                    HBox box = new HBox(8, editBtn, toggleBtn, deleteBtn);
                    box.setStyle("-fx-alignment: center;");
                    setGraphic(box);
                }
            }
        });
    }

    private void setupSearch() {
        FilteredList<Rol> filteredData = new FilteredList<>(masterData, p -> true);
        txtSearch.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(rol -> {
                if (newValue == null || newValue.isBlank()) return true;
                String lower = newValue.toLowerCase();
                return rol.getNombre().toLowerCase().contains(lower) || 
                       rol.getId().toLowerCase().contains(lower);
            });
        });
        tableRoles.setItems(filteredData);
    }

    @FXML
    public void loadData() {
        try {
            List<Rol> list = catalogAdapter.listarRoles();
            Rol rolActual = SessionManager.getInstance().getRolUsuario();
            
            if (rolActual != null) {
                // Filtrar roles que el usuario actual puede gestionar
                List<Rol> rolesVisibles = list.stream()
                    .filter(r -> rolActual.puedeGestionarRol(r) || r.getId().equals(rolActual.getId()))
                    .collect(Collectors.toList());
                masterData.setAll(rolesVisibles);
                if (lblTotalRoles != null) {
                    lblTotalRoles.setText(String.valueOf(rolesVisibles.size()));
                }
            } else {
                masterData.setAll(list);
                if (lblTotalRoles != null) {
                    lblTotalRoles.setText(String.valueOf(list.size()));
                }
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudieron cargar los roles: " + e.getMessage());
        }
    }

    @FXML
    protected void onOpenRegisterModal() {
        Rol rolActual = SessionManager.getInstance().getRolUsuario();
        
        // Verificar que tiene permiso para crear roles
        if (!SessionManager.getInstance().tienePermiso("ROLES")) {
            showAlert("Acceso Denegado", "No tiene permisos para gestionar roles.");
            return;
        }
        
        selectedRol = null;
        modalTitle.setText("Registrar Rol");
        txtNombre.clear();
        if (txtDescripcion != null) txtDescripcion.clear();
        
        // Configurar ComboBox de niveles con descripciones
        setupNivelCombo();
        
        // Configurar niveles disponibles (solo niveles mayores al nivel actual, hasta nivel 5)
        if (cmbNivel != null && rolActual != null) {
            int nivelActual = rolActual.getNivel();
            List<Integer> niveles = java.util.stream.IntStream.rangeClosed(nivelActual + 1, 5)
                .boxed()
                .collect(java.util.stream.Collectors.toList());
            cmbNivel.setItems(FXCollections.observableArrayList(niveles.stream()
                .map(NivelInfo::new)
                .collect(java.util.stream.Collectors.toList())));
            cmbNivel.setValue(new NivelInfo(nivelActual + 1));
        }
        
        // Limpiar checkboxes de permisos
        clearPermisoCheckboxes();
        
        modalRol.setVisible(true);
    }

    private void openEditModal(Rol rol) {
        Rol rolActual = SessionManager.getInstance().getRolUsuario();
        
        // Verificar que puede editar este rol
        if (!rolActual.puedeGestionarRol(rol) && !rol.getId().equals(rolActual.getId())) {
            showAlert("Acción no permitida", "No puede editar roles de igual o mayor jerarquía.");
            return;
        }
        
        selectedRol = rol;
        modalTitle.setText("Editar Rol");
        txtNombre.setText(rol.getNombre());
        if (txtDescripcion != null) txtDescripcion.setText(rol.getDescripcion());
        
        // Configurar nivel
        if (cmbNivel != null) {
            setupNivelCombo();
            cmbNivel.setItems(FXCollections.observableArrayList(
                java.util.stream.IntStream.rangeClosed(1, 5)
                    .mapToObj(NivelInfo::new)
                    .collect(java.util.stream.Collectors.toList())
            ));
            cmbNivel.setValue(new NivelInfo(rol.getNivel()));
        }
        
        // Cargar permisos del rol
        loadRolPermisos(rol);
        
        modalRol.setVisible(true);
    }

    private void loadRolPermisos(Rol rol) {
        if (rol.getPermisos() == null) return;
        
        clearPermisoCheckboxes();
        
        for (Permiso permiso : rol.getPermisos()) {
            switch (permiso.getCodigo()) {
                case "M4_LOTES": if (chkM4Lotes != null) chkM4Lotes.setSelected(true); break;
                case "M5_ENTRADAS": if (chkM5Entradas != null) chkM5Entradas.setSelected(true); break;
                case "M6_SALIDAS": if (chkM6Salidas != null) chkM6Salidas.setSelected(true); break;
                case "M8_ATENCIONES": if (chkM8Atenciones != null) chkM8Atenciones.setSelected(true); break;
                case "M9_DISPENSACION": if (chkM9Dispensacion != null) chkM9Dispensacion.setSelected(true); break;
                case "M2_SEDES": if (chkM2Sedes != null) chkM2Sedes.setSelected(true); break;
                case "M3_PRODUCTOS": if (chkM3Productos != null) chkM3Productos.setSelected(true); break;
                case "M7_PACIENTES": if (chkM7Pacientes != null) chkM7Pacientes.setSelected(true); break;
                case "USUARIOS": if (chkUsuarios != null) chkUsuarios.setSelected(true); break;
                case "CATEGORIAS": if (chkCategorias != null) chkCategorias.setSelected(true); break;
                case "MOV_CATALOGOS": if (chkMovCatalogos != null) chkMovCatalogos.setSelected(true); break;
                case "M10_REPORTES": if (chkM10Reportes != null) chkM10Reportes.setSelected(true); break;
                case "ROLES": if (chkRoles != null) chkRoles.setSelected(true); break;
            }
        }
    }

    private void clearPermisoCheckboxes() {
        if (chkM4Lotes != null) chkM4Lotes.setSelected(false);
        if (chkM5Entradas != null) chkM5Entradas.setSelected(false);
        if (chkM6Salidas != null) chkM6Salidas.setSelected(false);
        if (chkM8Atenciones != null) chkM8Atenciones.setSelected(false);
        if (chkM9Dispensacion != null) chkM9Dispensacion.setSelected(false);
        if (chkM2Sedes != null) chkM2Sedes.setSelected(false);
        if (chkM3Productos != null) chkM3Productos.setSelected(false);
        if (chkM7Pacientes != null) chkM7Pacientes.setSelected(false);
        if (chkUsuarios != null) chkUsuarios.setSelected(false);
        if (chkCategorias != null) chkCategorias.setSelected(false);
        if (chkMovCatalogos != null) chkMovCatalogos.setSelected(false);
        if (chkM10Reportes != null) chkM10Reportes.setSelected(false);
        if (chkRoles != null) chkRoles.setSelected(false);
    }

    @FXML
    protected void onSave() {
        String nombre = txtNombre.getText();
        if (nombre == null || nombre.isBlank()) {
            showAlert("Validación", "El nombre es obligatorio.");
            return;
        }

        Rol rolActual = SessionManager.getInstance().getRolUsuario();
        int nivelNuevo = cmbNivel != null && cmbNivel.getValue() != null ? cmbNivel.getValue().getNivel() : 99;
        
        // Validación de jerarquía al crear
        if (selectedRol == null && rolActual != null) {
            if (nivelNuevo <= rolActual.getNivel()) {
                showAlert("Validación", "No puede crear roles con nivel igual o menor al suyo.");
                return;
            }
        }
        
        // Obtener permisos seleccionados
        List<String> permisosSeleccionados = getSelectedPermisoIds();

        if (selectedRol == null) {
            Rol nuevo = new Rol(null, nombre);
            nuevo.setDescripcion(txtDescripcion != null ? txtDescripcion.getText() : "");
            nuevo.setNivel(nivelNuevo);
            nuevo.setIsSistema(0);
            nuevo.setIsActivo(1);
            String result = catalogAdapter.crearRol(nuevo);
            if (!"OK".equals(result)) {
                showAlert("Error", result);
                return;
            }
            
            // Guardar permisos del rol
            catalogAdapter.guardarPermisosRol(nuevo.getId(), permisosSeleccionados);
            
            showAlert("Éxito", "Rol creado correctamente.");
        } else {
            selectedRol.setNombre(nombre);
            selectedRol.setDescripcion(txtDescripcion != null ? txtDescripcion.getText() : "");
            selectedRol.setNivel(nivelNuevo);
            catalogAdapter.actualizarRol(selectedRol);
            
            // Actualizar permisos
            catalogAdapter.guardarPermisosRol(selectedRol.getId(), permisosSeleccionados);
            
            showAlert("Éxito", "Rol actualizado correctamente.");
        }
        loadData();
        onCloseModal();
    }

    private List<String> getSelectedPermisoIds() {
        List<String> permisos = new java.util.ArrayList<>();
        
        if (chkM4Lotes != null && chkM4Lotes.isSelected()) permisos.add("PERM-002");
        if (chkM5Entradas != null && chkM5Entradas.isSelected()) permisos.add("PERM-003");
        if (chkM6Salidas != null && chkM6Salidas.isSelected()) permisos.add("PERM-004");
        if (chkM8Atenciones != null && chkM8Atenciones.isSelected()) permisos.add("PERM-005");
        if (chkM9Dispensacion != null && chkM9Dispensacion.isSelected()) permisos.add("PERM-006");
        if (chkM2Sedes != null && chkM2Sedes.isSelected()) permisos.add("PERM-007");
        if (chkM3Productos != null && chkM3Productos.isSelected()) permisos.add("PERM-008");
        if (chkM7Pacientes != null && chkM7Pacientes.isSelected()) permisos.add("PERM-009");
        if (chkUsuarios != null && chkUsuarios.isSelected()) permisos.add("PERM-010");
        if (chkCategorias != null && chkCategorias.isSelected()) permisos.add("PERM-011");
        if (chkMovCatalogos != null && chkMovCatalogos.isSelected()) permisos.add("PERM-012");
        if (chkM10Reportes != null && chkM10Reportes.isSelected()) permisos.add("PERM-013");
        if (chkRoles != null && chkRoles.isSelected()) permisos.add("PERM-014");
        
        return permisos;
    }

    private void confirmToggle(Rol rol) {
        Rol rolActual = SessionManager.getInstance().getRolUsuario();
        String accion = rol.getIsActivo() == 1 ? "desactivar" : "activar";

        // Validación 1: No puede modificar roles de igual o mayor jerarquía
        if (rolActual != null && !rolActual.puedeGestionarRol(rol) && !rol.getId().equals(rolActual.getId())) {
            showAlert("Acción no permitida", 
                "No puede modificar roles de igual o mayor jerarquía.\n\n" +
                "Su nivel: " + rolActual.getNivel() + "\n" +
                "Nivel del rol: " + rol.getNivel());
            return;
        }

        // Validación 2: No puede desactivar su propio rol
        var currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRolId() != null
            && currentUser.getRolId().equals(rol.getId())) {
            showAlert("Acción no permitida",
                "No puedes desactivar tu propio rol (\"" + rol.getNombre() + "\") mientras estás autenticado.");
            return;
        }

        // Validación 3: No puede desactivar roles del sistema
        if (rol.esSistema()) {
            showAlert("Acción no permitida",
                "El rol \"" + rol.getNombre() + "\" es un rol del sistema y no puede ser desactivado.");
            return;
        }

        // Validación 4: Verificar usuarios asignados
        if (rol.getIsActivo() == 1) {
            try {
                int userCount = catalogAdapter.contarUsuariosPorRol(rol.getId());
                if (userCount > 0) {
                    showAlert("No se puede desactivar",
                        "Hay " + userCount + " usuario(s) asignado(s) al rol \"" + rol.getNombre() + "\".\n\n" +
                        "Primero reasigne o elimine los usuarios antes de desactivar este rol.");
                    return;
                }
            } catch (Exception e) {
                showAlert("Error", "No se pudo verificar usuarios: " + e.getMessage());
                return;
            }
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Cambio de Estado");
        alert.setHeaderText("¿Está seguro de " + accion + " el rol?");
        alert.setContentText("El rol \"" + rol.getNombre() + "\" será " + (rol.getIsActivo() == 1 ? "desactivado" : "activado") + ".");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String r = catalogAdapter.toggleEstadoRol(rol.getId());
            if ("OK".equals(r)) {
                loadData();
                showAlert("Éxito", "Rol " + (rol.getIsActivo() == 1 ? "desactivado" : "activado") + ".");
            } else {
                showAlert("Error", r);
            }
        }
    }

    private void confirmDelete(Rol rol) {
        Rol rolActual = SessionManager.getInstance().getRolUsuario();
        
        // Validación 1: No puede eliminar roles del sistema
        if (rol.esSistema()) {
            showAlert("Acción no permitida",
                "El rol \"" + rol.getNombre() + "\" es un rol del sistema y no puede ser eliminado.");
            return;
        }
        
        // Validación 2: No puede eliminar roles de igual o mayor jerarquía
        if (rolActual != null && !rolActual.puedeGestionarRol(rol)) {
            showAlert("Acción no permitida",
                "No puede eliminar roles de igual o mayor jerarquía.");
            return;
        }
        
        // Validación 3: Verificar usuarios asignados
        try {
            int userCount = catalogAdapter.contarUsuariosPorRol(rol.getId());
            if (userCount > 0) {
                showAlert("No se puede eliminar",
                    "Hay " + userCount + " usuario(s) asignado(s) al rol \"" + rol.getNombre() + "\".\n\n" +
                    "Primero reasigne o elimine los usuarios.");
                return;
            }
        } catch (Exception e) {
            showAlert("Error", "No se pudo verificar usuarios: " + e.getMessage());
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar Eliminación");
        dialog.setHeaderText("¿Está seguro de eliminar el rol \"" + rol.getNombre() + "\"?");
        dialog.setContentText("Esta acción no se puede deshacer. Escriba 'ELIMINAR' para confirmar:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && "ELIMINAR".equals(result.get().trim().toUpperCase())) {
            String r = catalogAdapter.eliminarRol(rol.getId());
            if ("OK".equals(r)) {
                loadData();
                showAlert("Éxito", "Rol eliminado.");
            } else {
                showAlert("Error", r);
            }
        }
    }

    @FXML protected void onCloseModal() { modalRol.setVisible(false); }

    private void setupNivelCombo() {
        cmbNivel.setConverter(new javafx.util.StringConverter<NivelInfo>() {
            @Override
            public String toString(NivelInfo info) {
                return info != null ? info.getDescripcion() : "";
            }
            @Override
            public NivelInfo fromString(String s) {
                return null;
            }
        });
    }

    /**
     * Wrapper descriptivo para los niveles de jerarquía.
     * Convierte números críticos en etiquetas comprensibles.
     */
    public static class NivelInfo {
        private final int nivel;

        private static final String[][] DESCRIPCIONES = {
            { "1", "Nivel 1 — Administrador Global" },
            { "2", "Nivel 2 — Director Regional" },
            { "3", "Nivel 3 — Jefe de Sede" },
            { "4", "Nivel 4 — Técnico de Farmacia" },
            { "5", "Nivel 5 — Auxiliar de Farmacia" },
        };

        public NivelInfo(int nivel) {
            this.nivel = nivel;
        }

        public int getNivel() { return nivel; }

        public String getDescripcion() {
            for (String[] d : DESCRIPCIONES) {
                if (d[0].equals(String.valueOf(nivel))) return d[1];
            }
            return "Nivel " + nivel + " — Rol personalizado";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NivelInfo other)) return false;
            return this.nivel == other.nivel;
        }

        @Override
        public int hashCode() { return Integer.hashCode(nivel); }
    }

    private void showAlert(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
