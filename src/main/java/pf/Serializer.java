package pf;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import pf.item.Item;

/**
 *
 * @author      Kieran Skvortsov
 * employee#    72141
 */
public final class Serializer {
    
    /**
     * The file name/path to write out to
     */
    private static final String FILE_NAME = "PlanogramHelper.ser";
    
    //prevent class instantiation
    private Serializer() {}
    
    /**
     * Serializes an ArrayList of Item objects to the file {@link FILE_NAME}
     * 
     * @param items The ArrayList of Item objects
     */
    public static void serializeItems(ArrayList<Item> items) {
        FileOutputStream fileOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        
        try {
            fileOutputStream = new FileOutputStream(FILE_NAME);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            
            for(Item i : items) {
                objectOutputStream.writeObject(i);
                objectOutputStream.flush();
            }
            
        } catch (IOException ex) { 
            System.err.println(ex);
        } finally {
            
            try {
                fileOutputStream.close();
            } catch (IOException ex) { System.err.println(ex); }
        }
    }
    
    /**
     * Deserializes the Item objects in the file {@link FILE_NAME} and
     * returns them contained within an ArrayList
     * 
     * @return The ArrayList of deserialized Item objects
     */
    public static ArrayList<Item> deserializeItems() {
        ArrayList<Item> items = new ArrayList<>();
        
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInputStream = null;
        
        try {
            fileInputStream = new FileInputStream(FILE_NAME);
            objectInputStream = new ObjectInputStream(fileInputStream);
            
            Object obj;
            while((obj = objectInputStream.readObject()) != null) {
                items.add((Item)obj);
            }
            
        } catch (IOException | ClassNotFoundException ex) { 
            System.err.println(ex);
        } finally {
            
            try {
                fileInputStream.close();
            } catch (IOException ex) { System.err.println(ex); }
        }
        
        return items;
    }
    
    
}
