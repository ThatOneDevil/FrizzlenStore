package org.frizzlenpop.frizzlenShop.shops;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.*;

/**
 * Implementation of an admin shop with infinite stock
 */
public class AdminShop implements Shop {

    private final FrizzlenShop plugin;
    private final UUID id;
    private String name;
    private Location location;
    private final List<ShopItem> items;
    private long lastAccessed;
    private final long creationTime;
    private String description;
    private double taxRate;
    private final Map<String, Double> stats;
    private boolean open;
    private boolean isPublic = true;
    private String theme = "default";
    private boolean notificationsEnabled = true;
    private int tier = 1;
    private String category = "misc";

    /**
     * Create a new admin shop
     *
     * @param plugin   The plugin instance
     * @param name     The name of the shop
     * @param location The location of the shop
     */
    public AdminShop(FrizzlenShop plugin, String name, Location location) {
        this.plugin = plugin;
        this.id = UUID.randomUUID();
        this.name = name;
        this.location = location;
        this.items = new ArrayList<>();
        this.lastAccessed = System.currentTimeMillis();
        this.creationTime = System.currentTimeMillis();
        this.description = "An admin shop";
        this.taxRate = plugin.getConfigManager().getAdminShopTaxRate();
        this.stats = new HashMap<>();
        this.open = true;
        
        // Initialize stats
        stats.put("totalSales", 0.0);
        stats.put("totalPurchases", 0.0);
        stats.put("totalRevenue", 0.0);
        stats.put("totalExpenditure", 0.0);
        stats.put("totalProfit", 0.0);
        stats.put("transactionCount", 0.0);
        stats.put("uniqueCustomers", 0.0);
    }
    
    /**
     * Create a new admin shop with a specific ID (for database loading)
     *
     * @param id       The shop ID
     * @param name     The name of the shop
     * @param location The location of the shop
     */
    public AdminShop(UUID id, String name, Location location) {
        this.plugin = FrizzlenShop.getInstance();
        this.id = id;
        this.name = name;
        this.location = location;
        this.items = new ArrayList<>();
        this.lastAccessed = System.currentTimeMillis();
        this.creationTime = System.currentTimeMillis();
        this.description = "An admin shop";
        this.taxRate = plugin.getConfigManager().getAdminShopTaxRate();
        this.stats = new HashMap<>();
        this.open = true;
        
        // Initialize stats
        stats.put("totalSales", 0.0);
        stats.put("totalPurchases", 0.0);
        stats.put("totalRevenue", 0.0);
        stats.put("totalExpenditure", 0.0);
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
        return null; // Admin shops have no owner
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
        return true;
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
        // Admin shops always have unlimited stock (-1)
        ShopItem shopItem = new ShopItem(item, buyPrice, sellPrice, currency, -1);
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
        
        // Set the shop ID on the item
        shopItem.setShopId(this.id);
        
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
        if (plugin.getConfigManager().haveAdminShopsInfiniteStock()) {
            return Integer.MAX_VALUE;
        }
        
        ShopItem shopItem = getShopItem(item);
        return shopItem != null ? shopItem.getStock() : -1;
    }

    @Override
    public boolean setStock(ItemStack item, int stock) {
        if (plugin.getConfigManager().haveAdminShopsInfiniteStock()) {
            return true; // Admin shops always have infinite stock
        }
        
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
        
        // Process the transaction
        if (!plugin.getEconomyManager().withdraw(player.getUniqueId(), totalPrice, currency)) {
            // Rollback the transaction by removing the item
            player.getInventory().removeItem(purchasedItem);
            MessageUtils.sendErrorMessage(player, "Transaction failed. Please try again later.");
            return false;
        }
        
        // Update shop stats
        shopItem.incrementSoldCount(amount);
        updateStat("totalSales", totalPrice);
        updateStat("totalRevenue", totalPrice);
        updateStat("totalProfit", taxAmount);
        updateStat("transactionCount", 1);
        updateStat("uniqueCustomers", 0.1); // This is a simplistic way to track unique customers
        
        // Record transaction with DynamicPricingManager
        if (plugin.getDynamicPricingManager() != null) {
            plugin.getDynamicPricingManager().recordTransaction(shopItem, amount, true);
        }
        
        // Log the transaction
        plugin.getLogManager().logTransaction(player, this, item, amount, totalPrice, currency, true);
        
        // Send success message
        String formattedPrice = plugin.getEconomyManager().formatCurrency(totalPrice, currency);
        MessageUtils.sendSuccessMessage(player, "You bought " + amount + "x " + getItemName(item) + " for " + formattedPrice + ".");
        
        return true;
    }

