/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor.sessions;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.editor.guis.CalendarEditGUI;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

/**
 * Logic for handling the chat input when creating a new calendar.
 */
public class CalendarCreateSession {

    private final PxCalendarPlugin plugin;
    private final Player player;
    private final String input;
    private final LangManager lang;

    public CalendarCreateSession(PxCalendarPlugin plugin, Player player, String input) {
        this.plugin = plugin;
        this.player = player;
        this.input = input.trim(); // Remove whitespace
        this.lang = plugin.getLangManager();
    }

    public void process() {
        if (input.equalsIgnoreCase("cancel")) {
            lang.sendMessage(player, "editor.prompt-cancelled");
            return;
        }

        // Validate the input (no spaces, file-safe)
        if (!input.matches("^[a-zA-Z0-9_]+$")) {
            lang.sendMessage(player, "&cInvalid ID. Use only letters, numbers, and underscores.");
            return;
        }

        String calendarId = input.toLowerCase();

        // Check if file already exists
        File calendarFile = new File(plugin.getDataFolder(), "guis/" + calendarId + ".yml");
        if (calendarFile.exists()) {
            lang.sendMessage(player, "editor.prompt-calendar-exists", "%id%", calendarId);
            return;
        }

        // Create the new file
        try {
            calendarFile.createNewFile();
            FileConfiguration newConfig = YamlConfiguration.loadConfiguration(calendarFile);

            // Set default values for the new calendar
            newConfig.set("gui-title", "&8New Calendar: " + calendarId);
            newConfig.set("rows", 6);
            newConfig.set("permission", "pxcalendar.open." + calendarId);
            newConfig.set("items.filler-pane.material", "BLACK_STAINED_GLASS_PANE");
            newConfig.set("items.filler-pane.name", " ");

            newConfig.save(calendarFile);

        } catch (IOException e) {
            e.printStackTrace();
            lang.sendMessage(player, "messages.error-generic");
            return;
        }

        // Success
        lang.sendMessage(player, "editor.prompt-calendar-created", "%id%", calendarId);

        // Reload GUIs to load the new file, then open the editor for it
        plugin.getGuiManager().loadGUIs();

        // JAVÍTVA: Megnyitja az új naptár szerkesztőjét
        new CalendarEditGUI(plugin, player, plugin.getGuiManager().getCalendar(calendarId)).open();
    }
}