/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.managers;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Manages all time-related logic for the calendars.
 * Handles timezones, start dates, and day availability.
 */
public class CalendarManager {

    private final PxCalendarPlugin plugin;
    private ZoneId serverZoneId;
    private LocalDate calendarStartDate;
    private DateTimeFormatter timeFormatter;

    public CalendarManager(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        loadConfigValues();
    }

    /**
     * Loads and validates the time settings from the config.yml.
     */
    public void loadConfigValues() {
        String timeZone = plugin.getConfigManager().getConfig().getString("calendar.timezone", "UTC");
        try {
            this.serverZoneId = ZoneId.of(timeZone);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid TimeZone '" + timeZone + "' in config.yml. Defaulting to UTC.");
            this.serverZoneId = ZoneId.of("UTC");
        }

        String startDate = plugin.getConfigManager().getConfig().getString("calendar.start-date", "2025-12-01");
        try {
            this.calendarStartDate = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            plugin.getLogger().severe("Invalid start-date '" + startDate + "' in config.yml. It MUST be in YYYY-MM-DD format.");
            this.calendarStartDate = LocalDate.of(2025, 12, 1);
        }

        String timeFormat = plugin.getConfigManager().getConfig().getString("calendar.time-format", "dd:HH:mm:ss");
        try {
            this.timeFormatter = DateTimeFormatter.ofPattern(timeFormat);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid time-format '" + timeFormat + "' in config.yml. Defaulting to dd:HH:mm:ss.");
            this.timeFormatter = DateTimeFormatter.ofPattern("dd:HH:mm:ss");
        }

        plugin.getLogger().info("Calendar time configured. Zone: " + serverZoneId + ", Start Date: " + calendarStartDate);
    }

    /**
     * Gets the current time in the configured server timezone.
     * @return The current ZonedDateTime.
     */
    public ZonedDateTime getNow() {
        return ZonedDateTime.now(this.serverZoneId);
    }

    /**
     * Gets the exact date and time when a specific calendar day becomes available.
     * @param day The day number (e.g., 1 for the first day)
     * @return The ZonedDateTime when this day unlocks.
     */
    public ZonedDateTime getDayUnlockTime(int day) {
        // A day unlocks at the *start* of that day (00:00).
        // Day 1 unlocks on calendarStartDate.
        // Day 2 unlocks on calendarStartDate + 1 day.
        LocalDate unlockDate = this.calendarStartDate.plusDays(day - 1);
        return unlockDate.atStartOfDay(this.serverZoneId);
    }

    /**
     * Checks if a specific calendar day is currently available to be claimed.
     * @param day The day number (e.g., 1)
     * @return true if the current time is *after* the unlock time.
     */
    public boolean isDayAvailable(int day) {
        ZonedDateTime unlockTime = getDayUnlockTime(day);
        return getNow().isAfter(unlockTime);
    }

    /**
     * Gets the time remaining until a specific day unlocks.
     * @param day The day number
     * @return A formatted string (e.g., "01:10:30:05"), or "00:00:00:00" if it's already unlocked.
     */
    public String getTimeLeftUntil(int day) {
        ZonedDateTime unlockTime = getDayUnlockTime(day);
        ZonedDateTime now = getNow();

        if (now.isAfter(unlockTime)) {
            return "00:00:00:00"; // Already unlocked
        }

        // This is complex because DateTimeFormatter doesn't format "durations" well.
        // We have to manually calculate the parts.
        long totalSeconds = ChronoUnit.SECONDS.between(now, unlockTime);

        long days = totalSeconds / (24 * 3600);
        totalSeconds %= (24 * 3600);
        long hours = totalSeconds / 3600;
        totalSeconds %= 3600;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        // We cheat and use LocalDateTime to format this duration
        // We start at an arbitrary "zero" point and add the duration
        LocalDateTime "durationTime" = LocalDateTime.MIN.plusDays(days).plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);

        try {
            return timeFormatter.format(durationTime);
        } catch (Exception e) {
            // Fallback in case the user's format is bad
            return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        }
    }

    /**
     * Checks if a specific day (relative to the start date) is today.
     * @param day The day number (e.g., 1)
     * @return true if it is today.
     */
    public boolean isToday(int day) {
        LocalDate unlockDate = getDayUnlockTime(day).toLocalDate();
        LocalDate now = getNow().toLocalDate();
        return unlockDate.isEqual(now);
    }

    /**
     * Finds the next day that hasn't unlocked yet for a calendar.
     * Used by holograms.
     * @param calendar The Calendar object
     * @return The day number (e.g., 1, 2, ...)
     */
    public int getNextUnlockableDay(com.pixydevelopment.pxCalendar.calendar.Calendar calendar) {
        ZonedDateTime now = getNow();

        // Find the smallest day number that is *after* the current time
        return calendar.getDaysByDayNumber().keySet().stream()
                .mapToInt(Integer::intValue)
                .filter(day -> getDayUnlockTime(day).isAfter(now))
                .min()
                .orElse(25); // Default to 25 if all are unlocked
    }
}