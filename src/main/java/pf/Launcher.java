
package pf;

import com.formdev.flatlaf.FlatDarkLaf;
import java.io.IOException;
import java.util.Properties;
import pf.gui.GUI;

/**
 *
 * @author      Kieran Skvortsov
 * @employee#   72141
 * 
 * @version     1.0
 * @date        08.29.2022
 */
public class Launcher {
    
    private final Properties props = new Properties();
    
    public static String APP_ARTIFACTID;
    public static String APP_VERSION;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            new Launcher();
        } catch (IOException ex) {
            System.err.println(ex);
            System.exit(1);
        }
    }
    
    private Launcher() throws IOException {
        FlatDarkLaf.setup();
        
        props.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        
        APP_VERSION     = props.getProperty("version");
        APP_ARTIFACTID  = props.getProperty("artifactId");
        
        System.out.println(String.format("%s v%s © 2022 Kieran Skvortsov", APP_ARTIFACTID, APP_VERSION));
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
}
