module com.example.tsp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jgrapht.core;
    requires java.datatransfer;
    requires com.opencsv;
    requires javafx.web;
    requires jdk.jsobject;
    requires com.google.gson;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;


    opens com.example.tsp to javafx.fxml,com.google.gson;
    exports com.example.tsp;
    exports com.example.tsp.strategic;
    opens com.example.tsp.strategic to com.google.gson, javafx.fxml;
    exports com.example.tsp.tactical;
    opens com.example.tsp.tactical to com.google.gson, javafx.fxml;
    exports com.example.tsp.model;
    opens com.example.tsp.model to com.google.gson, javafx.fxml;
}