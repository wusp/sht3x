package com.maxtropy.ilaundry.monitor.model;

import java.nio.ByteOrder;

/**
 * Created by wusp on 2017/4/28.
 */

public final class VoltageRequest extends ModbusRequest {
    public static final byte ADDRESS = 0x02;
    public static final byte FUNCTION_CODE = 0x03;
    public static final byte START_REGISTER_HIGH = 0x00;
    public static final byte START_REGISTER_LOW = 0x0A;
    public static final byte REGISTER_COUNT_HIGH = 0x00;
    public static final byte REGISTER_COUNT_LOW = 0x02;

    public VoltageRequest() {
        slaveAddress = ADDRESS;
        functionCode = FUNCTION_CODE;
        startRegister = new byte[]{START_REGISTER_HIGH, START_REGISTER_LOW};
        registerCount = new byte[]{REGISTER_COUNT_HIGH, REGISTER_COUNT_LOW};
        crcOrder = ByteOrder.LITTLE_ENDIAN;
    }
}
