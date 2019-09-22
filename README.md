[![Build Status](https://travis-ci.org/YoKeyword/Fragmentation.svg?branch=master)](https://travis-ci.org/YoKeyword/Fragmentation)
[![Download](https://api.bintray.com/packages/qdsfdhvh/maven/SerialFinger/images/download.svg)](https://bintray.com/qdsfdhvh/maven/SerialFinger/_latestVersion)
[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](https://www.apache.org/licenses/LICENSE-2.0)

# 如何使用

**1. 项目下app的build.gradle中依赖：**
````gradle
// 将x.y.z改为版本号
implementation 'com.seiko.serial:serial-finger:x.y.z'
````

**2.实现方式**
````kotlin

// 开启串口
val serial = RS232SerialPort(SerialPortPath.ttyS2, 115200)
val target = serial.toTarget()
target.iDecode = FingerDecode()
target.iFilter = SumFilter()
target.start()

// 指纹绑定
val register = RegisterModule(object : RegisterModule.Callback {
   override fun onBehavior(type: RegisterModule.Behavior, errCode: Int) {
       when (type) {
           RegisterModule.Behavior.NOT_GENERATE -> {
               // 指纹存放RamBuffer时失败
           }
           RegisterModule.Behavior.START_MERGE -> {
               // 开始合并模板...
           }
           RegisterModule.Behavior.NOT_MERGE -> {
               // 指纹合并失败
           }
           RegisterModule.Behavior.START_MATCH -> {
               // 请再次按下，进行测试...
           }
           RegisterModule.Behavior.NOT_MATCH -> {
               // 测试指纹失败
           }
           RegisterModule.Behavior.START_UP_CHAR -> {
               // 测试通过，正在获取模板数据...
           }
           RegisterModule.Behavior.NOT_UP_CHAR -> {
               // 获取指纹模板失败
           }
       }
   }

   override fun onMerge(index: Int, max: Int) {
       // 指纹验证index/count次
   }

   override fun onReceive(bytes: ByteArray) {
       // 获得一组指纹数据
   }
})
target.addSerialModule(register)

// 指纹验证
val verity = VerityModule(object : VerityModule.Callback {
    override fun onBehavior(type: VerityModule.Behavior, errCode: Int) {
        when(type) {
            VerityModule.Behavior.BAD_QUALITY -> {
                // 指纹质量不好
            }
            VerityModule.Behavior.VERITY_FAILED -> {
                // 指纹验证失败
            }
            VerityModule.Behavior.VERITY_SUCCESS -> {
                // 指纹验证成功
            }
        }
    }

    override fun onTryAgain(index: Int, max: Int) {
        // 再试一次 index/max
    }
})
target.addSerialModule(verity)

//关闭
target.close()

````

## LICENSE
````
Copyright 2019 Seiko

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
````
