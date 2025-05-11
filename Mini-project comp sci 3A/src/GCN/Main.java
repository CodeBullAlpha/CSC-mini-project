package GCN;

import javax.swing.SwingUtilities;

public class Main 
{
    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(() -> new GCNTrainerGUI().setVisible(true));
    }
}
