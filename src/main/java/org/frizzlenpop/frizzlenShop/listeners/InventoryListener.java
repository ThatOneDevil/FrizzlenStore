package org.frizzlenpop.frizzlenShop.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
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

        // Always cancel the event to prevent any item movement/interaction
        event.setCancelled(true);

        // Get the top inventory (the GUI inventory)
        Inventory topInventory = player.getOpenInventory().getTopInventory();
        
        // Additional checks for problematic click types
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY ||
            event.getAction() == InventoryAction.COLLECT_TO_CURSOR ||
            event.isShiftClick() || 
            event.getClick().isKeyboardClick()) {
            // These actions can sometimes bypass cancellation, so log them
            plugin.getLogger().info("Prevented potentially problematic inventory action: " + 
                                   event.getAction() + " by player " + player.getName());
            return;
        }
        
        // Check if the player clicked in the top inventory
        if (event.getClickedInventory() != topInventory) {
            // Player clicked in their own inventory - just cancel but don't process
            return;
        }
        
        // Log debug information for development purposes
        plugin.getLogger().info("Player " + player.getName() + " clicked slot " + event.getSlot() + 
                               " in menu " + menuData.getMenuType().name());

        // Handle the click in the GUI manager
        boolean handled = plugin.getGuiManager().handleClick(player, topInventory, event.getSlot(), event.getClick());
        
        // Log if the click was not handled
        if (!handled) {
            plugin.getLogger().warning("Click in menu " + menuData.getMenuType().name() + 
                                     " at slot " + event.getSlot() + " was not handled!");
        }
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
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        // Cancel any item movement events that involve our GUI inventories
        // This is a fallback in case other cancellations fail
        Inventory source = event.getSource();
        Inventory destination = event.getDestination();
        
        // Check inventory names to see if they're our GUIs
        String sourceName = source.getHolder() != null && source.getHolder() instanceof InventoryView ? 
                           ((InventoryView)source.getHolder()).getTitle() : "";
        String destName = destination.getHolder() != null && destination.getHolder() instanceof InventoryView ? 
                         ((InventoryView)destination.getHolder()).getTitle() : "";
                         
        if (sourceName.contains("FrizzlenShop") || destName.contains("FrizzlenShop")) {
            event.setCancelled(true);
        }
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