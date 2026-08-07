package com.prateek.learning.dsa.day07;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class KLargestElements {

    public List<Integer> findKLargestElements(int[] nums, int k) {
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

        List<Integer> result = new ArrayList<>();

        while (!minHeap.isEmpty()) {
            result.add(minHeap.poll());
        }

        return result;
    }
}