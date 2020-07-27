package io.sentry.core.cache

import com.nhaarman.mockitokotlin2.mock
import io.sentry.core.DateUtils
import io.sentry.core.SentryOptions
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CacheStrategyTest {

    private class Fixture {
        val dir = Files.createTempDirectory("sentry-disk-cache-test").toAbsolutePath().toFile()
        val options = SentryOptions().apply {
            setSerializer(mock())
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
        val sut = fixture.getSUT()

        val f1 = Files.createTempFile(fixture.dir.toPath(), "f1", ".json").toFile()
        f1.setLastModified(DateUtils.getDateTime("2020-03-27T08:52:58.015Z").time)

        val f2 = Files.createTempFile(fixture.dir.toPath(), "f2", ".json").toFile()
        f2.setLastModified(DateUtils.getDateTime("2020-03-27T08:52:59.015Z").time)

        val f3 = Files.createTempFile(fixture.dir.toPath(), "f3", ".json").toFile()
        f3.setLastModified(DateUtils.getDateTime("2020-03-27T08:53:00.015Z").time)

        val files = arrayOf(f3, f2, f1)

        sut.sortFilesOldestToNewest(files)

        assertEquals(files[0].absolutePath, f1.absolutePath)
        assertEquals(files[1].absolutePath, f2.absolutePath)
        assertEquals(files[2].absolutePath, f3.absolutePath)
    }

    @Test
    fun `Rotate cache folder to save new file`() {
        val sut = fixture.getSUT(3)

        val f1 = Files.createTempFile(fixture.dir.toPath(), "f1", ".json").toFile()
        f1.setLastModified(DateUtils.getDateTime("2020-03-27T08:52:58.015Z").time)

        val f2 = Files.createTempFile(fixture.dir.toPath(), "f2", ".json").toFile()
        f2.setLastModified(DateUtils.getDateTime("2020-03-27T08:52:59.015Z").time)

        val f3 = Files.createTempFile(fixture.dir.toPath(), "f3", ".json").toFile()
        f3.setLastModified(DateUtils.getDateTime("2020-03-27T08:53:00.015Z").time)

        val files = arrayOf(f3, f2, f1)

        sut.rotateCacheIfNeeded(files)

        assertFalse(f1.exists())
        assertTrue(f2.exists())
        assertTrue(f3.exists())
    }

    @AfterTest
    fun shutdown() {
        fixture.dir.listFiles()?.forEach {
            it.deleteRecursively()
        }
    }

    private class CustomCache(options: SentryOptions, path: String, maxSize: Int) : CacheStrategy(options, path, maxSize)
}
