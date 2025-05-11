package GCN;

import java.util.Arrays;

public class BatchNorm1d {
    private int features;
    private double eps = 1e-5, momentum = 0.1;
    private double[] gamma, beta, runningMean, runningVar;

    public BatchNorm1d(int features) {
        this.features = features;
        gamma = new double[features]; beta = new double[features];
        runningMean = new double[features]; runningVar = new double[features];
        Arrays.fill(gamma, 1.0);
    }

    /** Forward pass on shape [nodes][features]. */
    public double[][] forward(double[][] X, boolean training) {
        int N = X.length;
        double[] batchMean = new double[features], batchVar = new double[features];
        // 1) Compute batch mean
        for (double[] row : X)
            for (int j = 0; j < features; j++)
                batchMean[j] += row[j];
        for (int j = 0; j < features; j++)
            batchMean[j] /= N;
        // 2) Compute batch var
        for (double[] row : X)
            for (int j = 0; j < features; j++)
                batchVar[j] += Math.pow(row[j] - batchMean[j], 2);
        for (int j = 0; j < features; j++)
            batchVar[j] /= N;

        // 3) Update running stats
        if (training) {
            for (int j = 0; j < features; j++) {
                runningMean[j] = momentum * runningMean[j] + (1 - momentum) * batchMean[j];
                runningVar[j] = momentum * runningVar[j] + (1 - momentum) * batchVar[j];
            }
        } else {
            batchMean = runningMean;
            batchVar = runningVar;
        }

        // 4) Normalize
        double[][] out = new double[N][features];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < features; j++) {
                double xhat = (X[i][j] - batchMean[j]) / Math.sqrt(batchVar[j] + eps);
                out[i][j] = gamma[j] * xhat + beta[j];
            }
        }
        return out;
    }
}
