package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.templates.ShopTemplate;
import org.frizzlenpop.frizzlenShop.templates.TemplateManager;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the template management GUI
 */
public class TemplateMenuHandler {

    /**
     * Open the template management menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    public static void openTemplateManagementMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Check permission
        if (!player.hasPermission("frizzlenshop.admin.templates")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to manage templates.");
            return;
        }
        
        // Create inventory
        String title = "Template Management";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Get all templates
        Collection<ShopTemplate> allTemplates = plugin.getTemplateManager().getAllTemplates();
        
        // Create template category buttons
        ItemStack adminTemplatesItem = guiManager.createGuiItem(
                Material.DIAMOND_BLOCK,
                "&b&lAdmin Shop Templates",
                Arrays.asList(
                        "&7View and manage templates for admin shops",
                        "&7Count: &f" + plugin.getTemplateManager().getAdminTemplates().size(),
                        "",
                        "&eClick to view"
                )
        );
        inventory.setItem(11, adminTemplatesItem);
        
        ItemStack playerTemplatesItem = guiManager.createGuiItem(
                Material.GOLD_BLOCK,
                "&6&lPlayer Shop Templates",
                Arrays.asList(
                        "&7View and manage templates for player shops",
                        "&7Count: &f" + plugin.getTemplateManager().getPlayerTemplates().size(),
                        "",
                        "&eClick to view"
                )
        );
        inventory.setItem(13, playerTemplatesItem);
        
        ItemStack categoryTemplatesItem = guiManager.createGuiItem(
                Material.BOOKSHELF,
                "&e&lTemplate Categories",
                Arrays.asList(
                        "&7Browse templates by category",
                        "&7Total Categories: &f" + getCategories(plugin).size(),
                        "",
                        "&eClick to view"
                )
        );
        inventory.setItem(15, categoryTemplatesItem);
        
        // Template actions
        ItemStack createTemplateItem = guiManager.createGuiItem(
                Material.WRITABLE_BOOK,
                "&a&lCreate New Template",
                Arrays.asList(
                        "&7Create a new shop template",
                        "",
                        "&eClick to start"
                )
        );
        inventory.setItem(29, createTemplateItem);
        
        ItemStack importTemplateItem = guiManager.createGuiItem(
                Material.KNOWLEDGE_BOOK,
                "&d&lImport From Shop",
                Arrays.asList(
                        "&7Create a template from an existing shop",
                        "",
                        "&eClick to select a shop"
                )
        );
        inventory.setItem(31, importTemplateItem);
        
        ItemStack backupRestoreItem = guiManager.createGuiItem(
                Material.ENDER_CHEST,
                "&9&lBackup & Restore",
                Arrays.asList(
                        "&7Backup and restore shop configurations",
                        "",
                        "&eClick to view options"
                )
        );
        inventory.setItem(33, backupRestoreItem);
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack to Admin Menu",
                Collections.singletonList("&7Return to the admin menu")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.TEMPLATE_MANAGEMENT));
    }
    
    /**
     * Handle a click in the template management menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot) {
        switch (slot) {
            case 11: // Admin Shop Templates
                openTemplatesList(guiManager, plugin, player, true);
                return true;
                
            case 13: // Player Shop Templates
                openTemplatesList(guiManager, plugin, player, false);
                return true;
                
            case 15: // Template Categories
                openTemplateCategories(guiManager, plugin, player);
                return true;
                
            case 29: // Create New Template
                openTemplateCreation(guiManager, plugin, player);
                return true;
                
            case 31: // Import From Shop
                openShopSelectionMenu(guiManager, plugin, player);
                return true;
                
            case 33: // Backup & Restore
                openBackupRestoreMenu(guiManager, plugin, player);
                return true;
                
            case 49: // Back to Admin Menu
                guiManager.openShopAdminMenu(player);
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Open the templates list for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param adminTemplates Whether to show admin templates (true) or player templates (false)
     */
    private static void openTemplatesList(GuiManager guiManager, FrizzlenShop plugin, Player player, boolean adminTemplates) {
        // Create inventory
        String title = adminTemplates ? "Admin Shop Templates" : "Player Shop Templates";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Get templates
        List<ShopTemplate> templates;
        if (adminTemplates) {
            templates = plugin.getTemplateManager().getAdminTemplates();
        } else {
            templates = plugin.getTemplateManager().getPlayerTemplates();
        }
        
        // Sort by name
        templates.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        
        // Add templates to inventory
        int slot = 0;
        for (ShopTemplate template : templates) {
            if (slot >= 45) break;
            
            // Format date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String creationDate = dateFormat.format(new Date(template.getCreationTime()));
            
            ItemStack templateItem = guiManager.createGuiItem(
                    adminTemplates ? Material.DIAMOND_BLOCK : Material.GOLD_BLOCK,
                    "&b&l" + template.getName(),
                    Arrays.asList(
                            "&7" + template.getDescription(),
                            "&7Category: &f" + template.getCategory(),
                            "&7Creator: &f" + template.getCreator(),
                            "&7Created: &f" + creationDate,
                            "&7Items: &f" + template.getItems().size(),
                            "&7Version: &f" + template.getVersion(),
                            "",
                            "&eClick to view details"
                    )
            );
            
            inventory.setItem(slot, templateItem);
            slot++;
        }
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack",
                Collections.singletonList("&7Return to template management")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        Map<String, Object> data = new HashMap<>();
        data.put("templates", templates);
        data.put("isAdminTemplates", adminTemplates);
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.TEMPLATE_ITEMS, data));
    }
    
    /**
     * Handle a click in the templates list menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    @SuppressWarnings("unchecked")
    public static boolean handleTemplatesListClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        List<ShopTemplate> templates = (List<ShopTemplate>) menuData.getData("templates");
        boolean isAdminTemplates = menuData.getBoolean("isAdminTemplates");
        
        if (templates == null) {
            return false;
        }
        
        // Back button
        if (slot == 49) {
            openTemplateManagementMenu(guiManager, plugin, player);
            return true;
        }
        
        // Check if click was on a template
        if (slot < 45 && slot < templates.size()) {
            ShopTemplate template = templates.get(slot);
            openTemplateDetails(guiManager, plugin, player, template);
            return true;
        }
        
        return false;
    }
    
    /**
     * Open the template details menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param template The template to show
     */
    private static void openTemplateDetails(GuiManager guiManager, FrizzlenShop plugin, Player player, ShopTemplate template) {
        // Create inventory
        String title = "Template: " + template.getName();
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Format date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String creationDate = dateFormat.format(new Date(template.getCreationTime()));
        
        // Template info
        ItemStack infoItem = guiManager.createGuiItem(
                Material.PAPER,
                "&e&lTemplate Info",
                Arrays.asList(
                        "&7Name: &f" + template.getName(),
                        "&7Description: &f" + template.getDescription(),
                        "&7Category: &f" + template.getCategory(),
                        "&7Creator: &f" + template.getCreator(),
                        "&7Created: &f" + creationDate,
                        "&7Type: &f" + (template.isAdminTemplate() ? "Admin Shop" : "Player Shop"),
                        "&7Version: &f" + template.getVersion()
                )
        );
        inventory.setItem(4, infoItem);
        
        // Template items
        int itemCount = template.getItems().size();
        ItemStack itemsItem = guiManager.createGuiItem(
                Material.CHEST,
                "&e&lTemplate Items",
                Arrays.asList(
                        "&7Items: &f" + itemCount,
                        "",
                        "&eClick to view items"
                )
        );
        inventory.setItem(11, itemsItem);
        
        // Create shop from template
        ItemStack createShopItem = guiManager.createGuiItem(
                Material.EMERALD,
                "&a&lCreate Shop",
                Arrays.asList(
                        "&7Create a new shop from this template",
                        "",
                        "&eClick to create"
                )
        );
        inventory.setItem(13, createShopItem);
        
        // Edit template
        ItemStack editItem = guiManager.createGuiItem(
                Material.WRITABLE_BOOK,
                "&b&lEdit Template",
                Arrays.asList(
                        "&7Edit this template",
                        "",
                        "&eClick to edit"
                )
        );
        inventory.setItem(15, editItem);
        
        // Delete template
        ItemStack deleteItem = guiManager.createGuiItem(
                Material.BARRIER,
                "&c&lDelete Template",
                Arrays.asList(
                        "&7Delete this template",
                        "&cWarning: This cannot be undone!",
                        "",
                        "&eClick to delete"
                )
        );
        inventory.setItem(31, deleteItem);
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack",
                Collections.singletonList("&7Return to templates list")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        Map<String, Object> data = new HashMap<>();
        data.put("template", template);
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.TEMPLATE_ITEMS, data));
    }
    
    /**
     * Open the template creation menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    private static void openTemplateCreation(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Create inventory
        String title = "Create Template";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Template type selection
        ItemStack adminTemplateItem = guiManager.createGuiItem(
                Material.DIAMOND_BLOCK,
                "&b&lAdmin Shop Template",
                Arrays.asList(
                        "&7Create a template for admin shops",
                        "",
                        "&eClick to select"
                )
        );
        inventory.setItem(11, adminTemplateItem);
        
        ItemStack playerTemplateItem = guiManager.createGuiItem(
                Material.GOLD_BLOCK,
                "&6&lPlayer Shop Template",
                Arrays.asList(
                        "&7Create a template for player shops",
                        "",
                        "&eClick to select"
                )
        );
        inventory.setItem(15, playerTemplateItem);
        
        // Instructions
        ItemStack helpItem = guiManager.createGuiItem(
                Material.OAK_SIGN,
                "&e&lInstructions",
                Arrays.asList(
                        "&7Templates allow you to create shops quickly",
                        "&7with predefined items and prices.",
                        "",
                        "&71. Select the type of template to create",
                        "&72. Enter a name for your template",
                        "&73. Enter a description",
                        "&74. Select a category",
                        "&75. Add items to the template"
                )
        );
        inventory.setItem(31, helpItem);
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack",
                Collections.singletonList("&7Return to template management")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.TEMPLATE_CREATION));
    }
    
    /**
     * Open the template categories menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    private static void openTemplateCategories(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Create inventory
        String title = "Template Categories";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Get all categories
        List<String> categories = getCategories(plugin);
        
        // Sort categories alphabetically
        Collections.sort(categories);
        
        // Add categories to inventory
        int slot = 0;
        for (String category : categories) {
            if (slot >= 45) break;
            
            List<ShopTemplate> templatesInCategory = plugin.getTemplateManager().getTemplatesByCategory(category);
            int adminCount = (int) templatesInCategory.stream().filter(ShopTemplate::isAdminTemplate).count();
            int playerCount = templatesInCategory.size() - adminCount;
            
            ItemStack categoryItem = guiManager.createGuiItem(
                    Material.BOOKSHELF,
                    "&e&l" + category,
                    Arrays.asList(
                            "&7Templates in category: &f" + templatesInCategory.size(),
                            "&7Admin Templates: &f" + adminCount,
                            "&7Player Templates: &f" + playerCount,
                            "",
                            "&eClick to view templates"
                    )
            );
            
            inventory.setItem(slot, categoryItem);
            slot++;
        }
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack",
                Collections.singletonList("&7Return to template management")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        Map<String, Object> data = new HashMap<>();
        data.put("categories", categories);
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.TEMPLATE_CATEGORIES, data));
    }
    
    /**
     * Open the shop selection menu for creating a template from a shop
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    private static void openShopSelectionMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Create inventory
        String title = "Select Shop for Template";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Get all shops
        Collection<Shop> allShops = plugin.getShopManager().getAllShops();
        
        // Sort shops by name
        List<Shop> sortedShops = new ArrayList<>(allShops);
        sortedShops.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        
        // Add shops to inventory
        int slot = 0;
        for (Shop shop : sortedShops) {
            if (slot >= 45) break;
            
            Material material = shop.isAdminShop() ? Material.DIAMOND_BLOCK : Material.GOLD_BLOCK;
            
            ItemStack shopItem = guiManager.createGuiItem(
                    material,
                    "&e&l" + shop.getName(),
                    Arrays.asList(
                            "&7Type: &f" + (shop.isAdminShop() ? "Admin Shop" : "Player Shop"),
                            "&7Items: &f" + shop.getItems().size(),
                            "",
                            "&eClick to create template from this shop"
                    )
            );
            
            inventory.setItem(slot, shopItem);
            slot++;
        }
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack",
                Collections.singletonList("&7Return to template management")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        Map<String, Object> data = new HashMap<>();
        data.put("shops", sortedShops);
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.TEMPLATE_CREATION, data));
    }
    
    /**
     * Open the backup and restore menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     */
    private static void openBackupRestoreMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Create inventory
        String title = "Backup & Restore";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);
        
        // Backup options
        ItemStack backupAllItem = guiManager.createGuiItem(
                Material.ENDER_CHEST,
                "&a&lBackup All Shops",
                Arrays.asList(
                        "&7Create a backup of all shops",
                        "",
                        "&eClick to create backup"
                )
        );
        inventory.setItem(11, backupAllItem);
        
        ItemStack backupAdminItem = guiManager.createGuiItem(
                Material.DIAMOND_BLOCK,
                "&b&lBackup Admin Shops",
                Arrays.asList(
                        "&7Create a backup of admin shops only",
                        "",
                        "&eClick to create backup"
                )
        );
        inventory.setItem(13, backupAdminItem);
        
        ItemStack backupPlayerItem = guiManager.createGuiItem(
                Material.GOLD_BLOCK,
                "&6&lBackup Player Shops",
                Arrays.asList(
                        "&7Create a backup of player shops only",
                        "",
                        "&eClick to create backup"
                )
        );
        inventory.setItem(15, backupPlayerItem);
        
        // Restore options
        ItemStack viewBackupsItem = guiManager.createGuiItem(
                Material.CHEST,
                "&e&lView Backups",
                Arrays.asList(
                        "&7View and restore shop backups",
                        "",
                        "&eClick to view backups"
                )
        );
        inventory.setItem(31, viewBackupsItem);
        
        // Add navigation buttons
        ItemStack backButton = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack",
                Collections.singletonList("&7Return to template management")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Open inventory
        player.openInventory(inventory);
        
        // Store menu data
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.SHOP_BACKUP));
    }
    
    /**
     * Get all unique template categories
     *
     * @param plugin The plugin instance
     * @return A list of all categories
     */
    private static List<String> getCategories(FrizzlenShop plugin) {
        TemplateManager templateManager = plugin.getTemplateManager();
        return templateManager.getAllTemplates().stream()
                .map(ShopTemplate::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }
} 