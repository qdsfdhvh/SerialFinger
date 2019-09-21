package com.seiko.serial.finger

/**
 * 字节转Int
 * 指纹厂商的Int换算是 低位前 高位后。
 */
internal fun ByteArray.toModBus2Int(): Int {
    return when(size) {
        1 -> this[0].toInt()
        2 -> (this[1].toInt() and 0xFF shl 8) or
                (this[0].toInt() and 0xFF)
        4 -> (this[3].toInt() and 0xFF shl 24) or
                (this[2].toInt() and 0xFF shl 16) or
                (this[1].toInt() and 0xFF shl 8) or
                (this[0].toInt() and 0xFF)
        else -> 0
    }
}

/**
 * Int转字节
 */
internal fun Int.toModBus2Bytes(size: Int = 2): ByteArray {
    var bytes = ByteArray(size)
    when(size) {
        1 -> bytes[0] = this.toByte()
        2 -> {
            bytes[1] = (this shr 8 and 0xFF).toByte()
            bytes[0] = (this and 0xFF).toByte()
        }
        4 -> {
            bytes[3] = (this shr 24 and 0xFF).toByte()
            bytes[2] = (this shr 16 and 0xFF).toByte()
            bytes[1] = (this shr 8 and 0xFF).toByte()
            bytes[0] = (this and 0xFF).toByte()
        }
        else -> bytes = byteArrayOf(0)
    }
    return bytes
}

/**
 * 给字节添加校验
 */
internal fun ByteArray.toAddSum(): ByteArray {
    return this + toGetSum()
}

/**
 * 校验位：总和
 */
internal fun ByteArray.toGetSum(): ByteArray {
    var num1 = 0
    forEach { num1 += it.toInt() and 0xFF }
    return num1.toModBus2Bytes(2)
}