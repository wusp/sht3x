package com.maxtropy.ilaundry.monitor.model;

/**
 * Created by Gerald on 6/3/2017.
 */

public class VendPricePacket extends SerialPacket {
    public VendPricePacket() {
        data = new byte[2];
        data[0] = 0x12;
        data[1] = 0;
    }
}
