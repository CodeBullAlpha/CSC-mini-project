package WasteSimulation;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;

import WGraph.Graph;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Waste Collection Simulation Program using Graph ADT with Swing GUI
 */
public class WasteCollectionSimulation {
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

	static final String WASTE_IMAGE_PATH = "waste images/";
	static final String BIN_IMAGE_PATH = "bin_images/";

	// Color constants
	private static final int CELL_SIZE = 20;

	// Simulation state
	private Graph<GridCell> graph;
	private char[][] displayMap;
	public java.util.List<Robot> robots = new ArrayList<>();
	public java.util.List<Waste> wastes = new ArrayList<>();
	public java.util.List<Bin> bins = new ArrayList<>();
	int fieldOfView;
	int rows;
	int cols;
	private boolean mapFullyExplored = false;
	private boolean isPaused = true;
	boolean waitingForUser = false;

	// GUI components
	private JFrame frame;
	private SimulationPanel simulationPanel;
	private JButton pauseButton;
	private JLabel statusLabel;
	private Timer simulationTimer;

	public WasteCollectionSimulation() {

	}

	public WasteCollectionSimulation(int size, int numRobots, int fieldOfView) {
		this.rows = size;
		this.cols = size;
		this.fieldOfView = fieldOfView;

		initializeGUI();
		generateGraph();
		spawnRobots(numRobots);

		startSimulation();
	}

