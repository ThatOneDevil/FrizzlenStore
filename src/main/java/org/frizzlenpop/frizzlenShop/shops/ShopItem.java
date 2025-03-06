package org.frizzlenpop.frizzlenShop.shops;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents an item in a shop
 */
public class ShopItem {

    private final UUID id;
    private final UUID shopId;
    private final ItemStack item;
    private double buyPrice;
    private double sellPrice;
    private String currency;
    private int stock;
    private int soldCount;
    private int boughtCount;
    private long lastPriceChange;

    /**
     * Create a new shop item
     *
     * @param item       The item
     * @param buyPrice   The price to buy the item
     * @param sellPrice  The price to sell the item
     * @param currency   The currency to use
     * @param stock      The initial stock amount
     */
    public ShopItem(ItemStack item, double buyPrice, double sellPrice, String currency, int stock) {
        this.id = UUID.randomUUID();
        this.shopId = null; // This will be set when added to a shop
        this.item = item.clone();
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.currency = currency;
        this.stock = stock;
        this.soldCount = 0;
        this.boughtCount = 0;
        this.lastPriceChange = System.currentTimeMillis();
    }
    
    /**
     * Create a new shop item with specific IDs (for database loading)
     *
     * @param id         The item ID
     * @param shopId     The shop ID
     * @param item       The item
     * @param buyPrice   The price to buy the item
     * @param sellPrice  The price to sell the item
     * @param currency   The currency to use
     * @param stock      The initial stock amount
     */
    public ShopItem(UUID id, UUID shopId, ItemStack item, double buyPrice, double sellPrice, String currency, int stock) {
        this.id = id;
        this.shopId = shopId;
        this.item = item.clone();
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.currency = currency;
        this.stock = stock;
        this.soldCount = 0;
        this.boughtCount = 0;
        this.lastPriceChange = System.currentTimeMillis();
    }
    
    /**
     * Create a new shop item with specific ID and price (simplified for database loading)
     *
     * @param id         The item ID
     * @param shopId     The shop ID
     * @param item       The item
     * @param price      The price (used for both buy and sell)
     */
    public ShopItem(UUID id, UUID shopId, ItemStack item, double price) {
        this.id = id;
        this.shopId = shopId;
        this.item = item.clone();
        this.buyPrice = price;
        this.sellPrice = price * 0.8; // Default sell price is 80% of buy price
        this.currency = "coin"; // Default currency
        this.stock = -1; // Unlimited stock by default
        this.soldCount = 0;
        this.boughtCount = 0;
        this.lastPriceChange = System.currentTimeMillis();
    }
    
    /**
     * Get the unique ID of this item
     * 
     * @return The item ID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Get the shop ID this item belongs to
     * 
     * @return The shop ID
     */
    public UUID getShopId() {
        return shopId;
    }

    /**
     * Get the item
     *
     * @return The item
     */
    public ItemStack getItem() {
        return item.clone();
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
     * Get the price (synonym for getBuyPrice for DatabaseManager compatibility)
     *
     * @return The buy price
     */
    public double getPrice() {
        return buyPrice;
    }

    /**
     * Set the buy price
     *
     * @param buyPrice The new buy price
     */
    public void setBuyPrice(double buyPrice) {
        this.buyPrice = buyPrice;
        this.lastPriceChange = System.currentTimeMillis();
    }
    
    /**
     * Set the price (affects both buy and sell prices)
     *
     * @param price The new price
     */
    public void setPrice(double price) {
        this.buyPrice = price;
        this.sellPrice = price * 0.8; // Update sell price to be 80% of buy price
        this.lastPriceChange = System.currentTimeMillis();
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

    /**
     * Add stock
     *
     * @param amount The amount to add
     * @return The new stock amount
     */
    public int addStock(int amount) {
        stock += amount;
        return stock;
    }

    /**
     * Remove stock
     *
     * @param amount The amount to remove
     * @return The new stock amount, or -1 if there's not enough stock
     */
    public int removeStock(int amount) {
        if (stock < amount) {
            return -1;
        }
        stock -= amount;
        return stock;
    }

    /**
     * Get the number of times this item has been sold to players
     *
     * @return The sold count
     */
    public int getSoldCount() {
        return soldCount;
    }

    /**
     * Increment the sold count
     *
     * @param amount The amount to increment by
     */
    public void incrementSoldCount(int amount) {
        soldCount += amount;
    }

    /**
     * Get the number of times this item has been bought from players
     *
     * @return The bought count
     */
    public int getBoughtCount() {
        return boughtCount;
    }

    /**
     * Increment the bought count
     *
     * @param amount The amount to increment by
     */
    public void incrementBoughtCount(int amount) {
        boughtCount += amount;
    }

    /**
     * Get the last time the price was changed
     *
     * @return The timestamp of the last price change
     */
    public long getLastPriceChange() {
        return lastPriceChange;
    }

    /**
     * Check if the item matches another item
     *
     * @param other The other item to check
     * @return True if the items match, false otherwise
     */
    public boolean matches(ItemStack other) {
        if (other == null) {
            return false;
        }
        
        // Check material and metadata
        if (item.getType() != other.getType()) {
            return false;
        }
        
        // For simplicity, we'll just check basic attributes
        // In a real implementation, you'd want to check more NBT data
        if (item.hasItemMeta() != other.hasItemMeta()) {
            return false;
        }
        
        if (item.hasItemMeta()) {
            if (item.getItemMeta().hasDisplayName() != other.getItemMeta().hasDisplayName()) {
                return false;
            }
            
            if (item.getItemMeta().hasDisplayName() && !item.getItemMeta().getDisplayName().equals(other.getItemMeta().getDisplayName())) {
                return false;
            }
            
            if (item.getItemMeta().hasEnchants() != other.getItemMeta().hasEnchants()) {
                return false;
            }
            
            if (item.getItemMeta().hasEnchants() && !item.getItemMeta().getEnchants().equals(other.getItemMeta().getEnchants())) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Calculate the buy price for a specific amount
     *
     * @param amount The amount to buy
     * @return The total price
     */
    public double calculateBuyPrice(int amount) {
        return buyPrice * amount;
    }

    /**
     * Calculate the sell price for a specific amount
     *
     * @param amount The amount to sell
     * @return The total price
     */
    public double calculateSellPrice(int amount) {
        return sellPrice * amount;
    }
} 