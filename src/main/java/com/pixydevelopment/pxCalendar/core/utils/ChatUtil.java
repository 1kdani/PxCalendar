/*
 ################################################################################
 #                                                                              #
 #                             PIXY-DEVELOPMENT                                 #
 #                                                                              #
 ################################################################################
 #                                                                              #
 #   This file is part of a plugin developed by Pixy-Development.               #
 #                                                                              #
 ################################################################################
*/

package com.pixydevelopment.pxCalendar.core.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for formatting messages.
 * Supports legacy codes (&), and HEX codes (<#RRGGBB> and &#RRGGBB).
 * Requires BungeeCord Chat API (part of Spigot 1.16+).
 */
public class ChatUtil {

    // Pattern for <#RRGGBB>
    private static final Pattern HEX_PATTERN_BRACKETS = Pattern.compile("<#([a-fA-F0-9]{6})>");
    // Pattern for &#RRGGBB
    private static final Pattern HEX_PATTERN_AMPERSAND = Pattern.compile("&#([a-fA-F0-9]{6})");

    private final static int CENTER_PX = 154;

    /**
     * Translates color codes (legacy & and HEX) in the given string.
     *
     * @param text The string to format.
     * @return The formatted string.
     */
    public static String format(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        // Translate <#RRGGBB> patterns
        Matcher bracketMatcher = HEX_PATTERN_BRACKETS.matcher(text);
        while (bracketMatcher.find()) {
            String color = bracketMatcher.group(1);
            text = text.replace(bracketMatcher.group(), ChatColor.of("#" + color) + "");
        }

        // Translate &#RRGGBB patterns
        Matcher ampersandMatcher = HEX_PATTERN_AMPERSAND.matcher(text);
        while (ampersandMatcher.find()) {
            String color = ampersandMatcher.group(1);
            text = text.replace(ampersandMatcher.group(), ChatColor.of("#" + color) + "");
        }

        // Translate legacy & codes
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * Sends a formatted message to a player or console.
     *
     * @param sender The receiver (player or console).
     * @param message The message (unformatted).
     */
    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(format(message));
    }

    /**
     * Sends a centered message to the player.
     * NOTE: This is a simplified, non-pixel-perfect centering.
     *
     * @param sender The receiver.
     * @param message The message (unformatted).
     */
    public static void sendCenteredMessage(CommandSender sender, String message) {
        if (message == null || message.isEmpty()) {
            sendMessage(sender, "");
            return;
        }

        message = format(message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                // Simplified pixel calculation
                if (c == ' ') {
                    messagePxSize += 4;
                } else {
                    messagePxSize += isBold ? 7 : 6;
                }
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = 4; // Space pixel width
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }

        sender.sendMessage(sb.toString() + message);
    }
}