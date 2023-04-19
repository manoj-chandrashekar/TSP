package com.example.tsp.Utility;

import com.example.tsp.model.City;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlottingUtil {

    public static List<City> readCitiesFromCSV(File file) {
        List<City> cities = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip the header row if it exists
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 3) {
                    String crimeIdLong = values[0];
                    String crimeId = crimeIdLong.substring(crimeIdLong.length() - 5);
                    double longitude = Double.parseDouble(values[1]);
                    double latitude = Double.parseDouble(values[2]);
                    cities.add(new City(longitude, latitude, latitude, longitude, crimeId));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cities;
    }

    public static List<City> readCitiesFromOldCSV(File file) {
        List<City> cities = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // Skip the header row if it exists
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length >= 3 && values[0] != null && values[4] != null && values[5] != null
                && !values[0].isEmpty() && !values[4].isEmpty() && !values[5].isEmpty()) {
                    String crimeIdLong = values[0];
                    double longitude = Double.parseDouble(values[4]);
                    double latitude = Double.parseDouble(values[5]);
                    cities.add(new City(longitude, latitude, latitude, longitude, crimeIdLong));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cities;
    }

    public static void plotCities(GraphicsContext citiesGc, GraphicsContext linesGc, Canvas citiesCanvas, Canvas linesCanvas, List<City> cities) {
        citiesGc.clearRect(0, 0, citiesCanvas.getWidth(), citiesCanvas.getHeight());
        linesGc.clearRect(0, 0, linesCanvas.getWidth(), linesCanvas.getHeight());

        // Calculate the min and max values for longitude and latitude
        double minX = cities.stream().mapToDouble(City::getX).min().orElse(0.0);
        double maxX = cities.stream().mapToDouble(City::getX).max().orElse(0.0);
        double minY = cities.stream().mapToDouble(City::getY).min().orElse(0.0);
        double maxY = cities.stream().mapToDouble(City::getY).max().orElse(0.0);

        // Padding
        double padding = 60;

        // Calculate the scaling factors and offsets
        double scaleX = (citiesCanvas.getWidth() - padding) / (maxX - minX);
        double scaleY = (citiesCanvas.getHeight() - 2 * padding) / (maxY - minY);
        double offsetX = minX;
        double offsetY = maxY;

        double pointRadius = 2.5;

        for (int i = 0; i < cities.size(); i++) {
            City city = cities.get(i);
            double canvasX = 20 + (city.getX() - offsetX) * scaleX;
            double canvasY = 20 + (offsetY - city.getY()) * scaleY;

            city.setX(canvasX); // Update the city's X coordinate
            city.setY(canvasY); // Update the city's Y coordinate

            citiesGc.setFill(Color.BLUE);
            citiesGc.fillOval(canvasX - pointRadius, canvasY - pointRadius,  pointRadius * 2, pointRadius * 2);

            // Draw labels
            /*String labelText = city.getCrimeId();
            citiesGc.setFill(Color.BLACK);
            citiesGc.setFont(Font.font(10));
            citiesGc.fillText(labelText, canvasX + pointRadius * 2, canvasY - pointRadius);*/
        }

        // Draw lines between cities on the linesCanvas
        for (int i = 1; i < cities.size(); i++) {
            City from = cities.get(i - 1);
            City to = cities.get(i);
            linesGc.setStroke(Color.BLACK);
            linesGc.setLineWidth(1);
            linesGc.strokeLine(from.getX(), from.getY(), to.getX(), to.getY());
        }

        // Connect the last city to the first one
        City firstCity = cities.get(0);
        City lastCity = cities.get(cities.size() - 1);
        linesGc.setStroke(Color.BLACK);
        linesGc.setLineWidth(1);
        linesGc.strokeLine(firstCity.getX(), firstCity.getY(), lastCity.getX(), lastCity.getY());
    }
}
