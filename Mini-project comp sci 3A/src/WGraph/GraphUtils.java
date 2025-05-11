package WGraph;

public class GraphUtils {
    public static double[][] normalizeAdjacency(double[][] A) {
        int n = A.length;
        double[][] A_hat = new double[n][n];

        // Add self-loops
        for (int i = 0; i < n; i++) {
            A_hat[i][i] = A[i][i] + 1;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    A_hat[i][j] = A[i][j];
                }
            }
        }

        // Compute degree matrix
        double[] D = new double[n];
        for (int i = 0; i < n; i++) {
            D[i] = 0;
            for (int j = 0; j < n; j++) {
                D[i] += A_hat[i][j];
            }
        }

        // Compute D^{-1/2}
        double[] D_inv_sqrt = new double[n];
        for (int i = 0; i < n; i++) {
            D_inv_sqrt[i] = 1.0 / Math.sqrt(D[i]);
        }

        // Compute normalized adjacency matrix
        double[][] A_norm = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                A_norm[i][j] = A_hat[i][j] * D_inv_sqrt[i] * D_inv_sqrt[j];
            }
        }

        return A_norm;
    }
    
    public static double[][] softmax(double[][] X) {
        double[][] result = new double[X.length][X[0].length];
        
        for (int i = 0; i < X.length; i++) {
            double max = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < X[0].length; j++) {
                if (X[i][j] > max) {
                    max = X[i][j];
                }
            }
            
            double sum = 0.0;
            for (int j = 0; j < X[0].length; j++) {
                result[i][j] = Math.exp(X[i][j] - max); // Numerical stability
                sum += result[i][j];
            }
            
            for (int j = 0; j < X[0].length; j++) {
                result[i][j] /= sum;
            }
        }
        
        return result;
    }
}
