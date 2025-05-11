package GCN;
import java.util.Arrays;

public class LayerNorm {
    private int features;
    private double eps = 1e-5;
    private double[] gamma, beta;

    public LayerNorm(int features) {
        this.features = features;
        gamma = new double[features];
        beta  = new double[features];
        Arrays.fill(gamma, 1.0);
    }

    public double[][] forward(double[][] X) {
        int N = X.length;
        double[][] out = new double[N][features];
        for (int i = 0; i < N; i++) {
            // compute mean & var per node
            double mean=0, var=0;
            for (int j = 0; j < features; j++) mean += X[i][j];
            mean /= features;
            for (int j = 0; j < features; j++) var += Math.pow(X[i][j] - mean, 2);
            var /= features;
            // normalize
            for (int j = 0; j < features; j++) {
                double xhat = (X[i][j] - mean) / Math.sqrt(var + eps);
                out[i][j] = gamma[j]*xhat + beta[j];
            }
        }
        return out;
    }
}
