package com.maxtropy.ilaundry.monitor.model;

/**
 * Created by Gerald on 6/3/2017.
 */

public class VendPricePacket extends SerialPacket {
    public VendPricePacket() {
        tag = "vend price";
        data = new byte[4];
        data[0] = 0x12;
        data[3] = 1;
    }
}
