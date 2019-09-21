package com.seiko.serial.finger

object FingerContracts {

//    const val GD_RECORD_SIZE = 498
//    const val GD_MAX_RECORD_COUNT = 2000
//    const val ID_NOTE_SIZE = 64
//    const val MODULE_SN_LEN = 16
//
//    const val SCSI_TIMEOUT = 5000     // ms
//    const val COMM_SLEEP_TIME = 40    // ms
//
//    const val CMD_PACKET_LEN = 26
//    const val RCM_PACKET_LEN = 26
//    const val RCM_DATA_OFFSET = 10
//    const val IMAGE_DATA_UNIT = 496
//    const val MAX_DATA_LEN = 498

    private const val PACKET_SID: Byte = 1
    private const val PACKET_DID: Byte = 1

    private const val MIN_CHAR_ID = 1
    private const val MAX_CHAR_ID = 500

    /**
     * 命令包帧结构
     * PREFIX SID DID  CMD   LEN   DATA  CKS
     * 0、1   2   3    4、5  6、7  8~23  24、25
     */

    /**
     * 注册流程：
     * // CMD_GET_STATUS
     * ->
     * CMD_GET_IMAGE
     * ->
     * CMD_GENERATE(EnrollStep: Ram Buffer No)
     * ->
     * CMD_MERGE
     * ->
     * CMD_STORE_CHAR
     */

    /**
     * 检验流程：
     * CMD_GET_IMAGE
     * ->
     * CMD_GENERATE
     * ->
     * CMD_VERIFY / CMD_SEARCH
     */

    /***************************************************************************
     *                          通讯包Packet识别代码                             *
     ***************************************************************************/

    const val CMD_PREFIX_CODE = 0xAA55       // 命令包
    const val CMD_DATA_PREFIX_CODE = 0xA55A  // 相应包

    const val RCM_PREFIX_CODE = 0x55AA       // 指令数据包
    const val RCM_DATA_PREFIX_CODE = 0x5AA5  // 响应数据包

    fun getPrefMsg(pref: Int): String = when(pref) {
        CMD_PREFIX_CODE -> "命令包"
        CMD_DATA_PREFIX_CODE -> "相应包"
        RCM_PREFIX_CODE -> "指令数据包"
        RCM_DATA_PREFIX_CODE -> "响应数据包"
        else -> "未知pref = $pref"
    }

    /***************************************************************************
     *                                命令列表                                  *
     ***************************************************************************/

    const val CMD_TEST_CONNECTION = 0x0001  // 进行与设备的通讯测试
    const val CMD_SET_PARAM = 0x0002        // 设置设备参数
    const val CMD_GET_PARAM = 0x0003        // 获取设备参数
    const val CMD_GET_DEVICE_INFO = 0x0004  // 获取设备信息
//    const val CMD_ENTER_ISPMODE = 0x0005    // 将设备设置为IAP状态
    const val CMD_SET_ID_NOTE = 0x0006
    const val CMD_GET_ID_NOTE = 0x0007
    const val CMD_SET_MODULE_SN = 0x0008
    const val CMD_GET_MODULE_SN = 0x0009

    const val CMD_GET_IMAGE = 0x0020        // 从采集器中采集图像并保存到ImageBuffer中
    const val CMD_FINGER_DETECT = 0x0021    // 检测指纹输入状态
    const val CMD_UP_IMAGE = 0x0022         // 将保存于ImageBuffer的指纹图像上传给上位机
    const val CMD_DOWN_IMAGE = 0x0023       // 上位机下载图像给ImageBuffer
    const val CMD_SLED_CTRL = 0x0024        // 控制采集器背光灯开关

    const val CMD_STORE_CHAR = 0x0040        // 将指定编码的RamBuffer中得Template之策到指定编码库中
    const val CMD_LOAD_CHAR = 0x0041         // 读取库中指定编码中得Template到指定编号的RamBuffer
    const val CMD_UP_CHAR = 0x0042           // 将保存于指定编号的RamBuffer中得Template上传给上位机
    const val CMD_DOWN_CHAR = 0x0043         // 从上位机下载的Template存到指定编号的RamBuffer中
    const val CMD_DEL_CHAR = 0x0044          // 删除指定编号内的Template
    const val CMD_GET_EMPTY_ID = 0x0045      // 获取指定范围内可注册的第一个模板编号
    const val CMD_GET_STATUS = 0x0046        // 获取指定编号的模板状态
    const val CMD_GET_BROKEN_ID = 0x0047     // 检查指定编号范围内的所有模板是否存在损坏情况
    const val CMD_GET_ENROLL_COUNT = 0x0048  // 获取指定编号范围内的已注册的模板个数

