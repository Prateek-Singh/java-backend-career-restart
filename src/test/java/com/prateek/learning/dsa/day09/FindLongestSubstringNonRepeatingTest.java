package com.prateek.learning.dsa.day09;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FindLongestSubstringNonRepeatingTest {

    private final FindLongestSubstringNonRepeating findLongestSubStringNonRepeating =  new FindLongestSubstringNonRepeating();

    @ParameterizedTest
    @MethodSource("testCases")
    void findLongestSubstringNonRepeating(String input, String expected) {
        String result = findLongestSubStringNonRepeating.findLongestSubStringNonRepeating(input);
        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> testCases() {
        return Stream.of(
                Arguments.of("abba", "ab"),
                Arguments.of("abcabcbb", "abc"),
                Arguments.of("bbbbb", "b"),
                Arguments.of("pwwkew", "wke"),
                Arguments.of("", ""),
                Arguments.of(null, "")
        );
    }

}