package com.seiko.serial.finger.module

import android.os.Handler
import android.os.Looper
import com.seiko.serial.finger.FingerBean
import com.seiko.serial.finger.FingerContracts
import com.seiko.serial.finger.FingerContracts.CMD_GENERATE
import com.seiko.serial.finger.FingerContracts.CMD_GET_IMAGE
import com.seiko.serial.finger.FingerContracts.CMD_SEARCH
import java.util.concurrent.atomic.AtomicInteger

class VerityModule(private val callback: Callback): BaseFingerModule() {

    companion object {

        /**
         * 最大验证次数
         */
        private const val MAX_VERITY_COUNT = 3
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override val cmdArray = hashMapOf(
        CMD_GET_IMAGE to GetImageCmd(),
        CMD_GENERATE  to GeneRateCmd(),
        CMD_SEARCH    to SearchCmd()
    )

    private val verifyCount = AtomicInteger(1)

    fun start() {
        verifyCount.set(1)
        FingerContracts.bind(CMD_GET_IMAGE).post()
    }

    /*************************************************************
     *
     *                       验证指纹
     *        CMD_GET_IMAGE     // 验证 - 获取指纹
     *        ->
     *        CMD_GENERATE      // 存入RamBuffer0中
     *        ->
     *        CMD_SEARCH        // 与硬件中的所有模板进行比对
     *
     *************************************************************/

    // 获取指纹图像
    private inner class GetImageCmd: FingerCmd {
        override fun onSuccess(bean: FingerBean): Boolean {
            // 存入RamBuffer0
            FingerContracts.generate(0).post()
            return true
        }

        override fun onFailed(code: Int) {
            // 持续读取
            FingerContracts.bind(CMD_GET_IMAGE).post()
        }
    }

    private inner class GeneRateCmd: FingerCmd {
        override fun onSuccess(bean: FingerBean): Boolean {
            // 开始验证指纹
            FingerContracts.search().post()
            return true
        }

        override fun onFailed(code: Int) {
            // 指纹质量不好
            handler.post {
                callback.onBehavior(Behavior.BAD_QUALITY, 0)
            }
            FingerContracts.bind(CMD_GET_IMAGE).post()
        }
    }

    private inner class SearchCmd: FingerCmd {
        override fun onSuccess(bean: FingerBean): Boolean {
            // 验证成功
            handler.post {
                callback.onBehavior(Behavior.VERITY_SUCCESS, 0)
            }
            return true
        }

        override fun onFailed(code: Int) {
            // 验证失败
            val count = verifyCount.getAndAdd(1)
            if (count < MAX_VERITY_COUNT) {
                handler.post {
                    callback.onTryAgain(count, MAX_VERITY_COUNT)
                }
                FingerContracts.bind(CMD_GET_IMAGE).post()
            } else {
                handler.post {
                    callback.onBehavior(Behavior.VERITY_FAILED, 0)
                }
            }
        }
    }

    enum class Behavior {
        BAD_QUALITY,    // 图像质量不好
//        TRY_AGAIN,      // 重新验证
        VERITY_SUCCESS, // 验证成功
        VERITY_FAILED   // 验证失败
    }
    interface Callback {
        fun onBehavior(type: Behavior, errCode: Int)
        fun onTryAgain(index: Int, max: Int)
    }

}