    const val CMD_GENERATE = 0x0060  // 将ImageBuffer中得指纹图像生成模板数据，并存到指定编号的RamBuffer中
    const val CMD_MERGE = 0x0061     // 将保存于RamBuffer中的2~3个模板数据融合成一个模板数据
    const val CMD_MATCH = 0x0062     // 指定RamBuffer中的两个指纹模板之间进行1:1比对
    const val CMD_SEARCH = 0x0063    // 指定RamBuffer中的模板与指纹库中指定编号范围内所有模板进行1:N比对
    const val CMD_VERIFY = 0x0064    // 指定RamBuffer中得指纹模板与指纹库中指定编号的指纹模板进行1:1比对
    const val CMD_GET_ENROLLED_ID_LIST = 0x0049 // 获取已注册的UserID列表

    const val RCM_INCORRECT_COMMAND = 0x00FF

    fun getCmdMsg(cmd: Int): String = when(cmd) {
        CMD_TEST_CONNECTION -> "测试通讯"
        CMD_SET_PARAM -> "设置设备参数"
        CMD_GET_PARAM -> "获取设备参数"
        CMD_GET_DEVICE_INFO -> "获取设备信息"
        CMD_GET_IMAGE -> "采集图像"
        CMD_FINGER_DETECT -> "检测指纹输入"
        CMD_UP_IMAGE -> "从ImageBuffer中获取图像"
        CMD_DOWN_IMAGE -> "写入图像到ImageBuffer"
        CMD_SLED_CTRL -> "控制采集器背光灯开关"

        CMD_STORE_CHAR -> "将指定RamBuffer中的指纹图像存到指定Template中"
        CMD_LOAD_CHAR -> "读取库中指定编码中得Template到指定编号的RamBuffer"
        CMD_UP_CHAR -> "将保存于指定编号的RamBuffer中得Template上传给上位机"
        CMD_DOWN_CHAR -> "从上位机下载的Template存到指定编号的RamBuffer中"
        CMD_DEL_CHAR -> "删除指定编号内的Template"
        CMD_GET_EMPTY_ID -> "获取指定范围内可注册的第一个模板编号"
        CMD_GET_STATUS -> "获取指定编号的模板状态"
        CMD_GET_BROKEN_ID -> "检查指定编号范围内的所有模板是否存在损坏情况"
        CMD_GET_ENROLL_COUNT -> "获取指定编号范围内的已注册的模板个数"

        CMD_GENERATE -> "将ImageBuffer中得指纹图像生成模板数据，并存到指定编号的RamBuffer中"
        CMD_MERGE -> "将保存于RamBuffer中的2~3个模板数据融合成一个模板数据"
        CMD_MATCH -> "指定RamBuffer中的两个指纹模板之间进行1:1比对"
        CMD_SEARCH -> "指定RamBuffer中的模板与指纹库中指定编号范围内所有模板进行1:N比对"
        CMD_VERIFY -> "指定RamBuffer中得指纹模板与指纹库中指定编号的指纹模板进行1:1比对"
        CMD_GET_ENROLLED_ID_LIST -> "获取已注册的UserID列表"
        else -> "未知cmd = $cmd"
    }

    /***************************************************************************
     *                              Error Code                                 *
     ***************************************************************************/

