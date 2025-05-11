import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * A generic graph implementation that can be either directed or undirected.
 * 
 * @param <T> The type of data stored in the graph nodes, must implement Comparable
 */
@SuppressWarnings("unchecked")
public class Graph<T extends Comparable<T>> {

    private List<GraphNode<T>> nodes = new ArrayList<GraphNode<T>>();
    private List<GraphLink<T>> links = new ArrayList<GraphLink<T>>();

    // Graph type constants
    private static final char DIRECTED = 'D';
    private static final char UNDIRECTED = 'U';

    /** The type of graph (DIRECTED or UNDIRECTED) */
    private char graphType = UNDIRECTED;

    /**
     * Constructs an empty undirected graph.
     */
    public Graph() { }

    /**
     * Constructs an empty graph of specified type.
     * @param type The type of graph (DIRECTED or UNDIRECTED)
     */
    public Graph(char type) {
        if (type != DIRECTED && type != UNDIRECTED) {
            throw new IllegalArgumentException("Graph type must be either 'D' (directed) or 'U' (undirected)");
        }
        this.graphType = type;
    }

    /**
     * Constructs a deep copy of the specified graph.
     * @param g The graph to copy
     */
    public Graph(Graph<T> g) {
        this.graphType = g.getGraphType();

        // Copy all nodes (which includes their connections)
        for (GraphNode<T> v : g.getNodes()) {
            this.nodes.add(new GraphNode<T>(v));
        }

        // Copy all links between nodes
        for (GraphNode<T> v : this.getNodes()) {
            for (GraphLink<T> e : v.getLinks()) {
                this.links.add(e);
            }
        }
    }

    /**
     * Constructs an undirected graph from collections of nodes and links.
     * @param nodes Collection of graph nodes
     * @param links Collection of graph links
     */
    public Graph(Collection<GraphNode<T>> nodes, Collection<GraphLink<T>> links) {
        this(UNDIRECTED, nodes, links);
    }

    /**
     * Constructs a graph of specified type from collections of nodes and links.
     * @param type The type of graph (DIRECTED or UNDIRECTED)
     * @param nodes Collection of graph nodes
     * @param links Collection of graph links
     */
    public Graph(char type, Collection<GraphNode<T>> nodes, Collection<GraphLink<T>> links) {
        this(type);

        this.nodes.addAll(nodes);
        this.links.addAll(links);

        // Establish connections between nodes
        for (GraphLink<T> e : links) {
            final GraphNode<T> from = e.fromNode;
            final GraphNode<T> to = e.toNode;

            // Skip links with nodes not in this graph
            if (!this.nodes.contains(from) || !this.nodes.contains(to)) {
                continue;
            }

            from.addLink(e);
            if (this.graphType == UNDIRECTED) {
                // For undirected graphs, add reciprocal link
                GraphLink<T> reciprocal = new GraphLink<T>(e.weight, to, from);
                to.addLink(reciprocal);
                this.links.add(reciprocal);
            }
        }
    }

    /**
     * Returns the type of this graph.
     * @return 'D' for directed, 'U' for undirected
     */
    public char getGraphType() {
        return graphType;
    }

    /**
     * Returns all nodes in this graph.
     * @return List of graph nodes
     */
    public List<GraphNode<T>> getNodes() {
        return nodes;
    }

    /**
     * Returns all links in this graph.
     * @return List of graph links
     */
    public List<GraphLink<T>> getLinks() {
        return links;
    }