    @Override
    public boolean sellItem(Player player, ItemStack item, int amount, String currency) {
        updateLastAccessed();
        
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
        
        // Create a copy of the item with the correct amount
        ItemStack soldItem = shopItem.getItem().clone();
        soldItem.setAmount(amount);
        
        // Process the transaction
        if (!plugin.getEconomyManager().deposit(player.getUniqueId(), price, currency)) {
            MessageUtils.sendErrorMessage(player, "Transaction failed. Please try again later.");
            return false;
        }
        
        // Remove the items from the player's inventory
        player.getInventory().removeItem(soldItem);
        
        // Update shop stats
        shopItem.incrementBoughtCount(amount);
        updateStat("totalPurchases", price);
        updateStat("totalExpenditure", price);
        updateStat("transactionCount", 1);
        
        // Record transaction with DynamicPricingManager
        if (plugin.getDynamicPricingManager() != null) {
            plugin.getDynamicPricingManager().recordTransaction(shopItem, amount, false);
        }
        
        // Log the transaction
        plugin.getLogManager().logTransaction(player, this, item, amount, price, currency, false);
        
        // Send success message
        String formattedPrice = plugin.getEconomyManager().formatCurrency(price, currency);
        MessageUtils.sendSuccessMessage(player, "You sold " + amount + "x " + getItemName(item) + " for " + formattedPrice + ".");
        
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
     * Get a formatted name for an item
     *
     * @param item The item
     * @return The formatted name
     */
    private String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return item.getType().name().replace("_", " ").toLowerCase();
    }

