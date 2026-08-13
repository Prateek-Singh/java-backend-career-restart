package com.prateek.learning.dsa.day12;

public class MaxConsecutiveOnes {

    public int findMaxConsecutiveOnes(int[] nums, int k) {
        if (nums == null || nums.length == 0) {
            return 0;
        }

        int left = 0;
        int zeroCounter = 0;
        int maxLength = 0;

        for (int right = 0; right < nums.length; right++) {
            if (nums[right] == 0) {
                zeroCounter++;
            }
            while (zeroCounter > k) {
                if (nums[left] == 0) {
                    zeroCounter--;
                }
                left++;
            }
            maxLength = Math.max(maxLength, right - left + 1);
        }
        return maxLength;
    }
}
