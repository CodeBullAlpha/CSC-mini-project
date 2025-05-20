package GCN;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.List;
import WGraph.GCNGraph;


import DataStructures.CustomArrayList;

public class GCNMemory {

    private static final String SAVE_FOLDER = "saved_sessions";

    static {
        // Ensure the save folder exists
        File folder = new File(SAVE_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public static void saveSession(GCNModel model,
    		int currentEpoch,
    		double learningRate,
    		GCNTrainer trainer,
    		String sessionName,double Accuracy) {
    	try {
    		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    		String fileName = SAVE_FOLDER + "/" + sessionName + "_epoch" + currentEpoch + "_" + timestamp
    				+"Acccuracy%"+Accuracy;
    		new File(SAVE_FOLDER).mkdirs();

    		try (ObjectOutputStream out = new ObjectOutputStream(
    				new BufferedOutputStream(new FileOutputStream(fileName + ".dat")))) {
    			// Save GCN layer 1
    			out.writeObject(model.gcn1.weights);
    			out.writeObject(model.gcn1.bias);
    			// Save GCN layer 2
    			out.writeObject(model.gcn2.weights);
    			out.writeObject(model.gcn2.bias);
    			// Save Dense layer
    			out.writeObject(model.dense.getWeights());
    			out.writeObject(model.dense.getBiases());

    			// Save training state
    			out.writeDouble(learningRate);
    			out.writeInt(currentEpoch);

    			// Save optimizers
    			// Adam 1
    			out.writeObject(trainer.getOptimizer1().getM());
    			out.writeObject(trainer.getOptimizer1().getV());
    			out.writeLong(trainer.getOptimizer1().getT());
    			// Adam 2
    			out.writeObject(trainer.getOptimizer2().getM());
    			out.writeObject(trainer.getOptimizer2().getV());
    			out.writeLong(trainer.getOptimizer2().getT());
    			// Adam Dense
    			out.writeObject(trainer.getOptimizerDense().getM());
    			out.writeObject(trainer.getOptimizerDense().getV());
    			out.writeLong(trainer.getOptimizerDense().getT());
    		}

    		System.out.println("Session saved to: " + fileName + ".dat");

    	} catch (IOException e) {
    		e.printStackTrace();
    	}	
    }

    public static boolean resumeSession(String filePath,
    		GCNModel model,
    		GCNTrainer trainer) {
    	File file = new File(filePath);
    	if (!file.exists()) {
    		System.out.println("Save file does not exist: " + filePath);
    		return false;
    	}

    	try (ObjectInputStream in = new ObjectInputStream(
    			new BufferedInputStream(new FileInputStream(file)))) {
    		// Load GCN layer 1
    		model.gcn1.weights = (double[][]) in.readObject();
    		model.gcn1.bias = (double[]) in.readObject();
    		// Load GCN layer 2
    		model.gcn2.weights = (double[][]) in.readObject();
    		model.gcn2.bias = (double[]) in.readObject();
    		// Load Dense layer
    		model.dense.setWeight((double[][]) in.readObject());
    		model.dense.setBias((double[]) in.readObject()); 

    		// Load training state
    		double lr = in.readDouble();
    		trainer.setLearningRate(lr);
    		trainer.setCurrentEpoch(in.readInt());

    		// Load optimizers
    		// Adam 1
    		trainer.getOptimizer1().setM((double[][]) in.readObject());
    		trainer.getOptimizer1().setV((double[][]) in.readObject());
    		trainer.getOptimizer1().setT(in.readLong());
    		// Adam 2
    		trainer.getOptimizer2().setM((double[][]) in.readObject());
    		trainer.getOptimizer2().setV((double[][]) in.readObject());
    		trainer.getOptimizer2().setT(in.readLong());
    		// Adam Dense
    		trainer.getOptimizerDense().setM((double[][]) in.readObject());
    		trainer.getOptimizerDense().setV((double[][]) in.readObject());
    		trainer.getOptimizerDense().setT(in.readLong());

    		System.out.println("Session resumed from: " + filePath);
    		return true;

    	} catch (IOException | ClassNotFoundException e) {
    		e.printStackTrace();
    		return false;
    	}
    }

    // NEW FUNCTIONS BELOW

    public static void saveGraphList(CustomArrayList<GCNGraph> graphList, String sessionName) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = SAVE_FOLDER + "/" + sessionName + "_graphs_" + timestamp + ".dat";

            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
                out.writeObject(graphList);
            }

            System.out.println("Graphs saved to: " + fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static CustomArrayList<GCNGraph> loadGraphList(String graphFilePath) {
        File file = new File(graphFilePath);
        if (!file.exists()) {
            System.out.println("Graph file does not exist: " + graphFilePath);
            return null;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object object = in.readObject();

            // Check if it's a single GCNGraph or a list
            if (object instanceof GCNGraph) {
                // If it's a single GCNGraph, wrap it in a list
                CustomArrayList<GCNGraph> graphList = new CustomArrayList<>();
                graphList.add((GCNGraph) object);
                System.out.println("Single graph loaded from: " + graphFilePath);
                return graphList;
            } else if (object instanceof CustomArrayList<?>) {
                // If it's already a List<GCNGraph>, cast it properly
                CustomArrayList<GCNGraph> graphList = (CustomArrayList<GCNGraph>) object;
                System.out.println("Graphs loaded from: " + graphFilePath);
                return graphList;
            } else {
                System.out.println("Unexpected object type: " + object.getClass());
                return null;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}





