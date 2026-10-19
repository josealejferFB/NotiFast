package com.notifast.app.data.local.migrations

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.sqlite.db.SupportSQLiteOpenHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Migration8To9Test {

    @Test
    fun `migration 8 to 9 adds section column to subjects with empty default and preserves rows`() {
        val context = RuntimeEnvironment.getApplication()
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(null)
            .callback(object : SupportSQLiteOpenHelper.Callback(8) {
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
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
            })
            .build()

        val db = FrameworkSQLiteOpenHelperFactory().create(config).writableDatabase
        db.execSQL(
            "INSERT INTO `subjects` (`userId`, `id`, `name`, `icon`, `colorHex`, `cycle`, `createdAt`) VALUES ('u1', 's1', 'Sistemas Distribuidos', 'Computer', '#4338CA', '20262', 1000)"
        )

        NotiFastMigrations.MIGRATION_8_9.migrate(db)

        db.query("SELECT `name`, `cycle`, `section` FROM `subjects` WHERE `id` = 's1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Sistemas Distribuidos", cursor.getString(0))
            assertEquals("20262", cursor.getString(1))
            assertEquals("", cursor.getString(2))
        }
        db.close()
    }
}
