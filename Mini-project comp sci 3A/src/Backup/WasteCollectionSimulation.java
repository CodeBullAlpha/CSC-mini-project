//2D Array Working Simulation
package Backup;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;

public class WasteCollectionSimulation extends JPanel implements ActionListener {

	static final int CELL_SIZE = 25;
	static final int SMALL = 20, MEDIUM = 25, LARGE = 29;
	static final Color WALL_COLOR = Color.DARK_GRAY;
	static final Color WALKABLE_COLOR = Color.LIGHT_GRAY;
	static final Color GRID_LINE_COLOR = new Color(200, 200, 200);
	static final Color UNIDENTIFIED_WASTE_COLOR = Color.WHITE;
	static final int UNIDENTIFIED_WASTE_SIZE = 15;
	static final int IDENTIFIED_WASTE_SIZE = 15;
	static final int BIN_SIZE = 15;
	static final Color FOV_COLOR = new Color(200, 200, 255, 100);
	static final Color ROBOT_COLOR = new Color(128, 0, 128);
	static final Color EXPLORED_COLOR = new Color(200, 255, 200, 50);

	Timer timer;
	char[][] map;
	java.util.List<Robot> robots = new ArrayList<>();
	java.util.List<Waste> wastes = new ArrayList<>();
	java.util.List<Bin> bins = new ArrayList<>();
	int fieldOfView;
	int rows, cols;
	boolean mapFullyExplored = false;
	boolean isPaused = true;
	boolean waitingForUser = false;
	JButton startPauseButton;
	JPanel simulationPanel;
	JFrame parentFrame;

	static final String WASTE_IMAGE_BASE_PATH = "images/";
	static final String BIN_IMAGE_BASE_PATH = "bin_images/";

	static final Map<WasteType, File[]> wasteImageFiles = new HashMap<>();
	static final Map<WasteType, File[]> binImageFiles = new HashMap<>();
	static final double CLASSIFICATION_ERROR_PROBABILITY = 0; // 10% chance of misclassification

	static {

		File plasticFolder = new File(WASTE_IMAGE_BASE_PATH + "plastic");
		File paperFolder = new File(WASTE_IMAGE_BASE_PATH + "paper");
		File metalFolder = new File(WASTE_IMAGE_BASE_PATH + "metal");
		File glassFolder = new File(WASTE_IMAGE_BASE_PATH + "glass");

		wasteImageFiles.put(WasteType.PLASTIC, plasticFolder.listFiles());
		wasteImageFiles.put(WasteType.PAPER, paperFolder.listFiles());
		wasteImageFiles.put(WasteType.METAL, metalFolder.listFiles());
		wasteImageFiles.put(WasteType.GLASS, glassFolder.listFiles());

		File binPlasticFolder = new File(BIN_IMAGE_BASE_PATH + "plastic");
		File binPaperFolder = new File(BIN_IMAGE_BASE_PATH + "paper");
		File binMetalFolder = new File(BIN_IMAGE_BASE_PATH + "metal");
		File binGlassFolder = new File(BIN_IMAGE_BASE_PATH + "glass");

		binImageFiles.put(WasteType.PLASTIC, binPlasticFolder.listFiles());
		binImageFiles.put(WasteType.PAPER, binPaperFolder.listFiles());
		binImageFiles.put(WasteType.METAL, binMetalFolder.listFiles());
		binImageFiles.put(WasteType.GLASS, binGlassFolder.listFiles());
	}

