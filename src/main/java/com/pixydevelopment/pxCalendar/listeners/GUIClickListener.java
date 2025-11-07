/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.listeners;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.calendar.CalendarDay;
import com.pixydevelopment.pxCalendar.calendar.PlayerCalendarData;
import com.pixydevelopment.pxCalendar.calendar.RewardBundle;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.gui.GUIManager;
import com.pixydevelopment.pxCalendar.managers.CalendarManager;
import com.pixydevelopment.pxCalendar.managers.DataManager;
import com.pixydevelopment.pxCalendar.managers.RequirementChecker;
import com.pixydevelopment.pxCalendar.managers.RewardExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Handles all clicks inside PxCalendar GUIs.
 */
public class GUIClickListener implements Listener {

    private final PxCalendarPlugin plugin;
    private final GUIManager guiManager;
    private final DataManager dataManager;
    private final CalendarManager calendarManager;
    private final RequirementChecker requirementChecker;
    private final RewardExecutor rewardExecutor;
    private final LangManager lang;

    public GUIClickListener(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.guiManager = plugin.getGuiManager();
        this.dataManager = plugin.getDataManager();
        this.calendarManager = plugin.getCalendarManager();
        this.lang = plugin.getLangManager();

        // Initialize the reward helpers
        this.requirementChecker = new RequirementChecker(plugin);
        this.rewardExecutor = new RewardExecutor(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        // Check if the clicked inventory is a calendar GUI
        Calendar clickedCalendar = null;
        for (Calendar calendar : guiManager.getLoadedCalendars()) {
            if (event.getView().getTitle().equals(calendar.getTitle())) {
                clickedCalendar = calendar;
                break;
            }
        }

        // Not our GUI, do nothing
        if (clickedCalendar == null) {
            return;
        }

        // Player clicked *inside* our GUI, cancel the event
        event.setCancelled(true);

        if (event.getClickedInventory() == null || event.getCurrentItem() == null) {
            return;
        }

        // Check if they clicked an "exit" button (defined in guis/example1.yml)
        if (event.getSlot() == 40) { // TODO: Make this dynamic from config
            player.closeInventory();
            return;
        }

        // Check if they clicked a calendar day
        CalendarDay clickedDay = clickedCalendar.getDaysBySlot().get(event.getSlot());
        if (clickedDay == null) {
            return; // Clicked a filler pane
        }

        PlayerCalendarData playerData = dataManager.getPlayerData(player);
        if (playerData == null) return; // Data not loaded

        // --- Handle Day Click Logic ---

        // 1. Check if already claimed
        if (playerData.hasClaimed(clickedCalendar.getId(), clickedDay.getDay())) {
            lang.sendMessage(player, "calendar.claimed");
            playSound(player, "claim-locked");
            return;
        }

        // 2. Check if available
        if (calendarManager.isDayAvailable(clickedDay.getDay())) {

            // 3. Check for late claims
            boolean isLate = !calendarManager.isToday(clickedDay.getDay()); // We need to add this method to CalendarManager
            boolean allowLateClaims = plugin.getConfigManager().getConfig().getBoolean("calendar.allow-late-claims", true);

            if (isLate && !allowLateClaims && !player.hasPermission("pxcalendar.claim.late")) {
                lang.sendMessage(player, "calendar.late-claim-disabled");
                playSound(player, "claim-fail");
                return;
            }

            // 4. Check requirements
            RewardBundle bundle = plugin.getRewardManager().getBundle(clickedDay.getRewardBundleId());
            if (bundle == null) {
                player.sendMessage(lang.getMessage("messages.error-generic")); // Add this
                plugin.getLogger().severe("Could not find reward bundle: " + clickedDay.getRewardBundleId());
                return;
            }

            if (!requirementChecker.checkRequirements(player, bundle.getRequirements())) {
                playSound(player, "claim-fail");
                return; // RequirementChecker sends its own message
            }

            // --- ALL CHECKS PASSED ---

            // 5. Execute reward
            rewardExecutor.execute(player, bundle);

            // 6. Save data
            dataManager.saveClaim(player, clickedCalendar.getId(), clickedDay.getDay());

            // 7. Success
            playSound(player, "claim-success");
            player.closeInventory(); // Close or refresh? Closing is safer.
            // You could also call 'new CalendarGUI(...).open()' to refresh it.

        } else {
            // Day is LOCKED
            lang.sendMessage(player, "calendar.locked",
                    "%day%", String.valueOf(clickedDay.getDay()),
                    "%time_left%", calendarManager.getTimeLeftUntil(clickedDay.getDay())
            );
            playSound(player, "claim-locked");
        }
    }

    private void playSound(Player player, String soundKey) {
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