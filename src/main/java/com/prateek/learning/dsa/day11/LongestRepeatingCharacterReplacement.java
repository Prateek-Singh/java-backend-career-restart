package com.prateek.learning.dsa.day11;

import java.util.HashMap;
import java.util.Map;

public class LongestRepeatingCharacterReplacement {

    public int longestRepeatingCharacterReplacement(String input, int k) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        int left = 0;
        int maxFreq = 0;
        int maxLength = 0;

        Map<Character, Integer> map = new HashMap<>();

        for (int right = 0; right < input.length(); right++) {
            char ch = input.charAt(right);

            map.put(ch, map.getOrDefault(ch, 0) + 1);
            maxFreq = Math.max(maxFreq, map.get(ch));

            while ((right - left + 1) - maxFreq > k) {
                char leftChar = input.charAt(left);

                map.put(leftChar, map.get(leftChar) - 1);

                if (map.get(leftChar) == 0) {
                    map.remove(leftChar);
                }

                left++;
            }

            maxLength = Math.max(maxLength, right - left + 1);
        }

        return maxLength;
    }
}
