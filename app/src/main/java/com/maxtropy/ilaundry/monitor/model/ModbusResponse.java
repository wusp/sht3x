package com.maxtropy.ilaundry.monitor.model;

/**
 * Created by jiaoew on 16/2/26.
 */
public class ModbusResponse {

    private byte slaveAddress;

    private byte functionCode;

    private short dataCount;

    private byte[] data;

    public ModbusResponse(byte[] data, short dataCount, byte functionCode, byte slaveAddress) {
        this.dataCount = dataCount;
        this.data = data;
        this.functionCode = functionCode;
        this.slaveAddress = slaveAddress;
        if (this.dataCount != data.length) {
            throw new IllegalArgumentException("dataCount is not equal to data's length.");
        }
    }

    public byte[] getData() {
        return data;
    }

    public short getDataCount() {
        return dataCount;
    }

    public byte getFunctionCode() {
        return functionCode;
    }

    public byte getSlaveAddress() {
        return slaveAddress;
    }
}
