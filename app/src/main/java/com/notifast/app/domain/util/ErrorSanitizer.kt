package com.notifast.app.domain.util

import com.notifast.app.domain.repository.CloudAuthException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException

/**
 * Normaliza y sanitiza cualquier excepción, código de error técnico o mensaje crudo
 * en mensajes amigables, claros y respetuosos en español para la experiencia de usuario NotiFast AAA.
 */
object ErrorSanitizer {

    const val MSG_GENERIC = "Ocurrió un error inesperado. Intenta nuevamente."
    const val MSG_NETWORK_OFFLINE = "Sin conexión a internet. Revisa tu red e intenta de nuevo."
    const val MSG_NETWORK_TIMEOUT = "El servidor tardó demasiado en responder. Intenta nuevamente."
    const val MSG_AUTH_REQUIRED = "Debes iniciar sesión para continuar."
    const val MSG_PERMISSION_DENIED = "No tienes permisos suficientes para realizar esta acción."
    const val MSG_NOT_FOUND = "El elemento solicitado no fue encontrado."
    const val MSG_SCHEDULE_NOT_FOUND = "El horario solicitado no fue encontrado."
    const val MSG_EVALUATION_NOT_FOUND = "La evaluación solicitada no fue encontrada."
    const val MSG_SUBJECT_NOT_FOUND = "La materia solicitada no fue encontrada."
    const val MSG_NOTE_NOT_FOUND = "El apunte solicitado no fue encontrado."
    const val MSG_DUPLICATE_ENTRY = "Ya existe un registro con esos datos."
    const val MSG_RATE_LIMIT = "Demasiadas solicitudes. Por favor, espera unos momentos."
    const val MSG_GOOGLE_AUTH = "No se pudo autenticar con Google. Intenta conectar tu cuenta de nuevo."

    // Prefijos o cadenas técnicas que deben ser completamente removidas
    private val TECHNICAL_PREFIXES = listOf(
        Regex("""^java\.lang\.\w+Exception:\s*""", RegexOption.IGNORE_CASE),
        Regex("""^android\.database\.\w+Exception:\s*""", RegexOption.IGNORE_CASE),
        Regex("""^com\.google\.\w+Exception:\s*""", RegexOption.IGNORE_CASE),
        Regex("""^Exception:\s*""", RegexOption.IGNORE_CASE),
        Regex("""^Error:\s*""", RegexOption.IGNORE_CASE),
        Regex("""^ApiException:\s*\d*:\s*""", RegexOption.IGNORE_CASE)
    )

    private val STACK_TRACE_PATTERNS = listOf(
        "at com.",
        "at android.",
        "at java.",
        "NullPointerException",
        "ClassCastException",
        "IndexOutOfBoundsException",
        "StackOverflowError",
        "OutOfMemoryError"
    )

    /**
     * Determina si un [Throwable] representa una cancelación voluntaria por parte del usuario
     * (descarte de diálogo, retroceso, cancelación de selector de cuenta).
     */
    fun isCancellation(throwable: Throwable?): Boolean {
        if (throwable == null) return false
        if (throwable is java.util.concurrent.CancellationException) return true
        if (throwable is CloudAuthException && throwable.reason == CloudAuthException.Reason.CANCELLED) return true
        val cls = throwable.javaClass.simpleName
        if (cls.contains("Cancel", ignoreCase = true)) return true
        val msg = throwable.message.orEmpty()
        if (msg.contains("cancel", ignoreCase = true)) return true
        if (msg.contains("[16]") || msg.contains("16:")) return true
        val cause = throwable.cause
        if (cause != null && cause !== throwable) {
            return isCancellation(cause)
        }
        return false
    }

    /**
     * Sanitiza un [Throwable] clasificándolo rigurosamente por tipo de excepción y mensaje.
     */
    fun sanitize(throwable: Throwable?, fallback: String = MSG_GENERIC): String {
        if (throwable == null) return fallback

        // 1. Clasificación por tipo de excepción
        when (throwable) {
            is UnknownHostException, is ConnectException -> return MSG_NETWORK_OFFLINE
            is SocketTimeoutException, is TimeoutException -> return MSG_NETWORK_TIMEOUT
            is IOException -> {
                val msg = throwable.message.orEmpty().lowercase()
                if (msg.contains("network") || msg.contains("connection") || msg.contains("offline") || msg.contains("host")) {
                    return MSG_NETWORK_OFFLINE
                }
                if (msg.contains("timeout") || msg.contains("timed out")) {
                    return MSG_NETWORK_TIMEOUT
                }
            }
        }

        val rawMessage = throwable.message
        if (rawMessage.isNullOrBlank()) {
            return fallback
        }

        return sanitize(rawMessage, fallback)
    }

