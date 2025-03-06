package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CreativeCategory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.*;

/**
 * Handles the category menu GUI
 */
public class CategoryMenuHandler {

    // Maximum number of items per page
    private static final int ITEMS_PER_PAGE = 54; // 3 rows of 7 items

    /**
     * Open the category menu for a player
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player to open the menu for
     * @param category The category to show
     * @param page The page number
     */
    public static void openCategoryMenu(GuiManager guiManager, FrizzlenShop plugin, Player player, String category, int page) {
        // Get items in category from all shops
        List<ShopItemData> categoryItems = getCategoryItems(plugin, category);

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) categoryItems.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) {
            totalPages = 1; // At least one page, even if empty
        }

        // Validate page number
        if (page < 1) {
            page = 1;
        } else if (page > totalPages) {
            page = totalPages;
        }

        // Create inventory
        String title = "Shop - " + formatCategoryName(category) + " (Page " + page + "/" + totalPages + ")";
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, title);

        // Calculate start and end indices for this page
        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, categoryItems.size());

        // Add items to inventory
        for (int i = startIndex; i < endIndex; i++) {
            if (i == 45){
                continue;
            }

            ShopItemData itemData = categoryItems.get(i);
            int slot = getSlotFromIndex(i - startIndex);

            // Create item display with price and shop info
            ItemStack displayItem = itemData.getItem().clone();
            // Add lore with price and shop info
            // This would be a method call in a full implementation

            inventory.setItem(slot, displayItem);
        }

        // Add navigation buttons

        // Back button
        ItemStack backButton = guiManager.createGuiItem(Material.ARROW, "&7&lBack",
                Collections.singletonList("&7Return to main menu"));
        inventory.setItem(45, backButton);

        // Previous page button (if not on first page)
        if (page > 1) {
            ItemStack prevButton = guiManager.createGuiItem(Material.PAPER, "&7&lPrevious Page",
                    Collections.singletonList("&7Go to page " + (page - 1)));
            inventory.setItem(48, prevButton);
        }

        // Next page button (if not on last page)
        if (page < totalPages) {
            ItemStack nextButton = guiManager.createGuiItem(Material.PAPER, "&7&lNext Page",
                    Collections.singletonList("&7Go to page " + (page + 1)));
            inventory.setItem(50, nextButton);
        }

        // Fill empty slots
        guiManager.fillEmptySlots(inventory);

        // Open inventory
        player.openInventory(inventory);

        // Store menu data
        Map<String, Object> data = new HashMap<>();
        data.put("category", category);
        data.put("page", page);
        data.put("totalPages", totalPages);
        guiManager.menuData.put(player.getUniqueId(), new MenuData(MenuType.CATEGORY_MENU, data));
    }

    /**
     * Handle a click in the category menu
     *
     * @param guiManager The GUI manager
     * @param plugin The plugin instance
     * @param player The player who clicked
     * @param slot The slot that was clicked
     * @param menuData The menu data
     * @return True if the click was handled, false otherwise
     */
    public static boolean handleClick(GuiManager guiManager, FrizzlenShop plugin, Player player, int slot, MenuData menuData) {
        // Get category and page from menu data
        String category = menuData.getString("category");
        int page = menuData.getInt("page");
        int totalPages = menuData.getInt("totalPages");

        if (category == null) {
            return false;
        }

        // Back button
        if (slot == 45) {
            guiManager.openMainMenu(player);
            return true;
        }

        // Previous page button
        if (slot == 48 && page > 1) {
            guiManager.openCategoryMenu(player, category, page - 1);
            return true;
        }

        // Next page button
        if (slot == 50 && page < totalPages) {
            guiManager.openCategoryMenu(player, category, page + 1);
            return true;
        }

        // Check if the click was on an item slot
        int index = getIndexFromSlot(slot);
        if (index != -1) {
            // Calculate the actual index in the category items list
            int actualIndex = (page - 1) * ITEMS_PER_PAGE + index;

            // Get items in category
            List<ShopItemData> categoryItems = getCategoryItems(plugin, category);

            // Check if the index is valid
            if (actualIndex >= 0 && actualIndex < categoryItems.size()) {
                // Get the item data
                ShopItemData itemData = categoryItems.get(actualIndex);

                // Open the item details menu
                guiManager.openItemDetailsMenu(player, itemData);
                return true;
            }
        }

        return false;
    }

    /**
     * Get items in a category from all shops
     *
     * @param plugin The plugin instance
     * @param category The category to get items for
     * @return A list of shop item data
     */
    private static List<ShopItemData> getCategoryItems(FrizzlenShop plugin, String category) {
        List<ShopItemData> items = new ArrayList<>();

        // Get all shops
        Collection<Shop> shops = plugin.getShopManager().getAllShops();

        // For each shop, collect items that match the category
        for (Shop shop : shops) {
            if (!shop.isOpen()) {
                continue;
            }

            for (ShopItem shopItem : shop.getItems()) {
                // Check if the item belongs to the category
                Material material = shopItem.getItem().getType();

                boolean inCategory;


                CreativeCategory creativeCategory = material.getCreativeCategory();
                Bukkit.broadcastMessage(category);
                Bukkit.broadcastMessage(creativeCategory.name());

                if (category.equalsIgnoreCase("all")) {
                    inCategory = true;

                } else if (category.equalsIgnoreCase("tools")) {
                    inCategory = creativeCategory.equals(CreativeCategory.TOOLS);
                } else if (category.equalsIgnoreCase("weapons")) {
                    inCategory = material.name().contains("SWORD") || material.name().contains("AXE") || material.name().contains("TRIDENT") || material.name().contains("MACE") || material.name().contains("BOW") || material.name().contains("CROSSBOW") || material.name().contains("SHIELD");
                } else if (category.equalsIgnoreCase("armor")) {
                    inCategory = material.name().contains("HELMET") || material.name().contains("CHESTPLATE") || material.name().contains("LEGGINGS") || material.name().contains("BOOTS");
                } else if (category.equalsIgnoreCase("blocks")) {
                    inCategory = creativeCategory.equals(CreativeCategory.BUILDING_BLOCKS);
                } else if (category.equalsIgnoreCase("potions")) {
                    inCategory = creativeCategory.equals(CreativeCategory.BREWING);
                } else if (category.equalsIgnoreCase("miscellaneous")) {
                    inCategory = creativeCategory.equals(CreativeCategory.MISC);
                } else if (category.equalsIgnoreCase("food")) {
                    inCategory = creativeCategory.equals(CreativeCategory.FOOD);
                }else{
                    inCategory = true;
                }

                if (inCategory) {
                    items.add(new ShopItemData(shop, shopItem));
                }
            }
        }

        return items;
    }

    /**
     * Format a category name for display
     *
     * @param category The category name
     * @return The formatted category name
     */
    private static String formatCategoryName(String category) {
        if (category == null || category.isEmpty()) {
            return "Unknown";
        }

        // Capitalize first letter and lowercase the rest
        return category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
    }

    /**
     * Convert an index to a slot in the inventory
     *
     * @param index The index (0-53)
     * @return The slot number
     */
    private static int getSlotFromIndex(int index) {
        if (index < 0 || index >= ITEMS_PER_PAGE) {
            return -1;
        }

        int row = index / 7;
        int col = index % 7;
        return (row * 9 + col + 1) - 1; // +1 to start from the second column
    }

    /**
     * Convert a slot to an index
     *
     * @param slot The slot number
     * @return The index, or -1 if not an item slot
     */
    private static int getIndexFromSlot(int slot) {
        // Check if the slot is in the item area (first 6 rows, columns 1-7)
        int row = (slot - 1) / 9;
        int col = (slot - 1) % 9;

        if (row >= 0 && row <= 5 && col >= 1 && col <= 7) {
            return (row * 7 + (col - 1));
        }

        return -1;
    }
} 