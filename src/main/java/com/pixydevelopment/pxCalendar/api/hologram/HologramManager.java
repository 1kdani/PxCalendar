/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.api.hologram;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.api.hologram.impl.DecentHologramService;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.calendar.PlayerCalendarData;
import com.pixydevelopment.pxCalendar.managers.CalendarManager;
import com.pixydevelopment.pxCalendar.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages the active hologram service and updates physical calendars.
 */
public class HologramManager {

    private final PxCalendarPlugin plugin;
    private final DataManager dataManager;
    private final CalendarManager calendarManager;
    private HologramService hologramService;
    private BukkitTask updateTask;

    // Map<LocationHash, PhysicalCalendar>
    private final Map<String, PhysicalCalendar> activeCalendars = new ConcurrentHashMap<>();

    public HologramManager(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.calendarManager = plugin.getCalendarManager();

        initializeService();
    }

    private void initializeService() {
        if (!plugin.getConfigManager().getConfig().getBoolean("physical-calendars.enable", true)) {
            plugin.getLogger().info("Physical Calendars are disabled in config.yml.");
            return;
        }

        // Try to hook DecentHolograms
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            this.hologramService = new DecentHologramService(plugin);
            plugin.getLogger().info("Successfully hooked into DecentHolograms.");
        }
        // TODO: Add HolographicDisplays implementation
        // else if (Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays")) {
        //    this.hologramService = new HolographicDisplaysService(plugin);
        // }

        if (this.hologramService == null) {
            plugin.getLogger().warning("Physical Calendars are enabled, but no compatible hologram plugin was found.");
        }
    }

    /**
     * Starts the asynchronous task that updates holograms for nearby players.
     */
    public void startUpdateTask() {
        if (hologramService == null) return;

        long updateInterval = plugin.getConfigManager().getConfig().getLong("physical-calendars.hologram-update-interval", 100L);
        if (updateInterval <= 0) return;

        this.updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerCalendarData data = dataManager.getPlayerData(player);
                    if (data == null) continue;

                    for (PhysicalCalendar pCal : activeCalendars.values()) {
                        // Check if player is near this hologram
                        if (isNearby(player.getLocation(), pCal.getLocation())) {
                            updateHologramForPlayer(player, data, pCal);
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(plugin, 100L, updateInterval);
    }

    /**
     * Updates a single hologram for a single player based on their status.
     */
    private void updateHologramForPlayer(Player player, PlayerCalendarData data, PhysicalCalendar pCal) {
        Calendar calendar = plugin.getGuiManager().getCalendar(pCal.getCalendarId());
        if (calendar == null) return;

        int availableCount = calendar.getAvailableUnclaimedCount(player);
        List<String> lines;

        if (availableCount > 0) {
            // AVAILABLE
            lines = plugin.getLangManager().getLangConfig().getStringList("holograms.available");
        } else {
            // Check if they claimed everything that IS available
            if (calendar.getDaysByDayNumber().values().stream()
                    .anyMatch(day -> calendarManager.isDayAvailable(day.getDay()) && !data.hasClaimed(calendar.getId(), day.getDay()))) {
                // This case should be handled by availableCount > 0, but as a fallback
                lines = plugin.getLangManager().getLangConfig().getStringList("holograms.available");
            } else {
                // ALL CLAIMED (for now)
                lines = plugin.getLangManager().getLangConfig().getStringList("holograms.all-claimed");
            }
        }

        // Find the next upcoming day
        int nextDay = calendarManager.getNextUnlockableDay(calendar);
        String timeLeft = calendarManager.getTimeLeftUntil(nextDay);

        // Apply placeholders
        List<String> parsedLines = lines.stream()
                .map(line -> ChatUtil.format(line
                        .replace("%calendar_name%", calendar.getTitle())
                        .replace("%available_count%", String.valueOf(availableCount))
                        .replace("%day%", String.valueOf(nextDay))
                        .replace("%time_left%", timeLeft)
                )).collect(Collectors.toList());

        // Send the player-specific update
        hologramService.updatePlayerHologram(player, pCal.getId(), parsedLines);
    }

    // TODO: We need to add getNextUnlockableDay(calendar) to CalendarManager
    // TODO: Add `isToday()` to CalendarManager

    private boolean isNearby(Location loc1, Location loc2) {
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
        // Use config range
        double range = plugin.getConfigManager().getConfig().getDouble("physical-calendars.interaction-range-blocks", 10);
        return loc1.distanceSquared(loc2) <= (range * range);
    }

    public void shutdown() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        if (hologramService != null) {
            hologramService.deleteAll();
            hologramService.shutdown();
        }
    }

    // ... (Methods to add/remove/load physical calendars from DB) ...
}