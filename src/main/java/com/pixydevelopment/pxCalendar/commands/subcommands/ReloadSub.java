/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.commands.subcommands;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.commands.CommandBase;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;

public class ReloadSub implements CommandBase {

    private final PxCalendarPlugin plugin;
    private final LangManager lang;

    public ReloadSub(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // 1. Reload core config
        plugin.getConfigManager().reloadConfig();

        // 2. Reload language files
        lang.reload();

        // 3. Reload time/calendar settings
        plugin.getCalendarManager().loadConfigValues();

        // 4. Reload reward bundles
        plugin.getRewardManager().loadRewards();

        // 5. Reload GUI definitions
        plugin.getGuiManager().loadGUIs();

        lang.sendMessage(sender, "commands.reload");
    }

    @Override
    public String getPermission() {
        return "pxcalendar.reload";
    }
}