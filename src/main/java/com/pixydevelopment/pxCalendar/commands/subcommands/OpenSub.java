/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.commands.subcommands;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.commands.CommandBase;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.gui.GUIManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class OpenSub implements CommandBase {

    private final PxCalendarPlugin plugin;
    private final LangManager lang;
    private final GUIManager guiManager;

    public OpenSub(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();
        this.guiManager = plugin.getGuiManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "messages.player-only");
            return;
        }

        Player player = (Player) sender;
        String calendarId;

        if (args.length > 1) {
            // User specified a calendar: /pxc open vip
            calendarId = args[1];
        } else {
            // User did not specify, get default: /pxc open
            calendarId = plugin.getConfigManager().getConfig().getString("calendar.default-calendar-id", "example1");
        }

        // The GUIManager handles all checks (permissions, if exists, etc.)
        guiManager.openCalendar(player, calendarId);
    }

    @Override
    public String getPermission() {
        return "pxcalendar.open"; // We check specific calendar perms in the GUIManager
    }
}