	public WasteCollectionSimulation(int size, int numRobots, int fieldOfView, JFrame frame) {
		this.rows = size;
		this.cols = size;
		this.fieldOfView = fieldOfView;
		this.parentFrame = frame;

		JPanel mainPanel = new JPanel(new BorderLayout());

		JPanel controlPanel = new JPanel();
		startPauseButton = new JButton("Start Simulation");
		startPauseButton.addActionListener(e -> toggleSimulation());
		controlPanel.add(startPauseButton);

		simulationPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				paintSimulation(g);
			}

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(cols * CELL_SIZE + 220, rows * CELL_SIZE);
			}
		};

		mainPanel.add(controlPanel, BorderLayout.NORTH);
		mainPanel.add(simulationPanel, BorderLayout.CENTER);

		// Create a scroll pane for the main panel
		JScrollPane scrollPane = new JScrollPane(mainPanel);
		scrollPane.setPreferredSize(
				new Dimension(Math.min(cols * CELL_SIZE + 250, Toolkit.getDefaultToolkit().getScreenSize().width - 50),
						Math.min(rows * CELL_SIZE + 100, Toolkit.getDefaultToolkit().getScreenSize().height - 100)));

		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);

		generateMap();
		spawnRobots(numRobots);
		timer = new Timer(300, this);

		saveSimulationAsImage();
		saveMapAsImage();
	}

	private void paintSimulation(Graphics g) {

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (map[r][c] == '#') {
					g.setColor(WALL_COLOR);
					g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				} else {
					g.setColor(WALKABLE_COLOR);
					g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				}
				g.setColor(GRID_LINE_COLOR);
				g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
			}
		}

		for (Robot robot : robots) {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (robot.exploredMap[r][c]) {
						g.setColor(EXPLORED_COLOR);
						g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
					}
				}
			}
		}

		for (Robot robot : robots)
			robot.drawFieldOfView(g);
		for (Waste w : wastes) {
			if (map[w.row][w.col] == '?') {
				g.setColor(UNIDENTIFIED_WASTE_COLOR);
				g.fillOval(w.col * CELL_SIZE + (CELL_SIZE - UNIDENTIFIED_WASTE_SIZE) / 2,
						w.row * CELL_SIZE + (CELL_SIZE - UNIDENTIFIED_WASTE_SIZE) / 2, UNIDENTIFIED_WASTE_SIZE,
						UNIDENTIFIED_WASTE_SIZE);
			} else {
				w.draw(g);
			}
		}
		for (Bin bin : bins)
			bin.draw(g);
		for (Robot r : robots)
			r.draw(g);

		drawLegend(g);
	}

	private void toggleSimulation() {
		if (waitingForUser) {
			return;
		}

		if (isPaused) {
			timer.start();
			startPauseButton.setText("Pause Simulation");
			isPaused = false;
		} else {
			timer.stop();
			startPauseButton.setText("Resume Simulation");
			isPaused = true;
		}
	}

	public void saveMapAsImage() {
		BufferedImage image = new BufferedImage(cols * CELL_SIZE, rows * CELL_SIZE, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (map[r][c] == '#') {
					g.setColor(WALL_COLOR);
					g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				} else {
					g.setColor(WALKABLE_COLOR);
					g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				}
				g.setColor(GRID_LINE_COLOR);
				g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
			}
		}

		try {
			ImageIO.write(image, "png", new File("map_only.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		g.dispose();
	}

	public void generateMap() {
		map = new char[rows][cols];
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				map[r][c] = ' ';
			}
		}

		for (int i = 0; i < rows; i++) {
			map[i][0] = map[i][cols - 1] = '#';
		}
		for (int i = 0; i < cols; i++) {
			map[0][i] = map[rows - 1][i] = '#';
		}

		Random rand = new Random();

		for (int i = 0; i < rows * cols / 6; i++) {
			int r = rand.nextInt(rows);
			int c = rand.nextInt(cols);
			if (map[r][c] == ' ')
				map[r][c] = '#';
		}

		WasteType[] types = WasteType.values();
		for (WasteType type : types) {
			for (int i = 0; i < 2; i++) {
				int r, c;
				do {
					r = rand.nextInt(rows);
					c = rand.nextInt(cols);
				} while (map[r][c] != ' ');
				map[r][c] = 'B';
				bins.add(new Bin(r, c, type));
			}
		}

		for (int i = 0; i < 10; i++) {
			int r, c;
			do {
				r = rand.nextInt(rows);
				c = rand.nextInt(cols);
			} while (map[r][c] != ' ');
			map[r][c] = '?';

			WasteType randomType = WasteType.values()[rand.nextInt(WasteType.values().length)];
			File[] images = wasteImageFiles.get(randomType);
			File imageFile = images[rand.nextInt(images.length)];
			wastes.add(new Waste(r, c, randomType, imageFile));
		}
	}

	public void spawnRobots(int count) {
		Random rand = new Random();
		for (int i = 0; i < count; i++) {
			int r, c;
			do {
				r = rand.nextInt(rows);
				c = rand.nextInt(cols);
			} while (map[r][c] != ' ');
			robots.add(new Robot(r, c));
		}
	}

	public void saveSimulationAsImage() {
		BufferedImage image = new BufferedImage(cols * CELL_SIZE, rows * CELL_SIZE, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = image.createGraphics();

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (map[r][c] == '#') {
					g.setColor(WALL_COLOR);
					g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				} else {
					g.setColor(WALKABLE_COLOR);
					g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				}
				g.setColor(GRID_LINE_COLOR);
				g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
			}
		}

		try {
			ImageIO.write(image, "png", new File("map_only.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		g.dispose();
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (map[r][c] == '#') {
					g.setColor(WALL_COLOR);
					g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				} else {
					g.setColor(WALKABLE_COLOR);
					g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
				}
				g.setColor(GRID_LINE_COLOR);
				g.drawRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
			}
		}

		for (Robot robot : robots) {
			for (int r = 0; r < rows; r++) {
				for (int c = 0; c < cols; c++) {
					if (robot.exploredMap[r][c]) {
						g.setColor(EXPLORED_COLOR);
						g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
					}
				}
			}
		}

		for (Robot robot : robots)
			robot.drawFieldOfView(g);
		for (Waste w : wastes) {
			if (map[w.row][w.col] == '?') {
				g.setColor(UNIDENTIFIED_WASTE_COLOR);
				g.fillOval(w.col * CELL_SIZE + (CELL_SIZE - UNIDENTIFIED_WASTE_SIZE) / 2,
						w.row * CELL_SIZE + (CELL_SIZE - UNIDENTIFIED_WASTE_SIZE) / 2, UNIDENTIFIED_WASTE_SIZE,
						UNIDENTIFIED_WASTE_SIZE);
			} else {
				w.draw(g);
			}
		}
		for (Bin bin : bins)
			bin.draw(g);
		for (Robot r : robots)
			r.draw(g);

		if (mapFullyExplored) {
			g.setColor(Color.BLACK);
			g.drawString("MAP FULLY EXPLORED!", cols * CELL_SIZE / 2 - 50, 20);
		}

		drawLegend(g);
	}

