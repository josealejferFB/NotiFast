package com.notifast.app.data.local.backup

import android.content.Context
import androidx.room.Room
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import kotlinx.coroutines.test.runTest
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
class RoomCoverBackupStoreTest {

    private lateinit var context: Context
    private lateinit var db: NotiFastDatabase
    private lateinit var store: RoomCoverBackupStore

    private val userId = "user-123"

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, NotiFastDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        store = RoomCoverBackupStore(db.coverDraftDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun apply_and_read_round_trip() = runTest {
        val draft = CoverDraft(
            id = "cd-1",
            userId = userId,
            templateType = CoverTemplateType.APA_7,
            workTitle = "Título de prueba",
            authors = listOf("Autor 1", "Autor 2")
        )

        val applyResult = store.apply(userId, listOf(draft))
        assertTrue(applyResult.isSuccess)

        val readResult = store.read(userId)
        assertTrue(readResult.isSuccess)

        val list = readResult.getOrThrow()
        assertEquals(1, list.size)
        assertEquals("cd-1", list[0].id)
        assertEquals(CoverTemplateType.APA_7, list[0].templateType)
        assertEquals("Título de prueba", list[0].workTitle)
        assertEquals(listOf("Autor 1", "Autor 2"), list[0].authors)
    }
}
