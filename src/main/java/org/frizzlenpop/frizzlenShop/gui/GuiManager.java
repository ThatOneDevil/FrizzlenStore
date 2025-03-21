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
import org.frizzlenpop.frizzlenShop.gui.AdminShopsMenuHandler;
import org.frizzlenpop.frizzlenShop.templates.TemplateMenuHandler;
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
     * @param playerUuid The player UUID
     * @return The menu data, or null if not found
     */
    public MenuData getMenuData(UUID playerUuid) {
        return menuData.get(playerUuid);
    }
    
    /**
     * Clear the menu data for a player
     *
     * @param playerUuid The player UUID
     */
    public void clearMenuData(UUID playerUuid) {
        menuData.remove(playerUuid);
    }
    
    /**
     * Updates menu data for a player, preserving previous menu information
     *
     * @param playerUuid The player UUID
     * @param newMenuData The new menu data
     */
    public void updateMenuData(UUID playerUuid, MenuData newMenuData) {
        MenuData currentData = menuData.get(playerUuid);
        if (currentData != null) {
            // Store current menu type as previous menu type in the new menu data
            newMenuData.setPreviousMenuType(currentData.getMenuType());
        }
        menuData.put(playerUuid, newMenuData);
    }
    
    /**
     * Open the main menu for a player
     *
     * @param player The player to open the menu for
     */
    public void openMainMenu(Player player) {
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
     * Open admin shops menu
     *
     * @param player The player to open the menu for
     */
    public void openAdminShopsMenu(Player player) {
        // Use the AdminShopsMenuHandler to handle the admin shops menu
        AdminShopsMenuHandler.openAdminShopsMenu(this, plugin, player);
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
     * Open shop statistics menu
     *
     * @param player The player to open the menu for
     */
    public void openShopStatisticsMenu(Player player) {
        // Use the ShopAdminMenuHandler to handle the shop statistics menu
        ShopAdminMenuHandler.openStatisticsMenu(this, plugin, player);
    }
    
    /**
     * Open price management menu
     *
     * @param player The player to open the menu for
     */
    public void openPriceManagementMenu(Player player) {
        // Use the ShopAdminMenuHandler to handle the price management menu
        ShopAdminMenuHandler.openPriceManagementMenu(this, plugin, player);
    }
    
    /**
     * Open tax management menu
     *
     * @param player The player to open the menu for
     */
    public void openTaxManagementMenu(Player player) {
        // Use the ShopAdminMenuHandler to handle the tax management menu
        ShopAdminMenuHandler.openTaxManagementMenu(this, plugin, player);
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
     * Open market trends menu
     *
     * @param player The player to open the menu for
     */
    public void openMarketTrendsMenu(Player player) {
        // Use the ShopAdminMenuHandler to handle the market trends menu
        ShopAdminMenuHandler.openMarketTrendsMenu(this, plugin, player);
    }
    
    /**
     * Open crafting opportunities menu
     *
     * @param player The player to open the menu for
     */
    public void openCraftingOpportunitiesMenu(Player player) {
        // Use the ShopAdminMenuHandler to handle the crafting opportunities menu
        ShopAdminMenuHandler.openCraftingOpportunitiesMenu(this, plugin, player);
    }
    
    /**
     * Open template management menu
     *
     * @param player The player to open the menu for
     */
    public void openTemplateManagementMenu(Player player) {
        // Use the TemplateMenuHandler to open the template management menu
        TemplateMenuHandler.openTemplateManagementMenu(this, plugin, player);
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
            plugin.getLogger().warning("No menu data found for player " + player.getName());
            return false;
        }
        
        // Debug log which menu type we're handling
        plugin.getLogger().info("Handling click in " + data.getMenuType() + " menu at slot " + slot);
        
        try {
            switch (data.getMenuType()) {
                case MAIN_MENU:
                    return MainMenuHandler.handleClick(this, plugin, player, slot);
                    
                case CATEGORY_MENU:
                    return CategoryMenuHandler.handleClick(this, plugin, player, slot, data);
                    
                case ITEM_DETAILS_MENU:
                    return ItemDetailsMenuHandler.handleClick(this, plugin, player, slot, data);
                    
                case MY_SHOPS_MENU:
                    return MyShopsMenuHandler.handleClick(this, plugin, player, slot);
                    
                case ADMIN_SHOPS_MENU:
                    return AdminShopsMenuHandler.handleClick(this, plugin, player, slot, data);
                    
                case CREATE_SHOP_MENU:
                    return CreateShopMenuHandler.handleClick(this, plugin, player, slot, data);
                    
                case SHOP_ADMIN_MENU:
                    return ShopAdminMenuHandler.handleClick(this, plugin, player, slot);
                    
                case ADMIN_SHOP_MANAGEMENT:
                    return ShopAdminMenuHandler.handleAdminShopManagementClick(this, plugin, player, slot, data);
                    
                case SHOP_MANAGEMENT:
                    return ShopManagementMenuHandler.handleClick(this, plugin, player, slot, data);
                    
                case ITEM_MANAGEMENT:
                    return ItemManagementMenuHandler.handleClick(this, plugin, player, slot, data, clickType);
                    
                case SHOP_ITEMS:
                    return ShopItemsMenuHandler.handleClick(this, plugin, player, slot, data);
                    
                case ADMIN_BULK_ITEM_MANAGEMENT:
                    return ShopAdminMenuHandler.handleBulkItemManagementClick(this, plugin, player, slot);
                    
                case PRICE_MANAGEMENT:
                    return ShopAdminMenuHandler.handlePriceManagementClick(this, plugin, player, slot, clickType);
                    
                case TAX_MANAGEMENT:
                    return ShopAdminMenuHandler.handleTaxManagementClick(this, plugin, player, slot, clickType);
                    
                case SHOP_STATISTICS:
                    return ShopAdminMenuHandler.handleStatisticsMenuClick(this, plugin, player, slot, clickType);
                    
                case MARKET_TRENDS:
                    return ShopAdminMenuHandler.handleMarketTrendsClick(this, plugin, player, slot);
                    
                case CRAFTING_OPPORTUNITIES:
                    return ShopAdminMenuHandler.handleCraftingOpportunitiesClick(this, plugin, player, slot);
                    
                case TEMPLATE_MANAGEMENT:
                    return TemplateMenuHandler.handleClick(this, plugin, player, slot);
                    
                case TEMPLATE_ITEMS:
                    return TemplateMenuHandler.handleTemplatesListClick(this, plugin, player, slot, data);
                    
                case TEMPLATE_CATEGORIES:
                    return TemplateMenuHandler.handleTemplateCategoriesClick(this, plugin, player, slot, data);
                    
                case TEMPLATE_CREATION:
                    return TemplateMenuHandler.handleTemplateCreationClick(this, plugin, player, slot, data);
                    
                case SHOP_BACKUP:
                    plugin.getLogger().warning("Shop backup click handling not implemented yet");
                    return false; // TODO: Implement backup click handling
                    
                case SHOP_RESTORE:
                    plugin.getLogger().warning("Shop restore click handling not implemented yet");
                    return false; // TODO: Implement restore click handling
                    
                case SHOP_SETTINGS:
                    return ShopSettingsMenuHandler.handleClick(this, plugin, player, slot, data);
                    
                case QUICK_SELL_MENU:
                    return QuickSellMenuHandler.handleClick(this, plugin, player, slot, data);
                    
                default:
                    plugin.getLogger().warning("Unknown menu type: " + data.getMenuType());
                    return false;
            }
        } catch (Exception e) {
            // Log any exceptions that occur during click handling
            plugin.getLogger().severe("Error handling click in " + data.getMenuType() + " menu: " + e.getMessage());
            e.printStackTrace();
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
    
    /**
     * Open the quick sell menu for a player
     *
     * @param player The player to open the menu for
     */
    public void openQuickSellMenu(Player player) {
        // Use the QuickSellMenuHandler to handle the quick sell menu
        QuickSellMenuHandler.openQuickSellMenu(this, plugin, player);
    }
    
    /**
     * Return to the previous menu for a player if available
     *
     * @param player The player to navigate
     * @return True if successfully returned to a previous menu, false otherwise
     */
    public boolean returnToPreviousMenu(Player player) {
        UUID playerUuid = player.getUniqueId();
        MenuData data = menuData.get(playerUuid);
        
        if (data == null || !data.hasPreviousMenu()) {
            // Default to main menu if no previous menu
            openMainMenu(player);
            return false;
        }
        
        MenuType previousType = data.getPreviousMenuType();
        
        // Handle navigation based on previous menu type
        switch (previousType) {
            case MAIN_MENU:
                openMainMenu(player);
                break;
                
            case SHOP_ADMIN_MENU:
                openShopAdminMenu(player);
                break;
                
            case ADMIN_SHOPS_MENU:
                openAdminShopsMenu(player);
                break;
                
            case MY_SHOPS_MENU:
                openMyShopsMenu(player);
                break;
                
            case CATEGORY_MENU:
                // We would need category info to return to the correct category
                // Default to main menu if we can't get it
                String category = data.getString("previous_category");
                int page = data.getInt("previous_page");
                if (category != null) {
                    openCategoryMenu(player, category, page > 0 ? page : 1);
                } else {
                    openMainMenu(player);
                }
                break;
                
            case SHOP_MANAGEMENT:
                UUID shopId = data.getUUID("previous_shop_id");
                if (shopId != null) {
                    openShopManagementMenu(player, shopId);
                } else {
                    openMyShopsMenu(player);
                }
                break;
                
            case SHOP_ITEMS:
                UUID itemsShopId = data.getUUID("id");
                if (itemsShopId != null) {
                    // Get the shop
                    Shop shop = plugin.getShopManager().getShop(itemsShopId);
                    if (shop != null) {
                        // Create a new menu to view shop items
                        ShopItemsMenuHandler.openShopItemsMenu(this, plugin, player, shop);
                        return true;
                    }
                }
                // Fallback to admin shops
                openAdminShopsMenu(player);
                break;
                
            default:
                // Default to main menu for any unhandled menu type
                openMainMenu(player);
                break;
        }
        
        return true;
    }
} 