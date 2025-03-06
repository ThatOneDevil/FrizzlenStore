package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Handles the item details menu GUI
 */
public class ItemDetailsMenuHandler {

    /**
     * Open the item details menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param shopItemData The shop item data to show
     */
    public static void openItemDetailsMenu(GuiManager guiManager, FrizzlenShop plugin, Player player, ShopItemData shopItemData) {
        // Create inventory
        String itemName = getItemName(shopItemData.getItem());
        String title = "Shop - Item Details";
        Inventory inventory = Bukkit.createInventory(null, 9 * 4, title);
        
        // Item display
        ItemStack displayItem = shopItemData.getItem().clone();
        ItemMeta meta = displayItem.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        
        // Add shop information to lore
        lore.add("");
        lore.add(ChatColor.GRAY + "Shop: " + ChatColor.YELLOW + shopItemData.getShopName());
        lore.add(ChatColor.GRAY + "Type: " + ChatColor.YELLOW + (shopItemData.isAdminShop() ? "Admin Shop" : "Player Shop"));
        lore.add("");
        lore.add(ChatColor.GRAY + "Buy Price: " + ChatColor.YELLOW + plugin.getEconomyManager().formatCurrency(shopItemData.getBuyPrice(), shopItemData.getCurrency()));
        lore.add(ChatColor.GRAY + "Sell Price: " + ChatColor.YELLOW + plugin.getEconomyManager().formatCurrency(shopItemData.getSellPrice(), shopItemData.getCurrency()));
        lore.add(ChatColor.GRAY + "Stock: " + ChatColor.YELLOW + (shopItemData.getStock() == -1 ? "Unlimited" : shopItemData.getStock()));
        
        meta.setLore(lore);
        displayItem.setItemMeta(meta);
        
        // Buy buttons (different amounts)
        int[] buySlots = {20, 21, 22, 23, 24};
        int[] buyAmounts = {1, 8, 16, 32, 64};
        
        for (int i = 0; i < buySlots.length; i++) {
            int amount = buyAmounts[i];
            double price = shopItemData.getBuyPrice() * amount;
            
            ItemStack buyItem = guiManager.createGuiItem(Material.EMERALD, "&a&lBuy " + amount, 
                    Arrays.asList(
                        "&7Buy " + amount + "x " + itemName,
                        "&7Price: &e" + plugin.getEconomyManager().formatCurrency(price, shopItemData.getCurrency()),
                        "",
                        "&7Click to purchase"
                    ));
            inventory.setItem(buySlots[i], buyItem);
        }
        
        // Sell option
        double sellPrice = shopItemData.getSellPrice();
        ItemStack sellItem = guiManager.createGuiItem(Material.GOLD_INGOT, "&6&lSell", 
                Arrays.asList(
                    "&7Sell " + itemName,
                    "&7Price: &e" + plugin.getEconomyManager().formatCurrency(sellPrice, shopItemData.getCurrency()) + " each",
                    "",
                    "&7Left-click to sell 1",
                    "&7Right-click to sell all"
                ));
        inventory.setItem(30, sellItem);
        
        // Back button
        ItemStack backItem = guiManager.createGuiItem(Material.ARROW, "&7&lBack", 
                Collections.singletonList("&7Return to category menu"));
        inventory.setItem(31, backItem);
        
        // Place the display item in the center of the top row
        inventory.setItem(4, displayItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.ITEM_DETAILS_MENU, shopItemData));
    }
    
    /**
     * Handle a click in the item details menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        ShopItemData shopItemData = (ShopItemData) menuData.getData("value");
        
        if (shopItemData == null) {
            return false;
        }
        
        // Buy buttons
        if (slot >= 20 && slot <= 24) {
            int[] buyAmounts = {1, 8, 16, 32, 64};
            int index = slot - 20;
            
            if (index >= 0 && index < buyAmounts.length) {
                int amount = buyAmounts[index];
                
                // Check if the shop has enough stock
                if (!shopItemData.hasStock(amount)) {
                    MessageUtils.sendErrorMessage(player, "This shop doesn't have enough stock.");
                    return true;
                }
                
                // Check if the player has enough money
                double price = shopItemData.calculateBuyPrice(amount);
                if (!plugin.getEconomyManager().has(player.getUniqueId(), price, shopItemData.getCurrency())) {
                    MessageUtils.sendErrorMessage(player, "You don't have enough money.");
                    return true;
                }
                
                // Buy the items
                ItemStack itemStack = shopItemData.getItem().clone();
                itemStack.setAmount(amount);
                
                // Try to give the items to the player
                if (player.getInventory().firstEmpty() == -1) {
                    MessageUtils.sendErrorMessage(player, "Your inventory is full.");
                    return true;
                }
                
                // Process the purchase through the shop
                if (shopItemData.getShop().buyItem(player, shopItemData.getItem(), amount, shopItemData.getCurrency())) {
                    MessageUtils.sendSuccessMessage(player, "You bought " + amount + "x " + getItemName(shopItemData.getItem()) + 
                            " for " + plugin.getEconomyManager().formatCurrency(price, shopItemData.getCurrency()));
                    
                    // Update the menu
                    openItemDetailsMenu(guiManager, plugin, player, shopItemData);
                } else {
                    MessageUtils.sendErrorMessage(player, "Failed to buy the items.");
                }
                
                return true;
            }
        }
        
        // Sell button
        if (slot == 30) {
            // Check if the player has the item
            ItemStack itemToSell = shopItemData.getItem().clone();
            itemToSell.setAmount(1);
            
            if (!player.getInventory().containsAtLeast(itemToSell, 1)) {
                MessageUtils.sendErrorMessage(player, "You don't have this item.");
                return true;
            }
            
            // Process the sale through the shop
            if (shopItemData.getShop().sellItem(player, shopItemData.getItem(), 1, shopItemData.getCurrency())) {
                double price = shopItemData.getSellPrice();
                MessageUtils.sendSuccessMessage(player, "You sold 1x " + getItemName(shopItemData.getItem()) + 
                        " for " + plugin.getEconomyManager().formatCurrency(price, shopItemData.getCurrency()));
                
                // Update the menu
                openItemDetailsMenu(guiManager, plugin, player, shopItemData);
            } else {
                MessageUtils.sendErrorMessage(player, "Failed to sell the item.");
            }
            
            return true;
        }
        
        // Back button
        if (slot == 31) {
            // Go back to the category menu
            // We'd need to get the category from somewhere, for now just go to the main menu
            guiManager.openMainMenu(player);
            return true;
        }
        
        return false;
    }
    
    /**
     * Get the display name of an item, or its material name if it doesn't have a display name
     *
     * @param item The item
     * @return The display name
     */
    private static String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return ChatColor.stripColor(item.getItemMeta().getDisplayName());
        } else {
            return formatMaterialName(item.getType().name());
        }
    }
    
    /**
     * Format a material name for display
     *
     * @param materialName The material name
     * @return The formatted material name
     */
    private static String formatMaterialName(String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return "Unknown";
        }
        
        // Replace underscores with spaces and capitalize each word
        String[] words = materialName.toLowerCase().split("_");
        StringBuilder formatted = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }
        
        return formatted.toString().trim();
    }
} 