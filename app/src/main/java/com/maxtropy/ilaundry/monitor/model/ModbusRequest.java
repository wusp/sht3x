package com.maxtropy.ilaundry.monitor.model;

import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by jiaoew on 16/2/26.
 */
public abstract class ModbusRequest {

    protected byte slaveAddress;

    protected byte functionCode;

    protected byte[] startRegister;

    protected byte[] registerCount;

    protected ByteOrder crcOrder;

    public final byte getFunctionCode() {
        return functionCode;
    }

    public final byte[] getRegisterCount() {
        return registerCount;
    }

    public final byte getSlaveAddress() {
        return slaveAddress;
    }

    public final byte[] getStartRegister() {
        return startRegister;
    }

    public final ByteOrder getCrcOrder() {
        return crcOrder;
    }

    @Override
    public String toString() {
        return "ModbusRequest{" +
                "crcOrder=" + crcOrder +
                ", slaveAddress=" + slaveAddress +
                ", functionCode=" + functionCode +
                ", startRegister=" + Arrays.toString(startRegister) +
                ", registerCount=" + Arrays.toString(registerCount) +
                '}';
    }
}
