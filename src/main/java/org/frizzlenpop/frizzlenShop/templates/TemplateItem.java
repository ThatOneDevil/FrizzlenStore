package org.frizzlenpop.frizzlenShop.templates;

import org.bukkit.inventory.ItemStack;

/**
 * Represents an item in a shop template
 */
public class TemplateItem {
    
    private final ItemStack item;
    private double buyPrice;
    private double sellPrice;
    private String currency;
    private int stock;
    
    /**
     * Creates a new template item
     *
     * @param item The item
     * @param buyPrice The buy price
     * @param sellPrice The sell price
     * @param currency The currency
     * @param stock The stock
     */
    public TemplateItem(ItemStack item, double buyPrice, double sellPrice, String currency, int stock) {
        this.item = item.clone(); // Clone to prevent reference issues
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.currency = currency;
        this.stock = stock;
    }
    
    /**
     * Get the item
     *
     * @return The item
     */
    public ItemStack getItem() {
        return item.clone(); // Return a clone to prevent modification
    }
    
    /**
     * Get the buy price
     *
     * @return The buy price
     */
    public double getBuyPrice() {
        return buyPrice;
    }
    
    /**
     * Set the buy price
     *
     * @param buyPrice The new buy price
     */
    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
    }
    
    /**
     * Get the sell price
     *
     * @return The sell price
     */
    public double getSellPrice() {
        return sellPrice;
    }
    
    /**
     * Set the sell price
     *
     * @param sellPrice The new sell price
     */
    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }
    
    /**
     * Get the currency
     *
     * @return The currency
     */
    public String getCurrency() {
        return currency;
    }
    
    /**
     * Set the currency
     *
     * @param currency The new currency
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    /**
     * Get the stock
     *
     * @return The stock
     */
    public int getStock() {
        return stock;
    }
    
    /**
     * Set the stock
     *
     * @param stock The new stock
     */
    public void setStock(int stock) {
        this.stock = stock;
    }
} 