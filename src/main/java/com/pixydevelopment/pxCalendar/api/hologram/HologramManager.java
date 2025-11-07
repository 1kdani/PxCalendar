/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.api.hologram;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.api.hologram.impl.DecentHologramService;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.calendar.PhysicalCalendar; // Importálva
import com.pixydevelopment.pxCalendar.calendar.PlayerCalendarData;
import com.pixydevelopment.pxCalendar.core.utils.ChatUtil; // Importálva
import com.pixydevelopment.pxCalendar.database.DatabaseManager; // Importálva
import com.pixydevelopment.pxCalendar.managers.CalendarManager;
import com.pixydevelopment.pxCalendar.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages the active hologram service and updates physical calendars.
 */
public class HologramManager {

    private final PxCalendarPlugin plugin;
    private final DataManager dataManager;
    private final CalendarManager calendarManager;
    private final DatabaseManager databaseManager; // KIEGÉSZÍTVE
    private HologramService hologramService;
    private BukkitTask updateTask;

    // Map<LocationHash, PhysicalCalendar>
    // This is the cache for all physical calendars on the server.
    private final Map<String, PhysicalCalendar> activeCalendars = new ConcurrentHashMap<>();

    public HologramManager(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.calendarManager = plugin.getCalendarManager();
        this.databaseManager = plugin.getDatabaseManager(); // KIEGÉSZÍTVE

        initializeService();
        loadPhysicalCalendars(); // KIEGÉSZÍTVE: Töltsük be őket indításkor
    }

    private void initializeService() {
        if (!plugin.getConfigManager().getConfig().getBoolean("physical-calendars.enable", true)) {
            plugin.getLogger().info("Physical Calendars are disabled in config.yml.");
            return;
        }

        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            this.hologramService = new DecentHologramService(plugin);
            plugin.getLogger().info("Successfully hooked into DecentHolograms.");
        }
        // TODO: Add HolographicDisplays implementation

        if (this.hologramService == null) {
            plugin.getLogger().warning("Physical Calendars are enabled, but no compatible hologram plugin was found.");
        }
    }

    /**
     * Loads all physical calendars from the database into the cache.
     */
    public void loadPhysicalCalendars() {
        if (hologramService == null) return; // Don't load if holograms are disabled

        activeCalendars.clear();
        // Run async
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, PhysicalCalendar> loaded = databaseManager.loadPhysicalCalendars();
            activeCalendars.putAll(loaded);
            plugin.getLogger().info("Loaded " + activeCalendars.size() + " physical calendars from the database.");

            // Create the hologram entities on the main thread
            Bukkit.getScheduler().runTask(plugin, this::createAllHolograms);
        });
    }

    /**
     * Creates the hologram entities in the world (called after loading)
     */
    private void createAllHolograms() {
        if (hologramService == null) return;

        List<String> defaultLines = List.of(ChatUtil.format("&7Loading..."));
        for (PhysicalCalendar pCal : activeCalendars.values()) {
            // Add +0.5, +2.0, +0.5 to center the hologram above the block
            Location holoLoc = pCal.getLocation().clone().add(0.5, 2.0, 0.5);
            hologramService.createHologram(pCal.getId(), holoLoc, defaultLines);
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
            lines = plugin.getLangManager().getLangConfig().getStringList("holograms.available");
        } else {
            // Check if they claimed everything that IS available
            boolean allClaimed = calendar.getDaysByDayNumber().values().stream()
                    .noneMatch(day -> calendarManager.isDayAvailable(day.getDay()) && !data.hasClaimed(calendar.getId(), day.getDay()));

            if (allClaimed) {
                lines = plugin.getLangManager().getLangConfig().getStringList("holograms.all-claimed");
            } else {
                lines = plugin.getLangManager().getLangConfig().getStringList("holograms.locked");
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

        hologramService.updatePlayerHologram(player, pCal.getId(), parsedLines);
    }

    private boolean isNearby(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null || loc1.getWorld() == null || loc2.getWorld() == null) {
            return false;
        }
        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }
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

    // --- HELPER METHODS FOR COMMANDS/LISTENERS ---

    /**
     * Gets a physical calendar from the cache.
     */
    public PhysicalCalendar getPhysicalCalendar(String locationHash) {
        return activeCalendars.get(locationHash);
    }

    /**
     * Adds a new physical calendar to the cache, database, and world.
     */
    public void addPhysicalCalendar(PhysicalCalendar pCal) {
        activeCalendars.put(pCal.getId(), pCal);

        if (hologramService != null) {
            Location holoLoc = pCal.getLocation().clone().add(0.5, 2.0, 0.5);
            List<String> defaultLines = plugin.getLangManager().getLangConfig().getStringList("holograms.locked");
            hologramService.createHologram(pCal.getId(), holoLoc, defaultLines);
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            databaseManager.savePhysicalCalendar(pCal);
        });
    }

    /**
     * Removes a physical calendar from the cache, database, and world.
     */
    public void removePhysicalCalendar(PhysicalCalendar pCal) {
        activeCalendars.remove(pCal.getId());

        if (hologramService != null) {
            hologramService.deleteHologram(pCal.getId());
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            databaseManager.deletePhysicalCalendar(pCal.getId());
        });
    }
}