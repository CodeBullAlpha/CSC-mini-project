package GCN;

public class GCNLayer {
    double[][] weights;
    double[] bias;
    //private BatchNorm1d bn;
    private final LayerNorm ln;

    public GCNLayer(int inputSize, int outputSize, String initType) {
        weights = new double[inputSize][outputSize];
        bias = new double[outputSize]; // one bias per output feature

        double std = 0;

        // Choose initialization method
        switch (initType) {
            case "he":
                std = Math.sqrt(2.0 / inputSize);
                break;
            case "xavier":
                std = Math.sqrt(2.0 / (inputSize + outputSize));
                break;
            case "lecun":
                std = Math.sqrt(1.0 / inputSize);
                break;
            default:
                throw new IllegalArgumentException("Unknown initialization type");
        }

        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < outputSize; j++) {
                weights[i][j] = (Math.random() * 2 * std) - std;  // Initialize randomly
            }
        }
        
        for (int j = 0; j < outputSize; j++) {
            bias[j] = 0.0;  // start bias at zero
        }
        //bn = new BatchNorm1d(outputSize);
        ln = new LayerNorm(outputSize);  
    }


 // In GCNLayer.java
    public double[][] forward(double[][] A, double[][] X) {
        double[][] AX = matmul(A, X);
        double[][] AXW = matmul(AX, weights);
        
        // Residual connection
        if (AXW.length == X.length && AXW[0].length == X[0].length) {
            for (int i = 0; i < AXW.length; i++) {
                for (int j = 0; j < AXW[0].length; j++) {
                    AXW[i][j] += X[i][j];  // Skip connection
                }
            }
        }
        AXW = ln.forward(AXW);

        return AXW;
    }
    

    private double[][] matmul(double[][] A, double[][] B) {
        int rows = A.length;
        int cols = B[0].length; 
        int inner = A[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                for (int k = 0; k < inner; k++)
                    result[i][j] += A[i][k] * B[k][j];
        
        return result;
    }

    // Standalone activation functions below (they will not be applied inside forward).
    public double[][] leakyReLU(double[][] X) {
        double alpha = 0.01;
        double[][] result = new double[X.length][X[0].length];
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < X[0].length; j++) {
                result[i][j] = X[i][j] > 0 ? X[i][j] : alpha * X[i][j];
            }
        }
        return result;
    }
    
    //This functions is used for binary classification 
    public double[][] sigmoid(double[][] X) {
        double[][] result = new double[X.length][X[0].length];
        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < X[0].length; j++) {
                result[i][j] = 1.0 / (1.0 + Math.exp(-X[i][j]));
            }
        }
        return result;
    }
    



    

}
