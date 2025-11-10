/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.database;

import com.pixydevelopment.pxCalendar.PxCalendarPlugin;
import com.pixydevelopment.pxCalendar.calendar.PhysicalCalendar;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Manages all database connections (using HikariCP) and executes SQL queries.
 */
public class DatabaseManager {

    private final PxCalendarPlugin plugin;
    private final StorageType storageType;
    private HikariDataSource hikari;

    // Table names
    private final String PLAYER_DATA_TABLE = "pxc_player_data";
    private final String PHYSICAL_CAL_TABLE = "pxc_physical_calendars";

    public DatabaseManager(PxCalendarPlugin plugin) {
        this.plugin = plugin;
        this.storageType = StorageType.valueOf(plugin.getConfigManager().getConfig().getString("database.type", "H2").toUpperCase());

        setupConnection();
        createTables();
    }

    /**
     * Sets up the HikariCP connection pool based on the config.yml.
     */
    private void setupConnection() {
        plugin.getLogger().info("Setting up database connection (" + storageType + ")...");
        HikariConfig config = new HikariConfig();
        FileConfiguration cfg = plugin.getConfigManager().getConfig();

        switch (storageType) {
            case H2:
                // H2 (File-based database)
                String dbPath = new File(plugin.getDataFolder(), "pxcalendar-data").getAbsolutePath();
                config.setJdbcUrl("jdbc:h2:" + dbPath);
                config.setUsername("sa");
                config.setPassword("");
                config.setDriverClassName("org.h2.Driver");
                break;

            case MYSQL:
            case POSTGRESQL:
                // MySQL / PostgreSQL (Remote database)
                String driver = (storageType == StorageType.MYSQL) ? "com.mysql.jdbc.jdbc2.optional.MysqlDataSource" : "org.postgresql.ds.PGSimpleDataSource";
                config.setDataSourceClassName(driver);
                config.addDataSourceProperty("serverName", cfg.getString("database.host"));
                config.addDataSourceProperty("portNumber", cfg.getInt("database.port"));
                config.addDataSourceProperty("databaseName", cfg.getString("database.database"));
                config.addDataSourceProperty("user", cfg.getString("database.username"));
                config.addDataSourceProperty("password", cfg.getString("database.password"));

                // MySQL specific optimizations
                if (storageType == StorageType.MYSQL) {
                    config.addDataSourceProperty("cachePrepStmts", "true");
                    config.addDataSourceProperty("prepStmtCacheSize", "250");
                    config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                    config.addDataSourceProperty("useServerPrepStmts", "true");
                }
                break;

            case MONGODB:
                plugin.getLogger().severe("MongoDB is not yet supported in this version.");
                return;
        }

        config.setMaximumPoolSize(10);
        config.setPoolName("PxCalendar-Pool");

        try {
            this.hikari = new HikariDataSource(config);
            plugin.getLogger().info("Database connection pool established.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to establish database connection!", e);
        }
    }

