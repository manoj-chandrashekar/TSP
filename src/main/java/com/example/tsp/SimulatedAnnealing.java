package com.example.tsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealing {
    public static List<City> optimize(List<City> tour, double initialTemp, double coolingRate, int maxIterations) {
        Random random = new Random();
        int n = tour.size();

        double currentCost = calculateTourCost(tour);
        List<City> currentTour = new ArrayList<>(tour);

        double bestCost = currentCost;
        List<City> bestTour = new ArrayList<>(tour);

        double temperature = initialTemp;

        int iterations = 0;
        while (iterations < maxIterations) {
            // Choose random indices i and j (i < j)
            int i = random.nextInt(n - 1);
            int j = i + 1;

            // Create a new tour by swapping cities at i and j
            List<City> newTour = new ArrayList<>(currentTour);
            Collections.swap(newTour, i, j);

            // Calculate the cost of the new tour
            double newCost = calculateTourCost(newTour);

            // Calculate the probability of accepting the new tour
            double delta = newCost - currentCost;
            double acceptanceProbability = delta < 0 ? 1.0 : Math.exp(-delta / temperature);

            // Accept the new tour based on the calculated probability
            if (acceptanceProbability > random.nextDouble()) {
                currentTour = newTour;
                currentCost = newCost;

                if (currentCost < bestCost) {
                    bestTour = new ArrayList<>(currentTour);
                    bestCost = currentCost;
                }
            }

            // Update the temperature
            temperature *= (1 - coolingRate);

            iterations++;
        }

        return bestTour;
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
