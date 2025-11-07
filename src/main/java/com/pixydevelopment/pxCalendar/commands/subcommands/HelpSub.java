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
import org.bukkit.configuration.ConfigurationSection;

public class HelpSub implements CommandBase {

    private final LangManager lang;

    public HelpSub(PxCalendarPlugin plugin) {
        this.lang = plugin.getLangManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Send header
        lang.sendMessage(sender, "commands.help-header");

        // Get the help lines from the language file
        ConfigurationSection helpLines = lang.getLangConfig().getConfigurationSection("commands.help-lines");
        if (helpLines != null) {
            for (String key : helpLines.getKeys(false)) {
                String permission = helpLines.getString(key + ".permission");
                String text = helpLines.getString(key + ".text");

                // Only show lines they have permission for
                if (permission == null || sender.hasPermission(permission)) {
                    lang.sendMessage(sender, text);
                }
            }
        }

        // Send footer
        lang.sendMessage(sender, "commands.help-footer");
    }

    @Override
    public String getPermission() {
        return "pxcalendar.help";
    }
}