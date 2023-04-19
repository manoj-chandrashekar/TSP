package com.example.tsp.strategic;

import com.example.tsp.model.City;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TwoOptOptimizer {

    public static List<City> optimize(List<City> tour) {
        if(tour.size() < 2) return new ArrayList<>();
        boolean improved = true;

        while (improved) {
            improved = false;

            for (int i = 0; i < tour.size() - 2; i++) {
                for (int j = i + 2; j < tour.size() - 1; j++) {
                    double oldDistance = tour.get(i).distanceTo(tour.get(i + 1)) + tour.get(j).distanceTo(tour.get(j + 1));
                    double newDistance = tour.get(i).distanceTo(tour.get(j)) + tour.get(i + 1).distanceTo(tour.get(j + 1));

                    if (newDistance < oldDistance) {
                        reverseSubList(tour, i + 1, j);
                        improved = true;
                    }
                }
            }
        }
        return tour;
    }

    private static void reverseSubList(List<City> list, int start, int end) {
        while (start < end) {
            Collections.swap(list, start, end);
            start++;
            end--;
        }
    }
}
