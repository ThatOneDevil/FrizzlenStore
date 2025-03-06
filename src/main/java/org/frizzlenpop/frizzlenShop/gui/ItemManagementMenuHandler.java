package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.listeners.ChatListener;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Handles the item management menu GUI
 */
public class ItemManagementMenuHandler {

    /**
     * Open the item management menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param shop The shop
     * @param shopItem The shop item to manage
     */
    public static void openItemManagementMenu(GuiManager guiManager, FrizzlenShop plugin, Player player, Shop shop, ShopItem shopItem) {
        // Create inventory
        String title = "Manage Item";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Item display
        ItemStack displayItem = shopItem.getItem().clone();
        ItemMeta meta = displayItem.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        
        // Add shop information to lore
        lore.add("");
        lore.add(ChatColor.GRAY + "Shop: " + ChatColor.YELLOW + shop.getName());
        lore.add(ChatColor.GRAY + "Type: " + ChatColor.YELLOW + (shop.isAdminShop() ? "Admin Shop" : "Player Shop"));
        lore.add("");
        lore.add(ChatColor.GRAY + "Buy Price: " + ChatColor.YELLOW + plugin.getEconomyManager().formatCurrency(shopItem.getBuyPrice(), shopItem.getCurrency()));
        lore.add(ChatColor.GRAY + "Sell Price: " + ChatColor.YELLOW + plugin.getEconomyManager().formatCurrency(shopItem.getSellPrice(), shopItem.getCurrency()));
        lore.add(ChatColor.GRAY + "Stock: " + ChatColor.YELLOW + (shopItem.getStock() == -1 ? "Unlimited" : shopItem.getStock()));
        
        meta.setLore(lore);
        displayItem.setItemMeta(meta);
        
        // Place item in the center
        inventory.setItem(4, displayItem);
        
        // Management options
        ItemStack changePriceItem = guiManager.createGuiItem(Material.GOLD_INGOT, "&e&lChange Price", 
                Arrays.asList(
                    "&7Current buy price: &f" + plugin.getEconomyManager().formatCurrency(shopItem.getBuyPrice(), shopItem.getCurrency()),
                    "&7Current sell price: &f" + plugin.getEconomyManager().formatCurrency(shopItem.getSellPrice(), shopItem.getCurrency()),
                    "",
                    "&7Click to change price"
                ));
        inventory.setItem(19, changePriceItem);
        
        ItemStack changeStockItem = guiManager.createGuiItem(Material.CHEST, "&e&lChange Stock", 
                Arrays.asList(
                    "&7Current stock: &f" + (shopItem.getStock() == -1 ? "Unlimited" : shopItem.getStock()),
                    "",
                    "&7Left-click to add stock",
                    "&7Right-click to remove stock"
                ));
        inventory.setItem(22, changeStockItem);
        
        ItemStack removeItem = guiManager.createGuiItem(Material.BARRIER, "&c&lRemove Item", 
                Arrays.asList(
                    "&7Remove this item from the shop",
                    "&cWarning: This action cannot be undone!",
                    "",
                    "&7Click to remove"
                ));
        inventory.setItem(25, removeItem);
        
        // Back button
        ItemStack backItem = guiManager.createGuiItem(Material.ARROW, "&7&lBack", 
                Collections.singletonList("&7Return to shop management"));
        inventory.setItem(49, backItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.ITEM_MANAGEMENT, shop.getId(), shopItem.getId()));
    }
    
    /**
     * Handle a click in the item management menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        // Get shop and item IDs from menu data
        UUID shopId = menuData.getUUID("shopId");
        UUID itemId = menuData.getUUID("itemId");
        
        if (shopId == null || itemId == null) {
            return false;
        }
        
        // Get shop and item
        Shop shop = plugin.getShopManager().getShop(shopId);
        
        if (shop == null) {
            MessageUtils.sendErrorMessage(player, "This shop no longer exists.");
            player.closeInventory();
            return true;
        }
        
        ShopItem shopItem = shop.getItem(itemId);
        
        if (shopItem == null) {
            MessageUtils.sendErrorMessage(player, "This item no longer exists in the shop.");
            player.closeInventory();
            return true;
        }
        
        switch (slot) {
            case 19: // Change Price
                // Open a chat prompt for the new price
                player.closeInventory();
                MessageUtils.sendMessage(player, "&eEnter a new price for this item:");
                // Register the chat action
                plugin.getChatListener().registerPendingAction(player, 
                        new ChatListener.ChatAction(ChatListener.ChatActionType.SET_ITEM_PRICE, shopId, itemId));
                return true;
                
            case 22: // Change Stock
                // Left-click adds stock, right-click removes stock
                // For simplicity, just update stock here
                int newStock = shopItem.getStock() + 10;
                shopItem.setStock(newStock);
                
                // Refresh menu
                openItemManagementMenu(guiManager, plugin, player, shop, shopItem);
                return true;
                
            case 25: // Remove Item
                // Remove item from shop
                shop.removeItem(shopItem.getItem());
                
                // Return to shop management
                player.closeInventory();
                guiManager.openShopManagementMenu(player, shopId);
                return true;
                
            case 49: // Back
                // Return to shop management
                guiManager.openShopManagementMenu(player, shopId);
                return true;
                
            default:
                return false;
        }
    }
} 