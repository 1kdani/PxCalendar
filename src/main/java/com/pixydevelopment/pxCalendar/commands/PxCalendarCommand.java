/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.commands;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.commands.subcommands.*;
import com.pixydevelopment.pxCalendar.commands.subcommands.PcolSub;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main command handler for /pxcalendar.
 * Delegates tasks to sub-command classes.
 */
public class PxCalendarCommand implements CommandExecutor, TabCompleter {

    private final PxCalendarPlugin plugin;
    private final LangManager lang;
    private final Map<String, CommandBase> subCommands = new HashMap<>();
    private final Map<String, String> aliasMap = new HashMap<>();

    public PxCalendarCommand(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();

        // Register sub-commands
        registerSubCommand("help", new HelpSub(plugin));
        registerSubCommand("open", new OpenSub(plugin));
        registerSubCommand("reload", new ReloadSub(plugin));
        registerSubCommand("reset", new ResetSub(plugin));
        registerSubCommand("pcal", new PcolSub(plugin));
        registerSubCommand("editor", new EditorSub(plugin));

        loadAliases();
    }

    private void registerSubCommand(String name, CommandBase command) {
        subCommands.put(name.toLowerCase(), command);
    }

    /**
     * Loads aliases from the config.yml (e.g., "rl" -> "reload")
     */
    private void loadAliases() {
        FileConfiguration config = plugin.getConfigManager().getConfig();
        if (!config.contains("aliases")) return;

        for (String subCommand : config.getConfigurationSection("aliases").getKeys(false)) {
            String alias = config.getString("aliases." + subCommand);
            // Map the alias to the main command name
            // e.g., aliasMap.put("rl", "reload");
            aliasMap.put(alias.toLowerCase(), subCommand.toLowerCase());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // No args, show default calendar or help
            // We'll make it show help by default
            getCommand("help").execute(sender, args);
            return true;
        }

        String subCommandName = args[0].toLowerCase();

        // Check if the input is an alias, if so, get the real command name
        subCommandName = aliasMap.getOrDefault(subCommandName, subCommandName);

        CommandBase subCommand = getCommand(subCommandName);

        if (subCommand == null) {
            // Invalid command
            lang.sendMessage(sender, "messages.invalid-command"); // Add this to lang.yml
            return true;
        }

        // Check permission
        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            lang.sendMessage(sender, "messages.no-permission");
            return true;
        }

        // Execute the sub-command
        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            // Add commands
            for (String cmdName : subCommands.keySet()) {
                if (cmdName.startsWith(args[0].toLowerCase())) {
                    completions.add(cmdName);
                }
            }
            // Add aliases
            for (String aliasName : aliasMap.keySet()) {
                if (aliasName.startsWith(args[0].toLowerCase())) {
                    completions.add(aliasName);
                }
            }
            return completions;
        }

        // TODO: Add tab completion for sub-commands (e.g., /pxc open <calendar_id>)
        return null;
    }

    private CommandBase getCommand(String name) {
        return subCommands.get(name.toLowerCase());
    }
}