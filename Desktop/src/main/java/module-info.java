module com.msu.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;
    requires firebase.admin;
    requires com.google.auth.oauth2;
    requires com.google.auth;

    exports com.kabc.desktop.database.sqlite;
    opens com.kabc.desktop.database.sqlite to javafx.fxml;
    exports com.kabc.desktop.unit;
    opens com.kabc.desktop.unit to javafx.fxml;
    exports com.kabc.desktop.controller;
    opens com.kabc.desktop.controller to javafx.fxml;
    exports com.kabc.desktop.date;
    opens com.kabc.desktop.date to javafx.fxml;
    exports com.kabc.desktop.main;
    opens com.kabc.desktop.main to javafx.fxml;
    exports com.kabc.desktop.database.firebase;
    opens com.kabc.desktop.database.firebase to javafx.fxml;
}