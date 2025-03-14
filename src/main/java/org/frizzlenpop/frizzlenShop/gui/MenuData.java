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
    private MenuType previousMenuType; // Track the previous menu type
    
    /**
     * Create a new menu data instance
     *
     * @param menuType The menu type
     */
    public MenuData(MenuType menuType) {
        this.menuType = menuType;
        this.data = new HashMap<>();
        this.previousMenuType = null;
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
        this.previousMenuType = null;
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
        this.previousMenuType = null;
    }
    
    /**
     * Create a new menu data instance with multiple data values
     *
     * @param menuType The menu type
     * @param data The data map
     */
    public MenuData(MenuType menuType, Map<String, Object> data) {
        this.menuType = menuType;
        this.data = new HashMap<>();
        if (data != null) {
            this.data.putAll(data);
        }
        this.previousMenuType = null;
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
        this.previousMenuType = null;
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
        this.previousMenuType = null;
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
     * Get the previous menu type
     *
     * @return The previous menu type, or null if not set
     */
    public MenuType getPreviousMenuType() {
        return previousMenuType;
    }
    
    /**
     * Set the previous menu type
     *
     * @param menuType The previous menu type
     */
    public void setPreviousMenuType(MenuType menuType) {
        this.previousMenuType = menuType;
    }
    
    /**
     * Check if this menu has a previous menu
     *
     * @return True if a previous menu type is set, false otherwise
     */
    public boolean hasPreviousMenu() {
        return previousMenuType != null;
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
     * Check if a data key exists
     *
     * @param key The data key
     * @return True if the key exists, false otherwise
     */
    public boolean hasData(String key) {
        return data.containsKey(key);
    }
    
    /**
     * Get all data
     *
     * @return The data map
     */
    public Map<String, Object> getAllData() {
        return new HashMap<>(data);
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
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    /**
     * Get a double data value
     *
     * @param key The data key
     * @return The data value, or -1 if not found or not a double
     */
    public double getDouble(String key) {
        Object value = data.get(key);
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
    
    /**
     * Get a boolean data value
     *
     * @param key The data key
     * @return The data value, or false if not found or not a boolean
     */
    public boolean getBoolean(String key) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }
    
    /**
     * Get a UUID data value
     *
     * @param key The data key
     * @return The data value, or null if not found or not a UUID
     */
    public UUID getUUID(String key) {
        Object value = data.get(key);
        if (value instanceof UUID) {
            return (UUID) value;
        } else if (value instanceof String) {
            try {
                return UUID.fromString((String) value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
    
    /**
     * Convert this menu data to a string for debugging
     *
     * @return A string representation of this menu data
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MenuData{menuType=").append(menuType);
        sb.append(", data={");
        
        boolean first = true;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            
            sb.append(entry.getKey()).append('=');
            
            Object value = entry.getValue();
            if (value instanceof String) {
                sb.append('"').append(value).append('"');
            } else if (value != null) {
                sb.append(value);
            } else {
                sb.append("null");
            }
        }
        
        sb.append("}}");
        return sb.toString();
    }
} 