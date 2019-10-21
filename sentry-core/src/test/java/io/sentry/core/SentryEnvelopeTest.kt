package io.sentry.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import io.sentry.core.protocol.SentryId
import java.io.InputStream
import java.io.Writer
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.apache.commons.io.IOUtils
import org.mockito.Mockito.`when`

class SentryEnvelopeTest {

    // TODO: test getLength + start index larger than available bytes

    @Test
    fun `deserialize sample envelope with event and two attachments`() {
        val envelopeReader = EnvelopeReader(TestSerializer())
        val testFile = this::class.java.classLoader.getResource("envelope-event-attachment.txt")
        val stream = testFile!!.openStream()
        val envelope = envelopeReader.read(stream)
        assertNotNull(envelope)
        assertEquals("9ec79c33ec9942ab8353589fcb2e04dc", envelope.header.eventId.toString())
        assertEquals(3, envelope.items.count())
        val firstItem = envelope.items.elementAt(0)
        assertEquals("event", firstItem.header.type)
        assertEquals("application/json", firstItem.header.contentType)
        assertEquals(107, firstItem.header.length)
        assertEquals(107, firstItem.data.size)
        assertNull(firstItem.header.fileName)
        val secondItem = envelope.items.elementAt(1)
        assertEquals("attachment", secondItem.header.type)
        assertEquals("text/plain", secondItem.header.contentType)
        assertEquals(61, secondItem.header.length)
        assertEquals(61, secondItem.data.size)
        assertEquals("attachment.txt", secondItem.header.fileName)
        val thirdItem = envelope.items.elementAt(2)
        assertEquals("attachment", thirdItem.header.type)
        assertEquals("text/plain", thirdItem.header.contentType)
        assertEquals(29, thirdItem.header.length)
        assertEquals(29, thirdItem.data.size)
        assertEquals("log.txt", thirdItem.header.fileName)
    }

    @Test
    fun `when envelope is empty, reader throws illegal argument`() {
        val envelopeReader = EnvelopeReader(TestSerializer())
        var stream = mock<InputStream>()
        `when`(stream.read(any())).thenReturn(-1)
        val exception = assertFailsWith<IllegalArgumentException> { envelopeReader.read(stream) }
        assertEquals("Empty stream.", exception.message)
    }

    @Test
    fun `when envelope has no line break, reader throws illegal argument`() {
        val envelopeReader = EnvelopeReader(TestSerializer())
        val stream = IOUtils.toInputStream("{}", StandardCharsets.UTF_8)
        val exception = assertFailsWith<IllegalArgumentException> { envelopeReader.read(stream) }
        assertEquals("Envelope contains no header.", exception.message)
    }

    @Test
    fun `when envelope header has no event_id, reader throws illegal argument`() {
        val envelopeReader = EnvelopeReader(TestSerializer())
        val stream = IOUtils.toInputStream("{}\n{\"item_header\":\"value\",\"length\":\"2\"}\n{}", StandardCharsets.UTF_8)
        val exception = assertFailsWith<IllegalArgumentException> { envelopeReader.read(stream) }
        assertEquals("Envelope header is missing required 'event_id'.", exception.message)
    }

    @Test
    fun `when envelope has only a header without line break, reader throws illegal argument`() {
        val envelopeReader = EnvelopeReader(TestSerializer())
        val stream = IOUtils.toInputStream("{\"event_id\":\"9ec79c33ec9942ab8353589fcb2e04dc\"}", StandardCharsets.UTF_8)
        val exception = assertFailsWith<IllegalArgumentException> { envelopeReader.read(stream) }
        assertEquals("Envelope contains no header.", exception.message)
    }

    @Test
    fun `when envelope has only a header and line break, reader throws illegal argument`() {
        val envelopeReader = EnvelopeReader(TestSerializer())
        val stream = IOUtils.toInputStream("{\"event_id\":\"9ec79c33ec9942ab8353589fcb2e04dc\"}\n", StandardCharsets.UTF_8)
        val exception = assertFailsWith<IllegalArgumentException> { envelopeReader.read(stream) }
        assertEquals("Invalid envelope. Item at index '0'. has no header delimiter.", exception.message)
    }

