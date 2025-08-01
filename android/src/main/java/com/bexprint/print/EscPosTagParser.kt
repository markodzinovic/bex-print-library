package com.bexprint.print

import android.annotation.SuppressLint
import java.io.ByteArrayOutputStream

object EscPosTagParser {
    fun parse(input: String): ByteArray {
        val output = ByteArrayOutputStream()
        var remaining = input

        val tagRegex = Regex("""<(/?)(\w+)([^>]*)>""")

        while (remaining.isNotEmpty()) {
            val match = tagRegex.find(remaining) ?: break
            val before = remaining.substring(0, match.range.first)
            output.write(before.toByteArray(Charsets.ISO_8859_1))

            val tag = match.groupValues[2].lowercase()
            val isClosing = match.groupValues[1] == "/"
            val attrs = match.groupValues[3]
            remaining = remaining.substring(match.range.last + 1)

            if (isClosing) return output.toByteArray()

            when (tag) {
                "b" -> {
                    output.write(EscPosCommands.boldOn)
                    val inner = parse(remaining.substringBefore("</b>"))
                    output.write(inner)
                    output.write(EscPosCommands.boldOff)
                    remaining = remaining.substringAfter("</b>")
                }

                "u" -> {
                    output.write(EscPosCommands.underlineOn)
                    val inner = parse(remaining.substringBefore("</u>"))
                    output.write(inner)
                    output.write(EscPosCommands.underlineOff)
                    remaining = remaining.substringAfter("</u>")
                }

                "font" -> {
                    val size = Regex("""size=["']big-(\d+)["']""").find(attrs)?.groupValues?.get(1)
                    val fontSize = EscPosCommands.setFontSize("big-$size")
                    output.write(fontSize)
                    val inner = parse(remaining.substringBefore("</font>"))
                    output.write(inner)
                    output.write(EscPosCommands.setFontSize("normal"))
                    remaining = remaining.substringAfter("</font>")
                }

                "barcode" -> {
                    val end = remaining.indexOf("</barcode>")
                    if (end >= 0) {
                        val content = remaining.substring(0, end)
                        val data = content.substringBefore("<").trim()
                        val height = Regex("""height=["'](\d+)["']""").find(attrs)?.groupValues?.get(1)?.toIntOrNull() ?: 100
                        val width = Regex("""width=["'](\d+)["']""").find(attrs)?.groupValues?.get(1)?.toIntOrNull() ?: 2
                        val showText = !attrs.contains("text='none'")

                        output.write(EscPosCommands.alignCenter)
                        output.write(BarcodeGenerator.generateCode128(data, height, width, showText))
                        output.write(EscPosCommands.alignLeft)
                        output.write(EscPosCommands.reset)

                        remaining = remaining.substring(end + "</barcode>".length)
                    }
                }

                else -> {
                    output.write(match.value.toByteArray(Charsets.ISO_8859_1))
                }
            }
        }

        output.write(remaining.toByteArray(Charsets.ISO_8859_1))
        return output.toByteArray()
    }


    // Generiši barcode 128 (kopirano iz tvog primera)
    @SuppressLint("SuspiciousIndentation")
    private fun generateBarcode128(
        data: String,
        height: Int = 110,
        width: Int = 3,
        printText: Boolean = false
    ): ByteArray {
        val output = ByteArrayOutputStream()

        // Postavi širinu barkoda
        output.write(byteArrayOf(0x1D, 0x77, width.toByte()))

        // Postavi visinu barkoda
        output.write(byteArrayOf(0x1D, 0x68, height.toByte()))

        // Postavi da li štampati tekst ispod barkoda (0x02 = ispiši, 0x00 = ne)
        output.write(byteArrayOf(0x1D, 0x48, if (printText) 0x02 else 0x00))

        // Pošalji komandu za štampu barkoda Code 128 (GS k 73)
        output.write(byteArrayOf(0x1D, 0x6B, 0x49))

        // dužina podataka (broj bajtova)
        output.write(data.length)

        // podaci barkoda (pretvaranje u ISO_8859_1 bajtove)
        output.write(data.toByteArray(Charsets.ISO_8859_1))

        // novi red nakon barkoda
        output.write(byteArrayOf(0x0A))

        return output.toByteArray()
    }
}
