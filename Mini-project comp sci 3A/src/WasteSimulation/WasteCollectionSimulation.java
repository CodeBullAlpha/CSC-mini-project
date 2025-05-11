import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.util.concurrent.TimeUnit;


/**
 * Waste Collection Simulation Program using Graph ADT
 * Manages the simulation and display
 */
public class WasteCollectionSimulation extends Application {
    // Constants
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

    public static final int SMALL = 20;
    public static final int MEDIUM = 25;
    public static final int LARGE = 29;
    
    static final String WASTE_IMAGE_PATH = "waste_images/";
    static final String BIN_IMAGE_PATH = "bin_images/";

    // Simulation state
    private Graph<GridCell> graph;
    private char[][] displayMap;
    public List<Robot> robots = new ArrayList<>();
    public List<Waste> wastes = new ArrayList<>();
    public List<Bin> bins = new ArrayList<>();
    int fieldOfView;
    int rows;
    int cols;
    private boolean mapFullyExplored = false;
    private boolean isPaused = true;
    boolean waitingForUser = false;
    private Scanner scanner = new Scanner(System.in);


    private Display display;
    private Stage displayStage;
    public WasteCollectionSimulation() {}
        
    public WasteCollectionSimulation(int size, int numRobots, int fieldOfView) {
        this.rows = size;
        this.cols = size;
        this.fieldOfView = fieldOfView;

        generateGraph();
        spawnRobots(numRobots);


	isPaused = false;
        
        // Initialize JavaFX components on the FX thread
        Platform.runLater(() -> {
            displayStage = new Stage();
            display = new Display(displayMap);
            displayStage.setScene(display.getCurrentScene());
            displayStage.show();
        });
        
        // Start simulation in a separate thread
        new Thread(() -> {
		while (!mapFullyExplored) {
		    if (!isPaused) {
			updateSimulation();
			updateDisplayMap();
                    
			// Update UI on FX thread
			Platform.runLater(() -> {
				display.updateMaize(displayMap);
				displayStage.setScene(display.getCurrentScene());
				displayStage.sizeToScene();
				printMap();
			    });
                    
			try {
			    TimeUnit.SECONDS.sleep(1);
			} catch (InterruptedException e) {
			    e.printStackTrace();
			}
		    }
		    mapFullyExplored = isMapFullyExplored();
		}
        }).start();

    }

