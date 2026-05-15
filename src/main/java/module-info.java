module com.utp.meditrackapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.sql;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires com.zaxxer.hikari;

    opens com.utp.meditrackapp to javafx.fxml;
    opens com.utp.meditrackapp.features.auth.ui to javafx.fxml;
    opens com.utp.meditrackapp.features.dashboard.ui to javafx.fxml;
    opens com.utp.meditrackapp.features.profile.ui to javafx.fxml;
    opens com.utp.meditrackapp.core.ui to javafx.fxml;
    opens com.utp.meditrackapp.core.models.entity to javafx.base;
    opens com.utp.meditrackapp.features.dashboard.models to javafx.base;

    exports com.utp.meditrackapp;
    exports com.utp.meditrackapp.core;
    exports com.utp.meditrackapp.core.models.entity;
    exports com.utp.meditrackapp.features.dashboard.models;

    opens com.utp.meditrackapp.core to javafx.fxml;
}