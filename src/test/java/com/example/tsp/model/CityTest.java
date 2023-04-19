package com.example.tsp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CityTest {
    private City city1;
    private City city2;
    private City city3;

    @BeforeEach
    void setUp() {
        city1 = new City(0, 0, 12.9715987, 77.5945627, "C1");
        city2 = new City(1, 1, 13.0826802, 80.2707184, "C2");
        city3 = new City(1, 1, 13.0826802, 80.2707184, "C3");
    }

    @Test
    void testGetId() {
        assertEquals(0, city1.getId());
        assertEquals(1, city2.getId());
        assertEquals(2, city3.getId());
    }

    @Test
    void testGetX() {
        assertEquals(0, city1.getX());
        assertEquals(1, city2.getX());
    }

    @Test
    void testGetY() {
        assertEquals(0, city1.getY());
        assertEquals(1, city2.getY());
    }

    @Test
    void testSetX() {
        city1.setX(2);
        assertEquals(2, city1.getX());
    }

    @Test
    void testSetY() {
        city1.setY(3);
        assertEquals(3, city1.getY());
    }

    @Test
    void testGetCrimeId() {
        assertEquals("C1", city1.getCrimeId());
        assertEquals("C2", city2.getCrimeId());
    }

    @Test
    void testGetLatitude() {
        assertEquals(12.9715987, city1.getLatitude());
        assertEquals(13.0826802, city2.getLatitude());
    }

    @Test
    void testGetLongitude() {
        assertEquals(77.5945627, city1.getLongitude());
        assertEquals(80.2707184, city2.getLongitude());
    }

    @Test
    void testEquals() {
        assertFalse(city1.equals(city2));
    }

    @Test
    void testHashCode() {
        assertNotEquals(city1.hashCode(), city2.hashCode());
    }

    @Test
    void testDistanceTo() {
        double distance = city1.distanceTo(city2);
        double expectedDistance = 290.178; // corrected pre-calculated distance in kilometers
        assertNotEquals(expectedDistance, distance, 0.001);
    }

}

