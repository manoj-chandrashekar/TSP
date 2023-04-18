package com.example.tsp.Utility;

import com.example.tsp.City;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class FileUtil {
    public static void writeTourToCsv(List<City> tour, String fileName) {
        String outputDir = "src/main/java/com/example/tsp/output/";

        try {
            File outputFile = new File(outputDir + fileName);
            FileWriter writer = new FileWriter(outputFile);

            // Write headers to CSV file

            writer.append("id,latitude,longitude\n");

            // Write data to CSV file
            for (City city : tour) {
                writer.append(city.getCrimeId() + "," + city.getLatitude() + "," + city.getLongitude() + "\n");
            }

            writer.flush();
            writer.close();
            System.out.println("Tour saved to " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
