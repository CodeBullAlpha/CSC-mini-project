package GCN;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SLICSuperpixel {
	 
	public static int[][] generateSuperpixels(BufferedImage img, int numSuperpixels, double compactness) {
	        int width = img.getWidth();
	        int height = img.getHeight();
	        int S = (int) Math.sqrt((width * height) / (double) numSuperpixels);
	        int[][] labels = new int[width][height];
	        double[][][] lab = new double[width][height][3];
	        for (int y = 0; y < height; y++)
	            for (int x = 0; x < width; x++)
	                lab[x][y] = rgbToLab(new Color(img.getRGB(x, y)));

	        List<ClusterCenter> centers = new ArrayList<>();
	        for (int y = S / 2; y < height; y += S) {
	            for (int x = S / 2; x < width; x += S) {
	                double[] color = lab[x][y];
	                centers.add(new ClusterCenter(color[0], color[1], color[2], x, y));
	            }
	        }

	        double[][] distances = new double[width][height];
	        for (double[] row : distances)
	            Arrays.fill(row, Double.MAX_VALUE);

	        for (int iter = 0; iter < 10; iter++) {
	            for (int k = 0; k < centers.size(); k++) {
	                ClusterCenter c = centers.get(k);
	                for (int dy = -S; dy <= S; dy++) {
	                    for (int dx = -S; dx <= S; dx++) {
	                        int x = c.x + dx;
	                        int y = c.y + dy;
	                        if (x >= 0 && x < width && y >= 0 && y < height) {
	                            double d = colorSpatialDistance(lab[x][y], x, y, c, S, compactness);
	                            if (d < distances[x][y]) {
	                                distances[x][y] = d;
	                                labels[x][y] = k;
	                            }
	                        }
	                    }
	                }
	            }

	            ClusterCenter[] newCenters = new ClusterCenter[centers.size()];
	            int[] counts = new int[centers.size()];
	            for (int i = 0; i < newCenters.length; i++)
	                newCenters[i] = new ClusterCenter(0, 0, 0, 0, 0);

	            for (int x = 0; x < width; x++) {
	                for (int y = 0; y < height; y++) {
	                    int label = labels[x][y];
	                    double[] c = lab[x][y];
	                    newCenters[label].l += c[0];
	                    newCenters[label].a += c[1];
	                    newCenters[label].b += c[2];
	                    newCenters[label].x += x;
	                    newCenters[label].y += y;
	                    counts[label]++;
	                }
	            }

	            for (int i = 0; i < newCenters.length; i++) {
	                if (counts[i] > 0) {
	                    newCenters[i].l /= counts[i];
	                    newCenters[i].a /= counts[i];
	                    newCenters[i].b /= counts[i];
	                    newCenters[i].x /= counts[i];
	                    newCenters[i].y /= counts[i];
	                }
	            }
	            //centers = (ArrayList<ClusterCenter>) Arrays.asList(newCenters);
	        }
	        return labels;
	    }

	    private static double[] rgbToLab(Color color) {
	        float r = color.getRed() / 255f;
	        float g = color.getGreen() / 255f;
	        float b = color.getBlue() / 255f;

	        r = (r > 0.04045) ? (float) Math.pow((r + 0.055) / 1.055, 2.4) : (r / 12.92f);
	        g = (g > 0.04045) ? (float) Math.pow((g + 0.055) / 1.055, 2.4) : (g / 12.92f);
	        b = (b > 0.04045) ? (float) Math.pow((b + 0.055) / 1.055, 2.4) : (b / 12.92f);

	        float X = r * 0.4124f + g * 0.3576f + b * 0.1805f;
	        float Y = r * 0.2126f + g * 0.7152f + b * 0.0722f;
	        float Z = r * 0.0193f + g * 0.1192f + b * 0.9505f;

	        X /= 0.95047; Y /= 1.00000; Z /= 1.08883;

	        float fx = (X > 0.008856) ? (float) Math.pow(X, 1.0 / 3) : (7.787f * X) + 16.0f / 116;
	        float fy = (Y > 0.008856) ? (float) Math.pow(Y, 1.0 / 3) : (7.787f * Y) + 16.0f / 116;
	        float fz = (Z > 0.008856) ? (float) Math.pow(Z, 1.0 / 3) : (7.787f * Z) + 16.0f / 116;

	        float L = (116 * fy) - 16;
	        float A = 500 * (fx - fy);
	        float B = 200 * (fy - fz);

	        return new double[]{L, A, B};
	    }

	    private static double colorSpatialDistance(double[] lab, int x, int y, ClusterCenter c, int S, double m) {
	        double dc = Math.sqrt(Math.pow(lab[0] - c.l, 2) + Math.pow(lab[1] - c.a, 2) + Math.pow(lab[2] - c.b, 2));
	        double ds = Math.sqrt((x - c.x) * (x - c.x) + (y - c.y) * (y - c.y));
	        return Math.sqrt(dc * dc + (ds * ds) * (m * m) / (S * S));
	    }

	    static class ClusterCenter {
	        double l, a, b;
	        int x, y;

	        ClusterCenter(double l, double a, double b, int x, int y) {
	            this.l = l;
	            this.a = a;
	            this.b = b;
	            this.x = x;
	            this.y = y;
	        }
	    }
}
