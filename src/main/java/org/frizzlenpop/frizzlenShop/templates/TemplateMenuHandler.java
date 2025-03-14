package org.frizzlenpop.frizzlenShop.templates;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.gui.GuiManager;
import org.frizzlenpop.frizzlenShop.gui.MenuData;
import org.frizzlenpop.frizzlenShop.gui.MenuType;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles GUI elements for template management
 */
public class TemplateMenuHandler {

    /**
     * Opens the template management menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player viewing the menu
     */
    public static void openTemplateManagementMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Check if player has permission to use templates
        if (!player.hasPermission("frizzlenshop.templates.view")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to use shop templates.");
            return;
        }

        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Shop Template Management");

        // Get template manager
        TemplateManager templateManager = plugin.getTemplateManager();
        
        // Create template categories button
        ItemStack categoriesItem = guiManager.createGuiItem(
                Material.BOOKSHELF,
                "&5&lTemplate Categories",
                Arrays.asList(
                        "&7View templates by category",
                        "&7There are &b" + getCategories(templateManager).size() + " &7categories available"
                )
        );
        inventory.setItem(11, categoriesItem);
        
        // Create my templates button
        ItemStack myTemplatesItem = guiManager.createGuiItem(
                Material.WRITABLE_BOOK,
                "&5&lMy Templates",
                Arrays.asList(
                        "&7View your created templates",
                        "&7You have &b" + templateManager.getTemplatesByCreator(player.getName()).size() + " &7templates"
                )
        );
        inventory.setItem(13, myTemplatesItem);
        
        // Create admin templates button
        ItemStack adminTemplatesItem = guiManager.createGuiItem(
                Material.ENCHANTED_BOOK,
                "&5&lAdmin Templates",
                Arrays.asList(
                        "&7View templates created by admins",
                        "&7There are &b" + templateManager.getAdminTemplates().size() + " &7admin templates"
                )
        );
        inventory.setItem(15, adminTemplatesItem);
        
        // Create new template button
        ItemStack createTemplateItem = guiManager.createGuiItem(
                Material.NETHER_STAR,
                "&a&lCreate New Template",
                Arrays.asList(
                        "&7Create a new shop template",
                        "&7Templates can be used to quickly",
                        "&7set up new shops with predefined items"
                )
        );
        inventory.setItem(29, createTemplateItem);
        
        // Create from existing shop button
        ItemStack createFromShopItem = guiManager.createGuiItem(
                Material.CRAFTING_TABLE,
                "&a&lCreate From Existing Shop",
                Arrays.asList(
                        "&7Create a template from one of your",
                        "&7existing shops to replicate its setup"
                )
        );
        inventory.setItem(31, createFromShopItem);
        
        // Backup/restore button
        ItemStack backupRestoreItem = guiManager.createGuiItem(
                Material.ENDER_CHEST,
                "&6&lBackup & Restore",
                Arrays.asList(
                        "&7Backup your shop templates",
                        "&7or restore from a previous backup"
                )
        );
        inventory.setItem(33, backupRestoreItem);
        
