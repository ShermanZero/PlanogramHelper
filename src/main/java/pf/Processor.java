package pf;

import pf.item.Item;
import pf.planogram.PlanogramHandler;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pf.gui.Main;
import pf.planogram.Planogram;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 *
 * @author      Kieran Skvortsov
 * @employee#   72141
 */
public class Processor {
    
    private PipedOutputStream outputStream;
    
    public Processor(PipedInputStream inputStream) {
        bindPipe(inputStream);
    }
    
    public void bindPipe(PipedInputStream inputStream) {
        if(outputStream != null)
            try {
                outputStream.close();
            } catch (IOException ex) {
                System.err.println(ex);
            }
        
        outputStream = new PipedOutputStream();
        
        try {
            inputStream.connect(outputStream);
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return;
        }
        
        System.setOut(new PrintStream(outputStream));
    }
    
    public enum SearchType {
        UPC, SKU, WORD;
    }
    
    //regex pattern that matches how products are listed
    private final String REGEX_PRODUCT = "(?<POSITION>\\d+)\\s(?<SKU>\\d+)"
            + "\\s(?=.*[a-zA-Z])(?<DESCRIPTION>(?:.*(?!\\d+\\W))*)\\s(?<UPC>\\d*)"
            + "\\s(?<FACINGS>\\d)\\s*(?<NEW>\\bNEW\\b){0,1}\\n*";
    
    //regex pattern that matches how locations are listed
    private final String REGEX_LOCATION = "(?:Fixture)\\s(?:(?!\\d).)*"
            + "(?<FIXTURE>(?:\\w|[.])*)\\s(?:Name)\\s(?<NAME>.*)";
    
    //an arraylist to hold individual item objects
    private PlanogramHandler planogramHandler = new PlanogramHandler();
    private ArrayList<Item> itemsFound = new ArrayList<>();
    
    public void startParsing(File file, Runnable callback) {
        Thread parsingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    parseToPlanogram(file);
                    callback.run();
                } catch (IOException | InterruptedException ex) {
                    System.out.println(ex);
                }
            }
        });
        
        parsingThread.start();
    }
    
    public boolean parseToPlanogram(File file) throws IOException, InterruptedException {
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

                //otherwise check to see if the current line is a product
                } else if(productMatcher.matches()){
                    String SKU = productMatcher.group("SKU");

                    if(planogramHandler.containsAny(SKU)) { 
                        itemsDiscarded++;
                        continue;
                    }

                    //create a new item
                    tempItem = new Item(
                            Integer.parseInt(productMatcher.group(1)), 
                            SKU,
                            productMatcher.group("DESCRIPTION"),
                            productMatcher.group("UPC"),
                            Integer.parseInt(productMatcher.group("FACINGS")),
                            productMatcher.group("NEW") != null &&
                            productMatcher.group("NEW").equalsIgnoreCase("new")
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
        
        if(!tempItems.isEmpty()) {
            System.out.println(String.format("[%d] items parsed from [%d] pages", 
                    tempItems.size(), pageStrings.length));
            
            Planogram p = new Planogram(file.getAbsolutePath());
            p.addItems(tempItems);
            
            planogramHandler.add(p);
        }
        
        Main.setProgress(0);
        return true;
    }
    
    public String getPrintableSheet(ArrayList<String> itemSKUs) {
        StringBuilder sb = new StringBuilder();
        sb.append(Item.getHeader());
        
        itemSKUs.stream().forEach(SKU -> {
            sb.append(planogramHandler.getItem(SKU).getPrintable());
        });
        
        return sb.toString();
    }
    
    public Item[] search(String query, SearchType searchType) {
        if(planogramHandler.isEmpty()) return null;
        
        itemsFound.clear();
        
        itemsFound = planogramHandler.getItems(query, searchType);
        
        if(itemsFound.isEmpty()) {
            System.out.println(
                String.format("No results for item(s) with %s",
                    query)
            );
        } else {
            System.out.println(
                String.format("Search results returned %d item(s) with %s",
                itemsFound.size(),
                query)
            );
        }
        
        Item[] itemsArray = new Item[itemsFound.size()];
        itemsArray = itemsFound.toArray(itemsArray);
        
        return itemsArray;
    }
    
    public void reset() {
        planogramHandler.clear();
        itemsFound.clear();
    }
}
