package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

/**
 * Handles the main menu GUI
 */
public class MainMenuHandler {

    /**
     * Open the main menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openMainMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "FrizzlenShop");
        
        // Create item to browse shops by category
        ItemStack browseItem = guiManager.createGuiItem(
                Material.CHEST,
                "&a&lBrowse Shops",
                Arrays.asList(
                        "&7Browse all shop categories",
                        "&7Find what you need to buy or sell"
                )
        );
        inventory.setItem(20, browseItem);
        
        // Create item to view my shops
        ItemStack myShopsItem = guiManager.createGuiItem(
                Material.ENDER_CHEST,
                "&a&lMy Shops",
                Arrays.asList(
                        "&7View and manage your own shops",
                        "&7Create new shops or edit existing ones"
                )
        );
        inventory.setItem(24, myShopsItem);
        
        // Create templates item
        if (player.hasPermission("frizzlenshop.templates.view")) {
            ItemStack templatesItem = guiManager.createGuiItem(
                    Material.BOOK,
                    "&5&lShop Templates",
                    Arrays.asList(
                            "&7Browse and use shop templates",
                            "&7Quickly create shops with predefined items"
                    )
            );
            inventory.setItem(30, templatesItem);
        }
        
        // Create item for user to see admin panel (if they have permission)
        if (player.hasPermission("frizzlenshop.admin")) {
            ItemStack adminItem = guiManager.createGuiItem(
                    Material.GOLDEN_HELMET,
                    "&6&lAdmin Panel",
                    Arrays.asList(
                            "&7Access the admin panel",
                            "&7Manage plugin settings and shops"
                    )
            );
            inventory.setItem(40, adminItem);
        }
        
        // Create item to view admin shops
        ItemStack adminShopsItem = guiManager.createGuiItem(
                Material.DIAMOND_BLOCK,
                "&b&lAdmin Shops",
                Arrays.asList(
                        "&7Browse all admin shops",
                        "&7For server-controlled trading"
                )
        );
        inventory.setItem(38, adminShopsItem);
        
        // Create Quick Sell item
        ItemStack quickSellItem = guiManager.createGuiItem(
                Material.GOLD_INGOT,
                "&6&lQuick Sell",
                Arrays.asList(
                        "&7Quickly sell items to admin shops",
                        "&7Get the best price automatically"
                )
        );
        inventory.setItem(32, quickSellItem);
        
        // Fill empty slots in the inventory
        guiManager.fillEmptySlots(inventory);
        
        // Set player's menu data
        UUID playerUuid = player.getUniqueId();
        MenuData menuData = new MenuData(MenuType.MAIN_MENU);
        guiManager.updateMenuData(playerUuid, menuData);
        
        // Open inventory
        player.openInventory(inventory);
    }
    
    /**
     * Handle clicks in the main menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot) {
        switch (slot) {
            case 20: // Browse Shops
                player.closeInventory();
                player.performCommand("shop browse");
                return true;
                
            case 24: // My Shops
                guiManager.openMyShopsMenu(player);
                return true;
                
            case 30: // Shop Templates
                if (player.hasPermission("frizzlenshop.templates.view")) {
                    guiManager.openTemplateManagementMenu(player);
                    return true;
                }
                return false;
                
            case 32: // Quick Sell
                QuickSellMenuHandler.openQuickSellMenu(guiManager, plugin, player);
                return true;
                
            case 38: // Admin shops
                guiManager.openAdminShopsMenu(player);
                return true;
                
            case 40: // Admin panel (if they have permission)
                if (player.hasPermission("frizzlenshop.admin")) {
                    guiManager.openShopAdminMenu(player);
                    return true;
                }
                return false;
                
            default:
                return false;
        }
    }
} 