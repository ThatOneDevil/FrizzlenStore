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
 * Handles the shop items menu interactions
 */
public class ShopItemsMenuHandler {

    /**
     * Handle a click in the shop items menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        // Log the click to help debug
        plugin.getLogger().info("ShopItemsMenuHandler: handleClick - slot: " + slot);
        
        // Get the shop from menu data
        Shop shop = null;
        List<ShopItem> items = null;
        
        // There are two ways to get the shop - either from an ID or directly
        UUID shopId = menuData.getUUID("id");
        if (shopId != null) {
            shop = plugin.getShopManager().getShop(shopId);
        } else {
            shop = (Shop) menuData.getData("shop");
        }
        
        if (shop == null) {
            MessageUtils.sendErrorMessage(player, "This shop no longer exists.");
            player.closeInventory();
            return true;
        }
        
        // Get the items - either from the shop or directly from menu data
        Object itemsObj = menuData.getData("items");
        if (itemsObj != null && itemsObj instanceof List) {
            items = (List<ShopItem>) itemsObj;
        } else {
            items = shop.getItems();
        }
        
        // Log the number of items for debugging
        plugin.getLogger().info("ShopItemsMenuHandler: shop: " + shop.getName() + ", items count: " + (items != null ? items.size() : "null"));
        
        // Handle add item button
        if (slot == 53) {
            handleAddItem(guiManager, plugin, player, shop);
            return true;
        }
        
        // Handle back button
        if (slot == 49) {
            if (shop.isAdminShop()) {
                guiManager.openAdminShopsMenu(player);
            } else {
                guiManager.openShopManagementMenu(player, shop.getId());
            }
            return true;
        }
        
        // Handle clicking on an item
        if (slot < 45) {
            // Check if the slot contains a valid item
            if (items != null && slot < items.size()) {
                ShopItem shopItem = items.get(slot);
                
                // Create a ShopItemData object to pass to the item details menu
                ShopItemData shopItemData = new ShopItemData(
                        shop.getId(),
                        shop.getName(),
                        shopItem.getId(),
                        shopItem.getItem(),
                        shopItem.getBuyPrice(),
                        shopItem.getSellPrice(),
                        shopItem.getStock(),
                        shopItem.getCurrency(),
                        shop.isAdminShop()
                );
                
                // Set the shop in the shop item data for easy access
                shopItemData.setShop(shop);
                
                // Log that we're opening the item details menu
                plugin.getLogger().info("Opening item details for " + shopItem.getItem().getType() + " in shop " + shop.getName());
                
                // Store the menu data for correct back button navigation
                MenuData currentData = guiManager.getMenuData(player.getUniqueId());
                if (currentData != null) {
                    // Create menu data to store the information that we're coming from a shop items menu
                    MenuData newData = new MenuData(MenuType.ITEM_DETAILS_MENU, shopItemData);
                    
                    // Store the shop ID for proper navigation
                    newData.setData("id", shop.getId());
                    
                    // Use updateMenuData to properly preserve the previous menu type hierarchy
                    guiManager.updateMenuData(player.getUniqueId(), newData);
                    
                    // Explicitly ensure the previous menu type is set to SHOP_ITEMS
                    MenuData updatedData = guiManager.getMenuData(player.getUniqueId());
                    if (updatedData != null) {
                        updatedData.setPreviousMenuType(MenuType.SHOP_ITEMS);
                    }
                    
                    // Open the menu with this data instead of the standard method
                    ItemDetailsMenuHandler.openItemDetailsMenu(guiManager, plugin, player, shopItemData);
                    return true;
                }
                
                // Fallback to standard opening method
                guiManager.openItemDetailsMenu(player, shopItemData);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Opens the shop items menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param shop The shop to show items for
     */
    public static void openShopItemsMenu(GuiManager guiManager, FrizzlenShop plugin, Player player, Shop shop) {
        // Create inventory
        String title = shop.getName() + " - Items";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Get the items
        List<ShopItem> items = shop.getItems();
        
        // Add items to inventory
        for (int i = 0; i < Math.min(items.size(), 45); i++) {
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
            
            inventory.setItem(i, item);
        }
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
            Material.ARROW,
            shop.isAdminShop() ? "&c&lBack to Admin Shops" : "&c&lBack",
            Collections.singletonList(shop.isAdminShop() ? "&7Return to the admin shops list" : "&7Return to your shops")
        );
        inventory.setItem(49, backButton);
        
