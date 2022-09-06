package pf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import pf.trackable.Trackable;
import pf.trackable.UnnamedException;

/**
 *
 * @author      Kieran Skvortsov
 * employee#    72141
 */
public final class UserSettings {
    
    private final static Properties props_local = new Properties();
    private final static ArrayList<Trackable> tracked = new ArrayList<>();
    
    private static boolean loaded = false;
    
    //Load the properties before anything else can happen
    static {
        load();
    }

    //pevent class instantiation
    private UserSettings() {}
    
    /**
     * Assign a component to be tracked by UserSettings, which will have its
     * value automatically saved/loaded to local properties.
     * 
     * @param trackable A Trackable component
     * @throws UnnamedException 
     */
    public static void track(Trackable trackable) throws UnnamedException {
        String name = trackable.getName();
        
        if(name == null)
            throw new UnnamedException();
        
        //if the Trackable object is not already part of the ArrayList holding
        //  trackable objects for later read/write usage
        if(!tracked.contains(trackable))
            tracked.add(trackable);
        
        //if the local properties shows that this component has been saved
        //  load the value from properties into the component
        if(props_local.containsKey(name))
            trackable.loadValue(props_local.get(name));
    }
    
    /**
     * Return the value of a property
     * 
     * @param propertyName The property's name
     * @return The property's value or null, if it does not exist
     */
    public static String getProperty(String propertyName) {
        return props_local.getProperty(propertyName, null);
    }
     
    /**
     * Loads the local properties file.  Called upon first reference to
     * the class.
     */
    protected static void load() {
        if(loaded) return;
        
        File localProps = new File("project.properties");
        
        //Loads properties (if they exist)
        if(localProps.exists()) {
            try {
                props_local.load(new FileInputStream(localProps));
                loaded = true;
            } catch (IOException ex) { System.err.println(ex); }
        }
    }
    
    /**
     * Saves all Trackable components to the local properties file
     * for subsequent loading
     */
    public static void save() {
        //clear any properties (in event of unused properties)
        props_local.clear();
        
        //write each trackable component's name and its converted property value
        tracked.forEach((trackable) -> {
            props_local.put(trackable.getName(), trackable.wrapToProperty());
        });
        
        //clear the tracked components
        tracked.clear();
        
        //write to file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("project.properties");
            props_local.store(fos, null);
        } catch (IOException ex) { System.err.println(ex); }
        
        try {
            if(fos != null) fos.close();
        } catch (IOException ex) { System.err.println(ex); }
    }
}
