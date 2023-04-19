package com.example.tsp.tactical;

import com.example.tsp.model.City;

import java.util.*;
import java.util.concurrent.*;

public class AntColonyOptimization {

    public static List<City> optimize(List<City> initialTour, int numAnts, int numIterations, double alpha, double beta, double evaporationRate) {
        Map<Integer, Integer> cityIndices = new HashMap<>();
        for (int i = 0; i < initialTour.size(); i++) {
            cityIndices.put(initialTour.get(i).getId(), i);
        }

        double[][] pheromoneLevels = initializePheromoneLevels(initialTour.size());
        double[][] distances = calculateDistances(initialTour);

        List<City> bestTour = new ArrayList<>(initialTour);
        double bestTourDistance = calculateTourDistance(initialTour);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int iteration = 0; iteration < numIterations; iteration++) {
            List<Future<List<City>>> antTourFutures = new ArrayList<>();

            for (int i = 0; i < numAnts; i++) {
                Callable<List<City>> callable = () -> constructAntTour(initialTour, distances, pheromoneLevels, alpha, beta, cityIndices);
                Future<List<City>> future = executor.submit(callable);
                antTourFutures.add(future);
            }

            List<List<City>> antTours = new ArrayList<>();
            for (Future<List<City>> future : antTourFutures) {
                try {
                    List<City> antTour = future.get();
                    antTours.add(antTour);
                    double antTourDistance = calculateTourDistance(antTour);

                    if (antTourDistance < bestTourDistance) {
                        bestTour = new ArrayList<>(antTour);
                        bestTourDistance = antTourDistance;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            updatePheromoneLevels(pheromoneLevels, antTours, evaporationRate, cityIndices);
        }

        executor.shutdown();
        return bestTour;
    }

    private static double[][] initializePheromoneLevels(int numCities) {
        double[][] pheromoneLevels = new double[numCities][numCities];
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                pheromoneLevels[i][j] = 1.0;
            }
        }
        return pheromoneLevels;
    }

    private static double[][] calculateDistances(List<City> cities) {
        int numCities = cities.size();
        double[][] distances = new double[numCities][numCities];
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                distances[i][j] = cities.get(i).distanceTo(cities.get(j));
            }
        }
        return distances;
    }

    private static List<City> constructAntTour(List<City> cities, double[][] distances, double[][] pheromoneLevels, double alpha, double beta, Map<Integer, Integer> cityIndices) {
        List<City> tour = new ArrayList<>();
        List<City> remainingCities = new ArrayList<>(cities);

        City startCity = remainingCities.remove(0);
        tour.add(startCity);
        City currentCity = startCity;

        Random random = new Random();
        while (!remainingCities.isEmpty()) {
            double totalProbability = 0;
            for (City city : remainingCities) {
                int i = cityIndices.get(currentCity.getId());
                int j = cityIndices.get(city.getId());
                totalProbability += Math.pow(pheromoneLevels[i][j], alpha) * Math.pow(1 / distances[i][j], beta);
            }

            double selectionValue = random.nextDouble() * totalProbability;
            double accumulatedProbability = 0;
            City nextCity = null;
            Iterator<City> iterator = remainingCities.iterator();
            while (iterator.hasNext() && nextCity == null) {
                City city = iterator.next();
                int i = cityIndices.get(currentCity.getId());
                int j = cityIndices.get(city.getId());
                accumulatedProbability += Math.pow(pheromoneLevels[i][j], alpha) * Math.pow(1 / distances[i][j], beta);
                if (accumulatedProbability >= selectionValue) {
                    nextCity = city;
                    iterator.remove();
                }
            }

            tour.add(nextCity);
            currentCity = nextCity;
        }

        tour.add(startCity);
        return tour;
    }

    private static void updatePheromoneLevels(double[][] pheromoneLevels, List<List<City>> antTours, double evaporationRate, Map<Integer, Integer> cityIndices) {
        int numCities = pheromoneLevels.length;

        // Evaporate pheromones
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                pheromoneLevels[i][j] *= (1 - evaporationRate);
            }
        }

        // Add new pheromones
        for (List<City> tour : antTours) {
            double tourDistance = calculateTourDistance(tour);

            for (int i = 0; i < tour.size() - 1; i++) {
                City city1 = tour.get(i);
                City city2 = tour.get(i + 1);

                int id1 = cityIndices.get(city1.getId());
                int id2 = cityIndices.get(city2.getId());

                pheromoneLevels[id1][id2] += 1 / tourDistance;
                pheromoneLevels[id2][id1] += 1 / tourDistance;
            }
        }
    }

    private static double calculateTourDistance(List<City> tour) {
        double totalDistance = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            totalDistance += tour.get(i).distanceTo(tour.get(i + 1));
        }
        return totalDistance;
    }
}