    @Test
    fun `when envelope has the first item missing length, reader throws illegal argument`() {
        val envelopeReader = EnvelopeReader(TestSerializer())
        val stream = IOUtils.toInputStream("""{"event_id":"9ec79c33ec9942ab8353589fcb2e04dc"}
{"content-type":"application/json","type":"event"}
{}""", StandardCharsets.UTF_8)
        val exception = assertFailsWith<IllegalArgumentException> { envelopeReader.read(stream) }
        assertEquals("Item header at index '0' has an invalid value: '0'.", exception.message)
    }

    @Test
    fun `when envelope two items, returns envelope with items`() {
        val envelopeReader = EnvelopeReader(TestSerializer())
        val stream = IOUtils.toInputStream("""{"event_id":"9ec79c33ec9942ab8353589fcb2e04dc"}
{"type":"event","length":"2"}
{}
{"content_type":"application/octet-stream","type":"attachment","length":"10","filename":"null.bin"}
abcdefghi""", StandardCharsets.UTF_8)
        val envelope = envelopeReader.read(stream)

        assertNotNull(envelope)
        assertEquals("9ec79c33ec9942ab8353589fcb2e04dc", envelope.header.eventId.toString())
        assertEquals(2, envelope.items.count())
        val firstItem = envelope.items.first()
        assertEquals("event", firstItem.header.type)
        assertNull(firstItem.header.contentType)
        assertEquals(2, firstItem.header.length)
        assertEquals(2, firstItem.data.size)
        assertNull(firstItem.header.fileName)
        val secondItem = envelope.items.last()
        assertEquals("attachment", secondItem.header.type)
        assertEquals("application/octet-stream", secondItem.header.contentType)
        assertEquals("null.bin", secondItem.header.fileName)
        assertEquals(10, secondItem.header.length)
        assertEquals(10, secondItem.data.size)
    }

    class TestSerializer : ISerializer {
        override fun serialize(event: SentryEvent?, writer: Writer?) {
            TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
        }

        private val gson: Gson = GsonBuilder()
            .registerTypeAdapter(SentryEnvelopeHeader::class.java, SentryEnvelopeHeaderAdapter())
            .registerTypeAdapter(SentryEnvelopeItemHeader::class.java, SentryEnvelopeItemHeaderAdapter())
            .create()

        override fun deserializeEvent(envelope: String?): SentryEvent {
            TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
        }

        override fun deserializeEnvelopeHeader(buffer: ByteArray?, offset: Int, length: Int): SentryEnvelopeHeader {
            val json = String(buffer!!, offset, length, StandardCharsets.UTF_8)
            return gson.fromJson<SentryEnvelopeHeader>(json, SentryEnvelopeHeader::class.java)
        }

        override fun deserializeEnvelopeItemHeader(buffer: ByteArray?, offset: Int, length: Int): SentryEnvelopeItemHeader {
            val json = String(buffer!!, offset, length, StandardCharsets.UTF_8)
            return gson.fromJson<SentryEnvelopeItemHeader>(json, SentryEnvelopeItemHeader::class.java)
        }

        internal class SentryEnvelopeHeaderAdapter : TypeAdapter<SentryEnvelopeHeader?>() {
            override fun write(out: JsonWriter?, value: SentryEnvelopeHeader?) {
                TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
            }

            override fun read(readerNullable: JsonReader?): SentryEnvelopeHeader? {
                    val reader = readerNullable!!
                    var sentryId = SentryId.EMPTY_ID
                    var auth: String? = null

                    reader.beginObject()
                    while (reader.hasNext()) {
                        // TODO: test when no key matches
                        when (reader.nextName()) {
                            "event_id" -> sentryId = SentryId(reader.nextString())
                            "auth" -> auth = reader.nextString()
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()

                    return SentryEnvelopeHeader(sentryId, auth)
                }
            }
        }

    internal class SentryEnvelopeItemHeaderAdapter : TypeAdapter<SentryEnvelopeItemHeader?>() {
        override fun write(out: JsonWriter?, value: SentryEnvelopeItemHeader?) {
            TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
        }

        override fun read(readerNullable: JsonReader?): SentryEnvelopeItemHeader? {

            val reader = readerNullable!!
            var contentType: String? = null
            var fileName: String? = null
            var type: String? = null
            var length = 0

            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.nextName()) {
                    "content_type" -> contentType = reader.nextString()
                    "filename" -> fileName = reader.nextString()
                    "type" -> type = reader.nextString()
                    "length" -> length = reader.nextInt()
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            return SentryEnvelopeItemHeader(type, length, contentType, fileName)
        }
    }
}