//-----------------------------------------------------------------------------------------------------------------------------
	private void drawLegend(Graphics g) {
		int legendX = cols * CELL_SIZE + 20;
		int legendY = 50;
		int itemHeight = 20;
		int boxSize = 15;
		int legendWidth = 180;
		int legendHeight = 350;

		// Legend box - gray background
		g.setColor(Color.gray);
		g.fillRect(legendX - 10, legendY - 10, legendWidth, legendHeight);
		g.setColor(Color.BLACK);
		g.drawRect(legendX - 10, legendY - 10, legendWidth, legendHeight);

		// Title in white
		g.setColor(Color.WHITE);
		g.drawString("LEGEND", legendX + 60, legendY + 5);

		int currentY = legendY + itemHeight;

		// Robot
		g.setColor(ROBOT_COLOR);
		g.fillOval(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Robot", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Waste header
		g.setColor(Color.WHITE);
		g.drawString("--- WASTE TYPES ---", legendX, currentY + 12);
		currentY += itemHeight;

		// Plastic Waste
		g.setColor(Color.YELLOW);
		g.fillOval(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Plastic Waste", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Paper Waste
		g.setColor(Color.BLUE);
		g.fillOval(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Paper Waste", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Metal Waste
		g.setColor(Color.RED);
		g.fillOval(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Metal Waste", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Glass Waste
		g.setColor(Color.GREEN);
		g.fillOval(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Glass Waste", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Unidentified waste
		g.setColor(UNIDENTIFIED_WASTE_COLOR);
		g.fillOval(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Unidentified Waste", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Bins header
		g.setColor(Color.WHITE);
		g.drawString("--- BIN TYPES ---", legendX, currentY + 12);
		currentY += itemHeight;

		// Plastic Bin
		g.setColor(Color.YELLOW);
		g.fillRect(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Plastic Bin", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Paper Bin
		g.setColor(Color.BLUE);
		g.fillRect(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Paper Bin", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Metal Bin
		g.setColor(Color.RED);
		g.fillRect(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Metal Bin", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Glass Bin
		g.setColor(Color.GREEN);
		g.fillRect(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Glass Bin", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Other elements
		g.setColor(Color.WHITE);
		g.drawString("--- OTHER ---", legendX, currentY + 12);
		currentY += itemHeight;

		// Field of View
		g.setColor(FOV_COLOR);
		g.fillRect(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Robot FOV", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Explored area
		g.setColor(EXPLORED_COLOR);
		g.fillRect(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Explored Area", legendX + boxSize + 10, currentY + 12);
		currentY += itemHeight;

		// Wall
		g.setColor(WALL_COLOR);
		g.fillRect(legendX, currentY, boxSize, boxSize);
		g.setColor(Color.WHITE);
		g.drawString("Wall", legendX + boxSize + 10, currentY + 12);
	}

//--------------------------------------------------------------------------------------------------------------------------------
	class Robot {
		int row, col;
		Waste carrying = null;
		Bin targetBin = null;
		java.util.List<Point> path = new ArrayList<>();
		java.util.List<Point> knownWastes = new ArrayList<>();
		java.util.List<Bin> knownBins = new ArrayList<>();
		boolean[][] exploredMap;
		Point explorationTarget;

		public Robot(int r, int c) {
			row = r;
			col = c;
			exploredMap = new boolean[rows][cols];
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
					}
				}
			}
		}

		private WasteType classifyWaste(File imageFile) {

			Random rand = new Random();
			if (rand.nextDouble() < CLASSIFICATION_ERROR_PROBABILITY) {

				WasteType[] types = WasteType.values();
				WasteType correctType = getTypeFromImageFile(imageFile);
				WasteType wrongType;
				do {
					wrongType = types[rand.nextInt(types.length)];
				} while (wrongType == correctType);
				return wrongType;
			} else {

				return getTypeFromImageFile(imageFile);
			}
		}

		private WasteType getTypeFromImageFile(File imageFile) {
			String path = imageFile.getPath().toLowerCase();
			if (path.contains("plastic"))
				return WasteType.PLASTIC;
			if (path.contains("paper"))
				return WasteType.PAPER;
			if (path.contains("metal"))
				return WasteType.METAL;
			if (path.contains("glass"))
				return WasteType.GLASS;
			return WasteType.PLASTIC;
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

				if (map[x0][y0] == '#') {
					return false;
				}
				if (x0 == x1 && y0 == y1) {
					return true;
				}

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

				for (int[] dir : new int[][] { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } }) {
					int nr = current.x + dir[0];
					int nc = current.y + dir[1];
					if (nr >= 0 && nc >= 0 && nr < rows && nc < cols && map[nr][nc] != '#' && !visited[nr][nc]) {
						Point neighbor = new Point(nr, nc);
						queue.add(neighbor);
						visited[nr][nc] = true;
						parent.put(neighbor, current);
					}
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

		private java.util.List<Point> findPathToBin(Bin bin) {
			return findPath(new Point(row, col), new Point(bin.row, bin.col));
		}

		private java.util.List<Point> findPath(Point start, Point goal) {
			Queue<Point> queue = new LinkedList<>();
			Map<Point, Point> parent = new HashMap<>();

			queue.add(start);
			parent.put(start, null);

			while (!queue.isEmpty()) {
				Point current = queue.poll();
				if (current.equals(goal))
					break;

				for (int[] dir : new int[][] { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } }) {
					int nr = current.x + dir[0];
					int nc = current.y + dir[1];
					Point neighbor = new Point(nr, nc);

					if (nr >= 0 && nc >= 0 && nr < rows && nc < cols && map[nr][nc] != '#'
							&& !parent.containsKey(neighbor)) {
						parent.put(neighbor, current);
						queue.add(neighbor);
					}
				}
			}

			java.util.List<Point> path = new ArrayList<>();
			Point current = goal;
			while (current != null && !current.equals(start)) {
				path.add(0, current);
				current = parent.get(current);
			}
			return path;
		}

		private void randomMove() {
			int[][] directions = { { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 } };
			Collections.shuffle(Arrays.asList(directions));

			for (int[] dir : directions) {
				int newRow = row + dir[0];
				int newCol = col + dir[1];
				if (newRow >= 0 && newRow < rows && newCol >= 0 && newCol < cols && map[newRow][newCol] != '#') {
					row = newRow;
					col = newCol;
					break;
				}
			}
		}

		public void drawFieldOfView(Graphics g) {
			g.setColor(FOV_COLOR);
			int minRow = Math.max(0, row - fieldOfView);
			int maxRow = Math.min(rows - 1, row + fieldOfView);
			int minCol = Math.max(0, col - fieldOfView);
			int maxCol = Math.min(cols - 1, col + fieldOfView);

			for (int r = minRow; r <= maxRow; r++) {
				for (int c = minCol; c <= maxCol; c++) {
					if (hasLineOfSight(row, col, r, c)) {
						g.fillRect(c * CELL_SIZE, r * CELL_SIZE, CELL_SIZE, CELL_SIZE);
					}
				}
			}
		}

		public void draw(Graphics g) {
			g.setColor(ROBOT_COLOR);
			g.fillOval(col * CELL_SIZE + 4, row * CELL_SIZE + 4, 16, 16);

			if (carrying != null) {
				g.setColor(switch (carrying.type) {
				case PLASTIC -> Color.YELLOW;
				case PAPER -> Color.BLUE;
				case METAL -> Color.RED;
				case GLASS -> Color.GREEN;
				});
				g.fillOval(col * CELL_SIZE + 8, row * CELL_SIZE, 8, 8);
			}
		}

		public void act() {
			if (waitingForUser)
				return;

			exploreCurrentPosition();

			for (Waste waste : wastes) {
				if (hasLineOfSight(row, col, waste.row, waste.col)) {
					waste.identified = true;
					map[waste.row][waste.col] = 'W';
					Point wasteLoc = new Point(waste.row, waste.col);
					if (!knownWastes.contains(wasteLoc)) {
						knownWastes.add(wasteLoc);
					}
				}
			}

			for (Bin bin : bins) {
				if (hasLineOfSight(row, col, bin.row, bin.col)) {
					if (!knownBins.contains(bin)) {
						knownBins.add(bin);
					}
				}
			}

			if (carrying == null) {

				for (Waste waste : wastes) {
					if (waste.row == row && waste.col == col) {
						carrying = waste;
						wastes.remove(waste);
						map[row][col] = ' ';
						knownWastes.remove(new Point(row, col));

						WasteType classifiedType = classifyWaste(waste.imageFile);
						carrying.type = classifiedType;

						showClassificationDialog(waste.imageFile, classifiedType);
						return;
					}
				}

				if (carrying == null && !knownWastes.isEmpty()) {
					Point nearestWaste = findNearestWaste();
					if (nearestWaste != null) {
						path = findPath(new Point(row, col), nearestWaste);
						if (!path.isEmpty()) {
							Point next = path.get(0);
							if (map[next.x][next.y] != '#') {
								row = next.x;
								col = next.y;
								path.remove(0);
							}
						}
					}
				}

				if (carrying == null && knownWastes.isEmpty()) {
					if (explorationTarget == null || (row == explorationTarget.x && col == explorationTarget.y)) {
						explorationTarget = findNearestUnexplored();
					}

					if (explorationTarget != null) {
						path = findPath(new Point(row, col), explorationTarget);
						if (!path.isEmpty()) {
							Point next = path.get(0);
							if (map[next.x][next.y] != '#') {
								row = next.x;
								col = next.y;
								path.remove(0);
							}
						}
					} else {
						randomMove();
					}
				}
			} else {

				if (targetBin != null) {
					if (path.isEmpty()) {
						path = findPathToBin(targetBin);
					}

					if (!path.isEmpty()) {
						Point next = path.get(0);
						if (map[next.x][next.y] != '#') {
							row = next.x;
							col = next.y;
							path.remove(0);
						}
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
		}

		private void showClassificationDialog(File imageFile, WasteType classifiedType) {
			waitingForUser = true;
			timer.stop();

			JDialog dialog = new JDialog(parentFrame, "Waste Classification", true);
			dialog.setLayout(new BorderLayout());

			try {
				BufferedImage wasteImage = ImageIO.read(imageFile);
				ImageIcon icon = new ImageIcon(wasteImage.getScaledInstance(100, 100, Image.SCALE_SMOOTH));
				JLabel imageLabel = new JLabel(icon);
				dialog.add(imageLabel, BorderLayout.CENTER);
			} catch (IOException e) {
				e.printStackTrace();
				JLabel errorLabel = new JLabel("Could not load waste image", JLabel.CENTER);
				dialog.add(errorLabel, BorderLayout.CENTER);
			}

			String typeName = classifiedType.toString().charAt(0)
					+ classifiedType.toString().substring(1).toLowerCase();
			JLabel resultLabel = new JLabel(typeName + " waste acquired. Preparing for disposal.", JLabel.CENTER);
			dialog.add(resultLabel, BorderLayout.NORTH);

			JButton continueButton = new JButton("Continue");
			continueButton.addActionListener(e -> {
				dialog.dispose();
				waitingForUser = false;
				if (!isPaused) {
					timer.start();
				}
			});

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(continueButton);
			dialog.add(buttonPanel, BorderLayout.SOUTH);

			dialog.pack();
			dialog.setLocationRelativeTo(parentFrame);
			dialog.setVisible(true);
		}

		private void showDisposalDialog(Waste waste, Bin bin) {
			waitingForUser = true;
			timer.stop();

			JDialog dialog = new JDialog(parentFrame, "Waste Disposal", true);
			dialog.setLayout(new BorderLayout());

			JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));

			try {

				BufferedImage wasteImage = ImageIO.read(waste.imageFile);
				ImageIcon wasteIcon = new ImageIcon(wasteImage.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
				JLabel wasteLabel = new JLabel(wasteIcon);
				wasteLabel.setHorizontalAlignment(JLabel.CENTER);
				imagePanel.add(wasteLabel);

				BufferedImage binImage = ImageIO.read(bin.imageFile);
				ImageIcon binIcon = new ImageIcon(binImage.getScaledInstance(150, 150, Image.SCALE_SMOOTH));
				JLabel binLabel = new JLabel(binIcon);
				binLabel.setHorizontalAlignment(JLabel.CENTER);
				imagePanel.add(binLabel);
			} catch (IOException e) {
				e.printStackTrace();
				JLabel errorLabel = new JLabel("Could not load images", JLabel.CENTER);
				imagePanel.add(errorLabel);
			}

			dialog.add(imagePanel, BorderLayout.CENTER);

			String typeName = waste.type.toString().charAt(0) + waste.type.toString().substring(1).toLowerCase();
			JLabel messageLabel = new JLabel("Disposing " + typeName + " waste in " + typeName + " bin", JLabel.CENTER);
			dialog.add(messageLabel, BorderLayout.NORTH);

			JButton continueButton = new JButton("Continue");
			continueButton.addActionListener(e -> {
				dialog.dispose();
				waitingForUser = false;
				if (!isPaused) {
					timer.start();
				}
			});

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(continueButton);
			dialog.add(buttonPanel, BorderLayout.SOUTH);

			dialog.pack();
			dialog.setLocationRelativeTo(parentFrame);
			dialog.setVisible(true);
		}

	}

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

		public String getImagePath() {
			return imageFile.getPath();
		}

		public void draw(Graphics g) {
			if (!identified)
				return;
			g.setColor(switch (type) {
			case PLASTIC -> Color.YELLOW;
			case PAPER -> Color.BLUE;
			case METAL -> Color.RED;
			case GLASS -> Color.GREEN;
			});
			g.fillOval(col * CELL_SIZE + (CELL_SIZE - IDENTIFIED_WASTE_SIZE) / 2,
					row * CELL_SIZE + (CELL_SIZE - IDENTIFIED_WASTE_SIZE) / 2, IDENTIFIED_WASTE_SIZE,
					IDENTIFIED_WASTE_SIZE);
		}
	}

	class Bin {
		int row, col;
		WasteType type;
		File imageFile;

		public Bin(int r, int c, WasteType t) {
			row = r;
			col = c;
			type = t;

			File[] images = binImageFiles.get(type);
			if (images != null && images.length > 0) {
				imageFile = images[new Random().nextInt(images.length)];
			}
		}

		public void draw(Graphics g) {
			g.setColor(switch (type) {
			case PLASTIC -> Color.YELLOW;
			case PAPER -> Color.BLUE;
			case METAL -> Color.RED;
			case GLASS -> Color.GREEN;
			});
			g.fillRect(col * CELL_SIZE + (CELL_SIZE - BIN_SIZE) / 2, row * CELL_SIZE + (CELL_SIZE - BIN_SIZE) / 2,
					BIN_SIZE, BIN_SIZE);
		}
	}

	private boolean isMapFullyExplored() {
		for (int r = 0; r < rows; r++) {
			for (int c = 0; c < cols; c++) {
				if (map[r][c] != '#') {
					boolean exploredByAny = false;
					for (Robot robot : robots) {
						if (robot.exploredMap[r][c]) {
							exploredByAny = true;
							break;
						}
					}
					if (!exploredByAny)
						return false;
				}
			}
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (waitingForUser)
			return;

		if (!mapFullyExplored) {
			mapFullyExplored = isMapFullyExplored();
			if (mapFullyExplored) {
				timer.stop();
				startPauseButton.setEnabled(false);

				JOptionPane.showMessageDialog(this, "The entire map has been fully explored!", "Exploration Complete",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
		if (!isPaused) {
			for (Robot robot : robots)
				robot.act();
			repaint();
		}
	}

	public static void main(String[] args) {
		String[] sizes = { "Small", "Medium", "Large" };
		int sizeChoice = JOptionPane.showOptionDialog(null, "Choose map size:", "Map Size", JOptionPane.DEFAULT_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, sizes, sizes[0]);
		int mapSize = switch (sizeChoice) {
		case 0 -> SMALL;
		case 1 -> MEDIUM;
		case 2 -> LARGE;
		default -> SMALL;
		};

		String[] skills = { "Beginner", "Intermediate", "Advanced" };
		int skillChoice = JOptionPane.showOptionDialog(null, "Choose robot skill:", "Robot Skill",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, skills, skills[0]);
		int fov = switch (skillChoice) {
		case 0 -> 1;
		case 1 -> 2;
		case 2 -> 3;
		default -> 1;
		};

		String[] robotCounts = { "1", "2", "3", "4" };
		int robotChoice = JOptionPane.showOptionDialog(null, "Number of robots:", "Robot Count",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, robotCounts, robotCounts[0]);
		int numRobots = switch (robotChoice) {
		case 0 -> 1;
		case 1 -> 2;
		case 2 -> 3;
		case 3 -> 4;
		default -> 1;
		};

		JFrame frame = new JFrame("Waste Sorting Simulation");
		WasteCollectionSimulation sim = new WasteCollectionSimulation(mapSize, numRobots, fov, frame);
		frame.setContentPane(sim);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);

		JOptionPane.showMessageDialog(frame,
				"Click 'Start Simulation' to begin the waste collection process.\n"
						+ "You can pause and resume the simulation at any time.",
				"Simulation Ready", JOptionPane.INFORMATION_MESSAGE);
	}
}