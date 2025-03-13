package org.frizzlenpop.frizzlenShop.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for GUI-related operations
 */
public class GuiUtils {

    /**
     * Creates an item with the specified material, name, and lore for information display
     *
     * @param material The material of the item
     * @param name The name of the item
     * @param lore The lore of the item
     * @return The created ItemStack
     */
    public static ItemStack createInfoItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            
            meta.setLore(coloredLore);
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * Creates an action item with the specified material and name
     *
     * @param material The material of the item
     * @param name The name of the item
     * @return The created ItemStack
     */
    public static ItemStack createActionItem(Material material, String name) {
        return createInfoItem(material, name, new ArrayList<>());
    }
    
    /**
     * Fills all empty slots in an inventory with glass panes
     *
     * @param inventory The inventory to fill
     */
    public static void fillEmptySlots(Inventory inventory) {
        ItemStack filler = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(" ");
            filler.setItemMeta(meta);
        }
        
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }
} 