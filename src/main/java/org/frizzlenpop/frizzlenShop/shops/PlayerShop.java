package org.frizzlenpop.frizzlenShop.shops;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.*;

/**
 * Implementation of a player-owned shop with limited stock
 */
public class PlayerShop implements Shop {

    private final FrizzlenShop plugin;
    private final UUID id;
    private String name;
    private final UUID owner;
    private Location location;
    private final List<ShopItem> items;
    private long lastAccessed;
    private final long creationTime;
    private long expirationTime;
    private String description;
    private double taxRate;
    private final Map<String, Double> stats;
    private boolean autoRenew;
    private boolean open;

    /**
     * Create a new player shop
     *
     * @param plugin   The plugin instance
     * @param name     The name of the shop
     * @param owner    The UUID of the shop owner
     * @param location The location of the shop
     */
    public PlayerShop(FrizzlenShop plugin, String name, UUID owner, Location location) {
        this.plugin = plugin;
        this.id = UUID.randomUUID();
        this.name = name;
        this.owner = owner;
        this.location = location;
        this.items = new ArrayList<>();
        this.lastAccessed = System.currentTimeMillis();
        this.creationTime = System.currentTimeMillis();
        this.description = "A player shop";
        this.taxRate = plugin.getConfigManager().getPlayerShopTaxRate();
        this.stats = new HashMap<>();
        this.autoRenew = plugin.getConfigManager().isAutoRenewEnabled();
        this.open = true;
        
        // Set expiration time based on config
        int rentDays = plugin.getConfigManager().getShopRentDays();
        this.expirationTime = System.currentTimeMillis() + (rentDays * 24 * 60 * 60 * 1000L);
        
        // Initialize stats
        stats.put("totalSales", 0.0);
        stats.put("totalPurchases", 0.0);
        stats.put("totalRevenue", 0.0);
        stats.put("totalExpenditure", 0.0);
        stats.put("uniqueCustomers", 0.0);
    }
    
