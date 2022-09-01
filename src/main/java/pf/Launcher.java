
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
 * @employee#   72141
 */
public class Launcher {
    
    private static final Properties props = new Properties();
    private static final Properties props_local = new Properties();
    
    public static String APP_ARTIFACTID;
    public static String APP_VERSION;
    public static boolean APP_UPLOAD_PLANOGRAMS_ON_LAUNCH;
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
    
    private Launcher() throws IOException {
        FlatDarkLaf.setup();
        
        File localProps = new File("project.properties");
        if(localProps.exists()) {
            props_local.load(new FileInputStream(localProps));
            
            APP_UPLOAD_PLANOGRAMS_ON_LAUNCH = Boolean.parseBoolean(
                    props_local.getProperty("uploadPlanogramsOnLaunch")
                );
            APP_PLANOGRAMS = props_local.getProperty("planograms");
        }
        
        props.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
        
        APP_VERSION                     = props.getProperty("version");
        APP_ARTIFACTID                  = props.getProperty("artifactId");
        
        System.out.println(String.format("%s v%s © 2022 Kieran Skvortsov", APP_ARTIFACTID, APP_VERSION));
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
    
    public static void writeProperties(boolean uploadOnLaunch, String planograms) {
        APP_UPLOAD_PLANOGRAMS_ON_LAUNCH = uploadOnLaunch;
        APP_PLANOGRAMS = planograms;
        
        props_local.setProperty("uploadPlanogramsOnLaunch", 
                Boolean.toString(APP_UPLOAD_PLANOGRAMS_ON_LAUNCH));
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
