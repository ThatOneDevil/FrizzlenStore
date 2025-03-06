package org.frizzlenpop.frizzlenShop.economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;
import org.frizzlenpop.frizzlenShop.utils.MessageUtils;

import java.util.UUID;
import java.util.logging.Level;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;

/**
 * Handles integration with the FrizzlenEco economy plugin
 */
public class EconomyManager {

    private final FrizzlenShop plugin;
    private Object frizzlenEcoPlugin; // Will be cast to the actual plugin type once we have the API

    public EconomyManager(FrizzlenShop plugin) {
        this.plugin = plugin;
        setupEconomy();
    }

    /**
     * Setup the economy integration
     */
    private void setupEconomy() {
        if (!plugin.isFrizzlenEcoHooked()) {
            plugin.getLogger().severe("FrizzlenEco is not hooked, economy functions will not work!");
            return;
        }

        frizzlenEcoPlugin = Bukkit.getPluginManager().getPlugin("FrizzlenEco");
        if (frizzlenEcoPlugin == null) {
            plugin.getLogger().severe("Failed to get FrizzlenEco plugin instance!");
            return;
        }

        plugin.getLogger().info("Economy integration setup successfully!");
    }

    /**
     * Check if a player has enough funds
     *
     * @param playerUuid The UUID of the player
     * @param amount     The amount to check
     * @param currency   The currency to check
     * @return True if the player has enough funds, false otherwise
     */
    public boolean has(UUID playerUuid, double amount, String currency) {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return false;
        }

        try {
            // Use a safer approach with proper error handling
            Object economyManager = null;
            try {
                economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not access EconomyManager from FrizzlenEco. Using fallback behavior.");
                return getBalance(playerUuid, currency) >= amount;
            }
            
            if (economyManager == null) {
                return getBalance(playerUuid, currency) >= amount;
            }
            
            // Check for the presence of the has method
            Method hasMethod = null;
            try {
                hasMethod = economyManager.getClass().getMethod("has", UUID.class, double.class, String.class);
            } catch (NoSuchMethodException e) {
                plugin.getLogger().warning("has() method not found in FrizzlenEco. Using getBalance instead.");
                return getBalance(playerUuid, currency) >= amount;
            }
            
            // Call the has method with proper exception handling
            try {
                Object result = hasMethod.invoke(economyManager, playerUuid, amount, currency);
                if (result instanceof Boolean) {
                    return (Boolean) result;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error invoking has() method: " + e.getMessage());
            }
            
            // Fallback to checking balance
            return getBalance(playerUuid, currency) >= amount;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking balance", e);
            return false;
        }
    }

    /**
     * Get the balance of a player
     *
     * @param playerUuid The UUID of the player
     * @param currency   The currency to check
     * @return The player's balance
     */
    public double getBalance(UUID playerUuid, String currency) {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return 0;
        }