    /**
     * Create a new player shop with a specific ID (for database loading)
     *
     * @param id       The shop ID
     * @param name     The name of the shop
     * @param owner    The UUID of the shop owner
     * @param location The location of the shop
     */
    public PlayerShop(UUID id, String name, UUID owner, Location location) {
        this.plugin = FrizzlenShop.getInstance();
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.location = location;
        this.items = new ArrayList<>();
        this.lastAccessed = System.currentTimeMillis();
        this.creationTime = System.currentTimeMillis();
        this.description = "A player shop";
        this.taxRate = plugin.getConfigManager().getPlayerShopTaxRate();
        this.stats = new HashMap<>();
        this.autoRenew = plugin.getConfigManager().isAutoRenewEnabled();
        this.open = true;
        
        // Set expiration time based on config
        int rentDays = plugin.getConfigManager().getShopRentDays();
        this.expirationTime = System.currentTimeMillis() + (rentDays * 24 * 60 * 60 * 1000L);
        
        // Initialize stats
        stats.put("totalSales", 0.0);
        stats.put("totalPurchases", 0.0);
        stats.put("totalRevenue", 0.0);
        stats.put("totalExpenditure", 0.0);
        stats.put("uniqueCustomers", 0.0);
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public boolean isAdminShop() {
        return false;
    }

    @Override
    public long getLastAccessed() {
        return lastAccessed;
    }

    @Override
    public void updateLastAccessed() {
        this.lastAccessed = System.currentTimeMillis();
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public List<ShopItem> getItems() {
        return new ArrayList<>(items); // Return a copy to prevent direct modification
    }

    @Override
    public boolean addItem(ItemStack item, double buyPrice, double sellPrice, String currency, int stock) {
        ShopItem shopItem = new ShopItem(item, buyPrice, sellPrice, currency, stock);
        return addItem(shopItem);
    }
    
    @Override
    public boolean addItem(ShopItem shopItem) {
        // Check if the item already exists
        for (ShopItem existingItem : items) {
            if (existingItem.matches(shopItem.getItem())) {
                return false; // Item already exists
            }
        }
        
        // Add the item
        items.add(shopItem);
        
        // Update last accessed
        updateLastAccessed();
        
        return true;
    }

    @Override
    public boolean removeItem(ItemStack item) {
        for (Iterator<ShopItem> iterator = items.iterator(); iterator.hasNext();) {
            ShopItem shopItem = iterator.next();
            if (shopItem.matches(item)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasItem(ItemStack item) {
        for (ShopItem shopItem : items) {
            if (shopItem.matches(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ShopItem getShopItem(ItemStack item) {
        for (ShopItem shopItem : items) {
            if (shopItem.matches(item)) {
                return shopItem;
            }
        }
        return null;
    }

    @Override
    public ShopItem getItem(UUID itemId) {
        for (ShopItem shopItem : items) {
            if (shopItem.getId().equals(itemId)) {
                return shopItem;
            }
        }
        return null;
    }

    @Override
    public double getBuyPrice(ItemStack item) {
        ShopItem shopItem = getShopItem(item);
        return shopItem != null ? shopItem.getBuyPrice() : -1;
    }

    @Override
    public boolean setBuyPrice(ItemStack item, double price) {
        ShopItem shopItem = getShopItem(item);
        if (shopItem != null) {
            shopItem.setBuyPrice(price);
            return true;
        }
        return false;
    }

    @Override
    public double getSellPrice(ItemStack item) {
        ShopItem shopItem = getShopItem(item);
        return shopItem != null ? shopItem.getSellPrice() : -1;
    }

    @Override
    public boolean setSellPrice(ItemStack item, double price) {
        ShopItem shopItem = getShopItem(item);
        if (shopItem != null) {
            shopItem.setPrice(price);
            return true;
        }
        return false;
    }

    @Override
    public String getCurrency(ItemStack item) {
        ShopItem shopItem = getShopItem(item);
        return shopItem != null ? shopItem.getCurrency() : null;
    }

    @Override
    public boolean setCurrency(ItemStack item, String currency) {
        ShopItem shopItem = getShopItem(item);
        if (shopItem != null) {
            shopItem.setCurrency(currency);
            return true;
        }
        return false;
    }

    @Override
    public int getStock(ItemStack item) {
        ShopItem shopItem = getShopItem(item);
        return shopItem != null ? shopItem.getStock() : -1;
    }

    @Override
    public boolean setStock(ItemStack item, int stock) {
        ShopItem shopItem = getShopItem(item);
        if (shopItem != null) {
            shopItem.setStock(stock);
            return true;
        }
        return false;
    }

    @Override
    public boolean buyItem(Player player, ItemStack item, int amount, String currency) {
        updateLastAccessed();
        
        // Check if the shop has expired
        if (isExpired()) {
            MessageUtils.sendErrorMessage(player, "This shop has expired.");
            return false;
        }
        
        ShopItem shopItem = getShopItem(item);
        if (shopItem == null) {
            MessageUtils.sendErrorMessage(player, "This item is not available in this shop.");
            return false;
        }
        
        // Check if the item uses the specified currency
        if (!shopItem.getCurrency().equals(currency)) {
            MessageUtils.sendErrorMessage(player, "This item cannot be purchased with " + currency + ".");
            return false;
        }
        
        // Check if there's enough stock
        if (shopItem.getStock() < amount) {
            MessageUtils.sendErrorMessage(player, "This shop doesn't have enough stock of this item.");
            return false;
        }
        
        // Calculate price with tax
        double price = shopItem.calculateBuyPrice(amount);
        double taxAmount = price * (taxRate / 100.0);
        double totalPrice = price + taxAmount;
        
        // Check if the player has enough money
        if (!plugin.getEconomyManager().has(player.getUniqueId(), totalPrice, currency)) {
            MessageUtils.sendErrorMessage(player, "You don't have enough " + currency + " to buy this item.");
            return false;
        }
        
        // Create a copy of the item with the correct amount
        ItemStack purchasedItem = shopItem.getItem().clone();
        purchasedItem.setAmount(amount);
        
        // Check if the player has enough inventory space
        Map<Integer, ItemStack> overflow = player.getInventory().addItem(purchasedItem);
        if (!overflow.isEmpty()) {
            MessageUtils.sendErrorMessage(player, "You don't have enough inventory space.");
            return false;
        }
        
        // Remove stock from the shop
        int newStock = shopItem.removeStock(amount);
        if (newStock < 0) {
            // This shouldn't happen since we already checked stock, but just in case
            MessageUtils.sendErrorMessage(player, "This shop doesn't have enough stock of this item.");
            return false;
        }
        
        // Process the transaction
        if (!plugin.getEconomyManager().withdraw(player.getUniqueId(), totalPrice, currency)) {
            // Rollback the transaction by removing the item and restoring stock
            player.getInventory().removeItem(purchasedItem);
            shopItem.addStock(amount);
            MessageUtils.sendErrorMessage(player, "Transaction failed. Please try again later.");
            return false;
        }
        
        // Pay the shop owner (minus tax)
        double ownerPayment = price; // The owner doesn't pay the tax
        if (!plugin.getEconomyManager().deposit(owner, ownerPayment, currency)) {
            // If payment to owner fails, still allow the transaction but log the error
            plugin.getLogger().warning("Failed to pay shop owner " + owner + " for transaction in shop " + name);
        }
        
        // Update shop stats
        shopItem.incrementSoldCount(amount);
        updateStat("totalSales", totalPrice);
        updateStat("totalRevenue", ownerPayment);
        updateStat("totalProfit", ownerPayment);
        updateStat("transactionCount", 1);
        updateStat("uniqueCustomers", 0.1); // This is a simplistic way to track unique customers
        
        // Log the transaction
        plugin.getLogManager().logTransaction(player, this, item, amount, totalPrice, currency, true);
        
        // Send success message
        String formattedPrice = plugin.getEconomyManager().formatCurrency(totalPrice, currency);
        MessageUtils.sendSuccessMessage(player, "You bought " + amount + "x " + getItemName(item) + " for " + formattedPrice + ".");
        
        // Notify the shop owner if they're online
        OfflinePlayer offlineOwner = Bukkit.getOfflinePlayer(owner);
        if (offlineOwner.isOnline() && offlineOwner.getPlayer() != null) {
            String formattedOwnerPayment = plugin.getEconomyManager().formatCurrency(ownerPayment, currency);
            MessageUtils.sendMessage(offlineOwner.getPlayer(), 
                    player.getName() + " bought " + amount + "x " + getItemName(item) + 
                    " from your shop for " + formattedOwnerPayment + ".");
        }
        
        return true;
    }

    @Override
    public boolean sellItem(Player player, ItemStack item, int amount, String currency) {
        updateLastAccessed();
        
        // Check if the shop has expired
        if (isExpired()) {
            MessageUtils.sendErrorMessage(player, "This shop has expired.");
            return false;
        }
        
        ShopItem shopItem = getShopItem(item);
        if (shopItem == null) {
            MessageUtils.sendErrorMessage(player, "This shop doesn't buy this item.");
            return false;
        }
        
        // Check if the item uses the specified currency
        if (!shopItem.getCurrency().equals(currency)) {
            MessageUtils.sendErrorMessage(player, "This item cannot be sold for " + currency + ".");
            return false;
        }
        
        // Check if the player has the item
        int count = 0;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (invItem != null && shopItem.matches(invItem)) {
                count += invItem.getAmount();
            }
        }
        
        if (count < amount) {
            MessageUtils.sendErrorMessage(player, "You don't have enough of this item to sell.");
            return false;
        }
        
        // Calculate the sell price
        double price = shopItem.calculateSellPrice(amount);
        
        // Check if the shop owner has enough money
        if (!plugin.getEconomyManager().has(owner, price, currency)) {
            MessageUtils.sendErrorMessage(player, "The shop owner doesn't have enough " + currency + " to buy your items.");
            return false;
        }
        
        // Create a copy of the item with the correct amount
        ItemStack soldItem = shopItem.getItem().clone();
        soldItem.setAmount(amount);
        
        // Process the transaction
        if (!plugin.getEconomyManager().withdraw(owner, price, currency)) {
            MessageUtils.sendErrorMessage(player, "Transaction failed. Please try again later.");
            return false;
        }
        
        // Pay the player
        if (!plugin.getEconomyManager().deposit(player.getUniqueId(), price, currency)) {
            // Rollback the transaction
            plugin.getEconomyManager().deposit(owner, price, currency);
            MessageUtils.sendErrorMessage(player, "Transaction failed. Please try again later.");
            return false;
        }
        
        // Remove the items from the player's inventory
        player.getInventory().removeItem(soldItem);
        
        // Add stock to the shop
        shopItem.addStock(amount);
        
        // Update shop stats
        shopItem.incrementBoughtCount(amount);
        updateStat("totalPurchases", price);
        updateStat("totalExpenditure", price);
        updateStat("transactionCount", 1);
        
        // Log the transaction
        plugin.getLogManager().logTransaction(player, this, item, amount, price, currency, false);
        
        // Send success message
        String formattedPrice = plugin.getEconomyManager().formatCurrency(price, currency);
        MessageUtils.sendSuccessMessage(player, "You sold " + amount + "x " + getItemName(item) + " for " + formattedPrice + ".");
        
        // Notify the shop owner if they're online
        OfflinePlayer offlineOwner = Bukkit.getOfflinePlayer(owner);
        if (offlineOwner.isOnline() && offlineOwner.getPlayer() != null) {
            MessageUtils.sendMessage(offlineOwner.getPlayer(), 
                    player.getName() + " sold " + amount + "x " + getItemName(item) + 
                    " to your shop for " + formattedPrice + ".");
        }
        
        return true;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public double getTaxRate() {
        return taxRate;
    }

    @Override
    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    @Override
    public Map<String, Double> getStats() {
        return new HashMap<>(stats);
    }

    @Override
    public void updateStat(String stat, double value) {
        double currentValue = stats.getOrDefault(stat, 0.0);
        stats.put(stat, currentValue + value);
    }
    
    /**
     * Get the display name of an item
     *
     * @param item The item
     * @return The display name or the type name
     */
    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ").toLowerCase();
    }
    
    /**
     * Check if the shop has expired
     *
     * @return True if the shop has expired, false otherwise
     */
    public boolean isExpired() {
        return expirationTime > 0 && System.currentTimeMillis() > expirationTime;
    }
    
    /**
     * Get the expiration time of the shop
     *
     * @return The expiration timestamp, or -1 if there's no expiration
     */
    public long getExpirationTime() {
        return expirationTime;
    }
    
    /**
     * Set the expiration time of the shop
     *
     * @param expirationTime The new expiration timestamp
     */
    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
    
    /**
     * Renew the shop for another rental period
     *
     * @return True if the shop was renewed successfully, false otherwise
     */
    public boolean renew() {
        int rentalPeriod = plugin.getConfigManager().getShopRentalPeriod();
        if (rentalPeriod <= 0) {
            return true; // No rental period, shop doesn't expire
        }
        
        // Calculate the renewal cost
        double renewalCost = plugin.getConfigManager().getShopRentalCost();
        String currency = plugin.getEconomyManager().getDefaultCurrency();
        
        // Check if the owner has enough money
        if (!plugin.getEconomyManager().has(owner, renewalCost, currency)) {
            return false;
        }
        
        // Process the payment
        if (!plugin.getEconomyManager().withdraw(owner, renewalCost, currency)) {
            return false;
        }
        
        // Set the new expiration time
        long newExpirationTime;
        if (isExpired()) {
            newExpirationTime = System.currentTimeMillis() + (rentalPeriod * 24 * 60 * 60 * 1000L);
        } else {
            newExpirationTime = expirationTime + (rentalPeriod * 24 * 60 * 60 * 1000L);
        }
        setExpirationTime(newExpirationTime);
        
        return true;
    }
    
    /**
     * Check if auto-renewal is enabled
     *
     * @return True if auto-renewal is enabled, false otherwise
     */
    public boolean isAutoRenewEnabled() {
        return autoRenew;
    }
    
    /**
     * Set whether auto-renewal is enabled
     *
     * @param autoRenew True to enable auto-renewal, false to disable
     */
    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean hasStock(ItemStack item, int amount) {
        ShopItem shopItem = getShopItem(item);
        if (shopItem == null) {
            return false;
        }
        return shopItem.getStock() >= amount;
    }
    
    @Override
    public int getCustomerCount() {
        // Get the customer count from stats
        return stats.getOrDefault("unique_customers", 0.0).intValue();
    }
    
    @Override
    public double getTotalSales() {
        // Get the total sales from stats
        return stats.getOrDefault("total_sales", 0.0);
    }
    
    @Override
    public boolean isOpen() {
        return open;
    }
    
    @Override
    public void setOpen(boolean open) {
        this.open = open;
    }
} 