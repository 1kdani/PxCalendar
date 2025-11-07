/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.calendar;

import com.pixydevelopment.pxCalendar.core.utils.ChatUtil;
import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a fully loaded Calendar GUI configuration from a .yml file.
 */
public class Calendar {

    private final String id;
    private final String title;
    private final int rows;
    private final String permission;

    // Static items (fillers, exit button, etc.)
    private final Map<Integer, ItemStack> staticItems;
    // Map<Slot, DayObject>
    private final Map<Integer, CalendarDay> daysBySlot;
    // Map<DayNumber, DayObject>
    private final Map<Integer, CalendarDay> daysByDayNumber;

    private ItemStack hoverItem;

    public Calendar(String id, FileConfiguration config) {
        this.id = id;
        this.title = ChatUtil.format(config.getString("gui-title", "&cInvalid Title"));
        this.rows = config.getInt("rows", 6);
        this.permission = config.getString("permission", "pxcalendar.open." + id);

        this.staticItems = new HashMap<>();
        this.daysBySlot = new HashMap<>();
        this.daysByDayNumber = new HashMap<>();

        loadItems(config);
        loadDays(config);
    }

    /**
     * Loads all static items from the 'items' section.
     */
    private void loadItems(FileConfiguration config) {
        ConfigurationSection itemsSection = config.getConfigurationSection("items");
        if (itemsSection == null) return;

        // Load hover item
        if (itemsSection.contains("hover-item")) {
            this.hoverItem = parseItemFromSection(itemsSection.getConfigurationSection("hover-item"));
        }

        // Load filler and other static items
        for (String key : itemsSection.getKeys(false)) {
            if (key.equals("hover-item")) continue; // Skip hover item

            ConfigurationSection itemConfig = itemsSection.getConfigurationSection(key);
            ItemStack item = parseItemFromSection(itemConfig);
            if (item == null) continue;

            if (itemConfig.contains("slot")) {
                // Single slot
                staticItems.put(itemConfig.getInt("slot"), item);
            } else if (itemConfig.contains("slots")) {
                // List of slots
                for (int slot : itemConfig.getIntegerList("slots")) {
                    staticItems.put(slot, item);
                }
            } else if (key.equals("filler-pane")) {
                // Special key for filler
                fillEmptySlots(item);
            }
        }
    }

    /**
     * Helper to parse an item from a config section (name, lore, texture, etc.)
     */
    private ItemStack parseItemFromSection(ConfigurationSection config) {
        if (config == null) return null;
        try {
            Material material = Material.matchMaterial(config.getString("material", "STONE"));
            ItemBuilder builder = new ItemBuilder(material);

            builder.name(config.getString("name", " "));
            builder.lore(config.getStringList("lore"));
            if (config.contains("texture")) {
                builder.texture(config.getString("texture"));
            }
            return builder.build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Pre-fills the staticItems map with the filler item.
     */
    private void fillEmptySlots(ItemStack filler) {
        for (int i = 0; i < this.rows * 9; i++) {
            staticItems.put(i, filler);
        }
    }

    /**
     * Loads all day definitions from the 'days' section.
     */
    private void loadDays(FileConfiguration config) {
        ConfigurationSection daysSection = config.getConfigurationSection("days");
        if (daysSection == null) return;

        for (String dayKey : daysSection.getKeys(false)) {
            try {
                int dayNumber = Integer.parseInt(dayKey);
                ConfigurationSection dayConfig = daysSection.getConfigurationSection(dayKey);

                CalendarDay calendarDay = new CalendarDay(dayNumber, dayConfig);

                daysByDayNumber.put(dayNumber, calendarDay);
                daysBySlot.put(calendarDay.getSlot(), calendarDay);

            } catch (NumberFormatException e) {
                System.err.println("[PxCalendar] Invalid day number: " + dayKey + " in " + id + ".yml");
            } catch (Exception e) {
                System.err.println("[PxCalendar] Failed to load day: " + dayKey + " in " + id + ".yml");
                e.printStackTrace();
            }
        }
    }

    // Getters for the GUIManager
    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getRows() { return rows; }
    public String getPermission() { return permission; }
    public Map<Integer, ItemStack> getStaticItems() { return staticItems; }
    public Map<Integer, CalendarDay> getDaysBySlot() { return daysBySlot; }
    public Map<Integer, CalendarDay> getDaysByDayNumber() { return daysByDayNumber; }
    public ItemStack getHoverItem() { return hoverItem; }

    /**
     * Helper method for PAPI.
     * Counts how many days are AVAILABLE but NOT_CLAIMED by this player.
     * @param player The player
     * @return The count of available rewards.
     */
    public int getAvailableUnclaimedCount(Player player) {
        PlayerCalendarData data = PxCalendarPlugin.getInstance().getDataManager().getPlayerData(player);
        if (data == null) return 0;

        CalendarManager cm = PxCalendarPlugin.getInstance().getCalendarManager();
        int count = 0;

        for (CalendarDay day : daysByDayNumber.values()) {
            // Check if (AVAILABLE) and (NOT CLAIMED)
            if (cm.isDayAvailable(day.getDay()) && !data.hasClaimed(this.id, day.getDay())) {
                count++;
            }
        }
        return count;
    }
}