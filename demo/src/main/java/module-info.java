module ma.enset.demo {
    requires javafx.fxml;
    requires java.sql;
    requires org.json;

    requires org.kordamp.ikonli.javafx;
    requires javafx.controls;


    opens ma.enset.demo to javafx.fxml;
    exports ma.enset.demo;
    exports ma.enset.demo.controllers;
    opens ma.enset.demo.controllers to javafx.fxml;
}