package com.wxn.reader.domain.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 方案 docs/plans/2026-09-13-plan-opds-remove-wenyuange-and-add-catalog-url-assist.md §6.1
 * 覆盖 parse / compose / isValid 的边界（端口、IPv6、query、尾斜线、空白）。
 */
class OpdsUrlAssistTest {

    // ---------- parse ----------

    @Test
    fun `parse detects pasted https scheme and keeps path`() {
        val parsed = OpdsUrlAssist.parse("https://m.gutenberg.org/opds/")
        assertEquals(OpdsUrlAssist.SCHEME_HTTPS, parsed.scheme)
        assertEquals("m.gutenberg.org/opds/", parsed.hostPath)
        assertTrue(parsed.valid)
    }

    @Test
    fun `parse lowercases detected scheme and preserves hostPath case`() {
        val parsed = OpdsUrlAssist.parse("HTTP://A.B")
        assertEquals(OpdsUrlAssist.SCHEME_HTTP, parsed.scheme)
        assertEquals("A.B", parsed.hostPath)
        assertTrue(parsed.valid)
    }

    @Test
    fun `parse rejects non http scheme`() {
        val parsed = OpdsUrlAssist.parse("ftp://x.com")
        assertFalse(parsed.valid)
    }

    @Test
    fun `parse rejects scheme only input`() {
        val parsed = OpdsUrlAssist.parse("https://")
        assertEquals(OpdsUrlAssist.SCHEME_HTTPS, parsed.scheme)
        assertEquals("", parsed.hostPath)
        assertFalse(parsed.valid)
    }

    @Test
    fun `parse input without scheme is valid when non-blank`() {
        val parsed = OpdsUrlAssist.parse(" m.gutenberg.org ")
        assertNull(parsed.scheme)
        assertEquals("m.gutenberg.org", parsed.hostPath)
        assertTrue(parsed.valid)
    }

    @Test
    fun `parse cleans zero-width characters`() {
        val parsed = OpdsUrlAssist.parse("\u200Bm.gutenberg.org\uFEFF")
        assertEquals("m.gutenberg.org", parsed.hostPath)
        assertTrue(parsed.valid)
    }

    // ---------- compose ----------

    @Test
    fun `compose appends default path for host only`() {
        val composed = OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTPS, "m.gutenberg.org")
        assertEquals("https://m.gutenberg.org/opds", composed!!.url)
        assertTrue(composed.pathAutoCompleted)
    }

    @Test
    fun `compose treats trailing slash as no path`() {
        val composed = OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTPS, "m.gutenberg.org/")
        assertEquals("https://m.gutenberg.org/opds", composed!!.url)
        assertTrue(composed.pathAutoCompleted)
    }

    @Test
    fun `compose keeps host with port and appends default path`() {
        val composed = OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTP, "192.168.1.5:8080")
        assertEquals("http://192.168.1.5:8080/opds", composed!!.url)
        assertTrue(composed.pathAutoCompleted)
    }

    @Test
    fun `compose preserves explicit path verbatim`() {
        val composed = OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTPS, "m.gutenberg.org/ebooks.opds/")
        assertEquals("https://m.gutenberg.org/ebooks.opds/", composed!!.url)
        assertFalse(composed.pathAutoCompleted)
    }

    @Test
    fun `compose preserves explicit path with query verbatim`() {
        val composed = OpdsUrlAssist.compose(
            OpdsUrlAssist.SCHEME_HTTPS,
            "m.gutenberg.org/ebooks.opds/?format=opds"
        )
        assertEquals("https://m.gutenberg.org/ebooks.opds/?format=opds", composed!!.url)
        assertFalse(composed.pathAutoCompleted)
    }

    @Test
    fun `compose treats bare query as explicit and does not append`() {
        val composed = OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTPS, "manybooks.net?x=1")
        assertEquals("https://manybooks.net?x=1", composed!!.url)
        assertFalse(composed.pathAutoCompleted)
    }

    @Test
    fun `compose returns null for blank input`() {
        assertNull(OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTPS, ""))
    }

    @Test
    fun `compose returns null for input with spaces`() {
        assertNull(OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTPS, "a b"))
    }

    @Test
    fun `compose returns null for host without dot`() {
        assertNull(OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTPS, "abc"))
    }

    @Test
    fun `compose returns null for path containing space`() {
        assertNull(OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTPS, "a.b/has space"))
    }

    @Test
    fun `compose accepts localhost with port`() {
        val composed = OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTPS, "localhost:8080")
        assertEquals("https://localhost:8080/opds", composed!!.url)
        assertTrue(composed.pathAutoCompleted)
    }

    @Test
    fun `compose accepts ipv6 literal`() {
        val composed = OpdsUrlAssist.compose(OpdsUrlAssist.SCHEME_HTTP, "[::1]:8080")
        assertEquals("http://[::1]:8080/opds", composed!!.url)
        assertTrue(composed.pathAutoCompleted)
    }

    // ---------- isValid ----------

    @Test
    fun `isValid accepts http https domain ip localhost ipv6`() {
        assertTrue(OpdsUrlAssist.isValid("https://a.b/opds"))
        assertTrue(OpdsUrlAssist.isValid("http://192.168.1.5:8080/opds"))
        assertTrue(OpdsUrlAssist.isValid("http://localhost:8080/opds"))
        assertTrue(OpdsUrlAssist.isValid("http://[::1]:8080/opds"))
        assertTrue(OpdsUrlAssist.isValid("HTTPS://A.B/opds"))
    }

    @Test
    fun `isValid rejects malformed urls`() {
        assertFalse(OpdsUrlAssist.isValid(""))
        assertFalse(OpdsUrlAssist.isValid("ftp://a.b"))
        assertFalse(OpdsUrlAssist.isValid("https://a b"))
        assertFalse(OpdsUrlAssist.isValid("https://"))
    }
}
