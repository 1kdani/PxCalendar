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

package com.pixydevelopment.pxCalendar.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Checks for updates using the Spiget API.
 */
public class UpdateChecker {

    private final JavaPlugin plugin;
    private final int resourceId;
    private String latestVersion;
    private boolean updateAvailable = false;

    /**
     * @param plugin Your plugin instance.
     * @param resourceId The numeric ID of your plugin on SpigotMC.
     */
    public UpdateChecker(JavaPlugin plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
        this.latestVersion = plugin.getDescription().getVersion();
    }

    /**
     * Runs the update check asynchronously.
     */
    public void checkVersion() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL("https://api.spiget.org/v2/resources/" + this.resourceId + "/versions/latest");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("User-Agent", "PixyDevelopment-Update-Checker");

                if (con.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    con.disconnect();

                    // Simple JSON parsing (no library needed)
                    // {"name":"1.0.1","uuid":"..."}
                    String json = content.toString();
                    if (json.contains("\"name\":\"")) {
                        this.latestVersion = json.split("\"name\":\"")[1].split("\"")[0];

                        if (!plugin.getDescription().getVersion().equalsIgnoreCase(this.latestVersion)) {
                            this.updateAvailable = true;
                            plugin.getLogger().warning("======================================================");
                            plugin.getLogger().warning("An update is available for " + plugin.getName() + "!");
                            plugin.getLogger().warning("Current version: " + plugin.getDescription().getVersion());
                            plugin.getLogger().warning("New version: " + this.latestVersion);
                            plugin.getLogger().warning("Download at: https://www.spigotmc.org/resources/" + this.resourceId);
                            plugin.getLogger().warning("======================================================");
                        } else {
                            plugin.getLogger().info("Plugin is up to date.");
                        }
                    }
                } else {
                    plugin.getLogger().warning("Could not check for updates (Spiget API response: " + con.getResponseCode() + ").");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error while checking for updates: " + e.getMessage());
            }
        });
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}