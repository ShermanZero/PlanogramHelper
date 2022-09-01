
package pf.item;

import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author      Kieran Skvortsov
 * @employee#   72141
 * 
 * @version     1.0
 * @date        08.29.2022
 */
public class ItemCustomTableModel extends DefaultTableModel {
    
    private ArrayList<String> itemsKeptSKUs;
    
    
    public ItemCustomTableModel() {
        setColumnIdentifiers(new Object[] {"PRINT", "SKU", "UPC", "DESCRIPTION", "FIXTURE", "NAME"});
        itemsKeptSKUs = new ArrayList<>();
    }
    
    public void addItem(Item i) {
        if(itemsKeptSKUs.contains(i.getSKU())) return;
        
        super.addRow(new Object[] {false, i.getSKU(), i.getUPC(), i.getDescription(), i.getFixture(), i.getName()});
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        if(columnIndex == 0) return Boolean.class;
        return String.class;
    }
    
    @Override
    public boolean isCellEditable(int row, int column) {
        return column == 0;
    }
    
    public void clearTable() {
        itemsKeptSKUs.clear();
        
        for(int i = 0; i < getRowCount(); ++i) {
            boolean keep = (boolean)getValueAt(i, 0);
            
            if(keep) {
                itemsKeptSKUs.add((String)getValueAt(i, 1));
                continue;
            }
            
            removeRow(i);
            --i;
        }
        
        updateTable();
    }
    
    public ArrayList<String> getItemsKept() {
        return itemsKeptSKUs;
    }
    
    public void updateTable() {
        fireTableDataChanged();
    }
    
}
