package com.seiko.serial.finger

import com.seiko.serial.finger.FingerContracts.RCM_DATA_PREFIX_CODE
import com.seiko.serial.finger.FingerContracts.RCM_PREFIX_CODE
import com.seiko.serial.modbus.indexOfArray
import com.seiko.serial.target.decode.IDecode
import okio.Buffer

class FingerDecode: IDecode {

    /** 残余缓冲区 **/
//    private var queueRemain: Queue<Byte> = LinkedList()
    private var queueRemain = Buffer()

    /** 包头 **/
    private val head1 = RCM_PREFIX_CODE.toModBus2Bytes(2)
    private val head2 = RCM_DATA_PREFIX_CODE.toModBus2Bytes(2)

//    override fun multiCheck(bytes: ByteArray): Observable<ByteArray> {
//        return ObservableCreate<ByteArray> { emitter ->
//            var bak = check(bytes)
//            while (!bak.contentEquals(EMPTY_BYTES)) {
//                emitter.onNext(bak)
//                bak = check(EMPTY_BYTES)
//            }
//            emitter.onComplete()
//        }
//    }

    override fun check(bytes: ByteArray): ByteArray {
//        var buffer = bytes
//        if (queueRemain.isNotEmpty()) {
//            if (queueRemain.size <= MAX_BUFFER_SIZE) {
//                synchronized(this) {
//                    buffer = queueRemain.toByteArray() + buffer
//                }
//            }
//            queueRemain.clear()
//        }
        val size = queueRemain.write(bytes).size
        val newBytes = if (size > 0) {
            val bak = queueRemain.readByteArray()
            queueRemain.clear()
            bak
        } else {
            EMPTY_BYTES
        }
        return decodeBytes(newBytes)
    }

    /**
     * 开始对缓存池进行处理
     */
    private fun decodeBytes(bytes: ByteArray): ByteArray {
        // 字节过少，存入缓存池
        if (bytes.size < 10) {
            queueRemain.write(bytes)
            return EMPTY_BYTES
        }

        // 寻找包头
        val index1 = bytes.indexOfArray(head1)
        val index2 = bytes.indexOfArray(head2)

        return when {
            // 如果两个包头都有，取最前面的
            index1 >= 0 && index2 >= 0 -> {
                if (index1 < index2) {
                    isSafeBytes(false, index1, bytes)
                } else {
                    isSafeBytes(true, index2, bytes)
                }
            }
            // 找到CMD包头
            index1 >= 0 -> isSafeBytes(false, index1, bytes)
            // 找到RCM包头
            index2 >= 0 -> isSafeBytes(true, index2, bytes)
            //没有结果，字节不做保留
            else -> EMPTY_BYTES
        }
    }

    private fun isSafeBytes(isData: Boolean, index: Int, bytes: ByteArray): ByteArray {
//        Timber.d("找到包头位置index = $index")

        // 数据没有包含长度数据，存入缓存池
        if (bytes.size < index + 8) {
//            Timber.d("长度字节${index + 8} > ${bytes.size}, 存入缓存池等下次操作。")
            queueRemain.write(bytes)
            return EMPTY_BYTES
        }

        // 有效数据长度
        var size = bytes.copyOfRange(index + 6, index + 8).toModBus2Int()
        if (size < 16 && !isData) size = 16
//        Timber.d("有效数据长度 size = $size")

        // 一帧数据长度 包含包头、校验
        val len = index + 8 + size + 2
//        Timber.d("一帧长度 len = $len")

        // 数据不够长，存入缓存池
        if (bytes.size < len) {
//            Timber.d("长度$len > ${bytes.size}, 存入缓存池等下次操作。")
            queueRemain.write(bytes)
            return EMPTY_BYTES
        }
        // 数据过长，存入缓存池
        else if (bytes.size > len) {
//            Timber.d("长度$len < ${bytes.size}，将多余数据存入缓存池")
            queueRemain.write(bytes.copyOfRange(len , bytes.size))
        }

        val data = bytes.copyOfRange(index, len)
//        Timber.d("有效字节：${data.toHexString()}")
        return data
    }

    override fun bytesOfSend(bytes: ByteArray) {

    }

    companion object {
        private val EMPTY_BYTES = ByteArray(0)

//        private const val MAX_BUFFER_SIZE = 1024 * 10
    }
}