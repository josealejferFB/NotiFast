package com.notifast.app.data.backup

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.notifast.app.data.model.NoteBlock
import com.notifast.app.data.model.Teacher
import com.notifast.app.data.model.package_data.SubjectPackage
import com.notifast.app.domain.util.NoteBlockCodec
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject

class BackupArchiveCodec @Inject constructor(
    private val optimizer: ImageOptimizer,
    private val hasher: ContentHasher = ContentHasher()
) {
    companion object {
        const val BACKUP_SCHEMA_VERSION = 1
        const val FORMAT_NAME = "notifast-backup"
        const val MANIFEST_FILE = "manifest.json"
        const val SUBJECT_FILE = "subject.json"
        const val GLOBAL_FILE = "global.json"
        const val AVATAR_FILE = "avatar.jpg"
        const val IMAGES_DIR = "images"
        private const val UTC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    }

    private data class GlobalDataDto(
        val firstName: String = "",
        val lastName: String = "",
        val prefs: Map<String, String> = emptyMap(),
        val teachers: List<Teacher> = emptyList(),
        val hasAvatar: Boolean = false
    )

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

    private val gson: Gson = NoteBlockCodec.registerAdapters(
        GsonBuilder().registerTypeAdapter(Date::class.java, utcFormat)
    ).create()

    fun writeSubject(snapshot: SubjectSnapshot, out: File, workDir: File): Result<Unit> {
        val stageDir = File(workDir, "stage_sub_${System.currentTimeMillis()}_${(0..99999).random()}")
        return try {
            stageDir.mkdirs()
            val imagesDir = File(stageDir, IMAGES_DIR).apply { mkdirs() }

            // 1. Collect all referenced images and optimize into images/<sha1>.jpg
            val pathMapping = mutableMapOf<String, String>()

            fun processImage(path: String?) {
                if (path.isNullOrBlank() || pathMapping.containsKey(path)) return
                val file = snapshot.imageFiles[path] ?: File(path.removePrefix("file://"))
                if (file.exists() && file.isFile && file.length() > 0) {
                    val sha1 = hasher.sha1(file)
                    if (sha1 != "missing") {
                        val relPath = "$IMAGES_DIR/$sha1.jpg"
                        val target = File(imagesDir, "$sha1.jpg")
                        if (!target.exists()) {
                            val optResult = optimizer.optimize(file, target)
                            if (optResult.isFailure) {
                                file.copyTo(target, overwrite = true)
                            }
                        }
                        pathMapping[path] = relPath
                    }
                }
            }

            snapshot.evaluations.forEach { processImage(it.imageUrl) }
            snapshot.notes.forEach { note ->
                processImage(note.imageUrl)
                note.blocks.forEach { block ->
                    if (block is NoteBlock.Image) {
                        processImage(block.path)
                    }
                }
            }

            // 2. Rewrite evaluations
            val rewrittenEvaluations = snapshot.evaluations.map { e ->
                val newUrl = e.imageUrl?.let { pathMapping[it] }
                e.copy(imageUrl = newUrl)
            }

            // 3. Rewrite notes
            val rewrittenNotes = snapshot.notes.map { note ->
                val newNoteImg = note.imageUrl?.let { pathMapping[it] }
                val newBlocks = note.blocks.mapNotNull { block ->
                    if (block is NoteBlock.Image) {
                        val newPath = pathMapping[block.path]
                        if (newPath != null) {
                            block.copy(path = newPath)
                        } else {
                            null // missing image block removed
                        }
                    } else {
                        block
                    }
                }
                note.copy(imageUrl = newNoteImg, blocks = newBlocks)
            }

            // 4. Write subject.json
            val pkg = SubjectPackage(
                schemaVersion = 1,
                exportTimestamp = System.currentTimeMillis(),
                subject = snapshot.subject,
                schedules = snapshot.schedules,
                evaluations = rewrittenEvaluations,
                notes = rewrittenNotes
            )
            File(stageDir, SUBJECT_FILE).writeText(gson.toJson(pkg), Charsets.UTF_8)

            // 5. Write manifest.json
            val manifest = BackupManifest(
                format = FORMAT_NAME,
                schemaVersion = BACKUP_SCHEMA_VERSION,
                kind = "subject",
                createdAt = System.currentTimeMillis()
            )
            File(stageDir, MANIFEST_FILE).writeText(gson.toJson(manifest), Charsets.UTF_8)

            // 6. Zip stageDir to out
            out.parentFile?.mkdirs()
            zipDirectory(stageDir, out)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            stageDir.deleteRecursively()
        }
    }

    fun readSubject(zip: File, extractDir: File): Result<SubjectSnapshot> {
        return try {
            if (!zip.exists() || !zip.isFile) {
                return Result.failure(IllegalArgumentException("Zip file does not exist: ${zip.path}"))
            }
            extractDir.mkdirs()
            unzip(zip, extractDir)

            // 1. Validate manifest
            val manifestFile = File(extractDir, MANIFEST_FILE)
            if (!manifestFile.exists()) {
                return Result.failure(IllegalArgumentException("Archive missing $MANIFEST_FILE"))
            }
            val manifest = gson.fromJson(manifestFile.readText(Charsets.UTF_8), BackupManifest::class.java)
                ?: return Result.failure(IllegalArgumentException("Corrupted $MANIFEST_FILE"))

            if (manifest.format != FORMAT_NAME) {
                return Result.failure(IllegalArgumentException("Invalid format: ${manifest.format}"))
            }
            if (manifest.schemaVersion > BACKUP_SCHEMA_VERSION) {
                return Result.failure(UnsupportedBackupException(manifest.schemaVersion))
            }
            if (manifest.kind != "subject") {
                return Result.failure(IllegalArgumentException("Expected subject backup, found: ${manifest.kind}"))
            }

            // 2. Read subject.json
            val subjectFile = File(extractDir, SUBJECT_FILE)
            if (!subjectFile.exists()) {
                return Result.failure(IllegalArgumentException("Archive missing $SUBJECT_FILE"))
            }
            val pkg = gson.fromJson(subjectFile.readText(Charsets.UTF_8), SubjectPackage::class.java)
                ?: return Result.failure(IllegalArgumentException("Corrupted $SUBJECT_FILE"))

            // 3. Map images
            val imagesDir = File(extractDir, IMAGES_DIR)
            val imageFilesMap = mutableMapOf<String, File>()
            if (imagesDir.exists() && imagesDir.isDirectory) {
                imagesDir.listFiles()?.forEach { imgFile ->
                    imageFilesMap["$IMAGES_DIR/${imgFile.name}"] = imgFile
                }
            }

            Result.success(
                SubjectSnapshot(
                    subject = pkg.subject,
                    schedules = pkg.schedules,
                    evaluations = pkg.evaluations,
                    notes = pkg.notes,
                    imageFiles = imageFilesMap
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun writeGlobal(snapshot: GlobalSnapshot, out: File, workDir: File): Result<Unit> {
        val stageDir = File(workDir, "stage_global_${System.currentTimeMillis()}_${(0..99999).random()}")
        return try {
            stageDir.mkdirs()

            var hasAvatar = false
            if (snapshot.avatar != null && snapshot.avatar.exists() && snapshot.avatar.isFile) {
                val targetAvatar = File(stageDir, AVATAR_FILE)
                val optResult = optimizer.optimize(snapshot.avatar, targetAvatar)
                if (optResult.isFailure) {
                    snapshot.avatar.copyTo(targetAvatar, overwrite = true)
                }
                hasAvatar = targetAvatar.exists()
            }

            val dto = GlobalDataDto(
                firstName = snapshot.firstName,
                lastName = snapshot.lastName,
                prefs = snapshot.prefs,
                teachers = snapshot.teachers,
                hasAvatar = hasAvatar
            )
            File(stageDir, GLOBAL_FILE).writeText(gson.toJson(dto), Charsets.UTF_8)

            val manifest = BackupManifest(
                format = FORMAT_NAME,
                schemaVersion = BACKUP_SCHEMA_VERSION,
                kind = "global",
                createdAt = System.currentTimeMillis()
            )
            File(stageDir, MANIFEST_FILE).writeText(gson.toJson(manifest), Charsets.UTF_8)

            out.parentFile?.mkdirs()
            zipDirectory(stageDir, out)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            stageDir.deleteRecursively()
        }
    }

    fun readGlobal(zip: File, extractDir: File): Result<GlobalSnapshot> {
        return try {
            if (!zip.exists() || !zip.isFile) {
                return Result.failure(IllegalArgumentException("Zip file does not exist: ${zip.path}"))
            }
            extractDir.mkdirs()
            unzip(zip, extractDir)

            val manifestFile = File(extractDir, MANIFEST_FILE)
            if (!manifestFile.exists()) {
                return Result.failure(IllegalArgumentException("Archive missing $MANIFEST_FILE"))
            }
            val manifest = gson.fromJson(manifestFile.readText(Charsets.UTF_8), BackupManifest::class.java)
                ?: return Result.failure(IllegalArgumentException("Corrupted $MANIFEST_FILE"))

            if (manifest.format != FORMAT_NAME) {
                return Result.failure(IllegalArgumentException("Invalid format: ${manifest.format}"))
            }
            if (manifest.schemaVersion > BACKUP_SCHEMA_VERSION) {
                return Result.failure(UnsupportedBackupException(manifest.schemaVersion))
            }
            if (manifest.kind != "global") {
                return Result.failure(IllegalArgumentException("Expected global backup, found: ${manifest.kind}"))
            }

            val globalFile = File(extractDir, GLOBAL_FILE)
            if (!globalFile.exists()) {
                return Result.failure(IllegalArgumentException("Archive missing $GLOBAL_FILE"))
            }
            val dto = gson.fromJson(globalFile.readText(Charsets.UTF_8), GlobalDataDto::class.java)
                ?: return Result.failure(IllegalArgumentException("Corrupted $GLOBAL_FILE"))

            val avatarFile = File(extractDir, AVATAR_FILE)
            val avatar = if (dto.hasAvatar && avatarFile.exists()) avatarFile else null

            Result.success(
                GlobalSnapshot(
                    firstName = dto.firstName,
                    lastName = dto.lastName,
                    avatar = avatar,
                    prefs = dto.prefs,
                    teachers = dto.teachers
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun zipDirectory(sourceDir: File, zipFile: File) {
        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
            sourceDir.walkTopDown().forEach { file ->
                if (file.isFile) {
                    val relativePath = file.relativeTo(sourceDir).path.replace('\\', '/')
                    zos.putNextEntry(ZipEntry(relativePath))
                    FileInputStream(file).use { fis ->
                        fis.copyTo(zos)
                    }
                    zos.closeEntry()
                }
            }
        }
    }

    private fun unzip(zipFile: File, targetDir: File) {
        val destCanonicalDir = targetDir.canonicalFile
        ZipInputStream(FileInputStream(zipFile)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val newFile = File(destCanonicalDir, entry.name).canonicalFile
                if (!newFile.path.startsWith(destCanonicalDir.path + File.separator) && newFile != destCanonicalDir) {
                    throw SecurityException("Zip slip detected in entry: ${entry.name}")
                }
                if (entry.isDirectory) {
                    newFile.mkdirs()
                } else {
                    newFile.parentFile?.mkdirs()
                    FileOutputStream(newFile).use { fos ->
                        zis.copyTo(fos)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}
