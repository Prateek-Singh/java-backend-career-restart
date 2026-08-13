package com.prateek.learning.dsa.day11;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class LongestRepeatingCharacterReplacementTest {

    private final LongestRepeatingCharacterReplacement longestRepeatingCharacterReplacement =  new LongestRepeatingCharacterReplacement();

    public static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of("ABAB", 2, 4),
                Arguments.of("AABABBA", 1, 4),
                Arguments.of("AAAA", 0, 4),
                Arguments.of("AABBB", 0, 3),
                Arguments.of("ABCD", 1, 2),
                Arguments.of("", 2, 0),
                Arguments.of(null, 2, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    void shouldReturnLongestRepeatingCharacterReplacement(String input, int k, int expected) {
        assertThat(
                longestRepeatingCharacterReplacement.longestRepeatingCharacterReplacement(input, k))
                .isEqualTo(expected);
    }

}