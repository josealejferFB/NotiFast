package com.notifast.app.data.remote.drive

import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DriveRestClientTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var server: MockWebServer
    private lateinit var client: DriveRestClient
    private val testDispatcher = StandardTestDispatcher()
    private val gson = Gson()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        val okHttpClient = OkHttpClient.Builder().build()
        client = DriveRestClient(
            http = okHttpClient,
            gson = gson,
            baseUrl = server.url("/"),
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun list_sendsCorrectHeadersAndQuery_andParsesFiles() = runTest(testDispatcher) {
        val jsonResponse = """
            {
              "files": [
                {
                  "id": "file1",
                  "name": "subject_math.nfbak",
                  "size": "1024",
                  "modifiedTime": "2026-10-15T12:00:00Z",
                  "appProperties": {
                    "kind": "subject",
                    "subjectId": "sub1"
                  }
                },
                {
                  "id": "file2",
                  "name": "global.nfbak",
                  "size": "2048",
                  "modifiedTime": "2026-10-15T13:00:00Z",
                  "appProperties": {
                    "kind": "global"
                  }
                }
              ]
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val result = client.list("test-token")

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
        val requestUrl = request.requestUrl!!
        assertEquals("appDataFolder", requestUrl.queryParameter("spaces"))
        assertTrue(requestUrl.queryParameter("fields")!!.contains("files(id,name,size,modifiedTime,appProperties)"))
        assertEquals(2, result.size)
        assertEquals("file1", result[0].id)
        assertEquals(1024L, result[0].size)
        assertEquals("sub1", result[0].appProperties["subjectId"])
        assertEquals("file2", result[1].id)
    }

    @Test
    fun list_followsNextPageToken_acrossMultiplePages() = runTest(testDispatcher) {
        val page1 = """
            {
              "nextPageToken": "page2token",
              "files": [
                { "id": "p1f1", "name": "f1.nfbak", "size": "100" }
              ]
            }
        """.trimIndent()
        val page2 = """
            {
              "files": [
                { "id": "p2f2", "name": "f2.nfbak", "size": "200" }
              ]
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(page1))
        server.enqueue(MockResponse().setResponseCode(200).setBody(page2))

        val result = client.list("test-token")

        assertEquals(2, result.size)
        assertEquals("p1f1", result[0].id)
        assertEquals("p2f2", result[1].id)

        val req1 = server.takeRequest()
        val req2 = server.takeRequest()
        assertEquals(null, req1.requestUrl!!.queryParameter("pageToken"))
        assertEquals("page2token", req2.requestUrl!!.queryParameter("pageToken"))
    }

    @Test
    fun create_sendsMultipartRelated_withAppDataFolderAndAppProperties() = runTest(testDispatcher) {
        val responseJson = """
            {
              "id": "newFileId",
              "name": "subject_123.nfbak",
              "size": "500",
              "modifiedTime": "2026-10-15T10:00:00Z",
              "appProperties": {
                "kind": "subject",
                "subjectId": "123"
              }
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseJson))

        val testFile = tempFolder.newFile("test.nfbak").apply {
            writeBytes("test binary payload".toByteArray())
        }

        val result = client.create(
            token = "test-token",
            name = "subject_123.nfbak",
            props = mapOf("kind" to "subject", "subjectId" to "123"),
            file = testFile
        )

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
        val contentType = request.getHeader("Content-Type")!!
        assertTrue(contentType.startsWith("multipart/related"))
        assertEquals("/upload/drive/v3/files", request.requestUrl!!.encodedPath)
        assertEquals("multipart", request.requestUrl!!.queryParameter("uploadType"))

        val bodyString = request.body.readUtf8()
        assertTrue(bodyString.contains("\"parents\":[\"appDataFolder\"]"))
        assertTrue(bodyString.contains("\"name\":\"subject_123.nfbak\""))
        assertTrue(bodyString.contains("\"subjectId\":\"123\""))
        assertTrue(bodyString.contains("test binary payload"))

        assertEquals("newFileId", result.id)
        assertEquals("123", result.appProperties["subjectId"])
    }

    @Test
    fun update_sendsPatchMultipartRelated_andReturnsDto() = runTest(testDispatcher) {
        val responseJson = """
            {
              "id": "file999",
              "name": "subject_123.nfbak",
              "size": "600",
              "modifiedTime": "2026-10-15T11:00:00Z",
              "appProperties": {
                "kind": "subject",
                "contentHash": "newhash"
              }
            }
        """.trimIndent()
        server.enqueue(MockResponse().setResponseCode(200).setBody(responseJson))

        val testFile = tempFolder.newFile("update.nfbak").apply {
            writeBytes("updated bytes".toByteArray())
        }

        val result = client.update(
            token = "test-token",
            fileId = "file999",
            props = mapOf("kind" to "subject", "contentHash" to "newhash"),
            file = testFile
        )

        val request = server.takeRequest()
        assertEquals("PATCH", request.method)
        assertEquals("/upload/drive/v3/files/file999", request.requestUrl!!.encodedPath)
        val bodyString = request.body.readUtf8()
        assertTrue(bodyString.contains("newhash"))
        assertTrue(bodyString.contains("updated bytes"))
        assertEquals("file999", result.id)
        assertEquals("newhash", result.appProperties["contentHash"])
    }

    @Test
    fun download_writesMediaStreamToTargetFile() = runTest(testDispatcher) {
        val targetFile = File(tempFolder.root, "downloaded.nfbak")
        val content = "downloaded file content"
        server.enqueue(MockResponse().setResponseCode(200).setBody(content))

        client.download("test-token", "file123", targetFile)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/drive/v3/files/file123", request.requestUrl!!.encodedPath)
        assertEquals("media", request.requestUrl!!.queryParameter("alt"))
        assertEquals(content, targetFile.readText())
    }

    @Test
    fun delete_handles204NoContent() = runTest(testDispatcher) {
        server.enqueue(MockResponse().setResponseCode(204))

        client.delete("test-token", "file123")

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/drive/v3/files/file123", request.requestUrl!!.encodedPath)
    }

    @Test
    fun delete_handles404WithoutThrowing() = runTest(testDispatcher) {
        server.enqueue(MockResponse().setResponseCode(404).setBody("Not found"))

        // Should not throw
        client.delete("test-token", "missingFile")

        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
    }

    @Test
    fun request_when401_throwsDriveExceptionWithCode401() = runTest(testDispatcher) {
        server.enqueue(MockResponse().setResponseCode(401).setBody("""
            {
              "error": {
                "code": 401,
                "message": "Invalid Credentials"
              }
            }
        """.trimIndent()))

        try {
            client.list("expired-token")
            fail("Expected DriveException")
        } catch (e: DriveException) {
            assertEquals(401, e.code)
            assertEquals("Invalid Credentials", e.reason)
        }
    }

    @Test
    fun request_when403StorageQuotaExceeded_parsesReason() = runTest(testDispatcher) {
        server.enqueue(MockResponse().setResponseCode(403).setBody("""
            {
              "error": {
                "code": 403,
                "message": "The user's Drive storage quota has been exceeded.",
                "errors": [
                  {
                    "domain": "usageLimits",
                    "reason": "storageQuotaExceeded",
                    "message": "The user's Drive storage quota has been exceeded."
                  }
                ]
              }
            }
        """.trimIndent()))

        val dummyFile = tempFolder.newFile("dummy.bin")

        try {
            client.create("test-token", "test.bin", emptyMap(), dummyFile)
            fail("Expected DriveException")
        } catch (e: DriveException) {
            assertEquals(403, e.code)
            assertEquals("storageQuotaExceeded", e.reason)
        }
    }

    @Test
    fun delete_when500_throwsDriveException() = runTest(testDispatcher) {
        server.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        try {
            client.delete("test-token", "file123")
            fail("Expected DriveException")
        } catch (e: DriveException) {
            assertEquals(500, e.code)
        }
    }
}
