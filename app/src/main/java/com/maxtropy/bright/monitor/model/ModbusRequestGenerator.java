package com.maxtropy.bright.monitor.model;

/**
 * Created by wusp on 2017/4/29.
 */

public class ModbusRequestGenerator {
    public static ModbusRequest generate(byte address, byte register) {
        ModbusRequest req = null;
        if (address == HumiTempRequest.ADDRESS) {
            //上海域信
            req = new HumiTempRequest();
        } else if (address == VoltageRequest.ADDRESS) {
            //电表
            switch (register) {
                case ElectricityRequest.START_REGISTER_LOW:
                    req = new ElectricityRequest();
                    break;
                case VoltageRequest.START_REGISTER_LOW:
                    req = new VoltageRequest();
                    break;
            }
        }
        return req;
    }
}
