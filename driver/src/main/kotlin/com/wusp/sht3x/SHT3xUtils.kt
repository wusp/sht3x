package com.wusp.sht3x

/**
 * Created by wusp on 2017/10/17.
 */
//accoring to doc. P(x)=x^8+x^5+x^4+1 = 100110001 is CRC8-MAXIM
fun crc8maxim(datas: ByteArray): Int? {
    if (datas.isEmpty()) return null
    val poly = 0x131 //100110001
    var crc = 0xFF //Doc. initialization is 0
    datas.forEach { data ->
        crc = crc.xor(data.toInt())
        for (it in 1..8) {
            crc = crc and 0xFF
            crc = if (crc > 0x80) (crc.shl(1).xor(poly)) else crc.shl(1)
        }
    }
    return crc
}

/**
 * Convert the sensor response raw data to humidity data,
 * return -1 if error occurs.
 */
fun convertToHumidity(datas: ByteArray): Float {
    if (datas.isEmpty()) return -1f
    var rawData: Float = (datas[0].toInt().and(0xFF).shl(8) + datas[1].toInt().and(0xFF)).toFloat()
    rawData *= 100
    rawData /= (2.shl(15) - 1)
    return rawData
}

/**
 * Convert the sensor response raw data to temperature data in degrees centigrade
 *
 * return -1 if error occurs.
 */
fun convertToTemperature(datas: ByteArray): Float {
    if (datas.isEmpty()) return -1f
    var rawData: Float = (datas[0].toInt().and(0xFF).shl(8) + datas[1].toInt().and(0xFF)).toFloat()
    rawData *= 175f
    rawData /= (2.shl(15) - 1)
    return rawData - 45f
}
