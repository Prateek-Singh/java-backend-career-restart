package com.prateek.learning.day01.dsa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class TwoSumTest {

    private TwoSum twoSum;

    @BeforeEach
    void setUp () {
        twoSum = new TwoSum();
    }

    @Test
    void bruteForceShouldFindPairAtBeginning() {
        assertArrayEquals(
                new int[]{0, 1},
                twoSum.twoSumBruteForce(new int[]{2, 7, 11, 15}, 9)
        );
    }

    @Test
    void bruteForceShouldFindPairInMiddle() {
        assertArrayEquals(
                new int[]{1, 2},
                twoSum.twoSumBruteForce(new int[]{3, 2, 4}, 6)
        );
    }

    @Test
    void bruteForceShouldFindPairWithJustTwoElements() {
        assertArrayEquals(
                new int[]{0, 1},
                twoSum.twoSumBruteForce(new int[]{3, 3}, 6)
        );
    }

    @Test
    void bruteForceShouldFindNoPair() {
        assertArrayEquals(
                new int[0],
                twoSum.twoSumBruteForce(new int[]{1, 2, 3}, 10)
        );
    }

    @Test
    void optimizedShouldFindPairAtBeginning() {
        assertArrayEquals(
                new int[]{0, 1},
                twoSum.twoSumOptimized(new int[]{2, 7, 11, 15}, 9)
        );
    }

    @Test
    void optimizedShouldFindPairInMiddle() {
        assertArrayEquals(
                new int[]{1, 2},
                twoSum.twoSumOptimized(new int[]{3, 2, 4}, 6)
        );
    }

    @Test
    void optimizedShouldFindPairWithJustTwoElements() {
        assertArrayEquals(
                new int[]{0, 1},
                twoSum.twoSumOptimized(new int[]{3, 3}, 6)
        );
    }

    @Test
    void optimizedShouldFindNoPair() {
        assertArrayEquals(
                new int[0],
                twoSum.twoSumOptimized(new int[]{1, 2, 3}, 10)
        );
    }

}
