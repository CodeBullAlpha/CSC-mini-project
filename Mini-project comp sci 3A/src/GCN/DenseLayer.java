package GCN;

import java.util.Random;


public class DenseLayer {
     private final int inputDim;
     final int outputDim;
      double[][] weights;
	private final double[] biases;

    public DenseLayer(int inputDim, int outputDim) {
        this.inputDim = inputDim;
        this.outputDim = outputDim;
        this.weights = new double[outputDim][inputDim];
        this.biases = new double[outputDim];
        initializeWeights();
    }

    private void initializeWeights() {
        Random rand = new Random();
        double limit = Math.sqrt(6.0 / (inputDim + outputDim)); // Xavier initialization
        for (int i = 0; i < outputDim; i++) {
            for (int j = 0; j < inputDim; j++) {
                weights[i][j] = (rand.nextDouble() * 2 - 1) * limit;
            }
            biases[i] = 0.0;
        }
    }

    public double[] forward(double[] input) {
        double[] output = new double[outputDim];
        for (int i = 0; i < outputDim; i++) {
            output[i] = biases[i];
            for (int j = 0; j < inputDim; j++) {
                output[i] += weights[i][j] * input[j];
            }
        }
        return output;
    }
    
    public double[][] getWeights() {
		return weights;
	}

	public double[] getBiases() {
		return biases;
	}
	
	public void setWeight(double[][] weight)
	{
        for (int i = 0; i < outputDim; i++) {
            for (int j = 0; j < inputDim; j++) {
                weights[i][j] = weight[i][j];
            }
        }
	}
	
	public void setBias(double[] bias)
	{
		for(int i=0;i<outputDim;i++)
		{
			bias[i]=bias[i];
		}
	}
}
