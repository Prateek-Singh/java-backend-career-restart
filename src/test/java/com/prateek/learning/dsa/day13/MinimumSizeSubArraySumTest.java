package com.prateek.learning.dsa.day13;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class MinimumSizeSubArraySumTest {

    private final MinimumSizeSubArraySum minimumSizeSubArraySum = new MinimumSizeSubArraySum();

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(new int[] {2,3,1,2,4,3}, 7, 2),
                Arguments.of(new int[] {1,4,4}, 4, 1),
                Arguments.of(new int[] {1,1,1,1,1}, 11, 0),
                Arguments.of(new int[] {1,2,3,4,5}, 15, 5),
                Arguments.of(new int[0], 3, 0),
                Arguments.of(null, 3, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void shouldReturnMinimumSizeSubArraySum(int[] input, int target, int expected) {
        assertThat(
                minimumSizeSubArraySum.minSubArrayLen(target, input)
        ).isEqualTo(expected);
    }
}