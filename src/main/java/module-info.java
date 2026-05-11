module com.utp.meditrackapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.sql;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    opens com.utp.meditrackapp to javafx.fxml;
    opens com.utp.meditrackapp.features.auth.ui to javafx.fxml;
    opens com.utp.meditrackapp.features.dashboard.ui to javafx.fxml;

    exports com.utp.meditrackapp;
    exports com.utp.meditrackapp.core;

    opens com.utp.meditrackapp.core to javafx.fxml;
}