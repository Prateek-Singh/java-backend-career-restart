package com.prateek.learning.day03.dsa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupAnagramTest {

    private GroupAnagram groupAnagram;

    @BeforeEach
    void setUp() {
        groupAnagram = new GroupAnagram();
    }

    @Test
    void shouldReturnEmptyListWhenInputWordsAreNull() {
        assertTrue(groupAnagram.groupAnagrams(null).isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenInputWordsAreEmpty() {
        assertTrue(groupAnagram.groupAnagrams(new String[]{}).isEmpty());
    }

    @Test
    void shouldReturnSingleGroupForSingleWord() {
        List<List<String>> groupAnagrams = groupAnagram.groupAnagrams(new String[]{"a"});
        assertEquals(1, groupAnagrams.size());
        assertEquals(List.of("a"), groupAnagrams.get(0));
    }

    @Test
    void shouldGroupMultipleAnagramGroups() {
        List<List<String>> result = groupAnagram.groupAnagrams(
                new String[]{"eat", "tea", "tan", "ate", "nat", "bat"}
        );

        assertEquals(3, result.size());
        assertTrue(result.contains(List.of("eat", "tea", "ate")));
        assertTrue(result.contains(List.of("tan", "nat")));
        assertTrue(result.contains(List.of("bat")));
    }

    @Test
    void shouldKeepDuplicateWordsInTheSameGroup() {
        List<List<String>> result = groupAnagram.groupAnagrams(
                new String[]{
                        "eat", "tea", "tan", "ate",
                        "nat", "tan", "bat"
                }
        );

        assertEquals(3, result.size());
        assertTrue(result.contains(List.of("eat", "tea", "ate")));
        assertTrue(result.contains(List.of("tan", "nat", "tan")));
        assertTrue(result.contains(List.of("bat")));
    }

    @Test
    void shouldGroupEmptyStringsTogether() {
        List<List<String>> result =
                groupAnagram.groupAnagrams(new String[]{"", "a", ""});

        assertEquals(2, result.size());
        assertTrue(result.contains(List.of("", "")));
        assertTrue(result.contains(List.of("a")));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenInputWordIsNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> groupAnagram.groupAnagrams(new String[]{"", null}));
        assertEquals("word cannot be null", exception.getMessage());
    }
}