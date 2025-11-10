/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.commands; // A HELYES CSOMAG

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.commands.subcommands.*;
import com.pixydevelopment.pxCalendar.commands.subcommands.PcalSub;
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
public class PxCalendarCommand implements CommandExecutor, TabCompleter { // A HELYES NÉV

    private final PxCalendarPlugin plugin; // Ez a fő plugin osztályra mutat
    private final LangManager lang;
    private final Map<String, CommandBase> subCommands = new HashMap<>();
    private final Map<String, String> aliasMap = new HashMap<>();

    // A konstruktor a FŐ PxCalendarPlugin-t várja
    public PxCalendarCommand(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager(); // Így már megtalálja

        // Register sub-commands
        registerSubCommand("help", new HelpSub(plugin));
        registerSubCommand("open", new OpenSub(plugin));
        registerSubCommand("reload", new ReloadSub(plugin));
        registerSubCommand("reset", new ResetSub(plugin));
        registerSubCommand("pcal", new PcalSub(plugin));
        registerSubCommand("editor", new EditorSub(plugin));

        loadAliases();
    }

    private void registerSubCommand(String name, CommandBase command) {
        subCommands.put(name.toLowerCase(), command);
    }

    private void loadAliases() {
        FileConfiguration config = plugin.getConfigManager().getConfig(); // Ez is a fő osztályt használja
        if (!config.contains("aliases")) return;

        for (String subCommand : config.getConfigurationSection("aliases").getKeys(false)) {
            String alias = config.getString("aliases." + subCommand);
            aliasMap.put(alias.toLowerCase(), subCommand.toLowerCase());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            getCommand("help").execute(sender, args);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        subCommandName = aliasMap.getOrDefault(subCommandName, subCommandName);

        CommandBase subCommand = getCommand(subCommandName);

        if (subCommand == null) {
            lang.sendMessage(sender, "messages.invalid-command");
            return true;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            lang.sendMessage(sender, "messages.no-permission");
            return true;
        }

        subCommand.execute(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            for (String cmdName : subCommands.keySet()) {
                if (cmdName.startsWith(args[0].toLowerCase())) {
                    completions.add(cmdName);
                }
            }
            for (String aliasName : aliasMap.keySet()) {
                if (aliasName.startsWith(args[0].toLowerCase())) {
                    completions.add(aliasName);
                }
            }
            return completions;
        }
        return null;
    }

    private CommandBase getCommand(String name) {
        return subCommands.get(name.toLowerCase());
    }
}