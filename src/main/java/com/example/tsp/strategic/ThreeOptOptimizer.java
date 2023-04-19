package com.example.tsp.strategic;

import com.example.tsp.model.City;

import java.util.ArrayList;
import java.util.List;

public class ThreeOptOptimizer {
    public static List<City> optimize(List<City> tour) {
        boolean improvement = true;
        while (improvement) {
            improvement = false;
            for (int i = 0; i < tour.size() - 2; i++) {
                for (int j = i + 1; j < tour.size() - 1; j++) {
                    for (int k = j + 1; k < tour.size(); k++) {
                        if (shouldSwap(tour, i, j, k)) {
                            tour = swap(tour, i, j, k);
                            improvement = true;
                        }
                    }
                }
            }
        }
        return tour;
    }

    private static boolean shouldSwap(List<City> tour, int i, int j, int k) {
        City a = tour.get(i);
        City b = tour.get(i + 1);
        City c = tour.get(j);
        City d = tour.get(j + 1);
        City e = tour.get(k);
        City f = tour.get((k + 1) % tour.size());

        double originalDistance = a.distanceTo(b) + c.distanceTo(d) + e.distanceTo(f);
        double newDistance = a.distanceTo(c) + b.distanceTo(e) + d.distanceTo(f);

        return newDistance < originalDistance;
    }

    private static List<City> swap(List<City> tour, int i, int j, int k) {
        List<City> newTour = new ArrayList<>(tour.size());

        // Add the unchanged part of the tour
        for (int index = 0; index <= i; index++) {
            newTour.add(tour.get(index));
        }

        // Add the reversed part between i+1 and j
        for (int index = j; index > i; index--) {
            newTour.add(tour.get(index));
        }

        // Add the reversed part between j+1 and k
        for (int index = k; index > j; index--) {
            newTour.add(tour.get(index));
        }

        // Add the unchanged part between k+1 and the end
        for (int index = k + 1; index < tour.size(); index++) {
            newTour.add(tour.get(index));
        }

        return newTour;
    }
}