	private void initializeGUI() {
		frame = new JFrame("Waste Collection Simulation");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		// Create simulation panel
		simulationPanel = new SimulationPanel();
		simulationPanel.setPreferredSize(new Dimension(cols * CELL_SIZE + 200, rows * CELL_SIZE));
		frame.add(simulationPanel, BorderLayout.CENTER);

		// Create control panel
		JPanel controlPanel = new JPanel();
		pauseButton = new JButton("Start");
		pauseButton.addActionListener(e -> togglePause());

		statusLabel = new JLabel("Simulation Ready - Click Start to begin");

		controlPanel.add(pauseButton);
		controlPanel.add(statusLabel);
		frame.add(controlPanel, BorderLayout.SOUTH);

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		// Set up simulation timer
		simulationTimer = new Timer(500, e -> {
			if (!isPaused && !waitingForUser) {
				updateSimulation();
				updateDisplayMap();
				simulationPanel.repaint();

				mapFullyExplored = isMapFullyExplored();
				if (mapFullyExplored && allWasteDisposed()) {
					simulationTimer.stop();
					statusLabel.setText("SIMULATION COMPLETE - All waste collected and map explored!");
					JOptionPane.showMessageDialog(frame,
							"MAP FULLY EXPLORED AND ALL WASTE DISPOSED - SIMULATION COMPLETE!", "Simulation Complete",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
	}

	private void togglePause() {
		isPaused = !isPaused;
		pauseButton.setText(isPaused ? "Resume" : "Pause");
		statusLabel.setText(isPaused ? "Simulation Paused" : "Simulation Running");

		if (!isPaused && !simulationTimer.isRunning()) {
			simulationTimer.start();
		}
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
			if (r == 0 || r == rows - 1 || c == 0 || c == cols - 1)
				continue;

			Graph.GraphNode<GridCell> node = findNode(r, c);
			if (node != null) {
				node.getData().type = WALL;
			}
		}

		// Add bins
		// Add bins
		for (WasteType type : WasteType.values()) {
		    File typeFolder = new File(BIN_IMAGE_PATH + type.toString().toLowerCase());
		    File[] binImages = typeFolder.exists() ? typeFolder.listFiles() : null;
		    
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
		        File binImage = null;
		        if (binImages != null && binImages.length > 0) {
		            binImage = binImages[rand.nextInt(binImages.length)];
		        }
		        bins.add(new Bin(r, c, type, binImage));
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
				if (node == null || node.getData().type == WALL)
					continue;

				int[][] directions = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };
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

	private void startSimulation() {
		updateDisplayMap();
		simulationPanel.repaint();
		simulationTimer.start();
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
				if (!exploredByAny)
					return false;
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
		if (node != null)
			node.getData().type = WALKABLE;
	}

	public void showWelcomeScreen() {
		JDialog welcomeDialog = new JDialog(frame, "Welcome", true);
		welcomeDialog.setLayout(new BorderLayout());
		welcomeDialog.setSize(600, 400);
		welcomeDialog.setLocationRelativeTo(frame);

		JTextArea welcomeText = new JTextArea("Welcome to the Waste Men, Waste Management Simulation!\n\n"
				+ "In this simulation, you'll explore an intelligent waste disposal system powered by autonomous robots "
				+ "and machine learning. Each robot is equipped with a Graph Convolutional Network (GCN) classifier that "
				+ "enables it to classify various types of waste—such as plastic, paper, metal, and glass "
				+ " extraction.\n\n"
				+ "Once classified, the robots efficiently dispose of the waste in designated waste bins, demonstrating "
				+ "how AI and robotics can work together to support cleaner, smarter environments. This simulation offers "
				+ "a glimpse into the future of sustainable waste management through real-time automation and intelligent "
				+ "decision-making.\n\n"
				+ "Get ready to see robotics and AI in action—Welcome To The Future Of Waste Disposal.");
		welcomeText.setEditable(false);
		welcomeText.setLineWrap(true);
		welcomeText.setWrapStyleWord(true);
		welcomeText.setMargin(new Insets(20, 20, 20, 20));
		welcomeText.setFont(new Font("Arial", Font.PLAIN, 14));

		JButton continueButton = new JButton("Continue");
		continueButton.addActionListener(e -> {
			welcomeDialog.dispose();
			showSetupInfoScreen();
		});

		welcomeDialog.add(new JScrollPane(welcomeText), BorderLayout.CENTER);
		welcomeDialog.add(continueButton, BorderLayout.SOUTH);
		welcomeDialog.setVisible(true);
	}

	private void showSetupInfoScreen() {
		JDialog setupInfoDialog = new JDialog(frame, "Simulation Setup", true);
		setupInfoDialog.setLayout(new BorderLayout());
		setupInfoDialog.setSize(600, 400);
		setupInfoDialog.setLocationRelativeTo(frame);

		JTextArea setupText = new JTextArea("Simulation Setup Parameters\n\n"
				+ "Before you begin, please configure the simulation to match your preferences. You can customize "
				+ "the environment and the capabilities of the robots to suit different scenarios:\n\n" + "Map Size\n"
				+ "Choose the area in which the robots will operate:\n"
				+ "▪ Small – Compact environment with quicker runs.\n"
				+ "▪ Medium – Balanced scale for moderate complexity.\n"
				+ "▪ Large – Wide area with more waste and navigation challenges.\n\n" + "Robot Type\n"
				+ "Select the intelligence level of the robots:\n"
				+ "▪ Beginner – Limited field of vision; good for observing basic behavior.\n"
				+ "▪ Intermediate – Moderate vision range; capable of smarter navigation.\n"
				+ "▪ Advanced – Wide field of vision; efficient at detecting and classifying waste from farther away.\n\n"
				+ "Number of Robots\n"
				+ "Set how many robots will participate in the simulation. More robots can improve coverage and "
				+ "facilitate quicker disposal, but realistically incur more costs.\n\n"
				+ "Adjust these settings to test various waste management strategies and see how different "
				+ "configurations impact the system's performance.");
		setupText.setEditable(false);
		setupText.setLineWrap(true);
		setupText.setWrapStyleWord(true);
		setupText.setMargin(new Insets(20, 20, 20, 20));
		setupText.setFont(new Font("Arial", Font.PLAIN, 14));

		JButton setupButton = new JButton("Configure Simulation");
		setupButton.addActionListener(e -> {
			setupInfoDialog.dispose();
			showConfigurationDialog();
		});

		setupInfoDialog.add(new JScrollPane(setupText), BorderLayout.CENTER);
		setupInfoDialog.add(setupButton, BorderLayout.SOUTH);
		setupInfoDialog.setVisible(true);
	}

	private void showConfigurationDialog() {
		JFrame setupFrame = new JFrame("Simulation Setup");
		setupFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setupFrame.setLayout(new GridLayout(0, 1));

		JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		// Map Size
		JLabel sizeLabel = new JLabel("Map Size:");
		String[] sizes = { "Small", "Medium", "Large" };
		JComboBox<String> sizeCombo = new JComboBox<>(sizes);

		// Robot Skill
		JLabel skillLabel = new JLabel("Robot Skill:");
		String[] skills = { "Beginner (FOV: 1)", "Intermediate (FOV: 2)", "Advanced (FOV: 3)" };
		JComboBox<String> skillCombo = new JComboBox<>(skills);

		// Number of Robots
		JLabel robotLabel = new JLabel("Number of Robots:");
		String[] robotCounts = { "1 robot", "2 robots", "3 robots", "4 robots" };
		JComboBox<String> robotCombo = new JComboBox<>(robotCounts);

		JButton startButton = new JButton("Start Simulation");

		panel.add(sizeLabel);
		panel.add(sizeCombo);
		panel.add(skillLabel);
		panel.add(skillCombo);
		panel.add(robotLabel);
		panel.add(robotCombo);
		panel.add(new JLabel()); // Empty cell
		panel.add(startButton);

		startButton.addActionListener(e -> {
			int mapSize = switch (sizeCombo.getSelectedIndex()) {
			case 0 -> 20;
			case 1 -> 25;
			case 2 -> 29;
			default -> 20;
			};

			int fov = switch (skillCombo.getSelectedIndex()) {
			case 0 -> 1;
			case 1 -> 2;
			case 2 -> 3;
			default -> 1;
			};

			int numRobots = switch (robotCombo.getSelectedIndex()) {
			case 0 -> 1;
			case 1 -> 2;
			case 2 -> 3;
			case 3 -> 4;
			default -> 1;
			};

			setupFrame.dispose();
			// Start the actual simulation with parameters
			new WasteCollectionSimulation(mapSize, numRobots, fov);
		});

		setupFrame.add(panel);
		setupFrame.pack();
		setupFrame.setLocationRelativeTo(null);
		setupFrame.setVisible(true);
	}

	public void showClassificationDialog(Waste waste) {
		waitingForUser = true;
		SwingUtilities.invokeLater(() -> {
			String message = "WASTE CLASSIFICATION\n\n" + "Detected Color: " + getColorName(waste.imageFile) + "\n"
					+ "Classified as: " + waste.type;

			JOptionPane.showMessageDialog(frame, message, "Waste Classification", JOptionPane.INFORMATION_MESSAGE);
			waitingForUser = false;
		});
	}

	public void showWasteObtainedPopup(Waste waste) {
		SwingUtilities.invokeLater(() -> {
			JDialog popup = new JDialog(frame, "Waste Collected", true);
			popup.setLayout(new BorderLayout(10, 10));

			// Main content panel
			JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
			contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			// Waste image
			JPanel imagePanel = new JPanel();
			try {
				BufferedImage img = ImageIO.read(waste.imageFile);
				ImageIcon icon = new ImageIcon(img.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
				imagePanel.add(new JLabel(icon));
			} catch (IOException e) {
				imagePanel.add(new JLabel("Image not available"));
			}

			// Text information
			JPanel textPanel = new JPanel(new GridLayout(0, 1, 5, 5));
			textPanel.add(new JLabel("Waste Obtained!", SwingConstants.CENTER));
			textPanel.add(new JLabel(" ")); // Spacer
			textPanel.add(new JLabel("Classified as: " + waste.type.toString(), SwingConstants.CENTER));
			// textPanel.add(new JLabel("Location: (" + waste.row + ", " + waste.col + ")",
			// SwingConstants.CENTER));

			contentPanel.add(imagePanel, BorderLayout.CENTER);
			contentPanel.add(textPanel, BorderLayout.SOUTH);

			// Continue button
			JButton continueButton = new JButton("Continue Collection");
			continueButton.addActionListener(e -> {
				waitingForUser = false;
				popup.dispose();
			});

			popup.add(contentPanel, BorderLayout.CENTER);
			popup.add(continueButton, BorderLayout.SOUTH);

			popup.pack();
			popup.setLocationRelativeTo(frame);
			popup.setVisible(true);
		});
		waitingForUser = true;
	}

	public void showDisposalPopup(Waste waste, Bin bin) {
		SwingUtilities.invokeLater(() -> {
			JDialog popup = new JDialog(frame, "Waste Disposal", true);
			popup.setLayout(new BorderLayout(10, 10));

			// Main content panel
			JPanel contentPanel = new JPanel();
			contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
			contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			// Title
			JLabel title = new JLabel("Disposing Waste", SwingConstants.CENTER);
			title.setFont(new Font("Arial", Font.BOLD, 16));
			contentPanel.add(title);
			contentPanel.add(Box.createVerticalStrut(10));

			// Images row
			JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

			// Waste image
			JPanel wastePanel = new JPanel(new BorderLayout());
			try {
				BufferedImage wasteImg = ImageIO.read(waste.imageFile);
				wastePanel.add(new JLabel(new ImageIcon(wasteImg.getScaledInstance(120, 120, Image.SCALE_SMOOTH))),
						BorderLayout.CENTER);
				wastePanel.add(new JLabel("Collected Waste", SwingConstants.CENTER), BorderLayout.SOUTH);
			} catch (IOException e) {
				wastePanel.add(new JLabel("Waste image missing"));
			}

			// Arrow icon
			JLabel arrow = new JLabel("→");
			arrow.setFont(new Font("Arial", Font.BOLD, 24));

			// Bin image
			JPanel binPanel = new JPanel(new BorderLayout());
			try {
			    BufferedImage binImg = bin.imageFile != null ? ImageIO.read(bin.imageFile) : null;
			    if (binImg != null) {
			        binPanel.add(new JLabel(new ImageIcon(binImg.getScaledInstance(120, 120, Image.SCALE_SMOOTH))),
			                BorderLayout.CENTER);
			        binPanel.add(new JLabel(bin.type + " Bin", SwingConstants.CENTER), BorderLayout.SOUTH);
			    } else {
			        binPanel.add(new JLabel("Bin image missing"));
			    }
			} catch (IOException e) {
			    binPanel.add(new JLabel("Error loading bin image"));
			}

			imagePanel.add(wastePanel);
			imagePanel.add(arrow);
			imagePanel.add(binPanel);
			contentPanel.add(imagePanel);
			contentPanel.add(Box.createVerticalStrut(10));

			// Disposal result
			String resultMessage = waste.type == bin.type ? "✓ Correct Disposal! ✓" : "✗ Incorrect Disposal! ✗";
			Color resultColor = waste.type == bin.type ? Color.GREEN.darker() : Color.RED.darker();

			JLabel resultLabel = new JLabel(resultMessage, SwingConstants.CENTER);
			resultLabel.setFont(new Font("Arial", Font.BOLD, 14));
			resultLabel.setForeground(resultColor);
			contentPanel.add(resultLabel);

			// Continue button
			JButton continueButton = new JButton("Continue Simulation");
			continueButton.addActionListener(e -> {
				waitingForUser = false;
				popup.dispose();
			});

			popup.add(contentPanel, BorderLayout.CENTER);
			popup.add(continueButton, BorderLayout.SOUTH);

			popup.pack();
			popup.setLocationRelativeTo(frame);
			popup.setVisible(true);
		});
		waitingForUser = true;
	}

	public void showImagePopup(String title, File... imageFiles) {
		SwingUtilities.invokeLater(() -> {
			JDialog popup = new JDialog(frame, title, true);
			popup.setLayout(new FlowLayout());

			for (File imageFile : imageFiles) {
				try {
					BufferedImage img = ImageIO.read(imageFile);
					ImageIcon icon = new ImageIcon(img.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
					popup.add(new JLabel(icon));
				} catch (IOException e) {
					popup.add(new JLabel("Image not found: " + imageFile.getName()));
				}
			}

			JButton closeButton = new JButton("Continue");
			closeButton.addActionListener(e -> {
				waitingForUser = false;
				popup.dispose();
			});
			popup.add(closeButton);

			popup.pack();
			popup.setLocationRelativeTo(frame);
			popup.setVisible(true);
		});
		waitingForUser = true;
	}

	private String getColorName(File imageFile) {
		try {
			BufferedImage image = ImageIO.read(imageFile);
			if (image == null)
				return "Unknown";
			Color color = getDominantColor(image);
			if (isRed(color))
				return "Red (Metal)";
			if (isBlue(color))
				return "Blue (Paper)";
			if (isYellow(color))
				return "Yellow (Plastic)";
			if (isGreen(color))
				return "Green (Glass)";
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

	class SimulationPanel extends JPanel {
		// Color definitions
		private static final Color UNEXPLORED_COLOR = new Color(220, 220, 220); // Light gray
		private static final Color EXPLORED_COLOR = new Color(200, 255, 200, 100); // Semi-transparent green
		private static final Color FOV_COLOR = new Color(200, 200, 255, 100); // Semi-transparent light blue
		private static final Color WALL_COLOR = new Color(100, 100, 100);
		private static final Color ROBOT_COLOR = new Color(128, 0, 128); // Purple
		private static final Color UNIDENTIFIED_WASTE_COLOR = Color.WHITE; // Plain white
		private static final int CELL_SIZE = 20;

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			// Draw the grid
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					int x = c * CELL_SIZE;
					int y = r * CELL_SIZE;

					// Draw cell background based on exploration state
					Graph.GraphNode<GridCell> node = findNode(r, c);
					if (node != null && node.getData().type == WALL) {
						g.setColor(WALL_COLOR);
						g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
					} else {
						boolean isExplored = false;
						for (Robot robot : robots) {
							if (robot.exploredMap[r][c]) {
								isExplored = true;
								break;
							}
						}

						if (!isExplored) {
							g.setColor(UNEXPLORED_COLOR);
							g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
						} else {
							boolean inFOV = false;
							for (Robot robot : robots) {
								if (robot.isInFOV(r, c)) {
									inFOV = true;
									break;
								}
							}

							if (inFOV) {
								g.setColor(FOV_COLOR);
								g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
							} else {
								g.setColor(EXPLORED_COLOR);
								g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
							}
						}
					}

					// Draw grid lines
					g.setColor(Color.LIGHT_GRAY);
					g.drawRect(x, y, CELL_SIZE, CELL_SIZE);

					// Draw entities (always visible)
					switch (displayMap[r][c]) {
					case ROBOT_EMPTY:
						g.setColor(ROBOT_COLOR);
						g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
						break;

					case ROBOT_CARRYING:
						g.setColor(ROBOT_COLOR);
						g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);

						Robot carryingRobot = getRobotAt(r, c);
						if (carryingRobot != null && carryingRobot.carrying != null) {
							g.setColor(getWasteColor(carryingRobot.carrying.type));
							g.fillOval(x + 6, y + 6, CELL_SIZE - 12, CELL_SIZE - 12);
						}
						break;

					case UNIDENTIFIED_WASTE:
						g.setColor(UNIDENTIFIED_WASTE_COLOR);
						g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
						break;

					case PLASTIC_WASTE:
						g.setColor(Color.YELLOW);
						g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
						break;

					case PAPER_WASTE:
						g.setColor(Color.BLUE);
						g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
						break;

					case METAL_WASTE:
						g.setColor(Color.RED);
						g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
						break;

					case GLASS_WASTE:
						g.setColor(Color.GREEN);
						g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
						break;

					case PLASTIC_BIN:
						g.setColor(Color.YELLOW);
						g.fillRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
						break;

					case PAPER_BIN:
						g.setColor(Color.BLUE);
						g.fillRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
						break;

					case METAL_BIN:
						g.setColor(Color.RED);
						g.fillRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
						break;

					case GLASS_BIN:
						g.setColor(Color.GREEN);
						g.fillRect(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
						break;
					}
				}
			}

			drawLegend(g);
		}

		private Robot getRobotAt(int r, int c) {
			for (Robot robot : robots) {
				if (robot.row == r && robot.col == c) {
					return robot;
				}
			}
			return null;
		}

		private Color getWasteColor(WasteType type) {
			return switch (type) {
			case PLASTIC -> Color.YELLOW;
			case PAPER -> Color.BLUE;
			case METAL -> Color.RED;
			case GLASS -> Color.GREEN;
			};
		}

		private void drawLegend(Graphics g) {
			int legendX = cols * CELL_SIZE + 20;
			int legendY = 50;
			int itemHeight = 18;
			int boxSize = 15;
			int legendWidth = 180;
			int legendHeight = 380;

			// Legend box
			g.setColor(Color.gray);
			g.fillRect(legendX - 10, legendY - 10, legendWidth, legendHeight);
			g.setColor(Color.BLACK);
			g.drawRect(legendX - 10, legendY - 10, legendWidth, legendHeight);

			// Title
			g.setColor(Color.WHITE);
			g.drawString("LEGEND", legendX + 60, legendY + 5);

			int currentY = legendY + itemHeight;

			// Exploration states
			g.setColor(Color.WHITE);
			g.drawString("--- EXPLORATION ---", legendX, currentY + 12);
			currentY += itemHeight;

			g.setColor(UNEXPLORED_COLOR);
			g.fillRect(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Unexplored", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			g.setColor(EXPLORED_COLOR);
			g.fillRect(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Explored", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			g.setColor(FOV_COLOR);
			g.fillRect(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Visible (FOV)", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			// Robot
			g.setColor(ROBOT_COLOR);
			g.fillOval(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Robot", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			// Robot carrying waste
			g.setColor(ROBOT_COLOR);
			g.fillOval(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.fillOval(legendX + 3, currentY + 3, boxSize - 6, boxSize - 6);
			g.setColor(Color.WHITE);
			g.drawString("Carrying Waste", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			// Waste types
			g.setColor(Color.WHITE);
			g.drawString("--- WASTE TYPES ---", legendX, currentY + 12);
			currentY += itemHeight;

			// Unidentified waste (plain white)
			g.setColor(UNIDENTIFIED_WASTE_COLOR);
			g.fillOval(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Unidentified", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			g.setColor(Color.YELLOW);
			g.fillOval(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Plastic", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			g.setColor(Color.BLUE);
			g.fillOval(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Paper", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			g.setColor(Color.RED);
			g.fillOval(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Metal", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			g.setColor(Color.GREEN);
			g.fillOval(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Glass", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			// Bins
			g.setColor(Color.WHITE);
			g.drawString("--- BIN TYPES ---", legendX, currentY + 12);
			currentY += itemHeight;

			g.setColor(Color.YELLOW);
			g.fillRect(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Plastic Bin", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			g.setColor(Color.BLUE);
			g.fillRect(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Paper Bin", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			g.setColor(Color.RED);
			g.fillRect(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Metal Bin", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			g.setColor(Color.GREEN);
			g.fillRect(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Glass Bin", legendX + boxSize + 10, currentY + 12);
			currentY += itemHeight;

			// Wall
			g.setColor(WALL_COLOR);
			g.fillRect(legendX, currentY, boxSize, boxSize);
			g.setColor(Color.WHITE);
			g.drawString("Wall", legendX + boxSize + 10, currentY + 12);
		}
	}


}