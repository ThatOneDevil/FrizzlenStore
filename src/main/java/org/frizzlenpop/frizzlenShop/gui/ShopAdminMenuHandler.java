package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.config.ConfigManager;
import org.frizzlenpop.frizzlenShop.listeners.ChatListener;
import org.frizzlenpop.frizzlenShop.shops.AdminShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopManager;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;
import org.frizzlenpop.frizzlenShop.utils.GuiUtils;
import org.frizzlenpop.frizzlenShop.economy.MarketAnalyzer;
import org.frizzlenpop.frizzlenShop.economy.CraftingRelationManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.ArrayList;
import java.util.Comparator;

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
        
        // Market trends item
        ItemStack marketTrendsItem = guiManager.createGuiItem(Material.CLOCK, "&d&lMarket Trends", 
                Arrays.asList(
                    "&7View current market trends",
                    "&7based on dynamic pricing",
                    "",
                    "&7Click to view"
                ));
        
        // Crafting opportunities item
        ItemStack craftingOpportunitiesItem = guiManager.createGuiItem(Material.CRAFTING_TABLE, "&d&lCrafting Opportunities", 
                Arrays.asList(
                    "&7View profitable crafting opportunities",
                    "&7based on current market prices",
                    "",
                    "&7Click to view"
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
        
        inventory.setItem(20, statsItem);
        inventory.setItem(22, bulkItemManagementItem);
        inventory.setItem(24, marketTrendsItem);
        inventory.setItem(26, craftingOpportunitiesItem);
        
        inventory.setItem(49, backItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Store menu data
        MenuData menuData = new MenuData(MenuType.SHOP_ADMIN_MENU);
        guiManager.updateMenuData(player.getUniqueId(), menuData);
        
        // Open inventory
        player.openInventory(inventory);
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
                
            case 20: // Shop Statistics
                // Show shop statistics
                openStatisticsMenu(guiManager, plugin, player);
                return true;
                
            case 22: // Bulk Item Management
                // Open the bulk item management menu
                openBulkItemManagementMenu(guiManager, plugin, player);
                return true;
                
            case 24: // Market Trends
                // Open the market trends menu
                guiManager.openMarketTrendsMenu(player);
                return true;
                
            case 26: // Crafting Opportunities
                // Open the crafting opportunities menu
                guiManager.openCraftingOpportunitiesMenu(player);
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
                
                // Use the dynamic pricing manager if available, otherwise just update config
                if (plugin.getDynamicPricingManager() != null) {
                    plugin.getDynamicPricingManager().setDynamicPricingEnabled(dynamicPricing, player);
                } else {
                    config.setDynamicPricingEnabled(dynamicPricing);
                    config.saveConfig();
                    
                    if (dynamicPricing) {
                        MessageUtils.sendMessage(player, "&aDynamic pricing enabled. Server restart recommended to fully activate the system.");
                    } else {
                        MessageUtils.sendMessage(player, "&cDynamic pricing disabled.");
                    }
                }
                
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
                guiManager.returnToPreviousMenu(player);
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
                guiManager.returnToPreviousMenu(player);
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

    /**
     * Handle clicks in the admin shop management menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player
     * @param slot The clicked slot
     * @param data The menu data
     * @return True if the click was handled
     */
    public static boolean handleAdminShopManagementClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData data) {
        // Back button is at slot 49
        if (slot == 49) {
            guiManager.openShopAdminMenu(player);
            return true;
        }
        
        // Slots 0-44 are shop buttons
        if (slot >= 0 && slot < 45) {
            // Get all shops
            java.util.Collection<Shop> shopCollection = plugin.getShopManager().getAllShops();
            List<Shop> shops = new java.util.ArrayList<>(shopCollection);
            
            // Check if the slot corresponds to a valid shop
            if (slot < shops.size()) {
                Shop shop = shops.get(slot);
                
                // Open shop management menu for the selected shop
                guiManager.openShopManagementMenu(player, shop.getId());
                return true;
            }
        }
        
        return false;
    }

    /**
     * Opens the market trends menu for the player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player
     */
    public static void openMarketTrendsMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Create inventory
        String title = "Market Trends";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Check if dynamic pricing is enabled
        boolean dynamicPricingEnabled = plugin.getConfigManager().isDynamicPricingEnabled();
        
        if (!dynamicPricingEnabled || plugin.getDynamicPricingManager() == null) {
            // Dynamic pricing is not enabled, show information message
            ItemStack infoItem = guiManager.createGuiItem(
                Material.BARRIER, 
                "&c&lDynamic Pricing Disabled", 
                Arrays.asList(
                    "&7Dynamic pricing is currently disabled.",
                    "&7Enable it in the Price Management menu",
                    "&7to see market trends."
                )
            );
            inventory.setItem(22, infoItem);
        } else {
            // Get trending items
            Map<Material, Double> trendingItems = plugin.getDynamicPricingManager().getTrendingItems(27);
            
            if (trendingItems.isEmpty()) {
                // No market data yet
                ItemStack infoItem = guiManager.createGuiItem(
                    Material.PAPER, 
                    "&e&lNo Market Data Yet", 
                    Arrays.asList(
                        "&7There is no market data available yet.",
                        "&7Market trends will appear as players buy",
                        "&7and sell items in shops."
                    )
                );
                inventory.setItem(22, infoItem);
            } else {
                // Display trending items
                int slot = 10;
                for (Map.Entry<Material, Double> entry : trendingItems.entrySet()) {
                    Material material = entry.getKey();
                    double trend = entry.getValue();
                    
                    List<String> lore = new ArrayList<>();
                    
                    // Trend description
                    if (trend > 0.3) {
                        lore.add("&c&lRapidly Rising Prices");
                        lore.add("&7Demand is much higher than supply");
                        lore.add("&7Prices are increasing significantly");
                    } else if (trend > 0.1) {
                        lore.add("&e&lSlightly Rising Prices");
                        lore.add("&7Demand is higher than supply");
                        lore.add("&7Prices are increasing gradually");
                    } else if (trend < -0.3) {
                        lore.add("&a&lRapidly Falling Prices");
                        lore.add("&7Supply is much higher than demand");
                        lore.add("&7Prices are decreasing significantly");
                    } else if (trend < -0.1) {
                        lore.add("&2&lSlightly Falling Prices");
                        lore.add("&7Supply is higher than demand");
                        lore.add("&7Prices are decreasing gradually");
                    } else {
                        lore.add("&f&lStable Prices");
                        lore.add("&7Supply and demand are balanced");
                        lore.add("&7Prices are relatively stable");
                    }
                    
                    // Add trend value
                    String trendFormat = String.format("%.2f", trend);
                    lore.add("");
                    lore.add("&7Trend value: &f" + trendFormat);
                    lore.add("&7(Positive = rising, negative = falling)");
                    
                    // Add market advice
                    lore.add("");
                    if (trend > 0) {
                        lore.add("&aGood time to sell!");
                    } else {
                        lore.add("&aGood time to buy!");
                    }
                    
                    // Create item
                    ItemStack trendItem = new ItemStack(material);
                    ItemMeta meta = trendItem.getItemMeta();
                    meta.setDisplayName(ChatColor.GREEN + formatMaterialName(material.toString()));
                    meta.setLore(lore);
                    trendItem.setItemMeta(meta);
                    
                    inventory.setItem(slot, trendItem);
                    slot++;
                    
                    if (slot % 9 == 8) {
                        slot += 2; // Skip to next row
                    }
                    
                    if (slot >= 36) {
                        break; // Only show 27 items max
                    }
                }
                
                // Add legend
                ItemStack risingItem = guiManager.createGuiItem(
                    Material.RED_CONCRETE, 
                    "&c&lRising Prices", 
                    Arrays.asList(
                        "&7Items with rising prices have",
                        "&7higher demand than supply.",
                        "",
                        "&7Good time to &fsell &7these items!"
                    )
                );
                inventory.setItem(45, risingItem);
                
                ItemStack stableItem = guiManager.createGuiItem(
                    Material.WHITE_CONCRETE, 
                    "&f&lStable Prices", 
                    Arrays.asList(
                        "&7Items with stable prices have",
                        "&7balanced supply and demand.",
                        "",
                        "&7Prices are at normal levels."
                    )
                );
                inventory.setItem(46, stableItem);
                
                ItemStack fallingItem = guiManager.createGuiItem(
                    Material.GREEN_CONCRETE, 
                    "&a&lFalling Prices", 
                    Arrays.asList(
                        "&7Items with falling prices have",
                        "&7higher supply than demand.",
                        "",
                        "&7Good time to &fbuy &7these items!"
                    )
                );
                inventory.setItem(47, fallingItem);
            }
        }
        
        // Add refresh button
        ItemStack refreshButton = guiManager.createGuiItem(
            Material.CLOCK, 
            "&e&lRefresh", 
            Arrays.asList("&7Click to refresh market trends")
        );
        inventory.setItem(53, refreshButton);
        
        // Add back button
        ItemStack backButton = guiManager.createGuiItem(
            Material.ARROW, 
            "&c&lBack to Admin Menu", 
            Arrays.asList("&7Click to return to the main admin menu")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.MARKET_TRENDS));
    }

    /**
     * Handles clicks in the market trends menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player
     * @param slot The clicked slot
     * @return True if the click was handled
     */
    public static boolean handleMarketTrendsClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot) {
        switch (slot) {
            case 49: // Back button
                guiManager.openShopAdminMenu(player);
                return true;
            
            case 53: // Refresh button
                openMarketTrendsMenu(guiManager, plugin, player);
                return true;
            
            default:
                return true; // Consume all clicks to prevent item taking
        }
    }

    /**
     * Formats a material name to be more readable
     *
     * @param materialName The material name to format
     * @return The formatted name
     */
    private static String formatMaterialName(String materialName) {
        String[] words = materialName.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                     .append(word.substring(1))
                     .append(" ");
            }
        }
        
        return result.toString().trim();
    }

    /**
     * Opens the crafting opportunities menu
     * This menu shows items that are profitable to craft based on current market prices
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player
     */
    public static void openCraftingOpportunitiesMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Crafting Opportunities");
        
        // Get market analyzer and crafting relation manager
        MarketAnalyzer marketAnalyzer = plugin.getMarketAnalyzer();
        CraftingRelationManager craftingManager = plugin.getCraftingRelationManager();
        
        if (marketAnalyzer == null || craftingManager == null) {
            MessageUtils.sendErrorMessage(player, "Crafting opportunities are not available.");
            return;
        }
        
        // Create item list with profit margins
        List<ProfitableItem> profitableItems = new ArrayList<>();
        
        // Check all craftable items
        for (Material material : Material.values()) {
            if (craftingManager.isCraftedItem(material) && material.isItem()) {
                double profitMargin = marketAnalyzer.getCraftingProfitMargin(material);
                if (profitMargin > 0) {
                    profitableItems.add(new ProfitableItem(material, profitMargin));
                }
            }
        }
        
        // Sort by profit margin (highest first)
        profitableItems.sort(Comparator.comparing(ProfitableItem::getProfitMargin).reversed());
        
        // Limit to top 45 items (to fit in the inventory)
        int count = Math.min(profitableItems.size(), 45);
        
        // Add items to inventory
        for (int i = 0; i < count; i++) {
            ProfitableItem profitItem = profitableItems.get(i);
            
            // Create item with profit information
            ItemStack item = new ItemStack(profitItem.getMaterial());
            ItemMeta meta = item.getItemMeta();
            
            // Format name
            String name = ChatColor.GREEN + formatMaterialName(profitItem.getMaterial().toString());
            meta.setDisplayName(name);
            
            // Create lore with crafting and profit information
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GOLD + "Profit Margin: " + ChatColor.YELLOW + 
                    String.format("%.1f%%", profitItem.getProfitMargin()));
            
            // Get component information
            Map<Material, Integer> components = craftingManager.getComponents(profitItem.getMaterial());
            
            lore.add(ChatColor.LIGHT_PURPLE + "Components:");
            for (Map.Entry<Material, Integer> entry : components.entrySet()) {
                Material component = entry.getKey();
                int quantity = entry.getValue();
                double price = marketAnalyzer.getBasePrice(component);
                
                lore.add(ChatColor.GRAY + "- " + quantity + "x " + formatMaterialName(component.toString()) +
                        ChatColor.GRAY + " (" + plugin.getEconomyManager().formatCurrency(price, "coin") + " each)");
            }
            
            // Add total craft cost
            double craftCost = craftingManager.calculateCraftValue(profitItem.getMaterial(), marketAnalyzer);
            lore.add(ChatColor.AQUA + "Total Cost: " + 
                    ChatColor.WHITE + plugin.getEconomyManager().formatCurrency(craftCost, "coin"));
            
            // Add market value
            double marketValue = marketAnalyzer.getSuggestedPrice(profitItem.getMaterial(), true);
            lore.add(ChatColor.AQUA + "Market Value: " + 
                    ChatColor.WHITE + plugin.getEconomyManager().formatCurrency(marketValue, "coin"));
            
            // Add profit per item
            double profit = marketValue - craftCost;
            lore.add(ChatColor.GREEN + "Profit: " + 
                    ChatColor.WHITE + plugin.getEconomyManager().formatCurrency(profit, "coin") + " per item");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            // Add to inventory
            inventory.setItem(i, item);
        }
        
        // Add refresh button
        ItemStack refreshButton = guiManager.createGuiItem(Material.EMERALD, 
                ChatColor.GREEN + "Refresh", 
                Collections.singletonList(ChatColor.GRAY + "Click to refresh the crafting opportunities"));
        inventory.setItem(49, refreshButton);
        
        // Add back button
        ItemStack backButton = guiManager.createGuiItem(Material.ARROW, 
                ChatColor.RED + "Back to Admin Menu", 
                Collections.singletonList(ChatColor.GRAY + "Return to the admin menu"));
        inventory.setItem(53, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Register menu in GUI manager
        MenuData menuData = new MenuData(MenuType.CRAFTING_OPPORTUNITIES);
        guiManager.menuData.put(player.getUniqueId(), menuData);
        
        // Open the inventory
        player.openInventory(inventory);
    }
    
    /**
     * Handle click in the crafting opportunities menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleCraftingOpportunitiesClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot) {
        // Check for refresh button
        if (slot == 49) {
            openCraftingOpportunitiesMenu(guiManager, plugin, player);
            return true;
        }
        
        // Check for back button
        if (slot == 53) {
            openShopAdminMenu(guiManager, plugin, player);
            return true;
        }
        
        return true; // Consume all clicks to prevent item taking
    }
    
    /**
     * Helper class to track profitable items for the crafting opportunities menu
     */
    private static class ProfitableItem {
        private final Material material;
        private final double profitMargin;
        
        public ProfitableItem(Material material, double profitMargin) {
            this.material = material;
            this.profitMargin = profitMargin;
        }
        
        public Material getMaterial() {
            return material;
        }
        
        public double getProfitMargin() {
            return profitMargin;
        }
    }
} 