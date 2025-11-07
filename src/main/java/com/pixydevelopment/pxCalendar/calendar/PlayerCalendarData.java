/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.calendar;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * A data object that holds the calendar progress for a single player.
 * This is cached in the DataManager.
 */
public class PlayerCalendarData {

    private final UUID playerUUID;
    // Map<CalendarID, Set<DayNumber>>
    private final Map<String, Set<Integer>> claimedDays;

    public PlayerCalendarData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.claimedDays = new HashMap<>();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    /**
     * Checks if a player has claimed a specific day in a specific calendar.
     * @param calendarId The ID of the calendar (e.g., "example1")
     * @param day The day number
     * @return true if already claimed, false otherwise.
     */
    public boolean hasClaimed(String calendarId, int day) {
        return claimedDays.getOrDefault(calendarId.toLowerCase(), Set.of()).contains(day);
    }

    /**
     * Marks a day as claimed for this player.
     * @param calendarId The ID of the calendar
     * @param day The day number
     */
    public void addClaim(String calendarId, int day) {
        this.claimedDays
                .computeIfAbsent(calendarId.toLowerCase(), k -> new HashSet<>())
                .add(day);
    }

    /**
     * Gets all claimed days for a specific calendar.
     * @param calendarId The ID of the calendar
     * @return A Set of day numbers, or an empty set.
     */
    public Set<Integer> getClaims(String calendarId) {
        return this.claimedDays.getOrDefault(calendarId.toLowerCase(), Set.of());
    }

    /**
     * Used by the DatabaseManager to populate this object on login.
     * @param calendarId The ID of the calendar
     * @param day The day number
     */
    public void addClaimFromDB(String calendarId, int day) {
        // Same as addClaim, but separated for clarity during loading
        addClaim(calendarId, day);
    }

    /**
     * Clears all claim data for this player (used by /pxc reset).
     */
    public void clearAllClaims() {
        this.claimedDays.clear();
    }
}