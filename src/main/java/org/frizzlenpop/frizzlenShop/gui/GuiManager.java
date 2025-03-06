package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.listeners.ChatListener;
import org.frizzlenpop.frizzlenShop.shops.AdminShop;
import org.frizzlenpop.frizzlenShop.shops.PlayerShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.*;

/**
 * Manages all GUI elements for the plugin
 */
public class GuiManager {

    private final FrizzlenShop plugin;
    public final Map<UUID, MenuData> menuData; // Stores what menu a player is viewing
    
    // Constants for menu names
    public static final String MAIN_MENU = "main";
    public static final String CATEGORY_MENU = "category";
    public static final String ITEM_DETAILS_MENU = "item_details";
    public static final String MY_SHOPS_MENU = "my_shops";
    public static final String CREATE_SHOP_MENU = "create_shop";
    public static final String SHOP_ADMIN_MENU = "shop_admin";
    
    /**
     * Create a new GUI manager
     *
     * @param plugin The plugin instance
     */
    public GuiManager(FrizzlenShop plugin) {
        this.plugin = plugin;
        this.menuData = new HashMap<>();
    }
    
    /**
     * Get the plugin instance
     *
     * @return The plugin instance
     */
    public FrizzlenShop getPlugin() {
        return plugin;
    }
    
    /**
     * Creates a GUI item with colored name and lore
     *
     * @param material The material for the item
     * @param name The name of the item
     * @param lore The lore for the item
     * @return The created ItemStack
     */
    public ItemStack createGuiItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        
        // Set the name (applying color codes)
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        
        // Set the lore (applying color codes)
        if (lore != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(coloredLore);
        }
        
        // Hide item attributes
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        
        item.setItemMeta(meta);
        
