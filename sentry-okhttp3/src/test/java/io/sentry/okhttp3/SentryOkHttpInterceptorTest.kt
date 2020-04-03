package io.sentry.okhttp3

import com.nhaarman.mockitokotlin2.mock
import io.sentry.core.Breadcrumb
import io.sentry.core.Sentry
import io.sentry.core.SentryOptions
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class SentryOkHttpInterceptorTest {

    private lateinit var file: File

    @BeforeTest
    fun `set up`() {
        file = Files.createTempDirectory("sentry-disk-cache-test").toAbsolutePath().toFile()
    }

    @AfterTest
    fun shutdown() {
        Files.delete(file.toPath())
    }

    @Test
    fun `when okhttp makes request, we log it`() {
        var bcCounter = 0
        Sentry.init {
            it.cacheDirPath = file.absolutePath
            it.beforeBreadcrumb = SentryOptions.BeforeBreadcrumbCallback {
                breadcrumb: Breadcrumb, _: Any? ->
                bcCounter++
                breadcrumb
            }
            it.dsn = "https://key@sentry.io/proj"
            it.setSerializer(mock())
        }

//        1) breadcrumb
//        Sending request http://www.publicobject.com/helloworld.txt on null
//        User-Agent: OkHttp Example
//
//        2) breadcrumb
//        Received response for https://publicobject.com/helloworld.txt in 28591.7ms
//        Server: nginx/1.10.0 (Ubuntu)
//        Date: Mon, 02 Mar 2020 17:42:12 GMT
//        Content-Type: text/plain
//        Content-Length: 1759
//        Last-Modified: Tue, 27 May 2014 02:35:47 GMT
//        Connection: keep-alive
//        ETag: "5383fa03-6df"
//        Accept-Ranges: bytes

        // TODO: use https://github.com/square/okhttp/tree/master/mockwebserver so we can test on the JVM
        // and test wont be flaky if theres no internet
        val client = OkHttpClient.Builder()
            .addInterceptor(SentryOkHttpInterceptor())
            .build()
        val request: Request = Request.Builder()
            .url("http://www.publicobject.com/helloworld.txt")
            .header("User-Agent", "OkHttp Example")
            .build()

        val response: Response = client.newCall(request).execute()
        response.body()?.close()
        assertEquals(2, bcCounter)
    }
}
