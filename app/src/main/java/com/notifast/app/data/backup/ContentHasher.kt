package com.notifast.app.data.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.domain.util.NoteBlockCodec
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import javax.inject.Inject

class ContentHasher @Inject constructor() {

    private val gson: Gson = NoteBlockCodec.registerAdapters(GsonBuilder()).create()

    fun sha1(file: File): String {
        if (!file.exists() || !file.isFile) return "missing"
        val digest = MessageDigest.getInstance("SHA-1")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun hash(snapshot: SubjectSnapshot): String {
        val root = JsonObject()

        // 1. Subject (normalized)
        val subjectJson = JsonObject().apply {
            addProperty("id", snapshot.subject.id)
            addProperty("name", snapshot.subject.name.trim().lowercase())
            addProperty("icon", snapshot.subject.icon)
            addProperty("colorHex", snapshot.subject.colorHex)
            addProperty("cycle", snapshot.subject.cycle)
        }
        root.add("subject", subjectJson)

        // 2. Schedules (sorted by id)
        val schedulesArray = JsonArray()
        snapshot.schedules.sortedBy { it.id }.forEach { s ->
            val obj = JsonObject().apply {
                addProperty("id", s.id)
                addProperty("subjectName", s.subjectName.trim().lowercase())
                addProperty("dayOfWeek", s.dayOfWeek)
                addProperty("startTime", s.startTime)
                addProperty("endTime", s.endTime)
                addProperty("classroom", s.classroom)
                addProperty("teacher", s.teacher)
                addProperty("meetingUrl", s.meetingUrl)
                addProperty("modality", s.modality)
                addProperty("color", s.color)
            }
            schedulesArray.add(obj)
        }
        root.add("schedules", schedulesArray)

        // 3. Evaluations (sorted by id, image path replaced by SHA-1)
        val evaluationsArray = JsonArray()
        snapshot.evaluations.sortedBy { it.id }.forEach { e ->
            val imgHash = e.imageUrl?.let { resolveImageSha1(it, snapshot.imageFiles) }
            val obj = JsonObject().apply {
                addProperty("id", e.id)
                addProperty("subjectName", e.subjectName.trim().lowercase())
                addProperty("title", e.title)
                addProperty("description", e.description)
                addProperty("date", e.date?.time)
                addProperty("type", e.type)
                addProperty("grade", e.grade)
                addProperty("weight", e.weight)
                addProperty("isPresented", e.isPresented)
                addProperty("gradeScaleId", e.gradeScaleId)
                addProperty("quickTips", e.quickTips)
                addProperty("imageSha1", imgHash)
            }
            evaluationsArray.add(obj)
        }
        root.add("evaluations", evaluationsArray)

        // 4. Notes (sorted by id, image paths in note and in NoteBlock.Image replaced by SHA-1)
        val notesArray = JsonArray()
        snapshot.notes.sortedBy { it.id }.forEach { n ->
            val noteImgHash = n.imageUrl?.let { resolveImageSha1(it, snapshot.imageFiles) }
            val normalizedBlocks = n.blocks.map { block ->
                if (block is NoteBlock.Image) {
                    val blockImgHash = resolveImageSha1(block.path, snapshot.imageFiles)
                    block.copy(path = blockImgHash)
                } else {
                    block
                }
            }
            val obj = JsonObject().apply {
                addProperty("id", n.id)
                addProperty("subject", n.subject.trim().lowercase())
                addProperty("title", n.title)
                addProperty("content", n.content)
                addProperty("imageSha1", noteImgHash)
                add("tags", gson.toJsonTree(n.tags.sorted()))
                add("blocks", gson.toJsonTree(normalizedBlocks))
            }
            notesArray.add(obj)
        }
        root.add("notes", notesArray)

        val canonicalString = sortJsonKeys(root).toString()
        val sha256 = MessageDigest.getInstance("SHA-256")
        val digest = sha256.digest(canonicalString.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun hashGlobal(snapshot: GlobalSnapshot): String {
        val root = JsonObject().apply {
            addProperty("firstName", snapshot.firstName.trim())
            addProperty("lastName", snapshot.lastName.trim())
            add("prefs", gson.toJsonTree(snapshot.prefs))
            val teachersArr = JsonArray()
            snapshot.teachers.sortedBy { it.id }.forEach { t ->
                val obj = JsonObject().apply {
                    addProperty("id", t.id)
                    addProperty("firstName", t.firstName.trim().lowercase())
                    addProperty("lastName", t.lastName.trim().lowercase())
                    addProperty("email", t.email.trim().lowercase())
                    addProperty("phoneNumber", t.phoneNumber.trim())
                }
                teachersArr.add(obj)
            }
            add("teachers", teachersArr)
            val avatarHash = snapshot.avatar?.let { sha1(it) } ?: "none"
            addProperty("avatarSha1", avatarHash)
        }
        val canonicalString = sortJsonKeys(root).toString()
        val sha256 = MessageDigest.getInstance("SHA-256")
        val digest = sha256.digest(canonicalString.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun resolveImageSha1(pathOrUri: String, imageFiles: Map<String, File>): String {
        val file = imageFiles[pathOrUri] ?: File(pathOrUri.removePrefix("file://"))
        return sha1(file)
    }

    private fun sortJsonKeys(element: JsonElement): JsonElement {
        return when {
            element.isJsonObject -> {
                val sortedObj = JsonObject()
                val entries = element.asJsonObject.entrySet().sortedBy { it.key }
                for ((key, value) in entries) {
                    sortedObj.add(key, sortJsonKeys(value))
                }
                sortedObj
            }
            element.isJsonArray -> {
                val sortedArr = JsonArray()
                for (item in element.asJsonArray) {
                    sortedArr.add(sortJsonKeys(item))
                }
                sortedArr
            }
            else -> element
        }
    }
}
