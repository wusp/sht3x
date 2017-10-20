package com.wusp.sht3x

import com.google.android.things.pio.I2cDevice
import com.google.android.things.pio.PeripheralManagerService
import kotlinx.coroutines.experimental.CancellationException
import kotlinx.coroutines.experimental.withTimeout
import java.io.IOException
import kotlin.experimental.and
import android.util.Log

/**
 * Created by wusp on 2017/10/17.
 */
class SHT3x(val bus: String, val address: Int) : AutoCloseable {
    val device = PeripheralManagerService().openI2cDevice(bus, address)

    companion object {
        val TAG: String = SHT3x::class.java.simpleName
    }

    enum class REPEATABILITY {
        HIGH, MEDIUM, LOW
    }

    enum class ACQUISITION_MODE {
        SINGLE_SHOT, PERIODIC, ART, STOP
    }

    @Throws(IOException::class)
    override fun close() {
        device?.close()
    }

    /**
     * Calling this method to read User register, return result contains 2 byte
     * register data and the last byte CRC.
     */
    fun readUserRegister(): ByteArray {
        device?.write(byteArrayOf(0b11110011.toByte(), 0x2D), 2)
        val regState = ByteArray(3)
        device?.read(regState, 3)
        return regState
    }

    fun clearUserRegister() {
        device?.write(byteArrayOf(0x30, 0x41), 2)
    }

    fun softReset() {
        device?.write(byteArrayOf(0x30, 0xA2.toByte()), 2)
    }

    fun enableHeater() {
        device?.write(byteArrayOf(0x30, 0x6D), 2)
    }

    fun disableHeater() {
        device?.write(byteArrayOf(0x30, 0x66), 2)
    }

    /**
     * Execute measurement command to get measurement data, return null if mps is not a invalid value.
     *
     */
    fun measurementCommandBuild(mode: ACQUISITION_MODE, repeatability: REPEATABILITY,
                                enabled: Boolean, mps: Float): ByteArray? {
        if (mode == ACQUISITION_MODE.SINGLE_SHOT) {
            when (repeatability) {
                REPEATABILITY.HIGH ->
                    if (enabled) return byteArrayOf(0x2C, 0x06) else return byteArrayOf(0x24, 0x00)
                REPEATABILITY.MEDIUM ->
                    if (enabled) return byteArrayOf(0x2C, 0x0D) else return byteArrayOf(0x24, 0x0B)
                REPEATABILITY.LOW ->
                    if (enabled) return byteArrayOf(0x2C, 0x10) else return byteArrayOf(0x24, 0x16)
            }
        } else if (mode == ACQUISITION_MODE.ART) {
            return byteArrayOf(0x2B, 0x32)
        } else if (mode == ACQUISITION_MODE.PERIODIC) {
            when (repeatability) {
                REPEATABILITY.HIGH ->
                    when (mps) {
                        0.5f -> return byteArrayOf(0x20, 0x32)
                        1f -> return byteArrayOf(0x21, 0x30)
                        2f -> return byteArrayOf(0x22, 0x36)
                        4f -> return byteArrayOf(0x23, 0x34)
                        10f -> return byteArrayOf(0x27, 0x37)
                        else -> return null
                    }
                REPEATABILITY.MEDIUM ->
                    when (mps) {
                        0.5f -> return byteArrayOf(0x20, 0x24)
                        1f -> return byteArrayOf(0x21, 0x26)
                        2f -> return byteArrayOf(0x22, 0x20)
                        4f -> return byteArrayOf(0x23, 0x22)
                        10f -> return byteArrayOf(0x27, 0x21)
                        else -> return null
                    }
                REPEATABILITY.LOW ->
                    when (mps) {
                        0.5f -> return byteArrayOf(0x20, 0x2F)
                        1f -> return byteArrayOf(0x21, 0x2D)
                        2f -> return byteArrayOf(0x22, 0x2B)
                        4f -> return byteArrayOf(0x23, 0x29)
                        10f -> return byteArrayOf(0x27, 0x2A)
                        else -> return null
                    }
            }
            return null
        } else {
            return byteArrayOf(0x30, 0x93.toByte())
        }
    }

    suspend fun singleShotFetch(commandCode: ByteArray): ByteArray? {
        if (commandCode == null) return null
        val result = ByteArray(6)
        device?.write(commandCode, 2)
        kotlinx.coroutines.experimental.delay(1)
        device?.read(result, 6)
        return result
    }

    suspend fun fetchPeriodicData(): ByteArray? {
        val result = ByteArray(6)
        device?.write(byteArrayOf(0xE0.toByte(), 0x00), 2)
        kotlinx.coroutines.experimental.delay(1)
        device?.read(result, 6)
        return result
    }

    fun sendCommand(commandCode: ByteArray) {
        commandCode?.let {
            device?.write(commandCode, 2)
        }
    }
}