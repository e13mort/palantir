package com.e13mort.palantir.cli

import com.e13mort.palantir.utils.Console
import java.io.Console as SystemConsole

internal class ConsoleImpl(console: SystemConsole) : Console {
    private var lastMessageStyle = Console.WriteStyle.ADD
    private var lastMessageLength = 0
    private val writer = console.writer()

    override fun write(message: String, writeStyle: Console.WriteStyle) {
        if (writeStyle == Console.WriteStyle.REPLACE_LAST) {
            clearConsole()
            writer.write(message)
        } else {
            if (lastMessageStyle == Console.WriteStyle.REPLACE_LAST) {
                clearConsole()
            }
            writer.println(message)
        }
        writer.flush()
        lastMessageStyle = writeStyle
        lastMessageLength = lastMessageLength.coerceAtLeast(message.length)
    }

    private fun clearConsole() {
        moveCursorToStart()
        removePreviousMessage()
        moveCursorToStart()
    }

    private fun removePreviousMessage() {
        if (lastMessageLength > 0) {
            writer.format("%1$${lastMessageLength}s", " ")
        }
    }

    private fun moveCursorToStart() {
        writer.write("\r")
    }

}