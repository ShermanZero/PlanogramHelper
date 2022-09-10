package pf;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.FindIterable;
import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.client.result.InsertOneResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import org.bson.Document;
import pf.gui.Main;
import pf.item.Item;

/**
 *
 * @author      Kieran Skvortsov
 * employee#    72141
 */
public class MongoDBConnection {
    
    private final String clusterName = "Main";
    private final String databaseName = "PlanogramHelper";
    
    private MongoCollection<Document> collection;
    
    private Thread pullThread;
    
    //force connection on instance creation
    public MongoDBConnection() {
        connect();
    }
    
    private void connect() {
        String clusterUser;
        String clusterPass;
        
        //check if custom authentication has been enabled in the UI
        if(Boolean.parseBoolean(UserSettings.getProperty("auth.custom"))) {
            clusterUser = UserSettings.getProperty("auth.user");
            clusterPass = UserSettings.getProperty("auth.pass");
        //check if authentication was passed as via command-line as a property
        } else {
            clusterUser = System.getProperty("db.user", null);
            clusterPass = System.getProperty("db.pass", null);
        }
        
        //revert to default read-only user if either user or pass is null
        if(clusterUser == null || clusterPass == null) {
            clusterUser = "read-only";
            clusterPass = "read-only";
        }
        
        //create the connection string
        StringBuilder sb = new StringBuilder("mongodb+srv://");
        sb.append(clusterUser);
        sb.append(":");
        sb.append(clusterPass);
        sb.append("@");
        sb.append(clusterName);
        sb.append(".syvegsj.mongodb.net/?retryWrites=true&w=majority");
        
        //create a connection to mongo
        MongoClient mongoClient = MongoClients.create(sb.toString());

        //fetch the database
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        
        //assign the collection to the instance variable
        collection = database.getCollection("Items");
    }
    
    /**
     * Finds all documents containing a query
     * 
     * @param queryBy The String to query by (e.g. 'SKU', 'UPC', 'DESC')
     * @param queryWith The String to query with (e.g. '302', 'PV') 
     * @return All documents found matching the query
     */
    private FindIterable<Document> findAllContaining(String queryBy, String queryWith) {
        return collection.find(regex(queryBy, queryWith, "i"));
    }
    
    /**
     * Unpacks the somewhat annoying FindIterable object into a more-easily
     * workable ArrayList of Documents.
     * 
     * @param iterable The FindIterable object to unpack
     * @return The ArrayList
     */
    private ArrayList<Document> unpack(FindIterable<Document> iterable) {
        ArrayList<Document> documents = new ArrayList<>();
        
        for(Document d : iterable)
            documents.add(d);
        
        if(documents.isEmpty())
            System.out.println("No items found");
        
        return documents;
    }
    
    /**
     * Unpacks the somewhat annoying FindIterable object into a more-easily
     * workable ArrayList of specified type, only including relevant information.
     * 
     * @param iterable The FindIterable object to unpack
     * @param via What the ArrayList will consists of
     * @param T What type of data the ArrayList will consist of
     * @return The ArrayList
     */
    private <T> ArrayList<T> unpack(FindIterable<Document> iterable, 
            String via, Class T) {
        ArrayList<T> documents = new ArrayList<>();
        
        for(Document d : iterable)
            documents.add((T)d.get(via, T));
        
        if(documents.isEmpty())
            System.out.println("No items found");
        
        return documents;
    }
    
