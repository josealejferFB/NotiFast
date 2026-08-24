package com.notifast.app.data.model.package_data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object SubjectPackageCodec {
    const val CURRENT_SCHEMA_VERSION = 1
    const val NOTIFAST_MAGIC_PREFIX = "NOTIFAST1:"

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    fun toJson(pkg: SubjectPackage): String {
        return gson.toJson(pkg)
    }

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
        val compressedBytes = byteOut.toByteArray()
        val base64 = Base64.getEncoder().encodeToString(compressedBytes)
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
            val decompressedBytes = GZIPInputStream(byteIn).use { it.readBytes() }
            val json = String(decompressedBytes, StandardCharsets.UTF_8)
            return fromJson(json)
        } catch (e: Exception) {
            throw IllegalArgumentException("No se pudo decodificar el Pack Académico: formato inválido o corrupto", e)
        }
    }
}
