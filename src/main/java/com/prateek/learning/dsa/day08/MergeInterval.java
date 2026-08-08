package com.prateek.learning.dsa.day08;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MergeInterval {

    public int[][] merge(int[][] intervals) {
        // validate
        if (intervals == null || intervals.length == 0) {
            throw new IllegalArgumentException("intervals is null or empty");
        }

        int[][] copy = Arrays.stream(intervals)
                .map(int[]::clone)
                .toArray(int[][]::new);

        Arrays.sort(copy, (a, b) -> Integer.compare(a[0], b[0]));

        List<int[]> result = new ArrayList<>();
        //[1,3],[2,6],[8,10]
        int[] currentInterval = copy[0];
        for (int i = 1; i < copy.length; i++) {
            int[] nextInterval = copy[i];
            if (nextInterval[0] <= currentInterval[1]) {
                currentInterval[1] = Math.max(currentInterval[1], nextInterval[1]);
            } else {
                result.add(currentInterval);
                currentInterval = nextInterval;
            }
        }
        result.add(currentInterval);
        return result.toArray(new int[result.size()][]);
    }
}
