package org.frizzlenpop.frizzlenShop.templates;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.AdminShop;
import org.frizzlenpop.frizzlenShop.shops.PlayerShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;
import org.frizzlenpop.frizzlenShop.shops.ShopItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a shop template that can be used to create new shops
 */
public class ShopTemplate {
    
    private final UUID id;
    private String name;
    private String description;
    private final boolean isAdminTemplate;
    private final List<TemplateItem> items;
    private final Map<String, String> metadata;
    private final long creationTime;
    private String creator;
    private String category;
    private int version;
    
    /**
     * Creates a new shop template
     *
     * @param name The name of the template
     * @param description The description of the template
     * @param isAdminTemplate Whether this is a template for admin shops
     * @param creator The name of the creator
     */
    public ShopTemplate(String name, String description, boolean isAdminTemplate, String creator) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.isAdminTemplate = isAdminTemplate;
        this.items = new ArrayList<>();
        this.metadata = new HashMap<>();
        this.creationTime = System.currentTimeMillis();
        this.creator = creator;
        this.category = "General";
        this.version = 1;
    }
    
    /**
     * Creates a shop template from an existing shop
     *
     * @param shop The shop to create the template from
     * @param name The name of the template
     * @param description The description of the template
     * @param creator The name of the creator
     * @return The created template
     */
    public static ShopTemplate fromShop(Shop shop, String name, String description, String creator) {
        ShopTemplate template = new ShopTemplate(name, description, shop.isAdminShop(), creator);
        
        // Add all items from the shop
        for (ShopItem shopItem : shop.getItems()) {
            template.addItem(
                shopItem.getItem(),
                shopItem.getBuyPrice(),
                shopItem.getSellPrice(),
                shopItem.getCurrency(),
                shopItem.getStock()
            );
        }
        
        return template;
    }
    
    /**
     * Get the template ID
     *
     * @return The template ID
     */
    public UUID getId() {
        return id;
    }
    
    /**
     * Get the template name
     *
     * @return The template name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the template name
     *
     * @param name The new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Get the template description
     *
     * @return The template description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the template description
     *
     * @param description The new description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Check if this is a template for admin shops
     *
     * @return True if this is for admin shops, false for player shops
     */
    public boolean isAdminTemplate() {
        return isAdminTemplate;
    }
    
    /**
     * Get the items in this template
     *
     * @return A list of template items
     */
    public List<TemplateItem> getItems() {
        return new ArrayList<>(items);
    }
    
    /**
     * Add an item to the template
     *
     * @param item The item to add
     * @param buyPrice The buy price
     * @param sellPrice The sell price
     * @param currency The currency
     * @param stock The stock (-1 for unlimited)
     * @return True if the item was added, false if already exists
     */
    public boolean addItem(ItemStack item, double buyPrice, double sellPrice, String currency, int stock) {
        // Check if item already exists
        for (TemplateItem templateItem : items) {
            if (templateItem.getItem().isSimilar(item)) {
                return false;
            }
        }
        
        // Add the item
        items.add(new TemplateItem(item, buyPrice, sellPrice, currency, stock));
        return true;
    }
    
    /**
     * Remove an item from the template
     *
     * @param index The index of the item to remove
     * @return True if the item was removed, false if index is invalid
     */
    public boolean removeItem(int index) {
        if (index < 0 || index >= items.size()) {
            return false;
        }
        
        items.remove(index);
        return true;
    }
    
    /**
     * Get the metadata for this template
     *
     * @return The metadata map
     */
    public Map<String, String> getMetadata() {
        return new HashMap<>(metadata);
    }
    
    /**
     * Add metadata to the template
     *
     * @param key The metadata key
     * @param value The metadata value
     */
    public void addMetadata(String key, String value) {
        metadata.put(key, value);
    }
    
    /**
     * Get a metadata value
     *
     * @param key The key
     * @return The value, or null if not found
     */
    public String getMetadata(String key) {
        return metadata.get(key);
    }
    
    /**
     * Get the creation time of this template
     *
     * @return The creation time
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Get the creator of this template
     *
     * @return The creator name
     */
    public String getCreator() {
        return creator;
    }
    
    /**
     * Set the creator of this template
     *
     * @param creator The creator name
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }
    
    /**
     * Get the category of this template
     *
     * @return The category
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * Set the category of this template
     *
     * @param category The new category
     */
    public void setCategory(String category) {
        this.category = category;
    }
    
    /**
     * Get the version of this template
     *
     * @return The version
     */
    public int getVersion() {
        return version;
    }
    
    /**
     * Increment the version of this template
     *
     * @return The new version
     */
    public int incrementVersion() {
        return ++version;
    }
    
    /**
     * Create a shop from this template
     *
     * @param plugin The plugin instance
     * @param shopName The name for the new shop
     * @param playerUuid The UUID of the shop owner (null for admin shops)
     * @param location The location for the shop
     * @return The created shop, or null if creation failed
     */
    public Shop createShop(FrizzlenShop plugin, String shopName, UUID playerUuid, org.bukkit.Location location) {
        Shop shop;
        
        if (isAdminTemplate) {
            // Create admin shop
            shop = plugin.getShopManager().createAdminShop(shopName, location);
        } else {
            // Create player shop
            shop = plugin.getShopManager().createPlayerShop(shopName, playerUuid, location);
        }
        
        if (shop == null) {
            return null;
        }
        
        // Add all items from the template
        for (TemplateItem item : items) {
            shop.addItem(
                item.getItem(),
                item.getBuyPrice(),
                item.getSellPrice(),
                item.getCurrency(),
                item.getStock()
            );
        }
        
        // Set shop description if available
        if (shop instanceof AdminShop) {
            ((AdminShop) shop).setDescription(description);
        } else if (shop instanceof PlayerShop) {
            ((PlayerShop) shop).setDescription(description);
        }
        
        return shop;
    }
    
    /**
     * Save this template to a configuration section
     *
     * @param section The configuration section to save to
     */
    public void saveToConfig(ConfigurationSection section) {
        section.set("id", id.toString());
        section.set("name", name);
        section.set("description", description);
        section.set("isAdminTemplate", isAdminTemplate);
        section.set("creationTime", creationTime);
        section.set("creator", creator);
        section.set("category", category);
        section.set("version", version);
        
        // Save metadata
        ConfigurationSection metadataSection = section.createSection("metadata");
        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            metadataSection.set(entry.getKey(), entry.getValue());
        }
        
        // Save items
        ConfigurationSection itemsSection = section.createSection("items");
        for (int i = 0; i < items.size(); i++) {
            TemplateItem item = items.get(i);
            ConfigurationSection itemSection = itemsSection.createSection(String.valueOf(i));
            itemSection.set("item", item.getItem());
            itemSection.set("buyPrice", item.getBuyPrice());
            itemSection.set("sellPrice", item.getSellPrice());
            itemSection.set("currency", item.getCurrency());
            itemSection.set("stock", item.getStock());
        }
    }
    
    /**
     * Load a template from a configuration section
     *
     * @param section The configuration section to load from
     * @return The loaded template, or null if loading failed
     */
    public static ShopTemplate loadFromConfig(ConfigurationSection section) {
        try {
            UUID id = UUID.fromString(section.getString("id"));
            String name = section.getString("name");
            String description = section.getString("description");
            boolean isAdminTemplate = section.getBoolean("isAdminTemplate");
            long creationTime = section.getLong("creationTime");
            String creator = section.getString("creator");
            String category = section.getString("category", "General");
            int version = section.getInt("version", 1);
            
            // Create a new template
            ShopTemplate template = new ShopTemplate(name, description, isAdminTemplate, creator);
            
            // Set ID and other properties
            try {
                java.lang.reflect.Field idField = ShopTemplate.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(template, id);
                
                java.lang.reflect.Field creationTimeField = ShopTemplate.class.getDeclaredField("creationTime");
                creationTimeField.setAccessible(true);
                creationTimeField.set(template, creationTime);
                
                template.setCategory(category);
                
                java.lang.reflect.Field versionField = ShopTemplate.class.getDeclaredField("version");
                versionField.setAccessible(true);
                versionField.set(template, version);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            
            // Load metadata
            ConfigurationSection metadataSection = section.getConfigurationSection("metadata");
            if (metadataSection != null) {
                for (String key : metadataSection.getKeys(false)) {
                    template.addMetadata(key, metadataSection.getString(key));
                }
            }
            
            // Load items
            ConfigurationSection itemsSection = section.getConfigurationSection("items");
            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                    ItemStack item = itemSection.getItemStack("item");
                    double buyPrice = itemSection.getDouble("buyPrice");
                    double sellPrice = itemSection.getDouble("sellPrice");
                    String currency = itemSection.getString("currency");
                    int stock = itemSection.getInt("stock");
                    
                    template.addItem(item, buyPrice, sellPrice, currency, stock);
                }
            }
            
            return template;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 