/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.core.utils.ChatUtil;
import com.pixydevelopment.pxCalendar.editor.sessions.CalendarCreateSession;
import com.pixydevelopment.pxCalendar.editor.sessions.RewardFileCreateSession;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Manages chat-based input sessions for the editor.
 */
public class EditorSessionManager {

    private final PxCalendarPlugin plugin;
    // Map<PlayerUUID, CallbackFunction>
    private final Map<UUID, Consumer<String>> chatSessions = new HashMap<>();

    public EditorSessionManager(PxCalendarPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Checks if a player is currently in a chat input session.
     * @param player The player
     * @return true if they are in a session.
     */
    public boolean isInSession(Player player) {
        return chatSessions.containsKey(player.getUniqueId());
    }

    /**
     * Handles the chat input from a player.
     * @param player The player
     * @param message The message they typed
     * @return true if the message was handled by a session, false otherwise.
     */
    public boolean handleChatInput(Player player, String message) {
        Consumer<String> sessionCallback = chatSessions.remove(player.getUniqueId());

        if (sessionCallback != null) {
            sessionCallback.accept(message); // Run the callback function
            return true;
        }
        return false;
    }

    /**
     * Puts a player into a session to create a new calendar.
     * @param player The admin player
     */
    public void startCalendarCreateSession(Player player) {
        player.closeInventory();

        // Send the prompt (from lang.yml)
        for (String line : plugin.getLangManager().getLangConfig().getStringList("editor.prompt-calendar-name")) {
            player.sendMessage(ChatUtil.format(line.replace("%plugin-prefix%", plugin.getLangManager().getMessage("plugin-prefix"))));
        }

        // The consumer (lambda function) that will run when the player types
        chatSessions.put(player.getUniqueId(), (input) -> {
            // This code runs when the player chats
            CalendarCreateSession session = new CalendarCreateSession(plugin, player, input);
            session.process();
        });
    }

    public void startRewardFileCreateSession(Player player) {
        player.closeInventory();

        // Send the prompt (from lang.yml)
        for (String line : plugin.getLangManager().getLangConfig().getStringList("editor.prompt-reward-file-name")) {
            player.sendMessage(ChatUtil.format(line.replace("%plugin-prefix%", plugin.getLangManager().getMessage("plugin-prefix"))));
        }

        chatSessions.put(player.getUniqueId(), (input) -> {
            new RewardFileCreateSession(plugin, player, input).process();
        });
    }

    /**
     * Removes a player from a session (e.g., if they disconnect).
     * @param player The player
     */
    public void clearSession(Player player) {
        chatSessions.remove(player.getUniqueId());
    }
}