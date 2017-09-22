package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Created by Gerald on 6/3/2017.
 * 3.6.9
 */
public class DisplayRequest extends SerialPacket {
    public final static byte code = 0x60;

    public DisplayRequest(int d0, int d1, int d2, int d3, int duration) {
        tag = this.getClass().getName();
        data = new byte[6];
        data[0] = code;
        data[1] = (byte)d0;
        data[2] = (byte)d1;
        data[3] = (byte)d2;
        data[4] = (byte)d3;
        data[5] = (byte) duration;  //Duration of display in seconds.
    }
}
