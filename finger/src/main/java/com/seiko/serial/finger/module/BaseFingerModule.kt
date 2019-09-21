package com.seiko.serial.finger.module

import com.seiko.serial.finger.FingerBean
import com.seiko.serial.finger.FingerContracts.ERR_SUCCESS
import com.seiko.serial.target.SerialModule

abstract class BaseFingerModule: SerialModule {

    /**
     * 串口
     */
    private var target: SerialModule.Target? = null

    override fun attach(target: SerialModule.Target?) {
        this.target = target
    }

    /**
     * 发送字节
     */
    protected fun ByteArray.post() {
        target?.send(this@BaseFingerModule, this)
    }

    /**
     * 优先级
     */
    override fun getPriority(): Int = 99

    /**
     * 标记
     */
    override fun getTag(): String = this.javaClass.simpleName


    internal abstract val cmdArray: HashMap<Int, FingerCmd>

    /**
     * 接收字节
     */
    override fun accept(bytes: ByteArray): Boolean {
        val bean = FingerBean(bytes)
        val cmd = cmdArray[bean.cmd] ?: return true
        return if (bean.code == ERR_SUCCESS) {
            cmd.onSuccess(bean)
        } else {
            cmd.onFailed(bean.code)
            true
        }
    }

}