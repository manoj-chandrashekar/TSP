package com.example.tsp.tactical;

import com.example.tsp.Utility.PlottingUtil;
import com.example.tsp.model.City;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChristofidesTSPTest {

    @Test
    void testOptimize_smallInput() {
        List<City> cities = new ArrayList<>(Arrays.asList(
                new City(0, 0, 37.7749, -122.4194, "SFO"),
                new City(1, 1, 34.0522, -118.2437, "LAX"),
                new City(2, 2, 41.8781, -87.6298, "CHI"),
                new City(3, 3, 40.7128, -74.0060, "NYC")
        ));

        List<City> optimizedCities = ChristofidesTSP.optimize(cities);
        double optimizedCost = ChristofidesTSP.calculateSolutionCost(optimizedCities);

        double optimalCost = 9362.993 * 1000; // This is the actual optimal cost for this specific problem in meters
        double maxAllowedCost = 1.5 * optimalCost;
        //checking if Christofide tour is within 1.5 times the optimalCost
        assertTrue(optimizedCost <= maxAllowedCost);
    }

    @Test
    void testExpectedCost_withCSVData() throws URISyntaxException {
        URL resourceUrl = getClass().getClassLoader().getResource("test1.csv");
        File file = new File(resourceUrl.toURI());

        List<City> cities = PlottingUtil.readCitiesFromCSV(file);
        List<City> optimizedRoute = ChristofidesTSP.optimize(cities);
        double optimizedCost = ChristofidesTSP.calculateSolutionCost(optimizedRoute);

        double expectedCost = 75500;
        assertEquals(expectedCost, optimizedCost, 20, "Optimized cost does not match the expected value");
    }

    @Test
    void testOptimize_largeInput() {
        List<City> cities = new ArrayList<>(Arrays.asList(
                new City(0, 0, 37.7749, -122.4194, "SFO"),
                new City(1, 1, 34.0522, -118.2437, "LAX"),
                new City(2, 2, 41.8781, -87.6298, "CHI"),
                new City(3, 3, 40.7128, -74.0060, "NYC"),
                new City(4, 4, 29.7604, -95.3698, "HOU"),
                new City(5, 5, 39.7392, -104.9903, "DEN"),
                new City(6, 6, 25.7617, -80.1918, "MIA"),
                new City(7, 7, 47.6062, -122.3321, "SEA"),
                new City(8, 8, 33.4484, -112.0740, "PHX"),
                new City(9, 9, 39.9526, -75.1652, "PHI")
        ));

        // Get the optimal cost
        double optimalCost = 14388.618663 * 1000;

        // Get the optimized cities and calculate the optimized cost
        List<City> optimizedCities = ChristofidesTSP.optimize(cities);
        double optimizedCost = ChristofidesTSP.calculateSolutionCost(optimizedCities);

        // Calculate the maximum allowed cost for the optimized solution
        double maxAllowedCost = optimalCost * 1.5;

        // Assert that the optimized cost is within the maximum allowed cost
        assertTrue(optimizedCost <= maxAllowedCost, "Optimized cost is not within 1.5 times the optimal solution");
    }

    @Test
    void testOptimize_emptyList() {
        List<City> cities = new ArrayList<>();
        List<City> optimizedCities = ChristofidesTSP.optimize(cities);
        assertEquals(0, optimizedCities.size());
    }

    @Test
    void testOptimize_nullList() {
        List<City> cities = null;
        assertThrows(NullPointerException.class, () -> ChristofidesTSP.optimize(cities));
    }

    @Test
    void testOptimize_singleCity() {
        List<City> cities = new ArrayList<>(Arrays.asList(new City(0, 0, 37.7749, -122.4194, "SFO")));
        List<City> optimizedCities = ChristofidesTSP.optimize(cities);
        assertEquals(0, optimizedCities.size());
    }


}


