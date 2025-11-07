/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.managers;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.calendar.PlayerCalendarData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Manages the task that reminds players of their unclaimed rewards.
 */
public class ReminderManager {

    private final PxCalendarPlugin plugin;
    private final DataManager dataManager;
    private final CalendarManager calendarManager;
    private final GUIManager guiManager;
    private final long intervalTicks;

    public ReminderManager(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.calendarManager = plugin.getCalendarManager();
        this.guiManager = plugin.getGuiManager();

        int intervalMinutes = plugin.getConfigManager().getConfig().getInt("reminders.interval-minutes", 60);
        this.intervalTicks = intervalMinutes * 60 * 20L; // minutes -> seconds -> ticks
    }

    public void startTask() {
        if (intervalTicks <= 0) {
            plugin.getLogger().info("Unclaimed reward reminders are disabled.");
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                checkAndRemindPlayers();
            }
        }.runTaskTimer(plugin, 1200L, intervalTicks); // Start after 1 min, then repeat

        plugin.getLogger().info("Unclaimed reward reminder task started.");
    }

    private void checkAndRemindPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerCalendarData data = dataManager.getPlayerData(player);
            if (data == null) {
                continue; // Player data not loaded yet
            }

            int unclaimedCount = 0;
            // Check all loaded calendars
            for (Calendar calendar : guiManager.getLoadedCalendars()) {
                // Check permission to see this calendar
                if (!player.hasPermission(calendar.getPermission())) {
                    continue;
                }

                unclaimedCount += calendar.getUnclaimedRewardCount(player);
            }

            if (unclaimedCount > 0) {
                // Send the reminder message
                plugin.getLangManager().sendMessage(player, "messages.unclaimed-rewards",
                        "%count%", String.valueOf(unclaimedCount));
            }
        }
    }
}