        // Back button
        ItemStack backItem = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack to Main Menu",
                Collections.singletonList("&7Return to the main menu")
        );
        inventory.setItem(49, backItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Set player's menu data
        UUID playerUuid = player.getUniqueId();
        MenuData menuData = new MenuData(MenuType.TEMPLATE_MANAGEMENT);
        guiManager.menuData.put(playerUuid, menuData);
        
        // Open inventory
        player.openInventory(inventory);
    }
    
    /**
     * Handle clicks in the template management menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot) {
        switch (slot) {
            case 11: // Template categories
                openTemplateCategories(guiManager, plugin, player);
                return true;
                
            case 13: // My templates
                openTemplatesList(guiManager, plugin, player, player.getUniqueId());
                return true;
                
            case 15: // Admin templates
                openTemplatesList(guiManager, plugin, player, null);
                return true;
                
            case 29: // Create new template
                openTemplateCreation(guiManager, plugin, player);
                return true;
                
            case 31: // Create from existing shop
                openShopSelectionMenu(guiManager, plugin, player);
                return true;
                
            case 33: // Backup/restore
                openBackupRestoreMenu(guiManager, plugin, player);
                return true;
                
            case 49: // Back to main menu
                guiManager.openMainMenu(player);
                return true;
                
            default:
                return false;
        }
    }
    
    /**
     * Opens the templates list
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player viewing the menu
     * @param creatorUuid The UUID of the creator, or null for admin templates
     */
    public static void openTemplatesList(GuiManager guiManager, FrizzlenShop plugin, Player player, UUID creatorUuid) {
        // Get the template manager
        TemplateManager templateManager = plugin.getTemplateManager();
        
        // Get the templates
        List<ShopTemplate> templates;
        String title;
        
        if (creatorUuid == null) {
            // Show admin templates
            templates = templateManager.getAdminTemplates();
            title = ChatColor.DARK_PURPLE + "Admin Templates";
        } else if (creatorUuid.equals(player.getUniqueId())) {
            // Show player's templates
            templates = templateManager.getTemplatesByCreator(player.getName());
            title = ChatColor.DARK_PURPLE + "My Templates";
        } else {
            // Show templates by a specific creator
            String creatorName = Bukkit.getOfflinePlayer(creatorUuid).getName();
            templates = templateManager.getTemplatesByCreator(creatorName);
            title = ChatColor.DARK_PURPLE + creatorName + "'s Templates";
        }
        
        // Sort templates by name
        templates.sort(Comparator.comparing(ShopTemplate::getName));
        
        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        
        // Add templates
        int slot = 0;
        for (ShopTemplate template : templates) {
            if (slot >= 45) break; // Max 45 templates per page
            
            // Display material based on category
            Material material = Material.PAPER;
            String category = template.getCategory();
            if (category != null) {
                switch (category.toLowerCase()) {
                    case "food":
                        material = Material.BREAD;
                        break;
                    case "weapons":
                        material = Material.IRON_SWORD;
                        break;
                    case "armor":
                        material = Material.IRON_CHESTPLATE;
                        break;
                    case "tools":
                        material = Material.IRON_PICKAXE;
                        break;
                    case "blocks":
                        material = Material.STONE;
                        break;
                    case "misc":
                        material = Material.CHEST;
                        break;
                }
            }
            
            // Create item
            List<String> lore = new ArrayList<>();
            lore.add("&7Category: &b" + (template.getCategory() != null ? template.getCategory() : "None"));
            lore.add("&7Items: &b" + template.getItems().size());
            lore.add("&7Created: &b" + new Date(template.getCreationTime()).toString());
            lore.add("");
            
            if (template.getDescription() != null && !template.getDescription().isEmpty()) {
                lore.add("&7Description:");
                lore.add("&f" + template.getDescription());
                lore.add("");
            }
            
            lore.add("&eClick to view details");
            
            ItemStack item = guiManager.createGuiItem(
                    material,
                    "&5&l" + template.getName(),
                    lore
            );
            
            inventory.setItem(slot, item);
            slot++;
        }
        
        // Back button
        ItemStack backItem = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack",
                Collections.singletonList("&7Return to the template management menu")
        );
        inventory.setItem(49, backItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Set player's menu data
        UUID playerUuid = player.getUniqueId();
        MenuData menuData = new MenuData(MenuType.TEMPLATE_ITEMS);
        menuData.setData("creatorUuid", creatorUuid != null ? creatorUuid.toString() : "admin");
        List<UUID> templateIdsList = templates.stream().map(ShopTemplate::getId).collect(Collectors.toList());
        menuData.setData("templates", templateIdsList);
        guiManager.menuData.put(playerUuid, menuData);
        
        // Open inventory
        player.openInventory(inventory);
    }
    
    /**
     * Handle clicks in the templates list menu
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
        if (slot == 49) {
            // Back button
            openTemplateManagementMenu(guiManager, plugin, player);
            return true;
        } else if (slot < 45) {
            // Template item
            Object templatesObj = menuData.getData("templates");
            if (templatesObj instanceof List) {
                List<UUID> templateIds = (List<UUID>) templatesObj;
                if (templateIds != null && slot < templateIds.size()) {
                    UUID templateId = templateIds.get(slot);
                    ShopTemplate template = plugin.getTemplateManager().getTemplate(templateId);
                    if (template != null) {
                        openTemplateDetails(guiManager, plugin, player, template);
                    }
                }
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Opens the template details menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player viewing the menu
     * @param template The template to show
     */
    public static void openTemplateDetails(GuiManager guiManager, FrizzlenShop plugin, Player player, ShopTemplate template) {
        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Template: " + template.getName());
        
        // Template info
        List<String> infoLore = new ArrayList<>();
        infoLore.add("&7Description: &f" + template.getDescription());
        infoLore.add("&7Category: &f" + template.getCategory());
        infoLore.add("&7Creator: &f" + template.getCreator());
        infoLore.add("&7Created: &f" + new Date(template.getCreationTime()).toString());
        infoLore.add("&7Type: &f" + (template.isAdminTemplate() ? "Admin Shop" : "Player Shop"));
        infoLore.add("&7Version: &f" + template.getVersion());
        
        ItemStack infoItem = guiManager.createGuiItem(
                Material.NAME_TAG,
                "&e&lTemplate Info",
                infoLore
        );
        inventory.setItem(4, infoItem);
        
        // Display template items
        List<TemplateItem> items = template.getItems();
        int itemSlot = 10;
        int count = 0;
        
        for (TemplateItem templateItem : items) {
            if (count >= 21) break; // Limit to 21 items for display
            
            ItemStack displayItem = templateItem.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            
            // Add additional information
            lore.add("");
            lore.add(ChatColor.GRAY + "Buy Price: " + ChatColor.GOLD + String.format("%.2f", templateItem.getBuyPrice()));
            lore.add(ChatColor.GRAY + "Sell Price: " + ChatColor.GOLD + String.format("%.2f", templateItem.getSellPrice()));
            lore.add(ChatColor.GRAY + "Currency: " + ChatColor.GOLD + templateItem.getCurrency());
            lore.add(ChatColor.GRAY + "Stock: " + ChatColor.GOLD + (templateItem.getStock() == -1 ? "Unlimited" : templateItem.getStock()));
            
            meta.setLore(lore);
            displayItem.setItemMeta(meta);
            
            inventory.setItem(itemSlot, displayItem);
            
            itemSlot++;
            count++;
            
            // Move to next row after 7 items
            if (count % 7 == 0) {
                itemSlot += 2;
            }
        }
        
        // Action buttons
        ItemStack useTemplateItem = guiManager.createGuiItem(
                Material.EMERALD,
                "&a&lUse Template",
                Arrays.asList(
                        "&7Create a new shop using this template",
                        "",
                        "&eClick to use"
                )
        );
        inventory.setItem(47, useTemplateItem);
        
        // Show edit button if player has permission
        if (player.hasPermission("frizzlenshop.admin.templates") || 
                (template.getCreator().equals(player.getName()) && player.hasPermission("frizzlenshop.templates.edit"))) {
            ItemStack editItem = guiManager.createGuiItem(
                    Material.WRITABLE_BOOK,
                    "&6&lEdit Template",
                    Arrays.asList(
                            "&7Edit this template's information and items",
                            "",
                            "&eClick to edit"
                    )
            );
            inventory.setItem(48, editItem);
        }
        
        // Show delete button if player has permission
        if (player.hasPermission("frizzlenshop.admin.templates") || 
                (template.getCreator().equals(player.getName()) && player.hasPermission("frizzlenshop.templates.delete"))) {
            ItemStack deleteItem = guiManager.createGuiItem(
                    Material.BARRIER,
                    "&c&lDelete Template",
                    Arrays.asList(
                            "&7Delete this template permanently",
                            "&cWarning: This cannot be undone!",
                            "",
                            "&eClick to delete"
                    )
            );
            inventory.setItem(50, deleteItem);
        }
        
        // Back button
        ItemStack backItem = guiManager.createGuiItem(
                Material.ARROW,
                "&b&lBack",
                Collections.singletonList("&7Return to templates list")
        );
        inventory.setItem(49, backItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Set player's menu data
        UUID playerUuid = player.getUniqueId();
        MenuData menuData = new MenuData(MenuType.TEMPLATE_ITEMS);
        menuData.setData("template", template.getId());
        menuData.setData("isAdmin", template.isAdminTemplate());
        guiManager.menuData.put(playerUuid, menuData);
        
        // Open inventory
        player.openInventory(inventory);
    }
    
    /**
     * Opens the template creation menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player viewing the menu
     */
    public static void openTemplateCreation(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Check permissions
        if (!player.hasPermission("frizzlenshop.templates.create")) {
            MessageUtils.sendErrorMessage(player, "You don't have permission to create templates.");
            return;
        }
        
        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_GREEN + "Create Template");
        
        // Add template type options
        ItemStack playerShopItem = guiManager.createGuiItem(
                Material.GOLD_BLOCK,
                "&6&lPlayer Shop Template",
                Arrays.asList(
                        "&7Create a template for player shops",
                        "",
                        "&eClick to select"
                )
        );
        inventory.setItem(11, playerShopItem);
        
        // Admin shop template option (if they have permission)
        if (player.hasPermission("frizzlenshop.admin.templates")) {
            ItemStack adminShopItem = guiManager.createGuiItem(
                    Material.DIAMOND_BLOCK,
                    "&b&lAdmin Shop Template",
                    Arrays.asList(
                            "&7Create a template for admin shops",
                            "&7(Requires admin permissions)",
                            "",
                            "&eClick to select"
                    )
            );
            inventory.setItem(15, adminShopItem);
        }
        
        // Add information/instructions
        ItemStack infoItem = guiManager.createGuiItem(
                Material.OAK_SIGN,
                "&e&lTemplate Creation",
                Arrays.asList(
                        "&7Creating a template allows you to quickly",
                        "&7set up new shops with predefined items.",
                        "",
                        "&71. Select the template type",
                        "&72. Enter a name for your template",
                        "&73. Enter a description",
                        "&74. Select a category",
                        "&75. Add items to your template"
                )
        );
        inventory.setItem(31, infoItem);
        
        // Back button
        ItemStack backItem = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack",
                Collections.singletonList("&7Return to template management")
        );
        inventory.setItem(49, backItem);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Set player's menu data
        UUID playerUuid = player.getUniqueId();
        MenuData menuData = new MenuData(MenuType.TEMPLATE_CREATION);
        guiManager.menuData.put(playerUuid, menuData);
        
        // Open inventory
        player.openInventory(inventory);
    }
    
    /**
     * Prompt player for template details via chat
     *
     * @param plugin The plugin instance
     * @param player The player to prompt
     * @param isAdminTemplate Whether this is an admin template
     */
    public static void promptForTemplateDetails(FrizzlenShop plugin, Player player, boolean isAdminTemplate) {
        // Prompt for template details
        player.closeInventory();
        
        // Start the conversation for template creation
        MessageUtils.sendMessage(player, "&a&lTemplate Creation");
        MessageUtils.sendMessage(player, "&7Please enter the following information in chat:");
        MessageUtils.sendMessage(player, "&7Type '&ccancel&7' at any time to cancel.");
        
        // Ask for template name
        MessageUtils.sendMessage(player, "");
        MessageUtils.sendMessage(player, "&eEnter a name for your template:");
        
        // Store this in player's metadata for the chat listener to pick up
        player.setMetadata("frizzlenshop.creating_template", new org.bukkit.metadata.FixedMetadataValue(plugin, isAdminTemplate));
    }
    
    /**
     * Create a new template with the given details
     *
     * @param plugin The plugin instance
     * @param player The player creating the template
     * @param name The template name
     * @param description The template description
     * @param category The template category
     * @param isAdminTemplate Whether this is an admin template
     */
    public static void createNewTemplate(FrizzlenShop plugin, Player player, String name, String description, 
                                        String category, boolean isAdminTemplate) {
        // Create the template
        ShopTemplate template = new ShopTemplate(name, description, isAdminTemplate, player.getName());
        template.setCategory(category);
        
        // Add the template to the manager
        boolean success = plugin.getTemplateManager().addTemplate(template);
        
        if (success) {
            MessageUtils.sendMessage(player, "&aTemplate created successfully!");
            MessageUtils.sendMessage(player, "&7You can now add items to this template.");
            
            // Open the template details to add items
            plugin.getGuiManager().openTemplateManagementMenu(player);
        } else {
            MessageUtils.sendErrorMessage(player, "Failed to create template. Please try again.");
        }
    }
    
    /**
     * Opens the template categories menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player viewing the menu
     */
    public static void openTemplateCategories(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // Get all categories from templates
        Set<String> categories = getCategories(plugin.getTemplateManager());
        
        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Template Categories");
        
        // Add categories to inventory
        int slot = 10;
        for (String category : categories) {
            // Skip if we're out of slots
            if (slot >= 45) break;
            
            // Get templates in this category
            List<ShopTemplate> templatesInCategory = plugin.getTemplateManager().getTemplatesByCategory(category);
            
            // Count admin and player templates
            long adminCount = templatesInCategory.stream().filter(ShopTemplate::isAdminTemplate).count();
            long playerCount = templatesInCategory.size() - adminCount;
            
            // Choose material based on category
            Material material = Material.BOOK;
            switch (category.toLowerCase()) {
                case "tools":
                    material = Material.IRON_PICKAXE;
                    break;
                case "weapons":
                    material = Material.IRON_SWORD;
                    break;
                case "armor":
                    material = Material.IRON_CHESTPLATE;
                    break;
                case "food":
                    material = Material.BREAD;
                    break;
                case "blocks":
                    material = Material.STONE;
                    break;
                case "potions":
                    material = Material.POTION;
                    break;
                case "misc":
                case "miscellaneous":
                    material = Material.CHEST;
                    break;
                case "resources":
                    material = Material.DIAMOND;
                    break;
                default:
                    material = Material.BOOK;
                    break;
            }
            
            // Create category item
            ItemStack categoryItem = guiManager.createGuiItem(
                    material,
                    "&e&l" + category,
                    Arrays.asList(
                            "&7Total Templates: &f" + templatesInCategory.size(),
                            "&7Admin Templates: &f" + adminCount,
                            "&7Player Templates: &f" + playerCount,
                            "",
                            "&eClick to view templates in this category"
                    )
            );
            
            inventory.setItem(slot, categoryItem);
            
            // Increment slot
            slot++;
            
            // Move to next row after 7 categories
            if ((slot - 10) % 7 == 0) {
                slot += 2;
            }
        }
        
        // Back button
        ItemStack backButton = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack",
                Collections.singletonList("&7Return to template management")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Store menu data
        MenuData menuData = new MenuData(MenuType.TEMPLATE_CATEGORIES);
        menuData.setData("categories", new ArrayList<>(categories));
        guiManager.menuData.put(player.getUniqueId(), menuData);
        
        // Open inventory
        player.openInventory(inventory);
    }
    
    /**
     * Opens the shop selection menu for creating templates
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player viewing the menu
     */
    public static void openShopSelectionMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // TODO: Implement shop selection menu for template creation
        player.sendMessage(ChatColor.YELLOW + "Shop selection for template creation is coming soon!");
        openTemplateManagementMenu(guiManager, plugin, player);
    }
    
    /**
     * Opens the backup and restore menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player viewing the menu
     */
    public static void openBackupRestoreMenu(GuiManager guiManager, FrizzlenShop plugin, Player player) {
        // TODO: Implement backup/restore menu
        player.sendMessage(ChatColor.YELLOW + "Backup and restore menu is coming soon!");
        openTemplateManagementMenu(guiManager, plugin, player);
    }
    
    /**
     * Get all unique template categories
     *
     * @param templateManager The template manager
     * @return A set of all unique categories
     */
    private static Set<String> getCategories(TemplateManager templateManager) {
        Set<String> categories = new HashSet<>();
        for (ShopTemplate template : templateManager.getAllTemplates()) {
            if (template.getCategory() != null && !template.getCategory().isEmpty()) {
                categories.add(template.getCategory());
            }
        }
        return categories;
    }
    
    /**
     * Opens a list of templates in a specific category
     * 
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player viewing the menu
     * @param category The category to show
     */
    public static void openTemplatesByCategory(GuiManager guiManager, FrizzlenShop plugin, Player player, String category) {
        // Get templates in the category
        List<ShopTemplate> templates = plugin.getTemplateManager().getTemplatesByCategory(category);
        
        // Create inventory
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.DARK_PURPLE + "Category: " + category);
        
        // Sort templates by name
        templates.sort(Comparator.comparing(ShopTemplate::getName));
        
        // Add templates to inventory
        int slot = 0;
        for (ShopTemplate template : templates) {
            if (slot >= 45) break;
            
            // Choose material based on template type
            Material material = template.isAdminTemplate() ? Material.DIAMOND_BLOCK : Material.GOLD_BLOCK;
            
            // Create template item
            List<String> lore = new ArrayList<>();
            lore.add("&7Description: &f" + template.getDescription());
            lore.add("&7Creator: &f" + template.getCreator());
            lore.add("&7Items: &f" + template.getItems().size());
            lore.add("&7Type: &f" + (template.isAdminTemplate() ? "Admin Shop" : "Player Shop"));
            lore.add("");
            lore.add("&eClick to view template details");
            
            ItemStack templateItem = guiManager.createGuiItem(
                    material,
                    "&b&l" + template.getName(),
                    lore
            );
            
            inventory.setItem(slot, templateItem);
            
            // Increment slot
            slot++;
        }
        
        // Back button
        ItemStack backButton = guiManager.createGuiItem(
                Material.ARROW,
                "&c&lBack",
                Collections.singletonList("&7Return to categories")
        );
        inventory.setItem(49, backButton);
        
        // Fill empty slots
        guiManager.fillEmptySlots(inventory);
        
        // Store menu data
        MenuData menuData = new MenuData(MenuType.TEMPLATE_ITEMS);
        menuData.setData("category", category);
        List<UUID> templateIds = templates.stream().map(ShopTemplate::getId).collect(Collectors.toList());
        menuData.setData("templates", templateIds);
        guiManager.menuData.put(player.getUniqueId(), menuData);
        
        // Open inventory
        player.openInventory(inventory);
    }
    
    /**
     * Convert a slot number to an index in the categories list
     * 
     * @param slot The inventory slot
     * @return The index in the list, or -1 if the slot is not valid
     */
    public static int getIndexFromSlot(int slot) {
        // For slots 10-16, 19-25, 28-34, 37-43
        int row = slot / 9;
        int col = slot % 9;
        
        // Check if slot is in a valid position
        if (col < 1 || col > 7) return -1;
        if (row < 1 || row > 4) return -1;
        
        // Calculate index
        return (row - 1) * 7 + (col - 1);
    }
    
    /**
     * Handle clicks in the template categories menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleTemplateCategoriesClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        if (slot == 49) {
            // Back button
            openTemplateManagementMenu(guiManager, plugin, player);
            return true;
        }
        
        // Check if click was on a category
        @SuppressWarnings("unchecked")
        List<String> categories = (List<String>) menuData.getData("categories");
        
        if (categories != null && slot >= 10 && slot < 45) {
            int index = getIndexFromSlot(slot);
            if (index >= 0 && index < categories.size()) {
                String category = categories.get(index);
                openTemplatesByCategory(guiManager, plugin, player, category);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Handle clicks in the template creation menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleTemplateCreationClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        switch (slot) {
            case 11: // Player shop template
                // Start conversation for player shop template
                player.sendMessage(ChatColor.GREEN + "Creating a player shop template...");
                promptForTemplateDetails(plugin, player, false);
                return true;
                
            case 15: // Admin shop template (if they have permission)
                if (player.hasPermission("frizzlenshop.admin.templates")) {
                    player.sendMessage(ChatColor.GREEN + "Creating an admin shop template...");
                    promptForTemplateDetails(plugin, player, true);
                }
                return true;
                
            case 49: // Back button
                openTemplateManagementMenu(guiManager, plugin, player);
                return true;
                
            default:
                return false;
        }
    }
} 