        // Add an add item button for shop owners or admins
        if (player.hasPermission("frizzlenshop.admin") || 
                (!shop.isAdminShop() && player.getUniqueId().equals(shop.getOwner()))) {
            ItemStack addButton = guiManager.createGuiItem(
                Material.EMERALD,
                "&a&lAdd Item",
                Arrays.asList(
                    "&7Add a new item to this shop",
                    "&7Hold the item you want to add"
                )
            );
            inventory.setItem(53, addButton);
        }
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        MenuData menuData = new MenuData(MenuType.SHOP_ITEMS);
        menuData.setData("id", shop.getId());
        guiManager.updateMenuData(player.getUniqueId(), menuData);
    }
    
    /**
     * Handle adding a new item to the shop
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param shop The shop to add an item to
     */
    private static void handleAddItem(GuiManager guiManager, FrizzlenShop plugin, Player player, Shop shop) {
        // Get the item in the player's hand
        ItemStack handItem = player.getInventory().getItemInMainHand();
        
        // Check if the player is holding an item
        if (handItem == null || handItem.getType() == Material.AIR || handItem.getAmount() <= 0) {
            MessageUtils.sendErrorMessage(player, "You must be holding an item to add it to the shop.");
            return;
        }
        
        // Check if the shop has reached its item limit
        int maxItems = shop.isAdminShop() ? 45 : plugin.getConfigManager().getMaxItemsPerPlayerShop();
        if (shop.getItems().size() >= maxItems) {
            MessageUtils.sendErrorMessage(player, "This shop has reached its item limit.");
            return;
        }
        
        // Check if the item is already in the shop
        for (ShopItem existingItem : shop.getItems()) {
            if (existingItem.matches(handItem)) {
                MessageUtils.sendErrorMessage(player, "This item is already in the shop.");
                return;
            }
        }
        
        // For player shops, ask how many items to add as stock
        if (!shop.isAdminShop()) {
            // Ask the player how many items to add as stock
            player.closeInventory();
            MessageUtils.sendMessage(player, "&eHow many items do you want to add as initial stock?");
            MessageUtils.sendMessage(player, "&7You have " + handItem.getAmount() + " of this item.");
            MessageUtils.sendMessage(player, "&7Type a number in chat, or 'all' to add all items.");
            
            // Register a chat action for the stock amount
            plugin.getChatListener().registerPendingAction(player, 
                    new ChatListener.ChatAction(ChatListener.ChatActionType.ADD_SHOP_ITEM, shop.getId()));
        } else {
            // For admin shops, just add the item with unlimited stock
            // Create a copy of the item for the shop (1 quantity)
            ItemStack shopItemStack = handItem.clone();
            shopItemStack.setAmount(1);
            
            // Set default prices based on config
            double buyPrice = plugin.getShopManager().getDefaultBuyPrice(shopItemStack);
            double sellPrice = plugin.getShopManager().getDefaultSellPrice(shopItemStack);
            String currency = plugin.getEconomyManager().getDefaultCurrency();
            int initialStock = -1; // -1 for unlimited stock in admin shops
            
            // Add the item to the shop
            boolean added = shop.addItem(shopItemStack, buyPrice, sellPrice, currency, initialStock);
            
            if (added) {
                // Get the item we just added (last item in the list)
                List<ShopItem> items = shop.getItems();
                ShopItem newItem = items.get(items.size() - 1);
                
                // Success! Open the item management menu for the new item
                MessageUtils.sendSuccessMessage(player, "Item added to the admin shop with unlimited stock!");
                guiManager.openItemManagementMenu(player, shop.getId(), newItem.getId());
            } else {
                // Failed to add the item
                MessageUtils.sendErrorMessage(player, "Failed to add the item to the shop.");
            }
        }
    }
} 