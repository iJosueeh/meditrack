package com.utp.meditrackapp.features.dashboard.models;

/**
 * POJO para representar el resumen de medicamentos en el Dashboard.
 * Ubicado en un paquete específico para facilitar el acceso de JavaFX TableView.
 */
public class MedicamentoResumen {
    private String code;
    private String name;
    private String category;
    private int currentStock;
    private int minStock;
    private String status;

    public MedicamentoResumen() {}

    public MedicamentoResumen(String code, String name, String category, int currentStock, int minStock, String status) {
        this.code = code;
        this.name = name;
        this.category = category;
        this.currentStock = currentStock;
        this.minStock = minStock;
        this.status = status;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getMinStock() { return minStock; }
    public void setMinStock(int minStock) { this.minStock = minStock; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
