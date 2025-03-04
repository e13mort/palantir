/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir.git

import java.nio.file.Files
import java.nio.file.Path

class GitMailMapFactory : MailMapFactory {
    companion object {
        const val MAP_FILE_NAME = ".mailmap"
        const val COMMENT_START = "#"
    }

    override fun createMailMap(gitRepo: Path): MailMap {
        val mailMapFilePath = gitRepo.resolve(MAP_FILE_NAME)
        if (Files.exists(mailMapFilePath)) {
            val lines = Files.readAllLines(mailMapFilePath)
            val mapItems = lines.filter {
                it.isNotBlank() && !it.startsWith(COMMENT_START)
            }
            return GitMailMap.create(mapItems)
        }
        return MailMap
    }
}