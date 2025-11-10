/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.api.hologram.impl;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.api.hologram.HologramService;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method; // JAVÍTÁS: Importálva
import java.util.List;
import java.util.logging.Level;

/**
 * HologramService implementation for DecentHolograms
 * JAVÍTVA Reflection-nel, hogy kikerülje az 1.16.5 vs 1.17+ API konfliktust.
 */
public class DecentHologramService implements HologramService {

    private final String HOLOGRAM_PREFIX = "pxcalendar-";
    private final PxCalendarPlugin plugin;

    // JAVÍTÁS: Gyorsítótár a reflection metódushoz, hogy ne legyen lassú
    private Method setHologramLinesMethod;

    public DecentHologramService(PxCalendarPlugin plugin) {
        this.plugin = plugin;

        // JAVÍTÁS: A konstruktorban megkeressük a metódust
        try {
            // Keressük azt a "setHologramLines" metódust, ami (Hologram, Player, List) paramétereket vár
            this.setHologramLinesMethod = DHAPI.class.getMethod("setHologramLines", Hologram.class, Player.class, List.class);
        } catch (NoSuchMethodException e) {
            plugin.getLogger().log(Level.SEVERE, "Hiba a DecentHolograms API betöltésekor!");
            plugin.getLogger().log(Level.SEVERE, "Nem található a 'setHologramLines(Hologram, Player, List)' metódus.");
            plugin.getLogger().log(Level.SEVERE, "Valószínűleg API-verzió ütközés van. Ellenőrizd a plugin verziókat.");
            this.setHologramLinesMethod = null;
        }
    }

    @Override
    public void createHologram(String id, Location location, List<String> lines) {
        Hologram hologram = DHAPI.getHologram(HOLOGRAM_PREFIX + id);
        if (hologram == null) {
            DHAPI.createHologram(HOLOGRAM_PREFIX + id, location, lines);
        }
    }

    @Override
    public void updateHologram(String id, List<String> newLines) {
        Hologram hologram = DHAPI.getHologram(HOLOGRAM_PREFIX + id);
        if (hologram != null) {
            DHAPI.setHologramLines(hologram, newLines);
        }
    }

    @Override
    public void deleteHologram(String id) {
        Hologram hologram = DHAPI.getHologram(HOLOGRAM_PREFIX + id);
        if (hologram != null) {
            hologram.delete();
        }
    }

    @Override
    public void deleteAll() {
        // Ezt a metódust már nem használjuk, a HologramManager
        // egyesével törli a saját listája alapján.
    }

    @Override
    public void updatePlayerHologram(Player player, String id, List<String> newLines) {
        Hologram hologram = DHAPI.getHologram(HOLOGRAM_PREFIX + id);

        // JAVÍTÁS: A gyorsítótárazott metódus meghívása reflection-nel
        if (hologram != null && this.setHologramLinesMethod != null) {
            try {
                // Meghívjuk a metódust: DHAPI.setHologramLines(hologram, player, newLines)
                this.setHologramLinesMethod.invoke(null, hologram, player, newLines);
            } catch (Exception e) {
                // Hiba futásidőben (pl. ha mégis rossz a DH verzió a szerveren)
                plugin.getLogger().log(Level.WARNING, "Hiba a hologram frissítésekor (reflection): " + e.getMessage());
            }
        }
    }

    @Override
    public void shutdown() {
        // DecentHolograms handles its own shutdown
    }
}