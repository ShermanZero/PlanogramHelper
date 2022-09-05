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
import org.bson.Document;
import pf.gui.Main;
import pf.item.Item;

/**
 *
 * @author      Kieran Skvortsov
 * employee#   72141
 */
public class MongoDBConnection {
    
    private MongoCollection<Document> collection;
    
    public MongoDBConnection() {
        connect();
    }
    
    private void connect() {
        String clusterUser = System.getProperty("db.user");
        String clusterPass = System.getProperty("db.pass");
        String clusterName = "Main";
        String databaseName = "PlanogramHelper";
        
        if(clusterUser == null) clusterUser = "read-only";
        if(clusterPass == null) clusterPass = "read-only";
        
        StringBuilder sb = new StringBuilder("mongodb+srv://");
        sb.append(clusterUser);
        sb.append(":");
        sb.append(clusterPass);
        sb.append("@");
        sb.append(clusterName);
        sb.append(".syvegsj.mongodb.net/?retryWrites=true&w=majority");
        
        String uri = sb.toString();
        MongoClient mongoClient = MongoClients.create(uri);

        MongoDatabase database = mongoClient.getDatabase(databaseName);
        collection = database.getCollection("Items");
    }
    
    private FindIterable<Document> findAllContaining(String queryBy, String queryWith) {
        return collection.find(regex(queryBy, queryWith, "i"));
    }
    
    private ArrayList<Document> unpack(FindIterable<Document> iterable) {
        ArrayList<Document> documents = new ArrayList<>();
        
        for(Document d : iterable)
            documents.add(d);
        
        if(documents.isEmpty())
            System.out.println("No items found");
        
        return documents;
    }
    
    public ArrayList<Item> pullAllItems() {
        ArrayList<Item> items = new ArrayList<>();
        
        Thread pullThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Main.setProgress(10);
                
                FindIterable<Document> allDocs = findAllContaining("DESC", "");
                
                Main.setProgress(50);
        
                for(Document d : allDocs) {
                    Item i = new Item(
                            0, d.get("SKU").toString(), d.get("DESC").toString(), 
                            d.get("UPC").toString(), 0, false
                        );

                    i.setFixture(d.get("Fixture").toString());
                    i.setName(d.get("Name").toString());

                    items.add(i);
                }
                
                Main.setProgress(0);
            }
        });
        
        pullThread.start();
        
        try {
            pullThread.join();
        } catch (InterruptedException ex) {
            System.err.println(ex);
        }
        
        return items;
    }
    
    private boolean itemAlreadyUploaded(String SKU) {
        return !unpack(findAllContaining("SKU", SKU)).isEmpty();
    }
    
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
    
    public void uploadItems(ArrayList<Item> items) {
        ArrayList<WriteModel<Document>> bulkItems = new ArrayList<>();
        FindIterable<Document> currentlyUploaded = findAllContaining("DESC", "");
        
        Thread uploadThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int count = 0;
                    int total = items.size()+1;
                    
                    for(Item i : items) {
                        if(currentlyUploaded.filter(eq("SKU", i.getSKU())).first() == null) {
                            bulkItems.add(new InsertOneModel<>(convertToDocument(i)));
                        } else {
                            total -= 1;
                        }
                        
                        Main.updateProgress(++count, total);
                    }

                    collection.bulkWrite(bulkItems);
                } catch (MongoBulkWriteException ex) {
                    System.err.println(ex);
                }
                
                Main.setProgress(0);
            }
        });
        
        uploadThread.start();
    }
    
    public void deleteAll() {
        collection.deleteMany(regex("SKU", ""));
    }
    
    private Document convertToDocument(Item i) {
        Document doc = new Document();
        doc.append("DESC", i.getDescription());
        doc.append("SKU", i.getSKU());
        doc.append("UPC", i.getUPC());
        doc.append("Fixture", i.getFixture());
        doc.append("Name", i.getName());
        
        return doc;
    }
    
}
