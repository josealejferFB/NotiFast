package com.notifast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2: stable subjectId, academic cycle, per-evaluation grade scale (D5, D7, D8).
 *
 * Subjects table is rebuilt so `id` becomes part of the primary key and rename works.
 * Children are linked by name (COLLATE NOCASE) during the migration only.
 */
object NotiFastMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // --- subjects: rebuild with id PK ---
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `subjects_new` (
                    `userId` TEXT NOT NULL,
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL COLLATE NOCASE,
                    `icon` TEXT NOT NULL,
                    `colorHex` TEXT NOT NULL,
                    `cycle` TEXT NOT NULL,
                    `createdAt` INTEGER,
                    PRIMARY KEY(`userId`, `id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `subjects_new` (`userId`, `id`, `name`, `icon`, `colorHex`, `cycle`, `createdAt`)
                SELECT `userId`,
                       lower(hex(randomblob(16))),
                       `name`,
                       `icon`,
                       `colorHex`,
                       '',
                       `createdAt`
                FROM `subjects`
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `subjects`")
            db.execSQL("ALTER TABLE `subjects_new` RENAME TO `subjects`")
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_subjects_userId_name` ON `subjects` (`userId`, `name`)"
            )

            // --- notes ---
            db.execSQL("ALTER TABLE `notes` ADD COLUMN `subjectId` TEXT NOT NULL DEFAULT ''")
            db.execSQL(
                """
                UPDATE `notes` SET `subjectId` = (
                    SELECT s.`id` FROM `subjects` s
                    WHERE s.`userId` = `notes`.`userId`
                      AND s.`name` = `notes`.`subject` COLLATE NOCASE
                    LIMIT 1
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_notes_subjectId` ON `notes` (`subjectId`)"
            )

            // --- evaluations ---
            db.execSQL("ALTER TABLE `evaluations` ADD COLUMN `subjectId` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `evaluations` ADD COLUMN `gradeScaleId` TEXT NOT NULL DEFAULT '0-10'")
            db.execSQL(
                """
                UPDATE `evaluations` SET `subjectId` = (
                    SELECT s.`id` FROM `subjects` s
                    WHERE s.`userId` = `evaluations`.`userId`
                      AND s.`name` = `evaluations`.`subjectName` COLLATE NOCASE
                    LIMIT 1
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_evaluations_subjectId` ON `evaluations` (`subjectId`)"
            )

            // --- schedules ---
            db.execSQL("ALTER TABLE `schedules` ADD COLUMN `subjectId` TEXT NOT NULL DEFAULT ''")
            db.execSQL(
                """
                UPDATE `schedules` SET `subjectId` = (
                    SELECT s.`id` FROM `subjects` s
                    WHERE s.`userId` = `schedules`.`userId`
                      AND s.`name` = `schedules`.`subjectName` COLLATE NOCASE
                    LIMIT 1
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_schedules_subjectId` ON `schedules` (`subjectId`)"
            )
        }
    }
}
