package com.maxtropy.ilaundry.monitor.serial.model.send;

import com.maxtropy.ilaundry.monitor.serial.model.SerialPacket;

/**
 * Not such Packet on Quantum
 *
 * Created by Gerald on 6/3/2017.
 */

public class AudioBeepRequest extends SerialPacket {
    public final static byte code = 0x61;

    public AudioBeepRequest(int length) {
        tag = "vend price";
        data = new byte[2];
        data[0] = code;
        data[1] = (byte)length;
    }
}
