package GCN;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class FeatureExtractor {
	  public static class FeatureExtractionResult {
	        public double[][] features;
	        public boolean[] mask; // true if node is background
	    }

	    public static FeatureExtractionResult extractF2Features(BufferedImage img, int[][] labels, int numRegions) {
	        double[][] features = new double[numRegions][8]; // Stores mean features per region
	        double[][] sumSqRGB = new double[numRegions][3]; // For stddev calculation
	        int[] counts = new int[numRegions]; // Number of pixels per region

	        // First pass: accumulate sums and squared sums
	        for (int y = 0; y < img.getHeight(); y++) {
	            for (int x = 0; x < img.getWidth(); x++) {
	                int label = labels[x][y];
	                Color c = new Color(img.getRGB(x, y));
	                float[] hsv = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);

	                // Normalize RGB to [0,1] and accumulate
	                double r = c.getRed() / 255.0;
	                double g = c.getGreen() / 255.0;
	                double b = c.getBlue() / 255.0;

	                features[label][0] += r; // Mean Red
	                features[label][1] += g; // Mean Green
	                features[label][2] += b; // Mean Blue
	                features[label][3] += hsv[0]; // Hue
	                features[label][4] += hsv[1]; // Saturation
	                features[label][5] += hsv[2]; // Brightness
	                features[label][6] += x / (double) img.getWidth(); // Normalized X
	                features[label][7] += y / (double) img.getHeight(); // Normalized Y

	                // Accumulate squared values for stddev later
	                sumSqRGB[label][0] += r * r;
	                sumSqRGB[label][1] += g * g;
	                sumSqRGB[label][2] += b * b;

	                counts[label]++;
	            }
	        }

	        // Second pass: finalize means and compute mask
	        boolean[] mask = new boolean[numRegions]; // true if node is pure white background

	        for (int i = 0; i < numRegions; i++) {
	            if (counts[i] == 0) continue; // Skip empty regions

	            // Finalize mean computation
	            for (int j = 0; j < 8; j++) {
	                features[i][j] /= counts[i];
	            }

	            // Compute standard deviation of RGB
	            double stdR = Math.sqrt(sumSqRGB[i][0] / counts[i] - features[i][0] * features[i][0]);
	            double stdG = Math.sqrt(sumSqRGB[i][1] / counts[i] - features[i][1] * features[i][1]);
	            double stdB = Math.sqrt(sumSqRGB[i][2] / counts[i] - features[i][2] * features[i][2]);

	            double avgStd = (stdR + stdG + stdB) / 3.0; // Average std across channels

	            // Determine if the node is background
	            boolean isBright = features[i][0] > 0.96 && features[i][1] > 0.96 && features[i][2] > 0.96;
	            boolean isFlat = avgStd < 0.01;

	            mask[i] = isBright && isFlat; // Only pure, textureless white areas are masked
	        }

	        FeatureExtractionResult result = new FeatureExtractionResult();
	        result.features = features;
	        result.mask = mask;
	        return result;
	    }
}