    private void generateGraph() {
        this.graph = new Graph<>('U');
        Random rand = new Random();

        // Create all nodes
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                char type = WALKABLE;
                if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1) {
                    type = WALL;
                }
                graph.getNodes().add(new Graph.GraphNode<>(new GridCell(r, c, type)));
            }
        }

        // Add random interior walls
        for (int i = 0; i < rows * cols / 6; i++) {
            int r = rand.nextInt(rows);
            int c = rand.nextInt(cols);
            if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1) continue;

            Graph.GraphNode<GridCell> node = findNode(r, c);
            if (node != null) {
                node.getData().type = WALL;
            }
        }

        // Add bins
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

                int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
                for (int[] dir : directions) {
                    int nr = r + dir[0];
                    int nc = c + dir[1];
                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                        Graph.GraphNode<GridCell> neighbor = findNode(nr, nc);
                        if (neighbor != null && neighbor.getData().type != WALL) {
                            Graph.GraphLink<GridCell> link = new Graph.GraphLink<>(1, node, neighbor);
                            graph.getLinks().add(link);
                            node.addLink(link);

                            Graph.GraphLink<GridCell> reverseLink = new Graph.GraphLink<>(1, neighbor, node);
                            graph.getLinks().add(reverseLink);
                            neighbor.addLink(reverseLink);
                        }
                    }
                }
            }
        }
    }

    public Graph.GraphNode<GridCell> findNode(int row, int col) {
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
            robots.add(new Robot(this, r, c));
        }
    }


    private boolean allWasteDisposed() {
        for (Waste waste : wastes) {
            if (waste.identified) {
                return false;
            }
        }
        return true;
    }

    private void updateSimulation() {
        for (Robot robot : robots) {
            robot.act();
        }
    }

    private void updateDisplayMap() {
        displayMap = new char[rows][cols];
        for (Graph.GraphNode<GridCell> node : graph.getNodes()) {
            GridCell cell = node.getData();
            displayMap[cell.row][cell.col] = cell.type;
        }
        
        for (Bin bin : bins) {
            char binChar = switch (bin.type) {
                case PLASTIC -> PLASTIC_BIN;
                case PAPER -> PAPER_BIN;
                case METAL -> METAL_BIN;
                case GLASS -> GLASS_BIN;
            };
            displayMap[bin.row][bin.col] = binChar;
        }
        
        for (Waste waste : wastes) {
            char wasteChar = waste.identified ? switch (waste.type) {
                case PLASTIC -> PLASTIC_WASTE;
                case PAPER -> PAPER_WASTE;
                case METAL -> METAL_WASTE;
                case GLASS -> GLASS_WASTE;
            } : UNIDENTIFIED_WASTE;
            displayMap[waste.row][waste.col] = wasteChar;
        }
        
        for (Robot robot : robots) {
            char robotChar = robot.carrying != null ? ROBOT_CARRYING : ROBOT_EMPTY;
            displayMap[robot.row][robot.col] = robotChar;
        }

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
                    if (robot.hasLineOfSight(robot.row, robot.col, r, c)
                            && (displayMap[r][c] == WALKABLE || displayMap[r][c] == EXPLORED_AREA)) {
                        displayMap[r][c] = FIELD_OF_VIEW;
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
                System.out.print(displayMap[r][c]);
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

    public Bin findBinAt(int r, int c) {
        for (Bin bin : bins) {
            if (bin.row == r && bin.col == c) {
                return bin;
            }
        }
        return null;
    }

    public Waste findWasteAt(int r, int c) {
        for (Waste waste : wastes) {
            if (waste.row == r && waste.col == c) {
                return waste;
            }
        }
        return null;
    }

    public void removeWaste(Waste waste) {
        wastes.remove(waste);
        Graph.GraphNode<GridCell> node = findNode(waste.row, waste.col);
        if (node != null) node.getData().type = WALKABLE;
    }

    public void showClassificationDialog(Waste waste) {
        waitingForUser = true;
        System.out.println("\n=== WASTE CLASSIFICATION ===");
        System.out.println("Detected Color: " + getColorName(waste.imageFile));
        System.out.println("Classified as: " + waste.type);
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
        waitingForUser = false;
    }

    public void showDisposalDialog(Waste waste, Bin bin) {
        waitingForUser = true;
        System.out.println("\n=== WASTE DISPOSAL ===");
        System.out.println("Waste Type: " + waste.type);
        System.out.println("Into Bin Type: " + bin.type);

        if (waste.type == bin.type) {
            System.out.println("CORRECT DISPOSAL!");
        } else {
            System.out.println("INCORRECT DISPOSAL! Wrong bin type!");
        }

        int remaining = 0;
        for (Waste w : wastes) {
            if (w.identified) remaining++;
        }
        System.out.println("Remaining identified waste: " + remaining);
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

    Color getDominantColor(BufferedImage image) {
        int x = image.getWidth() / 2;
        int y = image.getHeight() / 2;
        return new Color(image.getRGB(x, y));
    }

    public boolean isRed(Color color) {
        return color.getRed() > color.getGreen() + 50 && color.getRed() > color.getBlue() + 50;
    }

    public boolean isBlue(Color color) {
        return color.getBlue() > color.getRed() + 50 && color.getBlue() > color.getGreen() + 50;
    }

    public boolean isYellow(Color color) {
        return color.getRed() > 200 && color.getGreen() > 200 && color.getBlue() < 100;
    }

    public boolean isGreen(Color color) {
        return color.getGreen() > color.getRed() + 50 && color.getGreen() > color.getBlue() + 50;
    }

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
	FXMLLoader loader = new FXMLLoader(WasteCollectionSimulation.class.getResource("fxml/greeter.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 550, 800);
        
        Button startSimulationBtn = (Button) root.getChildrenUnmodifiable().getLast();
        
        if(startSimulationBtn != null ) {
        
	    startSimulationBtn.setOnAction(e -> {
		    primaryStage.close();
		    WasteCollectionSimulation simulation = new WasteCollectionSimulation(
											 MEDIUM,
											 10,
											 3
											 );
		});
        }
        
                
        primaryStage.setTitle("Waste Collection Simulation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    
}
