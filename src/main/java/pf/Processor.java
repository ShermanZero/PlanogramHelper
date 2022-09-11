package pf;

import pf.item.Item;
import pf.planogram.PlanogramHandler;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pf.gui.Main;
import pf.planogram.Planogram;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;
import pf.item.Item.SearchType;

/**
 * The Processor class is the heart of the backend.  It handles many tasks given
 * by the UI and respects the model-view implementation.
 * 
 * @author      Kieran Skvortsov
 * employee#    72141
 */
public final class Processor {
    
    //the custom output stream for communication with the UI
    private static PipedOutputStream outputStream;
    
    //regex pattern that matches how products are listed
    private static final String REGEX_PRODUCT = "(?<POSITION>\\d+)\\s(?<SKU>\\d+)"
            + "\\s(?=.*[a-zA-Z])(?<DESCRIPTION>(?:.*(?!\\d+\\W))*)\\s(?<UPC>\\d*)"
            + "\\s(?<FACINGS>\\d)\\s*(?<NEW>\\bNEW\\b){0,1}\\n*";
    
    //regex pattern that matches how locations are listed
    private static final String REGEX_LOCATION = "(?:Fixture)\\s(?:(?!\\d).)*"
            + "(?<FIXTURE>(?:\\w|[.])*)\\s(?:Name)\\s(?<NAME>.*)";
    
    //an arraylist to hold individual item objects
    private static PlanogramHandler planogramHandler = new PlanogramHandler();
    private static ArrayList<Item> itemsFound = new ArrayList<>();
    
    //the connection to the remote database
    private static MongoDBConnection mongoDBConnection = new MongoDBConnection(); 
    
    //prevent instantiation
    private Processor() {}
    
    /**
     * Binds the standard System::out pipeline to a custom output pipeline for
     * cross-communication between the UI and backend.
     * 
     * @param inputStream The PipedInputStream object to bind to
     */
    public static void bindPipe(PipedInputStream inputStream) {
        if(outputStream != null)
            try {
                outputStream.close();
            //Do not care about an output
            } catch (IOException ex) { }
        
        outputStream = new PipedOutputStream();
        
        try {
            inputStream.connect(outputStream);
        } catch (IOException ex) {
            System.err.println(ex);
            return;
        }
        
        System.setOut(new PrintStream(outputStream));
    }
    
    /**
     * Returns an ArrayList of all Item objects contained within the locally
     * instanced {@link PlanogramHandler}
     * 
     * @return The ArrayList of Items
     */
    public static ArrayList<Item> getAllItems() {
        return planogramHandler.getAllItems();
    }

    
    /**
     * Retrieves all Items from the remote database.  Consumes a {@link Consumer}
     * callback when finished, passing the Items fetched as a singular 
     * {@link Planogram} object
     * 
     * @param callback The callback execution to run on finish
     */
    public static void pullFromMongoDB(Consumer<Planogram> callback) {
        //start the thread to pull items and when finished
        //  create a new planogram
        mongoDBConnection.pullAllItems((items) -> {
            Planogram p = createNewPlanogram("Master Database", items);
            callback.accept(p);
        });
    }
    
    /**
     * Starts a threaded execution parsing a PDF into Item objects.  On completion,
     * runs a {@link Runnable} callback.
     * 
     * @param file The file to parse
     * @param planogramName The name of the planogram to associate with the items
     * @param callback The callback execution to run on finish
     */
    public static void startParsing(File file, String planogramName, Runnable callback) {
        Thread parsingThread = new Thread(() -> {
            try {
                parseToPlanogram(file, planogramName);
                
                if(callback != null)
                    callback.run();
            } catch (IOException | InterruptedException ex) {
                System.out.println(ex);
            }
        });
        
        parsingThread.start();
    }
    
