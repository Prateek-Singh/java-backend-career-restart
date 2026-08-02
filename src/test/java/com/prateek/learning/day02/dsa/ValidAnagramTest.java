package com.prateek.learning.day02.dsa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidAnagramTest {

    private ValidAnagram validAnagram;

    @BeforeEach
    void setup() {
        validAnagram = new ValidAnagram();
    }

    @Test
    void sortingShouldReturnTrueForValidAnagram() {
        assertTrue(validAnagram.isAnagramUsingSorting("anagram", "nagaram"));
    }

    @Test
    void sortingShouldReturnFalseForInvalidAnagram() {
        assertFalse(validAnagram.isAnagramUsingSorting("rat", "car"));
    }

    @Test
    void sortingShouldReturnTrueForEmptyStrings() {
        assertTrue(validAnagram.isAnagramUsingSorting("", ""));
    }

    @Test
    void sortingShouldReturnTrueForSingleCharStrings() {
        assertTrue(validAnagram.isAnagramUsingSorting("a", "a"));
    }

    @Test
    void sortingShouldReturnFalseForSingleCharStrings() {
        assertFalse(validAnagram.isAnagramUsingSorting("a", "b"));
    }

    @Test
    void sortingShouldReturnFalseForOneNullStrings() {
        assertFalse(validAnagram.isAnagramUsingSorting(null, "abc"));
    }

    @Test
    void sortingShouldReturnFalseForDifferentLengthStrings() {
        assertFalse(validAnagram.isAnagramUsingSorting("ab", "a"));
    }

    @Test
    void optimizedShouldReturnTrueForValidAnagram() {
        assertTrue(validAnagram.isAnagram("anagram", "nagaram"));
    }

    @Test
    void optimizedShouldReturnFalseForInvalidAnagram() {
        assertFalse(validAnagram.isAnagram("rat", "car"));
    }

    @Test
    void optimizedShouldReturnTrueForEmptyStrings() {
        assertTrue(validAnagram.isAnagram("", ""));
    }

    @Test
    void optimizedShouldReturnTrueForSingleCharStrings() {
        assertTrue(validAnagram.isAnagram("a", "a"));
    }

    @Test
    void optimizedShouldReturnFalseForSingleCharStrings() {
        assertFalse(validAnagram.isAnagram("a", "b"));
    }

    @Test
    void optimizedShouldReturnFalseForOneNullStrings() {
        assertFalse(validAnagram.isAnagram(null, "abc"));
    }

    @Test
    void optimizedShouldReturnFalseForDifferentLengthStrings() {
        assertFalse(validAnagram.isAnagram("ab", "a"));
    }
}
