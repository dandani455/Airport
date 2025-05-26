package airport.main;

import airport.AirportFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.UIManager;

public class Main {

    public static void main(String args[]) {
        System.setProperty("flatlaf.useNativeLibrary", "false");

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("No se pudo aplicar FlatDarkLaf: " + ex.getMessage());
        }

        java.awt.EventQueue.invokeLater(() -> {
            new AirportFrame().setVisible(true);
        });
    }
}