    /**
     * Parses a planogram PDF into Item objects used for the view model.  This is
     * a synchronized method to prevent multi-threaded executions.
     * 
     * @param file Planogram PDF file
     * @param planogramName The name of the planogram to associate with the items
     * 
     * @throws IOException
     * @throws InterruptedException 
     */
    public static synchronized void parseToPlanogram(File file, String planogramName) throws 
            IOException, InterruptedException {
        //an array to hold strings for each page
        String[] pageStrings;
        
        //attempt to load and parse the pdf from the command-line argument
        try(PDDocument document = Loader.loadPDF(file)) {
            AccessPermission ap = document.getCurrentAccessPermission();
            if(!ap.canExtractContent()) 
                throw new IOException("You do not have permission to extract text");
            
            int numPages = document.getNumberOfPages();
            pageStrings = new String[numPages];

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            //strip each page for text
            for(int p = 1; p <= numPages; ++p) {
                stripper.setStartPage(p);
                stripper.setEndPage(p);
                
                String text = stripper.getText(document);
                
                pageStrings[p-1] = text.trim();
            }
        }
        
        //temporary list to hold items parsed
        ArrayList<Item> tempItems = new ArrayList<>();

        Pattern regexProductPattern     = Pattern.compile(REGEX_PRODUCT);
        Pattern regexLocationPattern    = Pattern.compile(REGEX_LOCATION);
        
        System.out.println("Pattern rules:");
        System.out.println(regexProductPattern.pattern());
        System.out.println(regexLocationPattern.pattern());
        
        Matcher productMatcher, locationMatcher;
        
        String currFixture = null, currName = null;
        
        int itemsDiscarded = 0;
        Item tempItem = null;
        
        //iterate through the pdf's pages
        for(int p = 0; p < pageStrings.length; p++) {
            System.out.println("Checking page: " + p);
            Main.updateProgress(p, pageStrings.length);
            
            int currentProductPosition = 0;
            
            String[] lines = pageStrings[p].split("\n");
            
            //iterate through the lines in the page
            for(int l = 0; l < lines.length; l++) {
                
                String currLine = lines[l].trim();
                
                if(currLine.contains(",")) continue;
                
                productMatcher = regexProductPattern.matcher(currLine);
                locationMatcher = regexLocationPattern.matcher(currLine);
                
                //check to see if the current line details the location
                if(locationMatcher.matches()) {
                    //update the fixture and name we are currently looking at
                    currFixture = locationMatcher.group("FIXTURE");
                    currName    = locationMatcher.group("NAME");
                    
                    currentProductPosition = 0;
                //otherwise check to see if the current line is a product
                } else if(productMatcher.matches()){
                    String SKU = productMatcher.group("SKU");

                    if(planogramHandler.containsAny(SKU)) { 
                        itemsDiscarded++;
                        continue;
                    }

                    //create a new item
                    tempItem = new Item(
                            ++currentProductPosition, 
                            SKU,
                            productMatcher.group("DESCRIPTION"),
                            productMatcher.group("UPC"),
                            Integer.parseInt(productMatcher.group("FACINGS")),
                            productMatcher.group("NEW") != null &&
                            productMatcher.group("NEW").equalsIgnoreCase("new"),
                            planogramName
                        );
                    //set the fixture and name for the item
                    tempItem.setFixture(currFixture);
                    tempItem.setName(currName);

                    //add the item to the arraylist
                    tempItems.add(tempItem);
                }
            }
        }
        
        if(itemsDiscarded > 0)
            System.out.println(String.format("[%d] duplicate items discarded", 
                    itemsDiscarded));

        //add any non-duplicate items to a new Planogram object, and then that
        //object to the planogram handler
        if(!tempItems.isEmpty()) {
            System.out.println(String.format("[%d] items parsed from [%d] pages", 
                    tempItems.size(), pageStrings.length));
            
            createNewPlanogram(planogramName, tempItems);
        }
        
        //update the progress bar in the UI
        Main.setProgress(0);
    }
    
    /**
     * Creates a new Planogram object with a name and an ArrayList of Items, and
     * adds it to the locally instanced {@link PlanogramHandler}
     * 
     * @param name The planogram's name
     * @param items The Items in the planogram
     * @return The Planogram object created
     */
    private static Planogram createNewPlanogram(String name, ArrayList<Item> items) {
        Planogram p = new Planogram(name);
        p.addItems(items);
        
        planogramHandler.add(p);
        return p;
    }
    
    /**
     * Returns a printer-friendly formatted String of Items
     * 
     * @param itemSKUs The Item objects
     * @return A printer-friendly String of items
     */
    public static String getPrintableSheet(ArrayList<String> itemSKUs) {
        StringBuilder sb = new StringBuilder();
        sb.append(Launcher.APP_ARTIFACTID);
        sb.append(" v");
        sb.append(Launcher.APP_VERSION);
        sb.append(" ");
        sb.append(Launcher.APP_COPYRIGHT);
        sb.append("\n\n");
        
        sb.append(Item.getHeader());
        
        itemSKUs.stream().forEach(SKU -> {
            sb.append(planogramHandler.getItem(SKU).getPrinterFriendly());
        });
        
        return sb.toString();
    }
    
    /**
     * Returns an array of Items matching the query
     * 
     * @param query The String to query with
     * @param searchType The SearchType to query by
     * @return 
     */
    public static Item[] search(String query, SearchType searchType) {
        if(planogramHandler.isEmpty()) return null;
        
        //clear the arraylist
        itemsFound.clear();
        
        //search all planograms to find any matching Items based on query
        itemsFound = planogramHandler.getItems(query, searchType);
        
        if(itemsFound.isEmpty()) {
            System.out.println(
                String.format("No results for item(s) with %s",
                    query)
            );
        } else {
            System.out.println(
                String.format("Search results returned %d item(s)",
                itemsFound.size())
            );
        }
        
        Item[] itemsArray = new Item[itemsFound.size()];
        itemsArray = itemsFound.toArray(itemsArray);
        
        return itemsArray;
    }
        
    /**
     * Resets all data in the locally instanced {@link PlanogramHandler}
     */
    public static void reset() {
        planogramHandler.clear();
        itemsFound.clear();
    }
    
    // <editor-fold defaultstate="collapsed" desc="DEVELOPMENT-ONLY METHODS">
    
    /**
     * Deletes all data from the remote database.
     * 
     * DEVELOPMENT ONLY.
     */
    public static void resetMongoDB() {
        mongoDBConnection.deleteAll();
    }
    
    // </editor-fold>
}
