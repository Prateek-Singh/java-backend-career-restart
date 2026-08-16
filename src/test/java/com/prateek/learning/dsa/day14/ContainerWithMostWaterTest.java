package com.prateek.learning.dsa.day14;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ContainerWithMostWaterTest {

    private final ContainerWithMostWater containerWithMostWater = new ContainerWithMostWater();

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(new int[] {1,8,6,2,5,4,8,3,7}, 49),
                Arguments.of(new int[] {1,1}, 1),
                Arguments.of(new int[] {4,3,2,1,4}, 16),
                Arguments.of(new int[0], 0),
                Arguments.of(null, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void shouldReturnMaxArea(int[] heights, int expected) {
        assertThat(
                containerWithMostWater.maxArea(heights)
        ).isEqualTo(expected);
    }

}