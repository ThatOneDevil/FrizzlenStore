package org.frizzlenpop.frizzlenShop.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Listens for chat messages to handle shop creation and other operations
 */
public class ChatListener implements Listener {

    private final FrizzlenShop plugin;
    
    // Maps player UUID to chat action type
    private final Map<UUID, ChatAction> pendingActions = new HashMap<>();
    
    // Maps player UUID to custom action handlers
    private final Map<UUID, Consumer<String>> customActionHandlers = new HashMap<>();
    
    /**
     * Creates a new chat listener
     *
     * @param plugin The plugin instance
     */
    public ChatListener(FrizzlenShop plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Register a pending chat action for a player
     *
     * @param player The player
     * @param action The chat action
     */
    public void registerPendingAction(Player player, ChatAction action) {
        pendingActions.put(player.getUniqueId(), action);
        MessageUtils.sendMessage(player, "&7Type &c'cancel'&7 to cancel this operation.");
    }
    
    /**
     * Register a custom action handler for a player
     *
     * @param player The player
     * @param handler The action handler
     */
    public void registerPendingAction(Player player, Consumer<String> handler) {
        customActionHandlers.put(player.getUniqueId(), handler);
        MessageUtils.sendMessage(player, "&7Type &c'cancel'&7 to cancel this operation.");
    }
    
    /**
     * Clear a pending chat action for a player
     *
     * @param player The player
     */
    public void clearPendingAction(Player player) {
        pendingActions.remove(player.getUniqueId());
        customActionHandlers.remove(player.getUniqueId());
    }
    
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        // Check if the player has a pending action
        boolean hasPendingAction = pendingActions.containsKey(playerId);
        boolean hasCustomAction = customActionHandlers.containsKey(playerId);
        
        if (!hasPendingAction && !hasCustomAction) {
            return;
        }
        
        // Cancel the chat event so it doesn't appear in public chat
        event.setCancelled(true);
        
        String message = event.getMessage();
        
        // Check if the player wants to cancel the operation
        if (message.equalsIgnoreCase("cancel")) {
            pendingActions.remove(playerId);
            customActionHandlers.remove(playerId);
            MessageUtils.sendMessage(player, "&cOperation cancelled.");
            return;
        }
        
        // Handle custom action if present
        if (hasCustomAction) {
            Consumer<String> handler = customActionHandlers.get(playerId);
            handler.accept(message);
            customActionHandlers.remove(playerId);
            return;
        }
        
        // Get the pending action
        ChatAction action = pendingActions.get(playerId);
        
        // Handle the action based on its type
        switch (action.getType()) {
            case CREATE_ADMIN_SHOP:
                handleCreateAdminShop(player, message);
                break;
                
            case CREATE_PLAYER_SHOP:
                handleCreatePlayerShop(player, message);
                break;
                
            case SET_SHOP_DESCRIPTION:
                handleSetShopDescription(player, message, action);
                break;
                
            case SET_ITEM_PRICE:
                handleSetItemPrice(player, message, action);
                break;
                
            case ADD_SHOP_ITEM:
                handleAddShopItem(player, message, action);
                break;
                
            default:
                MessageUtils.sendErrorMessage(player, "Unknown action type. Please try again.");
                break;
        }
        
        // Remove the pending action
        pendingActions.remove(playerId);
    }
    
