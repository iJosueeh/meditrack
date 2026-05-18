package com.utp.meditrackapp.core.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.core.models.entity.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.io.IOException;

public class SidebarController {

    @FXML
    private Label userNameLabel;
    @FXML
    private Label userSedeLabel;

    @FXML
    public void initialize() {
        Usuario user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userNameLabel.setText(user.getNombres() + " " + user.getApellidos());
            String sede = user.getSedeNombre();
            userSedeLabel.setText(sede != null ? sede : "Sin Sede");
        }
    }

    @FXML
    protected void onGoToDashboard() throws IOException {
        NavigationService.toDashboard();
    }

    @FXML
    protected void onGoToPatients() {
        // NavigationService.toPatients();
    }

    @FXML
    protected void onGoToInventory() {
        // NavigationService.toInventory();
    }
}
