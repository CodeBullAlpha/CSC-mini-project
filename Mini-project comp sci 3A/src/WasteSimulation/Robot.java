import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class Robot {
    public int row, col;
    public Waste carrying = null;
    public Bin targetBin = null;
    public List<Point> path = new ArrayList<>();
    public List<Point> knownWastes = new ArrayList<>();
    public List<Bin> knownBins = new ArrayList<>();
    public boolean[][] exploredMap;
    public Point explorationTarget;
    
    private WasteCollectionSimulation simulation;
    private int fieldOfView;

    public Robot(WasteCollectionSimulation simulation, int r, int c) {
        this.simulation = simulation;
        this.row = r;
        this.col = c;
        this.fieldOfView = simulation.fieldOfView;
        this.exploredMap = new boolean[simulation.rows][simulation.cols];
        exploreCurrentPosition();
    }

    private void exploreCurrentPosition() {
        exploredMap[row][col] = true;

        int minRow = Math.max(0, row - fieldOfView);
        int maxRow = Math.min(simulation.rows - 1, row + fieldOfView);
        int minCol = Math.max(0, col - fieldOfView);
        int maxCol = Math.min(simulation.cols - 1, col + fieldOfView);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                if (hasLineOfSight(row, col, r, c)) {
                    exploredMap[r][c] = true;

                    // Discover wastes
                    Graph.GraphNode<GridCell> node = simulation.findNode(r, c);
                    if (node != null && node.getData().type == WasteCollectionSimulation.UNIDENTIFIED_WASTE) {
                        Point wasteLoc = new Point(r, c);
                        if (!knownWastes.contains(wasteLoc)) {
                            knownWastes.add(wasteLoc);
                        }
                    }

                    // Discover bins
                    node = simulation.findNode(r, c);
                    if (node != null) {
                        char cell = node.getData().type;
                        if (cell == WasteCollectionSimulation.PLASTIC_BIN || 
                            cell == WasteCollectionSimulation.PAPER_BIN || 
                            cell == WasteCollectionSimulation.METAL_BIN || 
                            cell == WasteCollectionSimulation.GLASS_BIN) {
                            Bin bin = simulation.findBinAt(r, c);
                            if (bin != null && !knownBins.contains(bin)) {
                                knownBins.add(bin);
                            }
                        }
                    }
                }
            }
        }
    }

    public void act() {
        if (simulation.waitingForUser) return;

        exploreCurrentPosition();
        identifyWastes();
        handleMovement();
    }

    private void identifyWastes() {
        for (Waste waste : simulation.wastes) {
            if (hasLineOfSight(row, col, waste.row, waste.col)) {
                waste.identified = true;
                Point wasteLoc = new Point(waste.row, waste.col);
                if (!knownWastes.contains(wasteLoc)) {
                    knownWastes.add(wasteLoc);
                }
            }
        }
    }

    private void handleMovement() {
        if (carrying != null) {
            handleWasteDisposal();
        } else {
            findAndCollectWaste();
        }
    }

    private void handleWasteDisposal() {
        if (targetBin != null) {
            if (path.isEmpty()) {
                path = findPath(new Point(row, col), new Point(targetBin.row, targetBin.col));
            }

            if (!path.isEmpty()) {
                moveAlongPath();
            }

            if (row == targetBin.row && col == targetBin.col) {
                simulation.showDisposalDialog(carrying, targetBin);
                carrying = null;
                targetBin = null;
            }
        } else {
            targetBin = findNearestMatchingBin(carrying.type);
            if (targetBin != null) {
                path = findPath(new Point(row, col), new Point(targetBin.row, targetBin.col));
            }
        }
    }

    private void findAndCollectWaste() {
        // Try to pick up waste at current position
        Waste waste = simulation.findWasteAt(row, col);
        if (waste != null) {
            carrying = waste;
            simulation.removeWaste(waste);
            knownWastes.remove(new Point(row, col));

            WasteType classifiedType = classifyWaste(waste.imageFile);
            carrying.type = classifiedType;

            simulation.showClassificationDialog(waste);
            return;
        }

        // Move toward nearest known waste
        if (!knownWastes.isEmpty()) {
            Point nearestWaste = findNearestWaste();
            if (nearestWaste != null) {
                path = findPath(new Point(row, col), nearestWaste);
                if (!path.isEmpty()) {
                    moveAlongPath();
                }
            }
        }
        // Explore if no wastes known
        else {
            if (explorationTarget == null || (row == explorationTarget.x && col == explorationTarget.y)) {
                explorationTarget = findNearestUnexplored();
            }

            if (explorationTarget != null) {
                path = findPath(new Point(row, col), explorationTarget);
                if (!path.isEmpty()) {
                    moveAlongPath();
                }
            } else {
                randomMove();
            }
        }
    }

    private WasteType classifyWaste(File wasteImage) {
        try {
        	
        
            BufferedImage image = javax.imageio.ImageIO.read(wasteImage);
            if (image == null) return WasteType.PLASTIC;

            Color color = simulation.getDominantColor(image);
            if (simulation.isRed(color)) return WasteType.METAL;
            if (simulation.isBlue(color)) return WasteType.PAPER;
            if (simulation.isYellow(color)) return WasteType.PLASTIC;
            if (simulation.isGreen(color)) return WasteType.GLASS;

            return WasteType.PLASTIC;
        } catch (Exception e) {
            return WasteType.PLASTIC;
        }
    }

    private Point findNearestWaste() {
        Point nearest = null;
        double minDistance = Double.MAX_VALUE;

        Iterator<Point> iterator = knownWastes.iterator();
        while (iterator.hasNext()) {
            Point wasteLoc = iterator.next();
            boolean wasteExists = false;

            for (Waste w : simulation.wastes) {
                if (w.row == wasteLoc.x && w.col == wasteLoc.y) {
                    wasteExists = true;
                    break;
                }
            }

            if (!wasteExists) {
                iterator.remove();
                continue;
            }

            double distance = Math.sqrt(Math.pow(wasteLoc.x - row, 2) + Math.pow(wasteLoc.y - col, 2));
            if (distance < minDistance) {
                minDistance = distance;
                nearest = wasteLoc;
            }
        }

        return nearest;
    }

    private Point findNearestUnexplored() {
        Queue<Point> queue = new LinkedList<>();
        boolean[][] visited = new boolean[simulation.rows][simulation.cols];
        Map<Point, Point> parent = new HashMap<>();

        queue.add(new Point(row, col));
        visited[row][col] = true;
        parent.put(new Point(row, col), null);

        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (!exploredMap[current.x][current.y]) {
                while (parent.get(current) != null && !parent.get(current).equals(new Point(row, col))) {
                    current = parent.get(current);
                }
                return current;
            }

            Graph.GraphNode<GridCell> currentNode = simulation.findNode(current.x, current.y);
            if (currentNode == null) continue;

            for (Graph.GraphLink<GridCell> link : currentNode.getLinks()) {
                GridCell neighborCell = link.getToNode().getData();
                Point neighbor = new Point(neighborCell.row, neighborCell.col);
                if (!visited[neighbor.x][neighbor.y]) {
                    queue.add(neighbor);
                    visited[neighbor.x][neighbor.y] = true;
                    parent.put(neighbor, current);
                }
            }
        }
        return null;
    }

    private List<Point> findPath(Point start, Point goal) {
        Queue<Point> queue = new LinkedList<>();
        Map<Point, Point> parent = new HashMap<>();

        queue.add(start);
        parent.put(start, null);

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(goal)) break;

            Graph.GraphNode<GridCell> currentNode = simulation.findNode(current.x, current.y);
            if (currentNode == null) continue;

            for (Graph.GraphLink<GridCell> link : currentNode.getLinks()) {
                GridCell neighborCell = link.getToNode().getData();
                Point neighbor = new Point(neighborCell.row, neighborCell.col);

                if (!parent.containsKey(neighbor)) {
                    parent.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        List<Point> path = new ArrayList<>();
        Point current = goal;
        while (current != null && !current.equals(start)) {
            path.add(0, current);
            current = parent.get(current);
        }
        return path;
    }

    private void moveAlongPath() {
        if (path.isEmpty()) return;
        
        Point next = path.get(0);
        Graph.GraphNode<GridCell> nextNode = simulation.findNode(next.x, next.y);
        if (nextNode != null && nextNode.getData().type != WasteCollectionSimulation.WALL) {
            row = next.x;
            col = next.y;
            path.remove(0);
        }
    }

    private void randomMove() {
        Graph.GraphNode<GridCell> currentNode = simulation.findNode(row, col);
        if (currentNode == null) return;

        List<Graph.GraphLink<GridCell>> links = new ArrayList<>(currentNode.getLinks());
        Collections.shuffle(links);

        for (Graph.GraphLink<GridCell> link : links) {
            GridCell neighbor = link.getToNode().getData();
            if (neighbor.type != WasteCollectionSimulation.WALL) {
                row = neighbor.row;
                col = neighbor.col;
                break;
            }
        }
    }

    public boolean hasLineOfSight(int x0, int y0, int x1, int y1) {
        if (Math.abs(x1 - x0) > fieldOfView || Math.abs(y1 - y0) > fieldOfView) {
            return false;
        }

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            Graph.GraphNode<GridCell> node = simulation.findNode(x0, y0);
            if (node == null || node.getData().type == WasteCollectionSimulation.WALL)
                return false;
            if (x0 == x1 && y0 == y1)
                return true;

            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    private Bin findNearestMatchingBin(WasteType type) {
        Bin nearestBin = null;
        double minDistance = Double.MAX_VALUE;

        for (Bin bin : knownBins) {
            if (bin.type == type) {
                double distance = Math.sqrt(Math.pow(bin.row - row, 2) + Math.pow(bin.col - col, 2));
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestBin = bin;
                }
            }
        }

        if (nearestBin == null) {
            for (Bin bin : simulation.bins) {
                if (bin.type == type) {
                    double distance = Math.sqrt(Math.pow(bin.row - row, 2) + Math.pow(bin.col - col, 2));
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestBin = bin;
                    }
                }
            }
        }

        return nearestBin;
    }
}