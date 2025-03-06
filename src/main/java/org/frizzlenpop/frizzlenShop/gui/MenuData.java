package org.frizzlenpop.frizzlenShop.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores data about what menu a player is viewing and related information
 */
public class MenuData {

    private final MenuType menuType;
    private final Map<String, Object> data;
    
    /**
     * Create a new menu data instance
     *
     * @param menuType The menu type
     */
    public MenuData(MenuType menuType) {
        this.menuType = menuType;
        this.data = new HashMap<>();
    }
    
    /**
     * Create a new menu data instance with a UUID data value
     *
     * @param menuType The menu type
     * @param id A UUID to store (usually a shop ID)
     */
    public MenuData(MenuType menuType, UUID id) {
        this.menuType = menuType;
        this.data = new HashMap<>();
        this.data.put("id", id);
    }
    
    /**
     * Create a new menu data instance with a string data value
     *
     * @param menuType The menu type
     * @param key The data key
     * @param value The data value
     */
    public MenuData(MenuType menuType, String key, Object value) {
        this.menuType = menuType;
        this.data = new HashMap<>();
        this.data.put(key, value);
    }
    
    /**
     * Create a new menu data instance with multiple data values
     *
     * @param menuType The menu type
     * @param data The data map
     */
    public MenuData(MenuType menuType, Map<String, Object> data) {
        this.menuType = menuType;
        this.data = new HashMap<>(data);
    }
    
    /**
     * Create a new menu data instance with a shop ID and an item ID
     *
     * @param menuType The menu type
     * @param shopId The shop ID
     * @param itemId The item ID
     */
    public MenuData(MenuType menuType, UUID shopId, UUID itemId) {
        this.menuType = menuType;
        this.data = new HashMap<>();
        this.data.put("shopId", shopId);
        this.data.put("itemId", itemId);
    }
    
    /**
     * Create a new menu data instance with a ShopItemData value
     *
     * @param menuType The menu type
     * @param shopItemData The shop item data
     */
    public MenuData(MenuType menuType, ShopItemData shopItemData) {
        this.menuType = menuType;
        this.data = new HashMap<>();
        this.data.put("value", shopItemData);
    }
    
    /**
     * Get the menu type
     *
     * @return The menu type
     */
    public MenuType getMenuType() {
        return menuType;
    }
    
    /**
     * Get a data value
     *
     * @param key The data key
     * @return The data value, or null if not found
     */
    public Object getData(String key) {
        return data.get(key);
    }
    
    /**
     * Get a string data value
     *
     * @param key The data key
     * @return The data value, or null if not found
     */
    public String getString(String key) {
        Object value = data.get(key);
        return value instanceof String ? (String) value : null;
    }
    
    /**
     * Get an integer data value
     *
     * @param key The data key
     * @return The data value, or -1 if not found or not an integer
     */
    public int getInt(String key) {
        Object value = data.get(key);
        return value instanceof Integer ? (Integer) value : -1;
    }
    
    /**
     * Get a double data value
     *
     * @param key The data key
     * @return The data value, or -1 if not found or not a double
     */
    public double getDouble(String key) {
        Object value = data.get(key);
        return value instanceof Double ? (Double) value : -1;
    }
    
    /**
     * Get a boolean data value
     *
     * @param key The data key
     * @return The data value, or false if not found or not a boolean
     */
    public boolean getBoolean(String key) {
        Object value = data.get(key);
        return value instanceof Boolean && (Boolean) value;
    }
    
    /**
     * Get a UUID data value
     *
     * @param key The data key
     * @return The data value, or null if not found or not a UUID
     */
    public UUID getUUID(String key) {
        Object value = data.get(key);
        return value instanceof UUID ? (UUID) value : null;
    }
    
    /**
     * Set a data value
     *
     * @param key The data key
     * @param value The data value
     */
    public void setData(String key, Object value) {
        data.put(key, value);
    }
    
    /**
     * Get all data
     *
     * @return The data map
     */
    public Map<String, Object> getAllData() {
        return new HashMap<>(data);
    }
} 