package com.example.tsp.model;

//import javafx.scene.paint.Color;
//import javafx.scene.paint.Color;
//import javafx.scene.shape.Circle;

import com.example.tsp.Utility.DistanceUtil;

public class City {
    private static int idCounter = 0;

    private  int id;
    private  double x;
    private  double y;
    private double latitude;
    private double longitude;
    private String crimeId;

    public City(double x, double y, double latitude, double longitude, String crimeId) {
        this.id = idCounter++;
        this.x = x;
        this.y = y;
        this.latitude = latitude;
        this.longitude = longitude;
        this.crimeId = crimeId;
    }

    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
    public void setY(double y) {
        this.y = y;
    }
    public void setX(double x) {
        this.x = x;
    }

    public String getCrimeId() {
        return crimeId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof City)) {
            return false;
        }
        City other = (City) obj;
        return this.id == other.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    public double distanceTo(City other) {
//        double dx = x - other.x;
//        double dy = y - other.y;
//        return Math.sqrt(dx * dx + dy * dy);
        return DistanceUtil.haversineDistance(this.latitude, this.longitude, other.latitude, other.longitude);
    }
}
