module com.utp.meditrackapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.utp.meditrackapp to javafx.fxml;
    exports com.utp.meditrackapp;
}