package org.frizzlenpop.frizzlenShop.shops;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Interface for all shop implementations
 */
public interface Shop {

    /**
     * Get the unique ID of the shop
     *
     * @return The shop's UUID
     */
    UUID getId();

    /**
     * Get the name of the shop
     *
     * @return The shop's name
     */
    String getName();

    /**
     * Set the name of the shop
     *
     * @param name The new name for the shop
     */
    void setName(String name);

    /**
     * Get the owner of the shop
     *
     * @return The UUID of the shop owner, or null for admin shops
     */
    UUID getOwner();

    /**
     * Get the shop's location
     *
     * @return The shop's location
     */
    Location getLocation();

    /**
     * Set the shop's location
     *
     * @param location The new location for the shop
     */
    void setLocation(Location location);

    /**
     * Get whether the shop is an admin shop
     *
     * @return True if the shop is an admin shop, false otherwise
     */
    boolean isAdminShop();

    /**
     * Get the last time the shop was accessed
     *
     * @return The timestamp of the last access
     */
    long getLastAccessed();

    /**
     * Update the last accessed timestamp
     */
    void updateLastAccessed();

    /**
     * Get the creation time of the shop
     *
     * @return The timestamp of when the shop was created
     */
    long getCreationTime();

    /**
     * Get the list of items in the shop
     *
     * @return A list of shop items
     */
    List<ShopItem> getItems();

    /**
     * Add an item to the shop
     *
     * @param item       The item to add
     * @param buyPrice   The price to buy the item
     * @param sellPrice  The price to sell the item
     * @param currency   The currency to use
     * @param stock      The initial stock amount
     * @return True if the item was added successfully, false otherwise
     */
    boolean addItem(ItemStack item, double buyPrice, double sellPrice, String currency, int stock);

    /**
     * Add a shop item to the shop
     *
     * @param shopItem The shop item to add
     * @return True if the item was added successfully, false otherwise
     */
    boolean addItem(ShopItem shopItem);

    /**
     * Remove an item from the shop
     *
     * @param item The item to remove
     * @return True if the item was removed successfully, false otherwise
     */
    boolean removeItem(ItemStack item);

    /**
     * Check if the shop has an item
     *
     * @param item The item to check
     * @return True if the shop has the item, false otherwise
     */
    boolean hasItem(ItemStack item);

    /**
     * Get a shop item by item stack
     *
     * @param item The item to get
     * @return The shop item, or null if not found
     */
    ShopItem getShopItem(ItemStack item);

    /**
     * Get a shop item by its ID
     *
     * @param itemId The ID of the item to get
     * @return The shop item, or null if not found
     */
    ShopItem getItem(UUID itemId);

    /**
     * Get the buy price for an item
     *
     * @param item The item to get the price for
     * @return The buy price, or -1 if the item is not in the shop
     */
    double getBuyPrice(ItemStack item);

    /**
     * Set the buy price for an item
     *
     * @param item  The item to set the price for
     * @param price The new buy price
     * @return True if the price was set successfully, false otherwise
     */
    boolean setBuyPrice(ItemStack item, double price);

    /**
     * Get the sell price for an item
     *
     * @param item The item to get the price for
     * @return The sell price, or -1 if the item is not in the shop
     */
    double getSellPrice(ItemStack item);

    /**
     * Set the sell price for an item
     *
     * @param item  The item to set the price for
     * @param price The new sell price
     * @return True if the price was set successfully, false otherwise
     */
    boolean setSellPrice(ItemStack item, double price);

    /**
     * Get the currency for an item
     *
     * @param item The item to get the currency for
     * @return The currency, or null if the item is not in the shop
     */
    String getCurrency(ItemStack item);

    /**
     * Set the currency for an item
     *
     * @param item     The item to set the currency for
     * @param currency The new currency
     * @return True if the currency was set successfully, false otherwise
     */
    boolean setCurrency(ItemStack item, String currency);