    @Override
    public int hashCode() {
        int code = this.graphType + this.nodes.size() + this.links.size();
        for (GraphNode<T> v : nodes) {
            code *= v.hashCode();
        }
        for (GraphLink<T> e : links) {
            code *= e.hashCode();
        }
        return 31 * code;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Graph)) {
            return false;
        }

        final Graph<T> g = (Graph<T>) obj;

        // Graphs must be of same type
        if (this.graphType != g.graphType) {
            return false;
        }

        // Must have same number of nodes and links
        if (this.nodes.size() != g.nodes.size() || this.links.size() != g.links.size()) {
            return false;
        }

        // Compare sorted nodes
        final Object[] thisNodes = this.nodes.toArray();
        Arrays.sort(thisNodes);
        final Object[] otherNodes = g.nodes.toArray();
        Arrays.sort(otherNodes);
        for (int i = 0; i < thisNodes.length; i++) {
            if (!thisNodes[i].equals(otherNodes[i])) {
                return false;
            }
        }

        // Compare sorted links
        final Object[] thisLinks = this.links.toArray();
        Arrays.sort(thisLinks);
        final Object[] otherLinks = g.links.toArray();
        Arrays.sort(otherLinks);
        for (int i = 0; i < thisLinks.length; i++) {
            if (!thisLinks[i].equals(otherLinks[i])) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (GraphNode<T> node : nodes) {
            builder.append(node.toString());
        }
        return builder.toString();
    }

    /**
     * Represents a node in the graph containing data and connections to other nodes.
     * @param <T> The type of data stored in the node
     */
    public static class GraphNode<T extends Comparable<T>> implements Comparable<GraphNode<T>> {

        private T data;
        private int nodeWeight;
        private List<GraphLink<T>> connections;

        /**
         * Creates a node with the given data and default weight (0).
         * @param data The data to store in this node
         */
        public GraphNode(T data) {
            this.data = data;
            this.nodeWeight = 0;
            this.connections = new ArrayList<>();
        }

        /**
         * Creates a node with the given data and weight.
         * @param data The data to store in this node
         * @param weight The weight of this node
         */
        public GraphNode(T data, int weight) {
            this(data);
            this.nodeWeight = weight;
        }

        /**
         * Creates a deep copy of the given node including its connections.
         * @param node The node to copy
         */
        public GraphNode(GraphNode<T> node) {
            this(node.data, node.nodeWeight);
            this.connections.addAll(node.connections);
        }

        /**
         * Returns the data stored in this node.
         * @return The node's data
         */
        public T getData() {
            return data;
        }

        /**
         * Returns the weight of this node.
         * @return The node's weight
         */
        public int getNodeWeight() {
            return nodeWeight;
        }

        /**
         * Sets the weight of this node.
         * @param weight The new weight
         */
        public void setNodeWeight(int weight) {
            this.nodeWeight = weight;
        }

        /**
         * Adds a connection (link) from this node to another.
         * @param link The link to add
         */
        public void addLink(GraphLink<T> link) {
            connections.add(link);
        }

        /**
         * Returns all outgoing connections from this node.
         * @return List of outgoing links
         */
        public List<GraphLink<T>> getLinks() {
            return connections;
        }

        /**
         * Returns the link from this node to the specified node if it exists.
         * @param target The target node
         * @return The link to target node, or null if no such link exists
         */
        public GraphLink<T> getLink(GraphNode<T> target) {
            for (GraphLink<T> link : connections) {
                if (link.toNode.equals(target)) {
                    return link;
                }
            }
            return null;
        }

        /**
         * Checks if there is a direct connection from this node to the target node.
         * @param target The target node
         * @return true if a direct connection exists, false otherwise
         */
        public boolean hasPathTo(GraphNode<T> target) {
            return getLink(target) != null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = data.hashCode();
            result = prime * result + nodeWeight;
            result = prime * result + connections.size();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof GraphNode)) return false;

            GraphNode<T> other = (GraphNode<T>) obj;

            if (nodeWeight != other.nodeWeight) return false;
            if (!data.equals(other.data)) return false;
            if (connections.size() != other.connections.size()) return false;

            // Compare connections by weight only
            Iterator<GraphLink<T>> thisIter = connections.iterator();
            Iterator<GraphLink<T>> otherIter = other.connections.iterator();
            while (thisIter.hasNext() && otherIter.hasNext()) {
                if (thisIter.next().weight != otherIter.next().weight) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int compareTo(GraphNode<T> other) {
            // First compare by data
            int dataCompare = this.data.compareTo(other.data);
            if (dataCompare != 0) return dataCompare;

            // Then by weight
            if (this.nodeWeight != other.nodeWeight) {
                return Integer.compare(this.nodeWeight, other.nodeWeight);
            }

            // Then by number of connections
            if (this.connections.size() != other.connections.size()) {
                return Integer.compare(this.connections.size(), other.connections.size());
            }

            // Finally compare connections by weight
            Iterator<GraphLink<T>> thisIter = connections.iterator();
            Iterator<GraphLink<T>> otherIter = other.connections.iterator();
            while (thisIter.hasNext() && otherIter.hasNext()) {
                int weightCompare = Integer.compare(thisIter.next().weight, otherIter.next().weight);
                if (weightCompare != 0) return weightCompare;
            }

            return 0;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Node [data=").append(data)
                  .append(", weight=").append(nodeWeight)
                  .append("]\n");
            for (GraphLink<T> link : connections) {
                builder.append("\t").append(link.toString());
            }
            return builder.toString();
        }
    }

    /**
     * Represents a connection between two nodes in the graph with an associated weight.
     * @param <T> The type of data stored in the connected nodes
     */
    public static class GraphLink<T extends Comparable<T>> implements Comparable<GraphLink<T>> {

        private GraphNode<T> fromNode;
        private GraphNode<T> toNode;
        private int weight;

        /**
         * Creates a weighted connection between two nodes.
         * @param weight The weight/cost of this connection
         * @param from The source node
         * @param to The destination node
         * @throws NullPointerException if either node is null
         */
        public GraphLink(int weight, GraphNode<T> from, GraphNode<T> to) {
            if (from == null || to == null) {
                throw new NullPointerException("Both source and destination nodes must be non-null");
            }
            this.weight = weight;
            this.fromNode = from;
            this.toNode = to;
        }

        /**
         * Creates a copy of an existing link.
         * @param link The link to copy
         */
        public GraphLink(GraphLink<T> link) {
            this(link.weight, link.fromNode, link.toNode);
        }

        /**
         * Returns the weight of this link.
         * @return The link's weight
         */
        public int getWeight() {
            return weight;
        }

        /**
         * Sets the weight of this link.
         * @param weight The new weight
         */
        public void setWeight(int weight) {
            this.weight = weight;
        }

        /**
         * Returns the source node of this link.
         * @return The source node
         */
        public GraphNode<T> getFromNode() {
            return fromNode;
        }

        /**
         * Returns the destination node of this link.
         * @return The destination node
         */
        public GraphNode<T> getToNode() {
            return toNode;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = fromNode.hashCode();
            result = prime * result + toNode.hashCode();
            result = prime * result + weight;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof GraphLink)) return false;

            GraphLink<T> other = (GraphLink<T>) obj;

            return weight == other.weight &&
                   fromNode.equals(other.fromNode) &&
                   toNode.equals(other.toNode);
        }

        @Override
        public int compareTo(GraphLink<T> other) {
            // First compare by weight
            int weightCompare = Integer.compare(this.weight, other.weight);
            if (weightCompare != 0) return weightCompare;

            // Then by source node
            int fromCompare = this.fromNode.compareTo(other.fromNode);
            if (fromCompare != 0) return fromCompare;

            // Finally by destination node
            return this.toNode.compareTo(other.toNode);
        }

        @Override
        public String toString() {
            return String.format("Link [%s -> %s, weight=%d]\n",
                fromNode.getData(), toNode.getData(), weight);
        }
    }

    /**
     * Represents a node with an associated cost, used in pathfinding algorithms.
     * @param <T> The type of data stored in the node
     */
    public static class NodeCostPair<T extends Comparable<T>> implements Comparable<NodeCostPair<T>> {

        private int pathCost;
        private GraphNode<T> node;

        /**
         * Creates a cost-node pair.
         * @param cost The cost to reach this node
         * @param node The graph node
         * @throws NullPointerException if node is null
         */
        public NodeCostPair(int cost, GraphNode<T> node) {
            if (node == null) {
                throw new NullPointerException("Node cannot be null");
            }
            this.pathCost = cost;
            this.node = node;
        }

        /**
         * Returns the path cost to reach this node.
         * @return The path cost
         */
        public int getPathCost() {
            return pathCost;
        }

        /**
         * Sets the path cost to reach this node.
         * @param cost The new path cost
         */
        public void setPathCost(int cost) {
            this.pathCost = cost;
        }

        /**
         * Returns the node in this pair.
         * @return The graph node
         */
        public GraphNode<T> getNode() {
            return node;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = pathCost;
            result = prime * result + node.hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof NodeCostPair)) return false;

            NodeCostPair<?> other = (NodeCostPair<?>) obj;
            return pathCost == other.pathCost &&
                   node.equals(other.node);
        }

        @Override
        public int compareTo(NodeCostPair<T> other) {
            if (other == null) {
                throw new NullPointerException("Cannot compare with null");
            }
            return Integer.compare(this.pathCost, other.pathCost);
        }

        @Override
        public String toString() {
            return String.format("NodeCostPair [node=%s, cost=%d]",
                node.getData(), pathCost);
        }
    }

    /**
     * Represents a path through the graph with an associated total cost.
     * @param <T> The type of data stored in the nodes along the path
     */
    public static class PathCostPair<T extends Comparable<T>> {

        private int totalCost;
        private List<GraphLink<T>> pathLinks;

        /**
         * Creates a path with the given total cost and sequence of links.
         * @param cost The total cost of the path
         * @param path The sequence of links comprising the path
         * @throws NullPointerException if path is null
         */
        public PathCostPair(int cost, List<GraphLink<T>> path) {
            if (path == null) {
                throw new NullPointerException("Path cannot be null");
            }
            this.totalCost = cost;
            this.pathLinks = path;
        }

        /**
         * Returns the total cost of this path.
         * @return The total cost
         */
        public int getTotalCost() {
            return totalCost;
        }

        /**
         * Sets the total cost of this path.
         * @param cost The new total cost
         */
        public void setTotalCost(int cost) {
            this.totalCost = cost;
        }

        /**
         * Returns the sequence of links comprising this path.
         * @return The path links
         */
        public List<GraphLink<T>> getPathLinks() {
            return pathLinks;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = totalCost;
            for (GraphLink<T> link : pathLinks) {
                result = prime * result + link.hashCode();
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof PathCostPair)) return false;

            PathCostPair<?> other = (PathCostPair<?>) obj;
            if (totalCost != other.totalCost) return false;
            if (pathLinks.size() != other.pathLinks.size()) return false;

            // Compare each link in the path
            Iterator<GraphLink<T>> thisIter = pathLinks.iterator();
            Iterator<?> otherIter = other.pathLinks.iterator();
            while (thisIter.hasNext() && otherIter.hasNext()) {
                if (!thisIter.next().equals(otherIter.next())) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("PathCostPair [totalCost=").append(totalCost).append("]\n");
            for (GraphLink<T> link : pathLinks) {
                builder.append("\t").append(link.toString());
            }
            return builder.toString();
        }
    }
}