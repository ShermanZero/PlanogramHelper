package pf.item;

import java.io.Serializable;

/**
 * The Item class represents an item in a planogram, or a better way to think of
 * it is just a product.
 * 
 * @author      Kieran Skvortsov
 * employee#    72141
 */
public class Item implements Serializable {
    
    private int position;
    private final String SKU;
    private final String description;
    private final String UPC;
    private final int facings;
    private final boolean isNew;
    
    private String friendlyLocation;
    private String fixture;
    private String name;
    
    /**
     * The SearchType enum represents how an Item can be searched by.  It accepts
     * either UPC, SKU, or WORD.
     * 
     */
    public enum SearchType {
        UPC, SKU, WORD;
    }
    
    /**
     * Creates a new Item object
     * 
     * @param position The position of the Item relative to its row/section
     * @param SKU The Item's SKU
     * @param description The Item's description
     * @param UPC The Item's UPC
     * @param facings The Item's facings
     * @param isNew If the Item is new
     */
    public Item(int position, String SKU, String description, String UPC, int facings, boolean isNew) {
        this.position = position;
        this.SKU = SKU.trim();
        this.description = description.trim();
        this.UPC = UPC.trim();
        this.facings = facings;
        this.isNew = isNew;
    }
    
    /**
     * Sets the fixture of the Item
     * 
     * @param fixture The fixture
     */
    public void setFixture(String fixture) {
        this.fixture = fixture;
        updateFriendlyLocation();
    }
    
    /**
     * Sets the name associated with the Item.  Note, in the planogram PDF
     * files, this is the name of the location.
     * 
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Sets the position relative to the shelf of the Item.
     * 
     * @param position The position (from left to right)
     */
    public void setPosition(int position) {
        this.position = position;
        updateFriendlyLocation();
    }
    
    /**
     * Returns the position of the Item
     * 
     * @return 
     */
    public int getPosition() {
        return position;
    }
    
    /**
     * Returns the SKU of the Item
     * 
     * @return The SKU
     */
    public String getSKU() { 
        return SKU;
    }
    
    /**
     * Returns the description of the Item
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Returns the UPC of the Item
     * 
     * @return The UPC
     */
    public String getUPC() {
        return UPC;
    }
    
    /**
     * Returns how many facings the Item has
     * 
     * @return The facings
     */
    public int getFacings() {
        return facings;
    }
    
    /**
     * Returns if the Item is new
     * 
     * @return New or not
     */
    public boolean getIsNew() {
        return isNew;
    }
    
    /**
     * Returns the fixture of the Item
     * 
     * @return The fixture
     */
    public String getFixture() {
        return fixture;
    }
    
    /**
     * Returns the name associated with the Item.  Note, in the planogram PDF
     * files, this is the name of the location.
     * 
     * 
     * @return 
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns if a query by a {@link SearchType} matches this Item object
     * 
     * @param queryString the String to query with
     * @param searchType the SearchType to query by
     * @return Matches or not
     */
    public boolean matches(String queryString, SearchType searchType) {
        switch(searchType) {
            case SKU: return SKU.endsWith(queryString);
            case UPC: return UPC.endsWith(queryString);
            case WORD: return description.toLowerCase().contains(queryString.toLowerCase());
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return String.format(
                "%-5s [SKU] %-10s [UPC] %-18s %-35s [FIXTURE] %-8s "
                + "[NAME] %s", isNew? "(NEW)":"", SKU, UPC, description, 
                fixture, name
            );
    }
    
    /**
     * Overridden equals method determines if an Item is equal to another item
     * if their SKUs are the same
     * 
     * @param other The other Item to compare to
     * @return If the two SKUs are equal
     */
    @Override
    public boolean equals(Object other) {
        Item item = (Item)other;
        
        return this.SKU.equals(item.getSKU());
    }
    
    /**
     * Updates the friendly location String with new data
     */
    private void updateFriendlyLocation() {
        String[] parsedFixture = fixture.split("[.]");
        
        StringBuilder sb = new StringBuilder();
        sb.append("Section #");
        sb.append(Integer.parseInt(parsedFixture[0]));
        sb.append(" | Shelf #");
        sb.append(Integer.parseInt(parsedFixture[1]));
        sb.append(" | Slot #");
        sb.append(position);
        
        friendlyLocation = sb.toString();
    }
    
    /**
     * Returns a printer-friendly formatted String representing the Item
     * 
     * @return The printer-friendly String
     */
    public String getPrinterFriendly() {
        return String.format(
                "%-10s %-13s %-35s %-13s %s\n", SKU, UPC, description, 
                friendlyLocation, isNew? "(NEW)":""
            );
    }
    
    /**
     * Returns a user-friendly readable description of the Item's location parsed
     * by the fixture.
     * 
     * @return User-friendly location
     */
    public String getFriendlyLocation() {
        return friendlyLocation;
    }
    
    /**
     * Returns the formatted header for a new page
     * 
     * @return The header
     */
    public static String getHeader() {
        return String.format(
                "%-10s %-13s %-35s %-13s\n",
                "[SKU]", "[UPC]", "[DESCRIPTION]", "[LOCATION]"
            );
    }
}
