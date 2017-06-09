package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Created by Gerald on 6/4/2017.
 */

public class TumblerProgrammingDataPacket extends SerialPacket {

    final public static byte code = 0x28;

    public TumblerProgrammingDataPacket(int cycle) {
        tag = this.getClass().getName();
        data = new byte[43];
        data[0] = code;
        putShort(1, 25);    // Vend price
        putShort(3, 25);    // Coin 1
        putShort(5, 100);   // Coin 2
        putShort(7, 25);    // Start pulse
        putByte(9, 10);     // cycle time
        putByte(10, 1);     // cool down time
        putByte(11, 10);    // coin 1 topoff
        putByte(12, 40);    // coin 2 topoff
        putByte(13, 0);     // h temp
        putByte(14, 2);     // m temp
        putByte(15, 6);     // l temp
        putByte(16, 12);    // delicate temp
        putByte(17, 12);    // control conf
        putByte(18, cycle); // default cycle: 1 Heavy, 2: Normal, 3: Perm Press, 4: Delicate
    }

}
