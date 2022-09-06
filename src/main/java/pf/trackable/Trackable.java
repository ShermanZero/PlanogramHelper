package pf.trackable;

/**
 *
 * @author      Kieran Skvortsov
 * employee#    72141
 * 
 * An interface implementable by Components that need
 * to be tracked.  Meaning, when their value changes,
 * an action should be taken (mainly updating a local
 * variable to match the new value).  Trackable components
 * are used for saving/loading user settings.
 */
public interface Trackable<T> {
    public String getName();
    
    /**
     * Watches for any user-defined relevant changes to the component
     */
    public void watch();
    
    /**
     * Returns the class-typed value of the Component
     * 
     * @param <T> The class-typed value
     * @return 
     */
    public <T> T getValue();
    
    /**
     * Loads a value into the Component
     * 
     * @param value 
     */
    public void loadValue(Object value);
    
    /**
     * Wraps whatever tracked value/data the component has into
     * a String that can be loaded via {@link loadValue}
     * 
     * @return 
     */
    public String wrapToProperty();
}
