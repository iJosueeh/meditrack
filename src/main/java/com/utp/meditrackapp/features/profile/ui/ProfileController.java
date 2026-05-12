package com.utp.meditrackapp.features.profile.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

import java.io.IOException;

public class ProfileController {

    @FXML
    private BorderPane profileRootPane; // Assuming the root BorderPane needs to be referenced

    // Labels for personal info (example for data binding later)
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label personalFullNameValue;
    @FXML
    private Label personalDMIValue;
    @FXML
    private Label personalEmailValue;
    @FXML
    private Label personalPhoneValue;

    // Activity Summary
    @FXML
    private Label lastAccessValue;
    @FXML
    private Label reportsGeneratedValue;
    @FXML
    private Label accountStatusValue;

    // System Preferences
    @FXML
    private ComboBox<String> languageComboBox;
    @FXML
    private ComboBox<String> notificationFrequencyComboBox;
    @FXML
    private CheckBox emailNotificationsToggle;
    @FXML
    private CheckBox highDensityModeToggle;


    @FXML
    public void initialize() {
        // Initialize with dummy data for now
        if (fullNameLabel != null) fullNameLabel.setText("Dr. Alejandro Rivas");
        if (roleLabel != null) roleLabel.setText("Administrador");
        if (statusLabel != null) statusLabel.setText("Estado Central");
        if (personalFullNameValue != null) personalFullNameValue.setText("Alejandro Rivas Montesinos");
        if (personalDMIValue != null) personalDMIValue.setText("7844525D-X");
        if (personalEmailValue != null) personalEmailValue.setText("alejandro.rivas@meditrackystem");
        if (personalPhoneValue != null) personalPhoneValue.setText("+34 8 1677 60560");

        if (lastAccessValue != null) lastAccessValue.setText("Hoy, 08:45 AM");
        if (reportsGeneratedValue != null) reportsGeneratedValue.setText("124");
        if (accountStatusValue != null) accountStatusValue.setText("ACTIVO");

        if (languageComboBox != null) {
            languageComboBox.getItems().addAll("Español (Latinoamericano)", "English", "Português");
            languageComboBox.getSelectionModel().select("Español (Latinoamericano)");
        }
        if (notificationFrequencyComboBox != null) {
            notificationFrequencyComboBox.getItems().addAll("Inmediatas", "Diarias", "Semanales");
            notificationFrequencyComboBox.getSelectionModel().select("Inmediatas");
        }
    }


    // Sidebar Navigation Actions
    @FXML
    protected void onGoToDashboard() throws IOException {
        NavigationService.toDashboard();
    }

    // Header and Sidebar Logout
    @FXML
    protected void onLogout() throws IOException {
        NavigationService.toLogin(); // Assuming logout navigates to login
    }

    // Security & Access Actions
    @FXML
    protected void onChangePassword() {
        System.out.println("Changing password...");
        // Logic to open change password dialog/view
    }

    @FXML
    protected void onConfigure2FA() {
        System.out.println("Configuring 2FA...");
        // Logic to open 2FA configuration dialog/view
    }

    // System Preferences Actions
    @FXML
    protected void onSelectLanguage() {
        if (languageComboBox != null) {
            System.out.println("Selected language: " + languageComboBox.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    protected void onSelectNotificationFrequency() {
        if (notificationFrequencyComboBox != null) {
            System.out.println("Selected notification frequency: " + notificationFrequencyComboBox.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    protected void onToggleEmailNotifications() {
        if (emailNotificationsToggle != null) {
            System.out.println("Email notifications toggled: " + emailNotificationsToggle.isSelected());
        }
    }

    @FXML
    protected void onToggleHighDensityMode() {
        if (highDensityModeToggle != null) {
            System.out.println("High density mode toggled: " + highDensityModeToggle.isSelected());
        }
    }

    // Account Actions
    @FXML
    protected void onCloseAllSessions() {
        System.out.println("Closing all active sessions...");
        // Logic to confirm and close all sessions
    }
}