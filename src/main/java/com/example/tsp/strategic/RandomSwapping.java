package com.example.tsp.strategic;

import com.example.tsp.model.City;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomSwapping {
    public static List<City> optimize(List<City> tour, int maxIterations) {
        if(tour.size() < 3) return tour;
        Random random = new Random();
        int n = tour.size();
        int iterations = 0;

        while (iterations < maxIterations) {
            // Choose random indices i and j (i < j)
            int i = random.nextInt(n - 1);
            int j = i + 1;

            // Create a new tour by swapping cities at i and j
            List<City> newTour = new ArrayList<>(tour);
            Collections.swap(newTour, i, j);

            // Calculate the cost of the new tour
            double newCost = calculateTourCost(newTour);

            // If the new tour has a lower cost, replace the current tour
            if (newCost < calculateTourCost(tour)) {
                tour = newTour;
            }

            iterations++;
        }

        return tour;
    }

    public static double calculateTourCost(List<City> tour) {
        double cost = 0.0;
        City prev = tour.get(0);

        for (int i = 1; i < tour.size(); i++) {
            City curr = tour.get(i);
            cost += prev.distanceTo(curr);
            prev = curr;
        }

        // Add distance from the last city to the first city
        cost += prev.distanceTo(tour.get(0));

        return cost;
    }
}
