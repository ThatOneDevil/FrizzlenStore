package org.frizzlenpop.frizzlenShop.shops;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages all shops in the system
 */
public class ShopManager {

    private final FrizzlenShop plugin;
    private final Map<UUID, Shop> shops;
    private final Map<UUID, List<UUID>> playerShops; // Map of player UUID to their shop UUIDs

    /**
     * Creates a new shop manager
     *
     * @param plugin The plugin instance
     */
    public ShopManager(FrizzlenShop plugin) {
        this.plugin = plugin;
        this.shops = new ConcurrentHashMap<>();
        this.playerShops = new ConcurrentHashMap<>();
    }

    /**
     * Get a shop by its ID
     *
     * @param id The shop ID
     * @return The shop, or null if not found
     */
    public Shop getShop(UUID id) {
        return shops.get(id);
    }

    /**
     * Get all shops
     *
     * @return A collection of all shops
     */
    public Collection<Shop> getAllShops() {
        return new ArrayList<>(shops.values());
    }

    /**
     * Get all admin shops
     *
     * @return A collection of all admin shops
     */
    public Collection<Shop> getAdminShops() {
        return shops.values().stream()
                .filter(Shop::isAdminShop)
                .collect(Collectors.toList());
    }

    /**
     * Get all player shops
     *
     * @return A collection of all player shops
     */
    public Collection<Shop> getPlayerShops() {
        return shops.values().stream()
                .filter(shop -> !shop.isAdminShop())
                .collect(Collectors.toList());
    }

