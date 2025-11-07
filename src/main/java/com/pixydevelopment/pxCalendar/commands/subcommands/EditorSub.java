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

public class EditorSub implements CommandBase {

    private final LangManager lang;

    public EditorSub(PxCalendarPlugin plugin) {
        this.lang = plugin.getLangManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // TODO: Implement the Admin GUI Editor
        lang.sendMessage(sender, "&cThe GUI Editor is still in development!");
    }

    @Override
    public String getPermission() {
        return "pxcalendar.editor";
    }
}