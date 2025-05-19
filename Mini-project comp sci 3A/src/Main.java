import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import WasteSimulation.WasteCollectionSimulation;

public class Main {
	public static void main(String[] args) {

		SwingUtilities.invokeLater(() -> {
			JFrame setupFrame = new JFrame("Simulation Setup");
			setupFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			// Show welcome screen first
			WasteCollectionSimulation simulation = new WasteCollectionSimulation();
			simulation.showWelcomeScreen();
		});
	}

}
