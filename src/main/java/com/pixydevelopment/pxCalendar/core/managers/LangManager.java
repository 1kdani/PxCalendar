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

package com.pixydevelopment.pxCalendar.core.managers;

import com.pixydevelopment.pxCalendar.core.utils.ChatUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Manages language files (i18n).
 * Loads the language file specified in config.yml.
 */
public class LangManager {

    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private FileConfiguration langConfig;
    private FileConfiguration defaultLangConfig; // en_US

    private final String defaultLang = "en_US";

    public LangManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        loadLangFiles();
    }

    /**
     * Loads the language files from the 'translations' folder.
     */
    private void loadLangFiles() {
        File translationsDir = new File(plugin.getDataFolder(), "translations");
        if (!translationsDir.exists()) {
            translationsDir.mkdirs();
        }

        // Save/load default (English) file
        saveDefaultLangFile(defaultLang);
        defaultLangConfig = YamlConfiguration.loadConfiguration(new File(translationsDir, defaultLang + ".yml"));

        // Save Hungarian file as well (since it's requested)
        saveDefaultLangFile("hu_HU");

        // Load active language from config
        String activeLang = configManager.getConfig().getString("language", defaultLang);
        File activeLangFile = new File(translationsDir, activeLang + ".yml");

        if (!activeLangFile.exists()) {
            plugin.getLogger().warning("Language file '" + activeLang + ".yml' not found!");
            plugin.getLogger().warning("Falling back to default language (" + defaultLang + ").");
            langConfig = defaultLangConfig;
        } else {
            langConfig = YamlConfiguration.loadConfiguration(activeLangFile);
        }
    }

    /**
     * Helper to save default language files from the JAR.
     * @param langFileName e.g., "en_US"
     */
    private void saveDefaultLangFile(String langFileName) {
        File langFile = new File(plugin.getDataFolder(), "translations/" + langFileName + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("translations/" + langFileName + ".yml", false);
        }
    }

    /**
     * Gets a formatted message from the loaded language file.
     *
     * @param path The path to the message (e.g., "messages.reload")
     * @return The formatted message, or an error message if not found.
     */
    public String getMessage(String path) {
        String message = langConfig.getString(path);

        // If not found in active lang, try default lang
        if (message == null) {
            message = defaultLangConfig.getString(path);
        }

        // If not found anywhere
        if (message == null) {
            plugin.getLogger().warning("Missing message in language file(s): " + path);
            return ChatUtil.format("&c[Error] Missing message: " + path);
        }

        return ChatUtil.format(message);
    }

    /**
     * Gets a message with placeholders.
     * e.g., getMessage("messages.welcome", "%player%", player.getName());
     */
    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        if (replacements.length % 2 != 0) {
            plugin.getLogger().warning("Invalid placeholder replacements for: " + path);
            return message; // Odd number of replacements
        }

        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }

    /**
     * Reloads the config and language files.
     */
    public void reload() {
        configManager.reloadConfig();
        loadLangFiles();
    }
}