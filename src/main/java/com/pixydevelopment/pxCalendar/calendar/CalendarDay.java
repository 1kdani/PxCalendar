/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.calendar;

import com.pixydevelopment.pxCalendar.core.utils.ItemBuilder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a single configurable day (slot) within a Calendar GUI.
 * This is a data-holder loaded from guis/ config files.
 */
public class CalendarDay {

    private final int day;
    private final int slot;
    private final String rewardBundleId;
    private final ItemStack lockedItem;
    private final ItemStack availableItem;
    private final ItemStack claimedItem;

    public CalendarDay(int day, ConfigurationSection config) {
        this.day = day;
        this.slot = config.getInt("slot", 0);
        this.rewardBundleId = config.getString("reward-bundle-id");

        // Use our ItemBuilder to parse the item strings
        this.lockedItem = ItemBuilder.fromConfigString(config.getString("locked-item"));
        this.availableItem = ItemBuilder.fromConfigString(config.getString("available-item"));
        this.claimedItem = ItemBuilder.fromConfigString(config.getString("claimed-item"));
    }

    // Getters
    public int getDay() { return day; }
    public int getSlot() { return slot; }
    public String getRewardBundleId() { return rewardBundleId; }
    public ItemStack getLockedItem() { return lockedItem.clone(); } // Clone to prevent meta modification
    public ItemStack getAvailableItem() { return availableItem.clone(); }
    public ItemStack getClaimedItem() { return claimedItem.clone(); }
}