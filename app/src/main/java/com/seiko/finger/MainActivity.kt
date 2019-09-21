package com.seiko.finger

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.seiko.serial.finger.FingerContracts
import com.seiko.serial.finger.FingerDecode
import com.seiko.serial.finger.module.RegisterModule
import com.seiko.serial.finger.SumFilter
import com.seiko.serial.rs232.RS232SerialPort
import com.seiko.serial.rs232.SerialPortPath
import com.seiko.serial.target.SerialTarget
import com.seiko.serial.target.toTarget
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private var target: SerialTarget? = null


    private val register by lazy(LazyThreadSafetyMode.NONE) {
        RegisterModule(object :
            RegisterModule.Callback {
            override fun onBehavior(type: RegisterModule.Behavior, errCode: Int) {
                when (type) {
                    RegisterModule.Behavior.NOT_GENERATE -> {
                        stopFinger()
                        textView.text = FingerContracts.getErrMsg(errCode)
                    }
                    RegisterModule.Behavior.START_MERGE -> {
                        textView.text = "开始合并模板..."
                    }
                    RegisterModule.Behavior.NOT_MERGE -> {
                        stopFinger()
                        textView.text = FingerContracts.getErrMsg(errCode)
                    }
                    RegisterModule.Behavior.START_MATCH -> {
                        textView.text = "请再次按下，进行测试..."
                    }
                    RegisterModule.Behavior.NOT_MATCH -> {
                        stopFinger()
                        textView.text = FingerContracts.getErrMsg(errCode)
                    }
                    RegisterModule.Behavior.START_UP_CHAR -> {
                        textView.text = "测试通过，正在获取模板数据..."
                    }
                    RegisterModule.Behavior.NOT_UP_CHAR -> {
                        stopFinger()
                        textView.text = FingerContracts.getErrMsg(errCode)
                    }
                }
            }

            override fun onMerge(index: Int, max: Int) {
                textView.text = "再按一次：$index/$max..."
            }

            override fun onReceive(bytes: ByteArray) {
                stopFinger()
                val msg = "获得一组有效指纹字节：${bytes.size}。"
                textView.text = msg
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        openTarget()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeTarget()
    }

    private fun initViews() {
        button.setOnClickListener {
            if (button.text == getString(R.string.btn_start)) {
                startFinger()
            } else {
                stopFinger()
            }
        }
        stopFinger()
    }

    private fun startFinger() {
        textView.text = "请按下指纹..."
        button.text = getString(R.string.btn_stop)
        register.start()
    }

    private fun stopFinger() {
        textView.text = ""
        button.text = getString(R.string.btn_start)
        register.stop()
    }

    private fun openTarget() {
        SerialTarget.IS_DEBUG = true
        val serial = RS232SerialPort(SerialPortPath.ttyS2, 115200)
        val target = serial.toTarget()
        target.iDecode = FingerDecode()
        target.iFilter = SumFilter()
        target.start()
        target.addSerialModule(register)
    }

    private fun closeTarget() {
        target?.close()
    }
}
