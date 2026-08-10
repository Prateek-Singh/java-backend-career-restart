package com.prateek.learning.dsa.day09;

import java.util.HashSet;
import java.util.Set;

public class FindLongestSubstringNonRepeating {

    public String findLongestSubStringNonRepeating(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        int left = 0;
        int bestStart = 0;
        int maxLength = 0;

        Set<Character> set = new HashSet<>();

        for (int right = 0; right < input.length(); right++) {
            char current = input.charAt(right);

            while (set.contains(current)) {
                set.remove(input.charAt(left));
                left++;
            }

            set.add(current);

            int currentLength = right - left + 1;

            if (currentLength > maxLength) {
                maxLength = currentLength;
                bestStart = left;
            }
        }

        return input.substring(bestStart, bestStart + maxLength);
    }
}