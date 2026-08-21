package com.prateek.learning.dsa.day15;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class ThreeSumTest {

    private final ThreeSum threeSum = new ThreeSum();

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of(new int[] {-1,0,1,2,-1,-4}, List.of(List.of(-1,-1,2), List.of(-1,0,1))),
                Arguments.of(new int[] {0,0,0}, List.of(List.of(0,0,0))),
                Arguments.of(new int[] {0,0,0,0}, List.of(List.of(0,0,0))),
                Arguments.of(new int[] {1,2,-2,-1}, List.of()),
                Arguments.of(new int[0], List.of())
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void testThreeSum(int[] nums, List<List<Integer>> res) {
        assertThat(
                threeSum.threeSum(nums)
        ).containsExactlyElementsOf(res);
    }

}