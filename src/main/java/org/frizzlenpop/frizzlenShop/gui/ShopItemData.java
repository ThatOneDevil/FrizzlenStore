package org.frizzlenpop.frizzlenShop.gui;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;

import java.util.UUID;

/**
 * Represents item data for the GUI, containing both the shop and the item
 */
public class ShopItemData {

    private Shop shop;
    private ShopItem shopItem;
    private UUID shopId;
    private String shopName;
    private UUID itemId;
    private ItemStack item;
    private double buyPrice;
    private double sellPrice;
    private int stock;
    private String currency;
    private boolean isAdminShop;
    
    /**
     * Create new shop item data
     *
     * @param shop     The shop
     * @param shopItem The shop item
     */
    public ShopItemData(Shop shop, ShopItem shopItem) {
        this.shop = shop;
        this.shopItem = shopItem;
        this.shopId = shop.getId();
        this.shopName = shop.getName();
        this.itemId = shopItem.getId();
        this.item = shopItem.getItem();
        this.buyPrice = shopItem.getBuyPrice();
        this.sellPrice = shopItem.getSellPrice();
        this.stock = shopItem.getStock();
        this.currency = shopItem.getCurrency();
        this.isAdminShop = shop.isAdminShop();
    }
    
    /**
     * Create new shop item data from individual parameters
     *
     * @param shopId      The shop ID
     * @param shopName    The shop name
     * @param itemId      The item ID
     * @param item        The item
     * @param buyPrice    The buy price
     * @param sellPrice   The sell price
     * @param stock       The stock
     * @param currency    The currency
     * @param isAdminShop Whether the shop is an admin shop
     */
    public ShopItemData(UUID shopId, String shopName, UUID itemId, ItemStack item, double buyPrice, double sellPrice, int stock, String currency, boolean isAdminShop) {
        this.shop = null; // Will be set later if needed
        this.shopItem = null; // Will be set later if needed
        this.shopId = shopId;
        this.shopName = shopName;
        this.itemId = itemId;
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.stock = stock;
        this.currency = currency;
        this.isAdminShop = isAdminShop;
    }
    
    /**
     * Set the shop
     *
     * @param shop The shop
     */
    public void setShop(Shop shop) {
        this.shop = shop;
        if (shop != null) {
            this.shopId = shop.getId();
            this.shopName = shop.getName();
            this.isAdminShop = shop.isAdminShop();
            
            // Try to find the matching shop item
            ShopItem matchingItem = shop.getItem(this.itemId);
            if (matchingItem != null) {
                this.shopItem = matchingItem;
            }
        }
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
        if (shopItem != null) {
            return shopItem.getItem();
        }
        return item;
    }
    
    /**
     * Get the buy price
     *
     * @return The buy price
     */
    public double getBuyPrice() {
        if (shopItem != null) {
            return shopItem.getBuyPrice();
        }
        return buyPrice;
    }
    
    /**
     * Get the sell price
     *
     * @return The sell price
     */
    public double getSellPrice() {
        if (shopItem != null) {
            return shopItem.getSellPrice();
        }
        return sellPrice;
    }
    
    /**
     * Get the currency
     *
     * @return The currency
     */
    public String getCurrency() {
        if (shopItem != null) {
            return shopItem.getCurrency();
        }
        return currency;
    }
    
    /**
     * Get the stock
     *
     * @return The stock
     */
    public int getStock() {
        if (shopItem != null) {
            return shopItem.getStock();
        }
        return stock;
    }
    
    /**
     * Get the shop ID
     *
     * @return The shop ID
     */
    public UUID getShopId() {
        if (shop != null) {
            return shop.getId();
        }
        return shopId;
    }
    
    /**
     * Get the item ID
     *
     * @return The item ID
     */
    public UUID getItemId() {
        if (shopItem != null) {
            return shopItem.getId();
        }
        return itemId;
    }
    
    /**
     * Check if the shop is an admin shop
     *
     * @return True if the shop is an admin shop, false otherwise
     */
    public boolean isAdminShop() {
        if (shop != null) {
            return shop.isAdminShop();
        }
        return isAdminShop;
    }
    
    /**
     * Get the shop name
     *
     * @return The shop name
     */
    public String getShopName() {
        if (shop != null) {
            return shop.getName();
        }
        return shopName;
    }
    
    /**
     * Check if the shop has enough stock
     *
     * @param amount The amount needed
     * @return True if the shop has enough stock, false otherwise
     */
    public boolean hasStock(int amount) {
        if (shop != null && shopItem != null) {
            return shop.hasStock(shopItem.getItem(), amount);
        }
        return stock == -1 || stock >= amount; // -1 means unlimited
    }
    
    /**
     * Calculate the buy price for a specific amount
     *
     * @param amount The amount to buy
     * @return The total price
     */
    public double calculateBuyPrice(int amount) {
        if (shopItem != null) {
            return shopItem.calculateBuyPrice(amount);
        }
        return buyPrice * amount; // Simple multiplication for now
    }
    
    /**
     * Calculate the sell price for a specific amount
     *
     * @param amount The amount to sell
     * @return The total price
     */
    public double calculateSellPrice(int amount) {
        if (shopItem != null) {
            return shopItem.calculateSellPrice(amount);
        }
        return sellPrice * amount; // Simple multiplication for now
    }
} 