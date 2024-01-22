package com.e13mort.palantir.cloc

import io.kotest.matchers.shouldBe
import org.junit.Test
import java.io.FileInputStream
import kotlin.test.assertNotNull

class ClocAdapterTest {
    @Test
    fun `test cloc json parsing`() {
        val adapter = ClocAdapter.create()
        val inputStream =
            FileInputStream(javaClass.classLoader.getResource("cloc-output.json")!!.file)
        val calculate = adapter.decodeResult(inputStream)
        assertNotNull(calculate)
    }

    @Test
    fun `correct cloc command with empty args`() {
        val adapter = ClocAdapter.create()
        adapter.buildClocCommand(emptyList()) shouldBe "cloc . --json"
    }

    @Test
    fun `correct cloc command with one arg`() {
        val adapter = ClocAdapter.create()
        adapter.buildClocCommand(listOf("dirA")) shouldBe "cloc . --json --fullpath --not-match-d=(dirA)"
    }

    @Test
    fun `correct cloc command with two args`() {
        val adapter = ClocAdapter.create()
        adapter.buildClocCommand(listOf("dirA", "dirB.*/")) shouldBe "cloc . --json --fullpath --not-match-d=(dirA|dirB.*/)"
    }
}