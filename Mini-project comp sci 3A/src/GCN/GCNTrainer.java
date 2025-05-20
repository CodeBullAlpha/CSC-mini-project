package GCN;

import java.util.Arrays;
import java.util.List;

import WGraph.GCNGraph;
import DataStructures.CustomArrayList;
import DataStructures.Iterator;

public class GCNTrainer {
    private GCNModel model;
    private double learningRate;
    private int currentEpoch = 0;
    private AdamOptimizer optimizer1, optimizer2,optimizerDense;
    private volatile boolean stopRequested = false;

    public GCNTrainer(GCNModel model, double learningRate) {
        this.model = model;
        this.learningRate = learningRate;
        optimizer1 = new AdamOptimizer(learningRate);
        optimizer2 = new AdamOptimizer(learningRate);
        optimizerDense= new AdamOptimizer(learningRate);
    }

    private double computeLoss(double[][] predictions, double[][] labels) {
        double loss = 0.0;
        double epsilon = 1e-9;
        int n = labels.length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < labels[0].length; j++) {
                double p = Math.max(epsilon, Math.min(1 - epsilon, predictions[i][j]));
                loss -= labels[i][j] * Math.log(p);
            }
        }
        return loss / n;
    }

    private double[][] computeLossGradient(double[][] predictions, double[][] labels) {
        int n = labels.length;
        int numClasses = labels[0].length;
        double[][] grad = new double[n][numClasses];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < numClasses; j++) {
                grad[i][j] = predictions[i][j] - labels[i][j];
            }
        }
        return grad;
    }

    private double[][] transpose(double[][] m) {
        assert m.length > 0 : "Cannot transpose empty matrix";
        int rows = m.length, cols = m[0].length;
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result[j][i] = m[i][j];
        return result;
    }

    private double[][] leakyReLUGradient(double[][] m) {
        int rows = m.length, cols = m[0].length;
        double[][] grad = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                grad[i][j] = m[i][j] > 0 ? 1.0 : 0.01;
        return grad;
    }

    private double[][] matmul(double[][] A, double[][] B) {
        int rowsA = A.length, colsA = A[0].length;
        int rowsB = B.length, colsB = B[0].length;
        assert colsA == rowsB : "matmul dimension mismatch: " + colsA + " vs " + rowsB;
        double[][] result = new double[rowsA][colsB];
        for (int i = 0; i < rowsA; i++)
            for (int j = 0; j < colsB; j++)
                for (int k = 0; k < colsA; k++)
                    result[i][j] += A[i][k] * B[k][j];
        return result;
    }

 // NEW helper: matrix-vector multiplication
    private double[] matVecMul(double[][] M, double[] v) {
        int rows = M.length;
        int cols = M[0].length;
        assert v.length == cols : "matVecMul dimension mismatch: vector length " + v.length + " vs cols " + cols;
        double[] result = new double[rows];
        for (int i = 0; i < rows; i++) {
            double sum = 0.0;
            for (int j = 0; j < cols; j++) {
                sum += M[i][j] * v[j];
            }
            result[i] = sum;
        }
        return result;
    }
    private double[][] elementwiseMultiply(double[][] A, double[][] B) {
        int rows = A.length, cols = A[0].length;
        assert rows == B.length && cols == B[0].length : "elementwiseMultiply dimension mismatch";
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result[i][j] = A[i][j] * B[i][j];
        return result;
    }

    private CustomArrayList<CustomArrayList<GCNGraph>> createMiniBatches(CustomArrayList<GCNGraph> graphs, int batchSize) {
        CustomArrayList<CustomArrayList<GCNGraph>> miniBatches = new CustomArrayList<>();
        for (int i = 0; i < graphs.size(); i += batchSize)
            miniBatches.add(graphs.subList(i, Math.min(graphs.size(), i + batchSize)));
        return miniBatches;
    }

    public void train(CustomArrayList<GCNGraph> graphs, CustomArrayList<GCNGraph> validationGraphs, int epochs, int batchSize) {
    	//keep track of the validation accuracy 
    	double validationAccuracy=0.0;
    	
        for (int epoch = currentEpoch; epoch < epochs; epoch++) {
        	
            if (stopRequested) {
                GCNMemory.saveSession(model, epoch, learningRate, this, "TRAIN",validationAccuracy);
                GCNMemory.saveGraphList(graphs, "GRAPH");
                System.out.println("Training stopped at epoch " + epoch);
                return;
            }

            // Shuffle training data
	    //            Collections.shuffle(graphs);
	    graphs.shuffle(); //might cause problems later on
            CustomArrayList<CustomArrayList<GCNGraph>> miniBatches = createMiniBatches(graphs, batchSize);
            double epochLoss = 0.0;

            // Training phase
	    Iterator<CustomArrayList<GCNGraph>> batchIter = miniBatches.iterator();
	    while(batchIter.hasNext()){
		CustomArrayList<GCNGraph> batch  = batchIter.next();
                // Accumulated gradients
                double[][] accGradW1 = new double[model.gcn1.weights.length][model.gcn1.weights[0].length];
                double[][] accGradW2 = new double[model.gcn2.weights.length][model.gcn2.weights[0].length];
                double[][] accGradWDense = new double[model.dense.weights.length][model.dense.weights[0].length];
                double batchLoss = 0.0;

		Iterator<GCNGraph> graphIter = batch.iterator();
		while(graphIter.hasNext()){
		    GCNGraph graph = graphIter.next() ;
		    double[][] A =  graph.getNormalizedAdjMatrix();
                    double[][] X = graph.getFeatureMatrix();
                    double[][] label = graph.getGraphLevelLabel();

                    // Forward through model
                    double[] graphOut = model.forward(A, X);
                    double[][] pred = new double[][] { graphOut };

                    // Loss
                    double loss = computeLoss(pred, label);
                    batchLoss += loss;

                    // Gradient at output
                    double[][] dGraph = computeLossGradient(pred, label);  // shape [1][C]

                 // BACKWARD PASS (high-level):
                    // 1) Dense layer grads
                    //    - compute grad w.r.t. weights and bias
                    //    - compute grad w.r.t. combined feature vector
                    double[] combined = model.getLastCombined(); // retrieve from forward
                    double[][] gradWDense = outerProduct(dGraph[0], combined);
                    double[] dCombined = matVecMul(transpose(model.dense.weights), dGraph[0]);

                    // 2) Split dCombined into dMean and dMax parts
                    int h = dCombined.length / 2;
                    double[] dMean = Arrays.copyOfRange(dCombined, 0, h);
                    double[] dMax  = Arrays.copyOfRange(dCombined, h, dCombined.length);

                    // 3) Backprop through pooling to get gradient on H2 embeddings
                    double[][] H2 = model.getLastH2(); // saved from forward
                    int N = H2.length;
                    double[][] dH2 = new double[N][h];

                    // Mean pooling gradient: equally distribute
                    for (int i = 0; i < N; i++) {
                        for (int j = 0; j < h; j++) {
                            dH2[i][j] += dMean[j] / N;
                        }
                    }
                    // Max pooling gradient: only at max indices
                    for (int j = 0; j < h; j++) {
                        int idx = model.getMaxIndex(j); // saved argmax per dim
                        dH2[idx][j] += dMax[j];
                    }

                    // 4) Backprop through GCN layers (same as before but two layers)
                    // grad for layer2 weights
                    double[][] H1 = model.getLastH1();
                    double[][] dZ2 = elementwiseMultiply(dH2, leakyReLUGradient(model.getLastZ2()));
                    double[][] gradW2 = matmul(transpose(H1), matmul(A, dZ2));

                    // gradient to H1
                    double[][] dH1 = matmul(A, matmul(dZ2, transpose(model.gcn2.weights)));

                    // grad for layer1 weights
                    double[][] X0 = X;
                    double[][] dZ1 = elementwiseMultiply(dH1, leakyReLUGradient(model.getLastZ1()));
                    double[][] gradW1 = matmul(transpose(X0), matmul(A, dZ1));

                    // accumulate
                    accumulate(accGradW1, gradW1);
                    accumulate(accGradW2, gradW2);
                    accumulate(accGradWDense, gradWDense);
                    
                }

                scaleGradients(accGradW1, batch.size());
                scaleGradients(accGradW2, batch.size());
                scaleGradients(accGradWDense, batch.size());

                // clip
                clipGradients(accGradW1, 1.0);
                clipGradients(accGradW2, 1.0);
                clipGradients(accGradWDense, 1.0);

                // update
                optimizer1.update(model.gcn1.weights, accGradW1);
                optimizer2.update(model.gcn2.weights, accGradW2);
                optimizerDense.update(model.dense.getWeights(), accGradWDense);
                
                epochLoss += batchLoss;
            }

            // Validation phase
            double validationLoss = 0.0;
            int correctPredictions = 0;
            int totalPredictions = 0;

	    Iterator<GCNGraph> graphIter = validationGraphs.iterator();
	    while(graphIter.hasNext()) {
		GCNGraph graph = graphIter.next();
                double[][] A = graph.getNormalizedAdjMatrix();
                double[][] X = graph.getFeatureMatrix();
                double[][] label = graph.getGraphLevelLabel();

                // Forward through model
                double[] graphOut = model.forward(A, X);
                double[][] pred = new double[][] { graphOut };

                // Calculate loss
                double loss = computeLoss(pred, label);
                validationLoss += loss;

                // Calculate accuracy
                int predictedClass = getMaxIndex(graphOut);
                int trueClass = getMaxIndex(label[0]); 

                if (predictedClass == trueClass) {
                    correctPredictions++;
                }
                totalPredictions++;
            }

            // Compute validation loss and accuracy
            double avgValidationLoss = validationLoss / validationGraphs.size();
             validationAccuracy = (double)correctPredictions / totalPredictions*100.0;

            // Print epoch stats
            System.out.println("Epoch " + epoch + " Avg Loss: " + (epochLoss / graphs.size())
                    + " Validation Loss: " + avgValidationLoss
                    + " Validation Accuracy: " + validationAccuracy
                    + " Learning rate: " + learningRate);

            currentEpoch = epoch + 1;
        }
    }

    // Helper method to get the index of the maximum value in an array (for classification)
    private int getMaxIndex(double[] array) {
        int maxIndex = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }

    public GCNModel getModel() {
		return model;
	}

	public void setModel(GCNModel model) {
		this.model = model;
	}

	public AdamOptimizer getOptimizer2() {
		return optimizer2;
	}

	public void setOptimizer2(AdamOptimizer optimizer2) {
		this.optimizer2 = optimizer2;
	}

	public AdamOptimizer getOptimizerDense() {
		return optimizerDense;
	}

	public void setOptimizerDense(AdamOptimizer optimizerDense) {
		this.optimizerDense = optimizerDense;
	}

	private void accumulate(double[][] total, double[][] grad) {
        for (int i = 0; i < total.length; i++) {
            for (int j = 0; j < total[0].length; j++) {
                total[i][j] += grad[i][j];
            }
        }
    }

    private void scaleGradients(double[][] grad, int batchSize) {
        for (int i = 0; i < grad.length; i++) {
            for (int j = 0; j < grad[0].length; j++) {
                grad[i][j] /= batchSize;
            }
        }
    }


    
    public double getLearningRate() {
		return learningRate;
	}


	//I tried to clip the gradients 
    private double[][] clipGradients(double[][] grads, double threshold) {
        double[][] clipped = new double[grads.length][grads[0].length];
        for (int i = 0; i < grads.length; i++) {
            for (int j = 0; j < grads[0].length; j++) {
                clipped[i][j] = Math.max(-threshold, Math.min(threshold, grads[i][j]));
            }
        }
        return clipped;
    }
    
    public void setCurrentEpoch(int epoch) {
        this.currentEpoch = epoch;
    }

    public int getCurrentEpoch()
    {
    	return this.currentEpoch;
    }
    
    public void setLearningRate(double lr)
    {
    	this.learningRate=lr;
    }

	public AdamOptimizer getOptimizer1() {
		return optimizer1;
	}

	public void setOptimizer1(AdamOptimizer optimizer1) {
		this.optimizer1 = optimizer1;
	}

	
    public void requestStop() {
    	System.out.println("Session stoping...");
        stopRequested = true;  //method to stop training
    }

	public void setStopRequested(boolean stopRequested) {
		this.stopRequested = stopRequested;
	}
	
	public double[][] meanAcrossNodes(double[][] nodeOutputs) {
	    int numNodes = nodeOutputs.length;
	    int numClasses = nodeOutputs[0].length;
	    double[] graphOutput = new double[numClasses];
	    
	    for (int i = 0; i < numNodes; i++) {
	        for (int j = 0; j < numClasses; j++) {
	            graphOutput[j] += nodeOutputs[i][j];
	        }
	    }
	    
	    for (int j = 0; j < numClasses; j++) {
	        graphOutput[j] /= numNodes;
	    }
	    
	    return new double[][]{graphOutput}; // returning [1][numClasses] shape
	}


	private double[][] outerProduct(double[] a, double[] b) {
        double[][] result = new double[a.length][b.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                result[i][j] = a[i] * b[j];
            }
        }
        return result;
    }
	
}



