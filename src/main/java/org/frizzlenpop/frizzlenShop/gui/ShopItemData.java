package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;

import java.util.UUID;

/**
 * Represents item data for the GUI, containing both the shop and the item
 */
public class ShopItemData {

    private final Shop shop;
    private final ShopItem shopItem;
    
    /**
     * Create new shop item data
     *
     * @param shop     The shop
     * @param shopItem The shop item
     */
    public ShopItemData(Shop shop, ShopItem shopItem) {
        this.shop = shop;
        this.shopItem = shopItem;
    }
    
    /**
     * Get the shop
     *
     * @return The shop
     */
    public Shop getShop() {
        return shop;
    }
    
    /**
     * Get the shop item
     *
     * @return The shop item
     */
    public ShopItem getShopItem() {
        return shopItem;
    }
    
    /**
     * Get the item
     *
     * @return The item
     */
    public ItemStack getItem() {
        return shopItem.getItem();
    }
    
    /**
     * Get the buy price
     *
     * @return The buy price
     */
    public double getBuyPrice() {
        return shopItem.getBuyPrice();
    }
    
    /**
     * Get the sell price
     *
     * @return The sell price
     */
    public double getSellPrice() {
        return shopItem.getSellPrice();
    }
    
    /**
     * Get the currency
     *
     * @return The currency
     */
    public String getCurrency() {
        return shopItem.getCurrency();
    }
    
    /**
     * Get the stock
     *
     * @return The stock
     */
    public int getStock() {
        return shopItem.getStock();
    }
    
    /**
     * Get the shop ID
     *
     * @return The shop ID
     */
    public UUID getShopId() {
        return shop.getId();
    }
    
    /**
     * Get the item ID
     *
     * @return The item ID
     */
    public UUID getItemId() {
        return shopItem.getId();
    }
    
    /**
     * Check if the shop is an admin shop
     *
     * @return True if the shop is an admin shop, false otherwise
     */
    public boolean isAdminShop() {
        return shop.isAdminShop();
    }
    
    /**
     * Get the shop name
     *
     * @return The shop name
     */
    public String getShopName() {
        return shop.getName();
    }
    
    /**
     * Check if the shop has enough stock
     *
     * @param amount The amount needed
     * @return True if the shop has enough stock, false otherwise
     */
    public boolean hasStock(int amount) {
        return shop.hasStock(shopItem.getItem(), amount);
    }
    
    /**
     * Calculate the buy price for a specific amount
     *
     * @param amount The amount to buy
     * @return The total price
     */
    public double calculateBuyPrice(int amount) {
        return shopItem.calculateBuyPrice(amount);
    }
    
    /**
     * Calculate the sell price for a specific amount
     *
     * @param amount The amount to sell
     * @return The total price
     */
    public double calculateSellPrice(int amount) {
        return shopItem.calculateSellPrice(amount);
    }
} 