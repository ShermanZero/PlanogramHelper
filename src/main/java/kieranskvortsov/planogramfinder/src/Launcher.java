
package kieranskvortsov.planogramfinder.src;

import com.formdev.flatlaf.FlatDarkLaf;
import kieranskvortsov.planogramfinder.src.gui.GUI;

/**
 *
 * @author      Kieran Skvortsov
 * @employee#   72141
 * 
 * @version     1.0
 * @date        08.29.2022
 */
public class Launcher {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        FlatDarkLaf.setup();
       
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
                System.out.println("PlanogramFinder v0.1.0 © 2022 Kieran Skvortsov");
            }
        });
    }
}
