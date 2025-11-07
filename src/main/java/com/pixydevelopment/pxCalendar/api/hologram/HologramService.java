/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.api.hologram;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface for interacting with different hologram plugins (DecentHolograms, etc.)
 */
public interface HologramService {

    /**
     * Creates a new hologram at a location.
     * @param id A unique ID for this hologram (e.g., world_x_y_z)
     * @param location The location to spawn the hologram
     * @param lines The text lines (already color-formatted)
     */
    void createHologram(String id, Location location, List<String> lines);

    /**
     * Updates an existing hologram with new lines.
     * @param id The unique ID of the hologram
     * @param newLines The new text lines
     */
    void updateHologram(String id, List<String> newLines);

    /**
     * Deletes a hologram.
     * @param id The unique ID of the hologram
     */
    void deleteHologram(String id);

    /**
     * Deletes all holograms created by this plugin.
     */
    void deleteAll();

    /**
     * Updates a hologram specifically for one player (if supported).
     * @param player The player to update for
     * @param id The unique ID of the hologram
     * @param newLines The new text lines
     */
    void updatePlayerHologram(Player player, String id, List<String> newLines);

    /**
     * Cleans up any resources.
     */
    void shutdown();
}