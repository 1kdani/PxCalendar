/*
 ################################################################################
 #                             PIXY-DEVELOPMENT                                 #
 ################################################################################
*/
package com.pixydevelopment.pxCalendar.database;

/**
 * Enum representing the supported database types.
 */
public enum StorageType {
    H2,
    MYSQL,
    POSTGRESQL,
    MONGODB // MongoDB will require a completely different manager, but we list it.
}