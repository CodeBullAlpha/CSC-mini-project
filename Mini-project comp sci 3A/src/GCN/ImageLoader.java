package GCN;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import WGraph.*;

public class ImageLoader {
    private static final int NUM_SUPERPIXELS = 75;
    private static final double COMPACTNESS = 10.0;

    public static GCNGraph extractGraphUsingSLICF2(File file, int numClasses) throws IOException {
        BufferedImage image = ImageIO.read(file);
        if (image == null) return null;

        // Step 1: Resize image to standard 64x64
        Image tmp = image.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        BufferedImage resized = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.drawImage(tmp, 0, 0, null);
        g.dispose();

        // Step 2: Generate superpixels using SLIC
        int[][] labels = SLICSuperpixel.generateSuperpixels(resized, NUM_SUPERPIXELS, COMPACTNESS);
        int maxLabel = Arrays.stream(labels).flatMapToInt(Arrays::stream).max().orElse(0);

        // Step 3: Extract features and background mask
        FeatureExtractor.FeatureExtractionResult extractionResult = FeatureExtractor.extractF2Features(resized, labels, maxLabel + 1);
        double[][] baseFeatures = extractionResult.features;
        boolean[] backgroundMask = extractionResult.mask;

        // Step 4: Compute global mean and max across all features
        int dim = baseFeatures[0].length;
        double[] mean = new double[dim];
        double[] max = new double[dim];
        Arrays.fill(max, Double.NEGATIVE_INFINITY);

        for (double[] feat : baseFeatures) {
            for (int d = 0; d < dim; d++) {
                mean[d] += feat[d];
                if (feat[d] > max[d]) max[d] = feat[d];
            }
        }
        for (int d = 0; d < dim; d++) {
            mean[d] /= baseFeatures.length;
        }

        // Step 5: Enhance each node feature by concatenating [original | mean | max]
        double[][] enhancedFeatures = new double[baseFeatures.length][];
        for (int i = 0; i < baseFeatures.length; i++) {
            enhancedFeatures[i] = new double[dim * 3];
            System.arraycopy(baseFeatures[i], 0, enhancedFeatures[i], 0, dim);
            System.arraycopy(mean, 0, enhancedFeatures[i], dim, dim);
            System.arraycopy(max, 0, enhancedFeatures[i], dim * 2, dim);
        }

        // Step 6: Create GCN graph
        GCNGraph graph = new GCNGraph(numClasses);
        for (int i = 0; i < enhancedFeatures.length; i++) {
            double[] nodeFeature = enhancedFeatures[i];

            // If node is background, zero-out its feature vector
            if (backgroundMask[i]) {
                Arrays.fill(nodeFeature, 0.0);
            }

            // Add node to graph with initialized zero label vector
            graph.addNode(nodeFeature, new double[numClasses]);
        }

        // Step 7: Connect nodes using 16-NN over enhanced features
        KNN.makeCombined16NNConnections(graph, enhancedFeatures);

        return graph;
    }
}
