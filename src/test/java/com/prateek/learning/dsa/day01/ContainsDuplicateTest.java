package com.prateek.learning.dsa.day01;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContainsDuplicateTest {

    private ContainsDuplicate containsDuplicate;

    @BeforeEach
    void setUp() {
        containsDuplicate = new ContainsDuplicate();
    }

    @Test
    void bruteForceShouldReturnMatch() {
        assertTrue(containsDuplicate.containsDuplicateBruteForce(new int[]{1, 2, 3, 1}));
    }

    @Test
    void bruteForceShouldNotReturnMatch() {
        assertFalse(containsDuplicate.containsDuplicateBruteForce(new int[]{1, 2, 3, 4}));
    }

    @Test
    void bruteForceShouldReturnMatchWithJustTwoElements() {
        assertTrue(containsDuplicate.containsDuplicateBruteForce(new int[]{1, 1}));
    }

    @Test
    void bruteForceShouldReturnFalseForEmptyArray() {
        assertFalse(containsDuplicate.containsDuplicateBruteForce(new int[]{}));
    }

    @Test
    void bruteForceShouldReturnFalseForNullArray() {
        assertFalse(containsDuplicate.containsDuplicateBruteForce(null));
    }

    @Test
    void optimizedShouldReturnMatch() {
        assertTrue(containsDuplicate.containsDuplicateOptimized(new int[]{1, 2, 3, 1}));
    }

    @Test
    void optimizedShouldNotReturnMatch() {
        assertFalse(containsDuplicate.containsDuplicateOptimized(new int[]{1, 2, 3, 4}));
    }

    @Test
    void optimizedShouldReturnMatchWithJustTwoElements() {
        assertTrue(containsDuplicate.containsDuplicateOptimized(new int[]{1, 1}));
    }

    @Test
    void optimizedShouldReturnFalseForEmptyArray() {
        assertFalse(containsDuplicate.containsDuplicateOptimized(new int[]{}));
    }

    @Test
    void optimizedShouldReturnFalseForNullArray() {
        assertFalse(containsDuplicate.containsDuplicateOptimized(null));
    }

}
