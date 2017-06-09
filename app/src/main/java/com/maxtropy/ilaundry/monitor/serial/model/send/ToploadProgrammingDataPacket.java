package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Created by Gerald on 6/4/2017.
 */

public class ToploadProgrammingDataPacket extends SerialPacket {

    final public static byte code = 0x21;

    public ToploadProgrammingDataPacket(int cycle) {
        tag = this.getClass().getName();
        data = new byte[43];
        data[0] = code;
        putShort(1, 100);    // Vend price
        putShort(3, 25);    // Coin 1
        putShort(5, 100);   // Coin 2
        putShort(7, 100);    // Start pulse
        putByte(9, 1);     // cycle length
        putByte(10, 8);     // control configuration
        putByte(11, cycle);    // default cycle
    }

}
