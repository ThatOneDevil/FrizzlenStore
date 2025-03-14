package org.frizzlenpop.frizzlenShop.economy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.AdminShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages dynamic pricing for shop items
 */
public class DynamicPricingManager {

    private final FrizzlenShop plugin;
    private MarketAnalyzer marketAnalyzer;
    private boolean isEnabled;
    
    // Cache for dynamic prices to avoid frequent recalculations
    private final Map<UUID, Double> dynamicBuyPriceCache;
    private final Map<UUID, Double> dynamicSellPriceCache;
    private long cacheExpiryTime = 300000; // 5 minutes in milliseconds
    private long lastCacheCleanup = 0;
    
    /**
     * Creates a new dynamic pricing manager
     * 
     * @param plugin The plugin instance
     */
    public DynamicPricingManager(FrizzlenShop plugin) {
        this.plugin = plugin;
        this.dynamicBuyPriceCache = new HashMap<>();
        this.dynamicSellPriceCache = new HashMap<>();
        this.isEnabled = plugin.getConfigManager().isDynamicPricingEnabled();
        
        // Initialize market analyzer
        if (this.isEnabled) {
            initializeMarketAnalyzer();
        }
        
        // Schedule regular market analysis
        scheduleMarketAnalysis();
    }
    
    /**
     * Initializes the market analyzer
     */
    private void initializeMarketAnalyzer() {
        try {
            this.marketAnalyzer = new MarketAnalyzer(plugin);
            plugin.getLogger().info("Market analyzer initialized for dynamic pricing");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize market analyzer: " + e.getMessage());
            this.marketAnalyzer = null;
            this.isEnabled = false;
        }
    }
    
    /**
     * Schedules regular market analysis to update prices
     */
    private void scheduleMarketAnalysis() {
        // Run market analysis once per day
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isEnabled && marketAnalyzer != null) {
                    marketAnalyzer.performMarketAnalysis();
                    
                    // Clear price caches after analysis
                    dynamicBuyPriceCache.clear();
                    dynamicSellPriceCache.clear();
                    
                    // Apply dynamic prices to admin shop base prices
                    updateAdminShopPrices();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60 * 60, 20 * 60 * 60 * 24); // Start after 1 hour, run daily
        
