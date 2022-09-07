
package pf.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import pf.item.Item.SearchType;

/**
 *
 * @author      Kieran Skvortsov
 * employee#   72141
 */
public class ItemHashMap extends HashMap<String, Item> {
    
    /**
     * Iterates through a collection of items and
     * adds them to the HashMap by [Item SKU, Item]
     * 
     * @param c Collection of Item objects
     */
    public void putAll(Collection<Item> c) {
        c.stream().forEach(item -> {
            this.put(item.getSKU(), item);
        });
    }
    
    /**
     * Adds an item to the HashMap
     * 
     * @param i Item object
     */
    public void put(Item i) {
        this.put(i.getSKU(), i);
    }
    
    /**
     * Returns whether the HashMap contains an Item
     * based on its SKU
     * 
     * @param SKU The SKU to search for
     * @return If the HashMap contains the SKU
     */
    public boolean containsItem(String SKU) {
        return this.containsKey(SKU);
    }
    
    /**
     * Returns the Item object from the HashMap
     * matching the SKU parameter
     * 
     * @param SKU The SKU to search for
     * @return An Item object with the SKU
     */
    public Item getItem(String SKU) {
        return this.get(SKU);
    }
    
    /**
     * Queries the HashMap by a specified SearchType
     * and returns an ArrayList of Item objects matching
     * the query
     * 
     * @param query Query to search through the HashMap for
     * @param searchType How to query for the items
     * @return An ArrayList of Item objects satisfying the query
     */
    public ArrayList<Item> findByQuery(String query, SearchType searchType) {
        ArrayList<Item> results = new ArrayList<Item>();
        
        this.keySet().stream().forEach(SKU -> {
            Item i = this.get(SKU);
            
            String UPC  = i.getUPC();
            String desc = i.getDescription().toLowerCase();
            
            if(searchType == SearchType.SKU && SKU.endsWith(query)) 
                results.add(i);
            else
            if(searchType == SearchType.UPC && UPC.endsWith(query)) 
                results.add(i);
            else
            if(searchType == SearchType.WORD && desc.contains(query)) 
                results.add(i);
        });
        
        return results;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        this.keySet().stream().forEach(SKU -> {
            sb.append(this.get(SKU).toString());
        });
        
        return sb.toString();
    }
}
