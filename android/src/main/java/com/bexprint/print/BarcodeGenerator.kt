package com.bexprint.print

import java.io.ByteArrayOutputStream

object BarcodeGenerator {

    /**
     * Generiše ESC/POS komande za štampanje Code128 barkoda.
     *
     * @param data Barkod podatak (tekst)
     * @param height Visina barkoda u tačkama (default: 110)
     * @param width Širina linije (1–6, default: 3)
     * @param printText Da li prikazati tekst ispod barkoda (true/false)
     */
    fun generateCode128(
        data: String,
        height: Int = 110,
        width: Int = 3,
        printText: Boolean = false
    ): ByteArray {
        val output = ByteArrayOutputStream()

        // Širina linije
        output.write(byteArrayOf(0x1D, 0x77, width.toByte()))

        // Visina barkoda
        output.write(byteArrayOf(0x1D, 0x68, height.toByte()))

        // Prikaz teksta ispod barkoda
        output.write(byteArrayOf(0x1D, 0x48, if (printText) 0x02 else 0x00))

        // Komanda za Code128 barkod
        output.write(byteArrayOf(0x1D, 0x6B, 0x49))
        output.write(data.length) // broj bajtova
        output.write(data.toByteArray(Charsets.ISO_8859_1))

        // Novi red
        output.write(byteArrayOf(0x0A))

        return output.toByteArray()
    }
}
