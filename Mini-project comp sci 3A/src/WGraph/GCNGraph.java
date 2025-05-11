package WGraph;

import java.io.Serializable;
import java.util.List;

/**
 * A GCNGraph that leverages the generic Graph<T> implementation internally.
 */
public class GCNGraph implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Node data type holding features and label
    public static class NodeData implements Comparable<NodeData>, Serializable {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private double[] features;
        private double[] label;  // one-hot

        public NodeData(double[] features, double[] label) {
            if (label == null) throw new IllegalArgumentException("Label cannot be null");
            this.features = features;
            this.label = label;
        }

        public double[] getFeatures() { return features; }
        public double[] getLabel()    { return label; }
        public void setLabel(double[] newLabel) {
            if (newLabel.length != label.length)
                throw new IllegalArgumentException("Label length mismatch");
            this.label = newLabel;
        }

        @Override
        public int compareTo(NodeData o) {
            // Compare by label length then feature length
            int cmp = Integer.compare(this.label.length, o.label.length);
            if (cmp != 0) return cmp;
            return Integer.compare(this.features.length, o.features.length);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof NodeData)) return false;
            NodeData other = (NodeData) obj;
            return java.util.Arrays.equals(features, other.features)
                && java.util.Arrays.equals(label, other.label);
        }

        @Override
        public int hashCode() {
            int result = java.util.Arrays.hashCode(features);
            result = 31 * result + java.util.Arrays.hashCode(label);
            return result;
        }

        @Override
        public String toString() {
            return "NodeData[label=" + java.util.Arrays.toString(label) + "]";
        }
    }

    private Graph<NodeData> graph;
    private final int numClasses;

    public GCNGraph(int numClasses) {
        this.graph = new Graph<>(Graph.getUndirected());
        this.numClasses = numClasses;
    }

    /**
     * Add a node with features and one-hot label.
     */
    public void addNode(double[] features, double[] label) {
        if (label.length != numClasses)
            throw new IllegalArgumentException(
                "Expected label length=" + numClasses + ", got " + label.length
            );
        NodeData data = new NodeData(features, label);
        graph.getNodes().add(new Graph.GraphNode<>(data));
    }

    /**
     * Add undirected edge between node indices i and j.
     */
    public void addEdge(int i, int j, int weight) {
        List<Graph.GraphNode<NodeData>> nodes = graph.getNodes();
        if (i < 0 || j < 0 || i >= nodes.size() || j >= nodes.size())
            throw new IllegalArgumentException("Invalid edge indices");
        Graph.GraphNode<NodeData> ni = nodes.get(i);
        Graph.GraphNode<NodeData> nj = nodes.get(j);
        Graph.GraphLink<NodeData> link = new Graph.GraphLink<>(weight, ni, nj);
        ni.addLink(link);
        if (graph.getGraphType() == Graph.getUndirected()) {
            Graph.GraphLink<NodeData> reciprocal = new Graph.GraphLink<>(weight, nj, ni);
            nj.addLink(reciprocal);
            graph.getLinks().add(reciprocal);
        }
        graph.getLinks().add(link);
    }

    public int getNodeCount() {
        return graph.getNodes().size();
    }

    /**
     * Returns feature matrix F of shape [n x d].
     */
    public double[][] getFeatureMatrix() {
        int n = getNodeCount();
        if (n == 0) return new double[0][0];
        int d = graph.getNodes().get(0).getData().getFeatures().length;
        double[][] F = new double[n][d];
        for (int i = 0; i < n; i++) {
            F[i] = graph.getNodes().get(i).getData().getFeatures();
        }
        return F;
    }

    /**
     * Returns label matrix L of shape [n x numClasses].
     */
    public double[][] getLabelMatrix() {
        int n = getNodeCount();
        double[][] L = new double[n][numClasses];
        for (int i = 0; i < n; i++) {
            L[i] = graph.getNodes().get(i).getData().getLabel();
        }
        return L;
    }

    /**
     * Set the one-hot label for a specific node.
     */
    public void setNodeLabel(int nodeIndex, double[] newLabel) {
        List<Graph.GraphNode<NodeData>> nodes = graph.getNodes();
        if (nodeIndex < 0 || nodeIndex >= nodes.size()) {
            throw new IllegalArgumentException("Invalid node index");
        }
        if (newLabel.length != numClasses) {
            throw new IllegalArgumentException("Expected label length=" + numClasses);
        }
        nodes.get(nodeIndex).getData().setLabel(newLabel);
    }

    /**
     * Returns a graph-level label (e.g., the label of the first node).
     */
    public double[][] getGraphLevelLabel() {
        if (getNodeCount() == 0) {
            throw new IllegalStateException("Graph has no nodes");
        }
        return new double[][] { graph.getNodes().get(0).getData().getLabel() };
    }

    /**
     * Constructs normalized adjacency matrix with self-loops.
     */
    public double[][] getNormalizedAdjMatrix() {
        int n = getNodeCount();
        double[][] A = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (Graph.GraphLink<NodeData> link : graph.getNodes().get(i).getLinks()) {
                int j = graph.getNodes().indexOf(link.getToNode());
                A[i][j] = 1.0;
            }
            A[i][i] = 1.0;
        }
        return GraphUtils.normalizeAdjacency(A);
    }

    /**
     * Prints each node's class index and its neighbors.
     */
    public void printGraph() {
        List<Graph.GraphNode<NodeData>> nodes = graph.getNodes();
        for (int i = 0; i < nodes.size(); i++) {
            double[] lab = nodes.get(i).getData().getLabel();
            int cls = -1;
            for (int k = 0; k < lab.length; k++) {
                if (lab[k] == 1.0) { cls = k; break; }
            }
            System.out.println("Node " + i + " → class " + cls + " neighbors=" +
                nodes.get(i).getLinks());
        }
    }
}
