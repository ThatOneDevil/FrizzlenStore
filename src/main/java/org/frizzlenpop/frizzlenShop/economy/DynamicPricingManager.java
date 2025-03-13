package org.frizzlenpop.frizzlenShop.economy;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
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
} 