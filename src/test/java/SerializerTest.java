
import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import pf.Serializer;
import pf.item.Item;


/**
 *
 * @author      Kieran Skvortsov
 * employee#    72141
 */
public class SerializerTest {
    
    private static ArrayList<Item> items;
    
    @Test
    public void testSerialization() {
        Serializer.serializeItems(items);
    }
    
    @Test
    public void testDeserialization() {
        ArrayList<Item> items = Serializer.deserializeItems();
        
        Assertions.assertTrue(testEqual(this.items, items));
    }
    
    private boolean testEqual(ArrayList<Item> items1, ArrayList<Item> items2) {
        if(items1.size() != items2.size()) return false;
        
        for(int i = 0; i < items1.size(); i++)
            if(!items1.get(i).equals(items2.get(i))) return false;
        
        return true;
    }
    
    @BeforeAll
    public static void createItems() {
        items = new ArrayList<>();

        Item item = null;
        for(int i = 0; i < 5; ++i) {
            item = new Item(0, 
                    String.valueOf((int)(Math.random()*10000000f)), 
                    "N/A", 
                    String.valueOf((int)(Math.random()*10000000000f)), 
                    0, false, "test");
            
            items.add(item);
        }
    }
}