    const val ERR_SUCCESS = 0x00              // 指令处理成功
    private const val ERR_FAIL = 0x01                 // 指令处理失败
    private const val ERR_VERIFY = 0x10               // 与指定编号中得Template的1:1比对失败
    private const val ERR_IDENTIFY = 0x11             // 已进行1:N比对，但相同Template不存在
    private const val ERR_TMPL_EMPTY = 0x12           // 再指定编号中不存在已注册的Template
    private const val ERR_TMPL_NOT_EMPTY = 0x13       // 再指定编号汇总已存在Template
    private const val ERR_ALL_TMPL_EMPTY = 0x14       // 不存在已注册的Template
    private const val ERR_EMPTY_ID_NOEXIST = 0x15     // 不存在可注册的Template ID
    private const val ERR_BROKEN_ID_NOEXIST = 0x16    // 不存在已损坏的Template
    private const val ERR_INVALID_TMPL_DATA = 0x17    // 指定的Template Data无效
    private const val ERR_DUPLICATION_ID = 0x18       // 该指纹已注册
    const val ERR_BAD_QUALITY = 0x19          // 指纹图像质量不好
    private const val ERR_MERGE_FAIL = 0x1A           // Template合成失败
    private const val ERR_NOT_AUTHORIZED = 0x1B       // 没有进行通讯密码确认
    private const val ERR_MEMORY = 0x1C               // 外部Flash烧写出错
    private const val ERR_INVALID_TMPL_NO = 0x1D      // 指定Template编号无效
    private const val ERR_INVALID_PARAM = 0x22        // 使用了不正确的参数
    private const val ERR_GEN_COUNT = 0x25            // 指纹合成个数无效
    private const val ERR_INVALID_BUFFER_ID = 0x26    // Buffer ID值不正确
    const val ERR_FP_NOT_DETECTED = 0x28      // 指令被取消


    fun getErrMsg(code: Int): String = when(code) {
        ERR_SUCCESS -> "指令处理成功"
        ERR_FAIL -> "指令处理失败"
        ERR_VERIFY -> "与指定编号中得Template的1:1比对失败"
        ERR_IDENTIFY -> "已进行1:N比对，但相同Template不存在"
        ERR_TMPL_EMPTY -> "再指定编号中不存在已注册的Template"
        ERR_TMPL_NOT_EMPTY -> "再指定编号汇总已存在Template"
        ERR_ALL_TMPL_EMPTY -> "不存在已注册的Template"
        ERR_EMPTY_ID_NOEXIST -> "不存在可注册的Template ID"
        ERR_BROKEN_ID_NOEXIST -> "不存在已损坏的Template"
        ERR_INVALID_TMPL_DATA -> "指定的Template Data无效"
        ERR_DUPLICATION_ID -> "该指纹已注册"
        ERR_BAD_QUALITY -> "指纹图像质量不好"
        ERR_MERGE_FAIL -> "Template合成失败"
        ERR_NOT_AUTHORIZED -> "没有进行通讯密码确认"
        ERR_MEMORY -> "外部Flash烧写出错"
        ERR_INVALID_TMPL_NO -> "指定Template编号无效"
        ERR_INVALID_PARAM -> "使用了不正确的参数"
        ERR_GEN_COUNT -> "指纹合成个数无效"
        ERR_INVALID_BUFFER_ID -> "Buffer ID值不正确"
        ERR_FP_NOT_DETECTED -> "指令被取消"
        else -> "未知代码：$code"
    }

    /***************************************************************************
     * Parameter Index
     */
    const val DP_DEVICE_ID = 0
    const val DP_SECURITY_LEVEL = 1
    const val DP_DUP_CHECK = 2
    const val DP_BAUDRATE = 3
    const val DP_AUTO_LEARN = 4

    /***************************************************************************
     * Device ID, Security Level
     */
    const val MIN_DEVICE_ID = 1
    const val MAX_DEVICE_ID = 255
    const val MIN_SECURITY_LEVEL = 1
    const val MAX_SECURITY_LEVEL = 5

    const val GD_TEMPLATE_NOT_EMPTY = 0x01
    const val GD_TEMPLATE_EMPTY = 0x00

    /***************************************************************************
     *                              ByteArray                                  *
     ***************************************************************************/

    /**
     * 读取超时时间
     * 0: Device ID
     * 1: Security Level
     * 2: BaudRate
     * 3: Duplication Check
     * 4: Auto Learn
     * 5: FP TimeOu
     */
//    fun readTimeOut(): ByteArray {
//        return bind(CMD_GET_PARAM, 5.toModBus2Bytes(2))
//    }

//    fun readDeviceInfo(): ByteArray {
//        return bind(CMD_GET_DEVICE_INFO)
//    }

    /**
     * 将ImageBuffer中生成的模板保存在RamBuffer中，
     * 0: RamBuffer0、1：RamBuffer1、2：RamBuffer2
     */
    fun generate(num: Int): ByteArray {
        return bind(
            CMD_GENERATE,
            num.toModBus2Bytes(2)
        )
    }

    /**
     * 将2/3个 RamBuffer中得指纹合并，并存到RamBuffer0
     * @param num 需要合并的数量，默认3
     */
    fun merge(num: Int): ByteArray {
        return bind(
            CMD_MERGE,
            byteArrayOf(0, 0, num.toByte())
        )
    }

