package pf;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoCommandException;
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
import java.util.HashMap;
import java.util.function.Consumer;
import org.bson.Document;
import pf.gui.Main;
import pf.item.Item;

/**
 * The MongoDBConnection class handles all backend processing with the remote
 * MongoDB database used for storing/updating/retrieving a master list of Items
 * 
 * @author Kieran Skvortsov employee# 72141
 */
public class MongoDBConnection {

    private final String clusterName = "Main";
    private final String databaseName = "PlanogramHelper";

    private MongoCollection<Document> collection;
    private HashMap<String, Item> remoteItems;

    private Thread pullThread;
    private Thread uploadThread;

    //force connection on instance creation
    public MongoDBConnection() {
        this(null, null);
    }
    
    public MongoDBConnection(String user, String pass) {
        connect(user, pass);
    }
    
    private void connect(String clusterUser, String clusterPass) {
        if(clusterUser == null || clusterPass == null 
                || clusterUser.length() == 0 || clusterPass.length() == 0) {
            //check if custom authentication has been enabled in the UI
            if (Boolean.parseBoolean(UserSettings.getProperty("auth.custom"))) {
                System.err.println("Using custom authentication");
                
                clusterUser = UserSettings.getProperty("auth.username");
                clusterPass = UserSettings.getProperty("auth.password");
            //check if authentication was passed as via command-line as a property
            } else {
                clusterUser = System.getProperty("db.user", null);
                clusterPass = System.getProperty("db.pass", null);
            }
            
            //revert to default read-only user if either user or pass is still null
            if (clusterUser == null || clusterPass == null) {
                clusterUser = "read-only";
                clusterPass = "read-only";
            }
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

        for (Document d : iterable) {
            documents.add(d);
        }

        if (documents.isEmpty()) {
            System.out.println("No items found");
        }

        return documents;
    }

    /**
     * Unpacks the somewhat annoying FindIterable object into a more-easily
     * workable ArrayList of specified type, only including relevant
     * information.
     *
     * @param iterable The FindIterable object to unpack
     * @param via What the ArrayList will consists of
     * @param T What type of data the ArrayList will consist of
     * @return The ArrayList
     */
    private <T> ArrayList<T> unpack(FindIterable<Document> iterable,
            String via, Class T) {
        ArrayList<T> documents = new ArrayList<>();

        for (Document d : iterable) {
            documents.add((T) d.get(via, T));
        }

        if (documents.isEmpty()) {
            System.out.println("No items found");
        }

        return documents;
    }

    /**
     * Fetches all items from the database. Synchronized to avoid multi-threaded
     * calls
     *
     * @param callback The callback method that accepts an ArrayList of Items
     */
    public synchronized void pullAllItems(Consumer<ArrayList<Item>> callback) {
        if (pullThread != null && pullThread.isAlive())
            try {
                pullThread.join();
            } catch (InterruptedException ex) {}

        //creates a thread to download all the Items from the database
        pullThread = new Thread(() -> {
            ArrayList<Item> items = new ArrayList<>();

            //number of documents in the collection
            int numItems = (int) collection.countDocuments();

            //current document we're parsing
            int currDoc = 0;

            //pulls all Documents
            FindIterable<Document> allDocs = collection.find();

            //create/overwrite a local (to this class) copy of the Items pulled
            remoteItems = new HashMap<>();

            //convert each Document into an Item
            String SKU;
            Item i;
            for (Document d : allDocs) {
                SKU = d.get("SKU", String.class);

                i = new Item(
                        d.get("Position", Integer.class),
                        SKU,
                        d.get("DESC", String.class),
                        d.get("UPC", String.class),
                        0, //TODO: add facings implementation?
                        false,
                        d.get("Planogram", String.class)
                );

                i.setFixture(d.get("Fixture").toString());
                i.setName(d.get("Name").toString());

                items.add(i);

                //add the Item to the local copy of Items pulled for easy
                //  lookups in other methods
                if (remoteItems.containsKey(SKU)) {
                    remoteItems.put(SKU, i);
                }

                Main.updateProgress(++currDoc, numItems);
            }

            Main.setProgress(0);

            //accept the callback and return the found Items
            if (callback != null) {
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
     * a temporary instance variable (e.g. HashMap) that can be referenced
     * locally instead of needing a remote fetch every time.
     *
     * @param SKU The SKU to search for
     * @return
     */
    private boolean itemAlreadyUploaded(String SKU) {
        if (remoteItems == null) {
            pullAllItems(null);

            try {
                pullThread.join();
            } catch (InterruptedException ex) {}
        }

        return remoteItems.containsKey(SKU);
    }

    /**
     * Uploads (inserts) a singular Item object to the database
     *
     * @param i The Item to upload
     */
    public void uploadItem(Item i) {
        if (itemAlreadyUploaded(i.getSKU())) {
            System.err.println("Can not upload item, already exists");
            return;
        }

        Document d = convertToDocument(i);

        InsertOneResult result = collection.insertOne(d);
        System.out.println("Inserted a document with the following id: "
                + result.getInsertedId().asObjectId().getValue());
    }

    /**
     * Uploads (inserts) multiple Item objects to the database through a bulk
     * write command. Much more efficient than an iterative upload.
     *
     * @param items A Collection of the Item objects to upload
     * @param callback A Runnable object to execute when finished
     */
    public synchronized void uploadItems(Collection<Item> items, Runnable callback) {
        ArrayList<WriteModel<Document>> bulkItems = new ArrayList<>();
        
        if (remoteItems == null) {
            pullAllItems(null);

            try {
                pullThread.join();
            } catch (InterruptedException ex) {}
        }

        if (uploadThread != null && uploadThread.isAlive()) {
            try {
                uploadThread.join();
            } catch (InterruptedException ex) {}
        }

        uploadThread = new Thread(() -> {
            try {
                int count = 0;
                int total = items.size() + 1;

                for (Item i : items) {
                    //if the current Item does not already exist in remote database
                    if (!remoteItems.containsKey(i.getSKU())) {
                        bulkItems.add(new InsertOneModel<>(convertToDocument(i)));
                    } else {
                        total -= 1;
                    }

                    Main.updateProgress(++count, total);
                }

                //write the changes to the database
                if (!bulkItems.isEmpty()) {
                    collection.bulkWrite(bulkItems);
                }
            } catch (MongoBulkWriteException ex) {
                System.err.println(ex);
            } finally {
                if(callback != null)
                    callback.run();
            }

            Main.setProgress(0);
        });

        uploadThread.start();
    }

    /**
     * Development purposes only - wipes the database.
     */
    public void deleteAll() {
        try {
            collection.deleteMany(regex("SKU", ""));
        } catch (MongoCommandException mce) {
            System.err.println(mce);
        }
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
        doc.append("Planogram", i.getPlanogramName());

        return doc;
    }

}
