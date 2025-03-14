package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.AdminShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the admin shops menu GUI
 */
public class AdminShopsMenuHandler {

    /**
     * Open the admin shops menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openAdminShopsMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Create inventory
        String title = "Admin Shops";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Debug the total shops in the system
        Collection<Shop> allShops = plugin.getShopManager().getAllShops();
        player.sendMessage(ChatColor.YELLOW + "Debug: Total shops in system: " + allShops.size());
        
        // Check each shop individually to identify issues
        for (Shop shop : allShops) {
            boolean isAdmin = shop.isAdminShop();
            boolean isOpen = shop.isOpen();
            player.sendMessage(ChatColor.YELLOW + "Shop: " + shop.getName() + " - Admin: " + isAdmin + ", Open: " + isOpen);
        }
        
        // Count admin shops directly from the ShopManager for verification
        Collection<Shop> adminShopsFromManager = plugin.getShopManager().getAdminShops();
        player.sendMessage(ChatColor.YELLOW + "Debug: Admin shops from ShopManager: " + adminShopsFromManager.size());
        
        // Get all admin shops - only filter for admin shops, not checking open status
        List<Shop> adminShops = plugin.getShopManager().getAllShops().stream()
                .filter(Shop::isAdminShop)
                .collect(Collectors.toList());
        
        // Debug message to player showing how many admin shops were found
        player.sendMessage(ChatColor.YELLOW + "Debug: Found " + adminShops.size() + " admin shops with stream filter");
        
        // If no admin shops were found, offer to create a test admin shop
        if (adminShops.isEmpty() && player.hasPermission("frizzlenshop.admin")) {
            player.sendMessage(ChatColor.RED + "No admin shops found. You can create test shops or regenerate the default shops.");
            
            ItemStack createTestShopItem = guiManager.createGuiItem(
                Material.EMERALD_BLOCK,
                "&a&lCreate Test Admin Shop",
                Arrays.asList(
                    "&7No admin shops were found in the system.",
                    "&7Click to create a test admin shop at your location."
                )
            );
            inventory.setItem(20, createTestShopItem);
            
            // Add regenerate default shops option
            ItemStack regenerateShopsItem = guiManager.createGuiItem(
                Material.DIAMOND_BLOCK,
                "&b&lRegenerate Default Shops",
                Arrays.asList(
                    "&7Recreate all the default category shops.",
                    "&7(Tools, Weapons, Armor, Food, etc.)",
                    "&7This will reset all shop prices to defaults."
                )
            );
            inventory.setItem(24, regenerateShopsItem);
            
            // Add navigation buttons
            ItemStack backButton = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack to Main Menu",
                Collections.singletonList("&7Return to the main menu")
            );
            inventory.setItem(49, backButton);
            
            // Fill empty slots
            guiManager.fillEmptySlots(inventory);
            
            // Open inventory
            player.openInventory(inventory);
            
            // Store menu data with special flag
            Map<String, Object> data = new HashMap<>();
            data.put("createTestShop", true);
            guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.ADMIN_SHOPS_MENU, data));
            
            return;
        }
        
        // Sort shops by name
        adminShops.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        
        // Show shops in the inventory
        int slot = 0;
        for (Shop shop : adminShops) {
            if (slot >= 45) break; // Max 45 shops per page
            
            // Include shop status in the display
            String statusText = shop.isOpen() ? "&aOpen" : "&cClosed";
            
            ItemStack shopIcon = guiManager.createGuiItem(
                Material.GOLD_BLOCK,
                "&e&l" + shop.getName(),
                Arrays.asList(
                    "&7Type: &6Admin Shop",
                    "&7Status: " + statusText,
                    "&7Location: &f" + formatLocation(shop.getLocation()),
                    "&7Items: &f" + shop.getItems().size(),
                    "",
                    "&eClick to view items"
                )
            );
            
            inventory.setItem(slot, shopIcon);
            slot++;
        }
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
            Material.ARROW,
            "&c&lBack to Main Menu",
            Collections.singletonList("&7Return to the main menu")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data with the list of shops
        MenuData menuData = new MenuData(MenuType.ADMIN_SHOPS_MENU);
        menuData.setData("shops", adminShops);
        guiManager.updateMenuData(player.getUniqueId(), menuData);
    }
    
    /**
     * Handle a click in the admin shops menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        // Get shops from menu data
        @SuppressWarnings("unchecked")
        List<Shop> shops = (List<Shop>) menuData.getData("shops");
        boolean isCreateTestShopMenu = menuData.getData("createTestShop") != null && (boolean) menuData.getData("createTestShop");
        
        // Handle the back button
        if (slot == 49) {
            guiManager.returnToPreviousMenu(player);
            return true;
        }
        
        // Create test shop button (only in the creation mode)
        if (isCreateTestShopMenu && slot == 20) {
            if (player.hasPermission("frizzlenshop.admin")) {
                // Create a test admin shop at the player's location
                String shopName = "Test Admin Shop";
                AdminShop shop = plugin.getShopManager().createAdminShop(shopName, player.getLocation());
                
                if (shop != null) {
                    // Add some example items to the shop
                    shop.addItem(new ItemStack(Material.DIAMOND), 100.0, 90.0, plugin.getEconomyManager().getDefaultCurrency(), -1);
                    shop.addItem(new ItemStack(Material.IRON_INGOT), 10.0, 9.0, plugin.getEconomyManager().getDefaultCurrency(), -1);
                    shop.addItem(new ItemStack(Material.GOLD_INGOT), 25.0, 20.0, plugin.getEconomyManager().getDefaultCurrency(), -1);
                    
                    player.sendMessage(ChatColor.GREEN + "Test admin shop created successfully! Refreshing view...");
                    // Reopen the admin shops menu
                    guiManager.openAdminShopsMenu(player);
                } else {
                    player.sendMessage(ChatColor.RED + "Failed to create test admin shop. Admin shops might be disabled in config.");
                    guiManager.openMainMenu(player);
                }
                return true;
            }
            return false;
        }
        
        // Regenerate default shops button (only in the creation mode)
        if (isCreateTestShopMenu && slot == 24) {
            if (player.hasPermission("frizzlenshop.admin")) {
                player.sendMessage(ChatColor.YELLOW + "Regenerating default admin shops...");
                
                // Force admin shop refresh in config
                plugin.getConfigManager().setForceAdminShopRefresh(true);
                plugin.getConfigManager().saveConfig();
                
                // Get the AdminShopPopulator
                org.frizzlenpop.frizzlenShop.shops.AdminShopPopulator populator = 
                    new org.frizzlenpop.frizzlenShop.shops.AdminShopPopulator(plugin);
                
                // Create all the default shops
                player.sendMessage(ChatColor.YELLOW + "Creating Main Admin Shop...");
                populator.createMainAdminShop();
                
                player.sendMessage(ChatColor.YELLOW + "Creating category shops...");
                populator.createCategoryShop("Tools");
                populator.createCategoryShop("Weapons");
                populator.createCategoryShop("Armor");
                populator.createCategoryShop("Food");
                populator.createCategoryShop("Blocks");
                populator.createCategoryShop("Resources");
                
                // Reset the flag
                plugin.getConfigManager().setForceAdminShopRefresh(false);
                plugin.getConfigManager().saveConfig();
                
                player.sendMessage(ChatColor.GREEN + "Default admin shops have been regenerated! Refreshing view...");
                // Reopen the admin shops menu
                guiManager.openAdminShopsMenu(player);
                return true;
            }
            return false;
        }
        
        // Regular shop list handling
        if (shops != null) {
            // Check if the click was on a shop
            if (slot < 45 && slot < shops.size()) {
                Shop shop = shops.get(slot);
                
                // Show shop items regardless of open status
                openShopItemsView(guiManager, plugin, player, shop);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Open the shop items view for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the view for
     * @param shop The shop to view
     */
    private static void openShopItemsView(GuiManager guiManager, FrizzlenShop plugin, Player player, Shop shop) {
        // Create inventory
        String title = "Admin Shop - " + shop.getName();
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Get all items in the shop
        List<ShopItem> items = new ArrayList<>(shop.getItems());
        
        // Debug message to show how many items were found in the shop
        player.sendMessage(ChatColor.YELLOW + "Debug: Found " + items.size() + " items in shop " + shop.getName());
        
        // Sort items by type
        items.sort(Comparator.comparing(item -> item.getItem().getType().name()));
        
        // Show items in the inventory (max 45 items per page)
        int itemsPerPage = 45;
        int page = 0; // First page
        
        int startIndex = page * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            ShopItem shopItem = items.get(i);
            ItemStack item = shopItem.getItem().clone();
            
            // Add price information to the lore
            ItemMeta meta = item.getItemMeta();
            List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            
            // Add spacing
            if (!lore.isEmpty()) {
                lore.add("");
            }
            
            // Add price information
            lore.add(ChatColor.GRAY + "Buy Price: " + ChatColor.YELLOW + 
                    plugin.getEconomyManager().formatCurrency(shopItem.getBuyPrice(), shopItem.getCurrency()));
            lore.add(ChatColor.GRAY + "Sell Price: " + ChatColor.YELLOW + 
                    plugin.getEconomyManager().formatCurrency(shopItem.getSellPrice(), shopItem.getCurrency()));
            
            // Add stock information
            String stockText = shopItem.getStock() == -1 ? "Unlimited" : String.valueOf(shopItem.getStock());
            lore.add(ChatColor.GRAY + "Stock: " + ChatColor.YELLOW + stockText);
            
            // Add instruction
            lore.add("");
            lore.add(ChatColor.GREEN + "Click to view details");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
            
            inventory.setItem(i - startIndex, item);
        }
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
            Material.ARROW,
            "&c&lBack to Admin Shops",
            Collections.singletonList("&7Return to the admin shops list")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        Map<String, Object> data = new HashMap<>();
        data.put("shop", shop);
        data.put("items", items);
        data.put("page", page);
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.SHOP_ITEMS, data));
    }
    
    /**
     * Format a location for display
     *
     * @param location The location to format
     * @return The formatted location string
     */
    private static String formatLocation(org.bukkit.Location location) {
        if (location == null) {
            return "Unknown";
        }
        
        return location.getWorld().getName() + 
               " (" + Math.round(location.getX()) + 
               ", " + Math.round(location.getY()) + 
               ", " + Math.round(location.getZ()) + ")";
    }
} 