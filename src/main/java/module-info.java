module com.utp.meditrackapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires io.github.cdimascio.dotenv.java;
    requires java.sql;

    opens com.utp.meditrackapp to javafx.fxml;

    exports com.utp.meditrackapp;
    exports com.utp.meditrackapp.core;

    opens com.utp.meditrackapp.core to javafx.fxml;
}