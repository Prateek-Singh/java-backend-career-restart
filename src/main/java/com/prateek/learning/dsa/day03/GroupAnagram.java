package com.prateek.learning.dsa.day03;

import java.util.*;

public class GroupAnagram {

    public List<List<String>> groupAnagrams(String[] words) {
        if (words == null || words.length == 0) {
            return Collections.emptyList();
        }

        Map<String, List<String>> groups = new HashMap<>();

        for (String word : words) {
            if (word == null) {
                throw new IllegalArgumentException("word cannot be null");
            }

            char[] characters = word.toCharArray();
            Arrays.sort(characters);
            String key = new String(characters);

            groups.computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(word);
        }

        return new ArrayList<>(groups.values());
    }
}
