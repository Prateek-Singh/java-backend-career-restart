package com.prateek.learning.dsa.day10;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FindLongestSubstringWith2DistinctCharactersTest {

    private final FindLongestSubstringWith2DistinctCharacters findLongestSubstring = new FindLongestSubstringWith2DistinctCharacters();

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("eceba", 3),
                Arguments.of("ccaabbb", 5),
                Arguments.of("aaaa", 4),
                Arguments.of("abc", 2),
                Arguments.of("", 0),
                Arguments.of(null, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    void shouldReturnLengthOfLongestSubstringWithAtMostTwoDistinctCharacters(String input, int expected) {

        assertThat(
                findLongestSubstring.lengthOfLongestSubstringWith2DistinctCharacters(input)
        ).isEqualTo(expected);
    }
}