        return item;
    }
    
    /**
     * Fill empty slots in inventory with glass panes
     *
     * @param inventory The inventory to fill
     */
    public void fillEmptySlots(Inventory inventory) {
        ItemStack fillerItem = createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " ", Collections.emptyList());
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillerItem);
            }
        }
    }
    
    /**
     * Get the menu data for a player
     *
     * @param playerUuid The player's UUID
     * @return The menu data, or null if not found
     */
    public MenuData getMenuData(UUID playerUuid) {
        return menuData.get(playerUuid);
    }
    
    /**
     * Clear the menu data for a player
     *
     * @param playerUuid The player's UUID
     */
    public void clearMenuData(UUID playerUuid) {
        menuData.remove(playerUuid);
    }
    
    /**
     * Open the main menu
     *
     * @param player The player to open the menu for
     */
    public void openMainMenu(Player player) {
        // Use the MainMenuHandler to handle the main menu
        MainMenuHandler.openMainMenu(this, plugin, player);
    }
    
    /**
     * Open the category menu
     *
     * @param player The player to open the menu for
     * @param category The category to show
     * @param page The page number
     */
    public void openCategoryMenu(Player player, String category, int page) {
        // Use the CategoryMenuHandler to handle the category menu
        CategoryMenuHandler.openCategoryMenu(this, plugin, player, category, page);
    }
    
    /**
     * Open the item details menu
     *
     * @param player The player to open the menu for
     * @param shopItemData The item data to show
     */
    public void openItemDetailsMenu(Player player, ShopItemData shopItemData) {
        // Use the ItemDetailsMenuHandler to handle the item details menu
        ItemDetailsMenuHandler.openItemDetailsMenu(this, plugin, player, shopItemData);
    }
    
    /**
     * Open my shops menu
     *
     * @param player The player to open the menu for
     */
    public void openMyShopsMenu(Player player) {
        // Use the MyShopsMenuHandler to handle the my shops menu
        MyShopsMenuHandler.openMyShopsMenu(this, plugin, player);
    }
    
    /**
     * Open create shop menu
     *
     * @param player The player to open the menu for
     * @param step The creation step
     */
    public void openCreateShopMenu(Player player, int step) {
        // Use the CreateShopMenuHandler to handle the create shop menu
        CreateShopMenuHandler.openCreateShopMenu(this, plugin, player, step);
    }
    
    /**
     * Open shop admin menu
     *
     * @param player The player to open the menu for
     */
    public void openShopAdminMenu(Player player) {
        // Use the ShopAdminMenuHandler to handle the shop admin menu
        ShopAdminMenuHandler.openShopAdminMenu(this, plugin, player);
    }
    
    /**
     * Open shop management menu
     *
     * @param player The player to open the menu for
     * @param shopId The shop ID
     */
    public void openShopManagementMenu(Player player, UUID shopId) {
        Shop shop = plugin.getShopManager().getShop(shopId);
        if (shop == null) {
            MessageUtils.sendErrorMessage(player, "Shop not found.");
            return;
        }
        // Use the ShopManagementMenuHandler to handle the shop management menu
        ShopManagementMenuHandler.openShopManagementMenu(this, plugin, player, shop);
    }
    
    /**
     * Open item management menu
     *
     * @param player The player to open the menu for
     * @param shopId The shop ID
     * @param itemId The item ID
     */
    public void openItemManagementMenu(Player player, UUID shopId, UUID itemId) {
        Shop shop = plugin.getShopManager().getShop(shopId);
        if (shop == null) {
            MessageUtils.sendErrorMessage(player, "Shop not found.");
            return;
        }
        
        ShopItem item = shop.getItem(itemId);
        if (item == null) {
            MessageUtils.sendErrorMessage(player, "Item not found.");
            return;
        }
        
        // Use the ItemManagementMenuHandler to handle the item management menu
        ItemManagementMenuHandler.openItemManagementMenu(this, plugin, player, shop, item);
    }
    
    /**
     * Handle click in any menu
     *
     * @param player The player who clicked
     * @param inventory The inventory that was clicked
     * @param slot The slot that was clicked
     * @return True if the click was handled, false otherwise
     */
    public boolean handleClick(Player player, Inventory inventory, int slot, ClickType clickType) {
        UUID playerUuid = player.getUniqueId();
        MenuData data = menuData.get(playerUuid);
        
        if (data == null) {
            return false;
        }
        
        switch (data.getMenuType()) {
            case MAIN_MENU:
                return MainMenuHandler.handleClick(this, plugin, player, slot);
                
            case CATEGORY_MENU:
                return CategoryMenuHandler.handleClick(this, plugin, player, slot, data);
                
            case ITEM_DETAILS_MENU:
                return ItemDetailsMenuHandler.handleClick(this, plugin, player, slot, data);
                
            case MY_SHOPS_MENU:
                return MyShopsMenuHandler.handleClick(this, plugin, player, slot);
                
            case CREATE_SHOP_MENU:
                return CreateShopMenuHandler.handleClick(this, plugin, player, slot, data);
                
            case SHOP_ADMIN_MENU:
                return ShopAdminMenuHandler.handleClick(this, plugin, player, slot);
                
            case SHOP_MANAGEMENT:
                return ShopManagementMenuHandler.handleClick(this, plugin, player, slot, data);
                
            case ITEM_MANAGEMENT:
                return ItemManagementMenuHandler.handleClick(this, plugin, player, slot, data, clickType);
                
            case SHOP_ITEMS:
                return ShopItemsMenuHandler.handleClick(this, plugin, player, slot, data);
                
            default:
                return false;
        }
    }
    
    /**
     * Open the shop creation menu for a player
     *
     * @param player The player
     * @param shopName The shop name
     * @param isAdmin Whether this is an admin shop
     */
    public void openShopCreationMenu(Player player, String shopName, boolean isAdmin) {
        // Implementation will be in CreateShopMenuHandler
        CreateShopMenuHandler.openShopCreationMenu(this, plugin, player, shopName, isAdmin);
    }
    
    /**
     * Open the transaction logs menu for a player
     *
     * @param player The player to open the menu for
     */
    public void openTransactionLogsMenu(Player player) {
        // Use the TransactionLogsMenuHandler to handle the transaction logs menu
        TransactionLogsMenuHandler.openTransactionLogsMenu(this, plugin, player);
    }
} 