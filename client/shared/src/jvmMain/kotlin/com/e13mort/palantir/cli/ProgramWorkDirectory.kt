package com.e13mort.palantir.cli

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ProgramWorkDirectory {
    companion object {
        private const val WORK_DIRECTORY_NAME = ".plntr"
    }

    fun directory(): Path {
        val homeDirectoryStr =
            System.getProperty("user.home")
                ?: throw IllegalStateException("Can't find home directory user.home")
        val homeDirectoryPath = Paths.get(homeDirectoryStr)
        if (!Files.exists(homeDirectoryPath)) throw IllegalStateException("Home directory doesn't exists")
        val workDirectory = homeDirectoryPath.resolve(WORK_DIRECTORY_NAME)
        if (!Files.exists(workDirectory)) {
            Files.createDirectory(workDirectory)
        }
        return workDirectory
    }
}