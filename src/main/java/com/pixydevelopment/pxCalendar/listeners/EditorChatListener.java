/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.listeners;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.editor.EditorSessionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listens for chat input from players who are in an editor session.
 */
public class EditorChatListener implements Listener {

    private final PxCalendarPlugin plugin;
    private final EditorSessionManager sessionManager;

    public EditorChatListener(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.sessionManager = plugin.getEditorSessionManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (sessionManager.isInSession(player)) {
            // Cancel the chat message from appearing publicly
            event.setCancelled(true);

            String message = event.getMessage();

            // We must run the handler on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> {
                sessionManager.handleChatInput(player, message);
            });
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up the session if the player leaves
        sessionManager.clearSession(event.getPlayer());
    }
}