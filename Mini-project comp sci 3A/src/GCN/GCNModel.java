package GCN;

import java.util.Arrays;
import WGraph.GraphUtils;

public class GCNModel {
    protected final GCNLayer gcn1, gcn2;
    protected final DenseLayer dense;

    // Stored activations for backprop
    private double[][] lastZ1, lastH1;
    private double[][] lastZ2, lastH2;
    private double[] lastCombined;
    private int[] maxIndices;

    public GCNModel(int inputSize, int hiddenSize, int outputSize) {
        this.gcn1 = new GCNLayer(inputSize, hiddenSize, "he");
        this.gcn2 = new GCNLayer(hiddenSize, hiddenSize, "he");
        this.dense = new DenseLayer(hiddenSize * 2, outputSize); // mean + max concatenated
    }

    public double[] forward(double[][] A, double[][] X) {
        // 1. GCN + LeakyReLU Layer 1
        lastZ1 = gcn1.forward(A, X);
        lastH1 = gcn1.leakyReLU(lastZ1);

        // 2. GCN + LeakyReLU Layer 2
        lastZ2 = gcn2.forward(A, lastH1);
        lastH2 = gcn2.leakyReLU(lastZ2);

        // 3. Global Pooling
        double[][] mean = meanPooling(lastH2);
        double[][] max = maxPoolingWithIndices(lastH2);

        // 4. Concatenate pooled features
        int dim = mean[0].length;
        lastCombined = new double[dim * 2];
        System.arraycopy(mean[0], 0, lastCombined, 0, dim);
        System.arraycopy(max[0], 0, lastCombined, dim, dim);

        // 5. Dense + Softmax
        double[] logits = dense.forward(lastCombined);
        return GraphUtils.softmax(new double[][] { logits })[0];
    }

    private double[][] meanPooling(double[][] features) {
        int dim = features[0].length;
        double[] sum = new double[dim];
        for (double[] vec : features) {
            for (int i = 0; i < dim; i++) sum[i] += vec[i];
        }
        for (int i = 0; i < dim; i++) sum[i] /= features.length;
        return new double[][] { sum };
    }

    private double[][] maxPoolingWithIndices(double[][] features) {
        int dim = features[0].length;
        maxIndices = new int[dim];
        double[] max = new double[dim];
        Arrays.fill(max, Double.NEGATIVE_INFINITY);
        for (int i = 0; i < features.length; i++) {
            for (int j = 0; j < dim; j++) {
                if (features[i][j] > max[j]) {
                    max[j] = features[i][j];
                    maxIndices[j] = i;
                }
            }
        }
        return new double[][] { max };
    }

    // Getters for Trainer
    public double[][] getLastZ1()    
    { 
    	return lastZ1; 
    }
    
    public double[][] getLastH1()    
    { 
    	return lastH1; 
    }
    
    public double[][] getLastZ2()    
    { 
    	return lastZ2; 
    }
    
    public double[][] getLastH2()    
    { 
    	return lastH2; 
    }
    
    public double[]   getLastCombined() 
    { 
    	return lastCombined; 
    }
    
    public int getMaxIndex(int featureDim) 
    { 
    	return maxIndices[featureDim]; 
    }
}

