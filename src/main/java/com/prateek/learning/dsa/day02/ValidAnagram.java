package com.prateek.learning.dsa.day02;

import java.util.Arrays;

public class ValidAnagram {

    public boolean isAnagramUsingSorting(String s, String t) {
        if (s == null || t == null || s.length() != t.length()) {
            return false;
        }
        char[] sArr = s.toCharArray();
        char[] tArr = t.toCharArray();

        Arrays.sort(sArr);
        Arrays.sort(tArr);

        return Arrays.equals(sArr, tArr);
    }

    public boolean isAnagram(String s, String t) {
        if (s == null || t == null || s.length() != t.length()) {
            return false;
        }

        int[] frequencies = new int[26];
        for (int i = 0; i < s.length(); i++) {
            frequencies[s.charAt(i) - 'a']++;
            frequencies[t.charAt(i) - 'a']--;
        }

        for (int frequency : frequencies) {
            if (frequency != 0) {
                return false;
            }
        }
        return true;
    }
}
