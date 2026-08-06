package com.prateek.learning.dsa.day06;

import java.util.PriorityQueue;

public class KthClosestToOrigin {

    public int[][] kClosest(int[][] points, int k) {
        if (points == null || points.length == 0) {
            throw new IllegalArgumentException("points must not be null or empty");
        }

        if (k <= 0 || k > points.length) {
            throw new IllegalArgumentException("k must be between 1 and " + points.length);
        }

        PriorityQueue<int[]> pq = new PriorityQueue<>(
                (points1, points2) -> Long.compare(squared(points2), squared(points1)));
        for (int[] point : points) {
            pq.offer(point);
            if (pq.size() > k) {
                pq.poll();
            }
        }

        return pq.toArray(new int[k][]);
    }

    private long squared(int[] point) {
        return (long)point[0] * point[0] + (long)point[1] * point[1];
    }
}
