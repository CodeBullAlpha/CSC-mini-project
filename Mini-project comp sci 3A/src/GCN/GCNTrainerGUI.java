package GCN;


import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import WGraph.*;

public class GCNTrainerGUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//GUI components
	private JButton loadButton;
    private JButton trainButton;
    private JTextArea logArea;
    private JButton resumeButton;
    private JButton saveButton;
    private JButton increaseLRButton ;
    private JButton decreaseLRButton;
    private JPanel previewPanel; // Panel to display graph representation (thumbnail)
    private JFileChooser folderChooser;
    private File selectedFolder;
    //GCN variables 
    private GCNTrainer trainer;
    private GCNModel model;  // keep a reference for test-time prediction
   
    List<BufferedImage> originalImages = new ArrayList<>();
    // experimentation variables 
	private static final int batchsize=25;
    private int totalEpochs =100000000;
    private static  double LearningRate=0.0001;
    private List<GCNGraph> graphsList= new ArrayList<GCNGraph>();
    List<Integer> originalLabels = new ArrayList<>();
    public static final String testFolder="./test-data";
    private static final String[] classNames= {"metal", "paper", "plastic", "brown-glass"};


    @SuppressWarnings("serial")
	public GCNTrainerGUI() {
        // Create a GCNModel (make sure its constructor accepts feature, hidden, and output sizes)
    	model = new GCNModel(24,16, 4);  // 24 input features, 32 hidden units, 4 output classes

        this.trainer = new GCNTrainer(model,LearningRate);  // learning rate(adjust as needed)

        setTitle("GCN Trainer - THEOracle AI");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top panel with load and train buttons
        JPanel topPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save Session");
        resumeButton = new JButton("Resume Session");
        decreaseLRButton = new JButton("--Decrease LR");
        increaseLRButton=new JButton("++Increase LR");
        loadButton = new JButton("Load Training Folder");
        trainButton = new JButton("Train GCN Model");
        trainButton.setEnabled(false);
        topPanel.add(loadButton);
        topPanel.add(trainButton);
        topPanel.add(increaseLRButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(resumeButton);
        buttonPanel.add(decreaseLRButton);
        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel,BorderLayout.SOUTH);
        // Center panel for log area and preview panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setPreferredSize(new Dimension(400, 600));
        centerPanel.add(logScrollPane, BorderLayout.CENTER);
        
        // Preview panel to display a sample image (graph representation)
        previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // If an image is available, it should have been set as an icon on this panel.
                // (We update the preview panel later with setPreviewImage)
            }
        };
        previewPanel.setPreferredSize(new Dimension(400, 600));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Graph Representation"));
        centerPanel.add(previewPanel, BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        

        
        loadButton.addActionListener(e -> selectFolder());
        trainButton.addActionListener(e -> {
        	    try {
					startTraining();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        });
        saveButton.addActionListener(e -> {
            trainer.requestStop();
            log("Session saved successfully! Training stopped.");
        });

        resumeButton.addActionListener(e -> {
        	try {
				resumeTrainingFromFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        });
        


        // Add action listeners
        increaseLRButton.addActionListener(e -> {
        	LearningRate=trainer.getLearningRate();
        	LearningRate *= 2; // Increase by 200%
            trainer.setLearningRate(LearningRate); // Update trainer
        });

        decreaseLRButton.addActionListener(e -> {
        	LearningRate=trainer.getLearningRate();
        	LearningRate *= 0.9; // Decrease by 10%
            trainer.setLearningRate(LearningRate); // Update trainer
          
        });

    }
    

    
    private void resumeTrainingFromFile() throws IOException {
        // First, choose the model file
        JFileChooser modelChooser = new JFileChooser();
        int modelOption = modelChooser.showOpenDialog(this);
        if (modelOption == JFileChooser.APPROVE_OPTION) {
            File selectedModelFile = modelChooser.getSelectedFile();

            // Resume session with the model file
            boolean resumed = GCNMemory.resumeSession(selectedModelFile.getAbsolutePath(), model, trainer);

            if (resumed) {
                // Now, choose the graph file
                JFileChooser graphChooser = new JFileChooser();
                int graphOption = graphChooser.showOpenDialog(this);
                if (graphOption == JFileChooser.APPROVE_OPTION) {
                    File selectedGraphFile = graphChooser.getSelectedFile();
                    
                    // Load the graph from the chosen file
                    graphsList=GCNMemory.loadGraphList(selectedGraphFile.getAbsolutePath());
                    List<GCNGraph> validationGraphs = loadValidationData();
                    trainer.setStopRequested(false);
                    // Start the training process
                    
                    //If they are the same then set current=0 then use the old loaded weights 
                    if(trainer.getCurrentEpoch()==totalEpochs)
                    {
                    	trainer.setCurrentEpoch(0);
                    }
                    new Thread(() -> {
                    	trainer.train(graphsList, validationGraphs, totalEpochs, batchsize);
                        log("Training complete!");
                        // After training, test the model on the "test-data" folder.
                        displayRandomTrainingImage();
                        testModel();
                    }).start();

                    JOptionPane.showMessageDialog(this, "Training session resumed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to load graph file.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to resume training session.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to select model file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }



    private void selectFolder() {
        int option = folderChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            selectedFolder = folderChooser.getSelectedFile();
            log("Selected folder: " + selectedFolder.getAbsolutePath());
            trainButton.setEnabled(true);
        }
    }

    public void startTraining() throws IOException {
        List<GCNGraph> trainingGraphs = loadTrainingData();
        List<GCNGraph> validationGraphs = loadValidationData();

        if (trainingGraphs.isEmpty()) {
            log("No valid training data!");
            return;
        }

        // Start the training process on a separate thread
        new Thread(() -> {
            trainer.train(trainingGraphs, validationGraphs, totalEpochs, batchsize);
            log("Training complete—" + trainingGraphs.size() + " graphs.");
            displayRandomTrainingImage();
            testModel();
        }).start();
    }
    
    private List<GCNGraph> loadTrainingData() throws IOException {
        List<GCNGraph> graphsList = new ArrayList<>();
        final int maxPerClass = 50;

        for (int cls = 0; cls < classNames.length; cls++) {
            File subFolder = new File(selectedFolder, classNames[cls]);
            if (!subFolder.isDirectory()) {
                log("Folder not found: " + classNames[cls]);
                continue;
            }

            File[] files = subFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
            if (files == null || files.length == 0) {
                log("No images in: " + classNames[cls]);
                continue;
            }

            int taken = 0;
            for (File file : files) {
              
                // Assuming ImageLoader is a utility that extracts the graph from the image
                GCNGraph graph = ImageLoader.extractGraphUsingSLICF2(file, classNames.length);
                if (graph != null) {
                    double[] oneHot = new double[classNames.length];
                    oneHot[cls] = 1; // Set the class as 1 in one-hot encoding

                    // Set labels to nodes in the graph
                    for (int i = 0; i < graph.getNodeCount(); i++) {
                        graph.setNodeLabel(i, oneHot);
                    }

                    graphsList.add(graph);
                    taken++;
                    if (taken == maxPerClass) break;  // Limit per class
                }
            }
        }
        return graphsList;
    }

    private List<GCNGraph> loadValidationData() throws IOException {
        List<GCNGraph> validationGraphs = new ArrayList<>();
        
        
        // Assuming all images are in one folder
        File validationFolder = new File(testFolder); // `testFolder` is the path to the folder with images

        // Load all image files in the validation folder
        File[] files = validationFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg"));
        
        if (files == null || files.length == 0) {
            log("No images found in test folder.");
            return validationGraphs;
        }

        // Sort files to process in numerical order, assuming files are named consistently
        Arrays.sort(files);

        // Go through each file and assign class based on image number
        for (File file : files) {            
            // Extract the graph from the image
            GCNGraph graph = ImageLoader.extractGraphUsingSLICF2(file, classNames.length);
            
            if (graph != null) {
                // Determine the class based on image number
                int imageNumber = getImageNumber(file.getName()); // Extract image number from the filename
                
                int classIdx = getClassIndexForImage(imageNumber);

                // Create a one-hot encoded vector for the class
                double[] oneHot = new double[classNames.length];
                oneHot[classIdx] = 1;

                // Set the label for each node
                for (int i = 0; i < graph.getNodeCount(); i++) {
                    graph.setNodeLabel(i, oneHot);
                }

                // Add graph to the validation list
                validationGraphs.add(graph);
            }
        }

        return validationGraphs;
    }

    // Helper method to extract image number from the filename
    private int getImageNumber(String fileName) {
        // Assuming file names end with something like "metal1.png", "paper13.jpg"
        String numberPart = fileName.replaceAll("\\D+", ""); // Extract digits from filename
        return Integer.parseInt(numberPart);
    }

    // Helper method to get class index based on image number
    private int getClassIndexForImage(int imageNumber) {
        if (imageNumber >= 1 && imageNumber <= 12) {
            return 0; // metal
        } else if (imageNumber >= 13 && imageNumber <= 24) {
            return 1; // paper
        } else if (imageNumber >= 25 && imageNumber <= 36) {
            return 2; // plastic
        } else if (imageNumber >= 37 && imageNumber <= 48) {
            return 3; // brown-glass
        }
        return -1; // Unknown class (shouldn't happen)
    }





    
    private void displayRandomTrainingImage() {
        if (originalImages.isEmpty()) {
            System.err.println("No original images to display.");
            return;
        }

        int randomIndex = new Random().nextInt(originalImages.size());
        BufferedImage original = originalImages.get(randomIndex);

        // Resize image (same size used during training)
        Image tmp = original.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(tmp, 0, 0, null);
        g.dispose();

        // Create overlay as just a copy of the resized image
        BufferedImage overlay = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D og = overlay.createGraphics();
        og.drawImage(resized, 0, 0, null);
        og.dispose();

        Image scaled = overlay.getScaledInstance(previewPanel.getWidth() - 20, previewPanel.getHeight() - 20, Image.SCALE_SMOOTH);
        ImageIcon icon = new ImageIcon(scaled);

        int labelIndex = (originalLabels != null && randomIndex < originalLabels.size()) ? originalLabels.get(randomIndex) : -1;
        String labelStr = (labelIndex >= 0 && labelIndex < classNames.length) ? classNames[labelIndex] : "Unknown";

        JLabel imageLabel = new JLabel(labelStr, icon, JLabel.CENTER);
        imageLabel.setVerticalTextPosition(JLabel.BOTTOM);
        imageLabel.setHorizontalTextPosition(JLabel.CENTER);

        previewPanel.removeAll();
        previewPanel.add(imageLabel);
        previewPanel.revalidate();
        previewPanel.repaint();
    }






    /**
     * Tests the model on all images in the "test-data" folder.
     * Logs predictions, compares to ground truth, and moves correctly classified images
     * into class-specific folders inside "waste images".
     */
    public void testModel() {
        File testFolder = new File(selectedFolder, "test-data");
        if (!testFolder.exists() || !testFolder.isDirectory()) {
            log("No 'test-data' folder found!");
            return;
        }

        File[] files = testFolder.listFiles((d, name) ->
            name.toLowerCase().endsWith(".png") ||
            name.toLowerCase().endsWith(".jpg")
        );
        if (files == null || files.length == 0) {
            log("No image files found in 'test-data' folder.");
            return;
        }

        log("Testing on " + files.length + " images from: " + testFolder.getAbsolutePath());

        int numClasses = classNames.length;
        int correct = 0, total = 0;

        for (File file : files) {
            GCNGraph miniGraph = null;
            try {
                // Extract graph from file
                miniGraph = ImageLoader.extractGraphUsingSLICF2(file, numClasses);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
            if (miniGraph == null) continue;

            // Run forward pass on GCN model
            double[][] A = miniGraph.getNormalizedAdjMatrix();
            double[][] X = miniGraph.getFeatureMatrix();
            double[] avgScores = model.forward(A, X);

            // Determine prediction
            int predictedClass = argMax(avgScores);
            
            String predicted = classNames[predictedClass];

            // Determine actual class based on image number
            int imgNum = extractImageNumber(file.getName());
            String actual = determineActualClass(imgNum);

            // If prediction is correct, move image to correct folder
            if (predicted.equals(actual)) {
                correct++;
                try {
                    Path destinationDir = Paths.get("waste images", predicted);
                    Files.createDirectories(destinationDir);
                    Path destination = destinationDir.resolve(file.getName());
                    Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Wrong prediction: " + file.getName() +
                                   " predicted=" + predicted +
                                   " actual=" + actual);
            }
            total++;

            log(String.format(
                "Test '%s' → %s (scores=%s) [Actual: %s]",
                file.getName(),
                predicted,
                Arrays.toString(avgScores),
                actual
            ));
        }

        double accuracy = total > 0 ? (double) correct / total * 100.0 : 0.0;
        log(String.format("Overall Test Accuracy: %.2f%%", accuracy));
    }
    
    

    private int argMax(double[] array) {
        int maxIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIdx]) {
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    private int extractImageNumber(String filename) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(\\d+)").matcher(filename);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    private String determineActualClass(int imgNum) {
      
        if (imgNum < 1) return "unknown";
        int block = (imgNum - 1) / 12;
        if (block >= 0 && block < classNames.length) {
            return classNames[block];
        }
        return "unknown";
    }
    

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }


}


