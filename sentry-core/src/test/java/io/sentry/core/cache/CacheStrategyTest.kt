package io.sentry.core.cache

import com.nhaarman.mockitokotlin2.mock
import io.sentry.core.DateUtils
import io.sentry.core.GsonSerializer
import io.sentry.core.SentryEnvelope
import io.sentry.core.SentryOptions
import io.sentry.core.Session
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CacheStrategyTest {

    private class Fixture {
        val dir = Files.createTempDirectory("sentry-disk-cache-test").toAbsolutePath().toFile()
        val options = SentryOptions().apply {
            setSerializer(GsonSerializer(mock(), envelopeReader))
        }

        fun getSUT(maxSize: Int = 5): CacheStrategy {
            return CustomCache(options, dir.absolutePath, maxSize)
        }
    }

    private val fixture = Fixture()

    @Test
    fun `isDirectoryValid returns true if a valid directory`() {
        val sut = fixture.getSUT()

        // sanity check
        assertTrue(fixture.dir.isDirectory)

        // this test assumes that the dir. has write/read permission.
        assertTrue(sut.isDirectoryValid)
    }

    @Test
    fun `Sort files from the oldest to the newest`() {
        val sut = fixture.getSUT(3)

        val files = createTempFilesSortByOldestToNewest()
        val reverseFiles = files.reversedArray()

        sut.rotateCacheIfNeeded(reverseFiles)

        assertEquals(files[0].absolutePath, reverseFiles[0].absolutePath)
        assertEquals(files[1].absolutePath, reverseFiles[1].absolutePath)
        assertEquals(files[2].absolutePath, reverseFiles[2].absolutePath)
    }

    @Test
    fun `Rotate cache folder to save new file`() {
        val sut = fixture.getSUT(3)

        val files = createTempFilesSortByOldestToNewest()
        val reverseFiles = files.reversedArray()

        sut.rotateCacheIfNeeded(reverseFiles)

        assertFalse(files[0].exists())
        assertTrue(files[1].exists())
        assertTrue(files[2].exists())
    }

    @Test
    fun `do not move init flag if state is not ok`() {
        val sut = fixture.getSUT(3)

        val files = createTempFilesSortByOldestToNewest()

        val crashedSession = createSessionMockData(Session.State.Crashed, null)
        val crashedEnvelope = SentryEnvelope.fromSession(sut.serializer, crashedSession, null)
        sut.serializer.serialize(crashedEnvelope, files[0].writer())

        val exitedSession = createSessionMockData(Session.State.Exited, null)
        val exitedEnvelope = SentryEnvelope.fromSession(sut.serializer, exitedSession, null)
        sut.serializer.serialize(exitedEnvelope, files[1].writer())

        val abnormalSession = createSessionMockData(Session.State.Exited, null)
        val abnormalEnvelope = SentryEnvelope.fromSession(sut.serializer, abnormalSession, null)
        sut.serializer.serialize(abnormalEnvelope, files[2].writer())

        sut.rotateCacheIfNeeded(files)

        // files[0] has been deleted because of rotation
        for (i in 1..2) {
            val envelope = sut.serializer.deserializeEnvelope(files[i].inputStream())
            val item = envelope.items.first()

            val reader = InputStreamReader(ByteArrayInputStream(item.data), Charsets.UTF_8)
            val expectedSession = sut.serializer.deserializeSession(reader)

            assertNull(expectedSession.init)
        }
    }

    @Test
    fun `move init flag if state is ok`() {
        val sut = fixture.getSUT(3)

        val files = createTempFilesSortByOldestToNewest()

        val okSession = createSessionMockData(Session.State.Ok, true)
        val okEnvelope = SentryEnvelope.fromSession(sut.serializer, okSession, null)
        sut.serializer.serialize(okEnvelope, files[0].writer())

        val updatedOkSession = okSession.clone()
        updatedOkSession.update(null, null, true)
        val updatedOkEnvelope = SentryEnvelope.fromSession(sut.serializer, updatedOkSession, null)
        sut.serializer.serialize(updatedOkEnvelope, files[1].writer())

        val abnormalSession = createSessionMockData(Session.State.Exited, null)
        val abnormalEnvelope = SentryEnvelope.fromSession(sut.serializer, abnormalSession, null)
        sut.serializer.serialize(abnormalEnvelope, files[2].writer())

        sut.rotateCacheIfNeeded(files)

        // files[1] should be the one with the init flag true
        val envelope = sut.serializer.deserializeEnvelope(files[1].inputStream())
        val item = envelope.items.first()

        val reader = InputStreamReader(ByteArrayInputStream(item.data), Charsets.UTF_8)
        val expectedSession = sut.serializer.deserializeSession(reader)

        assertTrue(expectedSession.init!!)
    }

    @AfterTest
    fun shutdown() {
        fixture.dir.listFiles()?.forEach {
            it.deleteRecursively()
        }
    }

    private class CustomCache(options: SentryOptions, path: String, maxSize: Int) : CacheStrategy(options, path, maxSize)

    private fun createTempFilesSortByOldestToNewest(): Array<File> {
        val f1 = Files.createTempFile(fixture.dir.toPath(), "f1", ".json").toFile()
        f1.setLastModified(DateUtils.getDateTime("2020-03-27T08:52:58.015Z").time)

        val f2 = Files.createTempFile(fixture.dir.toPath(), "f2", ".json").toFile()
        f2.setLastModified(DateUtils.getDateTime("2020-03-27T08:52:59.015Z").time)

        val f3 = Files.createTempFile(fixture.dir.toPath(), "f3", ".json").toFile()
        f3.setLastModified(DateUtils.getDateTime("2020-03-27T08:53:00.015Z").time)

        return arrayOf(f1, f2, f3)
    }

    private fun createSessionMockData(state: Session.State = Session.State.Ok, init: Boolean? = true): Session =
            Session(
                    state,
                    DateUtils.getDateTime("2020-02-07T14:16:00.000Z"),
                    DateUtils.getDateTime("2020-02-07T14:16:00.000Z"),
                    2,
                    "123",
                    UUID.fromString("c81d4e2e-bcf2-11e6-869b-7df92533d2db"),
                    init,
                    123456.toLong(),
                    6000.toDouble(),
                    "127.0.0.1",
                    "jamesBond",
                    "debug",
                    "io.sentry@1.0+123"
            )
}
