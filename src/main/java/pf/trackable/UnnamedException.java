package pf.trackable;

/**
 *
 * @author      Kieran Skvortsov
 * employee#    72141
 */
public class UnnamedException extends Exception {
    
    public UnnamedException() {
        super("You must assign a name to the component to track it");
    }
    
    public UnnamedException(String message) {
        super(message);
    }
    
}
