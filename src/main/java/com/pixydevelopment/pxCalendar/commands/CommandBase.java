/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.commands;

import org.bukkit.command.CommandSender;

/**
 * Interface for all sub-commands.
 */
public interface CommandBase {

    /**
     * Executes the sub-command.
     * @param sender The person who ran the command.
     * @param args The arguments (e.g., args[0] is the sub-command name).
     */
    void execute(CommandSender sender, String[] args);

    /**
     * Gets the permission required for this command.
     * @return The permission string, or null if no permission is required.
     */
    String getPermission();

    // We can add tab complete logic here later
}