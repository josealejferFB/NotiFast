package com.notifast.app.data.remote.drive

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DriveRestClientGoogleDocTest {

    private lateinit var server: MockWebServer
    private lateinit var client: DriveRestClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = DriveRestClient(
            http = OkHttpClient(),
            gson = com.google.gson.Gson(),
            baseUrl = server.url("/")
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `createGoogleDocFromHtml sends multipart request with vnd google-apps document mimeType and parses webViewLink`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"id":"gdoc-999","name":"Portada APA","webViewLink":"https://docs.google.com/document/d/gdoc-999/edit"}""")
        )

        val dto = client.createGoogleDocFromHtml(
            token = "token-xyz",
            title = "Portada APA",
            htmlContent = "<html><body><h1>Título</h1></body></html>"
        )

        assertEquals("gdoc-999", dto.id)
        assertEquals("https://docs.google.com/document/d/gdoc-999/edit", dto.webViewLink)

        val recorded = server.takeRequest()
        assertTrue(recorded.path!!.contains("upload/drive/v3/files"))
        val bodyStr = recorded.body.readUtf8()
        assertTrue(bodyStr.contains("application/vnd.google-apps.document"))
        assertFalse(bodyStr.contains("appDataFolder"))
        assertEquals("Bearer token-xyz", recorded.getHeader("Authorization"))
    }
}
