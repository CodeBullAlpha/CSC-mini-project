package GCN;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import WGraph.GCNGraph;
import WasteSimulation.WasteType;

public class GCNInferenceHelper {

    private static GCNModel model;
    private static final String[] classNames = {"metal", "paper", "plastic", "brown-glass"};

    public static void loadModel(String modelPath) {
        model = new GCNModel(24, 16, 4); // Make sure these match your saved model structure
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(modelPath))) {
            model.gcn1.weights = (double[][]) in.readObject();
            model.gcn1.bias = (double[]) in.readObject();
            model.gcn2.weights = (double[][]) in.readObject();
            model.gcn2.bias = (double[]) in.readObject();
            model.dense.setWeight((double[][]) in.readObject());
            model.dense.setBias((double[]) in.readObject());

            System.out.println("Model loaded from: " + modelPath);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load model from file: " + modelPath);
        }
    }

    public static WasteType Validate1Image(File wasteImage) {
        if (model == null) {
            throw new IllegalStateException("GCN model not loaded. Call loadModel() first.");
        }

        try {
            GCNGraph graph = ImageLoader.extractGraphUsingSLICF2(wasteImage, 4);
            if (graph == null) return WasteType.PLASTIC;

            double[][] A = graph.getNormalizedAdjMatrix();
            double[][] X = graph.getFeatureMatrix();

            double[] avgScores = model.forward(A, X);
            int predictedClass = argMax(avgScores);
            String predictedClassName = classNames[predictedClass];

            try {
                String actualClass = determineActualClass(extractImageNumber(wasteImage.getName()));
                if (predictedClassName.equals(actualClass)) {
                    Path destinationDir = Paths.get("waste images", predictedClassName);
                    Files.createDirectories(destinationDir);
                    Path destination = destinationDir.resolve(wasteImage.getName());
                    Files.copy(wasteImage.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            switch (predictedClassName) {
                case "metal": return WasteType.METAL;
                case "paper": return WasteType.PAPER;
                case "plastic": return WasteType.PLASTIC;
                case "brown-glass": return WasteType.GLASS;
                default: return WasteType.PLASTIC;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return WasteType.PLASTIC;
        }
    }

    private static int argMax(double[] array) {
        int maxIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIdx]) {
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    private static int extractImageNumber(String filename) {
        Matcher m = Pattern.compile("(\\d+)").matcher(filename);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }

    private static String determineActualClass(int imgNum) {
        if (imgNum < 1) return "unknown";
        int block = (imgNum - 1) / 12;
        if (block >= 0 && block < classNames.length) {
            return classNames[block];
        }
        return "unknown";
    }
}