    /**
     * Get the stock of an item
     *
     * @param item The item to get the stock for
     * @return The stock, or -1 if the item is not in the shop
     */
    int getStock(ItemStack item);

    /**
     * Set the stock of an item
     *
     * @param item  The item to set the stock for
     * @param stock The new stock
     * @return True if the stock was set successfully, false otherwise
     */
    boolean setStock(ItemStack item, int stock);

    /**
     * Buy an item from the shop
     *
     * @param player   The player buying the item
     * @param item     The item to buy
     * @param amount   The amount to buy
     * @param currency The currency to use
     * @return True if the purchase was successful, false otherwise
     */
    boolean buyItem(Player player, ItemStack item, int amount, String currency);

    /**
     * Sell an item to the shop
     *
     * @param player   The player selling the item
     * @param item     The item to sell
     * @param amount   The amount to sell
     * @param currency The currency to use
     * @return True if the sale was successful, false otherwise
     */
    boolean sellItem(Player player, ItemStack item, int amount, String currency);
    
    /**
     * Get the shop's description
     *
     * @return The shop's description
     */
    String getDescription();
    
    /**
     * Set the shop's description
     *
     * @param description The new description
     */
    void setDescription(String description);
    
    /**
     * Get the shop's tax rate
     *
     * @return The shop's tax rate
     */
    double getTaxRate();
    
    /**
     * Set the shop's tax rate
     *
     * @param taxRate The new tax rate
     */
    void setTaxRate(double taxRate);
    
    /**
     * Get the shop's stats
     *
     * @return A map of stats
     */
    Map<String, Double> getStats();
    
    /**
     * Update a shop stat
     *
     * @param stat  The stat to update
     * @param value The value to add to the stat
     */
    void updateStat(String stat, double value);

    /**
     * Check if the shop has enough stock of an item
     *
     * @param item The item to check
     * @param amount The amount needed
     * @return True if the shop has enough stock, false otherwise
     */
    boolean hasStock(ItemStack item, int amount);
    
    /**
     * Get the number of unique customers who have visited the shop today
     *
     * @return The customer count
     */
    int getCustomerCount();
    
    /**
     * Get the total sales value for today
     *
     * @return The total sales amount
     */
    double getTotalSales();
    
    /**
     * Check if the shop is currently open for business
     *
     * @return True if the shop is open, false if it's closed
     */
    boolean isOpen();
    
    /**
     * Set the shop's open status
     *
     * @param open True to open the shop, false to close it
     */
    void setOpen(boolean open);

    /**
     * Get the shop owner's name
     * 
     * @return The owner's name, or "Server" for admin shops
     */
    String getOwnerName();
    
    /**
     * Check if the shop is public
     * 
     * @return True if the shop is public, false if private
     */
    boolean isPublic();
    
    /**
     * Set whether the shop is public
     * 
     * @param isPublic True to make the shop public, false for private
     */
    void setPublic(boolean isPublic);
    
    /**
     * Get the shop theme/design
     * 
     * @return The shop theme
     */
    String getTheme();
    
    /**
     * Set the shop theme/design
     * 
     * @param theme The new theme
     */
    void setTheme(String theme);
    
    /**
     * Check if notifications are enabled for this shop
     * 
     * @return True if notifications are enabled
     */
    boolean areNotificationsEnabled();
    
    /**
     * Set whether notifications are enabled for this shop
     * 
     * @param enabled True to enable notifications, false to disable
     */
    void setNotificationsEnabled(boolean enabled);
    
    /**
     * Get the shop tier (for admin shops)
     * 
     * @return The shop tier (1-3)
     */
    int getTier();
    
    /**
     * Set the shop tier (for admin shops)
     * 
     * @param tier The new tier (1-3)
     */
    void setTier(int tier);
    
    /**
     * Get the shop category
     * 
     * @return The shop category
     */
    String getCategory();
    
    /**
     * Set the shop category
     * 
     * @param category The new category
     */
    void setCategory(String category);
} 