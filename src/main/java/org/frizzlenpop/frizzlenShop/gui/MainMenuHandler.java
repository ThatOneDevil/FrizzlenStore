package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.Arrays;
import java.util.Collections;

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
        String title = "FrizzlenShop - Main Menu";
        Inventory inventory = Bukkit.createInventory(null, 9 * 5, title);
        
        // Categories
        ItemStack toolsItem = guiManager.createGuiItem(Material.IRON_PICKAXE, "&e&lTools", 
                Arrays.asList("&7View all tools for sale", "&7Click to browse"));
        
        ItemStack weaponsItem = guiManager.createGuiItem(Material.IRON_SWORD, "&e&lWeapons", 
                Arrays.asList("&7View all weapons for sale", "&7Click to browse"));
        
        ItemStack armorItem = guiManager.createGuiItem(Material.IRON_CHESTPLATE, "&e&lArmor", 
                Arrays.asList("&7View all armor for sale", "&7Click to browse"));
        
        ItemStack foodItem = guiManager.createGuiItem(Material.BREAD, "&e&lFood", 
                Arrays.asList("&7View all food for sale", "&7Click to browse"));
        
        ItemStack blocksItem = guiManager.createGuiItem(Material.STONE, "&e&lBlocks", 
                Arrays.asList("&7View all blocks for sale", "&7Click to browse"));
        
        ItemStack potionsItem = guiManager.createGuiItem(Material.POTION, "&e&lPotions", 
                Arrays.asList("&7View all potions for sale", "&7Click to browse"));
        
        ItemStack miscItem = guiManager.createGuiItem(Material.CHEST, "&e&lMiscellaneous", 
                Arrays.asList("&7View all miscellaneous items for sale", "&7Click to browse"));
        
        ItemStack allItem = guiManager.createGuiItem(Material.ENDER_CHEST, "&e&lAll Items", 
                Arrays.asList("&7View all items for sale", "&7Click to browse"));
        
        // Functions
        ItemStack myShopsItem = guiManager.createGuiItem(Material.GOLD_BLOCK, "&6&lMy Shops", 
                Arrays.asList("&7Manage your shops", "&7Click to view"));
        
        ItemStack createShopItem = guiManager.createGuiItem(Material.EMERALD, "&a&lCreate Shop", 
                Arrays.asList("&7Create a new shop", "&7Click to start"));
        
        ItemStack searchItem = guiManager.createGuiItem(Material.COMPASS, "&e&lSearch", 
                Arrays.asList("&7Search for items", "&7Click to search"));
        
        ItemStack historyItem = guiManager.createGuiItem(Material.PAPER, "&e&lTransaction History", 
                Arrays.asList("&7View your transaction history", "&7Click to view"));
        
        ItemStack balanceItem = guiManager.createGuiItem(Material.GOLD_INGOT, "&e&lBalance", 
                Arrays.asList(
                    "&7Your balance: &6" + plugin.getEconomyManager().formatBalance(player),
                    "&7Currency: &6" + plugin.getEconomyManager().getDefaultCurrency()));
        
        // Admin item (only shown to admins)
        ItemStack adminItem = guiManager.createGuiItem(Material.REDSTONE, "&c&lAdmin Panel", 
                Arrays.asList("&7Manage the shop system", "&7Click to open"));
        
        // Place items in inventory
        inventory.setItem(10, toolsItem);
        inventory.setItem(11, weaponsItem);
        inventory.setItem(12, armorItem);
        inventory.setItem(13, foodItem);
        inventory.setItem(14, blocksItem);
        inventory.setItem(15, potionsItem);
        inventory.setItem(16, miscItem);
        
        inventory.setItem(22, allItem);
        
        inventory.setItem(28, myShopsItem);
        inventory.setItem(30, createShopItem);
        inventory.setItem(32, searchItem);
        inventory.setItem(34, historyItem);
        
        inventory.setItem(40, balanceItem);
        
        // Only show admin item to players with permission
        if (player.hasPermission("frizzlenshop.admin")) {
            inventory.setItem(44, adminItem);
        }
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.getMenuData(player.getUniqueId());
        guiManager.clearMenuData(player.getUniqueId());
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.MAIN_MENU));
    }
    
    /**
     * Handle a click in the main menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot) {
        switch (slot) {
            case 10: // Tools
                guiManager.openCategoryMenu(player, "tools", 1);
                return true;
                
            case 11: // Weapons
                guiManager.openCategoryMenu(player, "weapons", 1);
                return true;
                
            case 12: // Armor
                guiManager.openCategoryMenu(player, "armor", 1);
                return true;
                
            case 13: // Food
                guiManager.openCategoryMenu(player, "food", 1);
                return true;
                
            case 14: // Blocks
                guiManager.openCategoryMenu(player, "blocks", 1);
                return true;
                
            case 15: // Potions
                guiManager.openCategoryMenu(player, "potions", 1);
                return true;
                
            case 16: // Miscellaneous
                guiManager.openCategoryMenu(player, "misc", 1);
                return true;
                
            case 22: // All Items
                guiManager.openCategoryMenu(player, "all", 1);
                return true;
                
            case 28: // My Shops
                guiManager.openMyShopsMenu(player);
                return true;
                
            case 30: // Create Shop
                player.closeInventory();
                player.performCommand("shop create");
                return true;
                
            case 32: // Search
                player.closeInventory();
                MessageUtils.sendMessage(player, "&eEnter a search term in chat or use &6/shop search <term>");
                return true;
                
            case 34: // Transaction History
                player.performCommand("shop history");
                return true;
                
            case 40: // Balance
                // Just display current balance, no action
                return true;
                
            case 44: // Admin Panel (only accessible with permission)
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