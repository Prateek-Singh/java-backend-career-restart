package com.prateek.learning.dsa.day12;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MaxConsecutiveOnesTest {

    private final MaxConsecutiveOnes maxConsecutiveOnes = new MaxConsecutiveOnes();

    @ParameterizedTest
    @MethodSource("testCases")
    void shouldReturnExpectedMaxConsecutiveOnes(int[] nums, int k, int expected) {
        assertThat(
                maxConsecutiveOnes.findMaxConsecutiveOnes(nums, k)
        ).isEqualTo(expected);
    }

    private static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(new int[] {1,1,1,0,0,0,1,1,1,1,0}, 2, 6),
                Arguments.of(new int[] {1,1,0,0,1,1,1}, 1, 4),
                Arguments.of(new int[] {1,1,1,1}, 0, 4),
                Arguments.of(new int[] {0,0,0}, 1, 1),
                Arguments.of(new int[] {0,0,0}, 3, 3),
                Arguments.of(new int[0], 2, 0),
                Arguments.of(null, 2, 0)
        );
    }

}