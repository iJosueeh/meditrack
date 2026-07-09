package com.utp.meditrackapp.features.patients.ui;

import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.domain.entities.Paciente;
import com.utp.meditrackapp.infrastructure.adapters.PacienteAdapter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Optional;

public class PacienteController {

    @FXML private TextField searchField;
    @FXML private TableView<Paciente> patientsTable;
    @FXML private Button btnNuevoPaciente;
    @FXML private TableColumn<Paciente, String> colTipoDoc;
    @FXML private TableColumn<Paciente, String> colNumDoc;
    @FXML private TableColumn<Paciente, String> colNombres;
    @FXML private TableColumn<Paciente, String> colApellidos;
    @FXML private TableColumn<Paciente, String> colTelefono;
    @FXML private TableColumn<Paciente, Integer> colEstado;
    @FXML private TableColumn<Paciente, Void> colActions;

    // Summary Labels
    @FXML private Label lblTotalPatients;
    @FXML private Label lblTodayAttentions;
    @FXML private Label lblNewPatientsMonth;

    // Pagination
    @FXML private Pagination paginationPatients;
    private static final int ROWS_PER_PAGE = 10;
    private final ObservableList<Paciente> allPatients = FXCollections.observableArrayList();

    // Form Overlay Fields
    @FXML private StackPane formOverlay;
    @FXML private Label formTitle;
    @FXML private ComboBox<String> typeDocCombo;
    @FXML private TextField numDocField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private CheckBox chkActivo;

    private final PacienteAdapter pacienteAdapter = new PacienteAdapter();
    private Paciente currentPaciente;

    @FXML
    public void initialize() {
        setupTable();
        setupForm();
        loadDataInBackground();
        applyRoleRestrictions();
        handleInitialSearch();
    }

    private void handleInitialSearch() {
        String initialQuery = com.utp.meditrackapp.core.config.NavigationService.getPatientInitialSearch();
        if (initialQuery != null && !initialQuery.isEmpty()) {
            searchField.setText(initialQuery);
            onSearch();
            
            if (!patientsTable.getItems().isEmpty()) {
                patientsTable.getSelectionModel().select(0);
                patientsTable.scrollTo(0);
                patientsTable.requestFocus();
            }
        }
    }

    private void applyRoleRestrictions() {
        SessionManager session = SessionManager.getInstance();
        System.out.println("[AUTH] Permisos: M7_PACIENTES=" + session.tienePermiso("M7_PACIENTES")
            + " | USUARIOS=" + session.tienePermiso("USUARIOS"));

        btnNuevoPaciente.setVisible(session.tienePermiso("M7_PACIENTES"));
        btnNuevoPaciente.setManaged(session.tienePermiso("M7_PACIENTES"));
    }

