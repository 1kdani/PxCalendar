/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.listeners;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.api.hologram.HologramManager;
import com.pixydevelopment.pxCalendar.calendar.PhysicalCalendar;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Listens for clicks on physical calendar blocks.
 */
public class PhysicalCalendarListener implements Listener {

    private final PxCalendarPlugin plugin;
    private final HologramManager hologramManager;

    public PhysicalCalendarListener(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.hologramManager = plugin.getHologramManager();
    }

    @EventHandler
    public void onBlockClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) {
            return;
        }

        if (hologramManager == null) return; // Disabled

        Location loc = event.getClickedBlock().getLocation();
        // HELPER METÓDUS JAVÍTVA
        String locationHash = PhysicalCalendar.getLocationHash(loc);

        // HELPER METÓDUS JAVÍTVA
        PhysicalCalendar pCal = hologramManager.getPhysicalCalendar(locationHash);

        if (pCal != null) {
            // This is a calendar block!
            event.setCancelled(true);

            // Open the GUI
            plugin.getGuiManager().openCalendar(event.getPlayer(), pCal.getCalendarId());
        }
    }
}