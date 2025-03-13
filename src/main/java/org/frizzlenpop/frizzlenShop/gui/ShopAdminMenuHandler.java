package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.config.ConfigManager;
import org.frizzlenpop.frizzlenShop.listeners.ChatListener;
import org.frizzlenpop.frizzlenShop.shops.AdminShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopManager;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;
import org.frizzlenpop.frizzlenShop.utils.GuiUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;

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
        
        // New bulk item management item
        ItemStack bulkItemManagementItem = guiManager.createGuiItem(Material.CHEST_MINECART, "&d&lBulk Item Management", 
                Arrays.asList(
                    "&7Quickly add item categories",
                    "&7to admin shops",
                    "",
                    "&7Click to manage"
                ));
        
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
        
        inventory.setItem(21, statsItem);
        inventory.setItem(23, bulkItemManagementItem);  // Add the new button
        
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
                plugin.getConfigManager().setMaintenanceMode(!current);
                plugin.getConfigManager().saveConfig();
                MessageUtils.sendMessage(player, "&aShop system maintenance mode " + (current ? "disabled" : "enabled"));
                guiManager.openShopAdminMenu(player); // Refresh menu
                return true;
                
            case 16: // Reload Config
                // Reload the configuration
                plugin.reloadConfig();
                MessageUtils.sendMessage(player, "&aConfiguration reloaded successfully!");
                return true;
                
            case 21: // Shop Statistics
                // Show shop statistics
                openStatisticsMenu(guiManager, plugin, player);
                return true;
                
            case 23: // Bulk Item Management
                // Open the bulk item management menu
                openBulkItemManagementMenu(guiManager, plugin, player);
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
     * Opens the bulk item management menu for the player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openBulkItemManagementMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Create inventory
        String title = "Bulk Item Management";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Show admin shops
        java.util.Collection<Shop> adminShopCollection = plugin.getShopManager().getAdminShops();
        List<Shop> adminShops = new java.util.ArrayList<>(adminShopCollection);
        
        int slot = 0;
        for (Shop shop : adminShops) {
            if (slot >= 27) break; // Max 27 shops shown
            
            ItemStack shopItem = guiManager.createGuiItem(
                Material.CHEST, 
                "&b" + shop.getName(), 
                Arrays.asList(
                    "&7Type: &6Admin Shop",
                    "&7Location: &f" + formatLocation(shop.getLocation()),
                    "&7Items: &f" + shop.getItems().size(),
                    "",
                    "&eClick to select this shop"
                )
            );
            
            inventory.setItem(slot, shopItem);
            slot++;
        }
        
        // Add category items
        inventory.setItem(36, guiManager.createGuiItem(Material.IRON_PICKAXE, "&e&lTools Category", 
                Arrays.asList("&7Add all tools to selected shop", "&7Click to add")));
        
        inventory.setItem(37, guiManager.createGuiItem(Material.IRON_SWORD, "&e&lWeapons Category", 
                Arrays.asList("&7Add all weapons to selected shop", "&7Click to add")));
        
        inventory.setItem(38, guiManager.createGuiItem(Material.IRON_CHESTPLATE, "&e&lArmor Category", 
                Arrays.asList("&7Add all armor to selected shop", "&7Click to add")));
        
        inventory.setItem(39, guiManager.createGuiItem(Material.BREAD, "&e&lFood Category", 
                Arrays.asList("&7Add all food items to selected shop", "&7Click to add")));
        
        inventory.setItem(40, guiManager.createGuiItem(Material.STONE, "&e&lBlocks Category", 
                Arrays.asList("&7Add all building blocks to selected shop", "&7Click to add")));
        
        inventory.setItem(41, guiManager.createGuiItem(Material.REDSTONE, "&e&lRedstone Category", 
                Arrays.asList("&7Add all redstone items to selected shop", "&7Click to add")));
        
        inventory.setItem(42, guiManager.createGuiItem(Material.POTION, "&e&lPotions Category", 
                Arrays.asList("&7Add all potion items to selected shop", "&7Click to add")));
        
        // Back button
        inventory.setItem(49, guiManager.createGuiItem(Material.ARROW, "&7&lBack", 
                Collections.singletonList("&7Return to admin menu")));
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.ADMIN_BULK_ITEM_MANAGEMENT));
    }
    
    /**
     * Handle clicks in the bulk item management menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player
     * @param slot The clicked slot
     * @return True if the click was handled
     */
    public static boolean handleBulkItemManagementClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot) {
        // Get admin shops
        java.util.Collection<Shop> adminShopCollection = plugin.getShopManager().getAdminShops();
        List<Shop> adminShops = new java.util.ArrayList<>(adminShopCollection);
        
        // Check if the player clicked on a shop (slots 0-26)
        if (slot >= 0 && slot < 27 && slot < adminShops.size()) {
            // Selected shop
            Shop selectedShop = adminShops.get(slot);
            
            // Store the selected shop ID in the menu data
            MenuData menuData = new MenuData(MenuType.ADMIN_BULK_ITEM_MANAGEMENT);
            menuData.setData("selectedShopId", selectedShop.getId());
            guiManager.menuData.put(player.getUniqueId(), menuData);
            
            MessageUtils.sendMessage(player, "&aSelected shop: &f" + selectedShop.getName());
            MessageUtils.sendMessage(player, "&7Now click a category to add items");
            
            return true;
        }
        
        // Check if the player clicked on a category (slots 36-42)
        if (slot >= 36 && slot <= 42) {
            // Get the selected shop ID from menu data
            MenuData menuData = guiManager.getMenuData(player.getUniqueId());
            if (menuData == null || !menuData.getAllData().containsKey("selectedShopId")) {
                MessageUtils.sendErrorMessage(player, "Please select a shop first!");
                return true;
            }
            
            UUID selectedShopId = (UUID) menuData.getData("selectedShopId");
            Shop shop = plugin.getShopManager().getShop(selectedShopId);
            
            if (shop == null || !shop.isAdminShop()) {
                MessageUtils.sendErrorMessage(player, "Selected shop not found or is not an admin shop.");
                return true;
            }
            
            // Cast to AdminShop
            org.frizzlenpop.frizzlenShop.shops.AdminShop adminShop = 
                    (org.frizzlenpop.frizzlenShop.shops.AdminShop) shop;
            
            // Determine the category based on slot
            String category;
            switch (slot) {
                case 36: category = "tools"; break;
                case 37: category = "weapons"; break;
                case 38: category = "armor"; break;
                case 39: category = "food"; break;
                case 40: category = "blocks"; break;
                case 41: category = "redstone"; break;
                case 42: category = "potions"; break;
                default: category = null;
            }
            
            if (category != null) {
                // Add items from category
                int added = adminShop.addCategoryItems(category, plugin.getEconomyManager().getDefaultCurrency());
                
                if (added > 0) {
                    MessageUtils.sendSuccessMessage(player, "Added " + added + " items from the " + 
                            category + " category to " + shop.getName());
                } else {
                    MessageUtils.sendErrorMessage(player, "No items were added. Either the category is invalid or all items already exist in the shop.");
                }
            }
            
            return true;
        }
        
        // Check if the player clicked the back button
        if (slot == 49) {
            guiManager.openShopAdminMenu(player);
            return true;
        }
        
        return false;
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

    /**
     * Handle clicks in the price management menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player
     * @param slot The clicked slot
     * @param clickType The type of click
     * @return True if the click was handled
     */
    public static boolean handlePriceManagementClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, org.bukkit.event.inventory.ClickType clickType) {
        ConfigManager config = plugin.getConfigManager();
        boolean isShiftClick = clickType.isShiftClick();
        boolean isRightClick = clickType.isRightClick();
        
        switch (slot) {
            case 10: // Global price multiplier
                double globalMultiplier = config.getGlobalPriceMultiplier();
                if (isRightClick) {
                    // Decrease by 0.1, or 0.5 if shift clicking
                    globalMultiplier -= isShiftClick ? 0.5 : 0.1;
                } else {
                    // Increase by 0.1, or 0.5 if shift clicking
                    globalMultiplier += isShiftClick ? 0.5 : 0.1;
                }
                
                // Ensure minimum value of 0.1
                globalMultiplier = Math.max(0.1, globalMultiplier);
                
                // Update the config
                config.setGlobalPriceMultiplier(globalMultiplier);
                config.saveConfig();
                
                // Show success message
                MessageUtils.sendSuccessMessage(player, "Global price multiplier updated to: " + globalMultiplier);
                
                // Refresh the menu
                openPriceManagementMenu(guiManager, plugin, player);
                return true;
                
            case 12: // Default buy price
                double buyPrice = config.getDefaultBuyPrice();
                if (isRightClick) {
                    // Decrease by 10, or 50 if shift clicking
                    buyPrice -= isShiftClick ? 50 : 10;
                } else {
                    // Increase by 10, or 50 if shift clicking
                    buyPrice += isShiftClick ? 50 : 10;
                }
                
                // Ensure minimum value of 1
                buyPrice = Math.max(1, buyPrice);
                
                // Update the config
                config.setDefaultBuyPrice(buyPrice);
                config.saveConfig();
                
                // Show success message
                MessageUtils.sendSuccessMessage(player, "Default buy price updated to: " + buyPrice);
                
                // Refresh the menu
                openPriceManagementMenu(guiManager, plugin, player);
                return true;
                
            case 14: // Default sell price
                double sellPrice = config.getDefaultSellPrice();
                if (isRightClick) {
                    // Decrease by 10, or 50 if shift clicking
                    sellPrice -= isShiftClick ? 50 : 10;
                } else {
                    // Increase by 10, or 50 if shift clicking
                    sellPrice += isShiftClick ? 50 : 10;
                }
                
                // Ensure minimum value of 1
                sellPrice = Math.max(1, sellPrice);
                
                // Update the config
                config.setDefaultSellPrice(sellPrice);
                config.saveConfig();
                
                // Show success message
                MessageUtils.sendSuccessMessage(player, "Default sell price updated to: " + sellPrice);
                
                // Refresh the menu
                openPriceManagementMenu(guiManager, plugin, player);
                return true;
                
            case 16: // Sell price ratio
                double ratio = config.getSellPriceRatio();
                if (isRightClick) {
                    // Decrease by 0.05, or 0.1 if shift clicking
                    ratio -= isShiftClick ? 0.1 : 0.05;
                } else {
                    // Increase by 0.05, or 0.1 if shift clicking
                    ratio += isShiftClick ? 0.1 : 0.05;
                }
                
                // Ensure ratio is between 0.1 and 1.0
                ratio = Math.max(0.1, Math.min(1.0, ratio));
                
                // Update the config
                config.setSellPriceRatio(ratio);
                config.saveConfig();
                
                // Show success message
                MessageUtils.sendSuccessMessage(player, "Sell price ratio updated to: " + ratio);
                
                // Refresh the menu
                openPriceManagementMenu(guiManager, plugin, player);
                return true;
                
            case 20: // Dynamic pricing toggle
                boolean dynamicPricing = !config.isDynamicPricingEnabled();
                config.setDynamicPricingEnabled(dynamicPricing);
                config.saveConfig();
                
                MessageUtils.sendSuccessMessage(player, "Dynamic pricing " + (dynamicPricing ? "enabled" : "disabled"));
                
                // Refresh the menu
                openPriceManagementMenu(guiManager, plugin, player);
                return true;
                
            case 22: // Price fluctuation toggle
                boolean priceFluctuation = !config.isPriceFluctuationEnabled();
                config.setPriceFluctuationEnabled(priceFluctuation);
                config.saveConfig();
                
                MessageUtils.sendSuccessMessage(player, "Price fluctuation " + (priceFluctuation ? "enabled" : "disabled"));
                
                // Refresh the menu
                openPriceManagementMenu(guiManager, plugin, player);
                return true;
                
            case 31: // Back button
                guiManager.openShopAdminMenu(player);
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Handle clicks in the tax management menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player
     * @param slot The clicked slot
     * @param clickType The type of click
     * @return True if the click was handled
     */
    public static boolean handleTaxManagementClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, org.bukkit.event.inventory.ClickType clickType) {
        ConfigManager config = plugin.getConfigManager();
        boolean isShiftClick = clickType.isShiftClick();
        boolean isRightClick = clickType.isRightClick();
        
        switch (slot) {
            case 10: // Global tax rate
                double globalTaxRate = config.getGlobalTaxRate() * 100; // Convert to percentage
                if (isRightClick) {
                    // Decrease by 1%, or 5% if shift clicking
                    globalTaxRate -= isShiftClick ? 5 : 1;
                } else {
                    // Increase by 1%, or 5% if shift clicking
                    globalTaxRate += isShiftClick ? 5 : 1;
                }
                
                // Ensure tax rate is between 0 and 50%
                globalTaxRate = Math.max(0, Math.min(50, globalTaxRate));
                
                // Update the config (convert back to decimal)
                config.setGlobalTaxRate(globalTaxRate / 100);
                config.saveConfig();
                
                // Show success message
                MessageUtils.sendSuccessMessage(player, "Global tax rate updated to: " + globalTaxRate + "%");
                
                // Refresh the menu
                openTaxManagementMenu(guiManager, plugin, player);
                return true;
                
            case 12: // Admin shop tax rate
                double adminTaxRate = config.getAdminShopTaxRate() * 100; // Convert to percentage
                if (isRightClick) {
                    // Decrease by 1%, or 5% if shift clicking
                    adminTaxRate -= isShiftClick ? 5 : 1;
                } else {
                    // Increase by 1%, or 5% if shift clicking
                    adminTaxRate += isShiftClick ? 5 : 1;
                }
                
                // Ensure tax rate is between 0 and 50%
                adminTaxRate = Math.max(0, Math.min(50, adminTaxRate));
                
                // Update the config (convert back to decimal)
                config.setAdminShopTaxRate(adminTaxRate / 100);
                config.saveConfig();
                
                // Show success message
                MessageUtils.sendSuccessMessage(player, "Admin shop tax rate updated to: " + adminTaxRate + "%");
                
                // Refresh the menu
                openTaxManagementMenu(guiManager, plugin, player);
                return true;
                
            case 14: // Player shop tax rate
                double playerTaxRate = config.getPlayerShopTaxRate() * 100; // Convert to percentage
                if (isRightClick) {
                    // Decrease by 1%, or 5% if shift clicking
                    playerTaxRate -= isShiftClick ? 5 : 1;
                } else {
                    // Increase by 1%, or 5% if shift clicking
                    playerTaxRate += isShiftClick ? 5 : 1;
                }
                
                // Ensure tax rate is between 0 and 50%
                playerTaxRate = Math.max(0, Math.min(50, playerTaxRate));
                
                // Update the config (convert back to decimal)
                config.setPlayerShopTaxRate(playerTaxRate / 100);
                config.saveConfig();
                
                // Show success message
                MessageUtils.sendSuccessMessage(player, "Player shop tax rate updated to: " + playerTaxRate + "%");
                
                // Refresh the menu
                openTaxManagementMenu(guiManager, plugin, player);
                return true;
                
            case 16: // Minimum tax
                double minTax = config.getMinimumTax();
                if (isRightClick) {
                    // Decrease by 1, or 5 if shift clicking
                    minTax -= isShiftClick ? 5 : 1;
                } else {
                    // Increase by 1, or 5 if shift clicking
                    minTax += isShiftClick ? 5 : 1;
                }
                
                // Ensure minimum tax is non-negative
                minTax = Math.max(0, minTax);
                
                // Update the config
                config.setMinimumTax(minTax);
                config.saveConfig();
                
                // Show success message
                MessageUtils.sendSuccessMessage(player, "Minimum tax updated to: " + minTax);
                
                // Refresh the menu
                openTaxManagementMenu(guiManager, plugin, player);
                return true;
                
            case 20: // Maximum tax
                double maxTax = config.getMaximumTax();
                if (isRightClick) {
                    // Decrease by 10, or 50 if shift clicking
                    maxTax -= isShiftClick ? 50 : 10;
                } else {
                    // Increase by 10, or 50 if shift clicking
                    maxTax += isShiftClick ? 50 : 10;
                }
                
                // Ensure maximum tax is non-negative (0 = no maximum)
                maxTax = Math.max(0, maxTax);
                
                // Update the config
                config.setMaximumTax(maxTax);
                config.saveConfig();
                
                // Show success message
                MessageUtils.sendSuccessMessage(player, "Maximum tax updated to: " + maxTax + (maxTax == 0 ? " (no maximum)" : ""));
                
                // Refresh the menu
                openTaxManagementMenu(guiManager, plugin, player);
                return true;
                
            case 22: // Tax statistics
                // Simply show some additional stats in chat
                MessageUtils.sendMessage(player, "&e===== Tax Statistics =====");
                MessageUtils.sendMessage(player, "&7Tax collected today: &f" + config.getTaxCollectedToday());
                MessageUtils.sendMessage(player, "&7Total tax collected: &f" + config.getTotalTaxCollected());
                MessageUtils.sendMessage(player, "&7Tax collection account: &f" + 
                    (config.getTaxCollectionAccount() != null ? 
                    Bukkit.getOfflinePlayer(config.getTaxCollectionAccount()).getName() : 
                    "None (taxes are removed from economy)"));
                return true;
                
            case 31: // Back button
                guiManager.openShopAdminMenu(player);
                return true;
                
            default:
                return false;
        }
    }

    /**
     * Opens the shop statistics menu for the player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player
     */
    public static void openStatisticsMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        ConfigManager config = plugin.getConfigManager();
        ShopManager shopManager = plugin.getShopManager();
        
        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 36, ChatColor.translateAlternateColorCodes('&', "&6&lSHOP STATISTICS"));
        
        // Get statistics
        int totalShops = shopManager.getAllShops().size();
        int adminShops = shopManager.getAdminShops().size();
        int playerShops = shopManager.getPlayerShops().size();
        
        // Mock data for database-related statistics (in a real implementation, these would come from a database)
        int totalTransactions = 254;  // Mock data
        double totalRevenue = 12587.50;  // Mock data
        double totalTaxes = config.getTotalTaxCollected();
        
        // Create info items
        ItemStack totalShopsItem = GuiUtils.createInfoItem(Material.CHEST, "&e&lTotal Shops", 
                Arrays.asList("&7Total: &f" + totalShops, "&7Admin: &f" + adminShops, "&7Player: &f" + playerShops));
        
        ItemStack transactionsItem = GuiUtils.createInfoItem(Material.PAPER, "&e&lTransactions", 
                Arrays.asList("&7Total: &f" + totalTransactions, "&7Today: &f" + 15));  // Mock data
        
        ItemStack revenueItem = GuiUtils.createInfoItem(Material.GOLD_INGOT, "&e&lRevenue", 
                Arrays.asList("&7Total: &f$" + String.format("%.2f", totalRevenue), 
                              "&7Today: &f$" + String.format("%.2f", 450.75)));  // Mock data
        
        ItemStack taxItem = GuiUtils.createInfoItem(Material.EMERALD, "&e&lTaxes", 
                Arrays.asList("&7Total collected: &f$" + String.format("%.2f", totalTaxes), 
                              "&7Today: &f$" + String.format("%.2f", config.getTaxCollectedToday())));
        
        // Mock data for top sellers
        List<String> topSellersLore = new ArrayList<>();
        topSellersLore.add("&7These players have the most sales:");
        topSellersLore.add("&71. &fStevePlays");
        topSellersLore.add("&72. &fMinerGal");
        topSellersLore.add("&73. &fCrafterDude");
        
        ItemStack topSellersItem = GuiUtils.createInfoItem(Material.DIAMOND, "&e&lTop Sellers", topSellersLore);
        
        // Mock data for top items
        List<String> topItemsLore = new ArrayList<>();
        topItemsLore.add("&7Most popular items by sales:");
        topItemsLore.add("&71. &fDiamond Sword &7(&f45 sold&7)");
        topItemsLore.add("&72. &fEnchanted Golden Apple &7(&f30 sold&7)");
        topItemsLore.add("&73. &fElytra &7(&f18 sold&7)");
        topItemsLore.add("&74. &fNetherite Ingot &7(&f15 sold&7)");
        topItemsLore.add("&75. &fShulker Box &7(&f12 sold&7)");
        
        ItemStack topItemsItem = GuiUtils.createInfoItem(Material.ITEM_FRAME, "&e&lTop Items", topItemsLore);
        
        // Mock data for visitors
        ItemStack visitorsItem = GuiUtils.createInfoItem(Material.PLAYER_HEAD, "&e&lVisitors", 
                Arrays.asList("&7Total: &f135", "&7Today: &f12"));  // Mock data
        
        ItemStack backButton = GuiUtils.createActionItem(Material.ARROW, "&c&lBack to Admin Menu");
        
        // Add items to inventory
        inventory.setItem(10, totalShopsItem);
        inventory.setItem(12, transactionsItem);
        inventory.setItem(14, revenueItem);
        inventory.setItem(16, taxItem);
        inventory.setItem(20, topSellersItem);
        inventory.setItem(22, topItemsItem);
        inventory.setItem(24, visitorsItem);
        inventory.setItem(31, backButton);
        
        // Fill empty slots with glass panes
        GuiUtils.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        MenuData menuData = new MenuData(MenuType.SHOP_STATISTICS);
        guiManager.menuData.put(player.getUniqueId(), menuData);
    }
    
    /**
     * Handles clicks in the statistics menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player
     * @param slot The clicked slot
     * @param clickType The type of click
     * @return True if the click was handled
     */
    public static boolean handleStatisticsMenuClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, org.bukkit.event.inventory.ClickType clickType) {
        // Only handle back button
        if (slot == 31) {
            guiManager.openShopAdminMenu(player);
            return true;
        }
        
        // All other clicks are just informational items
        return true;
    }
} 