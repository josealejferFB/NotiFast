package com.notifast.app.data.model.package_data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Base64
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object SubjectPackageCodec {
    const val CURRENT_SCHEMA_VERSION = 1
    const val NOTIFAST_MAGIC_PREFIX = "NOTIFAST1:"
    private const val UTC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    private const val MAX_DECOMPRESSED_BYTES = 5_000_000

    private val utcFormat = object : JsonSerializer<Date>, JsonDeserializer<Date> {
        private fun fmt() = SimpleDateFormat(UTC_PATTERN, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive(fmt().format(src ?: Date(0)))
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
            val raw = json?.asString ?: return Date(0)
            return fmt().parse(raw) ?: Date(0)
        }
    }

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Date::class.java, utcFormat)
        .create()

    fun toJson(pkg: SubjectPackage): String = gson.toJson(pkg)

    fun fromJson(jsonStr: String): SubjectPackage {
        return gson.fromJson(jsonStr, SubjectPackage::class.java)
            ?: throw IllegalArgumentException("El contenido JSON no pudo ser deserializado como SubjectPackage")
    }

    fun encodeToCompressedString(pkg: SubjectPackage): String {
        val json = toJson(pkg)
        val byteOut = ByteArrayOutputStream()
        GZIPOutputStream(byteOut).use { gzip ->
            gzip.write(json.toByteArray(StandardCharsets.UTF_8))
        }
        val base64 = Base64.getEncoder().encodeToString(byteOut.toByteArray())
        return NOTIFAST_MAGIC_PREFIX + base64
    }

    fun decodeFromCompressedString(compressedPayload: String): SubjectPackage {
        val trimmed = compressedPayload.trim()
        val base64Data = if (trimmed.startsWith(NOTIFAST_MAGIC_PREFIX)) {
            trimmed.removePrefix(NOTIFAST_MAGIC_PREFIX)
        } else {
            trimmed
        }

        try {
            val compressedBytes = Base64.getDecoder().decode(base64Data)
            val byteIn = ByteArrayInputStream(compressedBytes)
            val decompressedBytes = GZIPInputStream(byteIn).use { stream ->
                stream.readBytes().also {
                    if (it.size > MAX_DECOMPRESSED_BYTES) {
                        throw IllegalArgumentException("El Pack Académico excede el tamaño máximo")
                    }
                }
            }
            val json = String(decompressedBytes, StandardCharsets.UTF_8)
            val pkg = fromJson(json)
            if (pkg.schemaVersion > CURRENT_SCHEMA_VERSION) {
                throw IllegalArgumentException("Versión de paquete no soportada: ${pkg.schemaVersion}")
            }
            return pkg
        } catch (e: IllegalArgumentException) {
            throw e
        } catch (e: Exception) {
            throw IllegalArgumentException("No se pudo decodificar el Pack Académico: formato inválido o corrupto", e)
        }
    }
}
