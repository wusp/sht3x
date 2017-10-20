# SHT3x driver for Android Things
================================

Android Things driver for Sensirion [STH3x][sht3x] series humidity and temperature sensor

NOTE: these drivers are not production-ready. They are offered as sample implementations of Android Things user space drivers for common peripherals as part of the Developer Preview release. There is no guarantee of correctness, completeness or robustness.

How to use the driver
---------------------

## Gradle dependency
To use the this driver, simply add the line below to your module's `build.gradle`.

```
dependencies {
    compile 'com.wusp.androidthings:sht3x:0.0.2'
}
```

This driver also need [kotlinx.coroutines][kotlinx.coroutines], add kotlinx.coroutines to module's `build.gradle` too.

```
dependencies {
    compile 'org.jetbrains.kotlinx:kotlinx-coroutines-core:0.17'
}
```

## Data Acquisition Mode
### Single Shot
In this mode, MCU should send a Single Shot command to trigger measurement, then the sensor would send back data results after measurement.
It's important for MCU to delay a few milliseconds before reading data result from sensor after sending command.
Call ` singleShotFetch(...) : ByteArray` to fetch Single Shot data, that API include such delay:

```
device?.write(commandCode, 2)
kotlinx.coroutines.experimental.delay(1)    //delay 1 ms
device?.read(result, 6)
```

### Periodic
MCU could send Measurement command to start Periodic mode and set the data measurement frequency `0.5\1\2\4\10 measurements per second`.
After sensor periodic mode is running, MCU could use Fetch Data command to read out data from sensor data cache memory.

NOTE: MCU should not fetch data from sensor in a higher frequency than sensor measurement frequency. Because each time the data was fetched,
the sensor would clear the memory. The I2cDevice would throw `ServiceSpecificException` on fetching data from empty memory until next measurement fill in the data memory.

### ART
SHT3x sensor supports ART(accelerated response time) feature that works like Periodic mode except to acquiring data in frequency of 4Hz.
MCU could send ART command to active it.

## CRC
MCU should use CRC8-maxim to check response data integrity, this library provide API to generate CRC:

```
val data = device.readUserRegister()
val crc = crc8maxim(byteArrayOf(data[0], data[1]))      //0-MSB 1-LSB 2-CRC
```

## Humidity & Temperature Conversion
Use `convertToTemperature(byteArrayOf(Temperature MSB, Temperature LSB))` to convert response data to temperature,
and `convertToHumidity(byteArrayOf(Humidity MSB, Humidity LSB))` to convert response data to humidity.

## Sample usage
```
        //Assign i2c bus and sensor address (according to ADDR Pin)
        val device = SHT3x(p.i2cBusList[0], 0x44)

        //1. Periodic mode
        val command = device.measurementCommandBuild(SHT3x.ACQUISITION_MODE.PERIODIC, SHT3x.REPEATABILITY.LOW, false, 2f)   //generate command
        command?.let {
            //Stop periodic measurement before setting.
            device.sendCommand(device.measurementCommandBuild(SHT3x.ACQUISITION_MODE.STOP, SHT3x.REPEATABILITY.LOW, false, -1f)!!)
            //send periodic command
            device.sendCommand(it)
            runBlocking {
                repeat(10) {
                    Log.d(Const.TAG, "mission: " + it)
                    val content = device.fetchPeriodicData()    //fetch Data
                    content?.let {
                        Log.d(Const.TAG, " temperature: " + convertToTemperature(byteArrayOf(it[0], it[1]))
                                + " humidity: " + convertToHumidity(byteArrayOf(it[3], it[4])))
                    }
                    delay(600)  //bigger than (1 / periodic mps) to prevent ServiceSpecificException
                }
            }
        }

        //2. Single Shot mode
        val singleShot = device.measurementCommandBuild(SHT3x.ACQUISITION_MODE.SINGLE_SHOT, SHT3x.REPEATABILITY.HIGH, false, -1f)   //generate command
        if (singleShot != null) {
            Log.d(Const.TAG, "execute command: " + singleShot[0] + " " + singleShot[1].toInt().and(0xFF))
            runBlocking {
                val data = device.singleShotFetch(singleShot)
                data?.let {
                    Log.d(Const.TAG, " temperature's crc: " + crc8maxim(byteArrayOf(data[0], data[1])) + " CRC: " + data[2].toInt().and(0xFF)
                            + " humidity's crc: " + crc8maxim(byteArrayOf(data[3], data[4])) +  " CRC: " + data[5].toInt().and(0xFF))   //byte to positive Int
                    Log.d(Const.TAG, " temperature: " + convertToTemperature(byteArrayOf(data[0], data[1])) + " humidity: " + convertToHumidity(byteArrayOf(data[3], data[4])))
                }
            }
        }
```

License
-------

Licensed to the Apache Software Foundation (ASF) under one or more contributor
license agreements.  See the NOTICE file distributed with this work for
additional information regarding copyright ownership.  The ASF licenses this
file to you under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License.  You may obtain a copy of
the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
License for the specific language governing permissions and limitations under
the License.

[sht3x]:https://www.sensirion.com/fileadmin/user_upload/customers/sensirion/Dokumente/2_Humidity_Sensors/Sensirion_Humidity_Sensors_SHT3x_Datasheet_digital.pdf
[kotlinx.coroutines]: https://github.com/Kotlin/kotlinx.coroutines