/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.core.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Utility class to easily create ItemStacks from configs or code.
 * JAVÍTVA Multi-Verzióra (1.16.5 - 1.21.10)
 */
public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;

    // --- Verzió-ellenőrző (Lusta Betöltés) ---
    private static Boolean IS_MODERN_API; // null = még nincs eldöntve

    // Modern (1.17+) Reflection Metódusok
    private static Method setOwnerProfileMethod;
    private static Method createPlayerProfileMethod;
    private static Method playerProfileGetTexturesMethod;
    private static Method playerTexturesSetSkinMethod;

    // Régi (1.16.5) Reflection Mező
    private static Field legacyProfileField;

    /**
     * Csak egyszer fut le, amikor először szükség van rá.
     * Ekkor már biztosan fut a Bukkit API.
     */
    private static boolean isModern() {
        if (IS_MODERN_API != null) {
            return IS_MODERN_API; // Már eldöntöttük
        }

        try {
            // Megpróbáljuk betölteni a 1.17+ API-t
            Class.forName("org.bukkit.inventory.meta.PlayerProfile");
            IS_MODERN_API = true;
            Bukkit.getLogger().log(Level.INFO, "[PxCalendar] Detected Modern (1.17+) Skull API. Initializing reflection...");

            // Betöltjük a modern metódusokat reflectionnel
            Class<?> playerProfileClass = Class.forName("org.bukkit.inventory.meta.PlayerProfile");

            try {
                // 1.18+ út: Bukkit.createPlayerProfile(UUID)
                createPlayerProfileMethod = Bukkit.class.getMethod("createPlayerProfile", UUID.class);
            } catch (NoSuchMethodException e) {
                // 1.17 út: Bukkit.getServer().createPlayerProfile(UUID)
                createPlayerProfileMethod = Bukkit.getServer().getClass().getMethod("createPlayerProfile", UUID.class);
            }

            setOwnerProfileMethod = SkullMeta.class.getMethod("setOwnerProfile", playerProfileClass);
            // JAVÍTÁS: A metódus neve 1.18.2-től "getProfileTextures", de korábban "getTextures" volt.
            // Univerzálisabb, ha a "getTextures"-t keressük.
            playerProfileGetTexturesMethod = playerProfileClass.getMethod("getTextures");
            playerTexturesSetSkinMethod = playerProfileGetTexturesMethod.getReturnType().getMethod("setSkin", URL.class);

        } catch (Exception e) {
            // 1.16.5 vagy régebbi
            IS_MODERN_API = false;
            Bukkit.getLogger().log(Level.INFO, "[PxCalendar] Detected Legacy (1.16.5) Skull API. Initializing reflection...");
            try {
                // Betöltjük a régi reflection mezőt
                // JAVÍTÁS: Ahelyett, hogy a verziószámot próbáljuk kitalálni,
                // közvetlenül a CraftMetaSkull osztályt keressük meg.
                Field profileField = null;
                try {
                    // Próbáljuk meg a CraftMetaSkullt (Spigot)
                    Class<?> craftMetaSkullClass = Class.forName("org.bukkit.craftbukkit.v1_16_R3.inventory.CraftMetaSkull");
                    profileField = craftMetaSkullClass.getDeclaredField("profile");
                } catch (Exception spigotError) {
                    try {
                        // Próbáljuk meg a Paper nevet (ha eltér)
                        Class<?> paperMetaSkullClass = Class.forName("io.papermc.paper.inventory.meta.PaperMetaSkull");
                        profileField = paperMetaSkullClass.getDeclaredField("profile");
                    } catch (Exception paperError) {
                        // Ha egyik sem sikerül, feladjuk
                        throw new IllegalStateException("Cannot find 'profile' field in CraftMetaSkull or PaperMetaSkull", paperError);
                    }
                }

                legacyProfileField = profileField;
                legacyProfileField.setAccessible(true);
            } catch (Exception legacyError) {
                Bukkit.getLogger().log(Level.SEVERE, "[PxCalendar] Failed to initialize Legacy (1.16.5) Skull API!", legacyError);
                legacyProfileField = null;
            }
        }
        return IS_MODERN_API;
    }
    // --- Verzió-ellenőrző vége ---


    public ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }

    public ItemBuilder(ItemStack item) {
        this.item = item.clone();
        this.meta = item.getItemMeta();
    }

    public ItemBuilder name(String name) {
        meta.setDisplayName(ChatUtil.format(name));
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        meta.setLore(lore.stream()
                .map(ChatUtil::format)
                .collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * Sets the texture for a PLAYER_HEAD item.
     * @param texture The Base64 texture HASH.
     */
    public ItemBuilder texture(String texture) {
        if (meta instanceof SkullMeta) {
            SkullMeta skullMeta = (SkullMeta) meta;

            if (isModern()) {
                // --- MODERN (1.17+ -> 1.21.10) ÚTVONAL (Reflection) ---
                try {
                    Object profile;
                    if (createPlayerProfileMethod.getDeclaringClass().equals(Bukkit.class)) {
                        profile = createPlayerProfileMethod.invoke(null, UUID.randomUUID());
                    } else {
                        profile = createPlayerProfileMethod.invoke(Bukkit.getServer(), UUID.randomUUID());
                    }

                    Object textures = playerProfileGetTexturesMethod.invoke(profile);
                    playerTexturesSetSkinMethod.invoke(textures, new URL("http://textures.minecraft.net/texture/" + texture));
                    setOwnerProfileMethod.invoke(skullMeta, profile);

                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, "[PxCalendar] Failed to set skull texture (Modern Reflection API): " + e.getMessage());
                }
            } else {
                // --- RÉGI (1.16.5) ÚTVONAL (Közvetlen hívás + Reflection) ---
                try {
                    if (legacyProfileField == null) throw new IllegalStateException("Legacy profileField failed to initialize.");

                    GameProfile profile = new GameProfile(UUID.randomUUID(), UUID.randomUUID().toString());
                    profile.getProperties().put("textures", new Property("textures", texture));

                    // Reflectionnel állítjuk be a mezőt
                    legacyProfileField.set(skullMeta, profile);
                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, "[PxCalendar] Failed to set skull texture (Legacy Reflection API): " + e.getMessage());
                }
            }
        }
        return this;
    }


    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

    // A fromConfigString metódus (az előző javításomból) már helyesen kezeli a NATIVE: formátumot
    public static ItemStack fromConfigString(String configString) {
        if (configString == null || configString.isEmpty()) {
            return null;
        }

        try {
            String[] parts = configString.split(":");
            if (parts.length == 0) return null;

            String type = parts[0].toUpperCase();
            ItemBuilder builder;
            Material material;

            switch (type) {
                case "PLAYER_HEAD":
                    // Formátum: PLAYER_HEAD:TEXTURE[:AMOUNT]
                    builder = new ItemBuilder(Material.PLAYER_HEAD);
                    if (parts.length > 1) {
                        builder.texture(parts[1]);
                    }
                    if (parts.length > 2) {
                        builder.amount(Integer.parseInt(parts[2]));
                    }
                    return builder.build();

                case "NATIVE":
                    // Formátum: NATIVE:MATERIAL:AMOUNT
                    material = Material.matchMaterial(parts[1]);
                    if (material == null) throw new IllegalArgumentException("Invalid material: " + parts[1]);
                    builder = new ItemBuilder(material);
                    if (parts.length > 2) {
                        builder.amount(Integer.parseInt(parts[2]));
                    }
                    return builder.build();

                case "ITEMSADDER":
                case "ORAXEN":
                    Bukkit.getLogger().log(Level.INFO, "[PxCalendar] Skipping item (not implemented): " + configString);
                    return new ItemStack(Material.STONE);

                default:
                    material = Material.matchMaterial(parts[0]);
                    if (material == null) {
                        throw new IllegalArgumentException("Invalid material: " + parts[0]);
                    }
                    builder = new ItemBuilder(material);
                    if (parts.length > 1) {
                        builder.amount(Integer.parseInt(parts[1]));
                    }
                    return builder.build();
            }

        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[PxCalendar] Failed to parse item string: " + configString);
            e.printStackTrace();
            return new ItemStack(Material.BARRIER);
        }
    }
}