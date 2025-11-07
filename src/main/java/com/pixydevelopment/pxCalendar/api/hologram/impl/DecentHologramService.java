/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.api.hologram.impl;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.api.hologram.HologramService;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * HologramService implementation for DecentHolograms.
 */
public class DecentHologramService implements HologramService {

    private final String HOLOGRAM_PREFIX = "pxcalendar-";

    public DecentHologramService(PxCalendarPlugin plugin) {
        // Constructor
    }

    @Override
    public void createHologram(String id, Location location, List<String> lines) {
        // DecentHolograms updates per-player by default, so we just create a base hologram
        Hologram hologram = DHAPI.getHologram(HOLOGRAM_PREFIX + id);
        if (hologram == null) {
            DHAPI.createHologram(HOLOGRAM_PREFIX + id, location, lines);
        }
    }

    @Override
    public void updateHologram(String id, List<String> newLines) {
        Hologram hologram = DHAPI.getHologram(HOLOGRAM_PREFIX + id);
        if (hologram != null) {
            DHAPI.setHologramLines(hologram, newLines);
        }
    }

    @Override
    public void deleteHologram(String id) {
        Hologram hologram = DHAPI.getHologram(HOLOGRAM_PREFIX + id);
        if (hologram != null) {
            hologram.delete();
        }
    }

    @Override
    public void deleteAll() {
        for (Hologram hologram : DHAPI.getHolograms()) {
            if (hologram.getName().startsWith(HOLOGRAM_PREFIX)) {
                hologram.delete();
            }
        }
    }

    @Override
    public void updatePlayerHologram(Player player, String id, List<String> newLines) {
        Hologram hologram = DHAPI.getHologram(HOLOGRAM_PREFIX + id);
        if (hologram != null) {
            // This is how DecentHolograms updates for a single player
            DHAPI.setHologramLines(hologram, player, newLines);
        }
    }

    @Override
    public void shutdown() {
        // DecentHolograms handles its own shutdown
    }
}