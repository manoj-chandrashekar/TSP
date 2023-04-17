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
}