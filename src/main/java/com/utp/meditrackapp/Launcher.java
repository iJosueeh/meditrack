package com.utp.meditrackapp;

import com.utp.meditrackapp.core.App;
import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        com.utp.meditrackapp.core.util.DbInitializer.initialize();
        Application.launch(App.class, args);
    }
}
