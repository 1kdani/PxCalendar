/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.gui;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.managers.DataManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages loading, validating, and opening all Calendar GUIs.
 */
public class GUIManager {

    private final PxCalendarPlugin plugin;
    private final DataManager dataManager;
    // Map<CalendarID, CalendarObject>
    private final Map<String, Calendar> loadedCalendars;

    public GUIManager(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.dataManager = plugin.getDataManager();
        this.loadedCalendars = new HashMap<>();
    }

    /**
     * Loads/reloads all .yml files from the /guis/ folder.
     */
    public void loadGUIs() {
        loadedCalendars.clear();
        File guiFolder = new File(plugin.getDataFolder(), "guis");
        if (!guiFolder.exists()) {
            guiFolder.mkdirs();
            // Save the default examples if the folder is new
            plugin.saveResource("guis/example1.yml", false);
            plugin.saveResource("guis/example2.yml", false);
        }

        File[] guiFiles = guiFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (guiFiles == null) {
            plugin.getLogger().severe("Could not read /guis/ folder!");
            return;
        }

        for (File file : guiFiles) {
            String calendarId = file.getName().replace(".yml", "");
            try {
                FileConfiguration guiConfig = YamlConfiguration.loadConfiguration(file);

                // Create and load the calendar object
                Calendar calendar = new Calendar(calendarId, guiConfig);

                loadedCalendars.put(calendarId.toLowerCase(), calendar);
                plugin.getLogger().info("Loaded calendar GUI: " + calendarId);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load GUI: " + file.getName(), e);
            }
        }
    }

    /**
     * Gets a loaded calendar by its ID.
     * @param id The ID (e.g., "example1")
     * @return The Calendar object, or null if not found.
     */
    public Calendar getCalendar(String id) {
        if (id == null) return null;
        return loadedCalendars.get(id.toLowerCase());
    }

    /**
     * Gets all loaded calendars.
     * @return A collection of all Calendar objects.
     */
    public Collection<Calendar> getLoadedCalendars() {
        return loadedCalendars.values();
    }

    /**
     * Opens a calendar GUI for a player.
     * This will be the main logic for displaying the GUI.
     * @param player The player
     * @param calendarId The ID of the calendar to open
     */
    public void openCalendar(Player player, String calendarId) {
        Calendar calendar = getCalendar(calendarId);
        if (calendar == null) {
            plugin.getLangManager().sendMessage(player, "messages.calendar-not-found", "%id%", calendarId);
            return;
        }

        // Check permission
        if (!player.hasPermission(calendar.getPermission())) {
            plugin.getLangManager().sendMessage(player, "messages.no-permission");
            return;
        }

        // Get the player's data
        var playerData = dataManager.getPlayerData(player);
        if (playerData == null) {
            // This can happen if the player just logged in and data hasn't loaded
            plugin.getLangManager().sendMessage(player, "&cYour data is still loading, please wait a moment and try again.");
            return;
        }

        // Create and open the dynamic GUI
        // We will create this class in the NEXT step.
        // new CalendarGUI(plugin, player, calendar, playerData).open();

        // TODO: This is the next class to write.
        player.sendMessage("DEBUG: Opening calendar " + calendar.getTitle());
    }
}