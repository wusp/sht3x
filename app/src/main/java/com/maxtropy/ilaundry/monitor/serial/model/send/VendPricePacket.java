package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.Global;
import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

import static com.maxtropy.ilaundry.monitor.Global.SystemType.MDC;

/**
 * Should be used on CR driven vend only.
 *
 * Created by Gerald on 6/3/2017.
 */

public class VendPricePacket extends SerialPacket {

    public VendPricePacket() {
        tag = "vend price";
        switch (Global.systemType) {
            case MDC:
                data = new byte[4];
                data[0] = getCode();
                putShort(1, Global.vendPrice);
                data[3] = 0;
                break;
            default:
                data = new byte[7];
                data[0] = getCode();
                putShort(1, Global.vendPrice);
                putShort(3, 0);
                data[5] = 0;
                data[6] = 0;
        }
    }

    public byte getCode() {
        return (byte)(Global.systemType == MDC ? 0x12 : 0x18);
    }

}
