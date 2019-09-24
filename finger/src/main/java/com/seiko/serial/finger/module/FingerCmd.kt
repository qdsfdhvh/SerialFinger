package com.seiko.serial.finger.module

import com.seiko.serial.finger.FingerBean

interface FingerCmd {

    fun onSuccess(bean: FingerBean): Boolean

    fun onFailed(code: Int)

}