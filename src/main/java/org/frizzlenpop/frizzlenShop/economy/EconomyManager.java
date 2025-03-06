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
    private Object economyAPI;

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

        // Try to get the API directly using getEconomyAPI method
        try {
            Method getApiMethod = frizzlenEcoPlugin.getClass().getMethod("getEconomyAPI");
            economyAPI = getApiMethod.invoke(frizzlenEcoPlugin);
            if (economyAPI != null) {
                plugin.getLogger().info("Successfully obtained EconomyAPI from FrizzlenEco!");
                
                // Check if it implements EconomyProvider
                Class<?> economyProviderClass = null;
                try {
                    economyProviderClass = Class.forName("org.frizzlenpop.frizzlenEco.api.EconomyProvider");
                    if (economyProviderClass.isInstance(economyAPI)) {
                        plugin.getLogger().info("EconomyAPI properly implements EconomyProvider interface!");
                    } else {
                        plugin.getLogger().warning("EconomyAPI does not implement EconomyProvider interface as expected.");
                    }
                } catch (ClassNotFoundException e) {
                    plugin.getLogger().warning("Could not find EconomyProvider interface: " + e.getMessage());
                }
                
                // Log available methods for debugging
                plugin.getLogger().info("Available methods in EconomyAPI:");
                for (Method method : economyAPI.getClass().getMethods()) {
                    plugin.getLogger().info("  " + method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")");
                }
            } else {
                plugin.getLogger().warning("EconomyAPI is null, will try alternative methods.");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not get EconomyAPI directly: " + e.getMessage());
            plugin.getLogger().info("Will try alternative methods to access economy functionality.");
            
            // Log all available methods in FrizzlenEco for debugging
            plugin.getLogger().info("Available methods in FrizzlenEco main class:");
            try {
                for (Method method : frizzlenEcoPlugin.getClass().getMethods()) {
                    plugin.getLogger().info("  " + method.getName() + "(" + Arrays.toString(method.getParameterTypes()) + ")");
                }
            } catch (Exception ignore) {}
        }

        plugin.getLogger().info("Economy integration setup completed!");
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

        // For testing and debugging, we can return true
        // This allows testing shop functionality without requiring FrizzlenEco to work
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test has() result for player " + playerUuid);
            return true; // Always return true in test mode
        }
        
        try {
            // First approach: Use the EconomyAPI directly if available
            if (economyAPI != null) {
                try {
                    // Convert amount to BigDecimal
                    java.math.BigDecimal bigAmount = java.math.BigDecimal.valueOf(amount);
                    
                    // Get the Currency object from the name
                    Object currencyObj = getCurrencyFromName(currency);
                    if (currencyObj == null) {
                        // Fall back to default currency if not found
                        Method getDefaultCurrency = economyAPI.getClass().getMethod("getDefaultCurrency");
                        currencyObj = getDefaultCurrency.invoke(economyAPI);
                    }
                    
                    // Get the OfflinePlayer object from UUID
                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
                    
                    // Call the has method
                    Method hasMethod = economyAPI.getClass().getMethod("has", OfflinePlayer.class, java.math.BigDecimal.class, Class.forName("org.frizzlenpop.frizzlenEco.economy.Currency"));
                    Object result = hasMethod.invoke(economyAPI, player, bigAmount, currencyObj);
                    
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error checking has via EconomyAPI: " + e.getMessage());
                    // Continue to fallback methods
                }
            }
            
            // First fallback: Use the balance
            double balance = getBalance(playerUuid, currency);
            if (balance >= amount) {
                return true;
            }

            // Second fallback: Try to find and call a dedicated "has" method via older approach
            Object economyManager = null;
            try {
                economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not access EconomyManager from FrizzlenEco: " + e.getMessage());
                return false;
            }
            
            if (economyManager == null) {
                return false;
            }
            
            // Try all possible "has" methods in order of likelihood
            String[] possibleMethodNames = {
                "has",
                "hasBalance",
                "hasMoney",
                "hasEnough",
                "hasAmount",
                "hasEconomy",
                "hasCurrency",
                "hasPlayerBalance",
                "hasPlayerMoney"
            };
            
            Method hasMethod = null;
            
            // Try with UUID, double, and currency parameters
            for (String methodName : possibleMethodNames) {
                try {
                    hasMethod = economyManager.getClass().getMethod(methodName, UUID.class, double.class, String.class);
                    plugin.getLogger().info("Found has method: " + methodName + "(UUID, double, String)");
                    break;
                } catch (NoSuchMethodException e) {
                    // Continue to next method name
                }
            }
            
            // If not found, try with UUID and double parameters
            if (hasMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        hasMethod = economyManager.getClass().getMethod(methodName, UUID.class, double.class);
                        plugin.getLogger().info("Found has method: " + methodName + "(UUID, double)");
                        break;
                    } catch (NoSuchMethodException e) {
                        // Continue to next method name
                    }
                }
            }
            
            // If not found, try OfflinePlayer variations
            if (hasMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        hasMethod = economyManager.getClass().getMethod(methodName, OfflinePlayer.class, double.class, String.class);
                        plugin.getLogger().info("Found has method: " + methodName + "(OfflinePlayer, double, String)");
                        break;
                    } catch (NoSuchMethodException e) {
                        // Continue to next method name
                    }
                }
            }
            
            // If still not found, try with just OfflinePlayer and double
            if (hasMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        hasMethod = economyManager.getClass().getMethod(methodName, OfflinePlayer.class, double.class);
                        plugin.getLogger().info("Found has method: " + methodName + "(OfflinePlayer, double)");
                        break;
                    } catch (NoSuchMethodException e) {
                        // Continue to next method name
                    }
                }
            }
            
            // If found a method, try to call it
            if (hasMethod != null) {
                try {
                    Object result;
                    Class<?>[] paramTypes = hasMethod.getParameterTypes();
                    
                    if (paramTypes.length == 3) {
                        if (paramTypes[0] == UUID.class) {
                            result = hasMethod.invoke(economyManager, playerUuid, amount, currency);
                        } else {
                            // Must be OfflinePlayer
                            result = hasMethod.invoke(economyManager, Bukkit.getOfflinePlayer(playerUuid), amount, currency);
                        }
                    } else {
                        if (paramTypes[0] == UUID.class) {
                            result = hasMethod.invoke(economyManager, playerUuid, amount);
                        } else {
                            // Must be OfflinePlayer
                            result = hasMethod.invoke(economyManager, Bukkit.getOfflinePlayer(playerUuid), amount);
                        }
                    }
                    
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error invoking has method: " + e.getMessage());
                }
            }
            
            // Fall back to using the balance result if no "has" method worked
            return balance >= amount;
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if player has funds", e);
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
            // First approach: Use the EconomyAPI directly if available
            if (economyAPI != null) {
                try {
                    // Get the Currency object from the name
                    Object currencyObj = getCurrencyFromName(currency);
                    if (currencyObj == null) {
                        // Fall back to default currency if not found
                        Method getDefaultCurrency = economyAPI.getClass().getMethod("getDefaultCurrency");
                        currencyObj = getDefaultCurrency.invoke(economyAPI);
                    }
                    
                    // Get the OfflinePlayer object from UUID
                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
                    
                    // Call the getBalance method
                    Method getBalanceMethod = economyAPI.getClass().getMethod("getBalance", OfflinePlayer.class, Class.forName("org.frizzlenpop.frizzlenEco.economy.Currency"));
                    Object result = getBalanceMethod.invoke(economyAPI, player, currencyObj);
                    
                    // Convert the BigDecimal result to double
                    if (result != null) {
                        if (result instanceof java.math.BigDecimal) {
                            return ((java.math.BigDecimal) result).doubleValue();
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error getting balance via EconomyAPI: " + e.getMessage());
                    // Continue to fallback methods
                }
            }
            
            // Fallback to older method if the new approach fails
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
            
            // Try all possible balance methods in order of likelihood
            String[] possibleMethodNames = {
                "getBalance", 
                "getUserBalance", 
                "getPlayerBalance",
                "getBal",
                "getCurrencyBalance",
                "checkBalance",
                "balance",
                "getPlayerMoney",
                "getMoney"
            };
            
            Method balanceMethod = null;
            
            // Try with UUID and String currency parameters first
            for (String methodName : possibleMethodNames) {
                try {
                    balanceMethod = economyManager.getClass().getMethod(methodName, UUID.class, String.class);
                    plugin.getLogger().info("Found balance method: " + methodName + "(UUID, String)");
                    break;
                } catch (NoSuchMethodException e) {
                    // Continue to next method name
                }
            }
            
            // If not found, try with just UUID parameter
            if (balanceMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        balanceMethod = economyManager.getClass().getMethod(methodName, UUID.class);
                        plugin.getLogger().info("Found balance method: " + methodName + "(UUID)");
                        break;
                    } catch (NoSuchMethodException e) {
                        // Continue to next method name
                    }
                }
            }
            
            // If not found, try OfflinePlayer variations
            if (balanceMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        balanceMethod = economyManager.getClass().getMethod(methodName, OfflinePlayer.class, String.class);
                        plugin.getLogger().info("Found balance method: " + methodName + "(OfflinePlayer, String)");
                        break;
                    } catch (NoSuchMethodException e) {
                        // Continue to next method name
                    }
                }
            }
            
            // If still not found, try with just OfflinePlayer parameter
            if (balanceMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        balanceMethod = economyManager.getClass().getMethod(methodName, OfflinePlayer.class);
                        plugin.getLogger().info("Found balance method: " + methodName + "(OfflinePlayer)");
                        break;
                    } catch (NoSuchMethodException e) {
                        // Continue to next method name
                    }
                }
            }
            
            // If found a method, try to call it
            if (balanceMethod != null) {
                try {
                    Object result;
                    Class<?>[] paramTypes = balanceMethod.getParameterTypes();
                    
                    if (paramTypes.length == 2) {
                        if (paramTypes[0] == UUID.class) {
                            result = balanceMethod.invoke(economyManager, playerUuid, currency);
                        } else {
                            // Must be OfflinePlayer
                            result = balanceMethod.invoke(economyManager, Bukkit.getOfflinePlayer(playerUuid), currency);
                        }
                    } else {
                        if (paramTypes[0] == UUID.class) {
                            result = balanceMethod.invoke(economyManager, playerUuid);
                        } else {
                            // Must be OfflinePlayer
                            result = balanceMethod.invoke(economyManager, Bukkit.getOfflinePlayer(playerUuid));
                        }
                    }
                    
                    if (result instanceof Double) {
                        return (Double) result;
                    } else if (result instanceof Number) {
                        return ((Number) result).doubleValue();
                    } else if (result instanceof String) {
                        try {
                            return Double.parseDouble((String) result);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    } else if (result instanceof java.math.BigDecimal) {
                        return ((java.math.BigDecimal) result).doubleValue();
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error invoking balance method: " + e.getMessage());
                }
            } else {
                plugin.getLogger().warning("No balance method found in FrizzlenEco. Using default balance 0.");
            }
            
            return 0;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error retrieving balance", e);
            return 0;
        }
    }

    /**
     * Helper method to get Currency object from name
     *
     * @param currencyName The name of the currency
     * @return The Currency object, or null if not found
     */
    private Object getCurrencyFromName(String currencyName) {
        if (economyAPI == null || currencyName == null) {
            return null;
        }
        
        try {
            // Get all currencies
            Method getCurrenciesMethod = economyAPI.getClass().getMethod("getCurrencies");
            Object[] currencies = (Object[]) getCurrenciesMethod.invoke(economyAPI);
            
            // Try to find by name
            for (Object currency : currencies) {
                Method getNameMethod = currency.getClass().getMethod("getName");
                String name = (String) getNameMethod.invoke(currency);
                
                if (currencyName.equalsIgnoreCase(name)) {
                    return currency;
                }
                
                // Also try code
                try {
                    Method getCodeMethod = currency.getClass().getMethod("getCode");
                    String code = (String) getCodeMethod.invoke(currency);
                    
                    if (currencyName.equalsIgnoreCase(code)) {
                        return currency;
                    }
                } catch (NoSuchMethodException ignored) {
                    // getCode method doesn't exist, ignore
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting currency from name: " + e.getMessage());
        }
        
        return null;
    }

    /**
     * Transfer an amount between two players
     *
     * @param fromUuid The UUID of the player to transfer from
     * @param toUuid   The UUID of the player to transfer to
     * @param amount   The amount to transfer
     * @param currency The currency to transfer
     * @return True if the transfer was successful
     */
    public boolean transfer(UUID fromUuid, UUID toUuid, double amount, String currency) {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return false;
        }

        // For testing and debugging
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test transfer from " + fromUuid + " to " + toUuid + ", amount: " + amount);
            return true;
        }
        
        // Ensure amount is positive
        if (amount <= 0) {
            plugin.getLogger().warning("Attempted to transfer a non-positive amount: " + amount);
            return false;
        }
        
        // No self-transfers
        if (fromUuid.equals(toUuid)) {
            plugin.getLogger().warning("Attempted to transfer money to self: " + fromUuid);
            return false;
        }

        try {
            // First approach: Use the EconomyAPI directly if available
            if (economyAPI != null) {
                try {
                    // Convert amount to BigDecimal
                    java.math.BigDecimal bigAmount = java.math.BigDecimal.valueOf(amount);
                    
                    // Get the Currency object from the name
                    Object currencyObj = getCurrencyFromName(currency);
                    if (currencyObj == null) {
                        // Fall back to default currency if not found
                        Method getDefaultCurrency = economyAPI.getClass().getMethod("getDefaultCurrency");
                        currencyObj = getDefaultCurrency.invoke(economyAPI);
                    }
                    
                    // Get the OfflinePlayer objects
                    OfflinePlayer fromPlayer = Bukkit.getOfflinePlayer(fromUuid);
                    OfflinePlayer toPlayer = Bukkit.getOfflinePlayer(toUuid);
                    
                    // Check if source player has enough funds first
                    Method hasMethod = economyAPI.getClass().getMethod("has", OfflinePlayer.class, java.math.BigDecimal.class, Class.forName("org.frizzlenpop.frizzlenEco.economy.Currency"));
                    Object hasResult = hasMethod.invoke(economyAPI, fromPlayer, bigAmount, currencyObj);
                    
                    if (hasResult instanceof Boolean && !(Boolean)hasResult) {
                        plugin.getLogger().warning("Source player does not have enough funds: " + fromUuid);
                        return false;
                    }
                    
                    // Call the transfer method
                    Method transferMethod = economyAPI.getClass().getMethod("transfer", OfflinePlayer.class, OfflinePlayer.class, java.math.BigDecimal.class, Class.forName("org.frizzlenpop.frizzlenEco.economy.Currency"));
                    Object result = transferMethod.invoke(economyAPI, fromPlayer, toPlayer, bigAmount, currencyObj);
                    
                    if (result instanceof Boolean) {
                        boolean success = (Boolean) result;
                        if (success) {
                            plugin.getLogger().info("Successfully transferred " + formatCurrency(amount, currency) + 
                                " from " + fromPlayer.getName() + " to " + toPlayer.getName());
                        } else {
                            plugin.getLogger().warning("Failed to transfer " + formatCurrency(amount, currency) + 
                                " from " + fromPlayer.getName() + " to " + toPlayer.getName());
                        }
                        return success;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error transferring via EconomyAPI: " + e.getMessage());
                    // Continue to fallback methods
                }
            }
            
            // Second approach: Withdraw + Deposit manually if direct transfer fails
            if (withdraw(fromUuid, amount, currency)) {
                if (deposit(toUuid, amount, currency)) {
                    plugin.getLogger().info("Successfully transferred " + formatCurrency(amount, currency) +
                            " from " + fromUuid + " to " + toUuid + " using withdraw/deposit");
                    return true;
                } else {
                    // Failed to deposit, return the money to the source
                    deposit(fromUuid, amount, currency);
                    plugin.getLogger().warning("Failed to transfer " + formatCurrency(amount, currency) +
                            " from " + fromUuid + " to " + toUuid + " - deposit failed, money returned");
                    return false;
                }
            } else {
                plugin.getLogger().warning("Failed to transfer " + formatCurrency(amount, currency) +
                        " from " + fromUuid + " to " + toUuid + " - withdraw failed");
                return false;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error transferring money", e);
            return false;
        }
    }

    /**
     * Withdraw an amount from a player's account
     *
     * @param playerUuid The UUID of the player to withdraw from
     * @param amount     The amount to withdraw
     * @param currency   The currency to withdraw
     * @return True if the withdrawal was successful
     */
    public boolean withdraw(UUID playerUuid, double amount, String currency) {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return false;
        }

        // For testing and debugging
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test withdraw for player " + playerUuid + ", amount: " + amount);
            return true;
        }
        
        // Ensure amount is positive
        if (amount <= 0) {
            plugin.getLogger().warning("Attempted to withdraw a non-positive amount: " + amount);
            return false;
        }

        try {
            // First approach: Use the EconomyAPI directly if available
            if (economyAPI != null) {
                try {
                    // Convert amount to BigDecimal
                    java.math.BigDecimal bigAmount = java.math.BigDecimal.valueOf(amount);
                    
                    // Get the Currency object from the name
                    Object currencyObj = getCurrencyFromName(currency);
                    if (currencyObj == null) {
                        // Fall back to default currency if not found
                        Method getDefaultCurrency = economyAPI.getClass().getMethod("getDefaultCurrency");
                        currencyObj = getDefaultCurrency.invoke(economyAPI);
                    }
                    
                    // Get the OfflinePlayer object from UUID
                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
                    
                    // Check if player has enough funds first
                    Method hasMethod = economyAPI.getClass().getMethod("has", OfflinePlayer.class, java.math.BigDecimal.class, Class.forName("org.frizzlenpop.frizzlenEco.economy.Currency"));
                    Object hasResult = hasMethod.invoke(economyAPI, player, bigAmount, currencyObj);
                    
                    if (hasResult instanceof Boolean && !(Boolean)hasResult) {
                        plugin.getLogger().warning("Player does not have enough funds: " + playerUuid);
                        return false;
                    }
                    
                    // Call the withdraw method
                    Method withdrawMethod = economyAPI.getClass().getMethod("withdraw", OfflinePlayer.class, java.math.BigDecimal.class, Class.forName("org.frizzlenpop.frizzlenEco.economy.Currency"));
                    Object result = withdrawMethod.invoke(economyAPI, player, bigAmount, currencyObj);
                    
                    if (result instanceof Boolean) {
                        boolean success = (Boolean) result;
                        if (success) {
                            plugin.getLogger().info("Successfully withdrew " + formatCurrency(amount, currency) + " from " + player.getName());
                        } else {
                            plugin.getLogger().warning("Failed to withdraw " + formatCurrency(amount, currency) + " from " + player.getName());
                        }
                        return success;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error withdrawing via EconomyAPI: " + e.getMessage());
                    // Continue to fallback methods
                }
            }
            
            // Fallback to older method if the new approach fails
            Object economyManager = null;
            try {
                economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not access EconomyManager from FrizzlenEco: " + e.getMessage());
                return false;
            }
            
            if (economyManager == null) {
                return false;
            }
            
            // First check if the player has enough money
            if (!has(playerUuid, amount, currency)) {
                plugin.getLogger().warning("Player " + playerUuid + " does not have enough money for withdrawal.");
                return false;
            }
            
            // Try various method names for withdraw operation
            String[] possibleMethodNames = {
                "withdraw",
                "withdrawPlayer",
                "removeBalance",
                "debit",
                "take",
                "removeMoney"
            };
            
            Method withdrawMethod = null;
            for (String methodName : possibleMethodNames) {
                try {
                    withdrawMethod = economyManager.getClass().getMethod(methodName, UUID.class, double.class, String.class);
                    break;
                } catch (NoSuchMethodException e) {
                    // Try next method name
                }
            }
            
            // If not found, try with UUID and double (default currency)
            if (withdrawMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        withdrawMethod = economyManager.getClass().getMethod(methodName, UUID.class, double.class);
                        break;
                    } catch (NoSuchMethodException e) {
                        // Try next method name
                    }
                }
            }
            
            // If not found, try with OfflinePlayer
            if (withdrawMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        withdrawMethod = economyManager.getClass().getMethod(methodName, OfflinePlayer.class, double.class, String.class);
                        break;
                    } catch (NoSuchMethodException e) {
                        // Try next method name
                    }
                }
            }
            
            // If not found, try with OfflinePlayer and double (default currency)
            if (withdrawMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        withdrawMethod = economyManager.getClass().getMethod(methodName, OfflinePlayer.class, double.class);
                        break;
                    } catch (NoSuchMethodException e) {
                        // Try next method name
                    }
                }
            }
            
            // If a method was found, call it
            if (withdrawMethod != null) {
                try {
                    Object result;
                    Class<?>[] paramTypes = withdrawMethod.getParameterTypes();
                    
                    if (paramTypes.length == 3) {
                        if (paramTypes[0] == UUID.class) {
                            result = withdrawMethod.invoke(economyManager, playerUuid, amount, currency);
                        } else {
                            // Must be OfflinePlayer
                            result = withdrawMethod.invoke(economyManager, Bukkit.getOfflinePlayer(playerUuid), amount, currency);
                        }
                    } else {
                        if (paramTypes[0] == UUID.class) {
                            result = withdrawMethod.invoke(economyManager, playerUuid, amount);
                        } else {
                            // Must be OfflinePlayer
                            result = withdrawMethod.invoke(economyManager, Bukkit.getOfflinePlayer(playerUuid), amount);
                        }
                    }
                    
                    if (result instanceof Boolean) {
                        boolean success = (Boolean) result;
                        if (success) {
                            plugin.getLogger().info("Successfully withdrew " + formatCurrency(amount, currency) + " from " + playerUuid);
                        }
                        return success;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error invoking withdraw method: " + e.getMessage());
                }
            }
            
            plugin.getLogger().warning("No withdraw method found in FrizzlenEco.");
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error withdrawing money", e);
            return false;
        }
    }

    /**
     * Deposit an amount to a player's account
     *
     * @param playerUuid The UUID of the player to deposit to
     * @param amount     The amount to deposit
     * @param currency   The currency to deposit
     * @return True if the deposit was successful
     */
    public boolean deposit(UUID playerUuid, double amount, String currency) {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return false;
        }

        // For testing and debugging
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test deposit for player " + playerUuid + ", amount: " + amount);
            return true;
        }
        
        // Ensure amount is positive
        if (amount <= 0) {
            plugin.getLogger().warning("Attempted to deposit a non-positive amount: " + amount);
            return false;
        }

        try {
            // First approach: Use the EconomyAPI directly if available
            if (economyAPI != null) {
                try {
                    // Convert amount to BigDecimal
                    java.math.BigDecimal bigAmount = java.math.BigDecimal.valueOf(amount);
                    
                    // Get the Currency object from the name
                    Object currencyObj = getCurrencyFromName(currency);
                    if (currencyObj == null) {
                        // Fall back to default currency if not found
                        Method getDefaultCurrency = economyAPI.getClass().getMethod("getDefaultCurrency");
                        currencyObj = getDefaultCurrency.invoke(economyAPI);
                    }
                    
                    // Get the OfflinePlayer object from UUID
                    OfflinePlayer player = Bukkit.getOfflinePlayer(playerUuid);
                    
                    // Call the deposit method
                    Method depositMethod = economyAPI.getClass().getMethod("deposit", OfflinePlayer.class, java.math.BigDecimal.class, Class.forName("org.frizzlenpop.frizzlenEco.economy.Currency"));
                    Object result = depositMethod.invoke(economyAPI, player, bigAmount, currencyObj);
                    
                    if (result instanceof Boolean) {
                        boolean success = (Boolean) result;
                        if (success) {
                            plugin.getLogger().info("Successfully deposited " + formatCurrency(amount, currency) + " to " + player.getName());
                        } else {
                            plugin.getLogger().warning("Failed to deposit " + formatCurrency(amount, currency) + " to " + player.getName());
                        }
                        return success;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error depositing via EconomyAPI: " + e.getMessage());
                    // Continue to fallback methods
                }
            }
            
            // Fallback to older method if the new approach fails
            Object economyManager = null;
            try {
                economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not access EconomyManager from FrizzlenEco: " + e.getMessage());
                return false;
            }
            
            if (economyManager == null) {
                return false;
            }
            
            // Try various method names for deposit operation
            String[] possibleMethodNames = {
                "deposit",
                "depositPlayer",
                "addBalance",
                "credit",
                "give",
                "addMoney"
            };
            
            Method depositMethod = null;
            for (String methodName : possibleMethodNames) {
                try {
                    depositMethod = economyManager.getClass().getMethod(methodName, UUID.class, double.class, String.class);
                    break;
                } catch (NoSuchMethodException e) {
                    // Try next method name
                }
            }
            
            // If not found, try with UUID and double (default currency)
            if (depositMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        depositMethod = economyManager.getClass().getMethod(methodName, UUID.class, double.class);
                        break;
                    } catch (NoSuchMethodException e) {
                        // Try next method name
                    }
                }
            }
            
            // If not found, try with OfflinePlayer
            if (depositMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        depositMethod = economyManager.getClass().getMethod(methodName, OfflinePlayer.class, double.class, String.class);
                        break;
                    } catch (NoSuchMethodException e) {
                        // Try next method name
                    }
                }
            }
            
            // If not found, try with OfflinePlayer and double (default currency)
            if (depositMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        depositMethod = economyManager.getClass().getMethod(methodName, OfflinePlayer.class, double.class);
                        break;
                    } catch (NoSuchMethodException e) {
                        // Try next method name
                    }
                }
            }
            
            // If a method was found, call it
            if (depositMethod != null) {
                try {
                    Object result;
                    Class<?>[] paramTypes = depositMethod.getParameterTypes();
                    
                    if (paramTypes.length == 3) {
                        if (paramTypes[0] == UUID.class) {
                            result = depositMethod.invoke(economyManager, playerUuid, amount, currency);
                        } else {
                            // Must be OfflinePlayer
                            result = depositMethod.invoke(economyManager, Bukkit.getOfflinePlayer(playerUuid), amount, currency);
                        }
                    } else {
                        if (paramTypes[0] == UUID.class) {
                            result = depositMethod.invoke(economyManager, playerUuid, amount);
                        } else {
                            // Must be OfflinePlayer
                            result = depositMethod.invoke(economyManager, Bukkit.getOfflinePlayer(playerUuid), amount);
                        }
                    }
                    
                    if (result instanceof Boolean) {
                        boolean success = (Boolean) result;
                        if (success) {
                            plugin.getLogger().info("Successfully deposited " + formatCurrency(amount, currency) + " to " + playerUuid);
                        }
                        return success;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error invoking deposit method: " + e.getMessage());
                }
            }
            
            plugin.getLogger().warning("No deposit method found in FrizzlenEco.");
            return false;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error depositing money", e);
            return false;
        }
    }

    /**
     * Create an account for a shop
     *
     * @param shopUuid   The UUID of the shop
     * @param shopName   The name of the shop
     * @param currency   The currency to use
     * @return True if the account was created successfully
     */
    public boolean createShopAccount(UUID shopUuid, String shopName, String currency) {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return false;
        }

        // For testing and debugging
        if (plugin.getConfigManager().isTestingMode()) {
            plugin.getLogger().info("Using test createShopAccount for shop " + shopName + " (" + shopUuid + ")");
            return true;
        }

        try {
            // First approach: Use the EconomyAPI directly if available
            if (economyAPI != null) {
                try {
                    // Get the Currency object from the name
                    Object currencyObj = getCurrencyFromName(currency);
                    if (currencyObj == null) {
                        // Fall back to default currency if not found
                        Method getDefaultCurrency = economyAPI.getClass().getMethod("getDefaultCurrency");
                        currencyObj = getDefaultCurrency.invoke(economyAPI);
                    }
                    
                    // Create an OfflinePlayer using the shop UUID
                    OfflinePlayer shopAccount = Bukkit.getOfflinePlayer(shopUuid);
                    
                    // Call the createAccount method
                    Method createAccountMethod = economyAPI.getClass().getMethod("createAccount", OfflinePlayer.class, Class.forName("org.frizzlenpop.frizzlenEco.economy.Currency"));
                    Object result = createAccountMethod.invoke(economyAPI, shopAccount, currencyObj);
                    
                    if (result instanceof Boolean) {
                        boolean success = (Boolean) result;
                        if (success) {
                            plugin.getLogger().info("Successfully created shop account for " + shopName + " (" + shopUuid + ")");
                        } else {
                            plugin.getLogger().warning("Failed to create shop account for " + shopName + " (" + shopUuid + ")");
                        }
                        return success;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error creating shop account via EconomyAPI: " + e.getMessage());
                    // Continue to fallback methods
                }
            }
            
            // Fallback to older method if the new approach fails
            Object economyManager = null;
            try {
                economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not access EconomyManager from FrizzlenEco: " + e.getMessage());
                return false;
            }
            
            if (economyManager == null) {
                return false;
            }
            
            // Try various method names for account creation
            String[] possibleMethodNames = {
                "createAccount",
                "createPlayerAccount",
                "createShopAccount",
                "registerAccount",
                "addAccount"
            };
            
            // Try with different parameter combinations
            Method createAccountMethod = null;
            
            // Try with UUID, String, String
            for (String methodName : possibleMethodNames) {
                try {
                    createAccountMethod = economyManager.getClass().getMethod(methodName, UUID.class, String.class, String.class);
                    break;
                } catch (NoSuchMethodException e) {
                    // Try next method name
                }
            }
            
            // If not found, try with UUID, String
            if (createAccountMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        createAccountMethod = economyManager.getClass().getMethod(methodName, UUID.class, String.class);
                        break;
                    } catch (NoSuchMethodException e) {
                        // Try next method name
                    }
                }
            }
            
            // If not found, try with just UUID
            if (createAccountMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        createAccountMethod = economyManager.getClass().getMethod(methodName, UUID.class);
                        break;
                    } catch (NoSuchMethodException e) {
                        // Try next method name
                    }
                }
            }
            
            // If not found, try with OfflinePlayer
            if (createAccountMethod == null) {
                for (String methodName : possibleMethodNames) {
                    try {
                        createAccountMethod = economyManager.getClass().getMethod(methodName, OfflinePlayer.class, String.class);
                        break;
                    } catch (NoSuchMethodException e) {
                        // Try next method name
                    }
                }
            }
            
            // If a method was found, call it
            if (createAccountMethod != null) {
                try {
                    Object result;
                    Class<?>[] paramTypes = createAccountMethod.getParameterTypes();
                    
                    if (paramTypes.length == 3 && paramTypes[0] == UUID.class) {
                        result = createAccountMethod.invoke(economyManager, shopUuid, shopName, currency);
                    } else if (paramTypes.length == 2) {
                        if (paramTypes[0] == UUID.class) {
                            result = createAccountMethod.invoke(economyManager, shopUuid, shopName);
                        } else {
                            // Must be OfflinePlayer
                            result = createAccountMethod.invoke(economyManager, Bukkit.getOfflinePlayer(shopUuid), currency);
                        }
                    } else {
                        // Must be just UUID
                        result = createAccountMethod.invoke(economyManager, shopUuid);
                    }
                    
                    if (result instanceof Boolean) {
                        return (Boolean) result;
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error invoking createAccount method: " + e.getMessage());
                }
            }
            
            // If we can't create an account directly, try to check if there's a balance already
            // This is a workaround as some economy plugins auto-create accounts
            try {
                double balance = getBalance(shopUuid, currency);
                plugin.getLogger().info("Shop account for " + shopName + " exists with balance " + balance);
                return true;
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check if shop account exists: " + e.getMessage());
            }
            
            plugin.getLogger().warning("Could not create shop account for " + shopName);
            return false;
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
     * Format the balance of a player
     *
     * @param player The player to get the balance for
     * @return The formatted balance
     */
    public String formatBalance(Player player) {
        // For testing and debugging, we can use a fixed format
        if (plugin.getConfigManager().isTestingMode()) {
            return "$10,000.00";
        }
        
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return "$0.00";
        }
        
        try {
            // First approach: Use the EconomyAPI directly if available
            if (economyAPI != null) {
                try {
                    // Get default currency
                    Method getDefaultCurrency = economyAPI.getClass().getMethod("getDefaultCurrency");
                    Object currencyObj = getDefaultCurrency.invoke(economyAPI);
                    
                    // Get the player's balance
                    Method getBalanceMethod = economyAPI.getClass().getMethod("getBalance", OfflinePlayer.class);
                    Object balanceObj = getBalanceMethod.invoke(economyAPI, player);
                    
                    // Format the balance
                    if (balanceObj != null) {
                        Method formatMethod = economyAPI.getClass().getMethod("format", java.math.BigDecimal.class);
                        Object formattedBalance = formatMethod.invoke(economyAPI, balanceObj);
                        if (formattedBalance != null) {
                            return formattedBalance.toString();
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error formatting balance via EconomyAPI: " + e.getMessage());
                    // Continue to fallback methods
                }
            }
            
            // Fall back to our own methods
            double balance = getBalance(player.getUniqueId(), getDefaultCurrency());
            return formatCurrency(balance, getDefaultCurrency());
        } catch (Exception e) {
            plugin.getLogger().warning("Error formatting player balance: " + e.getMessage());
            return "$0.00";
        }
    }
    
    /**
     * Format a currency amount with the appropriate symbol
     *
     * @param amount The amount to format
     * @param currency The currency to use
     * @return The formatted amount
     */
    public String formatCurrency(double amount, String currency) {
        // For testing and debugging, we can use a fixed format
        if (plugin.getConfigManager().isTestingMode()) {
            return "$" + String.format("%.2f", amount);
        }
        
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return "$" + String.format("%.2f", amount);
        }
        
        try {
            // First approach: Use the EconomyAPI directly if available
            if (economyAPI != null) {
                try {
                    // Convert amount to BigDecimal
                    java.math.BigDecimal bigAmount = java.math.BigDecimal.valueOf(amount);
                    
                    // Get the Currency object from the name
                    Object currencyObj = getCurrencyFromName(currency);
                    if (currencyObj == null) {
                        // Fall back to default currency if not found
                        Method getDefaultCurrency = economyAPI.getClass().getMethod("getDefaultCurrency");
                        currencyObj = getDefaultCurrency.invoke(economyAPI);
                    }
                    
                    // Call the format method
                    Method formatMethod = economyAPI.getClass().getMethod("format", java.math.BigDecimal.class, Class.forName("org.frizzlenpop.frizzlenEco.economy.Currency"));
                    Object result = formatMethod.invoke(economyAPI, bigAmount, currencyObj);
                    
                    if (result != null) {
                        return result.toString();
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error formatting currency via EconomyAPI: " + e.getMessage());
                    // Continue to fallback methods
                }
            }
            
            // Fallback to direct currency formatting if available
            try {
                // Try to format directly through the Currency object if we can get it
                Object currencyObj = getCurrencyFromName(currency);
                if (currencyObj != null) {
                    Method formatMethod = currencyObj.getClass().getMethod("format", java.math.BigDecimal.class);
                    Object result = formatMethod.invoke(currencyObj, java.math.BigDecimal.valueOf(amount));
                    if (result != null) {
                        return result.toString();
                    }
                }
            } catch (Exception e) {
                // Fall back to older methods
            }
        
            // Fallback to older approach
            String format = "%s%.2f";
            String symbol = "$"; // Default symbol
            
            if (frizzlenEcoPlugin != null) {
                // Try different possible method names for getting currency symbol
                String[] possibleMethodNames = {
                    "getCurrencySymbol",
                    "getSymbol",
                    "getCurrencySign",
                    "getSign",
                    "formatCurrency"
                };
                
                for (String methodName : possibleMethodNames) {
                    try {
                        Method method = frizzlenEcoPlugin.getClass().getMethod(methodName, String.class);
                        Object result = method.invoke(frizzlenEcoPlugin, currency);
                        if (result != null) {
                            if (methodName.equals("formatCurrency")) {
                                // This might return the full formatted amount
                                return result.toString();
                            } else {
                                symbol = result.toString();
                                plugin.getLogger().info("Found currency symbol '" + symbol + "' using method: " + methodName);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // Try the next method
                    }
                }
                
                // If we didn't find a method on the main plugin, try via EconomyManager
                if (symbol.equals("$")) {
                    try {
                        Object economyManager = frizzlenEcoPlugin.getClass().getMethod("getEconomyManager").invoke(frizzlenEcoPlugin);
                        if (economyManager != null) {
                            for (String methodName : possibleMethodNames) {
                                try {
                                    Method method = economyManager.getClass().getMethod(methodName, String.class);
                                    Object result = method.invoke(economyManager, currency);
                                    if (result != null) {
                                        if (methodName.equals("formatCurrency")) {
                                            // This might return the full formatted amount
                                            return result.toString();
                                        } else {
                                            symbol = result.toString();
                                            plugin.getLogger().info("Found currency symbol '" + symbol + "' via EconomyManager using method: " + methodName);
                                            break;
                                        }
                                    }
                                } catch (Exception e) {
                                    // Try the next method
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Just use the default symbol
                    }
                }
            }
            
            return String.format(format, symbol, amount);
        } catch (Exception e) {
            plugin.getLogger().warning("Error formatting currency: " + e.getMessage());
            return "$" + String.format("%.2f", amount);
        }
    }

    /**
     * Get all available currencies
     *
     * @return Array of available currency names
     */
    public String[] getAvailableCurrencies() {
        if (!plugin.isFrizzlenEcoHooked() || frizzlenEcoPlugin == null) {
            return new String[]{getDefaultCurrency()};
        }

        try {
            // First approach: Use the EconomyAPI directly if available
            if (economyAPI != null) {
                try {
                    // Get all Currency objects
                    Method getCurrenciesMethod = economyAPI.getClass().getMethod("getCurrencies");
                    Object[] currencies = (Object[]) getCurrenciesMethod.invoke(economyAPI);
                    
                    // Extract names
                    String[] currencyNames = new String[currencies.length];
                    for (int i = 0; i < currencies.length; i++) {
                        Method getNameMethod = currencies[i].getClass().getMethod("getName");
                        currencyNames[i] = (String) getNameMethod.invoke(currencies[i]);
                    }
                    
                    return currencyNames;
                } catch (Exception e) {
                    plugin.getLogger().warning("Error getting currencies via EconomyAPI: " + e.getMessage());
                    // Continue to fallback methods
                }
            }
            
            // Fallback to older method
            // Try to get from ConfigManager
            List<String> configCurrencies = plugin.getConfigManager().getAvailableCurrencies();
            if (configCurrencies != null && !configCurrencies.isEmpty()) {
                return configCurrencies.toArray(new String[0]);
            }
            
            // If all else fails, just return the default currency
            return new String[]{getDefaultCurrency()};
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting available currencies", e);
            return new String[]{getDefaultCurrency()};
        }
    }
} 