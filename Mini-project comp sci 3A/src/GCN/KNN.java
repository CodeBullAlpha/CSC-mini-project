package GCN;

import DataStructures.PriorityQueueHeap;
import DataStructures.Entry;
import WGraph.GCNGraph;

public class KNN {

    /**
     * Connect each node to its 16 nearest neighbors based on combined spatial+color distance.
     */
    public static void makeCombined16NNConnections(GCNGraph graph, double[][] features) {
        int n = features.length;

        for (int i = 0; i < n; i++) {
            // Custom max-heap: higher key = lower priority (i.e., further neighbors)
            PriorityQueueHeap<Integer, Integer> nearest = new PriorityQueueHeap<>((a, b) -> Integer.compare(b, a));

            for (int j = 0; j < n; j++) {
                if (i == j) continue;

                // Spatial distance (dims 6 and 7)
                double spatial = Math.hypot(features[i][6] - features[j][6], features[i][7] - features[j][7]);

                // Color distance (dims 0 to 5)
                double colorDist = 0;
                for (int d = 0; d < 6; d++) {
                    double diff = features[i][d] - features[j][d];
                    colorDist += diff * diff;
                }
                colorDist = Math.sqrt(colorDist);

                // Combine distances and scale
                double combined = spatial + colorDist;
                int scaled = (int) (combined * 1e6);

                if (nearest.size() < 16) {
                    nearest.insert(scaled, j);
                } else {
                    Entry<Integer, Integer> max = nearest.min(); // Since we use reversed comparator, this is the max
                    if (scaled < max.getKey()) {
                        nearest.removeMin(); // Remove the farthest neighbor
                        nearest.insert(scaled, j); // Add new closer neighbor
                    }
                }
            }

            // Add unweighted edges to the graph
            for (Entry<Integer, Integer> entry : nearest.getHeap()) {
                graph.addEdge(i, entry.getValue(), 1);
            }
        }
    }
}
