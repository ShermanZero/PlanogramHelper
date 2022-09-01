
package pf.planogram;

import java.util.ArrayList;
import java.util.Collection;
import pf.Processor;
import pf.item.Item;
import pf.item.ItemHashMap;

/**
 *
 * @author      Kieran Skvortsov
 * @employee#   72141
 * 
 * @version     1.0
 * @date        08.29.2022
 */
public class Planogram {
    
    private ItemHashMap itemsInPlanogram = new ItemHashMap();
    private final String name;
    
    private boolean includedInSearch;
    
    public Planogram(String name) {
        this.name = name;
    }
    
    public void addItems(Collection<Item> c) {
        itemsInPlanogram.putAll(c);
    }
    
    public boolean containsItem(String SKU) {
        return itemsInPlanogram.containsItem(SKU);
    }
    
    public ArrayList<Item> getItemsByQuery(String query, Processor.SearchType searchType) {
        return itemsInPlanogram.findByQuery(query, searchType);
    }
    
    public Item getItem(String SKU) {
        return itemsInPlanogram.getItem(SKU);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append(name);
        sb.append(itemsInPlanogram.toString());
        
        return sb.toString();
    }
}
