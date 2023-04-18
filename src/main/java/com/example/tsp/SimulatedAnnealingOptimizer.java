package com.example.tsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimulatedAnnealingOptimizer {
    private final double initialTemperature;
    private final double coolingRate;
    private final int maxIterations;

    public SimulatedAnnealingOptimizer(double initialTemperature, double coolingRate, int maxIterations) {
        this.initialTemperature = initialTemperature;
        this.coolingRate = coolingRate;
        this.maxIterations = maxIterations;
    }

    public List<City> optimizeTour(List<City> initialTour) {
        List<City> currentTour = new ArrayList<>(initialTour);
        List<City> bestTour = new ArrayList<>(initialTour);
        double currentTemperature = initialTemperature;
        Random random = new Random();

        for (int i = 0; i < maxIterations; i++) {
            List<City> newTour = new ArrayList<>(currentTour);

            int index1 = random.nextInt(newTour.size());
            int index2 = random.nextInt(newTour.size());
            Collections.swap(newTour, index1, index2);

            double currentTourDistance = calculateTourDistance(currentTour);
            double newTourDistance = calculateTourDistance(newTour);
            double deltaDistance = newTourDistance - currentTourDistance;

            if (deltaDistance < 0 || random.nextDouble() < Math.exp(-deltaDistance / currentTemperature)) {
                currentTour = newTour;
                if (newTourDistance < calculateTourDistance(bestTour)) {
                    bestTour = new ArrayList<>(newTour);
                }
            }

            currentTemperature *= coolingRate;
            //System.out.println("Iteration: " + (i + 1) + " | Current tour length: " + currentTourDistance + " | Best tour length: " + bestTour + " | Temperature: " + currentTemperature);
        }

        return bestTour;
    }

    private double calculateTourDistance(List<City> tour) {
        double totalDistance = 0;
        int tourSize = tour.size();
        for (int i = 0; i < tourSize; i++) {
            City from = tour.get(i);
            City to = (i + 1 < tourSize) ? tour.get(i + 1) : tour.get(0); // Go back to the first city at the end
            totalDistance += from.distanceTo(to);
        }
        return totalDistance;
    }
}

