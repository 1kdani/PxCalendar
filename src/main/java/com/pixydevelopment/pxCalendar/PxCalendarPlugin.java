/*
 ################################################################################
 #                                                                              #
 #                             PIXY-DEVELOPMENT                                 #
 #                                                                              #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar;

import com.pixydevelopment.pxCalendar.api.PAPIExpansion;
import com.pixydevelopment.pxCalendar.api.hologram.HologramManager;
import com.pixydevelopment.pxCalendar.commands.PxCalendarCommand;
import com.pixydevelopment.pxCalendar.core.managers.ConfigManager;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import com.pixydevelopment.pxCalendar.core.utils.UpdateChecker;
import com.pixydevelopment.pxCalendar.database.DatabaseManager;
import com.pixydevelopment.pxCalendar.editor.EditorSessionManager;
import com.pixydevelopment.pxCalendar.gui.GUIManager;
import com.pixydevelopment.pxCalendar.listeners.EditorChatListener;
import com.pixydevelopment.pxCalendar.editor.EditorManager;
import com.pixydevelopment.pxCalendar.listeners.EditorClickListener;
import com.pixydevelopment.pxCalendar.listeners.GUIClickListener;
import com.pixydevelopment.pxCalendar.listeners.PhysicalCalendarListener;
import com.pixydevelopment.pxCalendar.listeners.PlayerJoinListener;
import com.pixydevelopment.pxCalendar.listeners.UpdateListener;
import com.pixydevelopment.pxCalendar.managers.CalendarManager;
import com.pixydevelopment.pxCalendar.managers.DataManager;
import com.pixydevelopment.pxCalendar.managers.ReminderManager;
import com.pixydevelopment.pxCalendar.managers.RewardManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PxCalendarPlugin extends JavaPlugin { // EZ A HELYES FÁJL

    private static PxCalendarPlugin instance;
    private ConfigManager configManager;
    private LangManager langManager; // Itt van a LangManager
    private UpdateChecker updateChecker;
    private DatabaseManager databaseManager;
    private DataManager dataManager;
    private CalendarManager calendarManager;
    private RewardManager rewardManager;
    private GUIManager guiManager;
    private ReminderManager reminderManager;
    private HologramManager hologramManager;
    private EditorManager editorManager;
    private EditorSessionManager editorSessionManager;

    @Override
    public void onEnable() {
        instance = this;
        loadCoreManagers();
        loadPluginManagers();
        registerCommands();
        registerListeners();
        hookApis();
        startTasks();
        getLogger().info(getName() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (hologramManager != null) {
            hologramManager.shutdown();
        }
        getLogger().info(getName() + " has been disabled.");
    }

    private void loadCoreManagers() {
        configManager = new ConfigManager(this);
        langManager = new LangManager(this, configManager); // Itt jön létre
    }

    private void loadPluginManagers() {
        databaseManager = new DatabaseManager(this);
        dataManager = new DataManager();
        calendarManager = new CalendarManager(this);
        rewardManager = new RewardManager(this);
        rewardManager.loadRewards();
        guiManager = new GUIManager(this);
        guiManager.loadGUIs();
        editorManager = new EditorManager(this);
        editorSessionManager = new EditorSessionManager(this);
    }

    private void registerCommands() {
        PxCalendarCommand pxCalendarCommand = new PxCalendarCommand(this);
        getCommand("pxcalendar").setExecutor(pxCalendarCommand);
        getCommand("pxcalendar").setTabCompleter(pxCalendarCommand);
        getLogger().info("Commands registered.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIClickListener(this), this);
        getServer().getPluginManager().registerEvents(new EditorClickListener(), this);
        getServer().getPluginManager().registerEvents(new EditorChatListener(this), this);
        if (configManager.getConfig().getBoolean("physical-calendars.enable", true)) {
            getServer().getPluginManager().registerEvents(new PhysicalCalendarListener(this), this);
        }
        setupUpdateChecker();
        getLogger().info("Listeners registered.");
    }

    private void setupUpdateChecker() {
        if (configManager.getConfig().getBoolean("update-checker.enable", true)) {
            int spigotResourceId = 9089; // Placeholder
            this.updateChecker = new UpdateChecker(this, spigotResourceId);
            this.updateChecker.checkVersion();
            String permission = configManager.getConfig().getString("update-checker.notify-permission", "pxcalendar.admin.update");
            getServer().getPluginManager().registerEvents(new UpdateListener(this, updateChecker, langManager, permission), this);
        }
    }

    private void hookApis() {
        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PAPIExpansion(this).register();
            getLogger().info("Successfully hooked into PlaceholderAPI.");
        } else {
            getLogger().info("PlaceholderAPI not found, placeholders will not work.");
        }
        if (configManager.getConfig().getBoolean("physical-calendars.enable", true)) {
            this.hologramManager = new HologramManager(this);
        }
    }

    private void startTasks() {
        if (configManager.getConfig().getBoolean("reminders.enable", true)) {
            this.reminderManager = new ReminderManager(this);
            this.reminderManager.startTask();
        }
        if (hologramManager != null) {
            hologramManager.startUpdateTask();
        }
    }

    // --- Public Getters ---
    public static PxCalendarPlugin getInstance() { return instance; }
    public LangManager getLangManager() { return langManager; } // ITT A METÓDUS, AMIT KERESEL
    public ConfigManager getConfigManager() { return configManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public DataManager getDataManager() { return dataManager; }
    public CalendarManager getCalendarManager() { return calendarManager; }
    public RewardManager getRewardManager() { return rewardManager; }
    public GUIManager getGuiManager() { return guiManager; }
    public HologramManager getHologramManager() { return hologramManager; }
    public EditorManager getEditorManager() { return editorManager; }
    public EditorSessionManager getEditorSessionManager() { return editorSessionManager; }
}