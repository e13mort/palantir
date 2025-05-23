/*
 * Copyright: (c)  2023-2025, Pavel Novikov <mail@pavel.dev>
 * GNU General Public License v3.0+ (see COPYING or https://www.gnu.org/licenses/gpl-3.0.txt)
 */

package com.e13mort.palantir

import java.io.FileReader

fun Any.readTestFile(testFileName: String): String {
    val file = javaClass.classLoader.getResource(testFileName)!!.file
    return FileReader(file).readText()
}