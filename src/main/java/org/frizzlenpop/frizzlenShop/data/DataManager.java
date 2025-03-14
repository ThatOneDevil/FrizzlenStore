package org.frizzlenpop.frizzlenShop.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.AdminShop;
import org.frizzlenpop.frizzlenShop.shops.PlayerShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages data storage and retrieval for the plugin
 */
public class DataManager {

    private final FrizzlenShop plugin;
    private final File shopFile;
    private FileConfiguration shopConfig;

    /**
     * Creates a new data manager
     *
     * @param plugin The plugin instance
     */
    public DataManager(FrizzlenShop plugin) {
        this.plugin = plugin;
        this.shopFile = new File(plugin.getDataFolder(), "shops.yml");
        this.shopConfig = YamlConfiguration.loadConfiguration(shopFile);
    }

    /**
     * Load data from storage
     */
    public void loadData() {
        loadShops();
    }

    /**
     * Save data to storage
     */
    public void saveData() {
        saveShops();
    }

    /**
     * Load shops from storage
     */
    private void loadShops() {
        if (!shopFile.exists()) {
            plugin.getLogger().info("No shops file found, creating new one.");
            return;
        }

        shopConfig = YamlConfiguration.loadConfiguration(shopFile);
        
        // Load admin shops
        ConfigurationSection adminShopsSection = shopConfig.getConfigurationSection("admin-shops");
        if (adminShopsSection != null) {
            for (String key : adminShopsSection.getKeys(false)) {
                try {
                    UUID shopId = UUID.fromString(key);
                    ConfigurationSection shopSection = adminShopsSection.getConfigurationSection(key);
                    
                    if (shopSection != null) {
                        String name = shopSection.getString("name");
                        Location location = deserializeLocation(shopSection.getConfigurationSection("location"));
                        
                        if (name != null && location != null) {
                            AdminShop shop = new AdminShop(shopId, name, location);
                            loadShopItems(shop, shopSection.getConfigurationSection("items"));
                            
                            // Load additional properties
                            if (shopSection.contains("description")) {
                                shop.setDescription(shopSection.getString("description"));
                            }
                            
                            if (shopSection.contains("tax-rate")) {
                                shop.setTaxRate(shopSection.getDouble("tax-rate"));
                            }
                            
                            // Load stats
                            loadShopStats(shop, shopSection.getConfigurationSection("stats"));
                            
                            plugin.getShopManager().registerShop(shop);
                            plugin.getLogger().info("Loaded admin shop: " + name + " with ID: " + shopId);
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load admin shop: " + key, e);
                }
            }
        }
        
        // Load player shops
        ConfigurationSection playerShopsSection = shopConfig.getConfigurationSection("player-shops");
        if (playerShopsSection != null) {
            for (String key : playerShopsSection.getKeys(false)) {
                try {
                    UUID shopId = UUID.fromString(key);
                    ConfigurationSection shopSection = playerShopsSection.getConfigurationSection(key);
                    
                    if (shopSection != null) {
                        String name = shopSection.getString("name");
                        UUID owner = UUID.fromString(shopSection.getString("owner"));
                        Location location = deserializeLocation(shopSection.getConfigurationSection("location"));
                        
                        if (name != null && owner != null && location != null) {
                            PlayerShop shop = new PlayerShop(shopId, name, owner, location);
                            loadShopItems(shop, shopSection.getConfigurationSection("items"));
                            
                            // Load additional properties
                            if (shopSection.contains("description")) {
                                shop.setDescription(shopSection.getString("description"));
                            }
                            
                            if (shopSection.contains("tax-rate")) {
                                shop.setTaxRate(shopSection.getDouble("tax-rate"));
                            }
                            
                            if (shopSection.contains("expiration-time")) {
                                shop.setExpirationTime(shopSection.getLong("expiration-time"));
                            }
                            
                            if (shopSection.contains("auto-renew")) {
                                shop.setAutoRenew(shopSection.getBoolean("auto-renew"));
                            }
                            
                            // Load stats
                            loadShopStats(shop, shopSection.getConfigurationSection("stats"));
                            
                            plugin.getShopManager().registerShop(shop);
                            plugin.getLogger().info("Loaded player shop: " + name + " with ID: " + shopId);
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to load player shop: " + key, e);
                }
            }
        }
    }

    /**
     * Load shop items from storage
     *
     * @param shop         The shop to load items for
     * @param itemsSection The configuration section containing items
     */
    private void loadShopItems(Shop shop, ConfigurationSection itemsSection) {
        if (itemsSection == null) {
            return;
        }
        
        for (String key : itemsSection.getKeys(false)) {
            try {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection != null) {
                    ItemStack item = itemSection.getItemStack("item");
                    double buyPrice = itemSection.getDouble("buy-price");
                    double sellPrice = itemSection.getDouble("sell-price");
                    String currency = itemSection.getString("currency", plugin.getEconomyManager().getDefaultCurrency());
                    int stock = itemSection.getInt("stock");
                    
                    if (item != null) {
                        // Create the shop item with the shop's ID
                        UUID itemId = UUID.fromString(key);
                        ShopItem shopItem = new ShopItem(itemId, shop.getId(), item, buyPrice, sellPrice, currency, stock);
                        
                        // Explicitly set the shop ID to ensure it's not null
                        shopItem.setShopId(shop.getId());
                        
                        // Add the item to the shop
                        shop.addItem(shopItem);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load shop item: " + key, e);
            }
        }
    }

    /**
     * Load shop stats from storage
     *
     * @param shop        The shop to load stats for
     * @param statsSection The configuration section containing stats
     */
    private void loadShopStats(Shop shop, ConfigurationSection statsSection) {
        if (statsSection == null) {
            return;
        }
        
        for (String key : statsSection.getKeys(false)) {
            try {
                double value = statsSection.getDouble(key);
                shop.updateStat(key, value);
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load shop stat: " + key, e);
            }
        }
    }

    /**
     * Save shops to storage
     */
    private void saveShops() {
        // Clear existing shops config
        shopConfig = new YamlConfiguration();
        
        // Save admin shops
        ConfigurationSection adminShopsSection = shopConfig.createSection("admin-shops");
        for (Shop shop : plugin.getShopManager().getAdminShops()) {
            try {
                ConfigurationSection shopSection = adminShopsSection.createSection(shop.getId().toString());
                shopSection.set("name", shop.getName());
                shopSection.set("description", shop.getDescription());
                shopSection.set("tax-rate", shop.getTaxRate());
                
                // Save location
                serializeLocation(shop.getLocation(), shopSection.createSection("location"));
                
                // Save items
                ConfigurationSection itemsSection = shopSection.createSection("items");
                saveShopItems(shop, itemsSection);
                
                // Save stats
                ConfigurationSection statsSection = shopSection.createSection("stats");
                saveShopStats(shop, statsSection);
                
                // Save to database as well
                try {
                    if (plugin.getDatabaseManager() != null) {
                        plugin.getDatabaseManager().saveShop(shop);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to save shop to database: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save admin shop: " + shop.getName(), e);
            }
        }
        
        // Save player shops
        ConfigurationSection playerShopsSection = shopConfig.createSection("player-shops");
        for (Shop shop : plugin.getShopManager().getPlayerShops()) {
            try {
                ConfigurationSection shopSection = playerShopsSection.createSection(shop.getId().toString());
                shopSection.set("name", shop.getName());
                shopSection.set("owner", shop.getOwner().toString());
                shopSection.set("description", shop.getDescription());
                shopSection.set("tax-rate", shop.getTaxRate());
                
                // Save location
                serializeLocation(shop.getLocation(), shopSection.createSection("location"));
                
                // Save items
                ConfigurationSection itemsSection = shopSection.createSection("items");
                saveShopItems(shop, itemsSection);
                
                // Save stats
                ConfigurationSection statsSection = shopSection.createSection("stats");
                saveShopStats(shop, statsSection);
                
                // Save player shop specific fields
                if (shop instanceof PlayerShop) {
                    PlayerShop playerShop = (PlayerShop) shop;
                    shopSection.set("expiration-time", playerShop.getExpirationTime());
                    shopSection.set("auto-renew", playerShop.isAutoRenewEnabled());
                }
                
                // Save to database as well
                try {
                    if (plugin.getDatabaseManager() != null) {
                        plugin.getDatabaseManager().saveShop(shop);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to save shop to database: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save player shop: " + shop.getName(), e);
            }
        }
        
        // Save to file
        try {
            shopConfig.save(shopFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save shops to file", e);
        }
    }

    /**
     * Save shop items to storage
     *
     * @param shop        The shop to save items for
     * @param itemsSection The configuration section to save items to
     */
    private void saveShopItems(Shop shop, ConfigurationSection itemsSection) {
        for (ShopItem shopItem : shop.getItems()) {
            try {
                // Use the item's UUID as the section key for consistency with loading
                ConfigurationSection itemSection = itemsSection.createSection(shopItem.getId().toString());
                itemSection.set("item", shopItem.getItem());
                itemSection.set("buy-price", shopItem.getBuyPrice());
                itemSection.set("sell-price", shopItem.getSellPrice());
                itemSection.set("currency", shopItem.getCurrency());
                itemSection.set("stock", shopItem.getStock());
                // Save the shop ID for additional verification
                itemSection.set("shop-id", shopItem.getShopId().toString());
                
                // Save to database as well to ensure consistency
                try {
                    if (plugin.getDatabaseManager() != null) {
                        plugin.getDatabaseManager().saveShopItem(shopItem);
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Failed to save shop item to database: " + e.getMessage(), e);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to save shop item", e);
            }
        }
    }

    /**
     * Save shop stats to storage
     *
     * @param shop        The shop to save stats for
     * @param statsSection The configuration section to save stats to
     */
    private void saveShopStats(Shop shop, ConfigurationSection statsSection) {
        Map<String, Double> stats = shop.getStats();
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            statsSection.set(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Serialize a location to a configuration section
     *
     * @param location The location to serialize
     * @param section  The section to serialize to
     */
    private void serializeLocation(Location location, ConfigurationSection section) {
        if (location != null && location.getWorld() != null) {
            section.set("world", location.getWorld().getName());
            section.set("x", location.getX());
            section.set("y", location.getY());
            section.set("z", location.getZ());
            section.set("yaw", location.getYaw());
            section.set("pitch", location.getPitch());
        }
    }

    /**
     * Deserialize a location from a configuration section
     *
     * @param section The section to deserialize from
     * @return The deserialized location
     */
    private Location deserializeLocation(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        
        String worldName = section.getString("world");
        if (worldName == null) {
            return null;
        }
        
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        float yaw = (float) section.getDouble("yaw");
        float pitch = (float) section.getDouble("pitch");
        
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
} 