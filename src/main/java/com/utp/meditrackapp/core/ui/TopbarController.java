package com.utp.meditrackapp.core.ui;

import com.utp.meditrackapp.core.config.NavigationService;
import com.utp.meditrackapp.core.config.SessionManager;
import com.utp.meditrackapp.features.search.dao.GlobalSearchDAO;
import com.utp.meditrackapp.features.search.models.SearchResult;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class TopbarController {

    @FXML private FontIcon themeIcon;
    @FXML private TextField txtGlobalSearch;
    
    private final GlobalSearchDAO searchDAO = new GlobalSearchDAO();
    private ContextMenu searchResultsPopup;

    @FXML
    public void initialize() {
        updateThemeIcon();
        setupGlobalSearch();
    }

    private void setupGlobalSearch() {
        searchResultsPopup = new ContextMenu();
        searchResultsPopup.getStyleClass().add("search-results-popup");
        
        txtGlobalSearch.textProperty().addListener((obs, old, newValue) -> {
            if (newValue == null || newValue.trim().length() < 2) {
                searchResultsPopup.hide();
                return;
            }
            performSearch(newValue.trim());
        });

        // Evento para navegar con Enter si hay un solo resultado
        txtGlobalSearch.setOnAction(event -> {
            if (!searchResultsPopup.getItems().isEmpty()) {
                searchResultsPopup.getItems().get(0).fire();
            }
        });
    }

    private void performSearch(String query) {
        try {
            List<SearchResult> results = searchDAO.searchGlobal(query);
            searchResultsPopup.getItems().clear();

            if (results.isEmpty()) {
                MenuItem noResults = new MenuItem("No se encontraron resultados");
                noResults.setDisable(true);
                searchResultsPopup.getItems().add(noResults);
            } else {
                for (SearchResult res : results) {
                    MenuItem item = createSearchResultItem(res);
                    searchResultsPopup.getItems().add(item);
                }
            }

            if (!searchResultsPopup.isShowing()) {
                searchResultsPopup.show(txtGlobalSearch, javafx.geometry.Side.BOTTOM, 0, 5);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private MenuItem createSearchResultItem(SearchResult res) {
        HBox box = new HBox(12);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        box.setPadding(new javafx.geometry.Insets(5, 10, 5, 10));

        FontIcon icon = new FontIcon();
        switch (res.getType()) {
            case PATIENT -> icon.setIconLiteral("fas-user-injured");
            case PRODUCT -> icon.setIconLiteral("fas-box");
            case BATCH   -> icon.setIconLiteral("fas-barcode");
            case MODULE  -> icon.setIconLiteral("fas-external-link-alt");
        }
        icon.setIconSize(14);
        icon.getStyleClass().add("icon-accent");

        VBox textData = new VBox(2);
        Label title = new Label(res.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        Label subtitle = new Label(res.getSubtitle());
        subtitle.getStyleClass().add("text-muted");
        subtitle.setStyle("-fx-font-size: 11px;");

        textData.getChildren().addAll(title, subtitle);
        box.getChildren().addAll(icon, textData);

        CustomMenuItem menuItem = new CustomMenuItem(box);
        menuItem.setHideOnClick(true);
        menuItem.setOnAction(e -> handleNavigation(res));
        
        return menuItem;
    }

    private void handleNavigation(SearchResult res) {
        try {
            switch (res.getType()) {
                case MODULE -> {
                    if ("NAV_INV".equals(res.getId())) NavigationService.toInventory();
                    else if ("NAV_ATT".equals(res.getId())) NavigationService.toAttention();
                    else if ("NAV_SEDE".equals(res.getId())) NavigationService.toSedes();
                }
                case PATIENT -> {
                    NavigationService.toPatients(res.getTitle());
                }
                case PRODUCT -> {
                    NavigationService.toProductos(res.getTitle());
                }
                case BATCH -> {
                    NavigationService.toInventory(res.getTitle());
                }
            }
            txtGlobalSearch.clear();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    protected void onToggleTheme() {
        boolean isDark = NavigationService.isDarkThemeEnabled();
        NavigationService.setDarkThemeEnabled(!isDark);
        updateThemeIcon();
        
        // Efecto suave al cambiar
        if (txtGlobalSearch.getScene() != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(300), txtGlobalSearch.getScene().getRoot());
            ft.setFromValue(0.8);
            ft.setToValue(1.0);
            ft.play();
        }
    }

    private void updateThemeIcon() {
        if (NavigationService.isDarkThemeEnabled()) {
            themeIcon.setIconLiteral("fas-sun");
        } else {
            themeIcon.setIconLiteral("fas-moon");
        }
    }

    @FXML
    protected void onGoToProfile() throws IOException {
        NavigationService.toProfile();
    }

    @FXML
    protected void onLogout() throws IOException {
        SessionManager.getInstance().logout();
        NavigationService.toLogin();
    }
}
