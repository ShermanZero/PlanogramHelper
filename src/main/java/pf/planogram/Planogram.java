
package pf.planogram;

import java.util.ArrayList;
import java.util.Collection;
import pf.item.Item;
import pf.item.Item.SearchType;
import pf.item.ItemHashMap;

/**
 *
 * @author      Kieran Skvortsov
 * employee#   72141
 */
public class Planogram {
    
    private ItemHashMap itemsInPlanogram = new ItemHashMap();
    private final String name;
    
    //not implemented - but what I would do here is allow selection of planograms
    //  to search by instead of all uploaded planograms at once
    private boolean includedInSearch;
    
    public Planogram(String name) {
        this.name = name;
    }
    
    /**
     * Adds a collection of Items to the planogram.  Pass-through.
     * 
     * @param c 
     */
    public void addItems(Collection<Item> c) {
        itemsInPlanogram.putAll(c);
    }
    
    /**
     * Returns if the planogram contains an Item by SKU.  Pass-through.
     * 
     * @param SKU
     * @return 
     */
    public boolean containsItem(String SKU) {
        return itemsInPlanogram.containsItem(SKU);
    }
    
    /**
     * Returns an ArrayList of Item objects by query
     * 
     * @param query The String to query with
     * @param searchType The SearchType to query by
     * @return 
     */
    public ArrayList<Item> getItemsByQuery(String query, SearchType searchType) {
        return itemsInPlanogram.findByQuery(query, searchType);
    }
    
    public ArrayList<Item> getAllItems() {
        ArrayList<Item> items = new ArrayList<>();
        
        itemsInPlanogram.forEach((_sku,i) -> {
            items.add(i);
        });
        
        return items;
    }
    
    /**
     * Returns an Item by SKU.  Pass-through.
     * 
     * @param SKU
     * @return 
     */
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
