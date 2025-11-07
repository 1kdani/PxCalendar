/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.commands.subcommands;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.commands.CommandBase;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

public class ResetSub implements CommandBase {

    private final PxCalendarPlugin plugin;
    private final LangManager lang;
    private final DataManager dataManager;

    public ResetSub(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();
        this.dataManager = plugin.getDataManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            lang.sendMessage(sender, "messages.usage-reset"); // Add this to lang.yml: "Usage: /pxc reset <player>"
            return;
        }

        String playerName = args[1];

        // Get the OfflinePlayer to reset data even if they are offline
        @SuppressWarnings("deprecation")
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);

        if (target == null || !target.hasPlayedBefore()) {
            lang.sendMessage(sender, "commands.player-not-found", "%player%", playerName);
            return;
        }

        // The DataManager handles clearing cache AND database
        dataManager.resetPlayer(target.getUniqueId());

        lang.sendMessage(sender, "commands.reset-success", "%player%", target.getName());
    }

    @Override
    public String getPermission() {
        return "pxcalendar.reset";
    }
}