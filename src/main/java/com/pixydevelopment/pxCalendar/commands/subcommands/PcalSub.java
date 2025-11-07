/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.commands.subcommands;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.api.hologram.HologramManager;
import com.pixydevelopment.pxCalendar.calendar.Calendar;
import com.pixydevelopment.pxCalendar.calendar.PhysicalCalendar;
import com.pixydevelopment.pxCalendar.commands.CommandBase;
import com.pixydevelopment.pxCalendar.core.managers.LangManager;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Main sub-command for /pxc pcal [create|delete]
 */
public class PcalSub implements CommandBase { // Átnevezve PcalSub-ra

    private final PxCalendarPlugin plugin;
    private final LangManager lang;
    private final HologramManager hologramManager;

    public PcalSub(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLangManager();
        this.hologramManager = plugin.getHologramManager();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (hologramManager == null) {
            lang.sendMessage(sender, "&cPhysical calendars are disabled or no hologram plugin is installed.");
            return;
        }

        if (args.length < 2) {
            showUsage(sender);
            return;
        }

        String subAction = args[1].toLowerCase();

        if (!(sender instanceof Player)) {
            lang.sendMessage(sender, "messages.player-only");
            return;
        }
        Player player = (Player) sender;

        if (subAction.equals("create")) {
            // /pxc pcal create <id>
            if (args.length < 3) {
                lang.sendMessage(sender, "messages.usage-pcal-create");
                return;
            }
            handleCreate(player, args[2]);
        } else if (subAction.equals("delete")) {
            // /pxc pcal delete
            handleDelete(player);
        } else {
            showUsage(sender);
        }
    }

    private void handleCreate(Player player, String calendarId) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null) {
            lang.sendMessage(player, "messages.must-look-at-block");
            return;
        }

        // Check if calendar ID exists
        Calendar calendar = plugin.getGuiManager().getCalendar(calendarId);
        if (calendar == null) {
            lang.sendMessage(player, "messages.calendar-not-found", "%id%", calendarId);
            return;
        }

        Location loc = targetBlock.getLocation();
        String hash = PhysicalCalendar.getLocationHash(loc);

        // Check if one already exists here
        if (hologramManager.getPhysicalCalendar(hash) != null) {
            lang.sendMessage(player, "&cThere is already a physical calendar at this block.");
            return;
        }

        // Create and save
        PhysicalCalendar pCal = new PhysicalCalendar(loc, calendar.getId());
        hologramManager.addPhysicalCalendar(pCal);

        lang.sendMessage(player, "messages.physical-cal-created", "%id%", calendar.getId());
    }

    private void handleDelete(Player player) {
        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null) {
            lang.sendMessage(player, "messages.must-look-at-block");
            return;
        }

        Location loc = targetBlock.getLocation();
        String hash = PhysicalCalendar.getLocationHash(loc);

        PhysicalCalendar pCal = hologramManager.getPhysicalCalendar(hash);
        if (pCal == null) {
            lang.sendMessage(player, "messages.physical-cal-not-found");
            return;
        }

        // Remove it
        hologramManager.removePhysicalCalendar(pCal);
        lang.sendMessage(player, "messages.physical-cal-removed");
    }

    private void showUsage(CommandSender sender) {
        // We fetch the text from the lang file, as requested
        lang.sendMessage(sender, lang.getLangConfig().getString("commands.help-lines.6.text"));
        lang.sendMessage(sender, lang.getLangConfig().getString("commands.help-lines.7.text"));
    }

    @Override
    public String getPermission() {
        return "pxcalendar.admin.pcal";
    }
}