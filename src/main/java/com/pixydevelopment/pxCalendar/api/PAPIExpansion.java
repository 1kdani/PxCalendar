/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.api;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.calendar.PlayerCalendarData;
import com.pixydevelopment.pxCalendar.gui.GUIManager;
import com.pixydevelopment.pxCalendar.managers.CalendarManager;
import com.pixydevelopment.pxCalendar.managers.DataManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Handles all PlaceholderAPI placeholders for PxCalendar.
 */
public class PAPIExpansion extends PlaceholderExpansion {

    private final PxCalendarPlugin plugin;
    private final DataManager dataManager;
    private final CalendarManager calendarManager;
    private final GUIManager guiManager;

    public PAPIExpansion(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.calendarManager = plugin.getCalendarManager();
        this.guiManager = plugin.getGuiManager();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "pxcalendar"; // %pxcalendar_...%
    }

    @Override
    public @NotNull String getAuthor() {
        return "PixyDevelopment";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true; // Register this hook permanently
    }

    /**
     * Handles the placeholder request.
     * @param player The player
     * @param identifier The placeholder parameters
     * @return The parsed value
     */
    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        String[] args = identifier.split("_");

        // %pxcalendar_status_<calendarId>_<day>%
        if (args.length == 3 && args[0].equalsIgnoreCase("status")) {
            String calendarId = args[1];
            int day;
            try {
                day = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                return "Invalid Day";
            }

            PlayerCalendarData data = dataManager.getPlayerData(player);
            if (data == null) return "LOADING";

            if (data.hasClaimed(calendarId, day)) {
                return "CLAIMED";
            } else if (calendarManager.isDayAvailable(day)) {
                return "AVAILABLE";
            } else {
                return "LOCKED";
            }
        }

        // %pxcalendar_timeleft_global_<day>%
        if (args.length == 2 && args[0].equalsIgnoreCase("timeleft") && args[1].equalsIgnoreCase("global")) {
            int day;
            try {
                day = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                return "Invalid Day";
            }
            return calendarManager.getTimeLeftUntil(day);
        }

        // %pxcalendar_claimed_total_<calendarId>%
        if (args.length == 3 && args[0].equalsIgnoreCase("claimed") && args[1].equalsIgnoreCase("total")) {
            String calendarId = args[2];
            PlayerCalendarData data = dataManager.getPlayerData(player);
            if (data == null) return "0";
            return String.valueOf(data.getClaims(calendarId).size());
        }

        // %pxcalendar_available_total_<calendarId>%
        if (args.length == 3 && args[0].equalsIgnoreCase("available") && args[1].equalsIgnoreCase("total")) {
            String calendarId = args[2];
            Calendar calendar = guiManager.getCalendar(calendarId);
            if (calendar == null) return "Invalid Calendar";

            PlayerCalendarData data = dataManager.getPlayerData(player);
            if (data == null) return "0";

            int availableCount = calendar.getAvailableUnclaimedCount(player); // We need to create this method
            return String.valueOf(availableCount);
        }

        return "Invalid Placeholder";
    }
}