package pf.planogram;

import java.util.ArrayList;
import java.util.Optional;
import pf.item.Item;
import pf.item.Item.SearchType;

/**
 *
 * @author      Kieran Skvortsov
 * employee#   72141
 */
public class PlanogramHandler extends ArrayList<Planogram> {
    
    /**
     * Adds a Planogram object to this ArrayList
     * 
     * @param p 
     */
    public void addPlanogram(Planogram p) {
        this.add(p);
    }
    
    /**
     * Returns if a SKU is contained within any planogram
     * 
     * @param SKU
     * @return 
     */
    public boolean containsAny(String SKU) {
        return this.stream().anyMatch(p -> p.containsItem(SKU));
    }
    
    /**
     * Returns an item based on SKU from any planogram
     * 
     * @param SKU
     * @return 
     */
    public Item getItem(String SKU) {
        Optional<Planogram> planogram = this.stream().filter(p -> p.containsItem(SKU)).findAny();
                
        return planogram.isPresent() ? planogram.get().getItem(SKU) : null;
    }
    
    /**
     * Returns an ArrayList of Items based on a query
     * 
     * @param query The String to query with
     * @param searchType The SearchType to query by
     * @return 
     */
    public ArrayList<Item> getItems(String query, SearchType searchType) {
        ArrayList<Item> itemsFound = new ArrayList<>();
        
        this.stream().forEach(p -> {
            itemsFound.addAll(p.getItemsByQuery(query, searchType));
        });
        
        return itemsFound;
    }
    
    public ArrayList<Item> getAllItems() {
        ArrayList<Item> items = new ArrayList<>();
        
        this.forEach(p -> {
            items.addAll(p.getAllItems());
        });
        
        return items;
    }
}
