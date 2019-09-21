package com.seiko.serial.finger.module

import android.os.Handler
import android.os.Looper
import com.seiko.serial.finger.FingerBean
import com.seiko.serial.finger.FingerContracts
import com.seiko.serial.finger.FingerContracts.CMD_GENERATE
import com.seiko.serial.finger.FingerContracts.CMD_GET_IMAGE
import com.seiko.serial.finger.FingerContracts.CMD_MATCH
import com.seiko.serial.finger.FingerContracts.CMD_MERGE
import com.seiko.serial.finger.FingerContracts.CMD_UP_CHAR
import com.seiko.serial.finger.FingerContracts.ERR_BAD_QUALITY
import com.seiko.serial.finger.FingerContracts.ERR_SUCCESS
import com.seiko.serial.finger.FingerContracts.RCM_DATA_PREFIX_CODE
import com.seiko.serial.finger.FingerContracts.RCM_PREFIX_CODE
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class RegisterModule(private val callback: Callback): BaseFingerModule() {

    companion object {

        /**
         * 待合并的指纹图像数 2或3
         */
        private const val MAX_RAM_COUNT = 3
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override val cmdArray = hashMapOf(
        CMD_GET_IMAGE to GetImageCmd(),
        CMD_GENERATE  to GeneRateCmd(),
        CMD_MERGE     to MergeCmd(),
        CMD_MATCH     to MatchCmd(),
        CMD_UP_CHAR   to UpCharCmd()
    )

    override fun accept(bytes: ByteArray): Boolean {
        // 请求关闭时，停止任何解析
        if (!isGetting.get()) return true
        return super.accept(bytes)
    }

    /**
     * 是否正在请求
     */
    private val isGetting = AtomicBoolean(false)

    /**
     * 当前为注册/校验
     */
    private val state = AtomicReference<State>(State.REGISTER)

    /**
     * 存放到第X个RamCount
     */
    private val ramIndex = AtomicInteger(0)

    fun start() {
        if (isGetting.get()) return

        isGetting.set(true)
        state.set(State.REGISTER)
        ramIndex.set(0)

        FingerContracts.bind(CMD_GET_IMAGE).post()
    }

    fun stop() {
        if (!isGetting.get()) return

        isGetting.set(false)
    }

    /*************************************************************
     *                     生成指纹模板并提交后台
     *        CMD_GET_IMAGE     // 获取指纹图像
     *        ->
     *        CMD_GENERATE      // 存到RamBuffer 重复3次
     *        ->
     *        CMD_MERGE         // 合并指纹，默认存于RamBuffer0
     *        ->
     *        CMD_GET_IMAGE     // 校验 - 获取指纹图像
     *        ->
     *        CMD_GENERATE      // 校验 - 存到RamBuffer1
     *        ->
     *        CMD_MATCH         // 测试
     *        ->
     *        CMD_UP_CHAR       // 从硬件设备中获取指纹模板数据
     *        ->
     *        直接上传给服务器
     *
     *************************************************************/

    // 获取指纹图像
    private inner class GetImageCmd: FingerCmd {
        override fun onSuccess(bean: FingerBean): Boolean {
            when(state.get()!!) {
                State.REGISTER -> {
                    // 将指纹存放在第x个RamBuffer中
                    FingerContracts.generate(ramIndex.get()).post()
                }
                State.MATCH -> {
                    // 将待验证的指纹存入模板1中 与模板0的合并模板作比较
                    FingerContracts.generate(1).post()
                }
            }
            return true
        }

        override fun onFailed(code: Int) {
            // 持续读取
            FingerContracts.bind(CMD_GET_IMAGE).post()
        }
    }

    // 存到RamBuffer 重复3次
    private inner class GeneRateCmd: FingerCmd {
        override fun onSuccess(bean: FingerBean): Boolean {
            when(state.get()!!) {
                State.REGISTER -> {
                    // 获取当前RamIndex，低于合并的模板数(MAX_RAM_COUNT)时继续读取指纹，否则开始合并指纹。
                    val index = ramIndex.addAndGet(1)
                    if (index < MAX_RAM_COUNT) {
                        handler.post { callback.onMerge(index, MAX_RAM_COUNT) }
                        FingerContracts.bind(CMD_GET_IMAGE).post()
                    } else {
                        handler.post { callback.onBehavior(Behavior.START_MERGE, 0) }
                        FingerContracts.merge(MAX_RAM_COUNT).post()
                    }
                }
                State.MATCH -> {
                    // 用于测试的指纹成功存入RamBuffer1中，开始与RamBuffer0的指纹模板进行测试比较。
                    FingerContracts.match().post()
                }
            }
            return true
        }

        override fun onFailed(code: Int) {
            when(code) {
                ERR_BAD_QUALITY -> {
                    // 有时候指纹图像质量不好，重新开始获取图像
                    FingerContracts.bind(CMD_GET_IMAGE).post()
                }
                else -> {
                    // 存储失败
                    FingerContracts.bind(CMD_GET_IMAGE).post()
                    handler.post { callback.onBehavior(Behavior.NOT_GENERATE, code) }
                }
            }
        }
    }

    // 合并指纹，默认存于RamBuffer0
    private inner class MergeCmd: FingerCmd {
        override fun onSuccess(bean: FingerBean): Boolean {
            handler.post { callback.onBehavior(Behavior.START_MATCH, 0) }
            // 指纹模板合并成功，再次读取一次指纹进行验证
            state.set(State.MATCH)
            FingerContracts.bind(CMD_GET_IMAGE).post()
            return true
        }

        override fun onFailed(code: Int) {
            //  指纹模板合成失败
            handler.post { callback.onBehavior(Behavior.NOT_MERGE, code) }
        }
    }

    // 测试比较刚合并的指纹
    private inner class MatchCmd: FingerCmd {
        override fun onSuccess(bean: FingerBean): Boolean {
            // 比较成功，获取指纹模板数据，用于传给后台
            handler.post { callback.onBehavior(Behavior.START_UP_CHAR, 0) }
            FingerContracts.upChar().post()
            return true
        }

        override fun onFailed(code: Int) {
            // 当前合成的模板指纹比较不通过
            handler.post { callback.onBehavior(Behavior.NOT_MATCH, code) }
        }
    }

    // 获取指纹模板数据
    private inner class UpCharCmd: FingerCmd {
        override fun onSuccess(bean: FingerBean): Boolean {
            when(bean.prefix) {
                RCM_PREFIX_CODE -> {
                    // 接收一个指纹模板反馈包，确认数据长度，不需要处理，返回false继续接收。
//                    val len = bean.data.toModBus2Int()
                    return false
                }
                RCM_DATA_PREFIX_CODE -> {
                    // 获得指纹模板数据
                    handler.post { callback.onReceive(bean.data) }
                }
            }
            return true
        }

        override fun onFailed(code: Int) {
            // 获取指纹数据失败
            handler.post { callback.onBehavior(Behavior.NOT_UP_CHAR, code) }
        }
    }

    private enum class State {
        REGISTER,  // 注册
        MATCH      // 校验
    }

    enum class Behavior {
        NOT_GENERATE,   // 指纹存放RamBuffer时失败
        START_MERGE,    // 开始合并指纹模板
        NOT_MERGE,      // 指纹模板合并失败
        START_MATCH,    // 开始测试刚合成的模板
        NOT_MATCH,      // 测试指纹失败
        START_UP_CHAR,  // 开始提取模板
        NOT_UP_CHAR     // 提取模板失败
    }

    interface Callback {
        fun onBehavior(type: Behavior, errCode: Int)
        fun onMerge(index: Int, max: Int)
        fun onReceive(bytes: ByteArray)
    }

}