package com.utp.meditrackapp.features.products.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.infrastructure.adapters.ProductoAdapter;
import com.utp.meditrackapp.domain.entities.Categoria;
import com.utp.meditrackapp.domain.entities.Producto;
import com.utp.meditrackapp.domain.entities.Usuario;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductoController {

    @FXML private TableView<Producto> tableProductos;
    @FXML private TableColumn<Producto, String> colId, colDigemid, colNombre, colCategoria, colUnidad;
    @FXML private TableColumn<Producto, Double> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock, colEstado;
    @FXML private TableColumn<Producto, Void> colAcciones;

    @FXML private TextField txtSearch;
    @FXML private Label lblTotalProductos;

    // Pagination
    @FXML private Pagination paginationProducts;
    private static final int ROWS_PER_PAGE = 10;

    // Modal fields
    @FXML private StackPane modalProducto;
    @FXML private Label modalTitle;
    @FXML private TextField txtNombre, txtDigemid, txtPrecio;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<String> cmbUnidad;
    @FXML private TextArea txtDetalle;
    @FXML private CheckBox chkActivo;

    private final ProductoAdapter productoAdapter = new ProductoAdapter();
    private final SessionManager sessionManager = SessionManager.getInstance();
    
    private final ObservableList<Producto> masterData = FXCollections.observableArrayList();
    private final Map<String, Integer> stockMap = new HashMap<>();
    private Producto selectedProducto;

    @FXML
    public void initialize() {
        setupTable();
        setupForm();
        loadData();
        handleInitialSearch();
        applyWritePermissions();
    }

    private void applyWritePermissions() {
        boolean canWrite = sessionManager.tienePermiso("M2_SEDES");
        if (!canWrite) {
            // Hide the "Nuevo" button if it exists in FXML
        }
    }

    private void handleInitialSearch() {
        String initialSearch = NavigationService.getProductInitialSearch();
        if (initialSearch != null && !initialSearch.isEmpty()) {
            txtSearch.setText(initialSearch);
            onSearch();
        }
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDigemid.setCellValueFactory(new PropertyValueFactory<>("codigoDigemid"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoriaNombre"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidadMedida"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        
        colStock.setCellValueFactory(cellData -> {
            Producto p = cellData.getValue();
            int stock = stockMap.getOrDefault(p.getId(), 0);
            return new ReadOnlyObjectWrapper<>(stock);
        });

        colStock.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    Producto p = getTableRow().getItem();
                    if (p != null && p.getStockMinimo() != null && item < p.getStockMinimo()) {
                        getStyleClass().removeAll("text-danger");
                        getStyleClass().add("text-danger");
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        getStyleClass().removeAll("text-danger");
                        setStyle("");
                    }
                }
            }
        });

        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) setGraphic(null);
                else {
                    Producto p = getTableRow().getItem();
                    Label badge = new Label(p.getIsActivo() == 1 ? "ACTIVO" : "INACTIVO");
                    badge.getStyleClass().addAll("badge", p.getIsActivo() == 1 ? "success" : "danger");
                    setGraphic(badge);
                }
            }
        });

        colAcciones.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button();
            private final Button deactivateBtn = new Button();
            private final Button permanentDeleteBtn = new Button();
            private final boolean canWrite = sessionManager.tienePermiso("M2_SEDES");
            {
                editBtn.setGraphic(new FontIcon("fas-edit"));
                editBtn.getStyleClass().addAll("button", "flat", "accent", "sm");
                editBtn.setOnAction(e -> openEditModal(getTableRow().getItem()));

                deactivateBtn.setGraphic(new FontIcon("fas-toggle-off"));
                deactivateBtn.getStyleClass().addAll("button", "flat", "warning", "sm");
                deactivateBtn.setOnAction(e -> confirmDelete(getTableRow().getItem()));

                permanentDeleteBtn.setGraphic(new FontIcon("fas-trash"));
                permanentDeleteBtn.getStyleClass().addAll("button", "flat", "danger", "sm");
                permanentDeleteBtn.setOnAction(e -> confirmPermanentDelete(getTableRow().getItem()));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    if (!canWrite) {
                        setGraphic(null);
                    } else {
                        HBox box = new HBox(8, editBtn, deactivateBtn, permanentDeleteBtn);
                        box.getStyleClass().add("actions-cell");
                        box.setAlignment(Pos.CENTER);
                        setGraphic(box);
                    }
                }
            }
        });
    }

    private void setupForm() {
        cmbUnidad.setItems(FXCollections.observableArrayList("UNIDAD", "CAJA", "BLISTER", "FRASCO", "AMPOLLA", "TABLETA"));
        
        cmbCategoria.setConverter(new StringConverter<>() {
            @Override public String toString(Categoria c) { return c != null ? c.getNombre() : ""; }
            @Override public Categoria fromString(String s) { return null; }
        });
    }

    @FXML
    public void loadData() {
        tableProductos.setPlaceholder(new Label("Cargando productos..."));

        Task<List<?>> loadTask = new Task<>() {
            @Override
            protected List<?> call() throws Exception {
                Usuario user = sessionManager.getCurrentUser();
                Map<String, Integer> stock = user != null ? productoAdapter.obtenerStockTotalPorSede(user.getSedeId()) : new HashMap<>();
                List<Producto> productos = productoAdapter.listarProductos();
                List<Categoria> categorias = productoAdapter.listarCategorias();
                return List.of(productos, stock, categorias);
            }
        };

        loadTask.setOnSucceeded(e -> {
            List<?> result = loadTask.getValue();
            @SuppressWarnings("unchecked")
            List<Producto> productos = (List<Producto>) result.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Integer> stock = (Map<String, Integer>) result.get(1);
            @SuppressWarnings("unchecked")
            List<Categoria> categorias = (List<Categoria>) result.get(2);

            stockMap.clear();
            stockMap.putAll(stock);
            masterData.setAll(productos);
            cmbCategoria.setItems(FXCollections.observableArrayList(categorias));
            lblTotalProductos.setText(String.valueOf(productos.size()));

            refreshPagination();
            tableProductos.setPlaceholder(new Label("No se encontraron productos."));
        });

        loadTask.setOnFailed(e -> {
            tableProductos.setPlaceholder(new Label("Error al cargar productos."));
            Throwable ex = loadTask.getException();
            showAlert(Alert.AlertType.ERROR, "Error", "Error al cargar datos: " + (ex != null ? ex.getMessage() : "Error desconocido"));
        });

        new Thread(loadTask).start();
    }

    private void refreshPagination() {
        String query = txtSearch.getText();
        List<Producto> filtered = filterProducts(masterData, query);

        int totalItems = filtered.size();
        int pageCount = (int) Math.ceil((double) totalItems / ROWS_PER_PAGE);
        if (pageCount < 1) pageCount = 1;

        paginationProducts.setPageCount(pageCount);
        paginationProducts.currentPageIndexProperty().set(0);
        paginationProducts.setPageFactory(this::createPage);
    }

    private javafx.scene.Node createPage(int pageIndex) {
        String query = txtSearch.getText();
        List<Producto> filtered = filterProducts(masterData, query);

        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, filtered.size());

        ObservableList<Producto> pageItems = FXCollections.observableArrayList();
        if (fromIndex < toIndex) {
            pageItems.addAll(filtered.subList(fromIndex, toIndex));
        }
        tableProductos.setItems(pageItems);
        return new VBox();
    }

    private List<Producto> filterProducts(ObservableList<Producto> source, String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.copyOf(source);
        }
        String[] terms = query.trim().toLowerCase().split("\\s+");
        return source.stream()
            .filter(p -> {
                String fullData = (p.getNombre() + " " + p.getCodigoDigemid()).toLowerCase();
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
    protected void onOpenRegisterModal() {
        if (!sessionManager.tienePermiso("M2_SEDES")) {
            showAlert(Alert.AlertType.WARNING, "Sin permisos", "No tiene permisos para crear productos.");
            return;
        }
        selectedProducto = null;
        modalTitle.setText("Registrar Producto");
        clearForm();
        modalProducto.setVisible(true);
    }

    private void openEditModal(Producto p) {
        if (p == null) return;
        selectedProducto = p;
        modalTitle.setText("Editar Producto");
        txtNombre.setText(p.getNombre());
        txtDigemid.setText(p.getCodigoDigemid());
        txtDetalle.setText(p.getDetalle());
        txtPrecio.setText(p.getPrecioUnitario() != null ? String.valueOf(p.getPrecioUnitario()) : "0.00");
        cmbUnidad.setValue(p.getUnidadMedida());
        chkActivo.setSelected(p.getIsActivo() == 1);
        
        cmbCategoria.getItems().stream()
            .filter(c -> c.getId().equals(p.getCategoriaId()))
            .findFirst()
            .ifPresent(cmbCategoria::setValue);

        modalProducto.setVisible(true);
    }

    @FXML
    protected void onSave() {
        if (!sessionManager.tienePermiso("M2_SEDES")) {
            showAlert(Alert.AlertType.WARNING, "Sin permisos", "No tiene permisos para modificar productos.");
            return;
        }
        if (!validateForm()) return;

        try {
            if (selectedProducto == null) {
                Producto nuevo = new Producto();
                setProductoFromForm(nuevo);
                productoAdapter.guardarProducto(nuevo);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Producto creado correctamente.");
            } else {
                setProductoFromForm(selectedProducto);
                productoAdapter.actualizarProducto(selectedProducto);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            }
            loadData();
            onCloseModal();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
        }
    }

    private void setProductoFromForm(Producto p) {
        p.setNombre(txtNombre.getText().trim());
        p.setCodigoDigemid(txtDigemid.getText().trim());
        p.setCategoriaId(cmbCategoria.getValue().getId());
        p.setUnidadMedida(cmbUnidad.getValue());
        p.setDetalle(txtDetalle.getText());
        p.setIsActivo(chkActivo.isSelected() ? 1 : 0);
        
        try {
            p.setPrecioUnitario(Double.parseDouble(txtPrecio.getText()));
        } catch (NumberFormatException e) {
            p.setPrecioUnitario(0.0);
        }
    }

    private boolean validateForm() {
        if (txtNombre.getText().isBlank() || txtDigemid.getText().isBlank() || 
            cmbCategoria.getValue() == null || cmbUnidad.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Por favor complete todos los campos obligatorios.");
            return false;
        }
        
        try {
            Double.parseDouble(txtPrecio.getText());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validación", "El precio unitario debe ser un número válido.");
            return false;
        }
        
        return true;
    }

    private void confirmDelete(Producto p) {
        if (p == null) return;
        if (!sessionManager.tienePermiso("M2_SEDES")) {
            showAlert(Alert.AlertType.WARNING, "Sin permisos", "No tiene permisos para eliminar productos.");
            return;
        }

        // Verificar si tiene stock activo antes de desactivar
        try {
            var user = SessionManager.getInstance().getCurrentUser();
            String sedeId = user != null ? user.getSedeId() : null;
            if (sedeId != null) {
                int stock = productoAdapter.obtenerStockTotal(sedeId, p.getId());
                if (stock > 0) {
                    showAlert(Alert.AlertType.WARNING, "Stock Activo",
                        "El producto \"" + p.getNombre() + "\" tiene " + stock + " unidades en stock. No se puede desactivar mientras tenga inventario.");
                    return;
                }
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo verificar el stock del producto.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar Desactivación");
        dialog.setHeaderText("¿Está seguro de desactivar el producto \"" + p.getNombre() + "\"?");
        dialog.setContentText("El producto quedará inactivo. Escriba 'DESACTIVAR' para confirmar:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && "DESACTIVAR".equals(result.get().trim().toUpperCase())) {
            try {
                productoAdapter.desactivarProducto(p.getId());
                loadData();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo desactivar el producto.");
            }
        }
    }

    private void confirmPermanentDelete(Producto p) {
        if (p == null) return;
        if (!sessionManager.tienePermiso("M2_SEDES")) {
            showAlert(Alert.AlertType.WARNING, "Sin permisos", "No tiene permisos para eliminar productos.");
            return;
        }

        // Verificar si el producto tiene inventario en CUALQUIER sede
        try {
            if (productoAdapter.productoTieneLotes(p.getId())) {
                showAlert(Alert.AlertType.WARNING, "Inventario Activo",
                    "El producto \"" + p.getNombre() + "\" tiene registros de inventario en una o más sedes. No se puede eliminar.");
                return;
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "No se pudo verificar el inventario del producto.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar Eliminación Permanente");
        dialog.setHeaderText("¿Está seguro de ELIMINAR permanentemente el producto \"" + p.getNombre() + "\"?");
        dialog.setContentText("Esta acción es IRREVERSIBLE. Escriba 'ELIMINAR' para confirmar:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && "ELIMINAR".equals(result.get().trim().toUpperCase())) {
            try {
                productoAdapter.eliminarProducto(p.getId());
                loadData();
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Producto eliminado permanentemente.");
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el producto.");
            }
        }
    }

    @FXML protected void onCloseModal() { modalProducto.setVisible(false); }

    private void clearForm() {
        txtNombre.clear();
        txtDigemid.clear();
        txtPrecio.clear();
        txtDetalle.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbUnidad.getSelectionModel().selectFirst();
        chkActivo.setSelected(true);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}
