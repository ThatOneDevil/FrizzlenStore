package org.frizzlenpop.frizzlenShop.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.frizzlenpop.frizzlenShop.FrizzlenShop;

public class MessageUtils {

    private static FrizzlenShop plugin;
    private static final String PREFIX = "&8[&6FrizzlenShop&8] &r";
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public static void init(FrizzlenShop pluginInstance) {
        plugin = pluginInstance;
    }

    /**
     * Colorize a string with color codes
     *
     * @param message The message to colorize
     * @return The colorized message
     */
    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Convert a string to a Component using MiniMessage format
     *
     * @param message The message to convert
     * @return The Component
     */
    public static Component toComponent(String message) {
        if (message.contains("&")) {
            // Handle legacy color codes
            message = colorize(message);
            return legacySerializer.deserialize(message);
        }
        return miniMessage.deserialize(message);
    }

    /**
     * Send a message to a player with the plugin prefix
     *
     * @param player  The player to send the message to
     * @param message The message to send
     */
    public static void sendMessage(Player player, String message) {
        player.sendMessage(toComponent(PREFIX + message));
    }

    /**
     * Send a message to a command sender with the plugin prefix
     *
     * @param sender  The command sender to send the message to
     * @param message The message to send
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(toComponent(PREFIX + message));
    }

    /**
     * Send a message to a player without the plugin prefix
     *
     * @param player  The player to send the message to
     * @param message The message to send
     */
    public static void sendRawMessage(Player player, String message) {
        player.sendMessage(toComponent(message));
    }

    /**
     * Send a message to a command sender without the plugin prefix
     *
     * @param sender  The command sender to send the message to
     * @param message The message to send
     */
    public static void sendRawMessage(CommandSender sender, String message) {
        sender.sendMessage(toComponent(message));
    }

    /**
     * Send an error message to a player
     *
     * @param player  The player to send the message to
     * @param message The message to send
     */
    public static void sendErrorMessage(Player player, String message) {
        sendMessage(player, "&c" + message);
    }

    /**
     * Send an error message to a command sender
     *
     * @param sender  The command sender to send the message to
     * @param message The message to send
     */
    public static void sendErrorMessage(CommandSender sender, String message) {
        sendMessage(sender, "&c" + message);
    }

    /**
     * Send a success message to a player
     *
     * @param player  The player to send the message to
     * @param message The message to send
     */
    public static void sendSuccessMessage(Player player, String message) {
        sendMessage(player, "&a" + message);
    }

    /**
     * Send a success message to a command sender
     *
     * @param sender  The command sender to send the message to
     * @param message The message to send
     */
    public static void sendSuccessMessage(CommandSender sender, String message) {
        sendMessage(sender, "&a" + message);
    }
} 