package com.kieranskvortsov.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JTextArea;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 *
 * @author      Kieran Skvortsov
 * @employee#   72141
 * 
 * @version     1.0
 * @date        08.29.2022
 */
public class Processor {
    
    public Processor() {}
    
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
    
    private JTextArea output;
    
    //an arraylist to hold individual item objects
    private ItemArrayList items = new ItemArrayList();
    private ArrayList<Item> itemsFound = new ArrayList<>();
    
    public boolean parse(File file) throws IOException {
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
        Matcher productMatcher, locationMatcher;
        
        String currFixture = null, currName = null;
        
        int itemsDiscarded = 0;
        
        //iterate through the pdf's pages
        for(int p = 0; p < pageStrings.length; p++) {
            String[] lines = pageStrings[p].split("\n");
            
            //iterate through the lines in the page
            for(int l = 0; l < lines.length; l++) {
                String currLine = lines[l].trim();
                
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
                    
                    if(items.containsItem(SKU)) { 
                        itemsDiscarded++;
                        continue;
                    }
                    
                    //create a new item
                    Item i = new Item(
                            Integer.parseInt(productMatcher.group(1)), 
                            SKU,
                            productMatcher.group("DESCRIPTION"),
                            productMatcher.group("UPC"),
                            Integer.parseInt(productMatcher.group("FACINGS")),
                            productMatcher.group("NEW") != null &&
                            productMatcher.group("NEW").equalsIgnoreCase("new")
                        );
                    //set the fixture and name for the item
                    i.setFixture(currFixture);
                    i.setName(currName);

                    //add the item to the arraylist
                    tempItems.add(i);
                }
            }
        }
        
//        for(Item i : items)
//             output.append("\n"+i.toString());

        
        if(itemsDiscarded > 0)
            output.append(String.format("[%d] duplicate items discarded\n", itemsDiscarded));
        
        if(!tempItems.isEmpty()) {
            output.append(String.format("[%d] items parsed from [%d] pages\n", 
                    tempItems.size(), pageStrings.length));
            items.addAll(tempItems);
        }
        
        return true;
    }
    
    public void attachLog(JTextArea textArea) {
        output = textArea;
    }
    
    public String getPrintableSheet(ArrayList<String> itemSKUs) {
        String printable = Item.getHeader();
        for(String SKU : itemSKUs)
            printable += items.getItem(SKU).getPrintable()+ "\n";
        
        return printable;
    }
    
    public Item[] search(String lastDigits, SearchType searchType) {
        itemsFound.clear();
        
        for(Item i : items)
            if(i.matches(lastDigits, searchType)) itemsFound.add(i);
        
        if(itemsFound.isEmpty()) {
            System.out.println(
                String.format("No results for item(s) ending in %s:",
                    lastDigits)
            );
        } else {
            System.out.println(
                String.format("Search results returned %d item(s) ending in %s:",
                itemsFound.size(),
                lastDigits)
            );
            
            for(Item i : itemsFound)
                System.out.println(i);
        }
        
        System.out.println();
        
        Item[] itemsArray = new Item[itemsFound.size()];
        itemsArray = itemsFound.toArray(itemsArray);
        
        return itemsArray;
    }
}
