/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.git

class GitMailMap(
    private val nameMapping: Map<String, String>,
    private val emailMapping: Map<String, String>,
    private val fullCommitUserMapping: Map<String, Map<String, MailMap.FullMappedIdentity>>
) : MailMap {

    companion object {
        private val nameEmail = "^(.*) <(.*)>\$".toRegex()
        private val complex = "^(.*) ?<(.*)> ?(.*) ?<(.*)>\$".toRegex()

        fun create(data: List<String>): GitMailMap {
            val emailMap = mutableMapOf<String, String>()
            val nameMap = mutableMapOf<String, String>()
            val fullCommitUserMapping = mutableMapOf<String, MutableMap<String, MailMap.FullMappedIdentity>>()
            data.forEach { line ->
                complex.find(line)?.let { matchResult ->
                    val (properName, properEmail, commitName, commitEmail) = matchResult.destructured
                    if (properEmail.isBlank() || commitEmail.isBlank()) throw IllegalArgumentException()

                    if (commitName.isNotBlank()) {
                        val mapForEmail = fullCommitUserMapping.getOrPut(commitEmail.lowercase()) {
                            mutableMapOf()
                        }
                        mapForEmail[commitName.trim().lowercase()] =
                            MailMap.FullMappedIdentity(properName.trim(), properEmail)
                    } else {
                        emailMap[commitEmail.lowercase()] = properEmail
                        if (properName.isNotBlank()) {
                            nameMap[commitEmail.lowercase()] = properName.trim()
                        }
                    }

                    return@forEach
                }
                // nameEmail regex is more general, so it should be checked after the complex regex
                nameEmail.find(line)?.let { matchResult ->
                    val (properName, commitEmail) = matchResult.destructured
                    nameMap[commitEmail.lowercase()] = properName
                    return@forEach
                }
            }
            return GitMailMap(nameMap, emailMap, fullCommitUserMapping)
        }
    }



    override fun mapIdentity(commitEmail: String, commitName: String): MailMap.FullMappedIdentity {
        // read complex example from here https://datawookie.dev/blog/2023/05/using-mailmap-to-tidy-git-contributors/
        val mixed = fullCommitUserMapping[commitEmail.lowercase()]
        if (mixed != null) {
            val mappedIdentity = mixed[commitName.lowercase()]
            if (mappedIdentity != null) {
                return MailMap.FullMappedIdentity(mappedIdentity.name, mappedIdentity.email)
            }
        } else {
            return MailMap.FullMappedIdentity(
                nameMapping[commitEmail.lowercase()] ?: commitName,
                emailMapping[commitEmail.lowercase()] ?: commitEmail
            )
        }
        return MailMap.FullMappedIdentity(commitName, commitEmail)
    }
}