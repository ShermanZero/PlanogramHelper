package pf.item;

import pf.Processor.SearchType;

/**
 *
 * @author      Kieran Skvortsov
 * @employee#   72141
 */
public class Item {
    private final int position;
    private final String SKU;
    private final String description;
    private final String UPC;
    private final int facings;
    private final boolean isNew;
    
    private String fixture;
    private String name;
    
    public Item(int position, String SKU, String description, String UPC, int facings, boolean isNew) {
        this.position = position;
        this.SKU = SKU.trim();
        this.description = description.trim();
        this.UPC = UPC.trim();
        this.facings = facings;
        this.isNew = isNew;
    }
    
    public void setFixture(String fixture) {
        this.fixture = fixture;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public int getPosition() {
        return position;
    }
    
    public String getSKU() { 
        return SKU;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getUPC() {
        return UPC;
    }
    
    public int getFacings() {
        return facings;
    }
    
    public boolean getIsNew() {
        return isNew;
    }
    
    public String getFixture() {
        return fixture;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean matches(String queryString, SearchType searchType) {
        switch(searchType) {
            case SKU: return SKU.endsWith(queryString);
            case UPC: return UPC.endsWith(queryString);
            case WORD: return description.toLowerCase().contains(queryString.toLowerCase());
        }
        
        return false;
    }
    
    public String getItemData() {
        return (description + " @ #" + position + (isNew ? " NEW" : ""));
    }
    
    @Override
    public boolean equals(Object i) {
        return ((String)i).equals(this.SKU);
    }
    
    @Override
    public String toString() {
        return String.format(
                "%-5s [SKU] %-10s [UPC] %-18s %-35s [FIXTURE] %-8s "
                + "[NAME] %s", isNew? "(NEW)":"", SKU, UPC, description, 
                fixture, name
            );
    }
    
    public String getPrintable() {
        return String.format(
                "%-10s %-13s %-35s %-13s %-15s %s\n", SKU, UPC, description, 
                fixture, name, isNew? "(NEW)":""
            );
    }
    
    public static String getHeader() {
        return String.format(
                "%-10s %-13s %-35s %-13s %-15s\n",
                "[SKU]", "[UPC]", "[DESCRIPTION]", "[FIXTURE]", "[NAME]"
            );
    }
}
