package com.notifast.app.architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OfflineArchitectureBoundaryTest {

    private fun getSourceRoot(): File {
        val rootDir = when {
            File("src/main/java/com/notifast/app").exists() -> File("src/main/java/com/notifast/app")
            File("app/src/main/java/com/notifast/app").exists() -> File("app/src/main/java/com/notifast/app")
            File("../app/src/main/java/com/notifast/app").exists() -> File("../app/src/main/java/com/notifast/app")
            else -> File("src/main/java/com/notifast/app")
        }
        assertTrue("Source directory must exist at ${rootDir.absolutePath}", rootDir.exists() && rootDir.isDirectory)
        return rootDir
    }

    @Test
    fun `no active presentation, domain or local data files import firestore`() {
        val sourceRoot = getSourceRoot()
        val excludedDirectories = listOf(
            "data/remote",
            "di/AppModule"
        )
        val excludedLegacyFiles = setOf(
            "NoteRepositoryImpl.kt",
            "EvaluationRepositoryImpl.kt",
            "ScheduleRepositoryImpl.kt",
            "AuthRepositoryImpl.kt"
        )

        val violatingFiles = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filterNot { file ->
                val normalizedPath = file.path.replace('\\', '/')
                file.name in excludedLegacyFiles || excludedDirectories.any { normalizedPath.contains(it) }
            }
            .filter { file ->
                file.readText().contains("com.google.firebase.firestore")
            }
            .map { it.relativeTo(sourceRoot).path.replace('\\', '/') }
            .toList()

        assertTrue(
            "Found files violating offline-first firestore boundary: $violatingFiles",
            violatingFiles.isEmpty()
        )
    }

    @Test
    fun `no presentation or domain files import firebase auth`() {
        val sourceRoot = getSourceRoot()
        val targetDirs = listOf(
            File(sourceRoot, "presentation"),
            File(sourceRoot, "domain")
        )

        val violatingFiles = targetDirs.flatMap { dir ->
            if (dir.exists()) {
                dir.walkTopDown()
                    .filter { it.isFile && it.extension == "kt" }
                    .filter { file ->
                        file.readText().contains("com.google.firebase.auth")
                    }
                    .map { it.relativeTo(sourceRoot).path.replace('\\', '/') }
                    .toList()
            } else {
                emptyList()
            }
        }

        assertTrue(
            "Found presentation or domain files importing Firebase Auth: $violatingFiles",
            violatingFiles.isEmpty()
        )
    }
}
