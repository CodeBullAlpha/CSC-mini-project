package GCN;

public class AdamOptimizer {
    private double beta1 = 0.9;
    private double beta2 = 0.999;
    private double epsilon = 1e-8;
    private double currentLR;
    private long t = 0;
    private double[][] m;
    private double[][] v;

    public AdamOptimizer(double learningRate) {
        this.currentLR = learningRate;
    }

    public void setCurrentLR(double lr) {
        this.currentLR = lr;
    }

    /**
     * Performs an Adam update on the given weights using the provided gradients.
     * This method modifies the weights array in-place and does not return a new array.
     */
    public void update(double[][] weights, double[][] gradW) {
        t++;  // increment timestep

        // Ensure dimensions match
        assert weights.length == gradW.length && weights[0].length == gradW[0].length
            : "Dimension mismatch in Adam update";

        // Initialize moment accumulators on first call or if shape changes
        if (m == null || m.length != weights.length || m[0].length != weights[0].length) {
            m = new double[weights.length][weights[0].length];
            v = new double[weights.length][weights[0].length];
        }

        // Precompute bias-correction denominators
        double biasCorr1 = 1 - Math.pow(beta1, t);
        double biasCorr2 = 1 - Math.pow(beta2, t);

        for (int i = 0; i < weights.length; i++) {
            for (int j = 0; j < weights[0].length; j++) {
                // Update biased first moment estimate
                m[i][j] = beta1 * m[i][j] + (1 - beta1) * gradW[i][j];
                // Update biased second raw moment estimate
                v[i][j] = beta2 * v[i][j] + (1 - beta2) * gradW[i][j] * gradW[i][j];

                // Compute bias-corrected moment estimates
                double mHat = m[i][j] / biasCorr1;
                double vHat = v[i][j] / biasCorr2;

                // Update weight
                weights[i][j] -= currentLR * mHat / (Math.sqrt(vHat) + epsilon);
            }
        }
    }

	public double getBeta1() {
		return beta1;
	}

	public void setBeta1(double beta1) {
		this.beta1 = beta1;
	}

	public double getBeta2() {
		return beta2;
	}

	public void setBeta2(double beta2) {
		this.beta2 = beta2;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}

	public long getT() {
		return t;
	}

	public void setT(long t) {
		this.t = t;
	}

	public double[][] getM() {
		return m;
	}

	public void setM(double[][] m) {
		this.m = m;
	}

	public double[][] getV() {
		return v;
	}

	public void setV(double[][] v) {
		this.v = v;
	}

	public double getCurrentLR() {
		return currentLR;
	}
}

