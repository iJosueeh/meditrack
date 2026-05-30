package com.utp.meditrackapp.features.products.ui;

import com.utp.meditrackapp.core.dao.CategoriaDAO;
import com.utp.meditrackapp.core.dao.ProductoDAO;
import com.utp.meditrackapp.core.models.entity.Categoria;
import com.utp.meditrackapp.core.models.entity.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductoController {

    @FXML private TableView<Producto> tableProductos;
    @FXML private TableColumn<Producto, String> colId, colDigemid, colNombre, colCategoria, colUnidad;
    @FXML private TableColumn<Producto, Integer> colEstado;
    @FXML private TableColumn<Producto, Void> colAcciones;

    @FXML private TextField txtSearch;
    @FXML private Label lblTotalProductos;

    // Modal fields
    @FXML private StackPane modalProducto;
    @FXML private Label modalTitle;
    @FXML private TextField txtNombre, txtDigemid;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<String> cmbUnidad;
    @FXML private TextArea txtDetalle;
    @FXML private Spinner<Integer> spnStockMinimo;
    @FXML private CheckBox chkActivo;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ObservableList<Producto> masterData = FXCollections.observableArrayList();
    private Producto selectedProducto;

    @FXML
    public void initialize() {
        setupTable();
        setupForm();
        loadData();
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDigemid.setCellValueFactory(new PropertyValueFactory<>("codigoDigemid"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoriaNombre"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidadMedida"));

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
            private final Button deleteBtn = new Button();
            {
                editBtn.setGraphic(new FontIcon("fas-edit"));
                editBtn.getStyleClass().addAll("button", "flat", "accent", "sm");
                editBtn.setOnAction(e -> openEditModal(getTableRow().getItem()));

                deleteBtn.setGraphic(new FontIcon("fas-trash"));
                deleteBtn.getStyleClass().addAll("button", "flat", "danger", "sm");
                deleteBtn.setOnAction(e -> confirmDelete(getTableRow().getItem()));
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) setGraphic(null);
                else {
                    HBox box = new HBox(10, editBtn, deleteBtn);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
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

        spnStockMinimo.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 1000, 10));
    }

    @FXML
    public void loadData() {
        try {
            List<Producto> list = productoDAO.listarTodos();
            masterData.setAll(list);
            tableProductos.setItems(masterData);
            lblTotalProductos.setText(String.valueOf(list.size()));
            
            // Reload categories for the modal
            cmbCategoria.setItems(FXCollections.observableArrayList(categoriaDAO.listarTodas()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Error al cargar productos: " + e.getMessage());
        }
    }

    @FXML
    protected void onSearch() {
        String query = txtSearch.getText().toLowerCase();
        List<Producto> filtered = masterData.stream()
            .filter(p -> p.getNombre().toLowerCase().contains(query) || p.getCodigoDigemid().toLowerCase().contains(query))
            .collect(Collectors.toList());
        tableProductos.setItems(FXCollections.observableArrayList(filtered));
    }

    @FXML
    protected void onOpenRegisterModal() {
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
        cmbUnidad.setValue(p.getUnidadMedida());
        chkActivo.setSelected(p.getIsActivo() == 1);
        spnStockMinimo.getValueFactory().setValue(p.getStockMinimo() != null ? p.getStockMinimo() : 10);
        
        cmbCategoria.getItems().stream()
            .filter(c -> c.getId().equals(p.getCategoriaId()))
            .findFirst()
            .ifPresent(cmbCategoria::setValue);

        modalProducto.setVisible(true);
    }

    @FXML
    protected void onSave() {
        if (!validateForm()) return;

        try {
            if (selectedProducto == null) {
                Producto nuevo = new Producto();
                setProductoFromForm(nuevo);
                productoDAO.crear(nuevo);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Producto creado correctamente.");
            } else {
                setProductoFromForm(selectedProducto);
                productoDAO.actualizar(selectedProducto);
                showAlert(Alert.AlertType.INFORMATION, "Éxito", "Producto actualizado correctamente.");
            }
            loadData();
            onCloseModal();
        } catch (SQLException | IllegalArgumentException e) {
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
        p.setStockMinimo(spnStockMinimo.getValue());
    }

    private boolean validateForm() {
        if (txtNombre.getText().isBlank() || txtDigemid.getText().isBlank() || 
            cmbCategoria.getValue() == null || cmbUnidad.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Validación", "Por favor complete todos los campos obligatorios.");
            return false;
        }
        return true;
    }

    private void confirmDelete(Producto p) {
        if (p == null) return;
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "¿Desea desactivar el producto " + p.getNombre() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    productoDAO.desactivar(p.getId());
                    loadData();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "No se pudo desactivar el producto.");
                }
            }
        });
    }

    @FXML protected void onCloseModal() { modalProducto.setVisible(false); }

    private void clearForm() {
        txtNombre.clear();
        txtDigemid.clear();
        txtDetalle.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbUnidad.getSelectionModel().selectFirst();
        chkActivo.setSelected(true);
        spnStockMinimo.getValueFactory().setValue(10);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(content); a.showAndWait();
    }
}
