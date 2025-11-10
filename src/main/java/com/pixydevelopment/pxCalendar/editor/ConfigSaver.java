/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.editor;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Utility class to handle saving changes made in the editor back to the .yml files.
 */
public class ConfigSaver {

    private final PxCalendarPlugin plugin;
    private final Calendar calendar;
    private final File calendarFile;
    private final FileConfiguration config;

    public ConfigSaver(PxCalendarPlugin plugin, Calendar calendar) {
        this.plugin = plugin;
        this.calendar = calendar;
        this.calendarFile = new File(plugin.getDataFolder(), "guis/" + calendar.getId() + ".yml");
        this.config = YamlConfiguration.loadConfiguration(calendarFile);
    }

    /**
     * Assigns a specific slot to be a Calendar Day.
     * @param slot The slot being assigned
     * @param day The day number (e.g., 1, 2, 24)
     */
    public void setSlotAsDay(int slot, int day) {
        // 1. Remove this slot from any other definitions
        clearSlot(slot, false); // Don't save yet

        // 2. Set the new day definition
        String path = "days." + day;
        config.set(path + ".slot", slot);

        // Add default values if this is a new day
        if (!config.contains(path + ".reward-bundle-id")) {
            config.set(path + ".reward-bundle-id", "day-" + day); // Default bundle
            config.set(path + ".locked-item", "PLAYER_HEAD:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYjdlYjZkY2VjYjYwZWMxYjM3ZTZlN2Y3M2ZkOGY2N2VmYjZlMjYwZTRlNWQyODVjOWNlYjIzYjY0YjU4YmYifX19");
            config.set(path + ".available-item", "PLAYER_HEAD:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2Y5OWNiN2M5ZmM0ZDU5MGMxYjIyNzA1YjRkZGNlY2FhYjM0MTUwYjFmYTk0YTY1YzI0N2M3NTI2N2ZiNzIifX19");
            config.set(path + ".claimed-item", "PLAYER_HEAD:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmI4YjM3Y2M2ODRkYjYyYjJmYjI2YjE5ZTY5YmU1YjMxN2FmYjQ4ZGFhNTYyYmFkNmU2MjhkZTRkYmU4YjYifX19");
        }

        save();
    }

    /**
     * Sets a specific slot to be a filler pane.
     * @param slot The slot to set
     */
    public void setSlotAsFiller(int slot) {
        clearSlot(slot, false); // Don't save yet

        // Add this slot to the 'filler-pane.slots' list
        String path = "items.filler-pane.slots";
        List<Integer> slots = config.getIntegerList(path);
        if (!slots.contains(slot)) {
            slots.add(slot);
        }
        config.set(path, slots);

        save();
    }

    /**
     * Completely clears a slot, removing it from 'days' and 'items'.
     * @param slot The slot to clear
     * @param autoSave Whether to save immediately
     */
    public void clearSlot(int slot, boolean autoSave) {
        // 1. Check 'days'
        ConfigurationSection daysSection = config.getConfigurationSection("days");
        if (daysSection != null) {
            for (String dayKey : daysSection.getKeys(false)) {
                if (config.getInt("days." + dayKey + ".slot") == slot) {
                    config.set("days." + dayKey, null); // Remove the day definition
                    break;
                }
            }
        }

        // 2. Check 'items' (static items and fillers)
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String itemKey : itemsSection.getKeys(false)) {
                String path = "items." + itemKey;
                if (config.getInt(path + ".slot", -1) == slot) {
                    config.set(path, null); // Remove static item
                }
                if (config.isList(path + ".slots")) {
                    List<Integer> slots = config.getIntegerList(path + ".slots");
                    slots.remove(Integer.valueOf(slot));
                    config.set(path + ".slots", slots);
                }
            }
        }

        if (autoSave) {
            save();
        }
    }

    /**
     * Saves the changes back to the .yml file.
     */
    private void save() {
        try {
            config.save(calendarFile);
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().severe("Failed to save " + calendarFile.getName());
        }

        // Reload GUIs to reflect changes immediately
        plugin.getGuiManager().loadGUIs();
    }
}