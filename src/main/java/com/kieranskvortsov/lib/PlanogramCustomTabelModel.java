
package com.kieranskvortsov.lib;

import java.io.File;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author      Kieran Skvortsov
 * @employee#   72141
 * 
 * @version     1.0
 * @date        08.29.2022
 */
public class PlanogramCustomTabelModel extends DefaultTableModel {
    
    
    public PlanogramCustomTabelModel() {
        setColumnIdentifiers(new Object[] {"INCLUDE", "PLANOGRAM"});
    }
    
    public void addPlanogram(File f) {
        super.addRow(new Object[] {true, f.getAbsolutePath()});
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
    
    public void updateTable() {
        fireTableDataChanged();
    }
}
