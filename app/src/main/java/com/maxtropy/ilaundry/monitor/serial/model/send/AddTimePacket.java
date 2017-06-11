package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Created by Gerald on 6/3/2017.
 * 3.6.9
 */
public class AddTimePacket extends SerialPacket {
    public final static byte code = 0x49;

    public AddTimePacket() {
        tag = this.getClass().getName();
        data = new byte[1];
        data[0] = code;
    }
}
