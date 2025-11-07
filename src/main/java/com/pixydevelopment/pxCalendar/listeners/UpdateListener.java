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

package com.pixydevelopment.pxCalendar.listeners;

import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.core.utils.UpdateChecker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Notifies players with permission when an update is available.
 */
public class UpdateListener implements Listener {

    private final com.pixydevelopment.pxCalendar.PxCalendarPlugin plugin;
    private final UpdateChecker updateChecker;
    private final LangManager langManager;
    private final String permission;

    public UpdateListener(com.pixydevelopment.pxCalendar.PxCalendarPlugin plugin, UpdateChecker updateChecker, LangManager langManager, String permission) {
        this.plugin = plugin;
        this.updateChecker = updateChecker;
        this.langManager = langManager;
        this.permission = permission;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.hasPermission(this.permission) || player.isOp()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (updateChecker.isUpdateAvailable()) {
                    player.sendMessage(langManager.getMessage("messages.update-available",
                            "%current%", plugin.getDescription().getVersion(),
                            "%new%", updateChecker.getLatestVersion()
                    ));
                }
            }, 20L);
        }
    }
}