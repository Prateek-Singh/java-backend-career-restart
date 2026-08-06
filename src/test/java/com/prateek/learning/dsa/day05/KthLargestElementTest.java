package com.prateek.learning.dsa.day05;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KthLargestElementTest {

    private final KthLargestElement kthLargestElement = new KthLargestElement();

    @Test
    void shouldThrowIllegalArgumentWhenInputIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kthLargestElement.findKthLargestElement(null, 1);
        });

        assertEquals("nums cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenInputIsEmpty() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kthLargestElement.findKthLargestElement(new int[0], 1);
        });

        assertEquals("nums cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenKIsZero() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kthLargestElement.findKthLargestElement(new int[] {2,3,5}, 0);
        });

        assertEquals("k must be between 1 and nums length", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenKIsGreaterThanInput() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            kthLargestElement.findKthLargestElement(new int[] {2,3,5}, 6);
        });

        assertEquals("k must be between 1 and nums length", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenKIsNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kthLargestElement.findKthLargestElement(
                        new int[]{2, 3, 5},
                        -1
                )
        );

        assertEquals(
                "k must be between 1 and nums length",
                exception.getMessage()
        );
    }

    @Test
    void shouldReturnKthLargestElementWhenKIsOne() {
        assertEquals(5, kthLargestElement.findKthLargestElement(new int[] {2,3,5}, 1));
    }

    @Test
    void shouldReturnKthLargestElementWhenKEqualsInput() {
        assertEquals(2, kthLargestElement.findKthLargestElement(new int[] {2,3,5}, 3));
    }

    @Test
    void shouldReturnKthLargestElementWhenInputIsDuplicated() {
        assertEquals(3, kthLargestElement.findKthLargestElement(new int[] {2,3,5,5}, 3));
    }

    @Test
    void shouldReturnKthLargestElementWhenInputIsNotDuplicated() {
        assertEquals(5, kthLargestElement.findKthLargestElement(new int[] {2,3,5,6}, 2));
    }

    @Test
    void shouldReturnKthLargestWhenValuesAreNegative() {
        assertEquals(
                -2,
                kthLargestElement.findKthLargestElement(
                        new int[]{-5, -2, -8, -1},
                        2
                )
        );
    }
}