package com.bexprint.print

object EscPosCommands {
    val alignLeft = byteArrayOf(0x1B, 0x61, 0x00)
    val alignCenter = byteArrayOf(0x1B, 0x61, 0x01)
    val alignRight = byteArrayOf(0x1B, 0x61, 0x02)

    val boldOn = byteArrayOf(0x1B, 0x45, 0x01)
    val boldOff = byteArrayOf(0x1B, 0x45, 0x00)
    val underlineOn = byteArrayOf(0x1B, 0x2D, 0x01)
    val underlineOff = byteArrayOf(0x1B, 0x2D, 0x00)

    val reset = byteArrayOf(0x1B, 0x40)
    val selectFontA = byteArrayOf(0x1B, 0x4D, 0x00)

    fun setFontSize(size: String): ByteArray {
        val sizeMap = mapOf(
            "normal" to 0x00.toByte(),
            "big-1" to 0x11.toByte(),
            "big-2" to 0x22.toByte(),
            "big-3" to 0x33.toByte()
        )
        return byteArrayOf(0x1D, 0x21, sizeMap[size] ?: 0x00)
    }
}
