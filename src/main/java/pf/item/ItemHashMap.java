
package pf.item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;
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
        c.stream().forEach(i -> {
            this.put(i.getSKU()+getItemID(i), i);
        });
    }
    
    /**
     * Adds an item to the HashMap
     * 
     * @param i Item object
     */
    public void put(Item i) {
        this.put(i.getSKU()+getItemID(i), i);
    }
    
    /**
     * Creates and returns a unique ID for any Item.  This prevents
     * accidental duplicate removal in cases where the same product can
     * be assigned to multiple different locations in a planogram.
     * 
     * @param i The Item to get an ID for
     * @return A unique String ID for the Item
     */
    private String getItemID(Item i) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("@");
        sb.append(i.getPlanogramName());
        sb.append("+");
        sb.append(i.getFixture());
        sb.append("_");
        sb.append(i.getPosition());
        
        return sb.toString();
    }
    
    /**
     * Returns whether the HashMap contains an Item
     * based on its SKU
     * 
     * @param SKU The SKU to search for
     * @return If the HashMap contains the SKU
     */
    public boolean containsItem(String SKU) {
        Stream<String> results = this.keySet().stream().filter(id -> {
           return id.contains(SKU);
        });
        
        return results.count() != 0;
    }
    
    /**
     * Returns the Item object from the HashMap
     * matching the SKU parameter
     * 
     * @param SKU The SKU to search for
     * @return An Item object with the SKU
     */
    public Item getItem(String SKU) {
        Stream<String> results = this.keySet().stream().filter(id -> {
           String noID = id.split("@")[0];
           return noID.contains(SKU);
        });
        
        Optional<String> first = results.findFirst();
        String id = first.get();
        
        if(id == null) return null;
        return this.get(id);
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
        
        this.keySet().stream().forEach(id -> {
            Item i = this.get(id);
            String noIDSKU = id.split("@")[0];

            String UPC  = i.getUPC();
            String desc = i.getDescription().toLowerCase();
            
            if(searchType == SearchType.SKU && noIDSKU.endsWith(query)) 
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
