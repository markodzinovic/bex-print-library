package com.bexprint.print

import java.io.ByteArrayOutputStream

object EscPosFormatter {
    fun formatMultiLine(payload: String, lineWidth: Int): ByteArray {
        val out = ByteArrayOutputStream()
        payload.lines().forEachIndexed { i, line ->
            try {
                val formatted = formatColumns(line, lineWidth)
                out.write(EscPosTagParser.parse(formatted))
                out.write(byteArrayOf(0x0A))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return out.toByteArray()
    }

    fun formatColumns(line: String, lineWidth: Int): String {
        val regex = Regex("(\\[L]|\\[C]|\\[R])([^\\[\\]]*)")
        val matches = regex.findAll(line).toList()
        if (matches.isEmpty()) return line

        val parts = matches.map { it.groupValues[1] to it.groupValues[2].trim() }
        val baseWidth = lineWidth / parts.size
        val extra = lineWidth % parts.size

        return buildString {
            parts.forEachIndexed { idx, (align, text) ->
                val width = baseWidth + if (idx < extra) 1 else 0
                append(
                    when (align) {
                        "[L]" -> text.padEnd(width)
                        "[C]" -> text.center(width)
                        "[R]" -> text.padStart(width)
                        else -> text
                    }
                )
            }
        }
    }

    private fun String.center(width: Int): String {
        val padding = (width - this.length).coerceAtLeast(0)
        val left = padding / 2
        val right = padding - left
        return " ".repeat(left) + this + " ".repeat(right)
    }
}