    /**
     * 将两个RamBuffer中的指纹模板进行比较
     */
    fun match(num1: Int = 0, num2: Int = 1): ByteArray {
        return bind(
            CMD_MATCH,
            num1.toModBus2Bytes() + num2.toModBus2Bytes()
        )
    }

    /**
     * 将RamBuffer0的指纹存入XX Temple中
     * @param id Temple 编号
     */
    fun storeChar(id: Int): ByteArray {
        return bind(
            CMD_STORE_CHAR,
            id.toModBus2Bytes() + byteArrayOf(0, 0)
        )
    }

    /**
     * 将XX Temple中得模板存放在RamBuffer0中
     */
    fun loadChar(id: Int): ByteArray {
        return bind(
            CMD_LOAD_CHAR,
            id.toModBus2Bytes() + byteArrayOf(0, 0)
        )
    }

    /**
     * 指纹模块(RamBuffer0) -> 上位机
     */
    fun upChar(): ByteArray {
        return bind(
            CMD_UP_CHAR,
            byteArrayOf(0, 0)
        )
    }

    fun downCharHead(size: Int): ByteArray {
        return bind(
            CMD_DOWN_CHAR,
            (size + 2).toModBus2Bytes()
        )
    }

    /**
     * 将Temple 存入 RamBuffer0
     */
    fun downChar(bytes: ByteArray): ByteArray {
        return bind(
            CMD_DOWN_CHAR,
            byteArrayOf(0, 0) + bytes
        )
    }

    /**
     * 将RamBuffer0中得数据与指定范围内的指纹对比
     */
    fun search(start: Int = MIN_CHAR_ID, end: Int = MAX_CHAR_ID): ByteArray {
        return bind(
            CMD_SEARCH,
            byteArrayOf(0, 0) + start.toModBus2Bytes() + end.toModBus2Bytes()
        )
    }

//    /**
//     * 指纹模块(ImageBuffer) -> 上位机
//     * 0-完整的图像， 1-1/4图像
//     */
//    fun upImage(): ByteArray {
//        return bind(CMD_UP_IMAGE, byteArrayOf(1))
//    }
//
//    /**
//     * 上位机 -> 指纹模块(ImageBuffer)
//     * 下载
//     */
//    fun downImage(): List<ByteArray> {
//        return listOf(ByteArray(0))
//    }

    /**********************************************************************/

    /**
     * 获取第一个未注册的Temple ID
     */
    fun getEmptyId(start: Int = MIN_CHAR_ID, end: Int = MAX_CHAR_ID): ByteArray {
        return bind(
            CMD_GET_EMPTY_ID,
            start.toModBus2Bytes() + end.toModBus2Bytes()
        )
    }

    /**
     * 检查Temple ID 是否被注册
     */
    fun checkId(id: Int): ByteArray {
        return bind(
            CMD_GET_STATUS,
            id.toModBus2Bytes()
        )
    }

    /**
     * 删除指定位置的指纹
     */
    fun delChars(start: Int = MIN_CHAR_ID, end: Int = MAX_CHAR_ID): ByteArray {
        return bind(
            CMD_DEL_CHAR,
            start.toModBus2Bytes() + end.toModBus2Bytes()
        )
    }

    /**********************************************************************/

    fun bind(cmd: Int, data: ByteArray? = null): ByteArray {
        // 数据字节长度， PS：< 16时 =16, 但是发送的 数据长度字节 还是实际长度(bytes.size)
        val size: Int
        // 包头
        val array1: ByteArray
        // 根据实际数据长度，确定包头
        if (data == null || data.size <= 16) {
            size = 16
            array1 = CMD_PREFIX_CODE.toModBus2Bytes(2)
        } else {
            size = data.size
            array1 = CMD_DATA_PREFIX_CODE.toModBus2Bytes(2)
        }
        // 设备信息
        val array2 = byteArrayOf(
            PACKET_SID,
            PACKET_DID
        )
        // 指令
        val array3 = cmd.toModBus2Bytes(2)
        // 数据长度字节
        val array4: ByteArray
        // 数据字节
        val array5 = ByteArray(size)
        if (data != null) {
            array4 = data.size.toModBus2Bytes(2)
            data.forEachIndexed { i, it -> array5[i] = it }
        } else {
            array4 = 0.toModBus2Bytes(2)
        }
        return (array1 + array2 + array3 + array4 + array5).toAddSum()
    }

}