
package pf;

import com.formdev.flatlaf.FlatDarkLaf;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import pf.gui.Main;

/**
 *
 * @author      Kieran Skvortsov
 * employee#   72141
 * 
 * Main class and backbone
 */
public class Launcher {
    
    private static final Properties props = new Properties();
    private static final Properties props_local = new Properties();
    
    public static String APP_ARTIFACTID;
    public static String APP_VERSION;
    public static boolean APP_UPLOAD_PLANOGRAMS_ON_LAUNCH;
    public static boolean APP_DOWNLOAD_PLANOGRAMS_ON_LAUNCH;
    public static String APP_PLANOGRAMS;
    
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
    
    /**
     * Sets up the UI look and feel, loads internal and local properties,
     * and queues the main window for display
     * 
     * @throws IOException 
     */
    private Launcher() throws IOException {
        FlatDarkLaf.setup();
        
        //Loads local properties (if they exist)
        File localProps = new File("project.properties");
        if(localProps.exists()) {
            props_local.load(new FileInputStream(localProps));
            
            APP_UPLOAD_PLANOGRAMS_ON_LAUNCH = Boolean.parseBoolean(
                    props_local.getProperty("uploadPlanogramsOnLaunch")
                );
            APP_DOWNLOAD_PLANOGRAMS_ON_LAUNCH = Boolean.parseBoolean(
                    props_local.getProperty("downloadPlanogramsOnLaunch")
                );
            APP_PLANOGRAMS = props_local.getProperty("planograms");
        }
        
        //Loads internal properties packaged in .jar
        props.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        
        //These two properties are written to the internal properties file by Maven on build
        APP_VERSION    = props.getProperty("version");
        APP_ARTIFACTID = props.getProperty("artifactId");
        
        System.out.println(String.format("%s v%s © 2022 Kieran Skvortsov", APP_ARTIFACTID, APP_VERSION));
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
    
    /**
     * Writes out the two properties modifiable in the settings window
     * 
     * @param uploadOnLaunch Whether or not to automatically upload planograms on launch
     * @param downloadOnLaunch Whether or not to automatically download planograms on launch
     * @param planograms List of absolute paths to planogram pdf files
     */
    public static void writeProperties(boolean uploadOnLaunch, boolean downloadOnLaunch, String planograms) {
        APP_UPLOAD_PLANOGRAMS_ON_LAUNCH = uploadOnLaunch;
        APP_DOWNLOAD_PLANOGRAMS_ON_LAUNCH = downloadOnLaunch;
        APP_PLANOGRAMS = planograms;
        
        props_local.setProperty("uploadPlanogramsOnLaunch", 
                Boolean.toString(APP_UPLOAD_PLANOGRAMS_ON_LAUNCH));
        
        props_local.setProperty("downloadPlanogramsOnLaunch", 
                Boolean.toString(downloadOnLaunch));
        
        props_local.setProperty("planograms", APP_PLANOGRAMS);
        
        FileOutputStream fos = null;
        
        try {
            fos = new FileOutputStream("project.properties");
            props_local.store(fos, null);
        } catch (IOException ex) { System.err.println(ex); }
        
        try {
            if(fos != null)
                fos.close();
        } catch (IOException ex) { System.err.println(ex); }
    }
}
