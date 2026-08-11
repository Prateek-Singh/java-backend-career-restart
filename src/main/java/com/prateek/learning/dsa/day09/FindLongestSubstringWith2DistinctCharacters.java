package com.prateek.learning.dsa.day09;

import java.util.HashMap;
import java.util.Map;

public class FindLongestSubstringWith2DistinctCharacters {

    public int lengthOfLongestSubstringWith2DistinctCharacters(String input) {
        if (input == null || input.length() == 0) {
            return 0;
        }

        int left = 0;
        int maxLength = 0;

        Map<Character, Integer> map = new HashMap<>();

        for (int right = 0; right < input.length(); right++) {
            char current = input.charAt(right);

            map.put(current, map.getOrDefault(current, 0) + 1);

            while(map.size() > 2) {
                char leftChar = input.charAt(left);
                map.put(leftChar, map.get(leftChar) - 1);
                if(map.get(leftChar) == 0) {
                    map.remove(leftChar);
                }
                left++;
            }

            maxLength = Math.max(maxLength, right - left + 1);
        }
        return maxLength;
    }
}
