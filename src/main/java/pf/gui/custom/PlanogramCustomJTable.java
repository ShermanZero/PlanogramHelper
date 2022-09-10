package pf.gui.custom;

import java.awt.Font;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import pf.Processor;
import pf.planogram.Planogram;
import pf.planogram.PlanogramCustomTableModel;

/**
 * This class represent a custom JTable used for displaying and dealing with
 * Planograms
 * 
 * @author      Kieran Skvortsov <br>
 * employee#    72141
 */
public class PlanogramCustomJTable extends JTable {
    
    private static PlanogramCustomTableModel planogramCTM;
    
    public PlanogramCustomJTable() {
        init();
    }
    
    private void init() {
        planogramCTM = new PlanogramCustomTableModel();
        
        setModel(planogramCTM);
        
        //force the last column to fill remaining space
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        //force width
        int[] widths = {60};
        for(int i = 0; i < widths.length; ++i) {
            getColumnModel().getColumn(i).setMinWidth(widths[i]);
            getColumnModel().getColumn(i).setMaxWidth(widths[i]);
        }
        
        //prevent resizing
        for(int i = 0; i < getColumnCount(); ++i)
            getColumnModel().getColumn(i).setResizable(false);
        
        Font f = new Font(Font.MONOSPACED, Font.BOLD, 12);
        setFont(f);
        getTableHeader().setFont(f);
    }
    
    public void addFile(Runnable callback) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "PDF Files", "pdf");
        fileChooser.setFileFilter(filter);
        int returnVal = fileChooser.showOpenDialog(null);

        File f = null;
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            f = fileChooser.getSelectedFile();
            Processor.startParsing(f, () -> {
                callback.run();
            });
        
            planogramCTM.addPlanogram(f);
        }
    }
    
    public void addPlanogram(Planogram p) {
        planogramCTM.addPlanogram(p.getName());
        planogramCTM.updateTable();
    }
    
    public void clear() {
        planogramCTM.setRowCount(0);
        planogramCTM.updateTable();
    }
    
}
