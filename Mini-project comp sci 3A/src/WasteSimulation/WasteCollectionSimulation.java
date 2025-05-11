package WasteSimulation;

import javax.imageio.ImageIO;

import WGraph.Graph;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Waste Collection Simulation Program using Graph ADT
 * Simulates robots collecting and sorting waste based on color recognition
 */
public class WasteCollectionSimulation {
    // ==================== CONSTANTS ====================
    static final int SMALL = 20, MEDIUM = 25, LARGE = 29;
    static final char WALL = '#';
    static final char WALKABLE = '.';
    static final char ROBOT_EMPTY = 'R';
    static final char ROBOT_CARRYING = 'O';
    static final char UNIDENTIFIED_WASTE = '?';
    static final char PLASTIC_WASTE = 's';
    static final char PAPER_WASTE = 'p';
    static final char METAL_WASTE = 'm';
    static final char GLASS_WASTE = 'g';
    static final char PLASTIC_BIN = 'S';
    static final char PAPER_BIN = 'P';
    static final char METAL_BIN = 'M';
    static final char GLASS_BIN = 'G';
    static final char EXPLORED_AREA = '.';
    static final char FIELD_OF_VIEW = ' ';
    
    static final String WASTE_IMAGE_PATH = "waste_images/";
    static final String BIN_IMAGE_PATH = "bin_images/";

    // ==================== SIMULATION STATE ====================
    private Graph<GridCell> graph;
    private char[][] displayMap;
    private List<Robot> robots = new ArrayList<>();
    private List<Waste> wastes = new ArrayList<>();
    private List<Bin> bins = new ArrayList<>();
    private int fieldOfView;
    private int rows, cols;
    private boolean mapFullyExplored = false;
    private boolean isPaused = true;
    private boolean waitingForUser = false;
    private Scanner scanner = new Scanner(System.in);

    // ==================== GRID CELL CLASS ====================
    class GridCell implements Comparable<GridCell> {
        int row, col;
        char type;
        
        public GridCell(int row, int col, char type) {
            this.row = row;
            this.col = col;
            this.type = type;
        }
        
