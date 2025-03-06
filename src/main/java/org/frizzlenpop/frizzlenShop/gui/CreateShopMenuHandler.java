package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.listeners.ChatListener;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles the create shop menu GUI
 */
public class CreateShopMenuHandler {

    /**
     * Open the create shop menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param step The step in the creation process
     */
    public static void openCreateShopMenu(GuiManager guiManager, FrizzlenShop plugin, Player player, int step) {
        // Create inventory
        String title = "Create Shop - Step " + step + "/4";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Get existing menu data if available
        Map<String, Object> data = new HashMap<>();
        MenuData existingMenuData = guiManager.getMenuData(player.getUniqueId());
        if (existingMenuData != null && existingMenuData.getMenuType() == MenuType.CREATE_SHOP_MENU) {
            // Copy existing data to preserve it
            data.putAll(existingMenuData.getAllData());
        }
        
        // Update step
        data.put("step", step);
        
        // Setup the menu based on the step
        switch (step) {
            case 1:
                setupStep1(guiManager, inventory, player, data);
                break;
                
            case 2:
                setupStep2(guiManager, inventory, player, data);
                break;
                
            case 3:
                setupStep3(guiManager, inventory, player, data);
                break;
                
            case 4:
                setupStep4(guiManager, inventory, player, data);
                break;
                
            default:
                // Invalid step, go back to step 1
                openCreateShopMenu(guiManager, plugin, player, 1);
                return;
        }
        
        // Open inventory and store menu data
        player.openInventory(inventory);
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.CREATE_SHOP_MENU, data));
    }
    
    /**
     * Open the shop creation menu after entering a name
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param shopName The shop name
     * @param isAdmin Whether this is an admin shop
     */
    public static void openShopCreationMenu(GuiManager guiManager, FrizzlenShop plugin, Player player, String shopName, boolean isAdmin) {
        // Create a map with the shop data
        Map<String, Object> data = new HashMap<>();
        data.put("step", 2);
        data.put("name", shopName);
        data.put("isAdmin", isAdmin);
        
        // Open step 2 (shop size selection)
        String title = "Create Shop - Step 2/4";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        setupStep2(guiManager, inventory, player, data);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.CREATE_SHOP_MENU, data));
    }
    
    /**
     * Handle a click in the create shop menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        // Get step from menu data
        int step = menuData.getInt("step");
        
        // Handle based on step
        switch (step) {
            case 1:
                return handleStep1Click(guiManager, plugin, player, slot, menuData);
                
            case 2:
                return handleStep2Click(guiManager, plugin, player, slot, menuData);
                
            case 3:
                return handleStep3Click(guiManager, plugin, player, slot, menuData);
                
            case 4:
                return handleStep4Click(guiManager, plugin, player, slot, menuData);
                
            default:
                return false;
        }
    }
    
    /**
     * Setup step 1 of the shop creation process (enter name)
     *
     * @param guiManager The GUI manager
     * @param inventory The inventory to setup
     * @param player The player
     * @param data The shop data
     */
    private static void setupStep1(GuiManager guiManager, Inventory inventory, Player player, Map<String, Object> data) {
        // Display instructions
        ItemStack instructionsItem = guiManager.createGuiItem(Material.PAPER, "&e&lEnter Shop Name", 
                Arrays.asList(
                    "&7Close this menu and enter",
                    "&7the name for your shop in chat.",
                    "",
                    "&7The name must be between",
                    "&73 and 32 characters."
                ));
        inventory.setItem(22, instructionsItem);
        
        // Cancel button
        ItemStack cancelItem = guiManager.createGuiItem(Material.BARRIER, "&c&lCancel", 
                Collections.singletonList("&7Cancel shop creation"));
        inventory.setItem(49, cancelItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
    }
    
    /**
     * Setup step 2 of the shop creation process (select shop size)
     *
     * @param guiManager The GUI manager
     * @param inventory The inventory to setup
     * @param player The player
     * @param data The shop data
     */
    private static void setupStep2(GuiManager guiManager, Inventory inventory, Player player, Map<String, Object> data) {
        // Get plugin instance from guiManager
        FrizzlenShop plugin = guiManager.getPlugin();
        
        // Get shop name
        String shopName = (String) data.get("name");
        boolean isAdmin = data.containsKey("isAdmin") && (boolean) data.get("isAdmin");
        
        // Display info
        ItemStack infoItem = guiManager.createGuiItem(Material.NAME_TAG, "&e&lShop Info", 
                Arrays.asList(
                    "&7Name: &f" + shopName,
                    "&7Type: &f" + (isAdmin ? "Admin Shop" : "Player Shop")
                ));
        inventory.setItem(4, infoItem);
        
        // Shop size options
        ItemStack smallItem = guiManager.createGuiItem(Material.CHEST, "&e&lSmall Shop", 
                Arrays.asList(
                    "&727 slots",
                    "&7Cost: &f" + plugin.getEconomyManager().formatCurrency(100, plugin.getEconomyManager().getDefaultCurrency()),
                    "",
                    "&7Click to select"
                ));
        inventory.setItem(19, smallItem);
        
        ItemStack mediumItem = guiManager.createGuiItem(Material.CHEST, "&e&lMedium Shop", 
                Arrays.asList(
                    "&754 slots",
                    "&7Cost: &f" + plugin.getEconomyManager().formatCurrency(500, plugin.getEconomyManager().getDefaultCurrency()),
                    "",
                    "&7Click to select"
                ));
        inventory.setItem(22, mediumItem);
        
        ItemStack largeItem = guiManager.createGuiItem(Material.CHEST, "&e&lLarge Shop", 
                Arrays.asList(
                    "&781 slots",
                    "&7Cost: &f" + plugin.getEconomyManager().formatCurrency(1000, plugin.getEconomyManager().getDefaultCurrency()),
                    "",
                    "&7Click to select"
                ));
        inventory.setItem(25, largeItem);
        
        // Navigation buttons
        ItemStack prevItem = guiManager.createGuiItem(Material.ARROW, "&7&lPrevious Step", 
                Collections.singletonList("&7Return to shop name"));
        inventory.setItem(48, prevItem);
        
        ItemStack cancelItem = guiManager.createGuiItem(Material.BARRIER, "&c&lCancel", 
                Collections.singletonList("&7Cancel shop creation"));
        inventory.setItem(49, cancelItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
    }
    
    /**
     * Setup step 3 of the shop creation process (select location)
     *
     * @param guiManager The GUI manager
     * @param inventory The inventory to setup
     * @param player The player
     * @param data The shop data
     */
    private static void setupStep3(GuiManager guiManager, Inventory inventory, Player player, Map<String, Object> data) {
        // Get shop details
        String shopName = (String) data.get("name");
        boolean isAdmin = data.containsKey("isAdmin") && (boolean) data.get("isAdmin");
        String shopSize = (String) data.get("size");
        
        // Display info
        ItemStack infoItem = guiManager.createGuiItem(Material.NAME_TAG, "&e&lShop Info", 
                Arrays.asList(
                    "&7Name: &f" + shopName,
                    "&7Type: &f" + (isAdmin ? "Admin Shop" : "Player Shop"),
                    "&7Size: &f" + shopSize
                ));
        inventory.setItem(4, infoItem);
        
        // Location options
        ItemStack currentItem = guiManager.createGuiItem(Material.COMPASS, "&e&lCurrent Location", 
                Arrays.asList(
                    "&7Location: &f" + formatLocation(player.getLocation()),
                    "",
                    "&7Click to select"
                ));
        inventory.setItem(21, currentItem);
        
        ItemStack customItem = guiManager.createGuiItem(Material.MAP, "&e&lCustom Location", 
                Arrays.asList(
                    "&7Teleport to a location",
                    "&7and then select this option",
                    "",
                    "&7Click to select"
                ));
        inventory.setItem(23, customItem);
        
        // Navigation buttons
        ItemStack prevItem = guiManager.createGuiItem(Material.ARROW, "&7&lPrevious Step", 
                Collections.singletonList("&7Return to shop size"));
        inventory.setItem(48, prevItem);
        
        ItemStack cancelItem = guiManager.createGuiItem(Material.BARRIER, "&c&lCancel", 
                Collections.singletonList("&7Cancel shop creation"));
        inventory.setItem(49, cancelItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
    }
    
    /**
     * Setup step 4 of the shop creation process (confirmation)
     *
     * @param guiManager The GUI manager
     * @param inventory The inventory to setup
     * @param player The player
     * @param data The shop data
     */
    private static void setupStep4(GuiManager guiManager, Inventory inventory, Player player, Map<String, Object> data) {
        // Get plugin instance from guiManager
        FrizzlenShop plugin = guiManager.getPlugin();
        
        // Get shop details
        String shopName = (String) data.get("name");
        boolean isAdmin = data.containsKey("isAdmin") && (boolean) data.get("isAdmin");
        String shopSize = (String) data.get("size");
        String locationType = (String) data.get("locationType");
        
        // Fix for null locationType
        if (locationType == null) {
            // Default to current location if locationType is not set
            locationType = "current";
            data.put("locationType", locationType);
            data.put("location", player.getLocation());
        }
        
        org.bukkit.Location location = locationType.equals("current") 
                ? player.getLocation() 
                : (org.bukkit.Location) data.get("location");
        
        // Shop cost
        double cost = 0;
        switch (shopSize) {
            case "Small":
                cost = 100;
                break;
            case "Medium":
                cost = 500;
                break;
            case "Large":
                cost = 1000;
                break;
        }
        
        // Display info
        ItemStack infoItem = guiManager.createGuiItem(Material.NAME_TAG, "&e&lShop Info", 
                Arrays.asList(
                    "&7Name: &f" + shopName,
                    "&7Type: &f" + (isAdmin ? "Admin Shop" : "Player Shop"),
                    "&7Size: &f" + shopSize,
                    "&7Location: &f" + formatLocation(location),
                    "",
                    "&7Cost: &f" + plugin.getEconomyManager().formatCurrency(cost, plugin.getEconomyManager().getDefaultCurrency())
                ));
        inventory.setItem(4, infoItem);
        
        // Confirm button
        ItemStack confirmItem = guiManager.createGuiItem(Material.EMERALD_BLOCK, "&a&lConfirm", 
                Arrays.asList(
                    "&7Create your shop with",
                    "&7the selected options",
                    "",
                    "&7Click to create shop"
                ));
        inventory.setItem(22, confirmItem);
        
        // Navigation buttons
        ItemStack prevItem = guiManager.createGuiItem(Material.ARROW, "&7&lPrevious Step", 
                Collections.singletonList("&7Return to location selection"));
        inventory.setItem(48, prevItem);
        
        ItemStack cancelItem = guiManager.createGuiItem(Material.BARRIER, "&c&lCancel", 
                Collections.singletonList("&7Cancel shop creation"));
        inventory.setItem(49, cancelItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
    }
    
    /**
     * Handle a click in step 1 of the shop creation process
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    private static boolean handleStep1Click(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        Map<String, Object> data = menuData.getAllData();
        boolean isAdmin = data.containsKey("isAdmin") && (boolean) data.get("isAdmin");
        
        switch (slot) {
            case 22: // Enter name
                player.closeInventory();
                // Register chat action for shop name entry
                plugin.getChatListener().registerPendingAction(
                    player, 
                    new org.frizzlenpop.frizzlenShop.listeners.ChatListener.ChatAction(
                        isAdmin ? org.frizzlenpop.frizzlenShop.listeners.ChatListener.ChatActionType.CREATE_ADMIN_SHOP 
                              : org.frizzlenpop.frizzlenShop.listeners.ChatListener.ChatActionType.CREATE_PLAYER_SHOP
                    )
                );
                MessageUtils.sendMessage(player, "&eEnter a name for your shop:");
                return true;
                
            case 49: // Cancel
                player.closeInventory();
                MessageUtils.sendMessage(player, "&cShop creation cancelled.");
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Handle a click in step 2 of the shop creation process
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    private static boolean handleStep2Click(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        Map<String, Object> data = menuData.getAllData();
        
        switch (slot) {
            case 19: // Small shop
                data.put("size", "Small");
                
                // Update menu data before opening next step
                guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.CREATE_SHOP_MENU, data));
                
                guiManager.openCreateShopMenu(player, 3);
                return true;
                
            case 22: // Medium shop
                data.put("size", "Medium");
                
                // Update menu data before opening next step
                guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.CREATE_SHOP_MENU, data));
                
                guiManager.openCreateShopMenu(player, 3);
                return true;
                
            case 25: // Large shop
                data.put("size", "Large");
                
                // Update menu data before opening next step
                guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.CREATE_SHOP_MENU, data));
                
                guiManager.openCreateShopMenu(player, 3);
                return true;
                
            case 48: // Previous step
                guiManager.openCreateShopMenu(player, 1);
                return true;
                
            case 49: // Cancel
                player.closeInventory();
                MessageUtils.sendMessage(player, "&cShop creation cancelled.");
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Handle a click in step 3 of the shop creation process
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    private static boolean handleStep3Click(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        Map<String, Object> data = menuData.getAllData();
        
        switch (slot) {
            case 21: // Current location
                // Store the choice in menu data
                data.put("locationType", "current");
                
                // Update menu data before opening next step
                guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.CREATE_SHOP_MENU, data));
                
                // Open next step
                guiManager.openCreateShopMenu(player, 4);
                return true;
                
            case 23: // Custom location
                // Store the choice in menu data
                data.put("locationType", "custom");
                data.put("location", player.getLocation());
                
                // Update menu data before opening next step
                guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.CREATE_SHOP_MENU, data));
                
                // Open next step
                guiManager.openCreateShopMenu(player, 4);
                return true;
                
            case 48: // Previous step
                guiManager.openCreateShopMenu(player, 2);
                return true;
                
            case 49: // Cancel
                player.closeInventory();
                MessageUtils.sendMessage(player, "&cShop creation cancelled.");
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Handle a click in step 4 of the shop creation process
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    private static boolean handleStep4Click(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        Map<String, Object> data = menuData.getAllData();
        
        switch (slot) {
            case 22: // Confirm
                player.closeInventory();
                
                // Get shop details
                String shopName = (String) data.get("name");
                boolean isAdmin = data.containsKey("isAdmin") && (boolean) data.get("isAdmin");
                String shopSize = (String) data.get("size");
                String locationType = (String) data.get("locationType");
                org.bukkit.Location location = locationType.equals("current") 
                        ? player.getLocation() 
                        : (org.bukkit.Location) data.get("location");
                
                // Shop cost
                double cost = 0;
                switch (shopSize) {
                    case "Small":
                        cost = 100;
                        break;
                    case "Medium":
                        cost = 500;
                        break;
                    case "Large":
                        cost = 1000;
                        break;
                }
                
                // Check if player has enough money
                if (!isAdmin && !plugin.getEconomyManager().has(player.getUniqueId(), cost, plugin.getEconomyManager().getDefaultCurrency())) {
                    MessageUtils.sendErrorMessage(player, "You don't have enough money to create this shop.");
                    return true;
                }
                
                // Create the shop
                if (isAdmin) {
                    // Create admin shop
                    if (plugin.getShopManager().createAdminShop(shopName, location) != null) {
                        MessageUtils.sendSuccessMessage(player, "Admin shop created successfully!");
                    } else {
                        MessageUtils.sendErrorMessage(player, "Failed to create admin shop.");
                    }
                } else {
                    // Create player shop
                    if (plugin.getShopManager().createPlayerShop(shopName, player.getUniqueId(), location) != null) {
                        // Withdraw money
                        plugin.getEconomyManager().withdraw(player.getUniqueId(), cost, plugin.getEconomyManager().getDefaultCurrency());
                        MessageUtils.sendSuccessMessage(player, "Shop created successfully!");
                    } else {
                        MessageUtils.sendErrorMessage(player, "Failed to create shop.");
                    }
                }
                
                return true;
                
            case 48: // Previous step
                guiManager.openCreateShopMenu(player, 3);
                return true;
                
            case 49: // Cancel
                player.closeInventory();
                MessageUtils.sendMessage(player, "&cShop creation cancelled.");
                return true;
                
            default:
                return false;
        }
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
} 