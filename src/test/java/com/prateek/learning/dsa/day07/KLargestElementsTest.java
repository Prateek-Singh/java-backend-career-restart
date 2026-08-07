package com.prateek.learning.dsa.day07;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KLargestElementsTest {

    private final KLargestElements kLargestElements = new KLargestElements();

    @Test
    void shouldThrowIllegalArgumentWhenInputIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kLargestElements.findKLargestElements(null, 1)
        );

        assertEquals("nums cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenInputIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kLargestElements.findKLargestElements(new int[0], 1)
        );

        assertEquals("nums cannot be null or empty", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenKIsZero() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kLargestElements.findKLargestElements(new int[] {2,3,5}, 0)
        );

        assertEquals("k must be between 1 and nums length", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenKIsGreaterThanInput() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kLargestElements.findKLargestElements(new int[] {2,3,5}, 6)
        );

        assertEquals("k must be between 1 and nums length", exception.getMessage());
    }

    @Test
    void shouldThrowIllegalArgumentWhenKIsNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> kLargestElements.findKLargestElements(
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
    void shouldReturnLargestElementWhenKIsOne() {
        assertThat(kLargestElements.findKLargestElements(new int[] {2,3,5}, 1))
                .containsExactlyInAnyOrder(5);
    }

    @Test
    void shouldReturnAllElementsWhenKEqualsInputLength() {
        assertThat(kLargestElements.findKLargestElements(new int[] {2,3,5}, 3))
                .containsExactlyInAnyOrder(2,3,5);
    }

    @Test
    void shouldReturnKLargestElementsWhenInputContainsDuplicates() {
        assertThat(kLargestElements.findKLargestElements(new int[] {2,3,3,5}, 3))
                .containsExactlyInAnyOrder(3,3,5);
    }

    @Test
    void shouldReturnKLargestElementsForDistinctValues() {
        assertThat(kLargestElements.findKLargestElements(new int[] {2,3,5}, 2))
                .containsExactlyInAnyOrder(3,5);
    }

    @Test
    void shouldReturnKLargestElementsWhenInputContainsNegativeValues() {
        assertThat(
                kLargestElements.findKLargestElements(
                        new int[]{-10, -3, -7, -1},
                        2
                )
        ).containsExactlyInAnyOrder(-3, -1);
    }

}