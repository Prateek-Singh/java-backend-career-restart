package com.prateek.learning.dsa.day06;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KthClosestToOriginTest {

    private KthClosestToOrigin kthClosestToOrigin  = new KthClosestToOrigin();

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInputIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kthClosestToOrigin.kClosest(null, 2);
        });
        assertEquals("points must not be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInputIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kthClosestToOrigin.kClosest(new int[0][0], 2);
        });
        assertEquals("points must not be null or empty", exception.getMessage());
    }

    @Test
    void shouldReturnSingleResultWhenKIsOne() {
        int[][] points = {
                {1, 3},
                {-2, 2},
                {5, 8},
                {0, 1}
        };

        int[][] result = kthClosestToOrigin.kClosest(points, 1);

        Set<String> actual = Arrays.stream(result)
                .map(point -> point[0] + "," + point[1])
                .collect(Collectors.toSet());

        Set<String> expected = Set.of(
                "0,1"
        );

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnKClosestPoints() {
        int[][] points = {
                {1, 3},
                {-2, 2},
                {5, 8},
                {0, 1}
        };

        int[][] result = kthClosestToOrigin.kClosest(points, 2);

        Set<String> actual = Arrays.stream(result)
                .map(point -> point[0] + "," + point[1])
                .collect(Collectors.toSet());

        Set<String> expected = Set.of(
                "0,1",
                "-2,2"
        );

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnTwoDuplicateClosestPoints() {
        int[][] points = {
                {1, 3},
                {-2, 2},
                {-2, 2},
                {5, 8},
                {0, 1},
                {0, 1}
        };

        int[][] result = kthClosestToOrigin.kClosest(points, 2);

        List<String> actual = Arrays.stream(result)
                .map(point -> point[0] + "," + point[1])
                .sorted()
                .toList();

        List<String> expected = List.of(
                "0,1",
                "0,1"
        );

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnKClosestPointsWhenKEqualsInput() {
        int[][] points = {
                {1, 3},
                {-2, 2},
                {5, 8},
                {0, 1}
        };

        int[][] result = kthClosestToOrigin.kClosest(points, 4);

        Set<String> actual = Arrays.stream(result)
                .map(point -> point[0] + "," + point[1])
                .collect(Collectors.toSet());

        Set<String> expected = Set.of(
                "0,1",
                "-2,2",
                "5,8",
                "1,3"
        );

        assertEquals(expected, actual);
    }
}