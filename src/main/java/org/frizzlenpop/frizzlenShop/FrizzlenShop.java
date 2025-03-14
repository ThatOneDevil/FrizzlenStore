package org.frizzlenpop.frizzlenShop;

import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.frizzlenShop.commands.ShopAdminCommand;
import org.frizzlenpop.frizzlenShop.commands.ShopCommand;
import org.frizzlenpop.frizzlenShop.config.ConfigManager;
import org.frizzlenpop.frizzlenShop.data.DataManager;
import org.frizzlenpop.frizzlenShop.economy.CraftingRelationManager;
import org.frizzlenpop.frizzlenShop.economy.DynamicPricingManager;
import org.frizzlenpop.frizzlenShop.economy.EconomyManager;
import org.frizzlenpop.frizzlenShop.economy.MarketAnalyzer;
import org.frizzlenpop.frizzlenShop.gui.GuiManager;
import org.frizzlenpop.frizzlenShop.listeners.ChatListener;
import org.frizzlenpop.frizzlenShop.listeners.InventoryListener;
import org.frizzlenpop.frizzlenShop.listeners.PlayerListener;
import org.frizzlenpop.frizzlenShop.listeners.ShopListener;
import org.frizzlenpop.frizzlenShop.shops.AdminShopPopulator;
import org.frizzlenpop.frizzlenShop.shops.ShopManager;
import org.frizzlenpop.frizzlenShop.templates.TemplateManager;
import org.frizzlenpop.frizzlenShop.utils.DatabaseManager;
import org.frizzlenpop.frizzlenShop.utils.LogManager;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.logging.Level;

public final class FrizzlenShop extends JavaPlugin {

    private static FrizzlenShop instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private EconomyManager economyManager;
    private ShopManager shopManager;
    private GuiManager guiManager;
    private LogManager logManager;
    private ChatListener chatListener;
    private DatabaseManager databaseManager;
    private DynamicPricingManager dynamicPricingManager;
    private CraftingRelationManager craftingRelationManager;
    private AdminShopPopulator adminShopPopulator;
    private TemplateManager templateManager;

    @Override
    public void onEnable() {
        // Set instance for static access
        instance = this;
        
        // Initialize utilities
        MessageUtils.init(this);
        
        // Load configuration
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        // Initialize managers
        logManager = new LogManager(this);
        economyManager = new EconomyManager(this);
        
        // Check if the economy is available
        if (!economyManager.isVaultHooked()) {
            getLogger().severe("No Vault economy plugin found! Disabling plugin...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Initialize database
        databaseManager = new DatabaseManager(this);
        
        dataManager = new DataManager(this);
        shopManager = new ShopManager(this);
        guiManager = new GuiManager(this);
        chatListener = new ChatListener(this);
        
        // Initialize crafting relation manager
        craftingRelationManager = new CraftingRelationManager(this);
        
        // Initialize template manager
        templateManager = new TemplateManager(this);
        
        // Initialize dynamic pricing manager (must be after database is initialized)
        if (configManager.isDynamicPricingEnabled()) {
            getLogger().info("Dynamic pricing is enabled, initializing market system...");
            try {
                dynamicPricingManager = new DynamicPricingManager(this);
                getLogger().info("Dynamic pricing system initialized successfully!");
            } catch (Exception e) {
                getLogger().log(Level.SEVERE, "Failed to initialize dynamic pricing system: " + e.getMessage(), e);
                getLogger().warning("Dynamic pricing will be disabled due to initialization failure.");
                configManager.setDynamicPricingEnabled(false);
                configManager.saveConfig();
            }
        } else {
            getLogger().info("Dynamic pricing is disabled in config. Using static pricing.");
        }
        
        // Register commands
        getCommand("shop").setExecutor(new ShopCommand(this));
        getCommand("shopadmin").setExecutor(new ShopAdminCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopListener(this), this);
        getServer().getPluginManager().registerEvents(chatListener, this);
        
        // Load data
        dataManager.loadData();
        
        // Initialize admin shop with tiered pricing system
        adminShopPopulator = new AdminShopPopulator(this);
        
        // Add debug log about admin shop settings
        getLogger().info("Admin shops enabled: " + configManager.areAdminShopsEnabled());
        getLogger().info("First run: " + configManager.isFirstRun());
        getLogger().info("Force refresh admin shops: " + configManager.isForceAdminShopRefresh());
        
        // Check if we should populate admin shops (either first run or force via config)
        if (configManager.isFirstRun() || configManager.isForceAdminShopRefresh()) {
            getLogger().info("Populating admin shops with tiered pricing...");
            
            // Create the main admin shop with all items
            adminShopPopulator.createMainAdminShop();
            
            // Create category-specific shops for better organization
            adminShopPopulator.createCategoryShop("Tools");
            adminShopPopulator.createCategoryShop("Weapons");
            adminShopPopulator.createCategoryShop("Armor");
            adminShopPopulator.createCategoryShop("Food");
            adminShopPopulator.createCategoryShop("Blocks");
            adminShopPopulator.createCategoryShop("Resources");
            
            // Mark first run as complete
            if (configManager.isFirstRun()) {
                configManager.setFirstRun(false);
                configManager.saveConfig();
            }
            
            // Reset admin shop refresh flag
            if (configManager.isForceAdminShopRefresh()) {
                configManager.setForceAdminShopRefresh(false);
                configManager.saveConfig();
            }
            
            getLogger().info("Admin shops successfully populated!");
        } else {
            getLogger().info("Using existing admin shops, skipping population.");
            // Debug log the count of existing admin shops
            int adminShopCount = getShopManager().getAdminShops().size();
            getLogger().info("Found " + adminShopCount + " existing admin shops in the system.");
        }
        
        getLogger().info("FrizzlenShop has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        // Save templates
        if (templateManager != null) {
            templateManager.saveTemplates();
        }
        
        getLogger().info("FrizzlenShop has been disabled!");
    }
    
    public static FrizzlenShop getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    
    public EconomyManager getEconomyManager() {
        return economyManager;
    }
    
    public ShopManager getShopManager() {
        return shopManager;
    }
    
    public GuiManager getGuiManager() {
        return guiManager;
    }
    
    public LogManager getLogManager() {
        return logManager;
    }

    /**
     * Get the chat listener
     *
     * @return The chat listener
     */
    public ChatListener getChatListener() {
        return chatListener;
    }
    
    /**
     * Get the database manager
     *
     * @return The database manager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    /**
     * Get the dynamic pricing manager
     *
     * @return The dynamic pricing manager, or null if dynamic pricing is disabled
     */
    public DynamicPricingManager getDynamicPricingManager() {
        return dynamicPricingManager;
    }
    
    /**
     * Get the market analyzer from the dynamic pricing manager
     * This is a convenience method to simplify code in other classes
     *
     * @return The market analyzer, or null if dynamic pricing is disabled
     */
    public MarketAnalyzer getMarketAnalyzer() {
        return dynamicPricingManager != null ? dynamicPricingManager.getMarketAnalyzer() : null;
    }
    
    /**
     * Get the crafting relation manager
     *
     * @return The crafting relation manager
     */
    public CraftingRelationManager getCraftingRelationManager() {
        return craftingRelationManager;
    }
    
    /**
     * Get the admin shop populator
     *
     * @return The admin shop populator
     */
    public AdminShopPopulator getAdminShopPopulator() {
        return adminShopPopulator;
    }

    /**
     * Get the template manager
     *
     * @return The template manager
     */
    public TemplateManager getTemplateManager() {
        return templateManager;
    }
}