    /**
     * Creates the necessary database tables if they don't exist.
     */
    private void createTables() {
        // SQL for player data: (UUID, calendar_id, day, timestamp, ip_address)
        String playerDataSql = "CREATE TABLE IF NOT EXISTS " + PLAYER_DATA_TABLE + " (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "calendar_id VARCHAR(100) NOT NULL," +
                "day_claimed INT NOT NULL," +
                "claim_timestamp BIGINT NOT NULL," +
                "ip_address VARCHAR(45)," +
                "UNIQUE(player_uuid, calendar_id, day_claimed)" +
                ");";

        // SQL for physical calendars: (world, x, y, z, calendar_id)
        // This links one block to one *entire* calendar, as you requested.
        String physicalCalSql = "CREATE TABLE IF NOT EXISTS " + PHYSICAL_CAL_TABLE + " (" +
                "location_hash VARCHAR(100) PRIMARY KEY," + // "world_x_y_z"
                "world VARCHAR(100) NOT NULL," +
                "x INT NOT NULL," +
                "y INT NOT NULL," +
                "z INT NOT NULL," +
                "calendar_id VARCHAR(100) NOT NULL" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(playerDataSql);
            stmt.execute(physicalCalSql);
            plugin.getLogger().info("Database tables verified/created.");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not create database tables!", e);
        }
    }

    /**
     * Gets a connection from the pool.
     * @return A database connection
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        if (hikari == null) {
            throw new SQLException("Database connection is not initialized.");
        }
        return hikari.getConnection();
    }

    /**
     * Closes the connection pool.
     */
    public void close() {
        if (hikari != null) {
            hikari.close();
            plugin.getLogger().info("Database connection pool closed.");
        }
    }

    // --- Data Access Methods (Player Data) ---
    // These methods will be called by the DataManager

    /**
     * Loads all claimed days for a specific player from the database.
     * @param playerUUID The player's UUID
     * @return A PreparedStatement to be executed.
     */
    public PreparedStatement getPlayerDataStatement(Connection conn, UUID playerUUID) throws SQLException {
        String sql = "SELECT calendar_id, day_claimed FROM " + PLAYER_DATA_TABLE + " WHERE player_uuid = ?;";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, playerUUID.toString());
        return ps;
    }

    /**
     * Saves a player's claim to the database.
     */
    public void savePlayerClaim(UUID playerUUID, String calendarId, int day, String ipAddress) {
        String sql = "INSERT INTO " + PLAYER_DATA_TABLE +
                " (player_uuid, calendar_id, day_claimed, claim_timestamp, ip_address) " +
                "VALUES (?, ?, ?, ?, ?);";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ps.setString(2, calendarId);
            ps.setInt(3, day);
            ps.setLong(4, System.currentTimeMillis());
            ps.setString(5, ipAddress);
            ps.executeUpdate();
        } catch (SQLException e) {
            // We ignore "duplicate key" errors, as it just means they already claimed it.
            if (!e.getMessage().contains("Duplicate entry") && !e.getMessage().contains("UNIQUE constraint failed")) {
                plugin.getLogger().log(Level.WARNING, "Failed to save player claim for " + playerUUID, e);
            }
        }
    }

    /**
     * Resets (deletes) all data for a specific player.
     */
    public void resetPlayerData(UUID playerUUID) {
        String sql = "DELETE FROM " + PLAYER_DATA_TABLE + " WHERE player_uuid = ?;";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, playerUUID.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to reset player data for " + playerUUID, e);
        }
    }

    /**
     * Loads all physical calendar locations from the database.
     * @return A map of <LocationHash, CalendarID>
     */
    public Map<String, PhysicalCalendar> loadPhysicalCalendars() {
        Map<String, PhysicalCalendar> calendars = new HashMap<>();
        String sql = "SELECT * FROM " + PHYSICAL_CAL_TABLE;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String hash = rs.getString("location_hash");
                String world = rs.getString("world");
                int x = rs.getInt("x");
                int y = rs.getInt("y");
                int z = rs.getInt("z");
                String calendarId = rs.getString("calendar_id");

                Location loc = new Location(Bukkit.getWorld(world), x, y, z);
                calendars.put(hash, new PhysicalCalendar(loc, calendarId));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load physical calendars from database.", e);
        }
        return calendars;
    }

    /**
     * Saves a new physical calendar to the database.
     * @param pCal The PhysicalCalendar object to save.
     */
    public void savePhysicalCalendar(PhysicalCalendar pCal) {
        String sql = "INSERT INTO " + PHYSICAL_CAL_TABLE +
                " (location_hash, world, x, y, z, calendar_id) " +
                "VALUES (?, ?, ?, ?, ?, ?);";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pCal.getId());
            ps.setString(2, pCal.getLocation().getWorld().getName());
            ps.setInt(3, pCal.getLocation().getBlockX());
            ps.setInt(4, pCal.getLocation().getBlockY());
            ps.setInt(5, pCal.getLocation().getBlockZ());
            ps.setString(6, pCal.getCalendarId());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to save physical calendar.", e);
        }
    }

    /**
     * Deletes a physical calendar from the database.
     * @param hash The location hash (PhysicalCalendar.getId())
     */
    public void deletePhysicalCalendar(String hash) {
        String sql = "DELETE FROM " + PHYSICAL_CAL_TABLE + " WHERE location_hash = ?;";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to delete physical calendar.", e);
        }
    }

    // TODO: Add methods like loadPhysicalCalendars(), savePhysicalCalendar(Location, String), etc.
}