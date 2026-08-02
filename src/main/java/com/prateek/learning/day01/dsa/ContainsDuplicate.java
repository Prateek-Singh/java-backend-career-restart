package com.prateek.learning.day01.dsa;

import java.util.HashSet;
import java.util.Set;

public class ContainsDuplicate {

    public boolean containsDuplicateBruteForce(int[] nums) {
        if (nums == null || nums.length < 2) {
            return false;
        }

        for (int i = 0; i < nums.length - 1; i++) {
            for (int j = i + 1; j < nums.length; j++) {
                if (nums[i] == nums[j]) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsDuplicateOptimized(int[] nums) {
        if (nums == null || nums.length < 2) {
            return false;
        }
        Set<Integer> seen = new HashSet<>();

        for (int num : nums) {
            if(!seen.add(num)) {
                return true;
            }
        }
        return false;
    }
}
