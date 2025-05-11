package GCN;

import java.util.PriorityQueue;

import WGraph.GCNGraph;

public class KNN {

    /**
     * Connect each node to its 16 nearest neighbors based on combined spatial+color distance.
     */
    public static void makeCombined16NNConnections(GCNGraph graph, double[][] features) {
        int n = features.length;
        for (int i = 0; i < n; i++) {
            // (distanceScaled, index) pairs, max-heap by distance
            PriorityQueue<int[]> nearest = new PriorityQueue<>(16, (a, b) -> Integer.compare(b[0], a[0]));

            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                // spatial on dims 6,7
                double spatial = Math.hypot(features[i][6] - features[j][6], features[i][7] - features[j][7]);
                // color euclidean on first 6 dims
                double colorDist = 0;
                for (int d = 0; d < 6; d++) {
                    double diff = features[i][d] - features[j][d];
                    colorDist += diff * diff;
                }
                colorDist = Math.sqrt(colorDist);
                double combined = spatial + colorDist;

                int scaled = (int) (combined * 1e6);
                if (nearest.size() < 16) {
                    nearest.offer(new int[]{ scaled, j });
                } else if (scaled < nearest.peek()[0]) {
                    nearest.poll();
                    nearest.offer(new int[]{ scaled, j });
                }
            }

            // Add unweighted edges (weight=1) to the 16 nearest neighbors
            for (int[] entry : nearest) {
                graph.addEdge(i, entry[1], 1);
            }
        }
    }
}



