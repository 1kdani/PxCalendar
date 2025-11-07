/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.commands.subcommands;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.commands.CommandBase;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import org.bukkit.command.CommandSender;

/**
 * Main sub-command for /pxc pcal [create|delete]
 */
public class PcolSub implements CommandBase {

    private final PxCalendarPlugin plugin;
    private final LangManager lang;

    public PcolSub(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            lang.sendMessage(sender, "commands.help-lines.6"); // Show usage
            lang.sendMessage(sender, "commands.help-lines.7");
            return;
        }

        String subAction = args[1].toLowerCase();

        if (subAction.equals("create")) {
            // TODO: Logic for creating a physical calendar
            // 1. Check args.length > 2 (for calendarId)
            // 2. Check if sender is Player
            // 3. Get player's target block
            // 4. Check if calendarId exists (guiManager.getCalendar())
            // 5. Create new PhysicalCalendar object
            // 6. Save to DB (databaseManager.savePhysicalCalendar())
            // 7. Add to cache (hologramManager.addPhysicalCalendar())
            // 8. Send success message
            lang.sendMessage(sender, "DEBUG: Create command run");

        } else if (subAction.equals("delete")) {
            // TODO: Logic for deleting
            // 1. Check if sender is Player
            // 2. Get player's target block
            // 3. Get hash
            // 4. Check if it exists (hologramManager.getPhysicalCalendar())
            // 5. Remove from DB (databaseManager.deletePhysicalCalendar())
            // 6. Remove from cache (hologramManager.removePhysicalCalendar())
            // 7. Send success message
            lang.sendMessage(sender, "DEBUG: Delete command run");

        } else {
            lang.sendMessage(sender, "messages.invalid-command");
        }
    }

    @Override
    public String getPermission() {
        return "pxcalendar.admin.pcal";
    }
}