package pf.gui.custom.trackable;

import javax.swing.JCheckBox;

/**
 *
 * @author      Kieran Skvortsov
 * employee#    72141
 */
public final class TrackableCheckBox extends JCheckBox implements Trackable<Boolean> {

    private boolean trackedValue;
    
    public TrackableCheckBox() {
        watch();
    }
    
    @Override
    public void watch() {
        addActionListener((evt) -> {
            trackedValue = isSelected();
        });
    }

    @Override
    public Boolean getValue() {
        return trackedValue;
    }

    @Override
    public void loadValue(Object value) {
        trackedValue = Boolean.parseBoolean(String.valueOf(value));
        setSelected(trackedValue);
    }

    @Override
    public String wrapToProperty() {
        return Boolean.toString(trackedValue);
    }
}
