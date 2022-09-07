
package pf.planogram;

import java.io.File;
import javax.swing.table.DefaultTableModel;

/**
 * The PlanogramCustomTableModel class extends the DefaultTableModel and
 * sets up the custom view for the UI.
 * 
 * @author      Kieran Skvortsov
 * employee#    72141
 */
public class PlanogramCustomTableModel extends DefaultTableModel {
    
    
    public PlanogramCustomTableModel() {
        setColumnIdentifiers(new Object[] {"INCLUDE", "PLANOGRAM"});
    }
    
    /**
     * Adds a planogram to the table model (only the absolute path)
     * 
     * @param f Planogram PDF File 
     */
    public void addPlanogram(File f) {
        super.addRow(new Object[] {true, f.getAbsolutePath()});
    }
    
    /**
     * Adds a planogram to the table model by name
     * 
     * @param name Name of the planogram
     */
    public void addPlanogram(String name) {
        super.addRow(new Object[] {true, name});
    }
    
    // Forcing column 0 to be boolean and represented with a checkbox
    @Override
    public Class getColumnClass(int columnIndex) {
        if(columnIndex == 0) return Boolean.class;
        return String.class;
    }
    
    // Forcing only the first cell in each row to be editable
    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 0;
    }
    
    /**
     * Forces an update to the UI
     * 
     */
    public void updateTable() {
        fireTableDataChanged();
    }
}