    /**
     * Fetches all items from the database.  Synchronized to avoid multi-threaded
     * calls
     * 
     * @param callback The callback method that accepts an ArrayList of Items
     */
    public synchronized void pullAllItems(Consumer<ArrayList<Item>> callback) {
        if(pullThread != null && pullThread.isAlive())
            try { pullThread.join(); } catch (InterruptedException ex) {}
        
        //creates a thread to download all the Items from the database
        pullThread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Item> items = new ArrayList<>();

                //number of documents in the collection
                int numItems = (int)collection.countDocuments();
                
                System.out.println(String.format("Fetching %d documents from database", numItems));
                
                //current document we're parsing
                int currDoc = 0;
                
                //pulls all Documents
                FindIterable<Document> allDocs = collection.find();
                
                //convert each Document into an Item
                for(Document d : allDocs) {
                    Item i = new Item(
                            d.get("Position", Integer.class), 
                            d.get("SKU", String.class), 
                            d.get("DESC", String.class), 
                            d.get("UPC", String.class), 
                            0, //TODO: add facings implementation
                            false
                        );

                    i.setFixture(d.get("Fixture").toString());
                    i.setName(d.get("Name").toString());

                    items.add(i);
                    
                    Main.updateProgress(++currDoc, numItems);
                }
                
                Main.setProgress(100);
                
                //small delay before updating the progressbar
                try { 
                    Thread.sleep(100); 
                } catch (InterruptedException ex) { System.err.println(ex); }
                
                Main.setProgress(0);
                
                //accept the callback and return the found Items
                callback.accept(items);
            }
        });
        
        pullThread.start();
    }
    
    /**
     * A very inefficient lookup that queries the database for all documents,
     * and then finds if any have a SKU that match the parameter.
     * 
     * TODO: Fetch master list of documents at start of program, and store into
     * a temporary instance variable (e.g. HashMap) that can be referenced locally
     * instead of needing a remote fetch every time.
     * 
     * @param SKU The SKU to search for
     * @return 
     */
    private boolean itemAlreadyUploaded(String SKU) {
        return !unpack(findAllContaining("SKU", SKU)).isEmpty();
    }
    
    /**
     * Uploads (inserts) a singular Item object to the database
     * 
     * @param i The Item to upload
     */
    public void uploadItem(Item i) {
        if(itemAlreadyUploaded(i.getSKU())) {
            System.err.println("Can not upload item, already exists");
            return;
        }
        
        Document d = convertToDocument(i);
        
        InsertOneResult result = collection.insertOne(d);
        System.out.println("Inserted a document with the following id: " 
            + result.getInsertedId().asObjectId().getValue());
    }
    
    /**
     * Uploads (inserts) multiple Item objects to the database through
     * a bulk write command.  Much more efficient than an iterative upload.
     * 
     * @param items A Collection of the Item objects to upload
     */
    public void uploadItems(Collection<Item> items) {
        ArrayList<WriteModel<Document>> bulkItems = new ArrayList<>();
        ArrayList<String> currentlyUploadedSKUs = unpack(collection.find(), 
                "SKU", String.class);
        
        Thread uploadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int count = 0;
                    int total = items.size()+1;
                    
                    for(Item i : items) {
                        if(!currentlyUploadedSKUs.contains(i.getSKU())) {
                            bulkItems.add(new InsertOneModel<>(convertToDocument(i)));
                        } else {
                            total -= 1;
                        }
                        
                        Main.updateProgress(++count, total);
                    }
                    
                    if(!bulkItems.isEmpty())
                        collection.bulkWrite(bulkItems);
                } catch (MongoBulkWriteException ex) {
                    System.err.println(ex);
                }
                
                Main.setProgress(0);
            }
        });
        
        uploadThread.start();
    }
    
    /**
     * Development purposes only - wipes the database.
     */
    public void deleteAll() {
        collection.deleteMany(regex("DESC", ""));
    }
    
    /**
     * Converts an Item object to an upload-able Document
     * 
     * @param i The Item object to convert
     * @return 
     */
    private Document convertToDocument(Item i) {
        Document doc = new Document();
        doc.append("DESC", i.getDescription());
        doc.append("SKU", i.getSKU());
        doc.append("UPC", i.getUPC());
        doc.append("Fixture", i.getFixture());
        doc.append("Name", i.getName());
        doc.append("Position", i.getPosition());
        
        return doc;
    }
    
}