        @Override
        public int compareTo(GridCell other) {
            if (this.row != other.row) return Integer.compare(this.row, other.row);
            if (this.col != other.col) return Integer.compare(this.col, other.col);
            return Character.compare(this.type, other.type);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof GridCell)) return false;
            GridCell other = (GridCell)obj;
            return this.row == other.row && this.col == other.col && this.type == other.type;
        }
        
        @Override
        public int hashCode() {
            return 31 * (31 * row + col) + type;
        }
        
        @Override
        public String toString() {
            return String.format("Cell[%d,%d,%c]", row, col, type);
        }
    }

    // ==================== MAIN METHOD ====================
    public static void main(String[] args) {
        System.out.println("Waste Sorting Simulation - Graph ADT Version");
        new WasteCollectionSimulation(LARGE, 1, 2);
    }

    // ==================== INITIALIZATION METHODS ====================

    public WasteCollectionSimulation(int size, int numRobots, int fieldOfView) {
        this.rows = size;
        this.cols = size;
        this.fieldOfView = fieldOfView;

        generateGraph();         // Create the game graph
        spawnRobots(numRobots);  // Place robots on graph

        System.out.println("Simulation Initialized - Map Size: " + rows + "x" + cols);
        System.out.println("Press Enter to start...");
        scanner.nextLine();
        
        startSimulation();       // Begin main simulation loop
    }

    private void generateGraph() {
        // Initialize graph as undirected
        this.graph = new Graph<>('U');
        Random rand = new Random();
        
        // First create all nodes
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char type = WALKABLE;
                // Add border walls
                if (r == 0 || r == rows-1 || c == 0 || c == cols-1) {
                    type = WALL;
                }
                graph.getNodes().add(new Graph.GraphNode<>(new GridCell(r, c, type)));
            }
        }

        // Add random interior walls
        for (int i = 0; i < rows * cols / 6; i++) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (r == 0 || r == rows-1 || c == 0 || c == cols-1) continue;
            
            Graph.GraphNode<GridCell> node = findNode(r, c);
            if (node != null) {
                node.getData().type = WALL;
            }
        }

        // Add bins for each waste type
        for (WasteType type : WasteType.values()) {
            File typeFolder = new File(BIN_IMAGE_PATH + type.toString().toLowerCase());
            File[] binImages = typeFolder.listFiles();
            
            for (int i = 0; i < 2; i++) {
                int r, c;
                Graph.GraphNode<GridCell> node;
                do {
                    r = rand.nextInt(rows);
                    c = rand.nextInt(cols);
                    node = findNode(r, c);
                } while (node == null || node.getData().type != WALKABLE);
                
                char binChar = switch (type) {
                    case PLASTIC -> PLASTIC_BIN;
                    case PAPER -> PAPER_BIN;
                    case METAL -> METAL_BIN;
                    case GLASS -> GLASS_BIN;
                };
                
                node.getData().type = binChar;
                if (binImages != null && binImages.length > 0) {
                    bins.add(new Bin(r, c, type, binImages[rand.nextInt(binImages.length)]));
                }
            }
        }

        // Add waste items
        for (WasteType type : WasteType.values()) {
            File typeFolder = new File(WASTE_IMAGE_PATH + type.toString().toLowerCase());
            File[] wasteImages = typeFolder.listFiles();
            
            for (int i = 0; i < 3; i++) {
                int r, c;
                Graph.GraphNode<GridCell> node;
                do {
                    r = rand.nextInt(rows);
                    c = rand.nextInt(cols);
                    node = findNode(r, c);
                } while (node == null || node.getData().type != WALKABLE);
                
                node.getData().type = UNIDENTIFIED_WASTE;
                if (wasteImages != null && wasteImages.length > 0) {
                    wastes.add(new Waste(r, c, type, wasteImages[rand.nextInt(wasteImages.length)]));
                }
            }
        }

        // Create links between walkable nodes
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Graph.GraphNode<GridCell> node = findNode(r, c);
                if (node == null || node.getData().type == WALL) continue;
                
                // Check all 4 directions
                int[][] directions = {{0,1},{1,0},{0,-1},{-1,0}};
                for (int[] dir : directions) {
                    int nr = r + dir[0];
                    int nc = c + dir[1];
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                        Graph.GraphNode<GridCell> neighbor = findNode(nr, nc);
                        if (neighbor != null && neighbor.getData().type != WALL) {
                            // Add bidirectional link with weight 1
                            Graph.GraphLink<GridCell> link = new Graph.GraphLink<>(1, node, neighbor);
                            graph.getLinks().add(link);
                            node.addLink(link);
                            
                            // Undirected graph needs reciprocal link
                            Graph.GraphLink<GridCell> reverseLink = new Graph.GraphLink<>(1, neighbor, node);
                            graph.getLinks().add(reverseLink);
                            neighbor.addLink(reverseLink);
                        }
                    }
                }
            }
        }
    }

    private Graph.GraphNode<GridCell> findNode(int row, int col) {
        for (Graph.GraphNode<GridCell> node : graph.getNodes()) {
            if (node.getData().row == row && node.getData().col == col) {
                return node;
            }
        }
        return null;
    }

    private void spawnRobots(int count) {
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int r, c;
            Graph.GraphNode<GridCell> node;
            do {
                r = rand.nextInt(rows);
                c = rand.nextInt(cols);
                node = findNode(r, c);
            } while (node == null || node.getData().type != WALKABLE);
            robots.add(new Robot(r, c));
        }
    }

    // ==================== SIMULATION LOOP METHODS ====================

    private void startSimulation() {
        isPaused = false;
        while (!mapFullyExplored) {
            if (!isPaused && !waitingForUser) {
                updateSimulation();
                updateDisplayMap();
                printMap();
                
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            
            mapFullyExplored = isMapFullyExplored();
            if (mapFullyExplored) {
                System.out.println("\nMAP FULLY EXPLORED - SIMULATION COMPLETE!");
                break;
            }
        }
    }

    private void updateSimulation() {
        for (Robot robot : robots) {
            robot.act();
        }
    }

    private void updateDisplayMap() {
        displayMap = new char[rows][cols];
        
        // Initialize display map from graph nodes
        for (Graph.GraphNode<GridCell> node : graph.getNodes()) {
            GridCell cell = node.getData();
            displayMap[cell.row][cell.col] = cell.type;
        }

        // Update waste display status
        for (Waste waste : wastes) {
            if (waste.identified) {
                char wasteChar = switch (waste.type) {
                    case PLASTIC -> PLASTIC_WASTE;
                    case PAPER -> PAPER_WASTE;
                    case METAL -> METAL_WASTE;
                    case GLASS -> GLASS_WASTE;
                };
                displayMap[waste.row][waste.col] = wasteChar;
            } else {
                displayMap[waste.row][waste.col] = UNIDENTIFIED_WASTE;
            }
        }

        // Mark explored areas
        for (Robot robot : robots) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (robot.exploredMap[r][c] && displayMap[r][c] == WALKABLE) {
                        displayMap[r][c] = EXPLORED_AREA;
                    }
                }
            }
        }

        // Mark field of view
        for (Robot robot : robots) {
            int minRow = Math.max(0, robot.row - fieldOfView);
            int maxRow = Math.min(rows - 1, robot.row + fieldOfView);
            int minCol = Math.max(0, robot.col - fieldOfView);
            int maxCol = Math.min(cols - 1, robot.col + fieldOfView);

            for (int r = minRow; r <= maxRow; r++) {
                for (int c = minCol; c <= maxCol; c++) {
                    if (robot.hasLineOfSight(robot.row, robot.col, r, c)) {
                        Graph.GraphNode<GridCell> node = findNode(r, c);
                        if (node != null && node.getData().type == WALKABLE) {
                            displayMap[r][c] = FIELD_OF_VIEW;
                        }
                    }
                }
            }
        }
    }

    private void printMap() {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println("WASTE SORTING SIMULATION");
        System.out.println("Legend: #=Wall .=Walkable R=Robot O=CarryingRobot");
        System.out.println("?=Unknown s=Plastic p=Paper m=Metal g=Glass");
        System.out.println("S=PlasticBin P=PaperBin M=MetalBin G=GlassBin");
        System.out.println(".=Explored ' '=FieldOfView\n");

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                boolean hasRobot = false;
                char robotChar = ROBOT_EMPTY;
                for (Robot robot : robots) {
                    if (robot.row == r && robot.col == c) {
                        hasRobot = true;
                        robotChar = robot.carrying != null ? ROBOT_CARRYING : ROBOT_EMPTY;
                        break;
                    }
                }

                System.out.print(hasRobot ? robotChar : displayMap[r][c]);
                System.out.print(" ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private boolean isMapFullyExplored() {
        for (Graph.GraphNode<GridCell> node : graph.getNodes()) {
            GridCell cell = node.getData();
            if (cell.type != WALL) {
                boolean exploredByAny = false;
                for (Robot robot : robots) {
                    if (robot.exploredMap[cell.row][cell.col]) {
                        exploredByAny = true;
                        break;
                    }
                }
                if (!exploredByAny) return false;
            }
        }
        return true;
    }

    // ==================== ROBOT CLASS ====================
    class Robot {
        int row, col;
        Waste carrying = null;
        Bin targetBin = null;
        List<Point> path = new ArrayList<>();
        List<Point> knownWastes = new ArrayList<>();
        List<Bin> knownBins = new ArrayList<>();
        boolean[][] exploredMap;
        Point explorationTarget;

        public Robot(int r, int c) {
            this.row = r;
            this.col = c;
            this.exploredMap = new boolean[rows][cols];
            exploreCurrentPosition();
        }

        private void exploreCurrentPosition() {
            exploredMap[row][col] = true;

            int minRow = Math.max(0, row - fieldOfView);
            int maxRow = Math.min(rows - 1, row + fieldOfView);
            int minCol = Math.max(0, col - fieldOfView);
            int maxCol = Math.min(cols - 1, col + fieldOfView);

            for (int r = minRow; r <= maxRow; r++) {
                for (int c = minCol; c <= maxCol; c++) {
                    if (hasLineOfSight(row, col, r, c)) {
                        exploredMap[r][c] = true;
                        
                        // Discover wastes
                        Graph.GraphNode<GridCell> node = findNode(r, c);
                        if (node != null && node.getData().type == UNIDENTIFIED_WASTE) {
                            Point wasteLoc = new Point(r, c);
                            if (!knownWastes.contains(wasteLoc)) {
                                knownWastes.add(wasteLoc);
                            }
                        }
                        
                        // Discover bins
                        node = findNode(r, c);
                        if (node != null) {
                            char cell = node.getData().type;
                            if (cell == PLASTIC_BIN || cell == PAPER_BIN || 
                                cell == METAL_BIN || cell == GLASS_BIN) {
                                Bin bin = findBinAt(r, c);
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
            if (waitingForUser) return;

            exploreCurrentPosition();
            identifyWastes();
            handleMovement();
        }

        private void identifyWastes() {
            for (Waste waste : wastes) {
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
                    path = findPathToBin(targetBin);
                }

                if (!path.isEmpty()) {
                    moveAlongPath();
                }

                if (row == targetBin.row && col == targetBin.col) {
                    showDisposalDialog(carrying, targetBin);
                    carrying = null;
                    targetBin = null;
                }
            } else {
                targetBin = findNearestMatchingBin(carrying.type);
                if (targetBin != null) {
                    path = findPathToBin(targetBin);
                }
            }
        }

        private void findAndCollectWaste() {
            // Try to pick up waste at current position
            for (Waste waste : wastes) {
                if (waste.row == row && waste.col == col) {
                    carrying = waste;
                    wastes.remove(waste);
                    Graph.GraphNode<GridCell> node = findNode(row, col);
                    if (node != null) node.getData().type = WALKABLE;
                    knownWastes.remove(new Point(row, col));

                    WasteType classifiedType = classifyWaste(waste.imageFile);
                    carrying.type = classifiedType;

                    showClassificationDialog(waste);
                    return;
                }
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
                BufferedImage image = ImageIO.read(wasteImage);
                if (image == null) {
                    System.err.println("Could not read waste image: " + wasteImage.getName());
                    return WasteType.PLASTIC;
                }

                Color dominantColor = getDominantColor(image);
                
                if (isRed(dominantColor)) return WasteType.METAL;
                if (isBlue(dominantColor)) return WasteType.PAPER;
                if (isYellow(dominantColor)) return WasteType.PLASTIC;
                if (isGreen(dominantColor)) return WasteType.GLASS;
                
                return WasteType.PLASTIC;
            } catch (IOException e) {
                System.err.println("Error reading waste image: " + wasteImage.getName());
                return WasteType.PLASTIC;
            }
        }

        // ==================== ROBOT HELPER METHODS ====================

        private Color getDominantColor(BufferedImage image) {
            int x = image.getWidth() / 2;
            int y = image.getHeight() / 2;
            return new Color(image.getRGB(x, y));
        }

        private boolean isRed(Color color) {
            return color.getRed() > color.getGreen() + 50 && 
                   color.getRed() > color.getBlue() + 50;
        }

        private boolean isBlue(Color color) {
            return color.getBlue() > color.getRed() + 50 && 
                   color.getBlue() > color.getGreen() + 50;
        }

        private boolean isYellow(Color color) {
            return color.getRed() > 200 && 
                   color.getGreen() > 200 && 
                   color.getBlue() < 100;
        }

        private boolean isGreen(Color color) {
            return color.getGreen() > color.getRed() + 50 && 
                   color.getGreen() > color.getBlue() + 50;
        }

        private List<Point> findPathToBin(Bin bin) {
            return findPath(new Point(row, col), new Point(bin.row, bin.col));
        }

        private Point findNearestWaste() {
            Point nearest = null;
            double minDistance = Double.MAX_VALUE;

            Iterator<Point> iterator = knownWastes.iterator();
            while (iterator.hasNext()) {
                Point wasteLoc = iterator.next();
                boolean wasteExists = false;

                for (Waste w : wastes) {
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
            boolean[][] visited = new boolean[rows][cols];
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

                Graph.GraphNode<GridCell> currentNode = findNode(current.x, current.y);
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

                Graph.GraphNode<GridCell> currentNode = findNode(current.x, current.y);
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
            Point next = path.get(0);
            Graph.GraphNode<GridCell> nextNode = findNode(next.x, next.y);
            if (nextNode != null && nextNode.getData().type != WALL) {
                row = next.x;
                col = next.y;
                path.remove(0);
            }
        }

        private void randomMove() {
            Graph.GraphNode<GridCell> currentNode = findNode(row, col);
            if (currentNode == null) return;

            List<Graph.GraphLink<GridCell>> links = new ArrayList<>(currentNode.getLinks());
            Collections.shuffle(links);

            for (Graph.GraphLink<GridCell> link : links) {
                GridCell neighbor = link.getToNode().getData();
                if (neighbor.type != WALL) {
                    row = neighbor.row;
                    col = neighbor.col;
                    break;
                }
            }
        }

        private boolean hasLineOfSight(int x0, int y0, int x1, int y1) {
            if (Math.abs(x1 - x0) > fieldOfView || Math.abs(y1 - y0) > fieldOfView) {
                return false;
            }

            int dx = Math.abs(x1 - x0);
            int dy = Math.abs(y1 - y0);
            int sx = x0 < x1 ? 1 : -1;
            int sy = y0 < y1 ? 1 : -1;
            int err = dx - dy;

            while (true) {
                Graph.GraphNode<GridCell> node = findNode(x0, y0);
                if (node == null || node.getData().type == WALL) return false;
                if (x0 == x1 && y0 == y1) return true;

                int e2 = 2 * err;
                if (e2 > -dy) { err -= dy; x0 += sx; }
                if (e2 < dx) { err += dx; y0 += sy; }
            }
        }

        private Bin findBinAt(int r, int c) {
            for (Bin bin : bins) {
                if (bin.row == r && bin.col == c) {
                    return bin;
                }
            }
            return null;
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
                for (Bin bin : bins) {
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

        private void showClassificationDialog(Waste waste) {
            waitingForUser = true;
            
            System.out.println("\n=== WASTE CLASSIFICATION ===");
            System.out.println("Robot at (" + row + "," + col + ") picked up:");
            System.out.println("Image: " + waste.imageFile.getName());
            System.out.println("Detected Color: " + getColorName(waste.imageFile));
            System.out.println("Classified as: " + waste.type);
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            
            waitingForUser = false;
        }

        private String getColorName(File imageFile) {
            try {
                BufferedImage image = ImageIO.read(imageFile);
                if (image == null) return "Unknown";
                
                Color color = getDominantColor(image);
                if (isRed(color)) return "Red (Metal)";
                if (isBlue(color)) return "Blue (Paper)";
                if (isYellow(color)) return "Yellow (Plastic)";
                if (isGreen(color)) return "Green (Glass)";
                return "Unknown";
            } catch (IOException e) {
                return "Unknown";
            }
        }

        private void showDisposalDialog(Waste waste, Bin bin) {
            waitingForUser = true;
            
            System.out.println("\n=== WASTE DISPOSAL ===");
            System.out.println("Robot at (" + row + "," + col + ") disposing:");
            System.out.println("Waste Image: " + waste.imageFile.getName());
            System.out.println("Waste Type: " + waste.type);
            System.out.println("Into Bin Image: " + bin.imageFile.getName());
            System.out.println("Bin Type: " + bin.type);
            
            if (waste.type == bin.type) {
                System.out.println("CORRECT DISPOSAL!");
            } else {
                System.out.println("INCORRECT DISPOSAL! Wrong bin type!");
            }
            
            System.out.println("Press Enter to continue...");
            scanner.nextLine();
            
            waitingForUser = false;
        }
    }

    // ==================== SUPPORTING CLASSES ====================

    enum WasteType {
        PLASTIC, PAPER, METAL, GLASS
    }

    class Waste {
        int row, col;
        WasteType type;
        boolean identified = false;
        File imageFile;

        public Waste(int row, int col, WasteType type, File imageFile) {
            this.row = row;
            this.col = col;
            this.type = type;
            this.imageFile = imageFile;
        }
    }

    class Bin {
        int row, col;
        WasteType type;
        File imageFile;

        public Bin(int r, int c, WasteType t, File imageFile) {
            row = r;
            col = c;
            type = t;
            this.imageFile = imageFile;
        }
    }

    class Point {
        int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point point = (Point) obj;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
}