        // For testing and debugging, we can return a fixed amount
        // This allows testing shop functionality without requiring FrizzlenEco to work
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test balance for player " + playerUuid);
            return 10000.0; // High test balance for easy testing
        }

        try {
            // Use a safer approach with proper error handling
            Object economyManager = null;
            try {
                economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not access EconomyManager from FrizzlenEco: " + e.getMessage());
                return 0;
            }
            
            if (economyManager == null) {
                return 0;
            }
            
            // Check for the presence of the getBalance method
            Method getBalanceMethod = null;
            try {
                getBalanceMethod = economyManager.getClass().getMethod("getBalance", UUID.class, String.class);
            } catch (NoSuchMethodException e) {
                // Try alternative method names
                try {
                    getBalanceMethod = economyManager.getClass().getMethod("getUserBalance", UUID.class, String.class);
                } catch (NoSuchMethodException e2) {
                    try {
                        getBalanceMethod = economyManager.getClass().getMethod("getPlayerBalance", UUID.class, String.class);
                    } catch (NoSuchMethodException e3) {
                        plugin.getLogger().warning("Balance methods not found in FrizzlenEco. Using default balance 0.");
                        // Log the available methods for debugging
                        try {
                            plugin.getLogger().info("Available methods in EconomyManager:");
                            for (Method method : economyManager.getClass().getMethods()) {
                                plugin.getLogger().info("  " + method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")");
                            }
                        } catch (Exception ignore) {}
                        return 0;
                    }
                }
            }
            
            // Call the getBalance method with proper exception handling
            try {
                Object result = getBalanceMethod.invoke(economyManager, playerUuid, currency);
                if (result instanceof Double) {
                    return (Double) result;
                } else if (result instanceof Number) {
                    return ((Number) result).doubleValue();
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error invoking getBalance() method: " + e.getMessage());
            }
            
            return 0;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error retrieving balance", e);
            return 0;
        }
    }

    /**
     * Transfer funds from one player to another
     *
     * @param fromUuid The UUID of the player to transfer from
     * @param toUuid   The UUID of the player to transfer to
     * @param amount   The amount to transfer
     * @param currency The currency to use
     * @return True if the transfer was successful, false otherwise
     */
    public boolean transfer(UUID fromUuid, UUID toUuid, double amount, String currency) {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return false;
        }
        
        // For testing mode, all transactions succeed
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("TEST MODE: Transferring " + formatCurrency(amount, currency) + 
                    " from " + fromUuid + " to " + toUuid);
            return true;
        }
        
        // First check if the sender has enough money
        if (!has(fromUuid, amount, currency)) {
            return false;
        }

        try {
            // Get EconomyManager from FrizzlenEco
            Object economyManager;
            try {
                economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
                if (economyManager == null) {
                    plugin.getLogger().warning("Failed to get EconomyManager from FrizzlenEco");
                    return simulateTransfer(fromUuid, toUuid, amount, currency);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Exception getting EconomyManager from FrizzlenEco: " + e.getMessage());
                return simulateTransfer(fromUuid, toUuid, amount, currency);
            }
            
            // Try to find transfer method
            Method transferMethod;
            try {
                transferMethod = economyManager.getClass().getMethod("transfer", UUID.class, UUID.class, double.class, String.class);
            } catch (NoSuchMethodException e) {
                plugin.getLogger().warning("transfer() method not found in FrizzlenEco. Using fallback mechanism.");
                return simulateTransfer(fromUuid, toUuid, amount, currency);
            }
            
            // Try to invoke transfer method
            try {
                Object result = transferMethod.invoke(economyManager, fromUuid, toUuid, amount, currency);
                return result instanceof Boolean && (Boolean) result;
            } catch (Exception e) {
                plugin.getLogger().warning("Error invoking transfer() method: " + e.getMessage());
                return simulateTransfer(fromUuid, toUuid, amount, currency);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error transferring funds", e);
            return false;
        }
    }
    
    /**
     * Simulate a transfer by using withdraw and deposit
     * 
     * @param fromUuid The UUID of the player to transfer from
     * @param toUuid   The UUID of the player to transfer to
     * @param amount   The amount to transfer
     * @param currency The currency to use
     * @return True if the simulated transfer was successful
     */
    private boolean simulateTransfer(UUID fromUuid, UUID toUuid, double amount, String currency) {
        plugin.getLogger().info("Simulating transfer using withdraw/deposit");
        if (withdraw(fromUuid, amount, currency)) {
            if (deposit(toUuid, amount, currency)) {
                return true;
            } else {
                // Failed to deposit, refund the withdrawal
                deposit(fromUuid, amount, currency);
                return false;
            }
        }
        return false;
    }

    /**
     * Withdraw funds from a player
     *
     * @param playerUuid The UUID of the player
     * @param amount     The amount to withdraw
     * @param currency   The currency to use
     * @return True if the withdrawal was successful, false otherwise
     */
    public boolean withdraw(UUID playerUuid, double amount, String currency) {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return false;
        }
        
        // For testing mode, all transactions succeed
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("TEST MODE: Withdrawing " + formatCurrency(amount, currency) + 
                    " from " + playerUuid);
            return true;
        }
        
        // First check if the player has enough money
        if (!has(playerUuid, amount, currency)) {
            return false;
        }

        try {
            // Get EconomyManager from FrizzlenEco
            Object economyManager;
            try {
                economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
                if (economyManager == null) {
                    plugin.getLogger().warning("Failed to get EconomyManager from FrizzlenEco");
                    return false;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Exception getting EconomyManager from FrizzlenEco: " + e.getMessage());
                return false;
            }
            
            // Try to find withdraw method
            Method withdrawMethod;
            try {
                withdrawMethod = economyManager.getClass().getMethod("withdraw", UUID.class, double.class, String.class);
            } catch (NoSuchMethodException e) {
                // Try alternate method names
                try {
                    withdrawMethod = economyManager.getClass().getMethod("withdrawMoney", UUID.class, double.class, String.class);
                } catch (NoSuchMethodException ex) {
                    try {
                        withdrawMethod = economyManager.getClass().getMethod("removeMoney", UUID.class, double.class, String.class);
                    } catch (NoSuchMethodException ex2) {
                        plugin.getLogger().warning("No suitable withdraw method found in FrizzlenEco");
                        return false;
                    }
                }
            }
            
            // Try to invoke withdraw method
            try {
                Object result = withdrawMethod.invoke(economyManager, playerUuid, amount, currency);
                return result instanceof Boolean && (Boolean) result;
            } catch (Exception e) {
                plugin.getLogger().warning("Error invoking withdraw method: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error withdrawing funds", e);
            return false;
        }
    }

    /**
     * Deposit money to a player
     *
     * @param playerUuid The UUID of the player
     * @param amount     The amount to deposit
     * @param currency   The currency to use
     * @return True if the deposit was successful, false otherwise
     */
    public boolean deposit(UUID playerUuid, double amount, String currency) {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return false;
        }
        
        // For testing mode, all transactions succeed
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("TEST MODE: Depositing " + formatCurrency(amount, currency) + 
                    " to " + playerUuid);
            return true;
        }

        try {
            // Get EconomyManager from FrizzlenEco
            Object economyManager;
            try {
                economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
                if (economyManager == null) {
                    plugin.getLogger().warning("Failed to get EconomyManager from FrizzlenEco");
                    return false;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Exception getting EconomyManager from FrizzlenEco: " + e.getMessage());
                return false;
            }
            
            // Try to find deposit method
            Method depositMethod;
            try {
                depositMethod = economyManager.getClass().getMethod("deposit", UUID.class, double.class, String.class);
            } catch (NoSuchMethodException e) {
                // Try alternate method names
                try {
                    depositMethod = economyManager.getClass().getMethod("depositMoney", UUID.class, double.class, String.class);
                } catch (NoSuchMethodException ex) {
                    try {
                        depositMethod = economyManager.getClass().getMethod("addMoney", UUID.class, double.class, String.class);
                    } catch (NoSuchMethodException ex2) {
                        plugin.getLogger().warning("No suitable deposit method found in FrizzlenEco");
                        return false;
                    }
                }
            }
            
            // Try to invoke deposit method
            try {
                Object result = depositMethod.invoke(economyManager, playerUuid, amount, currency);
                return result instanceof Boolean && (Boolean) result;
            } catch (Exception e) {
                plugin.getLogger().warning("Error invoking deposit method: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error depositing funds", e);
            return false;
        }
    }

    /**
     * Create an account for a shop
     *
     * @param shopUuid  The UUID of the shop
     * @param shopName  The name of the shop
     * @param currency  The default currency for the shop
     * @return True if the account was created successfully, false otherwise
     */
    public boolean createShopAccount(UUID shopUuid, String shopName, String currency) {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return false;
        }
        
        // For testing mode, all operations succeed
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("TEST MODE: Creating shop account for " + shopName + 
                    " (" + shopUuid + ") with currency " + currency);
            return true;
        }

        try {
            // Get EconomyManager from FrizzlenEco
            Object economyManager;
            try {
                economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
                if (economyManager == null) {
                    plugin.getLogger().warning("Failed to get EconomyManager from FrizzlenEco");
                    return false;
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Exception getting EconomyManager from FrizzlenEco: " + e.getMessage());
                return false;
            }
            
            // Try to find createAccount method
            Method createAccountMethod;
            try {
                createAccountMethod = economyManager.getClass().getMethod("createAccount", UUID.class, String.class, String.class);
            } catch (NoSuchMethodException e) {
                // Try alternate method names
                try {
                    createAccountMethod = economyManager.getClass().getMethod("createBankAccount", UUID.class, String.class, String.class);
                } catch (NoSuchMethodException ex) {
                    try {
                        createAccountMethod = economyManager.getClass().getMethod("registerAccount", UUID.class, String.class, String.class);
                    } catch (NoSuchMethodException ex2) {
                        plugin.getLogger().warning("No suitable createAccount method found in FrizzlenEco");
                        // For shop accounts, we can just log this but proceed without creating an account
                        plugin.getLogger().info("Shop will operate without a dedicated economy account");
                        return true;
                    }
                }
            }
            
            // Try to invoke createAccount method
            try {
                Object result = createAccountMethod.invoke(economyManager, shopUuid, shopName, currency);
                return result instanceof Boolean && (Boolean) result;
            } catch (Exception e) {
                plugin.getLogger().warning("Error invoking createAccount method: " + e.getMessage());
                // Shop can still operate without an account, so return true
                plugin.getLogger().info("Shop will operate without a dedicated economy account");
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error creating shop account", e);
            return false;
        }
    }

    /**
     * Get the default currency
     *
     * @return The default currency
     */
    public String getDefaultCurrency() {
        // To avoid reflection issues, use a safer approach
        // Return the value from config or a default value
        return plugin.getConfigManager().getDefaultCurrency();
    }

    /**
     * Get a formatted string of a player's balance
     *
     * @param player The player to get the balance for
     * @return The formatted balance
     */
    public String formatBalance(Player player) {
        double balance = getBalance(player.getUniqueId(), getDefaultCurrency());
        return formatCurrency(balance, getDefaultCurrency());
    }
    
    /**
     * Format a currency amount with the appropriate symbol
     *
     * @param amount The amount to format
     * @param currency The currency to use
     * @return The formatted amount
     */
    public String formatCurrency(double amount, String currency) {
        String format = "%s%.2f";
        String symbol = "£"; // Default to £ if we can't get the symbol
        
        if (frizzlenEcoPlugin != null) {
            try {
                // Attempt to get the currency symbol from FrizzlenEco
                Method getCurrencySymbolMethod = frizzlenEcoPlugin.getClass().getMethod("getCurrencySymbol", String.class);
                Object result = getCurrencySymbolMethod.invoke(frizzlenEcoPlugin, currency);
                if (result != null) {
                    symbol = (String) result;
                }
            } catch (Exception e) {
                if (plugin.getConfigManager().isTestingMode()) {
                    plugin.getLogger().log(Level.WARNING, "Failed to get currency symbol, using default: " + e.getMessage());
                }
            }
        }
        
        return String.format(format, symbol, amount);
    }

    /**
     * Get available currencies
     *
     * @return Array of available currencies
     */
    public String[] getAvailableCurrencies() {
        // To avoid reflection issues, return a list from config or default values
        List<String> currencies = plugin.getConfigManager().getAvailableCurrencies();
        if (currencies != null && !currencies.isEmpty()) {
            return currencies.toArray(new String[0]);
        }
        // Default if config doesn't have currencies
        return new String[]{"coins", "gems", "points"};
    }
} 