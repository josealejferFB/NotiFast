package com.notifast.app.data.local.migrations

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.robolectric.RuntimeEnvironment
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Migration4To5Test {

    private lateinit var helper: SupportSQLiteOpenHelper
    private lateinit var db: SupportSQLiteDatabase

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null) // in-memory
            .callback(object : SupportSQLiteOpenHelper.Callback(4) {
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
    fun migration_4_to_5_creates_subject_cloud_state_and_preserves_subjects() {
        // 1. Insert row in v4 subjects
        db.execSQL(
            """
            INSERT INTO `subjects` (`userId`, `id`, `name`, `icon`, `colorHex`, `cycle`, `createdAt`)
            VALUES ('u1', 'sub-1', 'Matemáticas', 'icon', '#FF0000', '2026-1', 1000)
            """.trimIndent()
        )

        // 2. Execute migration 4 -> 5
        NotiFastMigrations.MIGRATION_4_5.migrate(db)

        // 3. Verify subjects row survived
        val cursor = db.query("SELECT id, name FROM subjects WHERE id = 'sub-1'")
        assertTrue(cursor.moveToFirst())
        assertEquals("sub-1", cursor.getString(0))
        assertEquals("Matemáticas", cursor.getString(1))
        cursor.close()

        // 4. Verify subject_cloud_state exists and can insert/query
        db.execSQL(
            """
            INSERT INTO `subject_cloud_state` (`accountId`, `subjectId`, `driveFileId`, `lastUploadedHash`, `lastSyncedAt`)
            VALUES ('acc-1', 'sub-1', 'drive-123', 'hash-abc', 5000)
            """.trimIndent()
        )

        val cloudCursor = db.query("SELECT accountId, subjectId, driveFileId, lastUploadedHash, lastSyncedAt FROM subject_cloud_state WHERE accountId = 'acc-1'")
        assertTrue(cloudCursor.moveToFirst())
        assertEquals("acc-1", cloudCursor.getString(0))
        assertEquals("sub-1", cloudCursor.getString(1))
        assertEquals("drive-123", cloudCursor.getString(2))
        assertEquals("hash-abc", cloudCursor.getString(3))
        assertEquals(5000L, cloudCursor.getLong(4))
        cloudCursor.close()
    }
}
