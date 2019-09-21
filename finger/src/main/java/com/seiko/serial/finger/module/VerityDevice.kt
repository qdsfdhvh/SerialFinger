package com.seiko.serial.finger.module

import android.os.Handler
import android.os.Looper
import com.seiko.serial.finger.FingerBean
import com.seiko.serial.finger.FingerContracts
import com.seiko.serial.finger.FingerContracts.CMD_GENERATE
import com.seiko.serial.finger.FingerContracts.CMD_GET_IMAGE
import com.seiko.serial.finger.FingerContracts.CMD_SEARCH
import java.util.concurrent.atomic.AtomicInteger

class VerityDevice(private val callback: Callback): BaseFingerModule() {

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

//    override fun onReceive(bean: FingerBean) {
//        super.onReceive(bean)
//        when(bean.cmd) {
////            CMD_GET_IMAGE -> {
////                when(bean.code) {
////                    ERR_SUCCESS -> {
////                        FingerContracts.generate(0).post()
////                    }
////                    else -> {
////                        FingerContracts.bind(CMD_GET_IMAGE).post()
////                    }
////                }
////            }
////            CMD_GENERATE -> {
////                when(bean.code) {
////                    ERR_SUCCESS -> {
////                        FingerContracts.search().post()
////                    }
////                    else -> {
////                        triggers.onNext(Behavior.BAD_QUALITY to bean.getMsg())
////                        FingerContracts.bind(CMD_GET_IMAGE).post()
////                    }
////                }
////            }
////            CMD_SEARCH -> {
////                when(bean.code) {
////                    ERR_SUCCESS -> {
//////                        triggers.onNext(type.VERITY_SUCCESS to "验证成功，正在提交后台...")
////                        triggers.onNext(Behavior.VERITY_SUCCESS to La.name_finger_toast_search_success)
////                    }
////                    else -> {
////                        val count = verifyCount.getAndAdd(1)
////                        if (count < MAX_VERITY_COUNT) {
//////                            triggers.onNext(type.TRY_AGAIN to "验证失败，再试一次：$count/$MAX_VERITY_COUNT")
////                            triggers.onNext(Behavior.TRY_AGAIN to La.name_finger_toast_search_again_format.format(count, MAX_VERITY_COUNT))
////                            Thread.sleep(1000)
////                            FingerContracts.bind(CMD_GET_IMAGE).post()
////                        } else {
//////                            triggers.onNext(type.VERITY_FAILED to "本次点名失败。")
////                            triggers.onNext(Behavior.VERITY_FAILED to La.name_finger_toast_search_failed)
////                        }
////                    }
////                }
////            }
//        }
//    }

    enum class Behavior {
        BAD_QUALITY,    // 图像质量不好
//        TRY_AGAIN,      // 重新验证
        VERITY_SUCCESS, // 验证成功
        VERITY_FAILED   // 验证失败
    }
    interface Callback {
        fun onBehavior(type: Behavior, errCode: Int)
        fun onTryAgain(count: Int, max: Int)
    }

}