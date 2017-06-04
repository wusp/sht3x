package com.maxtropy.ilaundry.monitor.model;

/**
 * Created by Gerald on 6/3/2017.
 */

public class StatusRequestPacket extends SerialPacket {
    public StatusRequestPacket() {
        data = new byte[2];
        data[0] = 0x10;
        data[1] = 0;
    }
}
