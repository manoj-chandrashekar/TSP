package com.example.tsp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GeneticAlgorithm {

    private final int populationSize;

    private final int generations;
    private final double mutationRate;
    private final int tournamentSize;
    private final Random random;

    public GeneticAlgorithm(int populationSize, int generations, double mutationRate, int tournamentSize) {
        this.populationSize = populationSize;
        this.generations = generations;
        this.mutationRate = mutationRate;
        this.tournamentSize = tournamentSize;
        this.random = new Random();
    }

    public List<City> optimizeTour(List<City> initialTour) {
        List<List<City>> population = initializePopulation(initialTour, populationSize);

        for (int generation = 0; generation < generations; generation++) {
            population = evolvePopulation(population);
        }

        List<City> bestTour = rankTours(population).get(0);
        return bestTour;
    }

    private List<List<City>> initializePopulation(List<City> initialTour, int populationSize) {
        List<List<City>> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            List<City> newTour = new ArrayList<>(initialTour);
            Collections.shuffle(newTour);
            population.add(newTour);
        }
        return population;
    }

    private List<List<City>> rankTours(List<List<City>> population) {
        return population.stream()
                .sorted(Comparator.comparingDouble(this::calculateTourDistance))
                .collect(Collectors.toList());
    }

    private List<City> tournamentSelection(List<List<City>> population) {
        List<List<City>> tournament = new ArrayList<>();
        for (int i = 0; i < tournamentSize; i++) {
            int randomIndex = random.nextInt(population.size());
            tournament.add(population.get(randomIndex));
        }
        return rankTours(tournament).get(0);
    }

    private List<City> orderedCrossover(List<City> parent1, List<City> parent2) {
        int size = parent1.size();
        int startIndex = random.nextInt(size);
        int endIndex = startIndex + random.nextInt(size - startIndex);

        List<City> child = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            child.add(null);
        }

        for (int i = startIndex; i <= endIndex; i++) {
            child.set(i, parent1.get(i));
        }

        int parent2Index = 0;
        for (int i = 0; i < size; i++) {
            if (!child.contains(parent2.get(parent2Index))) {
                int childIndex = (endIndex + 1 + i) % size;
                child.set(childIndex, parent2.get(parent2Index));
            }
            parent2Index++;
        }
        return child;
    }

    private List<City> mutation(List<City> tour) {
        List<City> mutatedTour = new ArrayList<>(tour);
        for (int i = 0; i < mutatedTour.size(); i++) {
            if (random.nextDouble() < mutationRate) {
                int swapIndex = random.nextInt(mutatedTour.size());
                Collections.swap(mutatedTour, i, swapIndex);
            }
        }
        return mutatedTour;
    }

    private List<List<City>> evolvePopulation(List<List<City>> population) {
        List<List<City>> newPopulation = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            List<City> parent1 = tournamentSelection(population);
            List<City> parent2 = tournamentSelection(population);
            List<City> child = orderedCrossover(parent1, parent2);
            child = mutation(child);
            newPopulation.add(child);
        }
        return newPopulation;
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

