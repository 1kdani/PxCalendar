/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.managers;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.PlayerCalendarData;
import com.pixydevelopment.pxCalendar.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages the caching of PlayerCalendarData.
 * Loads data from DB on join, saves to DB on claim, and removes from cache on quit.
 */
public class DataManager {

    private final PxCalendarPlugin plugin;
    private final DatabaseManager dbManager;
    private final Map<UUID, PlayerCalendarData> playerDataCache;

    public DataManager() {
        this.plugin = PxCalendarPlugin.getInstance();
        this.dbManager = plugin.getDatabaseManager();
        this.playerDataCache = new ConcurrentHashMap<>();
    }

    /**
     * Loads player data from the database asynchronously and adds it to the cache.
     * @param player The player joining.
     */
    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();

        // Don't load if already cached
        if (playerDataCache.containsKey(uuid)) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            PlayerCalendarData data = new PlayerCalendarData(uuid);

            try (Connection conn = dbManager.getConnection();
                 PreparedStatement ps = dbManager.getPlayerDataStatement(conn, uuid);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String calendarId = rs.getString("calendar_id");
                    int day = rs.getInt("day_claimed");
                    data.addClaimFromDB(calendarId, day);
                }

                // Add to cache, even if they have no data
                playerDataCache.put(uuid, data);
                plugin.getLogger().info("Loaded calendar data for " + player.getName());

            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load player data for " + player.getName(), e);
            }
        });
    }

    /**
     * Removes a player's data from the cache when they quit.
     * @param player The player quitting.
     */
    public void unloadPlayerData(Player player) {
        playerDataCache.remove(player.getUniqueId());
        plugin.getLogger().info("Unloaded calendar data for " + player.getName());
    }

    /**
     * Gets the cached PlayerCalendarData for an online player.
     * @param player The player
     * @return The PlayerCalendarData, or null if not loaded yet.
     */
    public PlayerCalendarData getPlayerData(Player player) {
        return playerDataCache.get(player.getUniqueId());
    }

    /**
     * Saves a new claim. This updates both the cache and the database.
     * @param player The player
     * @param calendarId The calendar ID
     * @param day The day number
     */
    public void saveClaim(Player player, String calendarId, int day) {
        // 1. Update cache
        PlayerCalendarData data = getPlayerData(player);
        if (data != null) {
            data.addClaim(calendarId, day);
        } else {
            // This shouldn't happen if they are online, but just in case
            plugin.getLogger().warning("Player data for " + player.getName() + " was not in cache during claim!");
            return;
        }

        // 2. Save to database asynchronously
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "UNKNOWN";
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            dbManager.savePlayerClaim(player.getUniqueId(), calendarId, day, ip);
        });
    }

    /**
     * Resets a player's data (both in cache and DB).
     * @param uuid The UUID of the player to reset.
     */
    public void resetPlayer(UUID uuid) {
        // 1. Clear from cache if online
        if (playerDataCache.containsKey(uuid)) {
            playerDataCache.get(uuid).clearAllClaims();
        }

        // 2. Clear from DB asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            dbManager.resetPlayerData(uuid);
        });
    }
}