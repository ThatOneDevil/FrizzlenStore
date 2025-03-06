package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.config.ConfigManager;
import org.frizzlenpop.frizzlenShop.listeners.ChatListener;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles the shop admin menu GUI
 */
public class ShopAdminMenuHandler {

    /**
     * Open the shop admin menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openShopAdminMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Check permission
        if (!player.hasPermission("frizzlenshop.admin")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to use this command.");
            return;
        }
        
        // Create inventory
        String title = "FrizzlenShop - Admin Menu";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Admin options
        ItemStack createAdminShopItem = guiManager.createGuiItem(Material.DIAMOND_BLOCK, "&b&lCreate Admin Shop", 
                Arrays.asList("&7Create a new admin shop", "&7Click to start"));
        
        ItemStack manageShopsItem = guiManager.createGuiItem(Material.CHEST, "&e&lManage Shops", 
                Arrays.asList("&7Manage all shops", "&7Click to view"));
        
        ItemStack priceManagementItem = guiManager.createGuiItem(Material.GOLD_INGOT, "&e&lPrice Management", 
                Arrays.asList("&7Manage item prices", "&7Set default prices and multipliers"));
        
        ItemStack taxManagementItem = guiManager.createGuiItem(Material.EMERALD, "&e&lTax Management", 
                Arrays.asList("&7Manage shop taxes", "&7Set tax rates and view collected taxes"));
        
        ItemStack logsItem = guiManager.createGuiItem(Material.PAPER, "&e&lTransaction Logs", 
                Arrays.asList("&7View transaction logs", "&7Filter by shop, player, or type"));
        
        boolean maintenanceMode = plugin.getConfigManager().isMaintenanceMode();
        ItemStack maintenanceItem = guiManager.createGuiItem(
                maintenanceMode ? Material.REDSTONE_BLOCK : Material.REDSTONE_TORCH, 
                maintenanceMode ? "&c&lDisable Maintenance Mode" : "&e&lEnable Maintenance Mode", 
                Arrays.asList("&7Current status: " + (maintenanceMode ? "&cEnabled" : "&aDisabled"), 
                        "&7Click to toggle"));
        
        ItemStack reloadConfigItem = guiManager.createGuiItem(Material.COMPASS, "&e&lReload Config", 
                Arrays.asList("&7Reload the configuration", "&7Click to reload"));
        
        ItemStack statsItem = guiManager.createGuiItem(Material.MAP, "&e&lShop Statistics", 
                Arrays.asList("&7View shop statistics", "&7Sales, revenue, etc."));
        
        ItemStack backItem = guiManager.createGuiItem(Material.ARROW, "&7&lBack", 
                Collections.singletonList("&7Return to main menu"));
        
        // Place items in inventory
        inventory.setItem(10, createAdminShopItem);
        inventory.setItem(11, manageShopsItem);
        inventory.setItem(12, priceManagementItem);
        inventory.setItem(13, taxManagementItem);
        inventory.setItem(14, logsItem);
        inventory.setItem(15, maintenanceItem);
        inventory.setItem(16, reloadConfigItem);
        
        inventory.setItem(22, statsItem);
        
        inventory.setItem(49, backItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.SHOP_ADMIN_MENU));
    }
    
    /**
     * Handle a click in the shop admin menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot) {
        switch (slot) {
            case 10: // Create Admin Shop
                // Open a chat prompt for the shop name
                player.closeInventory();
                MessageUtils.sendMessage(player, "&eEnter a name for the new admin shop:");
                // Register a pending chat action for admin shop creation
                plugin.getChatListener().registerPendingAction(player, 
                    new ChatListener.ChatAction(ChatListener.ChatActionType.CREATE_ADMIN_SHOP));
                return true;
                
            case 11: // Manage Shops
                // Open the shop management menu
                openAdminShopManagementMenu(guiManager, plugin, player);
                return true;
                
            case 12: // Price Management
                // Open the price management menu
                openPriceManagementMenu(guiManager, plugin, player);
                return true;
                
            case 13: // Tax Management
                // Open the tax management menu
                openTaxManagementMenu(guiManager, plugin, player);
                return true;
                
            case 14: // Transaction Logs
                // Open the transaction logs menu
                TransactionLogsMenuHandler.openTransactionLogsMenu(guiManager, plugin, player);
                return true;
                
            case 15: // Maintenance Mode
                // Toggle maintenance mode
                boolean current = plugin.getConfigManager().isMaintenanceMode();
                // TODO: Implement toggling maintenance mode
                MessageUtils.sendMessage(player, "&aShop system maintenance mode " + (current ? "disabled" : "enabled"));
                guiManager.openShopAdminMenu(player); // Refresh menu
                return true;
                
            case 16: // Reload Config
                // Reload the configuration
                plugin.reloadConfig();
                MessageUtils.sendMessage(player, "&aConfiguration reloaded successfully!");
                return true;
                
            case 22: // Shop Statistics
                // Show shop statistics
                MessageUtils.sendMessage(player, "&eShop Statistics:");
                MessageUtils.sendMessage(player, "&7Total Shops: &f" + plugin.getShopManager().getAllShops().size());
                MessageUtils.sendMessage(player, "&7Admin Shops: &f" + plugin.getShopManager().getAdminShops().size());
                MessageUtils.sendMessage(player, "&7Player Shops: &f" + plugin.getShopManager().getPlayerShops().size());
                // In a real plugin, you'd show more stats like total sales, etc.
                return true;
                
            case 49: // Back
                // Open the main menu
                guiManager.openMainMenu(player);
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Opens the admin shop management menu for the player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openAdminShopManagementMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, 
            MessageUtils.colorize(plugin.getConfigManager().getAdminMenuTitle() + " - Shops"));
        
        // Fill the inventory with glass panes
        guiManager.fillEmptySlots(inventory);
        
        java.util.Collection<Shop> shopCollection = plugin.getShopManager().getAllShops();
        List<Shop> shops = new java.util.ArrayList<>(shopCollection);
        int slot = 0;
        
        for (Shop shop : shops) {
            if (slot >= 45) break; // Max 45 shops per page
            
            Material icon = shop.isAdminShop() ? Material.GOLD_BLOCK : Material.CHEST;
            String shopType = shop.isAdminShop() ? "&6Admin Shop" : "&aPlayer Shop";
            String ownerName = shop.isAdminShop() ? "Server" : 
                Bukkit.getOfflinePlayer(shop.getOwner()).getName();
            
            List<String> lore = Arrays.asList(
                "&7Type: " + shopType,
                "&7Owner: &f" + ownerName,
                "&7Location: &f" + formatLocation(shop.getLocation()),
                "&7Items: &f" + shop.getItems().size(),
                "",
                "&eClick to manage this shop"
            );
            
            ItemStack item = guiManager.createGuiItem(
                icon, 
                "&b" + shop.getName(), 
                lore
            );
            
            inventory.setItem(slot, item);
            slot++;
        }
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
            Material.ARROW, 
            "&cBack to Admin Menu", 
            Arrays.asList("&7Click to return to the main admin menu")
        );
        inventory.setItem(49, backButton);
        
        player.openInventory(inventory);
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.ADMIN_SHOP_MANAGEMENT));
    }
    
    /**
     * Opens the price management menu for the player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openPriceManagementMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        Inventory inventory = Bukkit.createInventory(null, 36, 
            MessageUtils.colorize(plugin.getConfigManager().getAdminMenuTitle() + " - Prices"));
        
        // Fill the inventory with glass panes
        guiManager.fillEmptySlots(inventory);
        
        ConfigManager config = plugin.getConfigManager();
        
        // Global price multiplier
        ItemStack globalMultiplier = guiManager.createGuiItem(
            Material.GOLDEN_APPLE, 
            "&6Global Price Multiplier", 
            Arrays.asList(
                "&7Current value: &e" + config.getGlobalPriceMultiplier() + "x",
                "",
                "&aLeft-click to increase by 0.1",
                "&cRight-click to decrease by 0.1",
                "&eShift+click to change by 0.5"
            )
        );
        inventory.setItem(10, globalMultiplier);
        
        // Default buy price
        ItemStack defaultBuyPrice = guiManager.createGuiItem(
            Material.EMERALD, 
            "&aDefault Buy Price", 
            Arrays.asList(
                "&7Current value: &e" + config.getDefaultBuyPrice(),
                "",
                "&aLeft-click to increase by 10",
                "&cRight-click to decrease by 10",
                "&eShift+click to change by 50"
            )
        );
        inventory.setItem(12, defaultBuyPrice);
        
        // Default sell price
        ItemStack defaultSellPrice = guiManager.createGuiItem(
            Material.EMERALD_BLOCK, 
            "&aDefault Sell Price", 
            Arrays.asList(
                "&7Current value: &e" + config.getDefaultSellPrice(),
                "",
                "&aLeft-click to increase by 10",
                "&cRight-click to decrease by 10",
                "&eShift+click to change by 50"
            )
        );
        inventory.setItem(14, defaultSellPrice);
        
        // Sell price ratio
        ItemStack sellPriceRatio = guiManager.createGuiItem(
            Material.COMPARATOR, 
            "&eSell Price Ratio", 
            Arrays.asList(
                "&7Current value: &e" + config.getSellPriceRatio(),
                "&7(Sell price = Buy price × Ratio)",
                "",
                "&aLeft-click to increase by 0.05",
                "&cRight-click to decrease by 0.05",
                "&eShift+click to change by 0.1"
            )
        );
        inventory.setItem(16, sellPriceRatio);
        
        // Dynamic pricing toggle
        boolean dynamicPricing = config.isDynamicPricingEnabled();
        ItemStack dynamicPricingItem = guiManager.createGuiItem(
            dynamicPricing ? Material.LIME_DYE : Material.GRAY_DYE, 
            "&bDynamic Pricing", 
            Arrays.asList(
                "&7Status: " + (dynamicPricing ? "&aEnabled" : "&cDisabled"),
                "",
                "&eClick to toggle"
            )
        );
        inventory.setItem(20, dynamicPricingItem);
        
        // Price fluctuation toggle
        boolean priceFluctuation = config.isPriceFluctuationEnabled();
        ItemStack priceFluctuationItem = guiManager.createGuiItem(
            priceFluctuation ? Material.LIME_DYE : Material.GRAY_DYE, 
            "&bPrice Fluctuation", 
            Arrays.asList(
                "&7Status: " + (priceFluctuation ? "&aEnabled" : "&cDisabled"),
                "",
                "&eClick to toggle"
            )
        );
        inventory.setItem(22, priceFluctuationItem);
        
        // Back button
        ItemStack backButton = guiManager.createGuiItem(
            Material.ARROW, 
            "&cBack to Admin Menu", 
            Arrays.asList("&7Click to return to the main admin menu")
        );
        inventory.setItem(31, backButton);
        
        player.openInventory(inventory);
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.PRICE_MANAGEMENT));
    }
    
    /**
     * Opens the tax management menu for the player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openTaxManagementMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        Inventory inventory = Bukkit.createInventory(null, 36, 
            MessageUtils.colorize(plugin.getConfigManager().getAdminMenuTitle() + " - Taxes"));
        
        // Fill the inventory with glass panes
        guiManager.fillEmptySlots(inventory);
        
        ConfigManager config = plugin.getConfigManager();
        
        // Global tax rate
        ItemStack globalTaxRate = guiManager.createGuiItem(
            Material.GOLD_INGOT, 
            "&6Global Tax Rate", 
            Arrays.asList(
                "&7Current value: &e" + (config.getGlobalTaxRate() * 100) + "%",
                "",
                "&aLeft-click to increase by 1%",
                "&cRight-click to decrease by 1%",
                "&eShift+click to change by 5%"
            )
        );
        inventory.setItem(10, globalTaxRate);
        
        // Admin shop tax rate
        ItemStack adminShopTaxRate = guiManager.createGuiItem(
            Material.GOLD_BLOCK, 
            "&6Admin Shop Tax Rate", 
            Arrays.asList(
                "&7Current value: &e" + (config.getAdminShopTaxRate() * 100) + "%",
                "",
                "&aLeft-click to increase by 1%",
                "&cRight-click to decrease by 1%",
                "&eShift+click to change by 5%"
            )
        );
        inventory.setItem(12, adminShopTaxRate);
        
        // Player shop tax rate
        ItemStack playerShopTaxRate = guiManager.createGuiItem(
            Material.IRON_BLOCK, 
            "&6Player Shop Tax Rate", 
            Arrays.asList(
                "&7Current value: &e" + (config.getPlayerShopTaxRate() * 100) + "%",
                "",
                "&aLeft-click to increase by 1%",
                "&cRight-click to decrease by 1%",
                "&eShift+click to change by 5%"
            )
        );
        inventory.setItem(14, playerShopTaxRate);
        
        // Minimum tax
        ItemStack minimumTax = guiManager.createGuiItem(
            Material.IRON_NUGGET, 
            "&eMinimum Tax", 
            Arrays.asList(
                "&7Current value: &e" + config.getMinimumTax(),
                "",
                "&aLeft-click to increase by 1",
                "&cRight-click to decrease by 1",
                "&eShift+click to change by 5"
            )
        );
        inventory.setItem(16, minimumTax);
        
        // Maximum tax
        ItemStack maximumTax = guiManager.createGuiItem(
            Material.GOLD_NUGGET, 
            "&eMaximum Tax", 
            Arrays.asList(
                "&7Current value: &e" + config.getMaximumTax(),
                "&7(0 = no maximum)",
                "",
                "&aLeft-click to increase by 10",
                "&cRight-click to decrease by 10",
                "&eShift+click to change by 50"
            )
        );
        inventory.setItem(20, maximumTax);
        
        // Tax statistics
        ItemStack taxStats = guiManager.createGuiItem(
            Material.BOOK, 
            "&bTax Statistics", 
            Arrays.asList(
                "&7Tax collected today: &e" + config.getTaxCollectedToday(),
                "&7Total tax collected: &e" + config.getTotalTaxCollected(),
                "",
                "&7Tax collection account: &e" + 
                    (config.getTaxCollectionAccount() != null ? 
                    Bukkit.getOfflinePlayer(config.getTaxCollectionAccount()).getName() : 
                    "None (taxes are removed from economy)")
            )
        );
        inventory.setItem(22, taxStats);
        
        // Back button
        ItemStack backButton = guiManager.createGuiItem(
            Material.ARROW, 
            "&cBack to Admin Menu", 
            Arrays.asList("&7Click to return to the main admin menu")
        );
        inventory.setItem(31, backButton);
        
        player.openInventory(inventory);
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.TAX_MANAGEMENT));
    }
    
    /**
     * Formats a location to a readable string
     *
     * @param location The location to format
     * @return The formatted location
     */
    private static String formatLocation(org.bukkit.Location location) {
        if (location == null) return "Unknown";
        return String.format("%s, %d, %d, %d", 
            location.getWorld().getName(), 
            location.getBlockX(), 
            location.getBlockY(), 
            location.getBlockZ());
    }
} 