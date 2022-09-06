package pf.trackable;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JPasswordField;

/**
 *
 * @author Admin
 */
public class TrackablePasswordField extends JPasswordField implements Trackable<String> {

    private char[] trackedValue;
    
    public TrackablePasswordField() {
        watch();
    }
    
    @Override
    public void watch() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                trackedValue = getPassword();
            }
        });
    }

    @Override
    public String getValue() {
        if(trackedValue == null) return null;
        
        return new String(trackedValue);
    }

    @Override
    public void loadValue(Object value) {
        trackedValue = String.valueOf(value).toCharArray();
        setText(String.valueOf(value));
    }

    @Override
    public String wrapToProperty() {
        return getValue();
    }
    
}