        // Clean up price cache periodically
        new BukkitRunnable() {
            @Override
            public void run() {
                cleanupPriceCache();
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60 * 10, 20 * 60 * 10); // Run every 10 minutes
        
        // Update admin shop prices hourly to reflect market changes
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isEnabled && marketAnalyzer != null) {
                    updateAdminShopPrices();
                }
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60 * 30, 20 * 60 * 60); // Start after 30 minutes, run hourly
    }
    
    /**
     * Cleans up expired entries in the price cache
     */
    private void cleanupPriceCache() {
        long currentTime = System.currentTimeMillis();
        lastCacheCleanup = currentTime;
        
        // If dynamic pricing is disabled, clear all caches
        if (!isEnabled) {
            dynamicBuyPriceCache.clear();
            dynamicSellPriceCache.clear();
            return;
        }
        
        // Remove expired cache entries (older than cacheExpiryTime)
        // We would need timestamp storage for each entry, but this simple approach just clears all
        // cache entries periodically to prevent stale prices
        if (dynamicBuyPriceCache.size() > 100 || dynamicSellPriceCache.size() > 100) {
            dynamicBuyPriceCache.clear();
            dynamicSellPriceCache.clear();
        }
    }
    
    /**
     * Calculates a dynamic buy price for an item
     * 
     * @param item The shop item
     * @param staticPrice The static price to use if dynamic pricing is disabled
     * @return The calculated dynamic price or static price if dynamic pricing is disabled
     */
    public double calculateDynamicBuyPrice(ShopItem item, double staticPrice) {
        // Return static price if dynamic pricing is disabled or analyzer is not available
        if (!isEnabled || marketAnalyzer == null) {
            return staticPrice;
        }
        
        UUID itemId = item.getId();
        
        // Check cache first
        if (dynamicBuyPriceCache.containsKey(itemId)) {
            return dynamicBuyPriceCache.get(itemId);
        }
        
        // Calculate dynamic price using market analyzer
        double dynamicPrice = marketAnalyzer.calculateDynamicPrice(staticPrice, item, true);
        
        // Cache the result
        dynamicBuyPriceCache.put(itemId, dynamicPrice);
        
        return dynamicPrice;
    }
    
    /**
     * Calculates a dynamic sell price for an item
     * 
     * @param item The shop item
     * @param staticPrice The static price to use if dynamic pricing is disabled
     * @return The calculated dynamic price or static price if dynamic pricing is disabled
     */
    public double calculateDynamicSellPrice(ShopItem item, double staticPrice) {
        // Return static price if dynamic pricing is disabled or analyzer is not available
        if (!isEnabled || marketAnalyzer == null) {
            return staticPrice;
        }
        
        UUID itemId = item.getId();
        
        // Check cache first
        if (dynamicSellPriceCache.containsKey(itemId)) {
            return dynamicSellPriceCache.get(itemId);
        }
        
        // Calculate dynamic price using market analyzer
        double dynamicPrice = marketAnalyzer.calculateDynamicPrice(staticPrice, item, false);
        
        // Cache the result
        dynamicSellPriceCache.put(itemId, dynamicPrice);
        
        return dynamicPrice;
    }
    
    /**
     * Records a transaction for market analysis
     * 
     * @param item The shop item that was transacted
     * @param quantity The quantity that was transacted
     * @param isBuy True if this was a buy transaction (player buying from shop), false for sell
     */
    public void recordTransaction(ShopItem item, int quantity, boolean isBuy) {
        if (isEnabled && marketAnalyzer != null) {
            marketAnalyzer.recordTransaction(item, quantity, isBuy);
            
            // Invalidate cache entries for this item
            dynamicBuyPriceCache.remove(item.getId());
            dynamicSellPriceCache.remove(item.getId());
        }
    }
    
    /**
     * Toggles dynamic pricing on/off
     * 
     * @param enabled Whether dynamic pricing should be enabled
     * @param sender The command sender to notify (can be null)
     */
    public void setDynamicPricingEnabled(boolean enabled, CommandSender sender) {
        // Update the setting
        this.isEnabled = enabled;
        plugin.getConfigManager().setDynamicPricingEnabled(enabled);
        plugin.getConfigManager().saveConfig();
        
        // Reinitialize market analyzer if enabling
        if (enabled && marketAnalyzer == null) {
            initializeMarketAnalyzer();
        }
        
        // Clear caches
        dynamicBuyPriceCache.clear();
        dynamicSellPriceCache.clear();
        
        // Notify sender
        if (sender != null) {
            if (enabled) {
                MessageUtils.sendMessage(sender, "&aDynamic pricing has been enabled. Prices will now fluctuate based on supply and demand.");
            } else {
                MessageUtils.sendMessage(sender, "&cDynamic pricing has been disabled. All shops will now use static prices.");
            }
        }
    }
    
    /**
     * Gets the market analyzer
     * 
     * @return The market analyzer
     */
    public MarketAnalyzer getMarketAnalyzer() {
        return marketAnalyzer;
    }
    
    /**
     * Gets the trending items in the market
     * 
     * @param limit The maximum number of trending items to return
     * @return A map of materials to their trend values (positive = rising price, negative = falling price)
     */
    public Map<Material, Double> getTrendingItems(int limit) {
        if (isEnabled && marketAnalyzer != null) {
            return marketAnalyzer.getTopTrendingItems(limit);
        }
        return new HashMap<>();
    }
    
    /**
     * Gets the trend value for a specific material
     * 
     * @param material The material to check
     * @return The trend value (positive = rising price, negative = falling price), or 0 if not available
     */
    public double getTrendValueForMaterial(Material material) {
        if (isEnabled && marketAnalyzer != null) {
            Map<Material, Double> trends = marketAnalyzer.getMarketTrendSummary();
            return trends.getOrDefault(material, 0.0);
        }
        return 0.0;
    }
    
    /**
     * Checks if dynamic pricing is enabled
     * 
     * @return True if dynamic pricing is enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Updates the base prices of admin shop items based on dynamic pricing calculations
     * This ensures that admin shop prices reflect market trends over time
     */
    public void updateAdminShopPrices() {
        if (!isEnabled || marketAnalyzer == null) {
            return;
        }
        
        plugin.getLogger().info("Updating admin shop prices based on dynamic pricing...");
        
        // Use the main thread to update shops
        Bukkit.getScheduler().runTask(plugin, () -> {
            int updatedCount = 0;
            double totalPriceChange = 0;
            
            // Get all admin shops
            for (Shop shop : plugin.getShopManager().getAdminShops()) {
                if (!(shop instanceof AdminShop)) {
                    continue;
                }
                
                AdminShop adminShop = (AdminShop) shop;
                
                // Update each item in the shop
                for (ShopItem item : adminShop.getItems()) {
                    // Ensure the shop ID is set (fix for NULL in database saves)
                    if (item.getShopId() == null) {
                        item.setShopId(shop.getId());
                    }
                    
                    // Get current base prices
                    double oldBuyPrice = item.getBuyPrice();
                    double oldSellPrice = item.getSellPrice();
                    
                    // Calculate dynamic prices
                    double newBuyPrice = calculateDynamicBuyPrice(item, oldBuyPrice);
                    double newSellPrice = calculateDynamicSellPrice(item, oldSellPrice);
                    
                    // Calculate the percentage change
                    double buyPriceChange = Math.abs((newBuyPrice - oldBuyPrice) / oldBuyPrice);
                    
                    // Only update if there's a significant change (>1%)
                    if (buyPriceChange > 0.01) {
                        // Update the base prices
                        item.setBuyPrice(newBuyPrice);
                        item.setSellPrice(newSellPrice);
                        
                        try {
                            // Save the item to the database
                            plugin.getDatabaseManager().saveShopItem(item);
                            
                            updatedCount++;
                            totalPriceChange += buyPriceChange;
                        } catch (Exception e) {
                            plugin.getLogger().warning("Failed to save shop item to database: " + e.getMessage());
                        }
                    }
                }
            }
            
            // Calculate the average price change percentage
            double avgPriceChange = updatedCount > 0 ? (totalPriceChange / updatedCount) * 100 : 0;
            
            plugin.getLogger().info("Updated " + updatedCount + " admin shop items with an average price change of " 
                + String.format("%.2f", avgPriceChange) + "%");
            
            // Clear market trends after price update
            if (marketAnalyzer != null && updatedCount > 0) {
                marketAnalyzer.clearTrendData();
                plugin.getLogger().info("Market trends cleared after price update");
            }
        });
    }
} 