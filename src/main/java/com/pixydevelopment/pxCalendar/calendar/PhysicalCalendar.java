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
        this.id = location.getWorld().getName() + "_" +
                location.getBlockX() + "_" +
                location.getBlockY() + "_" +
                location.getBlockZ();
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
}