package org.frizzlenpop.frizzlenShop;

import org.bukkit.plugin.java.JavaPlugin;
import org.frizzlenpop.frizzlenShop.commands.ShopAdminCommand;
import org.frizzlenpop.frizzlenShop.commands.ShopCommand;
import org.frizzlenpop.frizzlenShop.config.ConfigManager;
import org.frizzlenpop.frizzlenShop.data.DataManager;
import org.frizzlenpop.frizzlenShop.economy.EconomyManager;
import org.frizzlenpop.frizzlenShop.gui.GuiManager;
import org.frizzlenpop.frizzlenShop.listeners.ChatListener;
import org.frizzlenpop.frizzlenShop.listeners.InventoryListener;
import org.frizzlenpop.frizzlenShop.listeners.PlayerListener;
import org.frizzlenpop.frizzlenShop.listeners.ShopListener;
import org.frizzlenpop.frizzlenShop.shops.ShopManager;
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
        
        dataManager = new DataManager(this);
        shopManager = new ShopManager(this);
        guiManager = new GuiManager(this);
        chatListener = new ChatListener(this);
        
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
        
        getLogger().info("FrizzlenShop has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveData();
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
}