    /**
     * Get shops owned by a player
     *
     * @param playerUuid The player's UUID
     * @return A list of shops owned by the player
     */
    public List<Shop> getPlayerShops(UUID playerUuid) {
        List<UUID> shopIds = playerShops.getOrDefault(playerUuid, new ArrayList<>());
        return shopIds.stream()
                .map(this::getShop)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Get the number of shops owned by a player
     *
     * @param playerUuid The player's UUID
     * @return The number of shops
     */
    public int getPlayerShopCount(UUID playerUuid) {
        return playerShops.getOrDefault(playerUuid, new ArrayList<>()).size();
    }

    /**
     * Check if a player has reached their shop limit
     *
     * @param player The player to check
     * @return True if the player has reached their limit, false otherwise
     */
    public boolean hasReachedShopLimit(Player player) {
        int maxShops = getMaxShopsForPlayer(player);
        return getPlayerShopCount(player.getUniqueId()) >= maxShops;
    }

    /**
     * Get the maximum number of shops a player can have
     *
     * @param player The player to check
     * @return The maximum number of shops
     */
    public int getMaxShopsForPlayer(Player player) {
        int maxShops = plugin.getConfigManager().getMaxShopsPerPlayer();
        
        // Check permission-based limits
        List<String> limitTiers = plugin.getConfigManager().getShopLimitTiers();
        for (String permission : limitTiers) {
            if (player.hasPermission(permission)) {
                // Extract the limit from the permission name
                try {
                    int limit = Integer.parseInt(permission.substring(permission.lastIndexOf('.') + 1));
                    if (limit > maxShops) {
                        maxShops = limit;
                    }
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Invalid shop limit permission: " + permission);
                }
            }
        }
        
        return maxShops;
    }

    /**
     * Create a new admin shop
     *
     * @param name     The name of the shop
     * @param location The location of the shop
     * @return The created shop
     */
    public AdminShop createAdminShop(String name, Location location) {
        if (!plugin.getConfigManager().areAdminShopsEnabled()) {
            return null;
        }
        
        AdminShop shop = new AdminShop(plugin, name, location);
        shops.put(shop.getId(), shop);
        
        return shop;
    }

    /**
     * Create a new player shop
     *
     * @param name     The name of the shop
     * @param owner    The UUID of the shop owner
     * @param location The location of the shop
     * @return The created shop, or null if the player has reached their shop limit
     */
    public PlayerShop createPlayerShop(String name, UUID owner, Location location) {
        if (!plugin.getConfigManager().arePlayerShopsEnabled()) {
            return null;
        }
        
        Player player = plugin.getServer().getPlayer(owner);
        if (player != null && hasReachedShopLimit(player)) {
            return null;
        }
        
        PlayerShop shop = new PlayerShop(plugin, name, owner, location);
        shops.put(shop.getId(), shop);
        
        // Add to player's shops
        playerShops.computeIfAbsent(owner, k -> new ArrayList<>()).add(shop.getId());
        
        return shop;
    }

    /**
     * Delete a shop
     *
     * @param shopId The ID of the shop to delete
     * @return True if the shop was deleted, false if it didn't exist
     */
    public boolean deleteShop(UUID shopId) {
        Shop shop = shops.remove(shopId);
        if (shop == null) {
            return false;
        }
        
        // If it's a player shop, remove from player's shops list
        if (!shop.isAdminShop() && shop.getOwner() != null) {
            List<UUID> ownedShops = playerShops.get(shop.getOwner());
            if (ownedShops != null) {
                ownedShops.remove(shopId);
                if (ownedShops.isEmpty()) {
                    playerShops.remove(shop.getOwner());
                }
            }
        }
        
        return true;
    }

    /**
     * Find a shop at a location
     *
     * @param location The location to check
     * @return The shop at the location, or null if no shop exists there
     */
    public Shop getShopAtLocation(Location location) {
        // Check if location or its world is null
        if (location == null || location.getWorld() == null) {
            return null;
        }
        
        // Simple implementation - in a real plugin, you'd want to use a more efficient spatial lookup
        for (Shop shop : shops.values()) {
            if (shop.getLocation() != null && 
                shop.getLocation().getWorld() != null && 
                shop.getLocation().getWorld().equals(location.getWorld())) {
                
                double distance = shop.getLocation().distance(location);
                if (distance < 1.0) { // Within 1 block
                    return shop;
                }
            }
        }
        return null;
    }

    /**
     * Find shops selling an item
     *
     * @param item The item to search for
     * @return A list of shops selling the item
     */
    public List<Shop> findShopsSelling(ItemStack item) {
        return shops.values().stream()
                .filter(shop -> shop.hasItem(item))
                .collect(Collectors.toList());
    }

    /**
     * Find shops selling an item with the best price
     *
     * @param item     The item to search for
     * @param currency The currency to use
     * @return The shop with the best price, or null if no shops sell the item
     */
    public Shop findBestPriceSelling(ItemStack item, String currency) {
        return shops.values().stream()
                .filter(shop -> shop.hasItem(item))
                .filter(shop -> currency.equals(shop.getCurrency(item)))
                .min(Comparator.comparingDouble(shop -> shop.getBuyPrice(item)))
                .orElse(null);
    }

    /**
     * Find shops buying an item with the best price
     *
     * @param item     The item to search for
     * @param currency The currency to use
     * @return The shop with the best price, or null if no shops buy the item
     */
    public Shop findBestPriceBuying(ItemStack item, String currency) {
        return shops.values().stream()
                .filter(shop -> shop.hasItem(item))
                .filter(shop -> currency.equals(shop.getCurrency(item)))
                .max(Comparator.comparingDouble(shop -> shop.getSellPrice(item)))
                .orElse(null);
    }

    /**
     * Buy an item from a shop
     *
     * @param player   The player buying the item
     * @param shop     The shop to buy from
     * @param item     The item to buy
     * @param amount   The amount to buy
     * @param currency The currency to use
     * @return True if the purchase was successful, false otherwise
     */
    public boolean buyItemFromShop(Player player, Shop shop, ItemStack item, int amount, String currency) {
        if (!player.hasPermission("frizzlenshop.buy")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to buy from shops.");
            return false;
        }
        
        return shop.buyItem(player, item, amount, currency);
    }

    /**
     * Sell an item to a shop
     *
     * @param player   The player selling the item
     * @param shop     The shop to sell to
     * @param item     The item to sell
     * @param amount   The amount to sell
     * @param currency The currency to use
     * @return True if the sale was successful, false otherwise
     */
    public boolean sellItemToShop(Player player, Shop shop, ItemStack item, int amount, String currency) {
        if (!player.hasPermission("frizzlenshop.sell")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to sell to shops.");
            return false;
        }
        
        return shop.sellItem(player, item, amount, currency);
    }

    /**
     * Check for and renew expired shops
     */
    public void checkExpiredShops() {
        for (Shop shop : getPlayerShops()) {
            if (shop instanceof PlayerShop) {
                PlayerShop playerShop = (PlayerShop) shop;
                
                if (playerShop.isExpired()) {
                    if (playerShop.isAutoRenewEnabled()) {
                        // Try to renew the shop
                        if (!playerShop.renew()) {
                            // Failed to renew - notify owner if online
                            Player owner = plugin.getServer().getPlayer(playerShop.getOwner());
                            if (owner != null) {
                                MessageUtils.sendErrorMessage(owner, 
                                        "Your shop " + playerShop.getName() + " has expired and could not be renewed automatically. " +
                                        "Visit the shop to renew it manually.");
                            }
                        }
                    } else {
                        // Notify owner if online
                        Player owner = plugin.getServer().getPlayer(playerShop.getOwner());
                        if (owner != null) {
                            MessageUtils.sendErrorMessage(owner, 
                                    "Your shop " + playerShop.getName() + " has expired. " +
                                    "Visit the shop to renew it.");
                        }
                    }
                }
            }
        }
    }

    /**
     * Register a shop with a specific ID (for loading from storage)
     *
     * @param shop The shop to register
     * @return True if successful, false if already exists
     */
    public boolean registerShop(Shop shop) {
        if (shop == null || shops.containsKey(shop.getId())) {
            return false;
        }
        
        shops.put(shop.getId(), shop);
        
        // If it's a player shop, add it to the player's shops list
        if (!shop.isAdminShop() && shop.getOwner() != null) {
            playerShops.computeIfAbsent(shop.getOwner(), k -> new ArrayList<>()).add(shop.getId());
        }
        
        return true;
    }
    
    /**
     * Add a shop (backwards compatibility method)
     *
     * @param shop The shop to add
     * @return True if successfully added, false otherwise
     */
    public boolean addShop(Shop shop) {
        // Forward to registerShop for proper registration
        return registerShop(shop);
    }

    /**
     * Get the most popular shops based on transaction count
     *
     * @param limit The maximum number of shops to return
     * @return A list of the most popular shops
     */
    public List<Shop> getMostPopularShops(int limit) {
        return shops.values().stream()
                .sorted(Comparator.comparingDouble(shop -> 
                        -shop.getStats().getOrDefault("transactionCount", 0.0)))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get shops by category
     *
     * @param category The category name
     * @return A list of shops in the specified category
     */
    public List<Shop> getShopsByCategory(String category) {
        if (category == null || category.equalsIgnoreCase("all")) {
            return new ArrayList<>(shops.values());
        }
        
        List<Shop> result = new ArrayList<>();
        
        // Convert category to lowercase for case-insensitive comparison
        String lowerCategory = category.toLowerCase();
        
        // Check each shop for items in the specified category
        for (Shop shop : shops.values()) {
            // Skip shops that aren't open
            if (!shop.isOpen()) {
                continue;
            }
            
            boolean hasItemsInCategory = false;
            
            // Check shop items
            for (ShopItem item : shop.getItems()) {
                Material material = item.getItem().getType();
                String materialName = material.name().toLowerCase();
                
                // Check if item belongs to category
                if (belongsToCategory(materialName, lowerCategory)) {
                    hasItemsInCategory = true;
                    break;
                }
            }
            
            if (hasItemsInCategory) {
                result.add(shop);
            }
        }
        
        return result;
    }
    
    /**
     * Check if an item belongs to a category
     * 
     * @param materialName The material name in lowercase
     * @param category The category in lowercase
     * @return true if the item belongs to the category
     */
    private boolean belongsToCategory(String materialName, String category) {
        switch (category) {
            case "tools":
                return materialName.contains("pickaxe") || 
                       materialName.contains("axe") || 
                       materialName.contains("shovel") || 
                       materialName.contains("hoe") || 
                       materialName.contains("shears");
                
            case "weapons":
                return materialName.contains("sword") || 
                       materialName.contains("bow") || 
                       materialName.contains("trident") || 
                       materialName.contains("crossbow") || 
                       materialName.contains("arrow");
                
            case "armor":
                return materialName.contains("helmet") || 
                       materialName.contains("chestplate") || 
                       materialName.contains("leggings") || 
                       materialName.contains("boots") || 
                       materialName.contains("shield");
                
            case "food":
                return materialName.contains("apple") || 
                       materialName.contains("bread") || 
                       materialName.contains("beef") || 
                       materialName.contains("pork") || 
                       materialName.contains("chicken") || 
                       materialName.contains("fish") || 
                       materialName.contains("cookie") || 
                       materialName.contains("carrot") || 
                       materialName.contains("potato") || 
                       materialName.contains("cake") || 
                       materialName.contains("stew") || 
                       materialName.contains("soup");
                
            case "blocks":
                return materialName.contains("stone") || 
                       materialName.contains("dirt") || 
                       materialName.contains("grass") || 
                       materialName.contains("wood") || 
                       materialName.contains("log") || 
                       materialName.contains("plank") || 
                       materialName.contains("brick") || 
                       materialName.contains("terracotta") || 
                       materialName.contains("concrete");
                
            case "ores":
                return materialName.contains("ore") || 
                       materialName.equals("coal") || 
                       materialName.equals("diamond") || 
                       materialName.equals("emerald") || 
                       materialName.equals("lapis") || 
                       materialName.equals("redstone") || 
                       materialName.equals("quartz") || 
                       materialName.equals("gold_ingot") || 
                       materialName.equals("iron_ingot");
                
            case "redstone":
                return materialName.contains("redstone") || 
                       materialName.contains("repeater") || 
                       materialName.contains("comparator") || 
                       materialName.contains("piston") || 
                       materialName.contains("hopper") || 
                       materialName.contains("dispenser") || 
                       materialName.contains("dropper") || 
                       materialName.contains("observer") || 
                       materialName.contains("lever") || 
                       materialName.contains("button") || 
                       materialName.contains("pressure_plate");
                
            default:
                // For custom categories, look for the category name in the material name
                return materialName.contains(category);
        }
    }

    /**
     * Get the default buy price for an item
     *
     * @param item The item to get the price for
     * @return The default buy price
     */
    public double getDefaultBuyPrice(ItemStack item) {
        // In a real implementation, you might have a more sophisticated pricing system
        // based on item rarity, enchantments, etc.
        // For now, we'll use a simple system based on material
        Material material = item.getType();
        
        // Base price categories
        if (material.name().contains("DIAMOND") || material.name().contains("NETHERITE")) {
            return 100.0;
        } else if (material.name().contains("GOLD") || material.name().contains("EMERALD")) {
            return 50.0;
        } else if (material.name().contains("IRON") || material.name().contains("LAPIS")) {
            return 25.0;
        } else if (material.name().contains("STONE") || material.name().contains("WOOD")) {
            return 5.0;
        } else {
            return 10.0;
        }
    }
    
    /**
     * Get the default sell price for an item
     *
     * @param item The item to get the price for
     * @return The default sell price
     */
    public double getDefaultSellPrice(ItemStack item) {
        // Sell price is typically lower than buy price
        return getDefaultBuyPrice(item) * 0.8;
    }
} 