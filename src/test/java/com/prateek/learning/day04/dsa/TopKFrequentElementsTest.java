package com.prateek.learning.day04.dsa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TopKFrequentElementsTest {

    private TopKFrequentElements topKFrequentElements;

    @BeforeEach
    void setUp() {
        topKFrequentElements = new TopKFrequentElements();
    }

    @Test
    void shouldReturnEmptyArrayWhenInputArrayIsNull() {
        assertArrayEquals(new int[0], topKFrequentElements.topKFrequent(null, 2));
    }

    @Test
    void shouldReturnEmptyArrayWhenInputArrayIsEmpty() {
        assertArrayEquals(new int[0], topKFrequentElements.topKFrequent(new int[0], 2));
    }

    @Test
    void shouldReturnEmptyArrayWhenKIsZero() {
        assertArrayEquals(
                new int[0],
                topKFrequentElements.topKFrequent(
                        new int[]{1, 1, 2},
                        0
                )
        );
    }

    @Test
    void shouldReturnEmptyArrayWhenKIsNegative() {
        assertArrayEquals(
                new int[0],
                topKFrequentElements.topKFrequent(
                        new int[]{1, 1, 2},
                        -1
                )
        );
    }

    @Test
    void shouldReturnTopKFrequentNumbers() {
        int[] result = topKFrequentElements.topKFrequent(
                new int[]{1, 1, 1, 2, 2, 3},
                2
        );

        assertArrayEqualsIgnoringOrder(
                new int[]{1, 2},
                result
        );
    }

    @Test
    void shouldReturnAllDistinctValuesWhenKExceedsDistinctCount() {
        assertArrayEqualsIgnoringOrder(new int[] {1,2,3}, topKFrequentElements.topKFrequent(new int[] {1,1,1,2,2,3}, 8));
    }

    @Test
    void shouldReturnExpectedWhenInputIsNegative() {
        assertArrayEqualsIgnoringOrder(new int[] {-1,-2}, topKFrequentElements.topKFrequent(new int[] {-1,-1,-1,-2,-2,-3}, 2));
    }

    @Test
    void shouldReturnAnyTwoNumbersWhenFrequenciesAreTied() {
        int[] result = topKFrequentElements.topKFrequent(
                new int[]{1, 1, 2, 2, 3, 3, 4},
                2
        );

        assertEquals(2, result.length);

        for (int number : result) {
            assertTrue(
                    number == 1 || number == 2 || number == 3
            );
        }

        assertNotEquals(result[0], result[1]);
    }

    @Test
    void shouldReturnSingleValueWhenOnlyOneDistinctNumberExists() {
        int[] result = topKFrequentElements.topKFrequent(
                new int[]{5, 5, 5, 5},
                1
        );

        assertArrayEqualsIgnoringOrder(new int[]{5}, result);
    }

    private void assertArrayEqualsIgnoringOrder(
            int[] expected,
            int[] actual
    ) {
        Arrays.sort(expected);
        Arrays.sort(actual);
        assertArrayEquals(expected, actual);
    }
}