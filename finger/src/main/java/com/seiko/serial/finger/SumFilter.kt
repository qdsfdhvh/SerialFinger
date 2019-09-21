package com.seiko.serial.finger

import com.seiko.serial.target.filter.IFilter


class SumFilter: IFilter {

    /**
     * 校验数据是否有效
     */
    override fun isSafe(bytes: ByteArray): Boolean {
        if (bytes.isEmpty()) return false


        val len = bytes.size
        val num1 = bytes.copyOfRange(0, len - 2).toGetSum()
        val num2 = bytes.copyOfRange(len - 2, len)
        return num1.contentEquals(num2)
    }

}