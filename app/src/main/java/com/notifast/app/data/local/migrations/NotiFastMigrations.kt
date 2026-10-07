package com.notifast.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v1 → v2: stable subjectId, academic cycle, per-evaluation grade scale (D5, D7, D8).
 *
 * Tables are rebuilt so the resulting schema (columns, PKs, indices) matches
 * exactly what Room generates for the v2 entities. Extra leftover indices from
 * v1 cause Room 2.7 schema validation to fail with
 * "Migration didn't properly handle: <table>".
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

            // --- notes: rebuild so only expected indices remain ---
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `notes_new` (
                    `id` TEXT NOT NULL,
                    `userId` TEXT NOT NULL,
                    `subjectId` TEXT NOT NULL,
                    `subject` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `content` TEXT NOT NULL,
                    `imageUrl` TEXT,
                    `tags` TEXT NOT NULL,
                    `timestamp` INTEGER,
                    `updatedAt` INTEGER,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO `notes_new` (`id`, `userId`, `subjectId`, `subject`, `title`, `content`, `imageUrl`, `tags`, `timestamp`, `updatedAt`)
                SELECT n.`id`,
                       n.`userId`,
                       COALESCE((
                           SELECT s.`id` FROM `subjects` s
                           WHERE s.`userId` = n.`userId`
                             AND s.`name` = n.`subject` COLLATE NOCASE
                           LIMIT 1
                       ), ''),
                       n.`subject`,
                       n.`title`,
                       n.`content`,
                       n.`imageUrl`,
                       n.`tags`,
                       n.`timestamp`,
                       n.`updatedAt`
                FROM `notes` n
                """.trimIndent()
            )
            db.execSQL("DROP TABLE `notes`")
            db.execSQL("ALTER TABLE `notes_new` RENAME TO `notes`")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_userId` ON `notes` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_notes_subjectId` ON `notes` (`subjectId`)")

            // --- evaluations: add columns, keep only expected indices ---
            db.execSQL("ALTER TABLE `evaluations` ADD COLUMN `subjectId` TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE `evaluations` ADD COLUMN `gradeScaleId` TEXT NOT NULL DEFAULT '0-10'")
            db.execSQL(
                """
                UPDATE `evaluations` SET `subjectId` = COALESCE((
                    SELECT s.`id` FROM `subjects` s
                    WHERE s.`userId` = `evaluations`.`userId`
                      AND s.`name` = `evaluations`.`subjectName` COLLATE NOCASE
                    LIMIT 1
                ), '')
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_evaluations_userId` ON `evaluations` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_evaluations_subjectId` ON `evaluations` (`subjectId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_evaluations_subjectName` ON `evaluations` (`subjectName`)")

            // --- schedules ---
            db.execSQL("ALTER TABLE `schedules` ADD COLUMN `subjectId` TEXT NOT NULL DEFAULT ''")
            db.execSQL(
                """
                UPDATE `schedules` SET `subjectId` = COALESCE((
                    SELECT s.`id` FROM `subjects` s
                    WHERE s.`userId` = `schedules`.`userId`
                      AND s.`name` = `schedules`.`subjectName` COLLATE NOCASE
                    LIMIT 1
                ), '')
                """.trimIndent()
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedules_userId` ON `schedules` (`userId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedules_subjectId` ON `schedules` (`subjectId`)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_schedules_dayOfWeek` ON `schedules` (`dayOfWeek`)")
        }
    }

    /**
     * v2 → v3: block-based note content (blocksJson + schemaVersion).
     *
     * Legacy rows keep schemaVersion=1 so reads convert markdown on the fly.
     * New writes store schemaVersion=2 with serialized NoteBlock JSON.
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE `notes` ADD COLUMN `blocksJson` TEXT NOT NULL DEFAULT '[]'")
            db.execSQL("ALTER TABLE `notes` ADD COLUMN `schemaVersion` INTEGER NOT NULL DEFAULT 1")
        }
    }
}
