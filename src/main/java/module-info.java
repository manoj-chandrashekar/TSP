module com.example.tsp {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jgrapht.core;


    opens com.example.tsp to javafx.fxml;
    exports com.example.tsp;
}