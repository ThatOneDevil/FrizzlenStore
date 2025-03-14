package org.frizzlenpop.frizzlenShop.templates;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.shops.Shop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages shop templates
 */
public class TemplateManager {
    
    private final FrizzlenShop plugin;
    private final Map<UUID, ShopTemplate> templates;
    private final File templateFile;
    private FileConfiguration templateConfig;
    
    /**
     * Creates a new template manager
     *
     * @param plugin The plugin instance
     */
    public TemplateManager(FrizzlenShop plugin) {
        this.plugin = plugin;
        this.templates = new HashMap<>();
        this.templateFile = new File(plugin.getDataFolder(), "templates.yml");
        
        // Create the templates.yml file if it doesn't exist
        if (!templateFile.exists()) {
            try {
                templateFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to create templates.yml", e);
            }
        }
        
        // Load the templates.yml file
        templateConfig = YamlConfiguration.loadConfiguration(templateFile);
        
        // Load templates
        loadTemplates();
    }
    
    /**
     * Load all templates from the templates.yml file
     */
    private void loadTemplates() {
        templates.clear();
        
        ConfigurationSection templatesSection = templateConfig.getConfigurationSection("templates");
        if (templatesSection == null) {
            return;
        }
        
        for (String key : templatesSection.getKeys(false)) {
            ConfigurationSection templateSection = templatesSection.getConfigurationSection(key);
            if (templateSection != null) {
                ShopTemplate template = ShopTemplate.loadFromConfig(templateSection);
                if (template != null) {
                    templates.put(template.getId(), template);
                    plugin.getLogger().info("Loaded template: " + template.getName());
                }
            }
        }
        
        plugin.getLogger().info("Loaded " + templates.size() + " shop templates");
    }
    
    /**
     * Save all templates to the templates.yml file
     */
    public void saveTemplates() {
        // Create templates section
        ConfigurationSection templatesSection = templateConfig.createSection("templates");
        
        // Save each template
        for (ShopTemplate template : templates.values()) {
            ConfigurationSection templateSection = templatesSection.createSection(template.getId().toString());
            template.saveToConfig(templateSection);
        }
        
        // Save the file
        try {
            templateConfig.save(templateFile);
            plugin.getLogger().info("Saved " + templates.size() + " shop templates");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save templates", e);
        }
    }
    
    /**
     * Get a template by its ID
     *
     * @param id The template ID
     * @return The template, or null if not found
     */
    public ShopTemplate getTemplate(UUID id) {
        return templates.get(id);
    }
    
    /**
     * Get all templates
     *
     * @return A collection of all templates
     */
    public Collection<ShopTemplate> getAllTemplates() {
        return new ArrayList<>(templates.values());
    }
    
    /**
     * Get all templates for admin shops
     *
     * @return A list of admin shop templates
     */
    public List<ShopTemplate> getAdminTemplates() {
        return templates.values().stream()
                .filter(ShopTemplate::isAdminTemplate)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all templates for player shops
     *
     * @return A list of player shop templates
     */
    public List<ShopTemplate> getPlayerTemplates() {
        return templates.values().stream()
                .filter(template -> !template.isAdminTemplate())
                .collect(Collectors.toList());
    }
    
    /**
     * Get templates by category
     *
     * @param category The category
     * @return A list of templates in the category
     */
    public List<ShopTemplate> getTemplatesByCategory(String category) {
        return templates.values().stream()
                .filter(template -> template.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
    
    /**
     * Get templates by creator
     *
     * @param creator The creator's name
     * @return A list of templates by the creator
     */
    public List<ShopTemplate> getTemplatesByCreator(String creator) {
        return templates.values().stream()
                .filter(template -> template.getCreator().equalsIgnoreCase(creator))
                .collect(Collectors.toList());
    }
    
    /**
     * Create a template from an existing shop
     *
     * @param shop The shop to create a template from
     * @param name The name for the template
     * @param description The description for the template
     * @param creator The creator's name
     * @return The created template
     */
    public ShopTemplate createTemplateFromShop(Shop shop, String name, String description, String creator) {
        ShopTemplate template = ShopTemplate.fromShop(shop, name, description, creator);
        
        // Add to templates
        templates.put(template.getId(), template);
        
        // Save templates
        saveTemplates();
        
        return template;
    }
    
    /**
     * Add a template
     *
     * @param template The template to add
     * @return True if added, false if already exists
     */
    public boolean addTemplate(ShopTemplate template) {
        if (templates.containsKey(template.getId())) {
            return false;
        }
        
        templates.put(template.getId(), template);
        saveTemplates();
        return true;
    }
    
    /**
     * Remove a template
     *
     * @param id The ID of the template to remove
     * @return True if removed, false if not found
     */
    public boolean removeTemplate(UUID id) {
        if (templates.remove(id) != null) {
            saveTemplates();
            return true;
        }
        
        return false;
    }
    
    /**
     * Update a template
     *
     * @param template The template to update
     * @return True if updated, false if not found
     */
    public boolean updateTemplate(ShopTemplate template) {
        if (!templates.containsKey(template.getId())) {
            return false;
        }
        
        templates.put(template.getId(), template);
        template.incrementVersion();
        saveTemplates();
        return true;
    }
    
    /**
     * Create a shop from a template
     *
     * @param templateId The template ID
     * @param shopName The name for the new shop
     * @param playerUuid The UUID of the shop owner (null for admin shops)
     * @param location The location for the shop
     * @return The created shop, or null if creation failed
     */
    public Shop createShopFromTemplate(UUID templateId, String shopName, UUID playerUuid, org.bukkit.Location location) {
        ShopTemplate template = getTemplate(templateId);
        if (template == null) {
            return null;
        }
        
        return template.createShop(plugin, shopName, playerUuid, location);
    }
} 