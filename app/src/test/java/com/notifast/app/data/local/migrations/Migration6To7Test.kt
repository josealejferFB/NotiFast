package com.notifast.app.data.local.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Migration6To7Test {

    private lateinit var helper: SupportSQLiteOpenHelper
    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null) // in-memory
            .callback(object : SupportSQLiteOpenHelper.Callback(6) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `cover_drafts` (
                            `id` TEXT NOT NULL,
                            `userId` TEXT NOT NULL,
                            `templateId` TEXT NOT NULL,
                            `subjectId` TEXT NOT NULL,
                            `subjectName` TEXT NOT NULL,
                            `workTitle` TEXT NOT NULL,
                            `subtitle` TEXT NOT NULL,
                            `authorsJson` TEXT NOT NULL,
                            `teacherName` TEXT NOT NULL,
                            `institutionName` TEXT NOT NULL,
                            `facultyOrDepartment` TEXT NOT NULL,
                            `city` TEXT NOT NULL,
                            `formattedDate` TEXT NOT NULL,
                            `abstractText` TEXT NOT NULL,
                            `keywordsText` TEXT NOT NULL,
                            `generatedLocalDocId` TEXT,
                            `googleDocId` TEXT,
                            `googleWebViewLink` TEXT,
                            `createdAt` INTEGER NOT NULL,
                            `updatedAt` INTEGER NOT NULL,
                            PRIMARY KEY(`id`)
                        )
                        """.trimIndent()
                    )
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        helper = FrameworkSQLiteOpenHelperFactory().create(config)
        db = helper.writableDatabase
    }

    @After
    fun tearDown() {
        helper.close()
    }

    @Test
    fun migration_6_to_7_adds_membrete_columns_and_preserves_existing_drafts() {
        // 1. Insert v6 row
        db.execSQL(
            """
            INSERT INTO `cover_drafts` (`id`, `userId`, `templateId`, `subjectId`, `subjectName`, `workTitle`, `subtitle`, `authorsJson`, `teacherName`, `institutionName`, `facultyOrDepartment`, `city`, `formattedDate`, `abstractText`, `keywordsText`, `generatedLocalDocId`, `googleDocId`, `googleWebViewLink`, `createdAt`, `updatedAt`)
            VALUES ('draft-1', 'u1', 'APA_7', 's1', 'Física II', 'Péndulo Simple', 'Lab 1', '["Ana"]', 'Dr. Max', 'UCV', 'Ciencias', 'Caracas', '19 de octubre', '', '', NULL, NULL, NULL, 1000, 1000)
            """.trimIndent()
        )

        // 2. Run migration 6 -> 7
        NotiFastMigrations.MIGRATION_6_7.migrate(db)

        // 3. Verify query works including new columns
        val cursor = db.query("SELECT id, workTitle, countryLine, ministryLine, programLine, curricularUnit FROM cover_drafts WHERE id = 'draft-1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("draft-1", cursor.getString(0))
        assertEquals("Péndulo Simple", cursor.getString(1))
        assertEquals("", cursor.getString(2)) // Default empty string
        assertEquals("", cursor.getString(3))
        assertEquals("", cursor.getString(4))
        assertEquals("", cursor.getString(5))
        cursor.close()

        // 4. Verify insert with new columns succeeds
        db.execSQL(
            """
            INSERT INTO `cover_drafts` (`id`, `userId`, `templateId`, `subjectId`, `subjectName`, `workTitle`, `subtitle`, `authorsJson`, `teacherName`, `institutionName`, `facultyOrDepartment`, `city`, `formattedDate`, `abstractText`, `keywordsText`, `countryLine`, `ministryLine`, `programLine`, `curricularUnit`, `generatedLocalDocId`, `googleDocId`, `googleWebViewLink`, `createdAt`, `updatedAt`)
            VALUES ('draft-2', 'u1', 'UNERG_DEGREE', 's2', 'Proyecto I', 'Hub Educativo', 'TEG', '["José"]', 'Prof. Miguel', 'UNERG', 'AIS', 'San Juan', 'Mayo 2026', '', '', 'REPÚBLICA BOLIVARIANA', 'MINISTERIO', 'ING INFORMÁTICA', 'PROYECTO DE GRADO I', NULL, NULL, NULL, 2000, 2000)
            """.trimIndent()
        )

        val cursor2 = db.query("SELECT countryLine, programLine FROM cover_drafts WHERE id = 'draft-2'")
        assertTrue(cursor2.moveToFirst())
        assertEquals("REPÚBLICA BOLIVARIANA", cursor2.getString(0))
        assertEquals("ING INFORMÁTICA", cursor2.getString(1))
        cursor2.close()
    }
}
