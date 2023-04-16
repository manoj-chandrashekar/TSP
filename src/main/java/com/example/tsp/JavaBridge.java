package com.example.tsp;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class JavaBridge {
    private final StringProperty message = new SimpleStringProperty();

    public void updateMapData(String data, String newOrder) {
        message.set(String.format("{\"data\": %s, \"newOrder\": %s}", data, newOrder));
    }

    public StringProperty messageProperty() {
        return message;
    }
}
