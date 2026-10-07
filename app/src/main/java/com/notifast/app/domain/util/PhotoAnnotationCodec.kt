package com.notifast.app.domain.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.notifast.app.data.model.PhotoAnnotation

object PhotoAnnotationCodec {

    private val gson: Gson = GsonBuilder().create()

    fun toJson(annotation: PhotoAnnotation): String = gson.toJson(annotation)

    fun fromJson(json: String): PhotoAnnotation {
        if (json.isBlank()) return PhotoAnnotation()
        return try {
            gson.fromJson(json, PhotoAnnotation::class.java) ?: PhotoAnnotation()
        } catch (e: Exception) {
            PhotoAnnotation()
        }
    }
}
