package io.sentry.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.sentry.core.protocol.SentryId
import java.io.Writer
import java.nio.charset.StandardCharsets
import kotlin.test.Test
import kotlin.test.assertNotNull

class SentryEnvelopeTest {

    @Test
    fun `deserialize sample envelope`() {
        val envelopeReader = EnvelopeReader(TestSerializer())
        val testFile = this::class.java.classLoader.getResource("single-event-envelope.txt")
        val stream = testFile!!.openStream()
        val envelope = envelopeReader.read(stream)
        assertNotNull(envelope)
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
                    }
                }
                reader.endObject()

                return if (sentryId != SentryId.EMPTY_ID) SentryEnvelopeHeader(sentryId, auth) else null
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
                }
            }
            reader.endObject()

            return SentryEnvelopeItemHeader(type, length, contentType, fileName)
        }
    }
}
