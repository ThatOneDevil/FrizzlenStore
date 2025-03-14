package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;
import org.frizzlenpop.frizzlenShop.listeners.ChatListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles the shop settings menu
 */
public class ShopSettingsMenuHandler {

    /**
     * Opens the shop settings menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param shop The shop to edit settings for
     */
    public static void openShopSettingsMenu(GuiManager guiManager, FrizzlenShop plugin, Player player, Shop shop) {
        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 36, "Shop Settings: " + shop.getName());
        
        // Store menu data
        Map<String, Object> data = new HashMap<>();
        data.put("shopId", shop.getId());
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.SHOP_SETTINGS, data));
        
        // Add shop info
        ItemStack infoItem = guiManager.createGuiItem(Material.BOOK, "&e&lShop Information",
                Arrays.asList(
                        "&7Name: &f" + shop.getName(),
                        "&7Type: &f" + (shop.isAdminShop() ? "Admin Shop" : "Player Shop"),
                        "&7Owner: &f" + (shop.isAdminShop() ? "Server" : shop.getOwnerName()),
                        "&7Location: &f" + formatLocation(shop.getLocation()),
                        "",
                        "&7Click to view more details"
                ));
        inventory.setItem(4, infoItem);
        
        // Add settings options
        if (!shop.isAdminShop()) {
            // Player shop settings
            
            // Shop description
            ItemStack descriptionItem = guiManager.createGuiItem(Material.NAME_TAG, "&e&lShop Description",
                    Arrays.asList(
                            "&7Current: &f" + (shop.getDescription().isEmpty() ? "None" : shop.getDescription()),
                            "",
                            "&7Click to change the shop description"
                    ));
            inventory.setItem(10, descriptionItem);
            
            // Shop visibility
            boolean isPublic = shop.isPublic();
            ItemStack visibilityItem = guiManager.createGuiItem(
                    isPublic ? Material.ENDER_EYE : Material.ENDER_PEARL,
                    "&e&lShop Visibility",
                    Arrays.asList(
                            "&7Current: &f" + (isPublic ? "Public" : "Private"),
                            "",
                            "&7Click to toggle shop visibility",
                            isPublic ? "&7Make private to hide from listings" : "&7Make public to show in listings"
                    ));
            inventory.setItem(12, visibilityItem);
            
            // Shop design
            ItemStack designItem = guiManager.createGuiItem(Material.PAINTING, "&e&lShop Design",
                    Arrays.asList(
                            "&7Current theme: &f" + shop.getTheme(),
                            "",
                            "&7Click to change the shop appearance"
                    ));
            inventory.setItem(14, designItem);
            
            // Shop notifications
            boolean notificationsEnabled = shop.areNotificationsEnabled();
            ItemStack notificationsItem = guiManager.createGuiItem(
                    notificationsEnabled ? Material.BELL : Material.LEVER,
                    "&e&lNotifications",
                    Arrays.asList(
                            "&7Current: &f" + (notificationsEnabled ? "Enabled" : "Disabled"),
                            "",
                            "&7Click to toggle transaction notifications"
                    ));
            inventory.setItem(16, notificationsItem);
        } else {
            // Admin shop settings
            
            // Shop tier
            ItemStack tierItem = guiManager.createGuiItem(Material.GOLD_INGOT, "&e&lShop Tier",
                    Arrays.asList(
                            "&7Current tier: &f" + shop.getTier(),
                            "",
                            "&7Click to change the shop tier",
                            "&7Higher tiers have better prices and items"
                    ));
            inventory.setItem(10, tierItem);
            
            // Shop category
            ItemStack categoryItem = guiManager.createGuiItem(Material.CHEST, "&e&lShop Category",
                    Arrays.asList(
                            "&7Current category: &f" + shop.getCategory(),
                            "",
                            "&7Click to change the shop category"
                    ));
            inventory.setItem(12, categoryItem);
            
            // Dynamic pricing
            boolean dynamicPricingEnabled = plugin.getConfigManager().isDynamicPricingEnabled();
            ItemStack dynamicPricingItem = guiManager.createGuiItem(
                    dynamicPricingEnabled ? Material.CLOCK : Material.BARRIER,
                    "&e&lDynamic Pricing",
                    Arrays.asList(
                            "&7Status: &f" + (dynamicPricingEnabled ? "Enabled" : "Disabled"),
                            "",
                            "&7Dynamic pricing is controlled globally",
                            "&7Use /fs admin pricing to manage"
                    ));
            inventory.setItem(14, dynamicPricingItem);
            
            // Tax rate
            double taxRate = shop.getTaxRate();
            ItemStack taxItem = guiManager.createGuiItem(Material.GOLD_NUGGET, "&e&lTax Rate",
                    Arrays.asList(
                            "&7Current rate: &f" + taxRate + "%",
                            "",
                            "&7Click to change the tax rate for this shop"
                    ));
            inventory.setItem(16, taxItem);
        }
        
        // Add back button
        ItemStack backButton = guiManager.createGuiItem(Material.ARROW, "&c&lBack",
                Arrays.asList("&7Return to shop management"));
        inventory.setItem(31, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
    }
    
    /**
     * Handles a click in the shop settings menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        // Get the shop
        Shop shop = plugin.getShopManager().getShop(menuData.getUUID("shopId"));
        if (shop == null) {
            MessageUtils.sendErrorMessage(player, "Shop not found.");
            player.closeInventory();
            return true;
        }
        
        // Handle clicks based on slot
        switch (slot) {
            case 4: // Shop info
                MessageUtils.sendMessage(player, "&e===== Shop Information =====");
                MessageUtils.sendMessage(player, "&7Name: &f" + shop.getName());
                MessageUtils.sendMessage(player, "&7Type: &f" + (shop.isAdminShop() ? "Admin Shop" : "Player Shop"));
                MessageUtils.sendMessage(player, "&7Owner: &f" + (shop.isAdminShop() ? "Server" : shop.getOwnerName()));
                MessageUtils.sendMessage(player, "&7Location: &f" + formatLocation(shop.getLocation()));
                MessageUtils.sendMessage(player, "&7Created: &f" + new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(shop.getCreationTime())));
                return true;
                
            case 10: // Description (player) or Tier (admin)
                if (shop.isAdminShop()) {
                    // Change tier
                    int currentTier = shop.getTier();
                    int newTier = (currentTier % 3) + 1; // Cycle through tiers 1-3
                    shop.setTier(newTier);
                    MessageUtils.sendSuccessMessage(player, "Shop tier changed to " + newTier);
                    
                    // Refresh menu
                    openShopSettingsMenu(guiManager, plugin, player, shop);
                } else {
                    // Change description
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "&eEnter a new description for your shop in chat:");
                    MessageUtils.sendMessage(player, "&7Current: &f" + (shop.getDescription().isEmpty() ? "None" : shop.getDescription()));
                    
                    // Register chat action
                    plugin.getChatListener().registerPendingAction(player, 
                            new org.frizzlenpop.frizzlenShop.listeners.ChatListener.ChatAction(
                                    org.frizzlenpop.frizzlenShop.listeners.ChatListener.ChatActionType.SET_SHOP_DESCRIPTION, 
                                    shop.getId()));
                }
                return true;
                
            case 12: // Visibility (player) or Category (admin)
                if (shop.isAdminShop()) {
                    // Change category
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "&eEnter a new category for this shop in chat:");
                    MessageUtils.sendMessage(player, "&7Current: &f" + shop.getCategory());
                    MessageUtils.sendMessage(player, "&7Available categories: &ftools, food, potions, armor, valuables, blocks, misc");
                    
                    // Register chat action
                    plugin.getChatListener().registerPendingAction(player, 
                            new org.frizzlenpop.frizzlenShop.listeners.ChatListener.ChatAction(
                                    org.frizzlenpop.frizzlenShop.listeners.ChatListener.ChatActionType.SET_SHOP_CATEGORY, 
                                    shop.getId()));
                } else {
                    // Toggle visibility
                    boolean isPublic = shop.isPublic();
                    shop.setPublic(!isPublic);
                    MessageUtils.sendSuccessMessage(player, "Shop is now " + (isPublic ? "private" : "public"));
                    
                    // Refresh menu
                    openShopSettingsMenu(guiManager, plugin, player, shop);
                }
                return true;
                
            case 14: // Design (player) or Dynamic pricing (admin)
                if (shop.isAdminShop()) {
                    // Dynamic pricing info
                    boolean dynamicPricingEnabled = plugin.getConfigManager().isDynamicPricingEnabled();
                    if (dynamicPricingEnabled) {
                        MessageUtils.sendMessage(player, "&eDynamic pricing is currently enabled.");
                        MessageUtils.sendMessage(player, "&7Prices will fluctuate based on supply and demand.");
                        MessageUtils.sendMessage(player, "&7Use &f/fs admin pricing&7 to manage dynamic pricing settings.");
                    } else {
                        MessageUtils.sendMessage(player, "&eDynamic pricing is currently disabled.");
                        MessageUtils.sendMessage(player, "&7Prices are static and won't change automatically.");
                        MessageUtils.sendMessage(player, "&7Use &f/fs admin pricing toggle&7 to enable dynamic pricing.");
                    }
                } else {
                    // Change design
                    // TODO: Implement shop design menu
                    MessageUtils.sendMessage(player, "&eShop design feature coming soon!");
                }
                return true;
                
            case 16: // Notifications (player) or Tax rate (admin)
                if (shop.isAdminShop()) {
                    // Change tax rate
                    player.closeInventory();
                    MessageUtils.sendMessage(player, "&eEnter a new tax rate for this shop in chat:");
                    MessageUtils.sendMessage(player, "&7Current: &f" + shop.getTaxRate() + "%");
                    MessageUtils.sendMessage(player, "&7Enter a value between 0 and 20");
                    
                    // Register chat action
                    plugin.getChatListener().registerPendingAction(player, 
                            new org.frizzlenpop.frizzlenShop.listeners.ChatListener.ChatAction(
                                    org.frizzlenpop.frizzlenShop.listeners.ChatListener.ChatActionType.SET_SHOP_TAX_RATE, 
                                    shop.getId()));
                } else {
                    // Toggle notifications
                    boolean notificationsEnabled = shop.areNotificationsEnabled();
                    shop.setNotificationsEnabled(!notificationsEnabled);
                    MessageUtils.sendSuccessMessage(player, "Shop notifications " + 
                            (notificationsEnabled ? "disabled" : "enabled"));
                    
                    // Refresh menu
                    openShopSettingsMenu(guiManager, plugin, player, shop);
                }
                return true;
                
            case 31: // Back button
                guiManager.openShopManagementMenu(player, shop.getId());
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Formats a location for display
     *
     * @param location The location to format
     * @return The formatted location string
     */
    private static String formatLocation(org.bukkit.Location location) {
        if (location == null) {
            return "Unknown";
        }
        return location.getWorld().getName() + " (" + 
               location.getBlockX() + ", " + 
               location.getBlockY() + ", " + 
               location.getBlockZ() + ")";
    }
} 