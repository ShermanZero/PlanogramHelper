
package com.kieranskvortsov.lib;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author      Kieran Skvortsov
 * @employee#   72141
 * 
 * @version     1.0
 * @date        08.29.2022
 */
public class ItemArrayList extends ArrayList<Item> {
    
    private ArrayList<String> SKUs = new ArrayList<>();
    
    @Override
    public boolean add(Item i) {
        boolean a = super.add(i);
        return a && SKUs.add(i.getSKU());
    }
    
    @Override
    public boolean addAll(Collection c) {
        super.addAll(c);
        
        for(Object item : c)
            SKUs.add(((Item)item).getSKU());
        
        return true;
    }
    
    @Override
    public void clear() {
        super.clear();
        SKUs.clear();
    }
    
    public boolean containsItem(String SKU) {
        return SKUs.contains(SKU);
    }
    
    public Item getItem(String SKU) {
        for(Item i : this)
            if(i.getSKU().equals(SKU)) return i;
        
        return null;
    }
    
}
