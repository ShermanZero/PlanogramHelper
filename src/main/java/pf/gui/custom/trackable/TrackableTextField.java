package pf.gui.custom.trackable;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JTextField;

/**
 *
 * @author Admin
 */
public class TrackableTextField extends JTextField implements Trackable<String> {

    private String trackedValue;
    
    public TrackableTextField() {
        watch();
    }
    
    @Override
    public void watch() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent evt) {
                trackedValue = getText();
            }
        });
    }

    @Override
    public String getValue() {
        return trackedValue;
    }

    @Override
    public void loadValue(Object value) {
        trackedValue = String.valueOf(value);
        setText(trackedValue);
    }

    @Override
    public String wrapToProperty() {
        return trackedValue;
    }
    
}
