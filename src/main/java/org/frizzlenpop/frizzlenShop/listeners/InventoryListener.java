package org.frizzlenpop.frizzlenShop.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.gui.MenuData;

/**
 * Handles inventory-related events for the shop system
 */
public class InventoryListener implements Listener {

    private final FrizzlenShop plugin;

    /**
     * Creates a new inventory listener
     *
     * @param plugin The plugin instance
     */
    public InventoryListener(FrizzlenShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        
        // Check if the player is viewing a shop menu
        MenuData menuData = plugin.getGuiManager().getMenuData(player.getUniqueId());
        if (menuData == null) {
            return;
        }

        // Cancel the event to prevent item movement
        event.setCancelled(true);

        // Handle the click in the GUI manager
        plugin.getGuiManager().handleClick(player, event.getClickedInventory(), event.getSlot());
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        
        // Check if the player is viewing a shop menu
        MenuData menuData = plugin.getGuiManager().getMenuData(player.getUniqueId());
        if (menuData == null) {
            return;
        }

        // Cancel the event to prevent item movement
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        
        // Clear menu data when the player closes the inventory
        plugin.getGuiManager().clearMenuData(player.getUniqueId());
    }
} 