    @Override
    public boolean hasStock(ItemStack item, int amount) {
        // Admin shops have infinite stock
        return true;
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

    @Override
    public ShopItem getItem(UUID itemId) {
        for (ShopItem shopItem : items) {
            if (shopItem.getId().equals(itemId)) {
                return shopItem;
            }
        }
        return null;
    }

    /**
     * Add multiple items to the shop at once
     * Useful for quickly populating admin shops with predefined items
     *
     * @param items List of items to add
     * @param currency The currency to use for all items
     * @return The number of items successfully added
     */
    public int addItems(List<ItemStack> items, String currency) {
        int added = 0;
        
        for (ItemStack item : items) {
            // Set default prices based on item value
            double buyPrice = plugin.getShopManager().getDefaultBuyPrice(item);
            double sellPrice = plugin.getShopManager().getDefaultSellPrice(item);
            
            // Add the item with unlimited stock
            if (addItem(item, buyPrice, sellPrice, currency, -1)) {
                added++;
            }
        }
        
        return added;
    }
    
    /**
     * Add all items from a specific category to the shop
     *
     * @param category The category to add (tools, weapons, armor, blocks, food, etc.)
     * @param currency The currency to use
     * @return The number of items added
     */
    public int addCategoryItems(String category, String currency) {
        List<ItemStack> categoryItems = generateCategoryItems(category);
        return addItems(categoryItems, currency);
    }
    
    /**
     * Generate a list of common items for a category
     *
     * @param category The category name
     * @return A list of items in that category
     */
    private List<ItemStack> generateCategoryItems(String category) {
        List<ItemStack> items = new ArrayList<>();
        
        switch (category.toLowerCase()) {
            case "tools":
                // Add common tools
                items.add(new ItemStack(Material.WOODEN_PICKAXE));
                items.add(new ItemStack(Material.STONE_PICKAXE));
                items.add(new ItemStack(Material.IRON_PICKAXE));
                items.add(new ItemStack(Material.GOLDEN_PICKAXE));
                items.add(new ItemStack(Material.DIAMOND_PICKAXE));
                items.add(new ItemStack(Material.NETHERITE_PICKAXE));
                
                items.add(new ItemStack(Material.WOODEN_AXE));
                items.add(new ItemStack(Material.STONE_AXE));
                items.add(new ItemStack(Material.IRON_AXE));
                items.add(new ItemStack(Material.GOLDEN_AXE));
                items.add(new ItemStack(Material.DIAMOND_AXE));
                items.add(new ItemStack(Material.NETHERITE_AXE));
                
                items.add(new ItemStack(Material.WOODEN_SHOVEL));
                items.add(new ItemStack(Material.STONE_SHOVEL));
                items.add(new ItemStack(Material.IRON_SHOVEL));
                items.add(new ItemStack(Material.GOLDEN_SHOVEL));
                items.add(new ItemStack(Material.DIAMOND_SHOVEL));
                items.add(new ItemStack(Material.NETHERITE_SHOVEL));
                
                items.add(new ItemStack(Material.WOODEN_HOE));
                items.add(new ItemStack(Material.STONE_HOE));
                items.add(new ItemStack(Material.IRON_HOE));
                items.add(new ItemStack(Material.GOLDEN_HOE));
                items.add(new ItemStack(Material.DIAMOND_HOE));
                items.add(new ItemStack(Material.NETHERITE_HOE));
                
                items.add(new ItemStack(Material.FISHING_ROD));
                items.add(new ItemStack(Material.FLINT_AND_STEEL));
                items.add(new ItemStack(Material.SHEARS));
                break;
                
            case "weapons":
                // Add common weapons
                items.add(new ItemStack(Material.WOODEN_SWORD));
                items.add(new ItemStack(Material.STONE_SWORD));
                items.add(new ItemStack(Material.IRON_SWORD));
                items.add(new ItemStack(Material.GOLDEN_SWORD));
                items.add(new ItemStack(Material.DIAMOND_SWORD));
                items.add(new ItemStack(Material.NETHERITE_SWORD));
                
                items.add(new ItemStack(Material.BOW));
                items.add(new ItemStack(Material.CROSSBOW));
                items.add(new ItemStack(Material.TRIDENT));
                
                items.add(new ItemStack(Material.ARROW, 64));
                items.add(new ItemStack(Material.SPECTRAL_ARROW, 16));
                items.add(new ItemStack(Material.TIPPED_ARROW, 8));
                break;
                
            case "armor":
                // Add common armor
                items.add(new ItemStack(Material.LEATHER_HELMET));
                items.add(new ItemStack(Material.LEATHER_CHESTPLATE));
                items.add(new ItemStack(Material.LEATHER_LEGGINGS));
                items.add(new ItemStack(Material.LEATHER_BOOTS));
                
                items.add(new ItemStack(Material.CHAINMAIL_HELMET));
                items.add(new ItemStack(Material.CHAINMAIL_CHESTPLATE));
                items.add(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                items.add(new ItemStack(Material.CHAINMAIL_BOOTS));
                
                items.add(new ItemStack(Material.IRON_HELMET));
                items.add(new ItemStack(Material.IRON_CHESTPLATE));
                items.add(new ItemStack(Material.IRON_LEGGINGS));
                items.add(new ItemStack(Material.IRON_BOOTS));
                
                items.add(new ItemStack(Material.GOLDEN_HELMET));
                items.add(new ItemStack(Material.GOLDEN_CHESTPLATE));
                items.add(new ItemStack(Material.GOLDEN_LEGGINGS));
                items.add(new ItemStack(Material.GOLDEN_BOOTS));
                
                items.add(new ItemStack(Material.DIAMOND_HELMET));
                items.add(new ItemStack(Material.DIAMOND_CHESTPLATE));
                items.add(new ItemStack(Material.DIAMOND_LEGGINGS));
                items.add(new ItemStack(Material.DIAMOND_BOOTS));
                
                items.add(new ItemStack(Material.NETHERITE_HELMET));
                items.add(new ItemStack(Material.NETHERITE_CHESTPLATE));
                items.add(new ItemStack(Material.NETHERITE_LEGGINGS));
                items.add(new ItemStack(Material.NETHERITE_BOOTS));
                
                items.add(new ItemStack(Material.SHIELD));
                break;
                
            case "food":
                // Add common food items
                items.add(new ItemStack(Material.APPLE, 64));
                items.add(new ItemStack(Material.BREAD, 64));
                items.add(new ItemStack(Material.COOKED_BEEF, 64));
                items.add(new ItemStack(Material.COOKED_PORKCHOP, 64));
                items.add(new ItemStack(Material.COOKED_CHICKEN, 64));
                items.add(new ItemStack(Material.COOKED_MUTTON, 64));
                items.add(new ItemStack(Material.COOKED_RABBIT, 64));
                items.add(new ItemStack(Material.COOKED_COD, 64));
                items.add(new ItemStack(Material.COOKED_SALMON, 64));
                items.add(new ItemStack(Material.GOLDEN_APPLE, 16));
                items.add(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 1));
                items.add(new ItemStack(Material.CAKE));
                items.add(new ItemStack(Material.COOKIE, 64));
                items.add(new ItemStack(Material.PUMPKIN_PIE, 64));
                items.add(new ItemStack(Material.CARROT, 64));
                items.add(new ItemStack(Material.GOLDEN_CARROT, 64));
                items.add(new ItemStack(Material.BAKED_POTATO, 64));
                items.add(new ItemStack(Material.HONEY_BOTTLE, 16));
                break;
                
            case "blocks":
                // Add common building blocks
                items.add(new ItemStack(Material.STONE, 64));
                items.add(new ItemStack(Material.GRANITE, 64));
                items.add(new ItemStack(Material.DIORITE, 64));
                items.add(new ItemStack(Material.ANDESITE, 64));
                items.add(new ItemStack(Material.DEEPSLATE, 64));
                
                items.add(new ItemStack(Material.OAK_LOG, 64));
                items.add(new ItemStack(Material.SPRUCE_LOG, 64));
                items.add(new ItemStack(Material.BIRCH_LOG, 64));
                items.add(new ItemStack(Material.JUNGLE_LOG, 64));
                items.add(new ItemStack(Material.ACACIA_LOG, 64));
                items.add(new ItemStack(Material.DARK_OAK_LOG, 64));
                
                items.add(new ItemStack(Material.OAK_PLANKS, 64));
                items.add(new ItemStack(Material.SPRUCE_PLANKS, 64));
                items.add(new ItemStack(Material.BIRCH_PLANKS, 64));
                items.add(new ItemStack(Material.JUNGLE_PLANKS, 64));
                items.add(new ItemStack(Material.ACACIA_PLANKS, 64));
                items.add(new ItemStack(Material.DARK_OAK_PLANKS, 64));
                
                items.add(new ItemStack(Material.COBBLESTONE, 64));
                items.add(new ItemStack(Material.DIRT, 64));
                items.add(new ItemStack(Material.GRASS_BLOCK, 64));
                items.add(new ItemStack(Material.SAND, 64));
                items.add(new ItemStack(Material.GRAVEL, 64));
                
                items.add(new ItemStack(Material.GLASS, 64));
                items.add(new ItemStack(Material.BRICKS, 64));
                items.add(new ItemStack(Material.BOOKSHELF, 64));
                break;
                
            case "redstone":
                // Add redstone components
                items.add(new ItemStack(Material.REDSTONE, 64));
                items.add(new ItemStack(Material.REDSTONE_TORCH, 64));
                items.add(new ItemStack(Material.REDSTONE_BLOCK, 64));
                items.add(new ItemStack(Material.REPEATER, 64));
                items.add(new ItemStack(Material.COMPARATOR, 64));
                items.add(new ItemStack(Material.PISTON, 64));
                items.add(new ItemStack(Material.STICKY_PISTON, 64));
                items.add(new ItemStack(Material.OBSERVER, 64));
                items.add(new ItemStack(Material.DISPENSER, 64));
                items.add(new ItemStack(Material.DROPPER, 64));
                items.add(new ItemStack(Material.HOPPER, 64));
                items.add(new ItemStack(Material.LEVER, 64));
                items.add(new ItemStack(Material.STONE_BUTTON, 64));
                items.add(new ItemStack(Material.STONE_PRESSURE_PLATE, 64));
                items.add(new ItemStack(Material.REDSTONE_LAMP, 64));
                break;
                
            case "potions":
                // Basic potions (Note: Complete potion creation would require more complex code)
                items.add(new ItemStack(Material.POTION));
                items.add(new ItemStack(Material.SPLASH_POTION));
                items.add(new ItemStack(Material.LINGERING_POTION));
                items.add(new ItemStack(Material.GLASS_BOTTLE, 64));
                items.add(new ItemStack(Material.BREWING_STAND));
                items.add(new ItemStack(Material.BLAZE_POWDER, 64));
                items.add(new ItemStack(Material.NETHER_WART, 64));
                items.add(new ItemStack(Material.GLISTERING_MELON_SLICE, 64));
                items.add(new ItemStack(Material.SPIDER_EYE, 64));
                items.add(new ItemStack(Material.FERMENTED_SPIDER_EYE, 64));
                items.add(new ItemStack(Material.GUNPOWDER, 64));
                items.add(new ItemStack(Material.SUGAR, 64));
                items.add(new ItemStack(Material.GOLDEN_CARROT, 64));
                break;
                
            default:
                // No items for unknown category
                break;
        }
        
        return items;
    }

    @Override
    public String getOwnerName() {
        return "Server";
    }

    @Override
    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public String getTheme() {
        return theme != null ? theme : "default";
    }

    @Override
    public void setTheme(String theme) {
        this.theme = theme;
    }

    @Override
    public boolean areNotificationsEnabled() {
        return notificationsEnabled;
    }

    @Override
    public void setNotificationsEnabled(boolean enabled) {
        this.notificationsEnabled = enabled;
    }

    @Override
    public int getTier() {
        return tier;
    }

    @Override
    public void setTier(int tier) {
        this.tier = Math.max(1, Math.min(3, tier));
    }

    @Override
    public String getCategory() {
        return category != null ? category : "misc";
    }

    @Override
    public void setCategory(String category) {
        this.category = category;
    }
} 