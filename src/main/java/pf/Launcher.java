
package pf;

import com.formdev.flatlaf.FlatDarkLaf;
import java.io.IOException;
import java.util.Properties;
import pf.gui.Main;

/**
 * Main class responsible for launching the program.
 * 
 * @author      Kieran Skvortsov
 * employee#    72141
 */
public class Launcher {
    
    private static final Properties props = new Properties();
    
    public static String APP_ARTIFACTID;
    public static String APP_VERSION;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        UserSettings.load();
        
        try {
            new Launcher();
        } catch (IOException ex) {
            System.err.println(ex);
            System.exit(1);
        }
    }
    
    /**
     * Sets up the UI look and feel, loads internal and local properties,
     * and queues the main window for display
     * 
     * @throws IOException 
     */
    private Launcher() throws IOException {
        FlatDarkLaf.setup();
        
        //Loads internal properties packaged in .jar
        props.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        
        //These two properties are written to the internal properties file by Maven on build
        APP_ARTIFACTID = props.getProperty("artifactId");
        APP_VERSION    = props.getProperty("version");
        
        //Credit given where credit due ;)
        System.out.println(String.format("%s v%s © 2022 Kieran Skvortsov", APP_ARTIFACTID, APP_VERSION));
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
}