    private void setupTable() {
        colTipoDoc.setCellValueFactory(new PropertyValueFactory<>("tipoDocumento"));
        colNumDoc.setCellValueFactory(new PropertyValueFactory<>("numeroDocumento"));
        colNombres.setCellValueFactory(new PropertyValueFactory<>("nombres"));
        colApellidos.setCellValueFactory(new PropertyValueFactory<>("apellidos"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("isActivo"));

        setupEstadoColumn();
        setupTooltipColumn(colNombres);
        setupTooltipColumn(colApellidos);
        addButtonToTable();
    }

    private void setupEstadoColumn() {
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label();
                    badge.getStyleClass().add("badge");
                    if (item == 1) {
                        badge.setText("ACTIVO");
                        badge.getStyleClass().add("success");
                    } else {
                        badge.setText("INACTIVO");
                        badge.getStyleClass().add("danger");
                    }
                    HBox box = new HBox(badge);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });
    }

    private <T> void setupTooltipColumn(TableColumn<Paciente, T> column) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item.toString());
                    setTooltip(new Tooltip(item.toString()));
                }
            }
        });
    }

    private void setupForm() {
        typeDocCombo.setItems(FXCollections.observableArrayList("DNI", "CE"));
        typeDocCombo.getSelectionModel().selectFirst();
    }

    private void loadDataInBackground() {
        patientsTable.setPlaceholder(new Label("Cargando pacientes..."));

        Task<List<?>> loadTask = new Task<>() {
            @Override
            protected List<?> call() throws Exception {
                List<Paciente> pacientes = pacienteAdapter.listarPacientes();
                int total = pacienteAdapter.getContadorTotal();
                int atendidosHoy = pacienteAdapter.getAtendidosHoy();
                int nuevosMes = pacienteAdapter.getNuevosDelMes();
                return List.of(pacientes, total, atendidosHoy, nuevosMes);
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<?> result = loadTask.getValue();
            @SuppressWarnings("unchecked")
            List<Paciente> pacientes = (List<Paciente>) result.get(0);
            int total = (int) result.get(1);
            int atendidosHoy = (int) result.get(2);
            int nuevosMes = (int) result.get(3);

            allPatients.setAll(pacientes);
            refreshPagination();

            lblTotalPatients.setText(String.valueOf(total));
            lblTodayAttentions.setText(String.valueOf(atendidosHoy));
            lblNewPatientsMonth.setText(String.valueOf(nuevosMes));

            patientsTable.setPlaceholder(new Label("No se encontraron pacientes registrados."));
        });

        loadTask.setOnFailed(e -> {
            patientsTable.setPlaceholder(new Label("Error al cargar pacientes."));
            Throwable ex = loadTask.getException();
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudieron cargar los pacientes: " + (ex != null ? ex.getMessage() : "Error desconocido"));
        });

        new Thread(loadTask).start();
    }

    private void refreshPagination() {
        String query = searchField.getText();
        List<Paciente> filtered = filterPatients(allPatients, query);

        int totalItems = filtered.size();
        int pageCount = (int) Math.ceil((double) totalItems / ROWS_PER_PAGE);
        if (pageCount < 1) pageCount = 1;

        paginationPatients.setPageCount(pageCount);
        paginationPatients.currentPageIndexProperty().set(0);
        paginationPatients.setPageFactory(this::createPage);
    }

    private javafx.scene.Node createPage(int pageIndex) {
        String query = searchField.getText();
        List<Paciente> filtered = filterPatients(allPatients, query);

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filtered.size());

        ObservableList<Paciente> pageItems = FXCollections.observableArrayList();
        if (fromIndex < toIndex) {
            pageItems.addAll(filtered.subList(fromIndex, toIndex));
        }
        patientsTable.setItems(pageItems);
        return new VBox();
    }

    private List<Paciente> filterPatients(ObservableList<Paciente> source, String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.copyOf(source);
        }
        String[] terms = query.trim().toLowerCase().split("\\s+");
        return source.stream()
            .filter(p -> {
                String fullData = (p.getNumeroDocumento() + " " + p.getNombres() + " " + p.getApellidos()).toLowerCase();
                for (String term : terms) {
                    if (!fullData.contains(term)) return false;
                }
                return true;
            })
            .toList();
    }

    @FXML
    protected void onSearch() {
        refreshPagination();
    }

    @FXML
    protected void onNewPatient() {
        currentPaciente = null;
        formTitle.setText("Registrar Nuevo Paciente");
        clearForm();
        formOverlay.setVisible(true);
    }

    @FXML
    protected void onSavePatient() {
        String tipoDoc = typeDocCombo.getValue();
        String numDoc = numDocField.getText();
        String nombres = firstNameField.getText();
        String apellidos = lastNameField.getText();

        if (tipoDoc == null || tipoDoc.isBlank()) {
            showAlert(Alert.AlertType.WARNING, "Campo requerido", "Seleccione el tipo de documento.");
            return;
        }
        if (numDoc == null || numDoc.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campo requerido", "Ingrese el número de documento.");
            return;
        }
        if (nombres == null || nombres.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campo requerido", "Ingrese el nombre del paciente.");
            return;
        }
        if (apellidos == null || apellidos.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Campo requerido", "Ingrese los apellidos del paciente.");
            return;
        }

        if (currentPaciente == null) {
            currentPaciente = new Paciente();
        }

        currentPaciente.setTipoDocumento(tipoDoc);
        currentPaciente.setNumeroDocumento(numDoc.trim());
        currentPaciente.setNombres(nombres.trim());
        currentPaciente.setApellidos(apellidos.trim());
        currentPaciente.setTelefono(phoneField.getText() != null ? phoneField.getText().trim() : "");
        currentPaciente.setIsActivo(chkActivo.isSelected() ? 1 : 0);

        String result = pacienteAdapter.guardarPaciente(currentPaciente);

        if ("OK".equals(result)) {
            showAlert(Alert.AlertType.INFORMATION, "Éxito", "Paciente guardado correctamente.");
            formOverlay.setVisible(false);
            loadDataInBackground();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error de Validación", result);
        }
    }

    @FXML
    protected void onCancelForm() {
        formOverlay.setVisible(false);
        clearForm();
    }

    private void clearForm() {
        numDocField.clear();
        firstNameField.clear();
        lastNameField.clear();
        phoneField.clear();
        typeDocCombo.getSelectionModel().selectFirst();
        chkActivo.setSelected(true);
    }

    private void addButtonToTable() {
        colActions.setCellFactory(param -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnBlock = new Button();
            private final Button btnDelete = new Button();
            private final HBox pane = new HBox(3, btnEdit, btnBlock, btnDelete);

            {
                pane.setAlignment(Pos.CENTER);
                pane.setMinWidth(84);
                pane.setMaxWidth(84);

                btnEdit.setGraphic(new FontIcon("fas-edit"));
                btnEdit.getStyleClass().addAll("button", "flat", "sm");
                btnEdit.setTooltip(new Tooltip("Editar"));
                btnEdit.setMinWidth(26);
                btnEdit.setMaxWidth(26);
                btnEdit.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    showEditForm(p);
                });

                btnBlock.getStyleClass().addAll("button", "flat", "sm");
                btnBlock.setTooltip(new Tooltip("Desactivar"));
                btnBlock.setMinWidth(26);
                btnBlock.setMaxWidth(26);
                btnBlock.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    handleToggleBlock(p);
                });

                btnDelete.setGraphic(new FontIcon("fas-trash"));
                btnDelete.getStyleClass().addAll("button", "flat", "danger", "sm");
                btnDelete.setTooltip(new Tooltip("Eliminar"));
                btnDelete.setMinWidth(26);
                btnDelete.setMaxWidth(26);
                btnDelete.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    handleDelete(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Paciente p = getTableView().getItems().get(getIndex());
                    if (p.getIsActivo() == 1) {
                        btnBlock.setGraphic(new FontIcon("fas-ban"));
                        btnBlock.getStyleClass().removeAll("success");
                        if (!btnBlock.getStyleClass().contains("danger")) {
                            btnBlock.getStyleClass().add("danger");
                        }
                        btnBlock.setTooltip(new Tooltip("Desactivar"));
                    } else {
                        btnBlock.setGraphic(new FontIcon("fas-unlock"));
                        btnBlock.getStyleClass().removeAll("danger");
                        if (!btnBlock.getStyleClass().contains("success")) {
                            btnBlock.getStyleClass().add("success");
                        }
                        btnBlock.setTooltip(new Tooltip("Reactivar"));
                    }
                    setGraphic(pane);
                }
            }
        });
    }

    private void handleToggleBlock(Paciente p) {
        if (p.getIsActivo() == 1) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Bloqueo");
            alert.setHeaderText("¿Está seguro de bloquear al paciente?");
            alert.setContentText(p.getNombres() + " " + p.getApellidos() + " no podrá recibir nuevas atenciones.");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                String resultMsg = pacienteAdapter.desactivarPaciente(p.getId());
                if ("OK".equals(resultMsg)) {
                    loadDataInBackground();
                } else {
                    showAlert(Alert.AlertType.WARNING, "No se pudo bloquear", resultMsg);
                }
            }
        } else {
            String resultMsg = pacienteAdapter.reactivarPaciente(p.getId());
            if ("OK".equals(resultMsg)) {
                loadDataInBackground();
            } else {
                showAlert(Alert.AlertType.WARNING, "No se pudo reactivar", resultMsg);
            }
        }
    }

    private void showEditForm(Paciente p) {
        currentPaciente = p;
        formTitle.setText("Editar Paciente");
        typeDocCombo.setValue(p.getTipoDocumento());
        numDocField.setText(p.getNumeroDocumento());
        firstNameField.setText(p.getNombres());
        lastNameField.setText(p.getApellidos());
        phoneField.setText(p.getTelefono());
        chkActivo.setSelected(p.getIsActivo() == 1);
        formOverlay.setVisible(true);
    }

    private void handleDelete(Paciente p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Eliminación");
        confirm.setHeaderText("¿Está seguro de eliminar permanentemente al paciente?");
        confirm.setContentText(p.getNombres() + " " + p.getApellidos() + "\nEsta acción no se puede deshacer.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String resultMsg = pacienteAdapter.eliminarPaciente(p.getId());
        if ("NO_HISTORY".equals(resultMsg)) {
            showAlert(Alert.AlertType.WARNING, "Tiene historial",
                "El paciente \"" + p.getNombres() + " " + p.getApellidos() + "\" tiene movimientos de inventario o atenciones registradas.\nNo se puede eliminar sin borrar primero el historial asociado.");
        } else if ("OK".equals(resultMsg)) {
            showAlert(Alert.AlertType.INFORMATION, "Eliminado", "Paciente eliminado correctamente.");
            loadDataInBackground();
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", resultMsg);
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
