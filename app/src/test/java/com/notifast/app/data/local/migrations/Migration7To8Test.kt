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
class Migration7To8Test {

    private lateinit var helper: SupportSQLiteOpenHelper
    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null) // in-memory
            .callback(object : SupportSQLiteOpenHelper.Callback(7) {
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
                            `countryLine` TEXT NOT NULL DEFAULT '',
                            `ministryLine` TEXT NOT NULL DEFAULT '',
                            `programLine` TEXT NOT NULL DEFAULT '',
                            `curricularUnit` TEXT NOT NULL DEFAULT '',
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
    fun migration_7_to_8_adds_tutor_and_font_columns_and_preserves_data() {
        // 1. Insert v7 row
        db.execSQL(
            """
            INSERT INTO `cover_drafts` (
                `id`, `userId`, `templateId`, `subjectId`, `subjectName`, `workTitle`, `subtitle`,
                `authorsJson`, `teacherName`, `institutionName`, `facultyOrDepartment`, `city`,
                `formattedDate`, `abstractText`, `keywordsText`, `countryLine`, `ministryLine`,
                `programLine`, `curricularUnit`, `generatedLocalDocId`, `googleDocId`, `googleWebViewLink`,
                `createdAt`, `updatedAt`
            )
            VALUES (
                'draft-v7', 'u1', 'UNERG_DEGREE', 's1', 'Tesis', 'Mi Título', 'Sub',
                '["Carlos"]', 'Prof. Ana', 'UNERG', 'AIS', 'San Juan',
                'Octubre 2026', '', '', 'PAIS', 'MINISTERIO',
                'PROGRAMA', 'UNIDAD', NULL, NULL, NULL,
                1000, 1000
            )
            """.trimIndent()
        )

        // 2. Run migration 7 -> 8
        NotiFastMigrations.MIGRATION_7_8.migrate(db)

        // 3. Verify query works including new columns
        val cursor = db.query(
            "SELECT id, workTitle, tutorName, fontName, fontSizePt FROM cover_drafts WHERE id = 'draft-v7'"
        )
        assertTrue(cursor.moveToFirst())
        assertEquals("draft-v7", cursor.getString(0))
        assertEquals("Mi Título", cursor.getString(1))
        assertEquals("", cursor.getString(2)) // Default empty string for tutorName
        assertEquals("Times New Roman", cursor.getString(3)) // Default font
        assertEquals(12, cursor.getInt(4)) // Default size 12 pt
        cursor.close()

        // 4. Verify insert with new columns succeeds
        db.execSQL(
            """
            INSERT INTO `cover_drafts` (
                `id`, `userId`, `templateId`, `subjectId`, `subjectName`, `workTitle`, `subtitle`,
                `authorsJson`, `teacherName`, `tutorName`, `fontName`, `fontSizePt`, `institutionName`,
                `facultyOrDepartment`, `city`, `formattedDate`, `abstractText`, `keywordsText`,
                `countryLine`, `ministryLine`, `programLine`, `curricularUnit`,
                `generatedLocalDocId`, `googleDocId`, `googleWebViewLink`, `createdAt`, `updatedAt`
            )
            VALUES (
                'draft-v8', 'u1', 'UNERG_DEGREE', 's1', 'Tesis', 'Nuevo Trabajo', 'Sub',
                '["Carlos"]', 'Prof. Ana', 'Dr. Roberto', 'Arial', 11, 'UNERG',
                'AIS', 'San Juan', 'Noviembre 2026', '', '',
                'PAIS', 'MINISTERIO', 'PROGRAMA', 'UNIDAD',
                NULL, NULL, NULL, 2000, 2000
            )
            """.trimIndent()
        )

        val cursor2 = db.query("SELECT tutorName, fontName, fontSizePt FROM cover_drafts WHERE id = 'draft-v8'")
        assertTrue(cursor2.moveToFirst())
        assertEquals("Dr. Roberto", cursor2.getString(0))
        assertEquals("Arial", cursor2.getString(1))
        assertEquals(11, cursor2.getInt(2))
        cursor2.close()
    }
}
