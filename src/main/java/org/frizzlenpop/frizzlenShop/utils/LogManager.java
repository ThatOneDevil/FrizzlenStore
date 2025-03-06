package org.frizzlenpop.frizzlenShop.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

public class LogManager {

    private final FrizzlenShop plugin;
    private final File logFolder;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LogManager(FrizzlenShop plugin) {
        this.plugin = plugin;
        this.logFolder = new File(plugin.getDataFolder(), "logs");
        
        // Create logs directory if it doesn't exist
        if (!logFolder.exists()) {
            if (logFolder.mkdirs()) {
                plugin.getLogger().info("Created logs directory");
            } else {
                plugin.getLogger().severe("Failed to create logs directory");
            }
        }
    }

    /**
     * Log a transaction between a player and a shop
     *
     * @param player       The player involved in the transaction
     * @param shop         The shop involved in the transaction
     * @param item         The item being bought/sold
     * @param amount       The amount of the item
     * @param price        The total price of the transaction
     * @param currency     The currency used
     * @param isBuy        True if the player is buying, false if selling
     */
    public void logTransaction(Player player, Shop shop, ItemStack item, int amount, double price, String currency, boolean isBuy) {
        if (!plugin.getConfigManager().isTransactionLoggingEnabled()) {
            return;
        }
        
        try {
            File file = new File(logFolder, "transactions.log");
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);
            
            String action = isBuy ? "bought" : "sold";
            String timestamp = DATE_FORMAT.format(new Date());
            String itemName = item.getType().name();
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                itemName = item.getItemMeta().getDisplayName();
            }
            
            pw.println("[" + timestamp + "] " + player.getName() + " (" + player.getUniqueId() + ") " 
                    + action + " " + amount + "x " + itemName + " for " + price + " " + currency 
                    + " at shop " + shop.getName() + " (" + shop.getId() + ")");
            
            pw.close();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to log transaction", e);
        }
    }

    /**
     * Log a shop creation
     *
     * @param player   The player who created the shop
     * @param shop     The shop that was created
     */
    public void logShopCreation(Player player, Shop shop) {
        if (!plugin.getConfigManager().isShopChangeLoggingEnabled()) {
            return;
        }
        
        try {
            File file = new File(logFolder, "shop_changes.log");
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);
            
            String timestamp = DATE_FORMAT.format(new Date());
            
            pw.println("[" + timestamp + "] " + player.getName() + " (" + player.getUniqueId() + ") " 
                    + "created shop " + shop.getName() + " (" + shop.getId() + ")");
            
            pw.close();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to log shop creation", e);
        }
    }

    /**
     * Log a shop deletion
     *
     * @param player   The player who deleted the shop
     * @param shopId   The ID of the shop that was deleted
     * @param shopName The name of the shop that was deleted
     */
    public void logShopDeletion(Player player, UUID shopId, String shopName) {
        if (!plugin.getConfigManager().isShopChangeLoggingEnabled()) {
            return;
        }
        
        try {
            File file = new File(logFolder, "shop_changes.log");
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);
            
            String timestamp = DATE_FORMAT.format(new Date());
            
            pw.println("[" + timestamp + "] " + player.getName() + " (" + player.getUniqueId() + ") " 
                    + "deleted shop " + shopName + " (" + shopId + ")");
            
            pw.close();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to log shop deletion", e);
        }
    }

    /**
     * Log a price change for an item in a shop
     *
     * @param player   The player who changed the price
     * @param shop     The shop where the price was changed
     * @param item     The item whose price was changed
     * @param oldBuyPrice The old buy price
     * @param newBuyPrice The new buy price
     * @param oldSellPrice The old sell price
     * @param newSellPrice The new sell price
     * @param currency The currency used
     */
    public void logPriceChange(Player player, Shop shop, ItemStack item, double oldBuyPrice, double newBuyPrice, 
                               double oldSellPrice, double newSellPrice, String currency) {
        if (!plugin.getConfigManager().isPriceChangeLoggingEnabled()) {
            return;
        }
        
        try {
            File file = new File(logFolder, "price_changes.log");
            FileWriter fw = new FileWriter(file, true);
            PrintWriter pw = new PrintWriter(fw);
            
            String timestamp = DATE_FORMAT.format(new Date());
            String itemName = item.getType().name();
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                itemName = item.getItemMeta().getDisplayName();
            }
            
            pw.println("[" + timestamp + "] " + player.getName() + " (" + player.getUniqueId() + ") " 
                    + "changed price of " + itemName + " in shop " + shop.getName() + " (" + shop.getId() + "): "
                    + "Buy: " + oldBuyPrice + " -> " + newBuyPrice + " " + currency + ", "
                    + "Sell: " + oldSellPrice + " -> " + newSellPrice + " " + currency);
            
            pw.close();
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to log price change", e);
        }
    }

    /**
     * Clean up old logs based on retention period
     */
    public void cleanupLogs() {
        int retentionPeriod = plugin.getConfigManager().getLogRetentionPeriod();
        if (retentionPeriod <= 0) {
            return; // Keep logs indefinitely
        }
        
        long cutoffTime = System.currentTimeMillis() - (retentionPeriod * 24 * 60 * 60 * 1000L);
        
        File[] logFiles = logFolder.listFiles((dir, name) -> name.endsWith(".log"));
        if (logFiles == null) {
            return;
        }
        
        for (File logFile : logFiles) {
            if (logFile.lastModified() < cutoffTime) {
                if (logFile.delete()) {
                    plugin.getLogger().info("Deleted old log file: " + logFile.getName());
                } else {
                    plugin.getLogger().warning("Failed to delete old log file: " + logFile.getName());
                }
            }
        }
    }
} 