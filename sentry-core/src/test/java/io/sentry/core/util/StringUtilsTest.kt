package io.sentry.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class StringUtilsTest {
    @Test
    fun `when str has full package, return last string after dot`() {
        assertEquals("TEST", StringUtils.getStringAfterDot("io.sentry.TEST"))
    }

    @Test
    fun `when str is null, return null`() {
        assertNull(StringUtils.getStringAfterDot(null))
    }

    @Test
    fun `when str is empty, return the original str`() {
        assertEquals("", StringUtils.getStringAfterDot(""))
    }

    @Test
    fun `when str ends with a dot, return the original str`() {
        assertEquals("io.sentry.", StringUtils.getStringAfterDot("io.sentry."))
    }

    @Test
    fun `when str has no dots, return the original str`() {
        assertEquals("iosentry", StringUtils.getStringAfterDot("iosentry"))
    }
}
