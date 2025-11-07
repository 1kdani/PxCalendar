/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.calendar;

import org.bukkit.Location;

/**
 * Data object representing a single physical calendar block in the world.
 */
public class PhysicalCalendar {

    private final String id; // Hash: "world_x_y_z"
    private final Location location;
    private final String calendarId;

    public PhysicalCalendar(Location location, String calendarId) {
        this.location = location;
        this.calendarId = calendarId;
        this.id = getLocationHash(location); // A statikus metódus használata
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public String getCalendarId() {
        return calendarId;
    }

    /**
     * Creates a standardized unique ID string from a Location.
     * @param location The block location
     * @return A hash string (e.g., "world_10_65_-100")
     */
    public static String getLocationHash(Location location) {
        if (location == null || location.getWorld() == null) {
            return "invalid_location";
        }
        return location.getWorld().getName() + "_" +
                location.getBlockX() + "_" +
                location.getBlockY() + "_" +
                location.getBlockZ();
    }
}