package org.frizzlenpop.frizzlenShop.utils;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for working with item lore
 */
public class ItemLoreUtils {

    /**
     * Get the lore from an item
     *
     * @param item The item to get lore from
     * @return The lore, or null if the item has no lore
     */
    public static List<String> getLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            return null;
        }
        
        return meta.getLore();
    }
    
    /**
     * Set the lore for an item
     *
     * @param item The item to set lore for
     * @param lore The lore to set
     */
    public static void setLore(ItemStack item, List<String> lore) {
        if (item == null) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        
        meta.setLore(lore);
        item.setItemMeta(meta);
    }
    
    /**
     * Add lines to the existing lore of an item
     *
     * @param item The item to add lore to
     * @param lines The lines to add
     */
    public static void addLore(ItemStack item, List<String> lines) {
        if (item == null || lines == null || lines.isEmpty()) {
            return;
        }
        
        List<String> lore = getLore(item);
        if (lore == null) {
            lore = new ArrayList<>();
        }
        
        lore.addAll(lines);
        setLore(item, lore);
    }
    
    /**
     * Add a single line to the existing lore of an item
     *
     * @param item The item to add lore to
     * @param line The line to add
     */
    public static void addLore(ItemStack item, String line) {
        if (item == null || line == null) {
            return;
        }
        
        List<String> lore = getLore(item);
        if (lore == null) {
            lore = new ArrayList<>();
        }
        
        lore.add(line);
        setLore(item, lore);
    }
    
    /**
     * Clear the lore from an item
     *
     * @param item The item to clear lore from
     */
    public static void clearLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        
        meta.setLore(new ArrayList<>());
        item.setItemMeta(meta);
    }
} 