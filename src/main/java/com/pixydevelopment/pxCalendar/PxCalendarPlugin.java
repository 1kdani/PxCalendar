/*
 ################################################################################
 #                                                                              #
 #                             PIXY-DEVELOPMENT                                 #
 #                                                                              #
 ################################################################################
 #                                                                              #
 #   This file is part of a plugin developed by Pixy-Development.               #
 #                                                                              #
 ################################################################################
*/

package com.pixydevelopment.pxCalendar;

import org.bukkit.plugin.java.JavaPlugin;
import com.pixydevelopment.pxCalendar.core.managers.ConfigManager;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.core.utils.UpdateChecker;
import com.pixydevelopment.pxCalendar.listeners.UpdateListener;

public final class PxCalendarPlugin extends JavaPlugin {

    private ConfigManager configManager;
    private LangManager langManager;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        configManager = new ConfigManager(this);
        langManager = new LangManager(this, configManager);

        if (configManager.getConfig().getBoolean("update-checker.enable", true)) {
            int spigotResourceId = 9089;
            this.updateChecker = new UpdateChecker(this, spigotResourceId);
            this.updateChecker.checkVersion();

            // Register listener
            String permission = configManager.getConfig().getString("update-checker.notify-permission", "pixy.update");
            getServer().getPluginManager().registerEvents(new UpdateListener(this, updateChecker, langManager, permission), this);
        }

        getLogger().info(getName() + " has been enabled!");
    }

    @Override
    public void onDisable() {
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}
