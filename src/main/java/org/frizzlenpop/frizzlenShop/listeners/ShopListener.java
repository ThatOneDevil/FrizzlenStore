package org.frizzlenpop.frizzlenShop.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

/**
 * Handles shop-specific events
 */
public class ShopListener implements Listener {

    private final FrizzlenShop plugin;

    /**
     * Creates a new shop listener
     *
     * @param plugin The plugin instance
     */
    public ShopListener(FrizzlenShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        // Check if there's a shop at this location
        Shop shop = plugin.getShopManager().getShopAtLocation(event.getBlock().getLocation());
        if (shop == null) {
            return;
        }
        
        // Check if the player has permission to remove shops
        boolean canRemove = shop.isAdminShop() ? 
                player.hasPermission("frizzlenshop.admin.remove") : 
                (shop.getOwner().equals(player.getUniqueId()) || player.hasPermission("frizzlenshop.admin.remove"));
        
        if (!canRemove) {
            event.setCancelled(true);
            MessageUtils.sendErrorMessage(player, "You don't have permission to remove this shop.");
            return;
        }
        
        // Remove the shop
        if (plugin.getShopManager().deleteShop(shop.getId())) {
            MessageUtils.sendSuccessMessage(player, "Shop '" + shop.getName() + "' has been removed.");
            
            // Log the deletion
            plugin.getLogManager().logShopDeletion(player, shop.getId(), shop.getName());
        } else {
            event.setCancelled(true);
            MessageUtils.sendErrorMessage(player, "Failed to remove the shop. Please try again.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        // Check if there's a shop at this location
        Shop shop = plugin.getShopManager().getShopAtLocation(event.getBlock().getLocation());
        if (shop == null) {
            return;
        }
        
        // Prevent placing blocks at shop locations
        event.setCancelled(true);
        MessageUtils.sendErrorMessage(player, "You cannot place blocks at a shop location.");
    }
} 