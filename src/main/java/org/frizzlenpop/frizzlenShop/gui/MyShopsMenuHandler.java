package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.*;

/**
 * Handles the my shops menu GUI
 */
public class MyShopsMenuHandler {

    /**
     * Open the my shops menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openMyShopsMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Get player's shops
        List<Shop> playerShops = plugin.getShopManager().getPlayerShops(player.getUniqueId());
        
        // Create inventory
        String title = "My Shops";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Add shop items
        for (int i = 0; i < playerShops.size() && i < 36; i++) {
            Shop shop = playerShops.get(i);
            int slot = getSlotFromIndex(i);
            
            // Create shop display item
            ItemStack shopItem = createShopDisplayItem(guiManager, shop);
            
            inventory.setItem(slot, shopItem);
        }
        
        // Create shop button
        int shopLimit = plugin.getShopManager().getMaxShopsForPlayer(player);
        boolean canCreateMore = playerShops.size() < shopLimit;
        
        ItemStack createShopItem = guiManager.createGuiItem(
                canCreateMore ? Material.EMERALD : Material.BARRIER, 
                canCreateMore ? "&a&lCreate New Shop" : "&c&lShop Limit Reached", 
                Arrays.asList(
                    "&7Shops: &f" + playerShops.size() + "/" + shopLimit,
                    canCreateMore ? "&7Click to create a new shop" : "&cYou have reached your shop limit"
                ));
        inventory.setItem(53, createShopItem);
        
        // Back button
        ItemStack backItem = guiManager.createGuiItem(Material.ARROW, "&7&lBack", 
                Collections.singletonList("&7Return to main menu"));
        inventory.setItem(49, backItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.MY_SHOPS_MENU));
    }
    
    /**
     * Handle a click in the my shops menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot) {
        // Get player's shops
        List<Shop> playerShops = plugin.getShopManager().getPlayerShops(player.getUniqueId());
        
        // Check if it's a shop slot
        int index = getIndexFromSlot(slot);
        if (index != -1 && index < playerShops.size()) {
            Shop shop = playerShops.get(index);
            
            // Open shop management menu
            guiManager.openShopManagementMenu(player, shop.getId());
            return true;
        }
        
        // Create shop button
        if (slot == 53) {
            int shopLimit = plugin.getShopManager().getMaxShopsForPlayer(player);
            if (playerShops.size() < shopLimit) {
                // Open shop creation menu
                player.closeInventory();
                player.performCommand("shop create");
            } else {
                MessageUtils.sendErrorMessage(player, "You have reached your shop limit.");
            }
            return true;
        }
        
        // Back button
        if (slot == 49) {
            guiManager.openMainMenu(player);
            return true;
        }
        
        return false;
    }
    
    /**
     * Create an ItemStack to display a shop in the menu
     *
     * @param guiManager The GUI manager
     * @param shop The shop to display
     * @return The ItemStack
     */
    private static ItemStack createShopDisplayItem(GuiManager guiManager, Shop shop) {
        Material material = shop.isAdminShop() ? Material.DIAMOND_BLOCK : Material.CHEST;
        String name = "&e&l" + shop.getName();
        
        List<String> lore = new ArrayList<>();
        lore.add("&7Type: &f" + (shop.isAdminShop() ? "Admin Shop" : "Player Shop"));
        lore.add("&7Status: &f" + (shop.isOpen() ? "&aOpen" : "&cClosed"));
        lore.add("&7Items: &f" + shop.getItems().size());
        
        if (!shop.isAdminShop()) {
            // Add expiration info for player shops
            // This would need to be implemented in the Shop interface
            lore.add("&7Location: &f" + formatLocation(shop.getLocation()));
        }
        
        lore.add("");
        lore.add("&7Click to manage this shop");
        
        return guiManager.createGuiItem(material, name, lore);
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
    
    /**
     * Convert an index to a slot in the inventory
     *
     * @param index The index (0-35)
     * @return The slot number
     */
    private static int getSlotFromIndex(int index) {
        if (index < 0 || index >= 36) {
            return -1;
        }
        
        // 6 rows of 6 items, with a gap on the right
        int row = index / 6;
        int col = index % 6;
        return row * 9 + col + 1; // +1 to start from second column
    }
    
    /**
     * Convert a slot to an index
     *
     * @param slot The slot number
     * @return The index, or -1 if not an item slot
     */
    private static int getIndexFromSlot(int slot) {
        int row = slot / 9;
        int col = slot % 9;
        
        if (row >= 0 && row < 6 && col >= 1 && col <= 6) {
            return row * 6 + (col - 1);
        }
        
        return -1;
    }
} 