/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.gui;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.calendar.CalendarDay;
import com.pixydevelopment.pxCalendar.calendar.PlayerCalendarData;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.core.utils.ChatUtil;
import com.pixydevelopment.pxCalendar.managers.CalendarManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles the creation and dynamic update of a specific Calendar inventory.
 */
public class CalendarGUI {

    private final PxCalendarPlugin plugin;
    private final Player player;
    private final Calendar calendar;
    private final PlayerCalendarData playerData;
    private final CalendarManager calendarManager;
    private final LangManager lang;

    private Inventory inventory;

    public CalendarGUI(PxCalendarPlugin plugin, Player player, Calendar calendar, PlayerCalendarData playerData) {
        this.plugin = plugin;
        this.player = player;
        this.calendar = calendar;
        this.playerData = playerData;
        this.calendarManager = plugin.getCalendarManager();
        this.lang = plugin.getLangManager();
    }

    /**
     * Creates and opens the inventory for the player.
     */
    public void open() {
        this.inventory = Bukkit.createInventory(null, calendar.getRows() * 9, calendar.getTitle());

        // 1. Fill with static items (fillers)
        for (Map.Entry<Integer, ItemStack> entry : calendar.getStaticItems().entrySet()) {
            inventory.setItem(entry.getKey(), entry.getValue());
        }

        // 2. Fill with dynamic day items
        for (Map.Entry<Integer, CalendarDay> entry : calendar.getDaysBySlot().entrySet()) {
            int slot = entry.getKey();
            CalendarDay day = entry.getValue();

            // Determine the state of the day
            ItemStack displayItem;
            if (playerData.hasClaimed(calendar.getId(), day.getDay())) {
                // CLAIMED
                displayItem = day.getClaimedItem();
                applyPlaceholders(displayItem, day, "calendar.claimed");
            } else if (calendarManager.isDayAvailable(day.getDay())) {
                // AVAILABLE
                displayItem = day.getAvailableItem();
                applyPlaceholders(displayItem, day, "calendar.available");
            } else {
                // LOCKED
                displayItem = day.getLockedItem();
                applyPlaceholders(displayItem, day, "calendar.locked");
            }

            inventory.setItem(slot, displayItem);
        }

        player.openInventory(inventory);
        playSound("gui-open");
    }

    /**
     * Applies placeholders like %day% and lore from the lang file to an item.
     */
    private void applyPlaceholders(ItemStack item, CalendarDay day, String langPath) {
        if (item == null || item.getItemMeta() == null) return;

        ItemMeta meta = item.getItemMeta();

        // Apply placeholders to Name
        if (meta.hasDisplayName()) {
            meta.setDisplayName(meta.getDisplayName()
                    .replace("%day%", String.valueOf(day.getDay()))
                    .replace("%time_left%", calendarManager.getTimeLeftUntil(day.getDay()))
            );
        }

        // Apply placeholders to Lore and add lang.yml lore
        List<String> newLore = new ArrayList<>();
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                newLore.add(ChatUtil.format(line
                        .replace("%day%", String.valueOf(day.getDay()))
                        .replace("%time_left%", calendarManager.getTimeLeftUntil(day.getDay()))
                ));
            }
        }

        // Add lore from lang.yml
        String langLore = lang.getMessage(langPath);
        if (!langLore.isEmpty()) {
            newLore.add(langLore
                    .replace("%day%", String.valueOf(day.getDay()))
                    .replace("%time_left%", calendarManager.getTimeLeftUntil(day.getDay()))
            );
        }

        meta.setLore(newLore);
        item.setItemMeta(meta);
    }

    public void playSound(String soundKey) {
        try {
            String soundName = plugin.getConfigManager().getConfig().getString("sounds." + soundKey, "UI_BUTTON_CLICK");
            if (!soundName.equalsIgnoreCase("NONE")) {
                player.playSound(player.getLocation(), Sound.valueOf(soundName.toUpperCase()), 1.0f, 1.0f);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid sound name in config.yml for: " + soundKey);
        }
    }
}