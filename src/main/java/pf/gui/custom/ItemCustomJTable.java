package pf.gui.custom;

import java.awt.Color;
import java.awt.Font;
import java.awt.print.PrinterException;
import javax.swing.JTable;
import javax.swing.JTextPane;
import pf.Processor;
import pf.item.Item;
import pf.item.ItemCustomTableModel;
import pf.planogram.Planogram;

/**
 * This class represent a custom JTable used for displaying and dealing with
 * Items
 * 
 * @author Kieran Skvortsov <br>
 * employee# 72141
 */
public class ItemCustomJTable extends JTable {

    private ItemCustomTableModel itemCTM;

    public ItemCustomJTable() {
        init();
    }

    private void init() {
        itemCTM = new ItemCustomTableModel();

        setModel(itemCTM);

        //force the last column to fill remaining space
        setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        //force width
        int[] widths = {60, 75, 110};
        for (int i = 0; i < widths.length; ++i) {
            getColumnModel().getColumn(i).setMinWidth(widths[i]);
            getColumnModel().getColumn(i).setMaxWidth(widths[i]);
        }

        //prevent resizing
        for (int i = 0; i < getColumnCount(); ++i) {
            getColumnModel().getColumn(i).setResizable(false);
        }
        
        Font f = new Font(Font.MONOSPACED, Font.BOLD, 12);
        setFont(f);
        getTableHeader().setFont(f);
    }

    public void search(String selection, String query) {
        Item.SearchType type = null;

        if (selection.equals("UPC")) {
            type = Item.SearchType.UPC;
        } else if (selection.equals("SKU")) {
            type = Item.SearchType.SKU;
        } else if (selection.equals("WORD")) {
            type = Item.SearchType.WORD;
        }

        Item[] itemsFound = Processor.search(query, type);

        if (itemsFound == null) {
            return;
        }

        //clear the table, keeping selected items
        itemCTM.clearTable(true);

        //add all items found to the table
        for (Item i : itemsFound) {
            itemCTM.addItem(i);
        }

        //update the table (force redraw)
        itemCTM.updateTable();
    }

    public void clear(boolean keepSelected) {
        itemCTM.clearTable(keepSelected);
    }

    /**
     * Generates a simple internal and temporary JTextPane with item information
     * and attempts to print it
     *
     */
    public void printSheet() {
        itemCTM.clearTable(true);

        if (itemCTM.getItemsKept().isEmpty()) {
            System.out.println("Nothing selected to print");
            return;
        }

        JTextPane printPane = new JTextPane();
        printPane.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        printPane.setForeground(Color.black);
        printPane.setBackground(Color.white);

        printPane.setText(Processor.getPrintableSheet(itemCTM.getItemsKept()));

        try {
            if (printPane.print()) {
                System.out.println("File printed successfully");
            }
        } catch (PrinterException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void addPlanogram(Planogram p) {
        itemCTM.addPlanogram(p);
    }

}
