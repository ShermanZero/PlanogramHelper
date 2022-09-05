
package pf.item;

import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author      Kieran Skvortsov
 * employee#   72141
 */
public class ItemCustomTableModel extends DefaultTableModel {
    
    //ArrayList to hold items we do not want to clear
    private ArrayList<String> itemsKeptSKUs;
    
    //Sets up the default configuration for the table
    public ItemCustomTableModel() {
        setColumnIdentifiers(new Object[] {"PRINT", "SKU", "UPC", "DESCRIPTION", "FIXTURE", "NAME"});
        itemsKeptSKUs = new ArrayList<>();
    }
    
    /**
     * Adds an item to the table model
     * 
     * @param i Item to add
     */
    public void addItem(Item i) {
        if(itemsKeptSKUs.contains(i.getSKU())) return;
        
        super.addRow(new Object[] {false, i.getSKU(), i.getUPC(), i.getDescription(), i.getFixture(), i.getName()});
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
     * Clears the table but keeps any items selected
     * 
     */
    public void clearTable(boolean keepSelected) {
        itemsKeptSKUs.clear();
        
        if(!keepSelected) {
            setRowCount(0);
        } else {
            for(int i = 0; i < getRowCount(); ++i) {
                boolean keep = (boolean)getValueAt(i, 0);

                if(keep) {
                    itemsKeptSKUs.add((String)getValueAt(i, 1));
                    continue;
                }

                removeRow(i);
                --i;
            }
        }
        
        updateTable();
    }
    
    /**
     * Returns an ArrayList of SKUs of items kept
     * 
     * @return 
     */
    public ArrayList<String> getItemsKept() {
        return itemsKeptSKUs;
    }
    
    /**
     * Forces an update to the UI
     * 
     */
    public void updateTable() {
        fireTableDataChanged();
    }
    
}
