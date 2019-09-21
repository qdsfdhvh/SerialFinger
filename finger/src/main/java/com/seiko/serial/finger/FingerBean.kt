package com.seiko.serial.finger

import com.seiko.serial.modbus.toHexString


open class FingerBean(private val bytes: ByteArray) {

    val prefix by lazy {
        bytes.copyOfRange(0, 2).toModBus2Int()
    }

    val cmd by lazy {
        bytes.copyOfRange(4, 6).toModBus2Int()
    }

    private val len by lazy {
        bytes.copyOfRange(6, 8).toModBus2Int()
    }

    val code by lazy {
        bytes.copyOfRange(8, 10).toModBus2Int()
    }

    val data by lazy {
        if (len <= 2) {
            ByteArray(0)
        } else {
            bytes.copyOfRange(8 + 2, 8 + len)
        }
    }

    fun getMsg() = FingerContracts.getErrMsg(code)

    override fun toString(): String {
        return "FingerBean Info: \n" +
                "Packet -> ${FingerContracts.getPrefMsg(prefix)} \n" +
                "Cmd    -> ${FingerContracts.getCmdMsg(cmd)} \n" +
                "Code   -> ${FingerContracts.getErrMsg(code)} \n" +
                "Data   -> ${data.toHexString()} \n" +
                "Hex    -> ${bytes.toHexString()}"
    }

}