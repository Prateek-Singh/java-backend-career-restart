package com.prateek.learning.day04.dsa;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class TopKFrequentElements {

    public int[] topKFrequent(int[] nums, int k) {
        if (nums == null || nums.length == 0 || k <= 0) {
            return new int[0];
        }

        Map<Integer, Integer> frequencyMap = new HashMap<>();

        for (int number : nums) {
            frequencyMap.merge(number, 1, Integer::sum);
        }

        PriorityQueue<Map.Entry<Integer, Integer>> minHeap =
                new PriorityQueue<>(
                        Map.Entry.comparingByValue()
                );

        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            minHeap.offer(entry);

            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        int resultSize = Math.min(k, frequencyMap.size());
        int[] result = new int[resultSize];

        for (int index = resultSize - 1; index >= 0; index--) {
            result[index] = minHeap.poll().getKey();
        }

        return result;
    }
}

//    Sorts entire entries from Map
//    public int[] topKFrequent(int[] nums, int k) {
//        if (nums == null || nums.length == 0) {
//            return new int[0];
//        }
//        if (k <= 0) {
//            return new int[0];
//        }
//
//        return Arrays
//                .stream(nums)
//                .boxed()
//                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
//                .entrySet()
//                .stream()
//                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
//                .mapToInt(Map.Entry::getKey)
//                .limit(k)
//                .toArray();
//    }



