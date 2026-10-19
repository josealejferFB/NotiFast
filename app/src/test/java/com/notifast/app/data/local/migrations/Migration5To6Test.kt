package com.notifast.app.data.local.migrations

import android.content.Context
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
class Migration5To6Test {

    private lateinit var helper: SupportSQLiteOpenHelper
    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null) // in-memory
            .callback(object : SupportSQLiteOpenHelper.Callback(5) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `subjects` (
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
    fun migration_5_to_6_creates_study_documents_and_cover_drafts_tables() {
        // 1. Insert existing row in subjects v5
        db.execSQL(
            """
            INSERT INTO `subjects` (`userId`, `id`, `name`, `icon`, `colorHex`, `cycle`, `createdAt`)
            VALUES ('u1', 'sub-1', 'Física II', '🔬', '#3B9797', '2026-1', 1000)
            """.trimIndent()
        )

        // 2. Run migration 5 -> 6
        NotiFastMigrations.MIGRATION_5_6.migrate(db)

        // 3. Verify subjects preserved
        val subCursor = db.query("SELECT id, name FROM subjects WHERE id = 'sub-1'")
        assertTrue(subCursor.moveToFirst())
        assertEquals("sub-1", subCursor.getString(0))
        assertEquals("Física II", subCursor.getString(1))
        subCursor.close()

        // 4. Verify study_documents table created and works
        db.execSQL(
            """
            INSERT INTO `study_documents` (`id`, `userId`, `subjectId`, `subjectName`, `title`, `fileName`, `docType`, `sizeBytes`, `pageCount`, `sourceType`, `googleDocId`, `googleWebViewLink`, `createdAt`, `updatedAt`)
            VALUES ('doc-1', 'u1', 'sub-1', 'Física II', 'Guía Lab 1', 'doc-1.pdf', 'PDF', 20480, 5, 'IMPORTED', NULL, NULL, 1000, 1000)
            """.trimIndent()
        )

        val docCursor = db.query("SELECT title, docType, pageCount FROM study_documents WHERE id = 'doc-1'")
        assertTrue(docCursor.moveToFirst())
        assertEquals("Guía Lab 1", docCursor.getString(0))
        assertEquals("PDF", docCursor.getString(1))
        assertEquals(5, docCursor.getInt(2))
        docCursor.close()

        // 5. Verify cover_drafts table created and works
        db.execSQL(
            """
            INSERT INTO `cover_drafts` (`id`, `userId`, `templateId`, `subjectId`, `subjectName`, `workTitle`, `subtitle`, `authorsJson`, `teacherName`, `institutionName`, `facultyOrDepartment`, `city`, `formattedDate`, `abstractText`, `keywordsText`, `generatedLocalDocId`, `googleDocId`, `googleWebViewLink`, `createdAt`, `updatedAt`)
            VALUES ('draft-1', 'u1', 'APA_7', 'sub-1', 'Física II', 'Informe Péndulo', 'Lab 1', '["Juan Perez"]', 'Dr. Newton', 'UCV', 'Ciencias', 'Caracas', '19 de octubre de 2026', '', '', 'doc-1', NULL, NULL, 1000, 1000)
            """.trimIndent()
        )

        val draftCursor = db.query("SELECT workTitle, templateId, institutionName FROM cover_drafts WHERE id = 'draft-1'")
        assertTrue(draftCursor.moveToFirst())
        assertEquals("Informe Péndulo", draftCursor.getString(0))
        assertEquals("APA_7", draftCursor.getString(1))
        assertEquals("UCV", draftCursor.getString(2))
        draftCursor.close()
    }
}
