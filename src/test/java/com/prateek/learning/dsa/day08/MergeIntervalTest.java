package com.prateek.learning.dsa.day08;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MergeIntervalTest {

    private final MergeInterval mergeInterval = new MergeInterval();

    @ParameterizedTest
    @MethodSource("mergeIntervals")
    void shouldMergeIntervals(int[][] input, int[][] expected) {
        int[][] result = mergeInterval.merge(input);

        assertThat(result).isDeepEqualTo(expected);
    }

    static Stream<Arguments> mergeIntervals() {
        return Stream.of(
                Arguments.of(
                        new int[][]{{1, 3}, {2, 6}, {8, 10}, {15, 18}},
                        new int[][]{{1, 6}, {8, 10}, {15, 18}}
                ),
                Arguments.of(
                        new int[][]{{1, 4}, {4, 5}},
                        new int[][]{{1, 5}}
                ),
                Arguments.of(
                        new int[][]{{1, 4}},
                        new int[][]{{1, 4}}
                ),
                Arguments.of(
                        new int[][]{{1, 10}, {2, 3}, {4, 8}},
                        new int[][]{{1, 10}}
                ),
                Arguments.of(
                        new int[][]{{5, 7}, {1, 2}, {2, 4}},
                        new int[][]{{1, 4}, {5, 7}}
                )
        );
    }

    @Test
    void shouldRejectNullIntervals() {
        assertThatThrownBy(() -> mergeInterval.merge(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmptyIntervals() {
        assertThatThrownBy(() -> mergeInterval.merge(new int[][]{}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}