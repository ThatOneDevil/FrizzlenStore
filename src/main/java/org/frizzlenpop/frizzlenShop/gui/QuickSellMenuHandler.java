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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles the quick sell menu GUI to sell multiple items at once
 */
public class QuickSellMenuHandler {

    /**
     * Open the quick sell menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openQuickSellMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Create inventory (chest with 5 rows - 45 slots for items)
        String title = "Quick Sell - Add Items to Sell";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Top row stays empty for instructions
        ItemStack infoItem = guiManager.createGuiItem(
            Material.BOOK, 
            "&e&lAdd items to sell",
            Arrays.asList(
                "&7Place items in the slots below",
                "&7All items will be sold to admin shops",
                "&7You'll get the highest possible price"
            )
        );
        inventory.setItem(4, infoItem);
        
        // Bottom row for buttons
        ItemStack confirmButton = guiManager.createGuiItem(
            Material.EMERALD, 
            "&a&lSell All Items",
            Collections.singletonList("&7Click to sell all items in this menu")
        );
        inventory.setItem(49, confirmButton);
        
        ItemStack cancelButton = guiManager.createGuiItem(
            Material.BARRIER, 
            "&c&lCancel",
            Collections.singletonList("&7Return to main menu")
        );
        inventory.setItem(45, cancelButton);
        
        // Fill remaining slots in the bottom row with glass panes
        for (int i = 45; i < 54; i++) {
            if (i != 45 && i != 49) {
                inventory.setItem(i, guiManager.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ", Collections.emptyList()));
            }
        }
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        MenuData menuData = new MenuData(MenuType.QUICK_SELL_MENU);
        guiManager.updateMenuData(player.getUniqueId(), menuData);
    }
    
    /**
     * Handle a click in the quick sell menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        // Cancel button
        if (slot == 45) {
            guiManager.returnToPreviousMenu(player);
            return true;
        }
        
        // Confirm button to sell all items
        if (slot == 49) {
            processQuickSell(guiManager, plugin, player);
            return true;
        }
        
        // Allow clicking in the item placement area (slots 9-44)
        // For these slots, we just let the default inventory handling work
        return false;
    }
    
    /**
     * Process the quick sell operation
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who is selling
     */
    private static void processQuickSell(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        
        // Check if there are any items to sell
        boolean hasItems = false;
        for (int i = 9; i < 45; i++) {
            if (inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR) {
                hasItems = true;
                break;
            }
        }
        
        if (!hasItems) {
            MessageUtils.sendMessage(player, "&cThere are no items to sell!");
            return;
        }
        
        // Get all admin shops
        Collection<Shop> adminShops = plugin.getShopManager().getAdminShops();
        
        if (adminShops.isEmpty()) {
            MessageUtils.sendMessage(player, "&cNo admin shops found to sell to!");
            return;
        }
        
        // Track total earnings and items sold
        double totalEarnings = 0.0;
        int totalItemsSold = 0;
        Map<String, Double> earningsByCurrency = new HashMap<>();
        List<String> soldItemsInfo = new ArrayList<>();
        
        // Process each item
        for (int i = 9; i < 45; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            
            // Try to find the best shop to sell this item to
            Map.Entry<Shop, ShopItem> bestMatch = findBestShopForItem(adminShops, itemStack);
            
            if (bestMatch != null) {
                Shop shop = bestMatch.getKey();
                ShopItem shopItem = bestMatch.getValue();
                
                int amount = itemStack.getAmount();
                double price = shopItem.calculateSellPrice(amount);
                String currency = shopItem.getCurrency();
                
                // Attempt to sell the item
                if (shop.sellItem(player, itemStack, amount, currency)) {
                    // Remove the item from the inventory
                    inventory.setItem(i, null);
                    
                    // Track earnings
                    totalEarnings += price;
                    totalItemsSold += amount;
                    
                    // Add to earnings by currency
                    earningsByCurrency.put(currency, earningsByCurrency.getOrDefault(currency, 0.0) + price);
                    
                    // Add to sold items info
                    String itemName = getItemName(itemStack);
                    String priceStr = plugin.getEconomyManager().formatCurrency(price, currency);
                    soldItemsInfo.add(ChatColor.YELLOW + String.valueOf(amount) + "x " + itemName + " - " + priceStr);
                }
            } else {
                // No shop buys this item
                String itemName = getItemName(itemStack);
                MessageUtils.sendMessage(player, "&cNo shop found that buys " + itemName + "!");
                
                // Return the item to the player
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(itemStack);
                if (!leftover.isEmpty()) {
                    // If inventory is full, drop the item
                    for (ItemStack leftoverItem : leftover.values()) {
                        player.getWorld().dropItem(player.getLocation(), leftoverItem);
                    }
                }
                
                // Remove from the inventory
                inventory.setItem(i, null);
            }
        }
        
        // Close the inventory
        player.closeInventory();
        
        // Show results to the player
        if (totalItemsSold > 0) {
            MessageUtils.sendMessage(player, "&a===== Quick Sell Results =====");
            MessageUtils.sendMessage(player, "&aSold " + totalItemsSold + " items for:");
            
            // Show earnings by currency
            for (Map.Entry<String, Double> entry : earningsByCurrency.entrySet()) {
                String formatted = plugin.getEconomyManager().formatCurrency(entry.getValue(), entry.getKey());
                MessageUtils.sendMessage(player, "&a" + formatted);
            }
            
            // Show sold items (limited to 10 to avoid spam)
            if (soldItemsInfo.size() <= 10) {
                for (String info : soldItemsInfo) {
                    player.sendMessage(info);
                }
            } else {
                for (int i = 0; i < 10; i++) {
                    player.sendMessage(soldItemsInfo.get(i));
                }
                MessageUtils.sendMessage(player, "&7...and " + (soldItemsInfo.size() - 10) + " more items");
            }
        } else {
            MessageUtils.sendMessage(player, "&cNo items could be sold to any shop.");
        }
        
        // Open the main menu
        guiManager.openMainMenu(player);
    }
    
    /**
     * Find the best shop to sell an item to
     *
     * @param adminShops All admin shops
     * @param itemStack The item to sell
     * @return The best shop and shop item to sell to, or null if no match
     */
    private static Map.Entry<Shop, ShopItem> findBestShopForItem(Collection<Shop> adminShops, ItemStack itemStack) {
        double bestPrice = -1;
        Map.Entry<Shop, ShopItem> bestMatch = null;
        
        for (Shop shop : adminShops) {
            for (ShopItem shopItem : shop.getItems()) {
                if (shopItem.matches(itemStack)) {
                    double price = shopItem.getSellPrice();
                    if (price > bestPrice) {
                        bestPrice = price;
                        bestMatch = new AbstractMap.SimpleEntry<>(shop, shopItem);
                    }
                }
            }
        }
        
        return bestMatch;
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