package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.listeners.ChatListener;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

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
        
        // Handle add item button
        if (slot == 53) {
            handleAddItem(guiManager, plugin, player, shop);
            return true;
        }
        
        // Handle back button
        if (slot == 49) {
            guiManager.openShopManagementMenu(player, shopId);
            return true;
        }
        
        // Handle clicking on an item
        if (slot < 45) {
            // Get the items in the shop
            java.util.List<ShopItem> items = shop.getItems();
            
            // Check if the slot contains a valid item
            if (slot < items.size()) {
                ShopItem shopItem = items.get(slot);
                
                // Open the item management menu for this item
                guiManager.openItemManagementMenu(player, shopId, shopItem.getId());
                return true;
            }
        }
        
        return false;
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