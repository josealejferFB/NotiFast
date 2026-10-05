package com.notifast.app.data.repository

import android.net.Uri
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.entity.EvaluationEntity
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.repository.ImageRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class LocalEvaluationRepositoryImplTest {

    private val evaluationDao: EvaluationDao = mockk(relaxed = true)
    private val imageRepository: ImageRepository = mockk(relaxed = true)

    private lateinit var repository: LocalEvaluationRepositoryImpl

    @Before
    fun setUp() {
        repository = LocalEvaluationRepositoryImpl(
            evaluationDao = evaluationDao,
            imageRepository = imageRepository
        )
    }

    @Test
    fun `getEvaluations maps entities to domain Evaluation`() = runTest {
        val date = Date()
        val entities = listOf(
            EvaluationEntity(
                id = "e1",
                userId = "u1",
                subjectName = "Matemáticas",
                title = "Parcial 1",
                description = "Cap 1-3",
                date = date,
                grade = 18.5,
                weight = 25.0
            )
        )
        coEvery { evaluationDao.getEvaluations("u1") } returns flowOf(entities)

        val evals = repository.getEvaluations("u1").first()
        assertEquals(1, evals.size)
        assertEquals("e1", evals[0].id)
        assertEquals("Parcial 1", evals[0].title)
        assertEquals(18.5, evals[0].grade)
        assertEquals(25.0, evals[0].weight)
    }

    @Test
    fun `getEvaluation maps single entity or null`() = runTest {
        val entity = EvaluationEntity(id = "e1", userId = "u1", title = "Quiz 1")
        coEvery { evaluationDao.getEvaluationById("u1", "e1") } returns flowOf(entity)
        coEvery { evaluationDao.getEvaluationById("u1", "unknown") } returns flowOf(null)

        val found = repository.getEvaluation("u1", "e1").first()
        assertEquals("Quiz 1", found?.title)

        val notFound = repository.getEvaluation("u1", "unknown").first()
        assertNull(notFound)
    }

    @Test
    fun `addEvaluation uploads image if provided and inserts into dao`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { imageRepository.uploadNoteImage("u1", mockUri) } returns Result.success("file:///eval_pic.jpg")
        coEvery { evaluationDao.insertEvaluation(any()) } returns 1L

        val eval = Evaluation(title = "Final Exam", subjectName = "Física")
        val result = repository.addEvaluation("u1", eval, mockUri)
        assertTrue(result.isSuccess)

        coVerify {
            evaluationDao.insertEvaluation(match {
                it.userId == "u1" && it.title == "Final Exam" && it.imageUrl == "file:///eval_pic.jpg" && it.id.isNotBlank()
            })
        }
    }

    @Test
    fun `updateEvaluation deletes old image if new image is uploaded`() = runTest {
        val oldEntity = EvaluationEntity(id = "e1", userId = "u1", imageUrl = "file:///old_eval.jpg")
        val mockUri = mockk<Uri>()
        coEvery { evaluationDao.getEvaluationByIdSync("u1", "e1") } returns oldEntity
        coEvery { imageRepository.uploadNoteImage("u1", mockUri) } returns Result.success("file:///new_eval.jpg")
        coEvery { imageRepository.deleteImage("file:///old_eval.jpg") } returns Result.success(Unit)
        coEvery { evaluationDao.updateEvaluation(any()) } returns 1

        val updatedEval = Evaluation(id = "e1", title = "Updated Title")
        val result = repository.updateEvaluation("u1", updatedEval, mockUri)
        assertTrue(result.isSuccess)

        coVerify { imageRepository.deleteImage("file:///old_eval.jpg") }
        coVerify {
            evaluationDao.updateEvaluation(match {
                it.id == "e1" && it.imageUrl == "file:///new_eval.jpg" && it.title == "Updated Title"
            })
        }
    }

    @Test
    fun `updateEvaluation deletes old image if image removed`() = runTest {
        val oldEntity = EvaluationEntity(id = "e1", userId = "u1", imageUrl = "file:///old_eval.jpg")
        coEvery { evaluationDao.getEvaluationByIdSync("u1", "e1") } returns oldEntity
        coEvery { imageRepository.deleteImage("file:///old_eval.jpg") } returns Result.success(Unit)
        coEvery { evaluationDao.updateEvaluation(any()) } returns 1

        val updatedEval = Evaluation(id = "e1", title = "Updated Title", imageUrl = null)
        val result = repository.updateEvaluation("u1", updatedEval, null)
        assertTrue(result.isSuccess)

        coVerify { imageRepository.deleteImage("file:///old_eval.jpg") }
        coVerify {
            evaluationDao.updateEvaluation(match {
                it.id == "e1" && it.imageUrl == null
            })
        }
    }

    @Test
    fun `deleteEvaluation deletes image and removes from dao`() = runTest {
        val eval = Evaluation(id = "e1", userId = "u1", imageUrl = "file:///eval.jpg")
        coEvery { imageRepository.deleteImage("file:///eval.jpg") } returns Result.success(Unit)
        coEvery { evaluationDao.deleteEvaluationById("u1", "e1") } returns 1

        val result = repository.deleteEvaluation("u1", eval)
        assertTrue(result.isSuccess)

        coVerify { imageRepository.deleteImage("file:///eval.jpg") }
        coVerify { evaluationDao.deleteEvaluationById("u1", "e1") }
    }
}
