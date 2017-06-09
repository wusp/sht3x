package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Created by Gerald on 6/3/2017.
 */

public class VendPricePacket extends SerialPacket {
    public final static byte code = 0x12;

    public VendPricePacket() {
        tag = "vend price";
        data = new byte[4];
        data[0] = code;
        data[3] = 1;
    }
}
