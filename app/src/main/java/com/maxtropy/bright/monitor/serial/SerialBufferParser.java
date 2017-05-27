package com.maxtropy.bright.monitor.serial;

import com.maxtropy.bright.monitor.CRC16Checker;
import com.maxtropy.bright.monitor.model.ModbusRequest;
import com.maxtropy.bright.monitor.model.ModbusResponse;

import java.nio.ByteOrder;

/**
 * Created by wusp on 2017/4/28.
 */

public class SerialBufferParser {

    /**
     * Generate a Modbus response by an input buffer.
     *
     * @param buffer
     * @return
     */
    public static ModbusResponse encodeResponse(byte[] buffer) {
        if (buffer == null) {
            return null;
        }
        short dataCount = Short.decode(Byte.toString(buffer[2]));
        byte[] data = new byte[dataCount];
        System.arraycopy(buffer, 3, data, 0, dataCount);
        return new ModbusResponse(data, dataCount, buffer[1], buffer[0]);
    }

    public static byte[] decodeRequest(ModbusRequest req, ByteOrder crcOrder) {
        if (req == null) {
            return null;
        }
        byte[] reqByte = new byte[2 + req.getStartRegister().length + req.getRegisterCount().length];
        byte[] data = new byte[reqByte.length + 2];
        reqByte[0] = req.getSlaveAddress();
        reqByte[1] = req.getFunctionCode();
        int i = 2;
        for (byte b : req.getStartRegister()) {
            reqByte[i] = b;
            i++;
        }
        for (byte b : req.getRegisterCount()) {
            reqByte[i] = b;
            i++;
        }
        byte[] crc = Utils.int2Bytes(CRC16Checker.calculate(reqByte));
        System.arraycopy(reqByte, 0, data, 0, reqByte.length);
        if (crcOrder.equals(ByteOrder.BIG_ENDIAN)) {
            System.arraycopy(crc, 2, data, reqByte.length, 2);
        } else {
            data[reqByte.length] = crc[3];
            data[reqByte.length + 1] = crc[2];
        }
        return data;
    }
}
