package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;
import org.frizzlenpop.frizzlenShop.utils.ItemLoreUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Handles the shop management menu GUI
 */
public class ShopManagementMenuHandler {

    /**
     * Open the shop management menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param shop The shop to manage
     */
    public static void openShopManagementMenu(GuiManager guiManager, FrizzlenShop plugin, Player player, Shop shop) {
        // Create inventory
        String title = "Manage Shop: " + shop.getName();
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Shop info
        ItemStack infoItem = guiManager.createGuiItem(Material.BOOK, "&e&lShop Info", 
                Arrays.asList(
                    "&7Name: &f" + shop.getName(),
                    "&7Type: &f" + (shop.isAdminShop() ? "Admin Shop" : "Player Shop"),
                    "&7Status: &f" + (shop.isOpen() ? "&aOpen" : "&cClosed"),
                    "&7Items: &f" + shop.getItems().size(),
                    "&7Location: &f" + formatLocation(shop.getLocation())
                ));
        inventory.setItem(4, infoItem);
        
        // Shop management options
        ItemStack itemsItem = guiManager.createGuiItem(Material.CHEST, "&e&lManage Items", 
                Arrays.asList(
                    "&7Add, remove, or edit items",
                    "&7Current items: &f" + shop.getItems().size(),
                    "",
                    "&7Click to manage items"
                ));
        inventory.setItem(19, itemsItem);
        
        ItemStack settingsItem = guiManager.createGuiItem(Material.REDSTONE_TORCH, "&e&lShop Settings", 
                Arrays.asList(
                    "&7Change shop settings",
                    "&7Name, description, etc.",
                    "",
                    "&7Click to change settings"
                ));
        inventory.setItem(22, settingsItem);
        
        ItemStack statsItem = guiManager.createGuiItem(Material.PAPER, "&e&lShop Statistics", 
                Arrays.asList(
                    "&7View shop statistics",
                    "&7Sales, revenue, etc.",
                    "",
                    "&7Click to view stats"
                ));
        inventory.setItem(25, statsItem);
        
        // Toggle shop status
        ItemStack statusItem = guiManager.createGuiItem(
                shop.isOpen() ? Material.REDSTONE_BLOCK : Material.EMERALD_BLOCK, 
                shop.isOpen() ? "&c&lClose Shop" : "&a&lOpen Shop", 
                Arrays.asList(
                    "&7Current status: " + (shop.isOpen() ? "&aOpen" : "&cClosed"),
                    "",
                    "&7Click to toggle"
                ));
        inventory.setItem(37, statusItem);
        
        // Delete shop
        ItemStack deleteItem = guiManager.createGuiItem(Material.BARRIER, "&c&lDelete Shop", 
                Arrays.asList(
                    "&7Permanently delete this shop",
                    "&cWarning: This action cannot be undone!",
                    "",
                    "&7Click to delete"
                ));
        inventory.setItem(43, deleteItem);
        
        // Back button
        ItemStack backItem = guiManager.createGuiItem(Material.ARROW, "&7&lBack", 
                Collections.singletonList("&7Return to previous menu"));
        inventory.setItem(49, backItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.SHOP_MANAGEMENT, shop.getId()));
    }
    
    /**
     * Handle a click in the shop management menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        // Get the shop ID from menu data
        UUID shopId = menuData.getUUID("id");
        
        if (shopId == null) {
            return false;
        }
        
        // Get the shop
        Shop shop = plugin.getShopManager().getShop(shopId);
        
        if (shop == null) {
            MessageUtils.sendErrorMessage(player, "This shop no longer exists.");
            player.closeInventory();
            return true;
        }
        
        switch (slot) {
            case 19: // Manage Items
                openItemsMenu(guiManager, plugin, player, shop);
                return true;
                
            case 22: // Shop Settings
                // Open shop settings menu
                ShopSettingsMenuHandler.openShopSettingsMenu(guiManager, plugin, player, shop);
                return true;
                
            case 25: // Shop Statistics
                // TODO: Implement statistics menu
                MessageUtils.sendMessage(player, "&eShop statistics would open here");
                return true;
                
            case 37: // Toggle shop status
                // Toggle shop status
                shop.setOpen(!shop.isOpen());
                
                // Refresh menu
                openShopManagementMenu(guiManager, plugin, player, shop);
                return true;
                
            case 43: // Delete shop
                // TODO: Add confirmation dialog
                boolean deleted = plugin.getShopManager().deleteShop(shopId);
                
                if (deleted) {
                    player.closeInventory();
                    MessageUtils.sendSuccessMessage(player, "Shop deleted successfully!");
                } else {
                    MessageUtils.sendErrorMessage(player, "Failed to delete shop.");
                }
                return true;
                
            case 49: // Back
                player.closeInventory();
                guiManager.openMyShopsMenu(player);
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Open the items management menu for a shop
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param shop The shop to manage
     */
    private static void openItemsMenu(GuiManager guiManager, FrizzlenShop plugin, Player player, Shop shop) {
        // Create inventory
        String title = "Shop Items: " + shop.getName();
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Show shop items
        List<ShopItem> items = shop.getItems();
        
        for (int i = 0; i < items.size() && i < 45; i++) {
            ShopItem item = items.get(i);
            
            // Clone the ItemStack to modify it
            ItemStack displayItem = item.getItem().clone();
            
            // Get existing lore or create new list
            List<String> lore = ItemLoreUtils.getLore(displayItem);
            if (lore == null) {
                lore = new java.util.ArrayList<>();
            } else {
                lore.add(""); // Add a blank line to separate original lore from shop info
            }
            
            // Add shop information to lore
            lore.add(MessageUtils.colorize("&7Price: &6" + plugin.getEconomyManager().formatCurrency(item.getPrice(), plugin.getEconomyManager().getDefaultCurrency())));
            if (shop.isAdminShop()) {
                lore.add(MessageUtils.colorize("&7Stock: &fUnlimited"));
            } else {
                lore.add(MessageUtils.colorize("&7Stock: &f" + item.getStock()));
            }
            lore.add(MessageUtils.colorize("&eClick to manage this item"));
            
            // Set the updated lore
            ItemLoreUtils.setLore(displayItem, lore);
            
            // Add the item to the inventory
            inventory.setItem(i, displayItem);
        }
        
        // Add item button
        ItemStack addItem = guiManager.createGuiItem(Material.EMERALD, "&a&lAdd Item", 
                Arrays.asList(
                    "&7Add a new item to the shop",
                    "",
                    "&7Click while holding an item"
                ));
        inventory.setItem(53, addItem);
        
        // Back button
        ItemStack backItem = guiManager.createGuiItem(Material.ARROW, "&7&lBack", 
                Collections.singletonList("&7Return to shop management"));
        inventory.setItem(49, backItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data for items menu
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.SHOP_ITEMS, shop.getId()));
    }
    
    /**
     * Format a location for display
     *
     * @param location The location to format
     * @return The formatted location
     */
    private static String formatLocation(org.bukkit.Location location) {
        if (location == null) {
            return "Unknown";
        }
        
        return location.getWorld().getName() + " (" + 
                (int) location.getX() + ", " + 
                (int) location.getY() + ", " + 
                (int) location.getZ() + ")";
    }
} 