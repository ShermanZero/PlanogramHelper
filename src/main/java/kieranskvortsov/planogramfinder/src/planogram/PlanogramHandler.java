package kieranskvortsov.planogramfinder.src.planogram;

import java.util.ArrayList;
import java.util.Optional;
import kieranskvortsov.planogramfinder.src.Processor.SearchType;
import kieranskvortsov.planogramfinder.src.item.Item;

/**
 *
 * @author      Kieran Skvortsov
 * @employee#   72141
 * 
 * @version     1.0
 * @date        08.29.2022
 */
public class PlanogramHandler extends ArrayList<Planogram> {
    
    public void addPlanogram(Planogram p) {
        this.add(p);
    }
    
    public boolean containsAny(String SKU) {
        return this.stream().anyMatch(p -> p.containsItem(SKU));
    }
    
    public Item getItem(String SKU) {
        Optional<Planogram> planogram = this.stream().filter(p -> p.containsItem(SKU)).findAny();
                
        return planogram.isPresent() ? planogram.get().getItem(SKU) : null;
    }
    
    public ArrayList<Item> getItems(String query, SearchType searchType) {
        ArrayList<Item> itemsFound = new ArrayList<>();
        
        this.stream().forEach(p -> {
            itemsFound.addAll(p.getItemsByQuery(query, searchType));
        });
        
        return itemsFound.isEmpty() ? null : itemsFound;
    }
}
