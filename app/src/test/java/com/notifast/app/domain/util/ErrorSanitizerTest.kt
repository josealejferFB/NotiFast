package com.notifast.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

class ErrorSanitizerTest {

    @Test
    fun `sanitize with null throwable returns fallback`() {
        val result = ErrorSanitizer.sanitize(null as Throwable?, fallback = "Error general")
        assertEquals("Error general", result)
    }

    @Test
    fun `sanitize with null or blank string returns fallback`() {
        assertEquals("Fallback por defecto", ErrorSanitizer.sanitize(null as String?, fallback = "Fallback por defecto"))
        assertEquals("Fallback por defecto", ErrorSanitizer.sanitize("   ", fallback = "Fallback por defecto"))
    }

    @Test
    fun `sanitize UnknownHostException returns network offline message`() {
        val exception = UnknownHostException("Unable to resolve host \"firestore.googleapis.com\": No address associated with hostname")
        val result = ErrorSanitizer.sanitize(exception)
        assertEquals(ErrorSanitizer.MSG_NETWORK_OFFLINE, result)
    }

    @Test
    fun `sanitize ConnectException returns network offline message`() {
        val exception = ConnectException("Failed to connect to /142.250.190.42:443")
        val result = ErrorSanitizer.sanitize(exception)
        assertEquals(ErrorSanitizer.MSG_NETWORK_OFFLINE, result)
    }

    @Test
    fun `sanitize SocketTimeoutException returns timeout message`() {
        val exception = SocketTimeoutException("Read timed out")
        val result = ErrorSanitizer.sanitize(exception)
        assertEquals(ErrorSanitizer.MSG_NETWORK_TIMEOUT, result)
    }

    @Test
    fun `sanitize TimeoutException returns timeout message`() {
        val exception = TimeoutException("Operation timed out after 10000ms")
        val result = ErrorSanitizer.sanitize(exception)
        assertEquals(ErrorSanitizer.MSG_NETWORK_TIMEOUT, result)
    }

    @Test
    fun `sanitize unauthenticated error returns session message`() {
        val raw1 = "User not authenticated"
        val raw2 = "Usuario no autenticado"
        val raw3 = "UNAUTHENTICATED: missing credentials"
        assertEquals(ErrorSanitizer.MSG_AUTH_REQUIRED, ErrorSanitizer.sanitize(raw1))
        assertEquals(ErrorSanitizer.MSG_AUTH_REQUIRED, ErrorSanitizer.sanitize(raw2))
        assertEquals(ErrorSanitizer.MSG_AUTH_REQUIRED, ErrorSanitizer.sanitize(raw3))
    }

    @Test
    fun `sanitize permission denied returns permission message`() {
        val raw = "PERMISSION_DENIED: Missing or insufficient permissions."
        assertEquals(ErrorSanitizer.MSG_PERMISSION_DENIED, ErrorSanitizer.sanitize(raw))
    }

    @Test
    fun `sanitize not found returns specific Spanish messages`() {
        assertEquals(ErrorSanitizer.MSG_SCHEDULE_NOT_FOUND, ErrorSanitizer.sanitize("Horario no encontrado"))
        assertEquals(ErrorSanitizer.MSG_EVALUATION_NOT_FOUND, ErrorSanitizer.sanitize("Evaluación no encontrada"))
        assertEquals(ErrorSanitizer.MSG_SUBJECT_NOT_FOUND, ErrorSanitizer.sanitize("Materia no encontrada"))
        assertEquals(ErrorSanitizer.MSG_NOT_FOUND, ErrorSanitizer.sanitize("Document not_found"))
    }

    @Test
    fun `sanitize duplicate or constraint violation returns duplicate message`() {
        val raw = "android.database.sqlite.SQLiteConstraintException: UNIQUE constraint failed: subjects.userId, subjects.name"
        assertEquals(ErrorSanitizer.MSG_DUPLICATE_ENTRY, ErrorSanitizer.sanitize(raw))
    }

    @Test
    fun `sanitize rate limit or quota exceeded returns rate limit message`() {
        val raw = "RESOURCE_EXHAUSTED: Quota exceeded for quota metric 'Read requests'"
        assertEquals(ErrorSanitizer.MSG_RATE_LIMIT, ErrorSanitizer.sanitize(raw))
    }

    @Test
    fun `sanitize Google sign in status codes returns friendly Google message`() {
        val raw = "ApiException: 12500: Sign in with Google failed"
        assertEquals(ErrorSanitizer.MSG_GOOGLE_AUTH, ErrorSanitizer.sanitize(raw))
    }

    @Test
    fun `sanitize raw string with java exception prefixes completely strips technical garbage`() {
        val raw = "java.lang.IllegalArgumentException: El nombre de la materia es requerido"
        val result = ErrorSanitizer.sanitize(raw)
        assertEquals("El nombre de la materia es requerido.", result)
        assertFalse(result.contains("java.lang"))
        assertFalse(result.contains("IllegalArgumentException"))
    }

    @Test
    fun `sanitize clean user message preserves Spanish text with proper punctuation and trimming`() {
        val raw = "  Las horas de inicio y fin son requeridas  "
        val result = ErrorSanitizer.sanitize(raw)
        assertEquals("Las horas de inicio y fin son requeridas.", result)
    }

    @Test
    fun `sanitize arbitrary technical stack trace leaks fallback without exposing code details`() {
        val raw = "NullPointerException at com.notifast.app.data.repository.FirestoreImpl.get(FirestoreImpl.kt:42)"
        val result = ErrorSanitizer.sanitize(raw)
        assertEquals(ErrorSanitizer.MSG_GENERIC, result)
        assertFalse(result.contains("NullPointerException"))
        assertFalse(result.contains("com.notifast"))
    }

    @Test
    fun `isCancellation detects user cancellation and Play Services code 16 correctly`() {
        assertTrue(ErrorSanitizer.isCancellation(java.util.concurrent.CancellationException("Job was cancelled")))
        assertTrue(ErrorSanitizer.isCancellation(Exception("16: The user canceled the sign-in flow.")))
        assertTrue(ErrorSanitizer.isCancellation(Exception("[16] The operation was canceled")))
        assertTrue(ErrorSanitizer.isCancellation(Exception("User cancelled the prompt")))
        assertTrue(ErrorSanitizer.isCancellation(RuntimeException("Wrapper", Exception("User canceled"))))

        assertFalse(ErrorSanitizer.isCancellation(null))
        assertFalse(ErrorSanitizer.isCancellation(NullPointerException("Something null")))
        assertFalse(ErrorSanitizer.isCancellation(java.net.ConnectException("Failed to connect")))
    }
}
