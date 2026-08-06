package com.prateek.learning.dsa.day05;

import java.util.PriorityQueue;

public class KthLargestElement {

    public int findKthLargestElement(int[] nums, int k) {
        if (nums == null || nums.length == 0) {
            throw new IllegalArgumentException(
                    "nums cannot be null or empty"
            );
        }

        if (k <= 0 || k > nums.length) {
            throw new IllegalArgumentException(
                    "k must be between 1 and nums length"
            );
        }

        PriorityQueue<Integer> minHeap = new PriorityQueue<>();

        for (int num : nums) {
            minHeap.offer(num);

            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        return minHeap.peek();
    }
}