    /**
     * Handle creating an admin shop
     *
     * @param player The player
     * @param shopName The shop name
     */
    private void handleCreateAdminShop(Player player, String shopName) {
        // Validate shop name
        if (shopName.length() < 3 || shopName.length() > 32) {
            MessageUtils.sendErrorMessage(player, "Shop name must be between 3 and 32 characters.");
            return;
        }
        
        // Create the admin shop
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getGuiManager().openShopCreationMenu(player, shopName, true);
        });
    }
    
    /**
     * Handle creating a player shop
     *
     * @param player The player
     * @param shopName The shop name
     */
    private void handleCreatePlayerShop(Player player, String shopName) {
        // Validate shop name
        if (shopName.length() < 3 || shopName.length() > 32) {
            MessageUtils.sendErrorMessage(player, "Shop name must be between 3 and 32 characters.");
            return;
        }
        
        // Create the player shop
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getGuiManager().openShopCreationMenu(player, shopName, false);
        });
    }
    
    /**
     * Handle setting a shop description
     *
     * @param player The player
     * @param description The shop description
     * @param action The chat action
     */
    private void handleSetShopDescription(Player player, String description, ChatAction action) {
        // Validate description
        if (description.length() > 100) {
            MessageUtils.sendErrorMessage(player, "Description must be less than 100 characters.");
            return;
        }
        
        // Set the shop description
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getShopManager().getShop(action.getShopId()).setDescription(description);
            MessageUtils.sendMessage(player, "&aShop description updated successfully!");
            plugin.getGuiManager().openShopManagementMenu(player, action.getShopId());
        });
    }
    
    /**
     * Handle setting an item price
     *
     * @param player The player
     * @param priceStr The price as a string
     * @param action The chat action
     */
    private void handleSetItemPrice(Player player, String priceStr, ChatAction action) {
        // Parse the price
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            MessageUtils.sendErrorMessage(player, "Invalid price format. Please enter a valid number.");
            return;
        }
        
        // Validate price
        if (price < 0.01 || price > 1000000) {
            MessageUtils.sendErrorMessage(player, "Price must be between 0.01 and 1,000,000.");
            return;
        }
        
        // Set the item price
        final double finalPrice = price;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            plugin.getShopManager().getShop(action.getShopId()).getItem(action.getItemId()).setPrice(finalPrice);
            MessageUtils.sendMessage(player, "&aItem price updated successfully!");
            plugin.getGuiManager().openItemManagementMenu(player, action.getShopId(), action.getItemId());
        });
    }
    
    /**
     * Handle adding an item to a shop
     *
     * @param player The player
     * @param amountStr The amount as a string
     * @param action The chat action
     */
    private void handleAddShopItem(Player player, String amountStr, ChatAction action) {
        // Parse the amount
        int amount;
        if (amountStr.equalsIgnoreCase("all")) {
            // Get the amount of the item in the player's hand
            ItemStack handItem = player.getInventory().getItemInMainHand();
            amount = handItem.getAmount();
        } else {
            try {
                amount = Integer.parseInt(amountStr);
            } catch (NumberFormatException e) {
                MessageUtils.sendErrorMessage(player, "Invalid amount format. Please enter a valid number or 'all'.");
                return;
            }
        }
        
        // Validate amount
        if (amount <= 0) {
            MessageUtils.sendErrorMessage(player, "Amount must be greater than 0.");
            return;
        }
        
        // Get the item in the player's hand
        ItemStack handItem = player.getInventory().getItemInMainHand();
        
        // Validate item
        if (handItem == null || handItem.getType().isAir() || handItem.getAmount() <= 0) {
            MessageUtils.sendErrorMessage(player, "You must be holding an item to add to the shop.");
            return;
        }
        
        // Check if the player has enough of the item
        if (amount > handItem.getAmount()) {
            MessageUtils.sendErrorMessage(player, "You don't have that many items. You only have " + handItem.getAmount() + ".");
            return;
        }
        
        // Get the shop
        Shop shop = plugin.getShopManager().getShop(action.getShopId());
        if (shop == null) {
            MessageUtils.sendErrorMessage(player, "Shop not found.");
            return;
        }
        
        // Create a copy of the item for the shop (1 quantity)
        ItemStack shopItemStack = handItem.clone();
        shopItemStack.setAmount(1);
        
        // Set default prices based on config
        double buyPrice = plugin.getShopManager().getDefaultBuyPrice(shopItemStack);
        double sellPrice = plugin.getShopManager().getDefaultSellPrice(shopItemStack);
        String currency = plugin.getEconomyManager().getDefaultCurrency();
        
        // Add the item to the shop
        final int finalAmount = amount;
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            boolean added = shop.addItem(shopItemStack, buyPrice, sellPrice, currency, finalAmount);
            
            if (added) {
                // Get the item we just added (last item in the list)
                List<ShopItem> items = shop.getItems();
                ShopItem newItem = items.get(items.size() - 1);
                
                // Remove the items from the player's inventory
                ItemStack itemsToRemove = handItem.clone();
                itemsToRemove.setAmount(finalAmount);
                player.getInventory().removeItem(itemsToRemove);
                
                // Success! Open the item management menu for the new item
                MessageUtils.sendSuccessMessage(player, "Item added to the shop with " + finalAmount + " initial stock!");
                plugin.getGuiManager().openItemManagementMenu(player, shop.getId(), newItem.getId());
            } else {
                // Failed to add the item
                MessageUtils.sendErrorMessage(player, "Failed to add the item to the shop.");
            }
        });
    }
    
    /**
     * Represents a pending chat action
     */
    public static class ChatAction {
        private final ChatActionType type;
        private final UUID shopId;
        private final UUID itemId;
        
        /**
         * Creates a new chat action for shop creation
         *
         * @param type The action type
         */
        public ChatAction(ChatActionType type) {
            this.type = type;
            this.shopId = null;
            this.itemId = null;
        }
        
        /**
         * Creates a new chat action for shop operations
         *
         * @param type The action type
         * @param shopId The shop ID
         */
        public ChatAction(ChatActionType type, UUID shopId) {
            this.type = type;
            this.shopId = shopId;
            this.itemId = null;
        }
        
        /**
         * Creates a new chat action for item operations
         *
         * @param type The action type
         * @param shopId The shop ID
         * @param itemId The item ID
         */
        public ChatAction(ChatActionType type, UUID shopId, UUID itemId) {
            this.type = type;
            this.shopId = shopId;
            this.itemId = itemId;
        }
        
        /**
         * Get the action type
         *
         * @return The action type
         */
        public ChatActionType getType() {
            return type;
        }
        
        /**
         * Get the shop ID
         *
         * @return The shop ID
         */
        public UUID getShopId() {
            return shopId;
        }
        
        /**
         * Get the item ID
         *
         * @return The item ID
         */
        public UUID getItemId() {
            return itemId;
        }
    }
    
    /**
     * Enum for chat action types
     */
    public enum ChatActionType {
        CREATE_ADMIN_SHOP,
        CREATE_PLAYER_SHOP,
        SET_SHOP_DESCRIPTION,
        SET_ITEM_PRICE,
        ADD_SHOP_ITEM
    }
} 