    /**
     * Sanitiza una cadena cruda de error eliminando ruido técnico y normalizando a español.
     */
    fun sanitize(rawMessage: String?, fallback: String = MSG_GENERIC): String {
        if (rawMessage.isNullOrBlank()) return fallback

        var cleaned = rawMessage.trim()

        // Si contiene indicios de stacktrace interno, nunca filtrar detalles a UI
        for (pattern in STACK_TRACE_PATTERNS) {
            if (cleaned.contains(pattern, ignoreCase = true)) {
                return fallback
            }
        }

        // Remover prefijos técnicos
        for (regex in TECHNICAL_PREFIXES) {
            cleaned = cleaned.replace(regex, "").trim()
        }

        val lower = cleaned.lowercase()

        // 1. Red / Conexión
        if (lower.contains("no address associated with hostname") ||
            lower.contains("failed to connect") ||
            lower.contains("unable to resolve host") ||
            lower.contains("network error") ||
            lower.contains("enotfound") ||
            lower.contains("connection refused") ||
            lower.contains("sin conexión")
        ) {
            return MSG_NETWORK_OFFLINE
        }

        // 2. Timeout
        if (lower.contains("timeout") || lower.contains("timed out") || lower.contains("deadline exceeded")) {
            return MSG_NETWORK_TIMEOUT
        }

        // 3. Autenticación / Sesión
        if (lower.contains("unauthenticated") ||
            lower.contains("user not authenticated") ||
            lower.contains("usuario no autenticado") ||
            lower.contains("missing credentials") ||
            lower.contains("token expired")
        ) {
            return MSG_AUTH_REQUIRED
        }

        // 4. Permisos
        if (lower.contains("permission_denied") ||
            lower.contains("permission denied") ||
            lower.contains("insufficient permissions") ||
            lower.contains("forbidden") ||
            lower.contains("access denied")
        ) {
            return MSG_PERMISSION_DENIED
        }

        // 5. Entidades no encontradas
        if (lower.contains("horario no encontrado")) return MSG_SCHEDULE_NOT_FOUND
        if (lower.contains("evaluación no encontrada") || lower.contains("evaluacion no encontrada")) return MSG_EVALUATION_NOT_FOUND
        if (lower.contains("materia no encontrada")) return MSG_SUBJECT_NOT_FOUND
        if (lower.contains("apunte no encontrado") || lower.contains("nota no encontrada")) return MSG_NOTE_NOT_FOUND
        if (lower.contains("not_found") || lower.contains("not found") || lower.contains("no encontrado")) {
            return MSG_NOT_FOUND
        }

        // 6. Duplicados / Conflictos
        if (lower.contains("unique constraint failed") ||
            lower.contains("already exists") ||
            lower.contains("already_exists") ||
            lower.contains("ya existe")
        ) {
            return MSG_DUPLICATE_ENTRY
        }

        // 7. Límites / Cuotas
        if (lower.contains("quota exceeded") ||
            lower.contains("resource_exhausted") ||
            lower.contains("too many requests") ||
            lower.contains("rate limit")
        ) {
            return MSG_RATE_LIMIT
        }

        // 8. Google Play Services / Drive Auth
        if (lower.contains("12500") || lower.contains("12501") || lower.contains("sign in with google failed")) {
            return MSG_GOOGLE_AUTH
        }

        // 9. Si el mensaje resultante ya es un texto en español limpio, formatearlo con puntuación correcta
        if (cleaned.isNotBlank()) {
            val capitalized = cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            return if (!capitalized.endsWith(".") && !capitalized.endsWith("!") && !capitalized.endsWith("?")) {
                "$capitalized."
            } else {
                capitalized
            }
        }

        return fallback
    }
}
