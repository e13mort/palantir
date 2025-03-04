/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.git

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class MailMapTest {
    // https://git-scm.com/docs/gitmailmap
    @Test
    fun simpleNameMapping() {
        val mailMap = GitMailMap.create(listOf("Proper Name <commit@email.xx>"))
        mailMap.mapIdentity("commit@email.xx", "someName") should {
            it.email shouldBe "commit@email.xx"
            it.name shouldBe "Proper Name"
        }
    }

    @Test
    fun emailMapping() {
        val mailMap = GitMailMap.create(listOf("<proper@email.xx> <commit@email.xx>"))
        mailMap.mapIdentity("commit@email.xx", "someName") should {
            it.email shouldBe "proper@email.xx"
            it.name shouldBe "someName"
        }
    }

    @Test
    fun nameWithEmailMapping() {
        val mailMap = GitMailMap.create(listOf("Proper Name <proper@email.xx> <commit@email.xx>"))
        mailMap.mapIdentity("commit@email.xx", "someName") should {
            it.email shouldBe "proper@email.xx"
            it.name shouldBe "Proper Name"
        }
    }

    @Test
    fun complexMapping() {
        val mailMap =
            GitMailMap.create(listOf("Proper Name <proper@email.xx> Commit Name <commit@email.xx>"))
        mailMap.mapIdentity("commit@email.xx", "Commit Name") should {
            it.email shouldBe "proper@email.xx"
            it.name shouldBe "Proper Name"
        }
    }

    @Test
    fun caseSensitivityMapping() {
        val mailMap =
            GitMailMap.create(listOf("Proper Name <proper@email.xx> CoMmIt NaMe <CoMmIt@EmAiL.xX>"))
        mailMap.mapIdentity("commit@email.xx", "Commit Name") should {
            it.email shouldBe "proper@email.xx"
            it.name shouldBe "Proper Name"
        }
    }

    @Test
    fun fewRulesMapping() {
        val mailMap = GitMailMap.create(
            listOf(
                "author <author@some.email>",
                "author <author@some.email> <author@localhost.null>",
                "author <author@some.email> <root@author.another.email>"
            )
        )
        mailMap.mapIdentity("author@some.email", "Commit Name") should {
            it.email shouldBe "author@some.email"
            it.name shouldBe "author"
        }
        mailMap.mapIdentity("author@localhost.null", "Commit Name") should {
            it.email shouldBe "author@some.email"
            it.name shouldBe "author"
        }
        mailMap.mapIdentity("root@author.another.email", "Commit Name") should {
            it.email shouldBe "author@some.email"
            it.name shouldBe "author"
        }
        mailMap.mapIdentity("someEmail", "Commit Name") should {
            it.email shouldBe "someEmail"
            it.name shouldBe "Commit Name"
        }
    }


    @Test
    fun fullRulesMapping() {
        val mailMap = GitMailMap.create(
            listOf(
                "Joe R. Developer <joe@example.com>",
                "Jane Doe <jane@example.com> <jane@laptop.(none)>",
                "Jane Doe <jane@example.com> <jane@desktop.(none)>",
                "Joe R. Developer <joe@example.com> Joe <bugs@example.com>",
                "Jane Doe <jane@example.com> Jane <bugs@example.com>"
            )
        )
        mailMap.mapIdentity("jane@laptop.(none)", "Commit Name") should {
            it.email shouldBe "jane@example.com"
            it.name shouldBe "Jane Doe"
        }
        mailMap.mapIdentity("jane@desktop.(none)", "Commit Name") should {
            it.email shouldBe "jane@example.com"
            it.name shouldBe "Jane Doe"
        }
        mailMap.mapIdentity("bugs@example.com", "Jane") should {
            it.email shouldBe "jane@example.com"
            it.name shouldBe "Jane Doe"
        }
        mailMap.mapIdentity("bugs@example.com", "Joe") should {
            it.email shouldBe "joe@example.com"
            it.name shouldBe "Joe R. Developer"
        }
        mailMap.mapIdentity("bugs@example.com", "Jane") should {
            it.email shouldBe "jane@example.com"
            it.name shouldBe "Jane Doe"
        }
        mailMap.mapIdentity("bugs@example.com", "Somebody") should {
            it.email shouldBe "bugs@example.com"
            it.name shouldBe "Somebody"
        }
    }
}