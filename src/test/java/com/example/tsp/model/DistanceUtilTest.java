package com.example.tsp.model;

import com.example.tsp.Utility.DistanceUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DistanceUtilTest {

    @Test
    void testHaversineDistance_sameLocation() {
        double lat1 = 12.9715987;
        double lon1 = 77.5945627;
        double lat2 = 12.9715987;
        double lon2 = 77.5945627;

        double distance = DistanceUtil.haversineDistance(lat1, lon1, lat2, lon2);
        assertEquals(0, distance, 0.001);
    }

    @Test
    void testHaversineDistance_differentLocations() {
        double lat1 = 12.9715987;
        double lon1 = 77.5945627;
        double lat2 = 13.0826802;
        double lon2 = 80.2707184;

        double distance = DistanceUtil.haversineDistance(lat1, lon1, lat2, lon2);
        double expectedDistance = 290_177.978; // corrected pre-calculated distance in meters
        assertEquals(expectedDistance, distance, 1);
    }

    @Test
    void testHaversineDistance_oppositeLocations() {
        double lat1 = 90;
        double lon1 = 0;
        double lat2 = -90;
        double lon2 = 180;

        double distance = DistanceUtil.haversineDistance(lat1, lon1, lat2, lon2);
        double expectedDistance = 20_015_086.796; // approximately half of Earth's circumference in meters
        assertEquals(expectedDistance, distance, 1000);
    }

}
